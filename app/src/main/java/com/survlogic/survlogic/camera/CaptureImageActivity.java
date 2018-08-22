package com.survlogic.survlogic.camera;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.ImageFormat;
import android.graphics.Point;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.location.Location;
import android.media.Image;
import android.media.ImageReader;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.survlogic.survlogic.ARvS.services.GnssService;
import com.survlogic.survlogic.ARvS.services.SensorService;
import com.survlogic.survlogic.R;
import com.survlogic.survlogic.camera.util.AutoFitTextureView;
import com.survlogic.survlogic.camera.util.CameraUtils;
import com.survlogic.survlogic.camera.util.CaptureImageSettings;
import com.survlogic.survlogic.utils.FileHelper;
import com.survlogic.survlogic.utils.PreferenceLoaderHelper;
import com.survlogic.survlogic.utils.StringUtilityHelper;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import mehdi.sakout.fancybuttons.FancyButton;

public class CaptureImageActivity extends AppCompatActivity{
    private static SparseIntArray ORIENTATIONS=new SparseIntArray();

    private Context mContext;

    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 90);
        ORIENTATIONS.append(Surface.ROTATION_90,0);
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }

    private String cameraId;
    private CameraDevice cameraDevice;
    private CameraCaptureSession cameraCaptureSessions;
    private CaptureRequest.Builder captureRequestBuilder;
    private Size imageDimension;
    private ImageReader imageReader;

    private File file;
    private static final int REQUEST_CAMERA_PERMISSION = 200;

    private Handler mBackgroundHandler;
    private HandlerThread mBackgroundThread;

    private AutoFitTextureView textureView;
    private FancyButton btCapture;
    private ImageButton ibMetadataInfo;

    private ProgressBar progressBar;

    private Semaphore mCameraOpenCloseLock = new Semaphore(1);

    private static final int MAX_PREVIEW_WIDTH = 1920;
    private static final int MAX_PREVIEW_HEIGHT = 1080;
    private int mSensorOrientation;

    //----------------------------------------------------------------------------------------------
    //Preferences
    private PreferenceLoaderHelper preferenceLoaderHelper;
    private boolean prefUsePredictedLocation = false;
    private boolean prefUseSensorGMF = false;
    private boolean prefIsGPSOn = true;

    //Location Service
    public GnssService locationService;
    private BroadcastReceiver locationUpdateReceiver;
    private BroadcastReceiver predictedLocationReceiver;

    //Sensor Service
    public SensorService sensorService;
    private BroadcastReceiver sensorDataReceiver;
    private BroadcastReceiver sensorGmfDataReceiver;
    private BroadcastReceiver sensorAccuracyReceiver;

    //Location Variables
    private boolean hasLocation = false, hasFilteredLocation = false;
    private int gpsRawCount=0, gpsFilteredCount =0;
    private boolean locationLoadedSuccess = false;

    //Sensor Variables
    private boolean sendSensorWarning = false;
    private boolean useFusionSensor = false;
    private float mSensorAlpha = 0.125f;
    private boolean hasGMFSensorLocationSent = false;

    //System Variables
    private int criteriaGpsFilteredCount = 1, criteriaGpsRawCount = 1;
    private static final boolean LOCATION_RAW = false, LOCATION_PREDICT = true;
    private static final boolean SENSOR_RAW = false, SENSOR_GMF = true;

    //Output Variables;
    private Bitmap mCurrentPhoto;
    private Location mCurrentLocation, mCurrentLocationRaw;
    private int mSensorAzimuth = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.camera_fragment);

        mContext = this;
        preferenceLoaderHelper = new PreferenceLoaderHelper(mContext);

        bindLocationService();
        bindSensorService();

        initViewWidgets();
        loadPreferences();

        makeUserWait();

    }


    private void loadPreferences() {
        prefUsePredictedLocation = preferenceLoaderHelper.getGPSLocationUsePrediction();
        useFusionSensor = preferenceLoaderHelper.getGPSLocationSensorUseFusion();
        prefUseSensorGMF = preferenceLoaderHelper.getGPSLocationSensorUseGMF();
        prefIsGPSOn = preferenceLoaderHelper.getCameraLocationUse();
    }

    private void initViewWidgets(){

        ImageButton ibBack = findViewById(R.id.button_back);
        ibBack.setVisibility(View.VISIBLE);
        ibBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });


        ImageButton ibSettings = findViewById(R.id.button_settings_right);
        ibSettings.setVisibility(View.VISIBLE);
        ibSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(CaptureImageActivity.this,CaptureImageSettings.class));
            }
        });

        progressBar = findViewById(R.id.progress_bar_camera);

        textureView=findViewById(R.id.texture_view);
        btCapture =findViewById(R.id.btn_take_picture);

        textureView.setSurfaceTextureListener(textureListener);

        btCapture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(setFileName()){
                    takePicture();
                }
            }
        });

        ibMetadataInfo = findViewById(R.id.info);
        ibMetadataInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showMetaData();
            }
        });
    }


    private void makeUserWait(){
        btCapture.setEnabled(false);
        progressBar.setVisibility(View.VISIBLE);

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                btCapture.setEnabled(true);
                progressBar.setVisibility(View.GONE);
            }
        },1000);


    }

    //----------------------------------------------------------------------------------------------//

    private void showMetaData(){
        Activity activity = (Activity) mContext;

        DecimalFormat df0 = StringUtilityHelper.createUSNonBiasDecimalFormatSelect(0);
        DecimalFormat df2 = StringUtilityHelper.createUSNonBiasDecimalFormatSelect(2);
        DecimalFormat df4 = StringUtilityHelper.createUSNonBiasDecimalFormatSelect(4);

        String locationRawStatus, locationAdjStatus, sensorStatus;


        if(prefIsGPSOn){
            if(!hasLocation){
                locationRawStatus = getResources().getString(R.string.camera_metadata_message_location_raw_no_data);
            }else{

                try{
                    locationRawStatus = getResources().getString(R.string.camera_metadata_message_location_raw_data,
                            df4.format(mCurrentLocationRaw.getLatitude()),
                            df4.format(mCurrentLocationRaw.getLongitude()),
                            df2.format(mCurrentLocationRaw.getAccuracy()));
                }catch(Exception ex){
                    locationRawStatus =getResources().getString(R.string.camera_metadata_message_location_raw_error);

                }

            }

            if(!hasFilteredLocation){
                locationAdjStatus = getResources().getString(R.string.camera_metadata_message_location_predict_no_data);
            }else{
                try{
                    locationAdjStatus = getResources().getString(R.string.camera_metadata_message_location_predict_data,
                            df4.format(mCurrentLocation.getLatitude()),
                            df4.format(mCurrentLocation.getLongitude()),
                            df2.format(mCurrentLocation.getAccuracy()));
                }catch(Exception ex){
                    locationAdjStatus = getResources().getString(R.string.camera_metadata_message_location_predict_error);
                }

            }


        }else {
            locationRawStatus = getResources().getString(R.string.camera_metadata_message_location_raw_off);
            locationAdjStatus = getResources().getString(R.string.camera_metadata_message_location_predict_off);

        }


        try{
            sensorStatus = getResources().getString(R.string.camera_metadata_message_sensor_data,
                    df0.format(mSensorAzimuth));
        }catch (Exception ex){
            sensorStatus = getResources().getString(R.string.camera_metadata_message_sensor_error);
        }


        String metadata = getResources().getString(R.string.camera_metadata_message_intro) +
                locationRawStatus +
                locationAdjStatus +
                sensorStatus;


        if (null != activity) {
            new AlertDialog.Builder(activity)
                    .setMessage(metadata)
                    .setPositiveButton(android.R.string.ok, null)
                    .show();
        }
    }



    //----------------------------------------------------------------------------------------------Camera Methods
    TextureView.SurfaceTextureListener textureListener=new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            openCamera(width,height);
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
            if (null != textureView || null == imageDimension) {
                textureView.setTransform(CameraUtils.configureTransform(width, height,imageDimension,CaptureImageActivity.this));
            }
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {

        }
    };

    CameraDevice.StateCallback stateCallback=new CameraDevice.StateCallback() {
        @Override
        public void onOpened(CameraDevice camera) {

            Log.e("tag", "onOpened");
            mCameraOpenCloseLock.release();
            cameraDevice = camera;
            createCameraPreview();
        }

        @Override
        public void onDisconnected(CameraDevice camera) {
            mCameraOpenCloseLock.release();
            cameraDevice.close();
        }

        @Override
        public void onError(CameraDevice camera, int error) {
            mCameraOpenCloseLock.release();
            cameraDevice.close();
            cameraDevice = null;
        }
    };
    private void openCamera(int width,int height) {
        CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        Log.e("tag", "is camera open");

        try {
            if (!mCameraOpenCloseLock.tryAcquire(2500, TimeUnit.MILLISECONDS)) {
                throw new RuntimeException("Time out waiting to lock camera opening.");
            }
            cameraId = manager.getCameraIdList()[0];
            CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
            StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            assert map != null;

            Size largest = Collections.max(
                    Arrays.asList(map.getOutputSizes(ImageFormat.JPEG)),
                    new CameraUtils.CompareSizesByArea());


            int displayRotation = getWindowManager().getDefaultDisplay().getRotation();
            //noinspection ConstantConditions
            mSensorOrientation = characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
            boolean swappedDimensions = false;
            switch (displayRotation) {
                case Surface.ROTATION_0:
                case Surface.ROTATION_180:
                    if (mSensorOrientation == 90 || mSensorOrientation == 270) {
                        swappedDimensions = true;
                    }
                    break;
                case Surface.ROTATION_90:
                case Surface.ROTATION_270:
                    if (mSensorOrientation == 0 || mSensorOrientation == 180) {
                        swappedDimensions = true;
                    }
                    break;
                default:
                    Log.e("tag", "Display rotation is invalid: " + displayRotation);
            }

            Point displaySize = new Point();
            getWindowManager().getDefaultDisplay().getSize(displaySize);
            int rotatedPreviewWidth = width;
            int rotatedPreviewHeight = height;
            int maxPreviewWidth = displaySize.x;
            int maxPreviewHeight = displaySize.y;

            if (swappedDimensions) {
                rotatedPreviewWidth = height;
                rotatedPreviewHeight = width;
                maxPreviewWidth = displaySize.y;
                maxPreviewHeight = displaySize.x;
            }

            if (maxPreviewWidth > MAX_PREVIEW_WIDTH) {
                maxPreviewWidth = MAX_PREVIEW_WIDTH;
            }

            if (maxPreviewHeight > MAX_PREVIEW_HEIGHT) {
                maxPreviewHeight = MAX_PREVIEW_HEIGHT;
            }

            imageDimension = CameraUtils.chooseOptimalSize(map.getOutputSizes(SurfaceTexture.class),
                    rotatedPreviewWidth, rotatedPreviewHeight, maxPreviewWidth,
                    maxPreviewHeight, largest);

            int orientation = getResources().getConfiguration().orientation;
            if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                textureView.setAspectRatio(
                        imageDimension.getWidth(), imageDimension.getHeight());
            } else {
                textureView.setAspectRatio(
                        imageDimension.getHeight(), imageDimension.getWidth());
            }

            if (null != textureView || null == imageDimension) {
                textureView.setTransform(CameraUtils.configureTransform(width, height,imageDimension,CaptureImageActivity.this));
            }

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(CaptureImageActivity.this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CAMERA_PERMISSION);
                return;
            }
            manager.openCamera(cameraId, stateCallback, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Log.e("tag", "openCamera X");
    }
    protected void createCameraPreview() {
        try {
            SurfaceTexture texture = textureView.getSurfaceTexture();
            assert texture != null;
            texture.setDefaultBufferSize(imageDimension.getWidth(), imageDimension.getHeight());
            Surface surface = new Surface(texture);
            captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            captureRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE,CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);
            captureRequestBuilder.addTarget(surface);
            cameraDevice.createCaptureSession(Arrays.asList(surface), new CameraCaptureSession.StateCallback(){
                @Override
                public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                    //The camera is already closed
                    if (null == cameraDevice) {
                        return;
                    }
                    // When the session is ready, we start displaying the preview.
                    cameraCaptureSessions = cameraCaptureSession;
                    updatePreview();
                }
                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
                    Toast.makeText(CaptureImageActivity.this, "Configuration change", Toast.LENGTH_SHORT).show();
                }
            }, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }
    protected void updatePreview() {
        if(null == cameraDevice) {
            Log.e("tag", "updatePreview error, return");
        }
        captureRequestBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
        try {
            cameraCaptureSessions.setRepeatingRequest(captureRequestBuilder.build(), null, mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }
    protected void startBackgroundThread() {
        mBackgroundThread = new HandlerThread("Camera Background");
        mBackgroundThread.start();
        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
    }
    protected void stopBackgroundThread() {
        mBackgroundThread.quitSafely();
        try {
            mBackgroundThread.join();
            mBackgroundThread = null;
            mBackgroundHandler = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    private void closeCamera(){
        try {
            mCameraOpenCloseLock.acquire();
            if (null != cameraCaptureSessions) {
                cameraCaptureSessions.close();
                cameraCaptureSessions = null;
            }
            if (null != cameraDevice) {
                cameraDevice.close();
                cameraDevice = null;
            }
            if (null != imageReader) {
                imageReader.close();
                imageReader = null;
            }
        } catch (InterruptedException e) {
            throw new RuntimeException("Interrupted while trying to lock camera closing.");
        } finally {
            mCameraOpenCloseLock.release();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                // close the app
                Toast.makeText(CaptureImageActivity.this, "Sorry!!!, you can't use this app without granting permission", Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        Log.e("tag", "onResume");
        startBackgroundThread();
        if (textureView.isAvailable()) {
            openCamera(textureView.getWidth(),textureView.getHeight());
        } else {
            textureView.setSurfaceTextureListener(textureListener);
        }

        registerLocationReceiver();
        registerSensorReceiver();

        loadPreferences();
    }
    @Override
    protected void onPause() {
        Log.e("tag", "onPause");
        closeCamera();
        stopBackgroundThread();


        unregisterLocationReceiver();
        unregisterSensorReceiver();

        super.onPause();
    }

    private boolean setFileName(){

        FileHelper fileHelper = new FileHelper(mContext);
        String path = fileHelper.getPathToFolder(0);

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "RAW_" + timeStamp;

        File folder = new File(path);
        file = new File(path,imageFileName + ".jpg");

        return true;
    }


    protected void takePicture() {
        if(null == cameraDevice) {
            Log.e("tag", "cameraDevice is null");
            return;
        }
        CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraDevice.getId());
            Size[] jpegSizes = null;
            if (characteristics != null) {
                jpegSizes = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP).getOutputSizes(ImageFormat.JPEG);
            }
            int width = 640;
            int height = 480;
            if (jpegSizes != null && 0 < jpegSizes.length) {
                width = jpegSizes[0].getWidth();
                height = jpegSizes[0].getHeight();
            }
            ImageReader reader = ImageReader.newInstance(width, height, ImageFormat.JPEG, 1);
            List<Surface> outputSurfaces = new ArrayList<Surface>(2);
            outputSurfaces.add(reader.getSurface());
            outputSurfaces.add(new Surface(textureView.getSurfaceTexture()));
            final CaptureRequest.Builder captureBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            captureBuilder.addTarget(reader.getSurface());
            captureBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
            // Orientation
            int rotation = getWindowManager().getDefaultDisplay().getRotation();
            captureBuilder.set(CaptureRequest.JPEG_ORIENTATION, ORIENTATIONS.get(rotation));



            reader.setOnImageAvailableListener(readerListener, mBackgroundHandler);

            final CameraCaptureSession.CaptureCallback captureListener = new CameraCaptureSession.CaptureCallback() {
                @Override
                public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result) {
                    super.onCaptureCompleted(session, request, result);

                    //createCameraPreview();  <- if you want multiple photos saved

                    returnResults();

                }

            };

            cameraDevice.createCaptureSession(outputSurfaces, new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(CameraCaptureSession session) {
                    try {
                        session.capture(captureBuilder.build(), captureListener, mBackgroundHandler);
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                }
                @Override
                public void onConfigureFailed(CameraCaptureSession session) {
                }
            }, mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }
    ImageReader.OnImageAvailableListener readerListener = new ImageReader.OnImageAvailableListener() {
        @Override
        public void onImageAvailable(ImageReader reader) {

            Image image = null;
            try {
                image = reader.acquireLatestImage();
                ByteBuffer buffer = image.getPlanes()[0].getBuffer();
                byte[] bytes = new byte[buffer.capacity()];
                buffer.get(bytes);
                save(bytes);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (image != null) {
                    image.close();
                }
            }
        }
        private void save(byte[] bytes) throws IOException {
            OutputStream output = null;
            try {
                output = new FileOutputStream(file);
                output.write(bytes);
            } finally {
                if (null != output) {
                    output.close();
                }
            }
        }
    };

    //---------------------------------------------------------------------------------------------- Location Service
    private void bindLocationService(){
        final Intent locationService = new Intent(this.getApplication(),GnssService.class);
        this.getApplication().startService(locationService);
        this.getApplication().bindService(locationService, locationServiceConnection, Context.BIND_AUTO_CREATE);

    }

    private ServiceConnection locationServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            String name = className.getClassName();

            if (name.endsWith("GnssService")) {
                locationService = ((GnssService.LocationServiceBinder) service).getService();
                locationService.startGPS();
                gpsRawCount = 0;
                gpsFilteredCount = 0;
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName className) {
            if (className.getClassName().equals("GnssService")) {
                locationService.stopGPS();
                locationService = null;
            }
        }
    };


    //----------------------------------------------------------------------------------------------Location Service
    private void registerLocationReceiver() {
        locationUpdateReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Location rawLocation = intent.getParcelableExtra("location");

                gpsRawCount++;

                if (!hasLocation) {
                    if (gpsRawCount > criteriaGpsRawCount) {
                        hasLocation = true;
                    }
                } else {
                    if(!locationService.getGpsLogging()){
                        locationService.startLogging();
                    }

                    mCurrentLocationRaw = rawLocation;
                }

            }
        };

        LocalBroadcastManager.getInstance(this).registerReceiver(
                locationUpdateReceiver,
                new IntentFilter("LocationUpdated")
        );

        //------------------------------------------------------------------------------------------

        predictedLocationReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Location predicatedLocation = intent.getParcelableExtra("location");

                gpsFilteredCount++;

                if (!hasFilteredLocation) {
                    if (gpsFilteredCount > criteriaGpsFilteredCount) {
                        hasFilteredLocation = true;
                    }
                } else {
                    mCurrentLocation = predicatedLocation;
                    updateSensorWithLocation(predicatedLocation);

                }

            }
        };

        LocalBroadcastManager.getInstance(this).registerReceiver(
                predictedLocationReceiver,
                new IntentFilter("PredictLocation")
        );

    }

    private void unregisterLocationReceiver(){

        try{
            if(locationUpdateReceiver !=null){
                unregisterReceiver(locationUpdateReceiver);

            }

            if (predictedLocationReceiver != null) {
                unregisterReceiver(predictedLocationReceiver);
            }


        }catch (IllegalArgumentException ex){
            ex.printStackTrace();
        }

    }



    //---------------------------------------------------------------------------------------------- Sensor Service

    private void bindSensorService(){
        final Intent sensorService = new Intent(this.getApplication(), SensorService.class);
        this.getApplication().startService(sensorService);
        this.getApplication().bindService(sensorService,sensorServiceConnection, Context.BIND_AUTO_CREATE);

    }

    private ServiceConnection sensorServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            String name = className.getClassName();

            if(name.endsWith("SensorService")){
                sensorService = ((SensorService.SensorServiceBinder) service).getService();
                sensorService.setupSensor(mContext,useFusionSensor);
                sensorService.setFilterAlpha(mSensorAlpha);
                sensorService.startSensor();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName className) {
            if (className.getClassName().equals("SensorService")) {
                sensorService.stopSensor();
                sensorService = null;
            }

        }
    };

    //-------------------------------------------------------------------------------------------------- Sensor Service

    private void registerSensorReceiver(){
        sensorDataReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                int sensorOrientation = 0;
                float sensorPitch = 0;
                float sensorRoll = 0;

                sensorOrientation = intent.getIntExtra("azimuth",sensorOrientation);
                sensorPitch = intent.getFloatExtra("pitch",sensorPitch);
                sensorRoll = intent.getFloatExtra("roll",sensorRoll);

                if(hasFilteredLocation){
                    evaluateSensorData(SENSOR_RAW,sensorOrientation,sensorPitch,sensorRoll);
                }
            }
        };

        LocalBroadcastManager.getInstance(this).registerReceiver(
                sensorDataReceiver,
                new IntentFilter("SensorData")
        );

        //------------------------------------------------------------------------------------------

        sensorGmfDataReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                int sensorOrientation = 0;
                float sensorPitch = 0;
                float sensorRoll = 0;

                sensorOrientation = intent.getIntExtra("azimuth",sensorOrientation);
                sensorPitch = intent.getFloatExtra("pitch",sensorPitch);
                sensorRoll = intent.getFloatExtra("roll",sensorRoll);

                evaluateSensorData(SENSOR_GMF,sensorOrientation,sensorPitch,sensorRoll);

            }
        };

        LocalBroadcastManager.getInstance(this).registerReceiver(
                sensorGmfDataReceiver,
                new IntentFilter("SensorGMFData")
        );

        //------------------------------------------------------------------------------------------

        sensorAccuracyReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                boolean mSensorWarning = intent.getBooleanExtra("sensorWarning",false);

                if(hasFilteredLocation){
                    evaluateSensorAccuracy(mSensorWarning);
                }

            }
        };

        LocalBroadcastManager.getInstance(this).registerReceiver(
                sensorAccuracyReceiver,
                new IntentFilter("SensorAccuracy")
        );




    }

    private void unregisterSensorReceiver(){
        try{
            if(sensorDataReceiver !=null){
                unregisterReceiver(sensorDataReceiver);
            }

            if(sensorAccuracyReceiver !=null){
                unregisterReceiver(sensorAccuracyReceiver);

            }

        }catch (IllegalArgumentException ex){
            ex.printStackTrace();
        }

    }

    //---------------------------------------------------------------------------------------------- Sensor Methods

    private void evaluateSensorData(boolean type, int orientation, float pitch, float roll){

        if(hasGMFSensorLocationSent){
            if (prefUseSensorGMF == type) {
                mSensorAzimuth = orientation;

            }
        }else{
            if(type == SENSOR_RAW){
                mSensorAzimuth = orientation;
            }
        }

    }

    private void updateSensorWithLocation(Location location){

        if(!hasGMFSensorLocationSent){

            sensorService.setSensorLocation(location);
            sensorService.setHasLocation(true);

            hasGMFSensorLocationSent = true;

        }else{
            sensorService.setSensorLocation(location);
        }

    }

    private void evaluateSensorAccuracy(boolean mWarning){

        if(!sendSensorWarning){
            showToast("Sensor Accuracy Low, Consider Calibrating", false);
            sendSensorWarning = true;
        }

    }

    //---------------------------------------------------------------------------------------------- Helpers
    private void showToast(String data, boolean shortTime) {

        if (shortTime) {
            Toast.makeText(this, data, Toast.LENGTH_SHORT).show();

        } else{
            Toast.makeText(this, data, Toast.LENGTH_LONG).show();

        }
    }

    //----------------------------------------------------------------------------------------------//
    private void returnResults(){
        Intent returnIntent = new Intent();
        returnIntent.putExtra(getString(R.string.KEY_IMAGE_FILE),file);


        if(prefIsGPSOn){
            Location toReturnLocation;

            if(hasFilteredLocation){
                toReturnLocation = mCurrentLocation;
            }else{
                toReturnLocation = mCurrentLocationRaw;
            }
            try{
                returnIntent.putExtra(getString(R.string.KEY_POSITION_USE),true);
                returnIntent.putExtra(getString(R.string.KEY_POSITION_LATITUDE),toReturnLocation.getLatitude());
                returnIntent.putExtra(getString(R.string.KEY_POSITION_LONGITUDE),toReturnLocation.getLongitude());
                returnIntent.putExtra(getString(R.string.KEY_POSITION_ELLIPSOID),toReturnLocation.getAltitude());
                returnIntent.putExtra(getString(R.string.KEY_POSITION_ACCURACY),toReturnLocation.getAccuracy());
            }catch (Exception ex){
                returnIntent.putExtra(getString(R.string.KEY_POSITION_USE),false);
            }

        }else{
            returnIntent.putExtra(getString(R.string.KEY_POSITION_USE),false);
        }

        returnIntent.putExtra(getString(R.string.KEY_SENSOR_USE),true);
        returnIntent.putExtra(getString(R.string.KEY_SENSOR_AZIMUTH),mSensorAzimuth);

        setResult(Activity.RESULT_OK,returnIntent);
        finish();

    }

}
