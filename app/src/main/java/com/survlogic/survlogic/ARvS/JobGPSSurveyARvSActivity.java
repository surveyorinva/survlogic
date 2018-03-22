package com.survlogic.survlogic.ARvS;

import android.Manifest;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.survlogic.survlogic.ARvS.interf.ArvSOnAzimuthChangeListener;
import com.survlogic.survlogic.ARvS.interf.ArvSOnLocationChangeListener;
import com.survlogic.survlogic.ARvS.model.ArvSLocationPOI;
import com.survlogic.survlogic.ARvS.utils.ArvSCurrentAzimuth;
import com.survlogic.survlogic.ARvS.utils.ArvSCurrentLocation;
import com.survlogic.survlogic.R;
import com.survlogic.survlogic.view.CompassLinearView;
import com.survlogic.survlogic.view.CompassRadarView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by chrisfillmore on 3/16/2018.
 */

public class JobGPSSurveyARvSActivity extends AppCompatActivity implements ArvSOnAzimuthChangeListener, ArvSOnLocationChangeListener {

    private static final String TAG = "JobGPSSurveyARvSActivit";


    private CameraManager cameraManager;
    private int cameraFacing;
    private Size mPreviewSize;

    private String mCameraId;
    private CameraDevice mCameraDevice;

    private CaptureRequest.Builder mPreviewRequestBuilder;
    private CameraCaptureSession mCaptureSession;
    private CaptureRequest mPreviewRequest;

    private Handler backgroundHandler;
    private HandlerThread backgroundThread;
    private CameraDevice.StateCallback mStateCallback;

    private boolean isCameraviewOn = false;

    private ArvSLocationPOI mPoi;

    private double mAzimuthObserved = 0;
    private double mAzimuthTheoretical = 0;
    private static double AZIMUTH_ACCURACY = 5;
    private double myCurrentLatitude = 0;
    private double myCurrentLongitude = 0;
    private double myCurrentLocationAccuracy = 0;

    private ArvSCurrentAzimuth myCurrentAzimuthListener;
    private ArvSCurrentLocation myCurrentLocationListener;
    private TextureView.SurfaceTextureListener surfaceTextureListener;

    private TextureView txvCameraView;
    private TextView descriptionTextView;
    private ImageView ivPointerIcon;

    private CompassLinearView compassLinearView;
    private CompassRadarView compassRadarView;

    private boolean useFusedSensors = true;

    private static final int REQUEST_CAMERA_PERMISSION = 200;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gps_ar_simple);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        initViewWidgets();

        initActivityListeners();
        initPOIPoint();

        initCompassLinearView();
        initCompassRadarView();

        initStateCallback();

    }

    //----------------------------------------------------------------------------------------------//

    @Override
    protected void onStop() {
        Log.d(TAG, "onStop: Started");
        myCurrentAzimuthListener.stopAzimith();
        myCurrentLocationListener.stopGPS();

        closeCamera();
        closeBackgroundThread();

        compassRadarView.stopSweep();

        super.onStop();
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "onResume: Started");
        super.onResume();

        openBackgroundThread();
        if(txvCameraView.isAvailable()){
            setUpCamera();
            openCamera();
        }else{
            txvCameraView.setSurfaceTextureListener(surfaceTextureListener);
        }

        myCurrentAzimuthListener.startAzimuth();
        myCurrentLocationListener.startGPS();

        startRadarView();
    }

    //----------------------------------------------------------------------------------------------//

    private void initActivityListeners(){
        Log.d(TAG, "setupActivityListeners: Started");

        myCurrentLocationListener = new ArvSCurrentLocation(this);
        myCurrentLocationListener.buildGoogleApiClient(this);
        myCurrentLocationListener.startGPS();

        myCurrentAzimuthListener = new ArvSCurrentAzimuth(this,this,useFusedSensors);
        myCurrentAzimuthListener.startAzimuth();

        surfaceTextureListener = new TextureView.SurfaceTextureListener(){
            @Override
            public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
                setUpCamera();
                openCamera();
            }

            @Override
            public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

            }

            @Override
            public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
                return false;
            }

            @Override
            public void onSurfaceTextureUpdated(SurfaceTexture surface) {

            }
        };


    }

    private void initViewWidgets(){
        Log.d(TAG, "initViewWidgets: Started");

        cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        cameraFacing = CameraCharacteristics.LENS_FACING_BACK;

        txvCameraView = (TextureView) findViewById(R.id.camera_view);

        descriptionTextView = (TextView) findViewById(R.id.cameraTextView);

        ivPointerIcon = (ImageView) findViewById(R.id.pointer_icon);

        compassLinearView = (CompassLinearView) findViewById(R.id.compassLinearView);

        compassRadarView = (CompassRadarView) findViewById(R.id.radar_view);

    }

    private void initPOIPoint(){
        Log.d(TAG, "initPOIPoint: Started");

        mPoi = new ArvSLocationPOI("manhole","manhole description",38.28464,-77.54806);

    }

    private void initCompassLinearView(){
        Log.d(TAG, "initCompassLinearView: Started");

        compassLinearView.setRangeDegrees(90);


    }

    private void initCompassRadarView(){
        Log.d(TAG, "initCompassRadarView: Started");

        compassRadarView.setShowRadarCircleText(false);

    }

    private void startRadarView(){
        Log.d(TAG, "startRadarView: Started");

        compassRadarView.setTarget(mPoi);
        compassRadarView.setUseMetric(false);
        compassRadarView.startSweep();


    }

    //----------------------------------------------------------------------------------------------//
    public double calculateTheoreticalAzimuth(ArvSLocationPOI mPOI){
        Log.d(TAG, "calculateTheoreticalAzimuth: Started");

        double dX = mPOI.getLatitude() - myCurrentLatitude;
        double dY = mPOI.getLongitude() - myCurrentLongitude;

        double phiAngle;
        double tanPhi;
        double azimuth = 0;

        tanPhi = Math.abs(dY / dX);
        phiAngle = Math.atan(tanPhi);
        phiAngle = Math.toDegrees(phiAngle);

        if (dX > 0 && dY > 0) { // I quater
            return azimuth = phiAngle;
        } else if (dX < 0 && dY > 0) { // II
            return azimuth = 180 - phiAngle;
        } else if (dX < 0 && dY < 0) { // III
            return azimuth = 180 + phiAngle;
        } else if (dX > 0 && dY < 0) { // IV
            return azimuth = 360 - phiAngle;
        }

        return azimuth;

    }

    private List<Double> calculateAzimuthAccuracy(double azimuth) {
        Log.d(TAG, "calculateAzimuthAccuracy: Started");
        double minAngle = azimuth - AZIMUTH_ACCURACY;
        double maxAngle = azimuth + AZIMUTH_ACCURACY;
        List<Double> minMax = new ArrayList<Double>();

        if (minAngle < 0)
            minAngle += 360;

        if (maxAngle >= 360)
            maxAngle -= 360;

        minMax.clear();
        minMax.add(minAngle);
        minMax.add(maxAngle);

        return minMax;
    }

    private boolean isBetween(double minAngle, double maxAngle, double azimuth) {
        Log.d(TAG, "isBetween: Started");
        if (minAngle > maxAngle) {
            if (isBetween(0, maxAngle, azimuth) && isBetween(minAngle, 360, azimuth))
                return true;
        } else {
            if (azimuth > minAngle && azimuth < maxAngle)
                return true;
        }
        return false;
    }
    
    //----------------------------------------------------------------------------------------------//
    private void updateMetadata(){
        Log.d(TAG, "updateMetadata: Started");

        String metaDescription = mPoi.getName() + " Theoretical Azimuth: " + mAzimuthTheoretical
                + ", Observed Azimuth: " + mAzimuthObserved + ". Current Location: " + myCurrentLatitude + ", " + myCurrentLongitude;


        descriptionTextView.setText(metaDescription);

    }

    private void updateCompassView(){
        Log.d(TAG, "updateCompassView: Started");

        float azimuth = (float) mAzimuthObserved;
        float compAzimuth = (float) mAzimuthTheoretical;

        compassLinearView.setCompDegree(compAzimuth);
        compassLinearView.setDegrees(azimuth);


    }

    private void updateRadarViewOrientation(){
        Log.d(TAG, "updateRadarView: Started");

        float azimuth = (float) mAzimuthObserved;

        compassRadarView.setOrientation(azimuth);

    }


    private void updateRadarViewLocation(Location location){
        Log.d(TAG, "updateRadarViewLocation: Started");

        if(location !=null){
            compassRadarView.setCurrentLocation(location);
        }


    }

    //----------------------------------------------------------------------------------------------//
    private void setUpCamera(){
        Log.d(TAG, "setUpCamera: Started");

        try {
            for (String cameraId : cameraManager.getCameraIdList()) {
                CameraCharacteristics cameraCharacteristics = cameraManager.getCameraCharacteristics(cameraId);
                if (cameraCharacteristics.get(CameraCharacteristics.LENS_FACING) == cameraFacing) {
                    StreamConfigurationMap streamConfigurationMap = cameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
                    mPreviewSize = streamConfigurationMap.getOutputSizes(SurfaceTexture.class)[0];
                    this.mCameraId = cameraId;
                }
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

    }

    private void openCamera(){
        Log.d(TAG, "openCamera: Started");

        try {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CAMERA_PERMISSION);
                return;
            }
            cameraManager.openCamera(mCameraId, mStateCallback, backgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }


    }

    private void closeCamera(){
        Log.d(TAG, "closeCamera: Started");

        if(mCaptureSession !=null){
            mCaptureSession.close();
            mCaptureSession = null;
        }

        if(mCameraDevice !=null){
            mCameraDevice.close();
            mCameraDevice = null;
        }

    }

    private void openBackgroundThread() {
        Log.d(TAG, "openBackgroundThread: Started");

        backgroundThread = new HandlerThread("camera_background_thread");
        backgroundThread.start();
        backgroundHandler = new Handler(backgroundThread.getLooper());
    }

    private void closeBackgroundThread(){
        if (backgroundHandler != null) {
            backgroundThread.quitSafely();
            backgroundThread = null;
            backgroundHandler = null;
        }
    }


    private void initStateCallback(){
        Log.d(TAG, "initStateCallback: Started");

        mStateCallback = new CameraDevice.StateCallback() {
            @Override
            public void onOpened(@NonNull CameraDevice camera) {
                mCameraDevice = camera;
                createCameraPreviewSession();
            }

            @Override
            public void onDisconnected(@NonNull CameraDevice camera) {
                mCameraDevice.close();
                mCameraDevice = null;
            }

            @Override
            public void onError(@NonNull CameraDevice camera, int error) {
                mCameraDevice.close();
                mCameraDevice = null;
            }
        };


    }

    private void createCameraPreviewSession(){
        Log.d(TAG, "createCameraPreviewSession: Started");

        try {
            SurfaceTexture surfaceTexture = txvCameraView.getSurfaceTexture();
            surfaceTexture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());

            Surface previewSurface = new Surface(surfaceTexture);
            mPreviewRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            mPreviewRequestBuilder.addTarget(previewSurface);

            mCameraDevice.createCaptureSession(Collections.singletonList(previewSurface),
                    new CameraCaptureSession.StateCallback() {

                        @Override
                        public void onConfigured(CameraCaptureSession cameraCaptureSession) {
                            if (mCameraDevice == null) {
                                return;
                            }

                            try {
                                mPreviewRequest = mPreviewRequestBuilder.build();
                                mCaptureSession = cameraCaptureSession;
                                mCaptureSession.setRepeatingRequest(mPreviewRequest,
                                        null, backgroundHandler);
                            } catch (CameraAccessException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onConfigureFailed(CameraCaptureSession cameraCaptureSession) {

                        }
                    }, backgroundHandler);

        } catch (CameraAccessException e) {
            e.printStackTrace();
        }


    }

    //----------------------------------------------------------------------------------------------//
    //ArvSOnAzimuthChangeListener
    @Override
    public void onAzimuthChanged(float azimuthFrom, float azimuthTo) {
        Log.d(TAG, "onAzimuthChanged....Started");

        mAzimuthObserved = azimuthTo;
        mAzimuthTheoretical = calculateTheoreticalAzimuth(mPoi);

        double minAngle = calculateAzimuthAccuracy(mAzimuthTheoretical).get(0);
        double maxAngle = calculateAzimuthAccuracy(mAzimuthTheoretical).get(1);

        if(isBetween(minAngle,maxAngle,mAzimuthObserved)){
            ivPointerIcon.setVisibility(View.VISIBLE);
        }else{
            ivPointerIcon.setVisibility(View.INVISIBLE);
        }

        updateCompassView();
        updateRadarViewOrientation();
        updateMetadata();

    }


    //ArvSOnLocationChangeListener
    @Override
    public void onLocationChanged(Location currentLocation) {
        Log.d(TAG, "onLocationChanged...Started");
        myCurrentLatitude = currentLocation.getLatitude();
        myCurrentLongitude = currentLocation.getLongitude();
        myCurrentLocationAccuracy = currentLocation.getAccuracy();

        mAzimuthTheoretical = calculateTheoreticalAzimuth(mPoi);

        updateRadarViewLocation(currentLocation);
        updateMetadata();
    }


    //----------------------------------------------------------------------------------------------//
    /**
     * Method Helpers
     */


    private void showToast(String data, boolean isShortTime) {

        if (isShortTime) {
            Toast.makeText(this, data, Toast.LENGTH_SHORT).show();

        } else{
            Toast.makeText(this, data, Toast.LENGTH_LONG).show();

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                // close the app
                Toast.makeText(this, "Sorry!!!, you can't use this app without granting permission", Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }



}
