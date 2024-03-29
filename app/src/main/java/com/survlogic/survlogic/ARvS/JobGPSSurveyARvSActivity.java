package com.survlogic.survlogic.ARvS;

import android.Manifest;
import android.animation.LayoutTransition;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
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
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.Range;
import android.util.Size;
import android.util.SizeF;
import android.view.MenuItem;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.survlogic.survlogic.ARvS.interf.ArvSOnSensorChangeListener;
import com.survlogic.survlogic.ARvS.interf.ArvSOnLocationChangeListener;
import com.survlogic.survlogic.ARvS.interf.GetNationalMapElevationListener;
import com.survlogic.survlogic.ARvS.interf.JobGPSSurveyARvSActivityListener;
import com.survlogic.survlogic.ARvS.model.ArvSLocationPOI;
import com.survlogic.survlogic.ARvS.utils.ArvSCurrentSensor;
import com.survlogic.survlogic.ARvS.utils.ArvSCurrentLocation;
import com.survlogic.survlogic.ARvS.utils.ArvSGnss;
import com.survlogic.survlogic.R;
import com.survlogic.survlogic.adapter.JobGpsStakeoutPointListAdapter;
import com.survlogic.survlogic.background.BackgroundGeodeticPointGet;
import com.survlogic.survlogic.background.BackgroundGetNationalMapElevation;
import com.survlogic.survlogic.background.BackgroundGetSRTMElevation;
import com.survlogic.survlogic.interf.GpsSurveyListener;
import com.survlogic.survlogic.interf.JobPointsMapListener;
import com.survlogic.survlogic.model.PointGeodetic;
import com.survlogic.survlogic.model.PointSurvey;
import com.survlogic.survlogic.view.CameraOverlayView;
import com.survlogic.survlogic.view.CompassLinearView;
import com.survlogic.survlogic.view.CompassRadarView;
import com.survlogic.survlogic.view.DistanceToTargetView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Class dedicated to the AR View using GPS and device sensors
 */

public class JobGPSSurveyARvSActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener,
                                                                            ArvSOnSensorChangeListener, ArvSOnLocationChangeListener,
                                                                            GpsSurveyListener,
                                                                            GetNationalMapElevationListener,
                                                                            JobPointsMapListener, OnMapReadyCallback,
                                                                            JobGPSSurveyARvSActivityListener {

    private static final String TAG = "JobGPSSurveyARvSActivit";

    private Context mContext;

    private SharedPreferences sharedPreferences;

    private DrawerLayout drawerLayout;

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
    private Location mCurrentLocation, mFilteredLocation;

    private ArvSCurrentSensor myCurrentAzimuthListener;
    private ArvSCurrentLocation myCurrentLocationListener;
    private ArvSGnss myGnss;

    private TextureView.SurfaceTextureListener surfaceTextureListener;

    private float mDistanceToTarget;

    //Widgets
    private ImageButton ibAppBarBackButton;
    private TextureView txvCameraView;
    private RelativeLayout rlCameraView, rlCameraOverlayView, rlMapOverlayView, rlCompassOverlayView, rlRadarOverlayView, rlGPSError, rlDistanceView;
    private TextView descriptionTextView;
    private ImageView ivPointerIcon, ivNavOptionsMapView, ivNavOptionsCameraView;

    private FloatingActionButton fabMenu;

    private CompassLinearView compassLinearView;
    private CompassRadarView compassRadarView;
    private CameraOverlayView cameraOverlayView;
    private DistanceToTargetView distanceToTargetView;

    //UI View Variables
    private int stageMap = STAGE_START;
    private int previousStageMap=STAGE_START;

    private int stageCamera = STAGE_START;

    private static final int STAGE_START = -1, STAGE_HIDDEN = 0, STAGE_HALF = 1, STAGE_FULL = 2;


    //    Mapping Variables
    private SupportMapFragment mMapFragment;
    private GoogleMap mMap;
    private UiSettings mUiSettings;
    private int mapType = 1;

    private boolean isTargetZoomDone = false;

    private Marker targetMarker, currentMarker;
    private List<Polyline> listMapPolylines = new ArrayList<>();

    //    UI Views
    private TextView tvNumSats, tvNumSatsLocked, tvTtff, tvOrientation,
            tvPdopView, tvHdopView, tvVdopView, tvHeightView;

    private TextView tvNavNumSats;

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
    private boolean hasLocation = false;
    private boolean hasFilteredLocation = false;
    private boolean isMaxOrientation = false;
    private boolean isMaxLocation = false;
    private boolean isCameraOverlaySet=false, isMapOverlaySet=false, isRadarOverlaySet=false, isCompassOverlaySet=false, isDistanceOverlaySet=false;
    private boolean isViewsAutomated = true;

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
    private static final int POI_TARGET = 0, POI_CURRENT = 1;

    //Geodetic Points
    private ArrayList<PointGeodetic> lstPointGeodetic = new ArrayList<>();

    //Loading Job Data
    private int project_id, job_id;
    private String jobDatabaseName;

    //Navigation View Controls
    private RecyclerView rvPointList;
    private RecyclerView.LayoutManager layoutManagerPointGeodetic;
    private JobGpsStakeoutPointListAdapter adapterGeodetic;


    //Mapping Variables
    private boolean isMapZoomable = true, didInitialMapZoom = false;
    private Timer mapZoomBlockingTimer;
    private Handler handlerOnUIThread;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gps_ar);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        mContext = JobGPSSurveyARvSActivity.this;

        initPreferences();
        initViewWidgets();
        setOnClickListeners();
        initActivityListeners();
        initStateCallback();

        initPointDataInBackground();

        LoadGPSDataTask loadGPSDataTask = new LoadGPSDataTask();
        loadGPSDataTask.execute();
    }

    //----------------------------------------------------------------------------------------------//

    @Override
    protected void onStop() {
        Log.d(TAG, "onStop: Started");
        myCurrentAzimuthListener.stopReading();
        // myCurrentLocationListener.stopGPS();
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
        if (txvCameraView.isAvailable()) {
            setUpCamera();
            openCamera();
        } else {
            txvCameraView.setSurfaceTextureListener(surfaceTextureListener);
        }

        myCurrentAzimuthListener.startReading();
        //myCurrentLocationListener.startGPS();

        ArvSGnss.getInstance().addListener(this);
        myGnss.startGPS();


    }

    //----------------------------------------------------------------------------------------------//

    private void initPreferences() {
        Log.d(TAG, "initPreferences: Started");

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);


    }

    private void initActivityListeners() {
        Log.d(TAG, "setupActivityListeners: Started");

//        myCurrentLocationListener = new ArvSCurrentLocation(this);
//        myCurrentLocationListener.buildGoogleApiClient(this);
//        myCurrentLocationListener.startGPS();

        myCurrentAzimuthListener = new ArvSCurrentSensor(this, this, useFusedSensors);
        myCurrentAzimuthListener.startReading();

        myGnss = new ArvSGnss(this);
        myGnss.buildGnssClient(this);
        myGnss.startGPS();


        surfaceTextureListener = new TextureView.SurfaceTextureListener() {
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

    private void initViewWidgets() {
        Log.d(TAG, "initViewWidgets: Started");

        Bundle extras = getIntent().getExtras();
        project_id = extras.getInt(getString(R.string.KEY_PROJECT_ID));
        job_id = extras.getInt(getString(R.string.KEY_JOB_ID));
        jobDatabaseName = extras.getString(getString(R.string.KEY_JOB_DATABASE));
        Log.d(TAG, "||Database|| : " + jobDatabaseName);

        ibAppBarBackButton = findViewById(R.id.button_back);
        ibAppBarBackButton.setVisibility(View.VISIBLE);
        ibAppBarBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        drawerLayout = findViewById(R.id.drawer_in_gps_ar);
        drawerLayout.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                adapterGeodetic.setCurrentLocation(mAveragedLocation);
                adapterGeodetic.notifyDataSetChanged();
            }

            @Override
            public void onDrawerOpened(View drawerView) {

            }

            @Override
            public void onDrawerClosed(View drawerView) {

            }

            @Override
            public void onDrawerStateChanged(int newState) {

            }
        });

        ivNavOptionsMapView = findViewById(R.id.mapViewOptions);
        ivNavOptionsCameraView = findViewById(R.id.cameraViewOptions);

        rlCameraView = findViewById(R.id.rl_camera_view);
        rlCameraOverlayView = findViewById(R.id.rl_camera_overlay_view);
        rlMapOverlayView = findViewById(R.id.rl_map_overlay_view);
        rlCompassOverlayView = findViewById(R.id.rlFrame_bottom_compass_rule);
        rlRadarOverlayView = findViewById(R.id.rl_radar_view);
        rlDistanceView= findViewById(R.id.rl_distance_view);
        rlGPSError = findViewById(R.id.rl_error_message);

        fabMenu = findViewById(R.id.fab_menu);
        fabMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(drawerLayout.isDrawerOpen(GravityCompat.START)){
                    drawerLayout.closeDrawer(GravityCompat.START);
                }else{
                    drawerLayout.openDrawer(GravityCompat.START);
                }
            }
        });

        cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        cameraFacing = CameraCharacteristics.LENS_FACING_BACK;

        txvCameraView = findViewById(R.id.camera_view);

        ivPointerIcon = findViewById(R.id.pointer_icon);

        compassLinearView = findViewById(R.id.compassLinearView);

        compassRadarView = findViewById(R.id.radar_view);

        cameraOverlayView = findViewById(R.id.camera_overlay_view);

        distanceToTargetView = findViewById(R.id.distance_view);

        //On Screen View Widgets handled within this Activity (for now!)
        initGnssWidget();

        //RecyclerView in Navigation Bar
        rvPointList = findViewById(R.id.pointListRecyclerView);
        layoutManagerPointGeodetic = new LinearLayoutManager(mContext);

        //Map Overlay View
        mMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);

    }

    private void setOnClickListeners(){
        Log.d(TAG, "setOnClickListeners: Started");

        ivNavOptionsMapView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                viewMapControllerByButton();
                isViewsAutomated = false;
            }
        });

        ivNavOptionsCameraView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                viewController();
                isViewsAutomated = false;
            }
        });


    }

    private void initPOIPoint(PointGeodetic pointGeodetic) {
        Log.d(TAG, "initPOIPoint: Started");

        mPoi = new ArvSLocationPOI(String.valueOf(pointGeodetic.getPoint_no()),
                                    pointGeodetic.getDescription(),
                                    pointGeodetic.getLatitude(), pointGeodetic.getLongitude(),
                                    pointGeodetic.getElevation());

        Location targetLocation = convertPOIToLocation(mPoi);

        switch (elevationServiceToUse) {
            case 0:
                //none
                break;

            case 1: //https://nationalmap.gov/epqs/
                BackgroundGetNationalMapElevation backgroundGetNationalMapElevation = new BackgroundGetNationalMapElevation(this, targetLocation, this,POI_TARGET);
                backgroundGetNationalMapElevation.execute();
                break;

            case 2: //http://www.geonames.org/export/web-services.html#srtm3
                BackgroundGetSRTMElevation backgroundGetSRTMElevation = new BackgroundGetSRTMElevation(this, targetLocation, this);
                backgroundGetSRTMElevation.execute();
                break;
        }

        startRadarView();


    }

    private void initCompassLinearView() {
        Log.d(TAG, "initCompassLinearView: Started");

        compassLinearView.setRangeDegrees(90);
        rlCompassOverlayView.setVisibility(View.VISIBLE);

        isCompassOverlaySet = true;
    }

    private void initRadarOverlayView() {
        Log.d(TAG, "initRadarOverlayView: Started");

        compassRadarView.setShowRadarCircleText(false);
        rlRadarOverlayView.setVisibility(View.VISIBLE);

        isRadarOverlaySet = true;
    }

    private void startRadarView() {
        Log.d(TAG, "startRadarView: Started");

        compassRadarView.setTarget(mPoi);
        compassRadarView.setUseMetric(false);
        compassRadarView.startSweep();

    }

    private void initCameraOverlayView() {
        Log.d(TAG, "initCameraOverlayView: Started");

        cameraOverlayView.setTarget(mPoi);
        cameraOverlayView.setDebugSensorData(true);
        cameraOverlayView.setUseGMF(false);
        cameraOverlayView.setAngleRange(10.0F, 10.0F);

        rlCameraView.setVisibility(View.VISIBLE);
        rlCameraOverlayView.setVisibility(View.VISIBLE);

        isCameraOverlaySet = true;
    }

    private void initMapOverlayView(){
        Log.d(TAG, "initMapOverlayView: Started");

        mMapFragment.getMapAsync(this);
        rlMapOverlayView.setVisibility(View.VISIBLE);

        isMapOverlaySet = true;
    }

    private void settingsMapOverlayView(GoogleMap map, UiSettings settings) {
        Log.d(TAG, "settingsMapOverlayView: Started");

        switch (mapType) {

            case 1:  //Normal
                map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                break;

            case 4:  //Hybrid
                map.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                break;

            case 2: //Satellite
                map.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                break;

            case 3:  //Terrain
                map.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                break;

        }

        settings.setZoomControlsEnabled(false);  //Zoom Controls
        settings.setMyLocationButtonEnabled(true);
        settings.setCompassEnabled(true); //Compass controls shown
    }

    private void initDistanceOverlayView(){
        Log.d(TAG, "initDistanceOverlayView: Started");

        distanceToTargetView.setLocationTarget(convertPOIToLocation(mPoi));
        distanceToTargetView.setUnitsToDisplay(distanceToTargetView.UNITS_USFEET);
        distanceToTargetView.setIsTracking(true);
        rlDistanceView.setVisibility(View.VISIBLE);

        isDistanceOverlaySet = true;

    }

    //----------------------------------------------------------------------------------------------//
    private void initPointDataInBackground() {
        //Point Geodetic Load
        loadPointGeodeticInBackground();

    }

    private void loadPointGeodeticInBackground() {
        Log.d(TAG, "loadPointGeodeticInBackground: Started...");
        BackgroundGeodeticPointGet backgroundGeodeticPointGet = new BackgroundGeodeticPointGet(mContext, jobDatabaseName, this);
        backgroundGeodeticPointGet.execute();

    }

    private void setPointGeodeticAdapter() {
        Log.d(TAG, "setPointGeodeticAdapter: Started");


        rvPointList.setLayoutManager(layoutManagerPointGeodetic);
        rvPointList.setHasFixedSize(false);

        adapterGeodetic = new JobGpsStakeoutPointListAdapter(mContext, lstPointGeodetic,this);

        rvPointList.setAdapter(adapterGeodetic);


    }


    //----------------------------------------------------------------------------------------------//
    private void updateCompassView() {
        Log.d(TAG, "updateCompassView: Started");

        if (isCompassOverlaySet) {
            float azimuth = (float) mAzimuthObserved;
            float compAzimuth = (float) mAzimuthTheoretical;

            compassLinearView.setCompDegree(compAzimuth);
            compassLinearView.setDegrees(azimuth);
        }


    }

    private void updateRadarViewOrientation() {
        Log.d(TAG, "updateRadarView: Started");

        if (isRadarOverlaySet) {
            float azimuth = (float) mAzimuthObserved;

            compassRadarView.setOrientation(azimuth);
        }

    }


    private void updateRadarViewLocation(Location location) {
        Log.d(TAG, "updateRadarViewLocation: Started");

        if (location != null) {
            compassRadarView.setCurrentLocation(location);
        }

    }

    private void updateCameraOverlayViewSensorEvent(SensorEvent event) {
        Log.d(TAG, "updateCameraOverlayViewSensorEvent: Started");

        cameraOverlayView.setMSensorEvent(event);

    }

    private void updateCameraOverlayViewSensorEvent(Location currentLocation, float azimuthTo, float[] orientation) {
        Log.d(TAG, "updateCameraOverlayViewSensorEvent: Started");

        Location targetLocation = convertPOIToLocation(mPoi);
        float currentAltitude = 0.0f;
        float distanceToTarget = distanceToTarget(convertPOIToLocation(mPoi),mAveragedLocation);

        if(distanceToTarget < 500){
            cameraOverlayView.useDTMModelCurrent(true);
            cameraOverlayView.setDTMModelCurrent(cameraOverlayView.getTargetPoiFalseAltitudeMsl());
        }else if (distanceToTarget >= 500 && distanceToTarget <= 1000){
            switch (elevationServiceToUse) {
                case 0:
                    //none
                    break;

                case 1: //https://nationalmap.gov/epqs/
                    BackgroundGetNationalMapElevation backgroundGetNationalMapElevation = new BackgroundGetNationalMapElevation(this, targetLocation, this,POI_CURRENT);
                    backgroundGetNationalMapElevation.execute();
                    break;

                case 2: //http://www.geonames.org/export/web-services.html#srtm3
                    BackgroundGetSRTMElevation backgroundGetSRTMElevation = new BackgroundGetSRTMElevation(this, targetLocation, this);
                    backgroundGetSRTMElevation.execute();
                    break;
            }
        }else{
            currentAltitude = (float) mAveragedLocation.getAltitude();
        }



        if (isCameraOverlaySet) {
            cameraOverlayView.setCurrentCameraSensorData(currentLocation, azimuthTo, orientation, currentAltitude);
        }

    }

    private void updateDistanceView(){
        Log.d(TAG, "updateDistanceView: Started");

        if(isDistanceOverlaySet){
            distanceToTargetView.setLocationCurrent(mAveragedLocation);
            distanceToTargetView.setHaveLocation(true);

        }

    }

    private void updateViewBasedUponDistance(){
        Log.d(TAG, "updateViewBasedUponDistance: Started");

        if(isViewsAutomated){
            mDistanceToTarget = distanceToTarget(convertPOIToLocation(mPoi),mAveragedLocation);
            viewControllerByDistance(mDistanceToTarget);
        }

    }


    //----------------------------------------------------------------------------------------------//
    private double calculateTheoreticalAzimuth(ArvSLocationPOI mPOI) {
        Log.d(TAG, "calculateTheoreticalAzimuth: Started");

        Location targetLocation = convertPOIToLocation(mPOI);

        float bearing = mAveragedLocation.bearingTo(targetLocation);

        Log.d(TAG, "bearingToTarget: " + bearing);

        return bearing;


    }

    private float distanceToTarget(Location target, Location current){
        Log.d(TAG, "distanceToTarget: Started");

        float distanceMetric = current.distanceTo(target);

        Log.d(TAG, "distanceToTarget: " + distanceMetric);
        return distanceMetric;

    }

    private Location convertPOIToLocation(ArvSLocationPOI mPOI) {
        Log.d(TAG, "convertPOIToLocation: Started");

        Location targetLocation = new Location(mPOI.getName());
        targetLocation.setLatitude(mPOI.getLatitude());
        targetLocation.setLongitude(mPOI.getLongitude());
        targetLocation.setAltitude(mPOI.getAltitude());

        return targetLocation;

    }


    //----------------------------------------------------------------------------------------------//
    private void setUpCamera() {
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

    private void openCamera() {
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
            mCameraHorizontalFOV = (float) (2 * Math.atan(w / (maxFocus[0] * 2)));
            mCameraVerticalFOV = (float) (2 * Math.atan(h / (maxFocus[0] * 2)));

            cameraOverlayView.setCameraParamsFOV(mCameraVerticalFOV, mCameraHorizontalFOV);


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void closeCamera() {
        Log.d(TAG, "closeCamera: Started");

        if (mCaptureSession != null) {
            mCaptureSession.close();
            mCaptureSession = null;
        }

        if (mCameraDevice != null) {
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

    private void closeBackgroundThread() {
        if (backgroundHandler != null) {
            backgroundThread.quitSafely();
            backgroundThread = null;
            backgroundHandler = null;
        }
    }


    private void initStateCallback() {
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

    private void createCameraPreviewSession() {
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
    //ArvSOnSensorChangeListener-------------------------------------------------------------------//
    @Override
    public void onSensorChangedFiltered(float azimuthFrom, float azimuthTo, float[] deviceOrientation) {
        Log.d(TAG, "onSensorChangedFiltered....Started");

        handleSensorDataFiltered(azimuthTo, deviceOrientation);
    }

    @Override
    public void onSensorChangedRaw(SensorEvent event) {
        Log.d(TAG, "onSensorChangedRaw: Started");

        updateCameraOverlayViewSensorEvent(event);

    }

    //ArvSOnLocationChangeListener------------------------------------------------------------------//
    @Override
    public void onLocationChanged(Location currentLocation) {
        Log.d(TAG, "onLocationChanged...Started");

        if(!hasLocation){
            hasLocation = true;
        }

        handleLocationData(currentLocation);

    }

    @Override
    public void onFilterLocationChange(Location filtered) {

        if(!hasFilteredLocation){
            hasFilteredLocation = true;
        }

        handleFilteredLocationData(filtered);

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
        tvNavNumSats.setText(String.valueOf(noOfSats));

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

        Log.d(TAG, "onNmeaMessage: Dops (P,H,V): " + mP + "," + mH + "," + mV);
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

    private void setGmfForSensor(Location location) {
        Log.d(TAG, "setGmfForSensor: Started");

        GeomagneticField geoField = new GeomagneticField(
                (float) location.getLatitude(),
                (float) location.getLongitude(),
                (float) location.getAltitude(),
                System.currentTimeMillis());

        myCurrentAzimuthListener.setGmf(geoField);

    }

    //----------------------------------------------------------------------------------------------//
    private void initGnssWidget() {
        Log.d(TAG, "initViewWidgets: Started");

        tvNumSats = findViewById(R.id.gps_status_noSatellites);
        tvNumSatsLocked = findViewById(R.id.gps_status_noSatellitesLocked);

        tvPdopView = findViewById(R.id.PDOP_value);
        tvHdopView = findViewById(R.id.HDOP_value);
        tvVdopView =  findViewById(R.id.VDOP_value);
        tvTtff =  findViewById(R.id.ttff_value);
        tvOrientation = findViewById(R.id.orientation_value);

        tvNavNumSats = findViewById(R.id.nav_gps_status_noSatellites);

    }

    private void setGnssWidget() {
        Log.d(TAG, "setGnssWidget: Started");

        tvNumSats.setText(getString(R.string.satellite_default));

        tvPdopView.setText(getString(R.string.gps_pdop_default));
        tvHdopView.setText(getString(R.string.gps_hdop_default));
        tvVdopView.setText(getString(R.string.gps_vdop_default));


    }

    //----------------------------------------------------------------------------------------------//

    private void handleSensorData(float azimuthTo, float[] orientation) {
        Log.d(TAG, "handleSensorData: Started");

        if (mRawLocation != null) {

            float[] averagedOrientation = new float[3];
            float averageAzimithTo = 0.00F;

            addOrientationToArrayList(orientation[0]);
            addAzimithToArrayList(azimuthTo);

            boolean isShaken = isSensorOrientationShaking(orientation[0], SENSOR_ACCURACY_ERROR_ELLIPSE);

            if (isShaken) {
                tvOrientation.setText(getResources().getString(R.string.ar_sensor_status_moving));

                clearOrientationToArrayList();
                clearAzimithToArrayList();
                isMaxOrientation = false;

            } else {
                if (!isMaxOrientation) {
                    if (listSampleOrientation.size() <= SENSOR_ORIENTATION_SAMPLE_RATE) {
                        averagedOrientation = orientation;
                        averagedOrientation[0] = computeAverageOrientation();
                        mLockedOrientationValue = averagedOrientation[0];

                        mAverageOrientation = averagedOrientation;

                        averageAzimithTo = computeAverageAzimithTo();
                        mAverageAzimithTo = averageAzimithTo;

                        String myResults = getResources().getString(R.string.ar_sensor_status_static) + "[" + String.valueOf(listSampleOrientation.size() + "]");
                        tvOrientation.setText(myResults);

                        if (listSampleOrientation.size() == SENSOR_ORIENTATION_SAMPLE_RATE) {
                            isMaxOrientation = true;

                        }
                    }
                }
            }

            boolean isStationary = isGnssStationary(LOCATION_ACCURACY_ERROR_ELLIPSE);
            Location averageLocation = mRawLocation;
            String myResults;

            if (isStationary) {
                if (!isMaxLocation) {
                    if (listSampledLocations.size() <= LOCATION_ACCURACY_SAMPLE_RATE) {
                        averageLocation = computeCentroidLocation();
                        mAveragedLocation = averageLocation;

                        myResults = getResources().getString(R.string.ar_gps_status_static) + "[" + String.valueOf(lstSampledSplitLocations.size()) + "]";
                        tvTtff.setText(myResults);

                        if (listSampledLocations.size() == LOCATION_ACCURACY_SAMPLE_RATE) {
                            isMaxLocation = true;
                        }
                    }
                }

            } else {
                averageLocation = mRawLocation;
                mAveragedLocation = averageLocation;

                clearLocationArrayList();

                myResults = getResources().getString(R.string.ar_gps_status_moving);
                tvTtff.setText(myResults);
                isMaxLocation = false;
            }

            mAverageOrientation[0] = mLockedOrientationValue;
            updateCameraOverlayViewSensorEvent(mAveragedLocation, mAverageAzimithTo, mAverageOrientation);

            mAzimuthObserved = azimuthTo;
            mAzimuthTheoretical = calculateTheoreticalAzimuth(mPoi);

            updateCompassView();
            updateRadarViewOrientation();
            updateRadarViewLocation(mAveragedLocation);
            updateCurrentLocationMarker();
            updateViewBasedUponDistance();
            updateDistanceView();
        }

    }

    private void handleLocationData(Location currentLocation) {
        Log.d(TAG, "handleLocationData: Started");
        mRawLocation = new Location(currentLocation);
        mCurrentLocation = currentLocation;

        addLocationToArrayList(mRawLocation);


    }

    private void handleFilteredLocationData(Location filteredLocation){
        Log.d(TAG, "handleFilteredLocationData: Started");

        if (!isFirstPosition) {
            setGmfForSensor(filteredLocation);
            isFirstPosition = true;
        }

        mFilteredLocation = filteredLocation;
    }

    private void handleSensorDataFiltered(float azimuthTo, float[] orientation) {
        Log.d(TAG, "handleSensorData: Started");

        if (mFilteredLocation != null) {

            float[] averagedOrientation;
            float averageAzimithTo;

            addOrientationToArrayList(orientation[0]);
            addAzimithToArrayList(azimuthTo);

            boolean isShaken = isSensorOrientationShaking(orientation[0], SENSOR_ACCURACY_ERROR_ELLIPSE);

            if (isShaken) {
                tvOrientation.setText(getResources().getString(R.string.ar_sensor_status_moving));

                clearOrientationToArrayList();
                clearAzimithToArrayList();
                isMaxOrientation = false;

            } else {
                if (!isMaxOrientation) {
                    if (listSampleOrientation.size() <= SENSOR_ORIENTATION_SAMPLE_RATE) {
                        averagedOrientation = orientation;
                        averagedOrientation[0] = computeAverageOrientation();
                        mLockedOrientationValue = averagedOrientation[0];

                        mAverageOrientation = averagedOrientation;

                        averageAzimithTo = computeAverageAzimithTo();
                        mAverageAzimithTo = averageAzimithTo;

                        String myResults = getResources().getString(R.string.ar_sensor_status_static) + "[" + String.valueOf(listSampleOrientation.size() + "]");
                        tvOrientation.setText(myResults);

                        if (listSampleOrientation.size() == SENSOR_ORIENTATION_SAMPLE_RATE) {
                            isMaxOrientation = true;

                        }
                    }
                }
            }


            if(hasFilteredLocation = true){
                mAveragedLocation = mFilteredLocation;
            }else{
                mAveragedLocation = mCurrentLocation;
            }


            mAverageOrientation[0] = mLockedOrientationValue;
            updateCameraOverlayViewSensorEvent(mAveragedLocation, mAverageAzimithTo, mAverageOrientation);

            mAzimuthObserved = azimuthTo;
            mAzimuthTheoretical = calculateTheoreticalAzimuth(mPoi);

            updateCompassView();
            updateRadarViewOrientation();
            updateRadarViewLocation(mAveragedLocation);
            updateCurrentLocationMarker();
            updateViewBasedUponDistance();
            updateDistanceView();
        }

    }







    //----------------------------------------------------------------------------------------------//
    private void addLocationToArrayList(Location location) {
        Log.d(TAG, "addLocationToArrayList: Started");

        listSampledLocations.add(location);

        LatLng sample = new LatLng(location.getLatitude(), location.getLongitude());
        lstSampledSplitLocations.add(sample);

        listSampleAltitudeHeights.add((float) location.getAltitude());
        listSampleAltitudeHeightsMsl.add((float) myGnss.getAltitudeMsl());
        listSampleLocationAccuracy.add(location.getAccuracy());

    }

    private void clearLocationArrayList() {
        Log.d(TAG, "clearLocationArrayList: Started");

        listSampledLocations.clear();
        lstSampledSplitLocations.clear();
        listSampleAltitudeHeights.clear();
        listSampleAltitudeHeightsMsl.clear();
        listSampleLocationAccuracy.clear();

    }

    private boolean isGnssStationary(double accuracyMeters) {
        Log.d(TAG, "isGnssStationary: Started");

        double sampleDistanceToCurrentDistance = computeCentroidToCurrentDistance();

        Log.d(TAG, "isGnssStationary: Distance to Centroid: " + sampleDistanceToCurrentDistance);

        if (sampleDistanceToCurrentDistance > accuracyMeters) {
            return false;
        } else {
            return true;
        }

    }

    private double computeCentroidToCurrentDistance() {
        Log.d(TAG, "computeCentroidToCurrentDistance: Started");

        LatLng centroidSplit = computeCentroid(lstSampledSplitLocations);

        double latitude = centroidSplit.latitude;
        double longitude = centroidSplit.longitude;

        Location centroid = new Location("sample");
        centroid.setLatitude(latitude);
        centroid.setLongitude(longitude);

        return centroid.distanceTo(mRawLocation);


    }

    private Location computeCentroidLocation() {
        Log.d(TAG, "computeCentroidLocation: Started");

        LatLng centroidSplit = computeCentroid(lstSampledSplitLocations);
        Float centroidAltitude = computeCentroidFloat(listSampleAltitudeHeights);
        Float centroidAccuracy = computeCentroidFloat(listSampleLocationAccuracy);

        double latitude = centroidSplit.latitude;
        double longitude = centroidSplit.longitude;

        Location centroid = new Location("");
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

        return new LatLng(latitude / n, longitude / n);
    }

    private void addOrientationToArrayList(float orientation) {
        Log.d(TAG, "addOrientationToArrayList: Started");

        listSampleOrientation.add(orientation);

    }

    private void clearOrientationToArrayList() {
        Log.d(TAG, "clearOrientationToArrayList: Started");

        listSampleOrientation.clear();
    }

    private void addAzimithToArrayList(float azimithIn) {
        Log.d(TAG, "addAzimithToArrayList: Started");

        listSampleAzimithTo.add(azimithIn);

    }

    private void clearAzimithToArrayList() {
        Log.d(TAG, "clearAzimithToArrayList: Started");

        listSampleAzimithTo.clear();

    }

    private boolean isSensorOrientationShaking(float currentOrientation, double accuracyAngle) {
        Log.d(TAG, "isSensorOrientationShaking: Started");

        float diff = Math.abs(Math.abs(currentOrientation) - Math.abs(computeAverageOrientation()));

        double accuracyRadians = Math.toRadians(accuracyAngle);

        if (diff > accuracyRadians) {
            return true;
        } else {
            return false;
        }

    }

    private float computeAverageAzimithTo() {
        Log.d(TAG, "computeAverageAzimithTo: Started");

        return computeCentroidFloat(listSampleAzimithTo);

    }

    private float computeAverageOrientation() {
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
    private float getMean(List<Float> points) {
        Float sum = 0.0F;
        if (!points.isEmpty()) {
            for (Float point : points) {
                sum += point;
            }
            return sum / points.size();
        }
        return sum;
    }

    private float getVariance(List<Float> points) {
        float mean = getMean(points);
        float temp = 0;
        for (Float point : points) {
            temp += (point - mean) * (point - mean);

        }

        return temp / (points.size() - 1);

    }

    private float getStdDev(List<Float> points) {
        return (float) Math.sqrt(getVariance(points));
    }


    //----------------------------------------------------------------------------------------------//

    /**
     * Method Helpers
     */


    private void showToast(String data, boolean isShortTime) {

        if (isShortTime) {
            Toast.makeText(this, data, Toast.LENGTH_SHORT).show();

        } else {
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
    public void getNationalMapResults(String results, String units, String source, Integer var) {
        Log.d(TAG, "getNationalMapResults: Started");

        switch(var){
            case POI_TARGET:
                cameraOverlayView.useDTMModelTarget(true);
                cameraOverlayView.setTargetPoiFalseElevation(Float.parseFloat(results));
                break;

            case POI_CURRENT:
                cameraOverlayView.useDTMModelCurrent(true);
                cameraOverlayView.setDTMModelCurrent(Float.parseFloat(results));
                break;
        }

    }


    @Override
    public void getResultSRTM(String results) {
        Log.d(TAG, "getResultSRTM: Started");

        cameraOverlayView.useDTMModelTarget(true);
        cameraOverlayView.setTargetPoiFalseElevation(Float.parseFloat(results));

    }

    //----------------------------------------------------------------------------------------------//
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        return false;
    }


    //----------------------------------------------------------------------------------------------//


    @Override
    public void isMapSelectorActive(boolean isSelected) {

    }

    @Override
    public void isMapSelectorOpen(boolean isSelected) {

    }

    @Override
    public void getPointsGeodetic(ArrayList<PointGeodetic> lstPointGeodetics) {
        this.lstPointGeodetic = lstPointGeodetics;

        if(lstPointGeodetics.size() >0){
            initPOIPoint(lstPointGeodetic.get(0));
        }

        setPointGeodeticAdapter();

    }

    @Override
    public void getPointsSurvey(ArrayList<PointSurvey> lstPointSurvey) {

    }
    //----------------------------------------------------------------------------------------------//

    @Override
    public void setTargetLocation(PointGeodetic pointGeodetic) {
        initPOIPoint(pointGeodetic);

        if(drawerLayout.isDrawerOpen(GravityCompat.START)){
            drawerLayout.closeDrawer(GravityCompat.START);
        }else{
            drawerLayout.openDrawer(GravityCompat.START);
        }


    }

    //----------------------------------------------------------------------------------------------//
    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d(TAG, "onMapReady: Started");
        mMap = googleMap;

        mUiSettings = mMap.getUiSettings();
        settingsMapOverlayView(googleMap, mUiSettings);

        try {
            mMap.setMyLocationEnabled(true);
            drawTargetLocationMarker(convertPOIToLocation(mPoi));

            mMap.setOnCameraMoveStartedListener(new GoogleMap.OnCameraMoveStartedListener() {
                @Override
                public void onCameraMoveStarted(int reason) {

                    if(reason == GoogleMap.OnCameraMoveStartedListener.REASON_GESTURE){
                        Log.d(TAG, "onCameraMoveStarted: User zoom action ");

                        isMapZoomable = false;

                        if(mapZoomBlockingTimer !=null){
                            mapZoomBlockingTimer.cancel();
                        }

                        handlerOnUIThread = new Handler();

                        TimerTask task = new TimerTask() {
                            @Override
                            public void run() {
                                handlerOnUIThread.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        mapZoomBlockingTimer = null;
                                        isMapZoomable = true;
                                    }
                                });
                            }
                        };

                        mapZoomBlockingTimer = new Timer();
                        mapZoomBlockingTimer.schedule(task,10*1000);
                        Log.d(TAG, "onCameraMoveStarted: start blocking autozoom for 10 seconds");

                    }
                }
            });


        } catch (SecurityException ex) {
            Log.e(TAG, "onMapReady: " + ex);
        }

    }

    private void updateCurrentLocationMarker() {
        Log.d(TAG, "updateCurrentLocationMarker: Started");
        if (mMap !=null) {
            if(!isTargetZoomDone){
                zoomToTarget();
            }else
                zoomMapTo(mAveragedLocation);
                //zoomExtents();

            if(mDistanceToTarget < 50.0F){
                drawPathToTarget();
            }

        }
    }


    private void drawTargetLocationMarker(Location targetLocation) {
        Log.d(TAG, "drawTargetLocationMarker: Started");
        LatLng latlng = new LatLng(targetLocation.getLatitude(), targetLocation.getLongitude());

        if (mMap != null) {
            targetMarker = mMap.addMarker(new MarkerOptions()
                    .position(latlng)
                    .title("Target")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));

        }
    }

    private void zoomToTarget(){
        Log.d(TAG, "zoomToTarget: Started");

        Location targetLocation = convertPOIToLocation(mPoi);
        LatLng targetLatLng = new LatLng(targetLocation.getLatitude(), targetLocation.getLongitude());

        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(targetLatLng)                               // Sets the center of the map to Target
                .zoom(17.5f)                                           // Sets the zoom
                .bearing((float) mAzimuthTheoretical)               // Sets the orientation of the camera to north
                .tilt(30)                   // Sets the tilt of the camera to 30 degrees
                .build();                   // Creates a CameraPosition from the builder


        mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        isTargetZoomDone = true;
    }


    private void zoomMapTo(Location location){
        Log.d(TAG, "zoomMapTo: Started");
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());

        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(latLng)                               // Sets the center of the map to Target
                .zoom(18.5f)                                           // Sets the zoom
                .bearing((float) mAzimuthTheoretical)               // Sets the orientation of the camera to north
                .tilt(15)                   // Sets the tilt of the camera to 30 degrees
                .build();                   // Creates a CameraPosition from the builder



        if(isMapZoomable){
            try{
                isMapZoomable = false;
                mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition), new GoogleMap.CancelableCallback() {
                    @Override
                    public void onFinish() {
                        isMapZoomable = true;
                    }

                    @Override
                    public void onCancel() {
                        isMapZoomable = true;
                    }
                });
            }catch (Exception e){
                e.printStackTrace();
            }
        }

    }


    private void zoomExtents() {
        Log.d(TAG, "zoomExtents: Started");

        if (isMapZoomable) {
            try {
                isMapZoomable = false;
                LatLng currentLatLng = new LatLng(mAveragedLocation.getLatitude(), mAveragedLocation.getLongitude());

                LatLngBounds.Builder builder = new LatLngBounds.Builder();

                builder.include(targetMarker.getPosition());
                builder.include(currentLatLng);

                LatLngBounds bounds = builder.build();

                int width = getResources().getDisplayMetrics().widthPixels;
                int padding = (int) (width * 0.10); // offset from edges of the map 10% of screen

                CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
                mMap.moveCamera(cu);
            } catch (Exception e) {
                LatLng currentLocation = new LatLng(mAveragedLocation.getLatitude(), mAveragedLocation.getLongitude());
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 13));
            }
        }

    }

    private void drawPathToTarget() {
        Log.d(TAG, "drawPathToTarget: Started");

        if(listMapPolylines.size() >0){
            clearPathToTarget();
        }

        LatLng currentLatLng = new LatLng(mAveragedLocation.getLatitude(), mAveragedLocation.getLongitude());

        Location targetLocation = convertPOIToLocation(mPoi);
        LatLng targetLatLng = new LatLng(targetLocation.getLatitude(), targetLocation.getLongitude());

        Polyline targetPath = mMap.addPolyline(new PolylineOptions()
                .add(currentLatLng, targetLatLng)
                .geodesic(false)
                .width(10)
                .color(Color.parseColor("#801B60FE")));

        listMapPolylines.add(targetPath);

    }

    private void clearPathToTarget(){
        Log.d(TAG, "clearPathToTarget: Started");

        for (Polyline line: listMapPolylines){
            line.remove();
        }
        listMapPolylines.clear();

    }

    //----------------------------------------------------------------------------------------------//

    private void viewControllerByDistance(float distance){
        Log.d(TAG, "viewControllerByDistance: Started");
        Log.d(TAG, "viewControllerByDistance: Distance: " + distance);
        previousStageMap = stageMap;
        float rangeLower = 10.00F, rangeHigh = 50.00F;

        //in meters
        Range rangeForSplit= new Range<>(rangeLower, rangeHigh);

        if(distance > rangeHigh){
            Log.d(TAG, "viewControllerByDistance: Larger than range High");
            stageMap = STAGE_FULL;
        }else if(rangeForSplit.contains(distance)){
            Log.d(TAG, "viewControllerByDistance: In between");
            stageMap = STAGE_HALF;
        }else{
            Log.d(TAG, "viewControllerByDistance: Close to target");
            stageMap = STAGE_HIDDEN;
        }

        Log.d(TAG, "viewControllerByDistance: Previous Stage: " + previousStageMap);
        Log.d(TAG, "viewControllerByDistance: Current Stage:" + stageMap);

        if(previousStageMap != stageMap){
            viewController();
        }

    }

    private void viewMapControllerByButton(){
        Log.d(TAG, "viewMapControllerByButton: Started");
        previousStageMap = stageMap;

        switch (stageMap){
            case STAGE_HIDDEN:
                stageMap = STAGE_HALF;

                break;
            case STAGE_HALF:
                stageMap = STAGE_FULL;
                break;
            case STAGE_FULL:
                stageMap = STAGE_HIDDEN;
                break;
        }

        if(previousStageMap != stageMap){
            viewController();
        }
    }


    private void viewController(){
        Log.d(TAG, "viewController: Started");

        switchMapView();

        switch (stageMap){
            case STAGE_HIDDEN:
                stageCamera = STAGE_FULL;

                break;
            case STAGE_HALF:
                stageCamera = STAGE_HALF;

                break;
            case STAGE_FULL:
                stageCamera = STAGE_HIDDEN;

                break;
        }

        Log.d(TAG, "viewController: stageMap/stageCamera:" + stageMap + "," + stageCamera);
        switchCameraView();

    }
    
    
    private void switchMapView(){
        Log.d(TAG, "switchMapView: Started");

        rlMapOverlayView.getLayoutTransition().enableTransitionType(LayoutTransition.CHANGING);
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) rlMapOverlayView.getLayoutParams();

        switch(stageMap){
            case STAGE_HIDDEN:
                params.addRule(RelativeLayout.BELOW, R.id.bottomShim);

                break;
            case STAGE_HALF:
                params.addRule(RelativeLayout.BELOW, R.id.centerShim);

                break;
            case STAGE_FULL:
                params.addRule(RelativeLayout.BELOW, R.id.appBar);

                break;

        }
    }

    private void switchCameraView(){
        Log.d(TAG, "switchCameraView: Started");

        rlCameraView.getLayoutTransition().enableTransitionType(LayoutTransition.CHANGING);
        RelativeLayout.LayoutParams paramsCamera= (RelativeLayout.LayoutParams) rlCameraView.getLayoutParams();

        rlCameraOverlayView.getLayoutTransition().enableTransitionType(LayoutTransition.CHANGING);
        RelativeLayout.LayoutParams paramsCameraOverlay= (RelativeLayout.LayoutParams) rlCameraOverlayView.getLayoutParams();

        switch(stageCamera){
            case STAGE_HIDDEN:
                paramsCamera.addRule(RelativeLayout.ABOVE, R.id.appBar);
                paramsCameraOverlay.addRule(RelativeLayout.ABOVE, R.id.appBar);
                break;

            case STAGE_HALF:
                paramsCamera.addRule(RelativeLayout.ABOVE,R.id.centerShim);
                paramsCamera.addRule(RelativeLayout.BELOW, R.id.appBar);

                paramsCameraOverlay.addRule(RelativeLayout.ABOVE,R.id.centerShim);
                paramsCameraOverlay.addRule(RelativeLayout.BELOW, R.id.appBar);
                break;

            case STAGE_FULL:
                paramsCamera.addRule(RelativeLayout.BELOW, R.id.appBar);
                paramsCamera.removeRule(RelativeLayout.ABOVE);

                paramsCameraOverlay.addRule(RelativeLayout.BELOW, R.id.appBar);
                paramsCameraOverlay.removeRule(RelativeLayout.ABOVE);
                break;

        }

        rlCameraOverlayView.invalidate();

    }

    //----------------------------------------------------------------------------------------------//
    private class LoadGPSDataTask extends AsyncTask<Void, Void, Void> {

        private ProgressDialog pd;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            pd = new ProgressDialog(mContext);
            pd.setMessage("Please wait");
            pd.setTitle("Looking for GPS Data");
            pd.setCancelable(false);
            pd.show();

        }


        @Override
        protected Void doInBackground(Void... params) {
            Long t = Calendar.getInstance().getTimeInMillis();
            while(!hasFilteredLocation && Calendar.getInstance().getTimeInMillis()-t < 30000){
                try {
                    Thread.sleep(1000);
                    publishProgress();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            return null;

        }


        @Override
        protected void onProgressUpdate(Void... values) {

            if(hasLocation && !hasFilteredLocation){
                pd.setTitle("Looking for Best GPS Data");
            }

            if(hasLocation && hasFilteredLocation){
                pd.setTitle("Found, Finishing Setup");
            }


        }

        @Override
        protected void onPostExecute(Void result) {

            if(hasLocation && hasFilteredLocation){
                rlGPSError.setVisibility(View.GONE);
                initCompassLinearView();
                initRadarOverlayView();
                initCameraOverlayView();
                initMapOverlayView();
                initDistanceOverlayView();
                tvTtff.setText("Post");

            }else{
                rlGPSError.setVisibility(View.VISIBLE);
                tvTtff.setText("No Fixed");

            }

            if (pd.isShowing()) {
                pd.dismiss();
            }


        }

    }
}
