package com.survlogic.survlogic.ARvS;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.SurfaceTexture;
import android.hardware.GeomagneticField;
import android.hardware.SensorEvent;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.location.GnssMeasurementsEvent;
import android.location.GnssStatus;
import android.location.GpsStatus;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.Size;
import android.util.SizeF;
import android.view.Surface;
import android.view.TextureView;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.survlogic.survlogic.ARvS.interf.ArvSOnAzimuthChangeListener;
import com.survlogic.survlogic.ARvS.interf.ArvSOnLocationChangeListener;
import com.survlogic.survlogic.ARvS.interf.GetNationalMapElevationListener;
import com.survlogic.survlogic.ARvS.model.ArvSLocationPOI;
import com.survlogic.survlogic.ARvS.utils.ArvSCurrentAzimuth;
import com.survlogic.survlogic.ARvS.utils.ArvSCurrentLocation;
import com.survlogic.survlogic.ARvS.utils.ArvSGnss;
import com.survlogic.survlogic.R;
import com.survlogic.survlogic.background.BackgroundGetNationalMapElevation;
import com.survlogic.survlogic.background.BackgroundGetSRTMElevation;
import com.survlogic.survlogic.interf.GpsSurveyListener;
import com.survlogic.survlogic.view.CameraOverlayView;
import com.survlogic.survlogic.view.CompassLinearView;
import com.survlogic.survlogic.view.CompassRadarView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.security.auth.login.LoginException;

import static com.survlogic.survlogic.utils.GPSLocationConverter.convertMetersToValue;

/**
 * Class dedicated to the AR View using GPS and device sensors
 */

public class JobGPSSurveyARvSActivity extends AppCompatActivity implements ArvSOnAzimuthChangeListener, ArvSOnLocationChangeListener, GpsSurveyListener, GetNationalMapElevationListener {

    private static final String TAG = "JobGPSSurveyARvSActivit";

    private SharedPreferences sharedPreferences;

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
    private boolean isFirstPosition = false;

    private ArvSLocationPOI mPoi;

    //Values
    private double mAzimuthObserved = 0;
    private double mAzimuthTheoretical = 0;
    private static double AZIMUTH_ACCURACY = 5;

    private Location mRawLocation;

    private ArvSCurrentAzimuth myCurrentAzimuthListener;
    private ArvSCurrentLocation myCurrentLocationListener;
    private ArvSGnss myGnss;

    private TextureView.SurfaceTextureListener surfaceTextureListener;

    private TextureView txvCameraView;
    private TextView descriptionTextView;
    private ImageView ivPointerIcon;

    private CompassLinearView compassLinearView;
    private CompassRadarView compassRadarView;
    private CameraOverlayView cameraOverlayView;

    //    UI Views
    private TextView tvNumSats, tvNumSatsLocked, tvTtff, tvOrientation,
            tvPdopView, tvHdopView, tvVdopView, tvHeightView;

    //  Location Average and checking
    private List<Location> listSampledLocations = new ArrayList<>();
    private List<LatLng> lstSampledSplitLocations = new ArrayList<>();
    private List<Float> listSampleAltitudeHeights = new ArrayList<>();
    private List<Float> listSampleAltitudeHeightsMsl = new ArrayList<>();
    private List<Float> listSampleLocationAccuracy = new ArrayList<>();

    //Sample Sensor and checking
    private List<Float> listSampleOrientation = new ArrayList<>();
    private List<Float> listSampleAzimithTo = new ArrayList<>();

    //System set variables
    boolean isMaxOrientation = false;
    boolean isMaxLocation = false;

    private int elevationServiceToUse = 1;

    private Location mAveragedLocation;
    private double mOrthoHeight;

    private float mAverageAzimithTo;
    private float[] mAverageOrientation = new float[3];
    private float mLockedOrientationValue = 0;

    private float mCameraVerticalFOV, mCameraHorizontalFOV;


    //Custom Variables
    private boolean useFusedSensors = true;
    private static final int REQUEST_CAMERA_PERMISSION = 200;
    private static final double LOCATION_ACCURACY_ERROR_ELLIPSE = 5;
    private static final int LOCATION_ACCURACY_SAMPLE_RATE = 10;
    private static final int SENSOR_ORIENTATION_SAMPLE_RATE = 10;
    private static final float SENSOR_ACCURACY_ERROR_ELLIPSE = 0.50F;
    private static final float LOCATION_ACCURACY_ERROR_ALTITUDE_MSL = 5;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gps_ar_simple);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        initPreferences();
        initViewWidgets();

        initActivityListeners();
        initPOIPoint();

        initCompassLinearView();
        initCompassRadarView();
        initCameraOverlayView();

        initStateCallback();

    }

    //----------------------------------------------------------------------------------------------//

    @Override
    protected void onStop() {
        Log.d(TAG, "onStop: Started");
        myCurrentAzimuthListener.stopAzimith();
        myCurrentLocationListener.stopGPS();
        myGnss.stopGPS();

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

        ArvSGnss.getInstance().addListener(this);
        myGnss.startGPS();

        startRadarView();
    }

    //----------------------------------------------------------------------------------------------//

    private void initPreferences(){
        Log.d(TAG, "initPreferences: Started");

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);



    }

    private void initActivityListeners(){
        Log.d(TAG, "setupActivityListeners: Started");

        myCurrentLocationListener = new ArvSCurrentLocation(this);
        myCurrentLocationListener.buildGoogleApiClient(this);
        myCurrentLocationListener.startGPS();

        myCurrentAzimuthListener = new ArvSCurrentAzimuth(this,this,useFusedSensors);
        myCurrentAzimuthListener.startAzimuth();

        myGnss = new ArvSGnss(this);
        myGnss.buildGnssClient(this);
        myGnss.startGPS();


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

        cameraOverlayView = (CameraOverlayView) findViewById(R.id.camera_overlay_view);

        //On Screen View Widgets handled within this Activity (for now!)
        initGnssWidget();


    }

    private void initPOIPoint(){
        Log.d(TAG, "initPOIPoint: Started");

        mPoi = new ArvSLocationPOI("manhole","manhole description",38.953610,-76.833974,52.80);

        Location targetLocation = new Location(mPoi.getName());
        targetLocation.setLatitude(mPoi.getLatitude());
        targetLocation.setLongitude(mPoi.getLongitude());


        switch(elevationServiceToUse){
            case 0:
                //none
                break;

            case 1: //https://nationalmap.gov/epqs/
                BackgroundGetNationalMapElevation backgroundGetNationalMapElevation = new BackgroundGetNationalMapElevation(this,targetLocation,this);
                backgroundGetNationalMapElevation.execute();
                break;

            case 2: //http://www.geonames.org/export/web-services.html#srtm3
                BackgroundGetSRTMElevation backgroundGetSRTMElevation = new BackgroundGetSRTMElevation(this,targetLocation,this);
                backgroundGetSRTMElevation.execute();
                break;
        }





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

    private void initCameraOverlayView(){
        Log.d(TAG, "initCameraOverlayView: Started");

        cameraOverlayView.setTarget(mPoi);
        cameraOverlayView.setDebugSensorData(true);
        cameraOverlayView.setUseGMF(false);
        cameraOverlayView.setAngleRange(10.0F,10.0F);


    }

    //----------------------------------------------------------------------------------------------//
    private void updateMetadata(){
        Log.d(TAG, "updateMetadata: Started");

        String metaDescription = mPoi.getName() + " Theoretical Azimuth: " + mAzimuthTheoretical
                + ", Observed Azimuth: " + mAzimuthObserved + ". Current Location: " + mAveragedLocation.getLatitude() + ", " + mAveragedLocation.getLongitude();


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


    private void updateCameraOverlayViewSensorEvent(SensorEvent event){
        Log.d(TAG, "updateCameraOverlayViewSensorEvent: Started");

        cameraOverlayView.setMSensorEvent(event);

    }

    private void updateCameraOverlayViewSensorEvent(Location currentLocation, float azimuthFrom, float azimuthTo, float [] orientation){
        Log.d(TAG, "updateCameraOverlayViewSensorEvent: Started");

        double altitudeMslRaw = myGnss.getAltitudeMsl();
        double altitudeMslAverage = computeCentroidFloat(listSampleAltitudeHeightsMsl);
        float currentAltitude = 0.00F;

        if(altitudeMslAverage !=0){
            double variance = altitudeMslAverage - altitudeMslRaw;

            if(variance >= LOCATION_ACCURACY_ERROR_ALTITUDE_MSL){
                currentAltitude = (float) altitudeMslRaw;
            }else{
                currentAltitude = (float) altitudeMslAverage;
            }

        }

        cameraOverlayView.setCurrentCameraSensorData(currentLocation,azimuthFrom,azimuthTo, orientation,currentAltitude);

    }


    //----------------------------------------------------------------------------------------------//
    public double calculateTheoreticalAzimuth(ArvSLocationPOI mPOI){
        Log.d(TAG, "calculateTheoreticalAzimuth: Started");

        double dX = mPOI.getLatitude() - mAveragedLocation.getLatitude();
        double dY = mPOI.getLongitude() - mAveragedLocation.getLongitude();

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
    private void setUpCamera(){
        Log.d(TAG, "setUpCamera: Started");

        try {
            for (String cameraId : cameraManager.getCameraIdList()) {
                CameraCharacteristics cameraCharacteristics = cameraManager.getCameraCharacteristics(cameraId);
                if (cameraCharacteristics.get(CameraCharacteristics.LENS_FACING) == cameraFacing) {
                    StreamConfigurationMap streamConfigurationMap = cameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
                    mPreviewSize = streamConfigurationMap.getOutputSizes(SurfaceTexture.class)[0];
                    this.mCameraId = cameraId;
                    calculateFOV(cameraCharacteristics);
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

    private void calculateFOV(CameraCharacteristics characteristics) {
        Log.d(TAG, "calculateFOV: Started");

        try {
            float[] maxFocus = characteristics.get(CameraCharacteristics.LENS_INFO_AVAILABLE_FOCAL_LENGTHS);
            SizeF size = characteristics.get(CameraCharacteristics.SENSOR_INFO_PHYSICAL_SIZE);
            float w = size.getWidth();
            float h = size.getHeight();
            mCameraHorizontalFOV = (float) (2*Math.atan(w/(maxFocus[0]*2)));
            mCameraVerticalFOV = (float) (2*Math.atan(h/(maxFocus[0]*2)));

            cameraOverlayView.setCameraParamsFOV(mCameraVerticalFOV,mCameraHorizontalFOV);


        } catch (Exception e) {
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
    //ArvSOnAzimuthChangeListener-------------------------------------------------------------------//
    @Override
    public void onAzimuthChanged(float azimuthFrom, float azimuthTo, float[] orientation) {
        Log.d(TAG, "onAzimuthChanged....Started");

        handleSensorData(azimuthFrom,azimuthTo,orientation);
    }

    @Override
    public void onDeviceSensorChange(SensorEvent event) {
        Log.d(TAG, "onDeviceSensorChange: Started");

        updateCameraOverlayViewSensorEvent(event);

    }

    //ArvSOnLocationChangeListener------------------------------------------------------------------//
    @Override
    public void onLocationChanged(Location currentLocation) {
        Log.d(TAG, "onLocationChanged...Started");

        handleLocationData(currentLocation);

    }


    //GpsSurveyListener-----------------------------------------------------------------------------//

    @Override
    public void gpsStart() {
        Log.d(TAG, "gpsStart: Started");
        setGnssWidget();

    }

    @Override
    public void gpsStop() {

    }

    @Override
    public void onGpsStatusChanged(int event, GpsStatus status) {
        Log.d(TAG, "onGpsStatusChanged: Started");

    }

    @Override
    public void onGnssFirstFix(int ttffMillis) {

    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onSatelliteStatusChanged(GnssStatus status) {
        int noOfSats = myGnss.getHowManySatellitesAvailable();
        int lockedSats = myGnss.getHowManySatellitesLocked();

        tvNumSats.setText(String.valueOf(noOfSats));
        tvNumSatsLocked.setText(String.valueOf(lockedSats));


    }

    @Override
    public void onGnssStarted() {
        Log.d(TAG, "onGnssStarted: Started");
    }

    @Override
    public void onGnssStopped() {
        Log.d(TAG, "onGnssStopped: Started");

    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onGnssMeasurementsReceived(GnssMeasurementsEvent event) {

    }

    @Override
    public void onOrientationChanged(double orientation, double tilt) {

    }

    @Override
    public void onNmeaMessage(String message, long timestamp) {
        double mP, mH, mV;
        mP = myGnss.getpDop();
        mH = myGnss.gethDop();
        mV = myGnss.getvDop();

        tvPdopView.setText(getString(R.string.gps_pdop_value, mP));
        tvHdopView.setText(getString(R.string.gps_hdop_value, mH));
        tvVdopView.setText(getString(R.string.gps_vdop_value, mV));

        Log.d(TAG, "onNmeaMessage: Dops (P,H,V): " + mP + "," + mH + "," +  mV);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    //----------------------------------------------------------------------------------------------//

    private void setGmfForSensor(){
        Log.d(TAG, "setGmfForSensor: Started");

        GeomagneticField geoField = new GeomagneticField(
                (float) mRawLocation.getLatitude(),
                (float) mRawLocation.getLongitude(),
                (float) mRawLocation.getAltitude(),
                System.currentTimeMillis());

        myCurrentAzimuthListener.setGmf(geoField);


    }

    //----------------------------------------------------------------------------------------------//
    private void initGnssWidget(){
        Log.d(TAG, "initViewWidgets: Started");

        tvNumSats = (TextView) findViewById(R.id.gps_status_noSatellites);
        tvNumSatsLocked = (TextView) findViewById(R.id.gps_status_noSatellitesLocked);

        tvPdopView = (TextView) findViewById(R.id.PDOP_value);
        tvHdopView = (TextView) findViewById(R.id.HDOP_value);
        tvVdopView = (TextView) findViewById(R.id.VDOP_value);
        tvTtff = (TextView) findViewById(R.id.ttff_value);
        tvOrientation = (TextView) findViewById(R.id.orientation_value);

    }

    private void setGnssWidget(){
        Log.d(TAG, "setGnssWidget: Started");

        tvNumSats.setText(getString(R.string.satellite_default));

        tvPdopView.setText(getString(R.string.gps_pdop_default));
        tvHdopView.setText(getString(R.string.gps_hdop_default));
        tvVdopView.setText(getString(R.string.gps_vdop_default));


    }

    //----------------------------------------------------------------------------------------------//

    private void handleSensorData(float azimuthFrom, float azimuthTo, float[] orientation){
        Log.d(TAG, "handleSensorData: Started");

        if(mRawLocation != null){

            float[] averagedOrientation = new float[3];
            float averageAzimithTo = 0.00F;

            addOrientationToArrayList(orientation[0]);
            addAzimithToArrayList(azimuthTo);

            boolean isShaken = isSensorOrientationShaking(orientation[0],SENSOR_ACCURACY_ERROR_ELLIPSE);

            if(isShaken){
                tvOrientation.setText(getResources().getString(R.string.ar_sensor_status_moving));

                clearOrientationToArrayList();
                clearAzimithToArrayList();
                isMaxOrientation = false;

            }else{
                if(!isMaxOrientation){
                    if(listSampleOrientation.size() <= SENSOR_ORIENTATION_SAMPLE_RATE ){
                        averagedOrientation = orientation;
                        averagedOrientation[0] = computeAverageOrientation();
                        mLockedOrientationValue = averagedOrientation[0];

                        mAverageOrientation = averagedOrientation;

                        averageAzimithTo = computeAverageAzimithTo();
                        mAverageAzimithTo = averageAzimithTo;

                        String myResults = getResources().getString(R.string.ar_sensor_status_static) + "[" + String.valueOf(listSampleOrientation.size() + "]");
                        tvOrientation.setText(myResults);

                        if(listSampleOrientation.size() == SENSOR_ORIENTATION_SAMPLE_RATE){
                            isMaxOrientation = true;

                        }
                    }
                }
            }
            boolean isStationary = isGnssStationary(LOCATION_ACCURACY_ERROR_ELLIPSE);
            Location averageLocation = mRawLocation;
            String myResults;

            if(isStationary){
                if(!isMaxLocation){
                    if(listSampledLocations.size() <= LOCATION_ACCURACY_SAMPLE_RATE){
                        averageLocation = computeCentroidLocation();
                        mAveragedLocation = averageLocation;

                        myResults = getResources().getString(R.string.ar_gps_status_static) + "[" + String.valueOf(lstSampledSplitLocations.size()) + "]";
                        tvTtff.setText(myResults);

                        if(listSampledLocations.size() == LOCATION_ACCURACY_SAMPLE_RATE){
                            isMaxLocation = true;
                        }
                    }
                }

            }else{
                clearLocationArrayList();

                averageLocation = mRawLocation;
                mAveragedLocation = averageLocation;

                myResults = getResources().getString(R.string.ar_gps_status_moving);
                tvTtff.setText(myResults);
                isMaxLocation = false;
            }


            mAverageOrientation[0] = mLockedOrientationValue;
            updateCameraOverlayViewSensorEvent(mAveragedLocation,azimuthFrom,mAverageAzimithTo,mAverageOrientation);

            mAzimuthObserved = azimuthTo;
            mAzimuthTheoretical = calculateTheoreticalAzimuth(mPoi);

            updateCompassView();
            updateRadarViewOrientation();
            updateMetadata();
            updateRadarViewLocation(mAveragedLocation);

        }

    }

    private void handleLocationData(Location currentLocation){
        Log.d(TAG, "handleLocationData: Started");
        mRawLocation = new Location(currentLocation);

        addLocationToArrayList(mRawLocation);

        if(!isFirstPosition){
            setGmfForSensor();
            isFirstPosition = true;
        }

    }
    //----------------------------------------------------------------------------------------------//
    private void addLocationToArrayList(Location location){
        Log.d(TAG, "addLocationToArrayList: Started");

        listSampledLocations.add(location);

        LatLng sample = new LatLng(location.getLatitude(),location.getLongitude());
        lstSampledSplitLocations.add(sample);

        listSampleAltitudeHeights.add((float) location.getAltitude());
        listSampleAltitudeHeightsMsl.add((float) myGnss.getAltitudeMsl());
        listSampleLocationAccuracy.add(location.getAccuracy());

    }

    private void clearLocationArrayList(){
        Log.d(TAG, "clearLocationArrayList: Started");

        listSampledLocations.clear();
        lstSampledSplitLocations.clear();
        listSampleAltitudeHeights.clear();
        listSampleAltitudeHeightsMsl.clear();
        listSampleLocationAccuracy.clear();

    }

    private boolean isGnssStationary(double accuracyMeters ){
        Log.d(TAG, "isGnssStationary: Started");

        double sampleDistanceToCurrentDistance = computeCentroidToCurrentDistance();

        Log.d(TAG, "isGnssStationary: Distance to Centroid: " + sampleDistanceToCurrentDistance);

        if(sampleDistanceToCurrentDistance > accuracyMeters){
            return false;
        }else{
            return true;
        }

    }

    private double computeCentroidToCurrentDistance(){
        Log.d(TAG, "computeCentroidToCurrentDistance: Started");

        LatLng centroidSplit = computeCentroid(lstSampledSplitLocations);

        double latitude = centroidSplit.latitude;
        double longitude = centroidSplit.longitude;

        Location centroid = new Location("sample");
        centroid.setLatitude(latitude);
        centroid.setLongitude(longitude);

        return centroid.distanceTo(mRawLocation);


    }

    private Location computeCentroidLocation(){
        Log.d(TAG, "computeCentroidLocation: Started");

        LatLng centroidSplit = computeCentroid(lstSampledSplitLocations);
        Float centroidAltitude = computeCentroidFloat(listSampleAltitudeHeights);
        Float centroidAccuracy = computeCentroidFloat(listSampleLocationAccuracy);

        double latitude = centroidSplit.latitude;
        double longitude = centroidSplit.longitude;

        Location centroid = new Location("sample");
        centroid.setLatitude(latitude);
        centroid.setLongitude(longitude);
        centroid.setAltitude(centroidAltitude);
        centroid.setAccuracy(centroidAccuracy);
        return centroid;

    }


    private LatLng computeCentroid(List<LatLng> points) {
        double latitude = 0;
        double longitude = 0;
        int n = points.size();

        for (LatLng point : points) {
            latitude += point.latitude;
            longitude += point.longitude;
        }

        return new LatLng(latitude/n, longitude/n);
    }

    private void addOrientationToArrayList(float orientation){
        Log.d(TAG, "addOrientationToArrayList: Started");

        listSampleOrientation.add(orientation);

    }
    private void clearOrientationToArrayList(){
        Log.d(TAG, "clearOrientationToArrayList: Started");

        listSampleOrientation.clear();
    }

    private void addAzimithToArrayList(float azimithIn){
        Log.d(TAG, "addAzimithToArrayList: Started");

        listSampleAzimithTo.add(azimithIn);

    }

    private void clearAzimithToArrayList(){
        Log.d(TAG, "clearAzimithToArrayList: Started");

        listSampleAzimithTo.clear();

    }
    private boolean isSensorOrientationShaking(float currentOrientation, double accuracyAngle){
        Log.d(TAG, "isSensorOrientationShaking: Started");

        float diff = Math.abs(Math.abs(currentOrientation) - Math.abs(computeAverageOrientation()));

        double accuracyRadians = Math.toRadians(accuracyAngle);

        if(diff > accuracyRadians){
            return true;
        }else{
            return false;
        }

    }

    private float computeAverageAzimithTo(){
        Log.d(TAG, "computeAverageAzimithTo: Started");

        return computeCentroidFloat(listSampleAzimithTo);

    }

    private float computeAverageOrientation(){
        Log.d(TAG, "computeAverageOrientation: Started");
        Log.i(TAG, "computeAverageOrientation: " + computeCentroidFloat(listSampleOrientation));
        Log.i(TAG, "computeAverageOrientation: Size: " + listSampleOrientation.size());
        return computeCentroidFloat(listSampleOrientation);


    }

    private float computeCentroidFloat(List<Float> points) {
        Log.d(TAG, "computeCentroidFloat: Started");
        Float mean = 0.0F;
        mean = getMean(points);

        Log.i(TAG, "Results: Mean: " + mean);
        Log.i(TAG, "Results: Variance: " + getVariance(points));
        Log.i(TAG, "Results: Std Deviation: " + getStdDev(points));

        return mean;
    }


    //Simple Mathmatics
    private float getMean(List<Float> points){
        Float sum = 0.0F;
        if(!points.isEmpty()){
            for (Float point : points){
                sum +=point;
            }
            return sum / points.size();
        }
        return sum;
    }

    private float getVariance(List<Float> points){
        float mean = getMean(points);
        float temp = 0;
        for (Float point : points){
            temp +=(point-mean)*(point-mean);

        }

        return temp/(points.size() - 1);

    }

    private float getStdDev(List<Float> points){
        return (float) Math.sqrt(getVariance(points));
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


    //----------------------------------------------------------------------------------------------//

    @Override
    public void getNationalMapResults(String results, String units, String source) {
        Log.d(TAG, "getNationalMapResults: Started");

        cameraOverlayView.setTargetPoiFalseElevation(Float.parseFloat(results));
        
    }

    @Override
    public void getResultSRTM(String results) {
        Log.d(TAG, "getResultSRTM: Started");

        cameraOverlayView.setTargetPoiFalseElevation(Float.parseFloat(results));

    }
}
