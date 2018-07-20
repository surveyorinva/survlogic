package com.survlogic.survlogic.ARvS;

import android.animation.LayoutTransition;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.LinearInterpolator;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.survlogic.survlogic.ARvS.dialog.DialogGPSSurveyMeasureResults;
import com.survlogic.survlogic.ARvS.interf.GPSMeasureHelperListener;
import com.survlogic.survlogic.ARvS.interf.POIHelperListener;
import com.survlogic.survlogic.ARvS.utils.CameraService;
import com.survlogic.survlogic.ARvS.utils.GPSMeasureHelper;
import com.survlogic.survlogic.ARvS.utils.GetPOIHelper;
import com.survlogic.survlogic.ARvS.utils.GnssService;
import com.survlogic.survlogic.ARvS.utils.SensorService;
import com.survlogic.survlogic.R;
import com.survlogic.survlogic.dialog.DialogJobPointView;
import com.survlogic.survlogic.model.PointGeodetic;
import com.survlogic.survlogic.utils.GPSLocationConverter;
import com.survlogic.survlogic.utils.PreferenceLoaderHelper;
import com.survlogic.survlogic.utils.RumbleHelper;
import com.survlogic.survlogic.utils.StringUtilityHelper;
import com.survlogic.survlogic.utils.SurveyMathHelper;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

public class JobGPSSurveyWork extends AppCompatActivity implements POIHelperListener, GPSMeasureHelperListener, OnMapReadyCallback {
    private static final String TAG = "JobGPSSurveyArvTActivit";

    private Context mContext;

    //Preferences
    private PreferenceLoaderHelper preferenceLoaderHelper;
    private boolean prefUsePredictedLocation = false;
    private boolean prefUseSensorGMF = false;

    //Location Service
    public GnssService locationService;
    private BroadcastReceiver locationUpdateReceiver;
    private BroadcastReceiver predictedLocationReceiver;
    private BroadcastReceiver locationMetadataReceier;
    private BroadcastReceiver gnssStatusReceiver;
    private BroadcastReceiver batteryCriticalReceiver;

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
    private int mSensorAzimuth = 0, mSensorAzimuthPrevious = 0;
    private boolean sendSensorWarning = false;
    private boolean useFusionSensor = false;
    private float mSensorAlpha = 0.125f;
    private boolean hasGMFSensorLocationSent = false;

    //App Bar Widget
    private int currentMode;
    private static final int MODE_SURVEY = 0, MODE_STAKE = 1;

    //Widgets
    private RelativeLayout rlGPSErrorMessage;
    private FloatingActionButton fabMenu;
    private Switch swAppBarSwitch;
    private ImageButton ibSettings;
    private DrawerLayout mDrawerLayout;

    private boolean isDrawerOpen = false;

    //---------------------------------------------------------------------------------------------- View Controller
    private int view_stage_map = VIEW_MAP_START, view_stage_map_previous;
    private static final int VIEW_MAP_FULL = 1, VIEW_MAP_HALF = 2, VIEW_MAP_HIDDEN = 0, VIEW_MAP_START = -1;

    //---------------------------------------------------------------------------------------------- GPS Hub View
    //System Variables
    private int criteriaGpsFilteredCount = 3, criteriaGpsRawCount = 5;
    private static final int LOCATION_METADATA_SPEED = 0, LOCATION_METADATA_BEARING = 1;
    private int cycleHudMetadata = 0;

    private boolean hasGPSHubInit = false;
    private boolean isShownOptionsStatus = false, isShownOptionsSensor = false;
    private boolean isShownOptionsMetadata = true;
    private boolean isPOIPOintActive = false;
    private static final boolean LOCATION_RAW = false, LOCATION_PREDICT = true;
    private static final boolean SENSOR_RAW = false, SENSOR_GMF = true;

    //Widgets
    private RelativeLayout rlGPSHUD, rlGPSHUDSat, rlGPSOptionsSat, rlGPSOptionsSensor;
    private RelativeLayout rlGPSHUDLocationMetadata;
    private RelativeLayout rlGPSStakePointID, rlGPSStakePointDirection;
    private LinearLayout llGPSHUDCompass;

    private TextView tvGpsHud_Location_Header, tvGpsHud_Sensor_Header;
    private TextView tvGpsHud_Status_noSatellites, tvGpsHud_Status_noSatellitesLocked;
    private TextView tvGPSHud_Location_Lat, tvGpsHud_Location_Lon, tvGpsHud_Location_Alt;
    private TextView tvGpsHud_Location_Metadata, tvGpsHud_Location_Metadata_Header;
    private TextView tvGpsHud_Status_Value, tvGpsHud_Accuracy_Value;
    private TextView tvGpsHud_Sensor_Orientation, tvGpsHud_Sensor_Pitch, tvGpsHud_Sensor_Roll;
    private TextView tvGpsHud_Stakeout_Point_Id;


    //---------------------------------------------------------------------------------------------- GNSS Measure
    private GPSMeasureHelper gpsMeasureHelper;
    private ArrayList<Location> measuringLocationList;

    private boolean isMeasuringPosition = false;
    private boolean hasGPSMeasureInit = false;

    //---------------------------------------------------------------------------------------------- Map Overlay View
    //API
    private SupportMapFragment mMapFragment;
    private GoogleMap mMap;
    private UiSettings mUiSettings;

    //System Variables
    private boolean isMapShown = true;
    private boolean hasMapViewInit = false;
    private boolean isMapViewZoomable;
    private boolean didMapViewInitialZoom;
    private boolean isMarkerRotating = false;
    private Timer zoomBlockingTimer;
    private Handler handlerMapViewOnUIThread;

    //Camera Positions
    private static final int CAMERA_POSITION_NORTH = 0, CAMERA_POSITION_SENSOR = 1, CAMERA_POSITION_SPEED = 2;
    private static final float MOTION_THRESHOLD = 1.4f;

    private int mCurrentCameraPosition = CAMERA_POSITION_NORTH;
    private float camera_bearing_sensor = 0, camera_bearing_speed = 0;

    //Mapping Symbols
    private Marker userPositionMarker;
    private BitmapDescriptor userPositionMarkerBitmapDescriptor;
    private Circle locationAccuracyCircle;

    //Widgets
    private RelativeLayout rlMapOverlayView;

    //---------------------------------------------------------------------------------------------- Camera
    private CameraService cameraService;

    private RelativeLayout rlCameraView;
    private TextureView txvCameraView;
    private boolean isCameraInit = false;
    private boolean isCameraStarted = false;

    //---------------------------------------------------------------------------------------------- Data
    private String mJob_DatabaseName;
    private int mProject_id, mJob_id;

    private boolean isDataSetup = false;
    private GetPOIHelper getPOIHelper;

    private Location mCurrentLocation;

    private RecyclerView rlDataView;
    private ImageButton ibDataAction1, ibDataAction2;

    private ProgressBar progressBarData;
    private RelativeLayout rlDataWarning;

    //Method Constants
    private int card_type = TYPE_NONE;
    private static final int TYPE_NONE = 0, TYPE_INTERNAL_JOB = 1, TYPE_EXTERNAL_NGS = 2;

    //----------------------------------------------------------------------------------------------

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        Log.d(TAG, "Activity Started-------------------------------------------->");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gps_ar_v2);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        mContext = JobGPSSurveyWork.this;
        preferenceLoaderHelper = new PreferenceLoaderHelper(mContext);

        bindLocationService();
        bindSensorService();

        startCameraService();
        startDataService();

        loadPreferences();
        initActivityWidgets();
        setupBoardViews();   //setup the correct location for views

        LoadGPSDataTask loadGPSDataTask = new LoadGPSDataTask();
        loadGPSDataTask.execute();

    }

    @Override
    protected void onPause() {
        super.onPause();

        unregisterLocationReceiver();
        unregisterBatteryWarningReceiver();
        unregisterSensorReceiver();
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerLocationReceiver();
        registerBatteryWarningReceiver();
        registerSensorReceiver();

        loadPreferences();

        if(isCameraStarted){
            cameraService.onResume();
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();

        if(isCameraStarted){
            cameraService.onStop();
        }

    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onBackPressed() {
        if(gpsMeasureHelper.isGPSMeasureSetupMenuOpen()){
            gpsMeasureHelper.requestSetupMenuClose();
            return;
        }

        if(isDrawerOpen){
            mDrawerLayout.closeDrawer(GravityCompat.START);
            return;
        }


    }

    //---------------------------------------------------------------------------------------------- Location Service
    private void bindLocationService(){
        Log.d(TAG, "bindLocationService: Started");

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


    //----------------------------------------------------------------------------------------------Receivers
    private void registerLocationReceiver(){
        Log.d(TAG, "registerLocationReceiver: Started");

        locationUpdateReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Location rawLocation = intent.getParcelableExtra("location");

                gpsRawCount++;

                if(!hasLocation){
                    if(gpsRawCount > criteriaGpsRawCount){
                        hasLocation = true;
                    }
                }else{
                    updateHudLocation(LOCATION_RAW,rawLocation);
                    updateMapLocation(LOCATION_RAW,rawLocation);
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

                if(!hasFilteredLocation){
                    if(gpsFilteredCount > criteriaGpsFilteredCount){
                        hasFilteredLocation = true;
                    }
                }else{
                    mCurrentLocation = predicatedLocation;

                    updateSensorWithLocation(predicatedLocation);

                    updateHudLocation(LOCATION_PREDICT,predicatedLocation);
                    updateMapLocation(LOCATION_PREDICT,predicatedLocation);

                    if(isMeasuringPosition){
                        measuringLocationList.add(mCurrentLocation);
                    }

                }

            }
        };

        LocalBroadcastManager.getInstance(this).registerReceiver(
                predictedLocationReceiver,
                new IntentFilter("PredictLocation")
        );

        //------------------------------------------------------------------------------------------

        gnssStatusReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                int mSvCount = 0;
                int mUsedInFixCount = 0;

                mSvCount = intent.getIntExtra("svCount",mSvCount);
                mUsedInFixCount = intent.getIntExtra("usedInFixCount",mUsedInFixCount);

                if(hasFilteredLocation){
                    updateHudGnssStatus(mSvCount,mUsedInFixCount);
                }

            }
        };

        LocalBroadcastManager.getInstance(this).registerReceiver(
                gnssStatusReceiver,
                new IntentFilter("GnssStatus")
        );

    //----------------------------------------------------------------------------------------------
        locationMetadataReceier = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                float mAzimuth = 0;
                float mSpeed = 0;

                mAzimuth = intent.getFloatExtra("azimuth",mAzimuth);
                mSpeed = intent.getFloatExtra("speed",mSpeed);

                if(hasFilteredLocation){
                    updateHudLocationMetadata(mSpeed,mAzimuth);

                    if(isMapShown && currentMode == MODE_SURVEY) {
                        if (mSpeed > MOTION_THRESHOLD) {

                            camera_bearing_speed = mAzimuth;
                            Log.d(TAG, "locationMetadataReceier: " + CAMERA_POSITION_SPEED);
                            mCurrentCameraPosition = CAMERA_POSITION_SPEED;

                        }
                    }
                }

            }
        };

        LocalBroadcastManager.getInstance(this).registerReceiver(
                locationMetadataReceier,
                new IntentFilter("GPSLocationMetadata")
        );


    }

    private void unregisterLocationReceiver(){
        Log.d(TAG, "unregisterLocationReceiver: Started");

        try{
            if(locationUpdateReceiver !=null){
                unregisterReceiver(locationUpdateReceiver);

            }

            if (predictedLocationReceiver != null) {
                unregisterReceiver(predictedLocationReceiver);
            }

            if(gnssStatusReceiver !=null){
                unregisterReceiver(gnssStatusReceiver);
            }

        }catch (IllegalArgumentException ex){
            ex.printStackTrace();
        }

    }

    //------------------------------------------------------------------------------------------

    private void registerBatteryWarningReceiver(){
        Log.d(TAG, "registerBatteryWarningReceiver: Started");

        batteryCriticalReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                float batteryScaledLevel = 0;
                batteryScaledLevel = intent.getFloatExtra("batteryLevel",batteryScaledLevel);

                //Todo send warning to UI to slow datastream down on GPS data

                locationService.setBatteryWarningSurpressed(true);
            }
        };

        LocalBroadcastManager.getInstance(this).registerReceiver(
                batteryCriticalReceiver,
                new IntentFilter("BatteryWarning")
        );


    }

    private void unregisterBatteryWarningReceiver(){
        Log.d(TAG, "unregisterBatteryWarningReceiver: Started");

        try{
            if(batteryCriticalReceiver !=null){
                unregisterReceiver(batteryCriticalReceiver);
            }
        }catch (IllegalArgumentException ex){
            ex.printStackTrace();
        }

    }

    //---------------------------------------------------------------------------------------------- Sensor Service

    private void bindSensorService(){
        Log.d(TAG, "bindSensorService: Started");

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

    //--------------------------------------------------------------------------------------------------

    private void registerSensorReceiver(){
        Log.d(TAG, "registerSensorReceiver: Started");

        sensorDataReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                int sensorOrientation = 0;
                float sensorPitch = 0;
                float sensorRoll = 0;

                sensorOrientation = intent.getIntExtra("azimuth",sensorOrientation);
                sensorPitch = intent.getFloatExtra("pitch",sensorPitch);
                sensorRoll = intent.getFloatExtra("roll",sensorRoll);

                Log.d(TAG, "onReceive: Sensor Azimuth:" + sensorOrientation);
                Log.d(TAG, "onReceive: Sensor Pitch:" + sensorPitch);
                Log.d(TAG, "onReceive: Filtered Location: " + hasFilteredLocation);

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

                Log.d(TAG, "onReceive: Sensor Azimuth:" + sensorOrientation);
                Log.d(TAG, "onReceive: Sensor Pitch:" + sensorPitch);
                Log.d(TAG, "onReceive: Filtered Location: " + hasFilteredLocation);

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
        Log.d(TAG, "unregisterSensorReceiver: Started");

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
        Log.d(TAG, "evaluateSensorData: Started");


        if(hasGMFSensorLocationSent){

            if (prefUseSensorGMF == type) {
                mSensorAzimuth = orientation;
                updateHudSensors(orientation,pitch,roll);
            }
        }else{

            if(type == SENSOR_RAW){
                mSensorAzimuth = orientation;
                updateHudSensors(orientation,pitch,roll);
            }

        }

    }

    private void updateSensorWithLocation(Location location){
        Log.d(TAG, "updateSensorWithLocation: Started");

        if(!hasGMFSensorLocationSent){

            sensorService.setSensorLocation(location);
            sensorService.setHasLocation(true);

            hasGMFSensorLocationSent = true;

        }else{
            sensorService.setSensorLocation(location);
        }

    }

    private void evaluateSensorAccuracy(boolean mWarning){
        Log.d(TAG, "evaluateSensorAccuracy: Started");

        if(!sendSensorWarning){
            showToast("Sensor Accuracy Low, Consider Calibrating", false);
            sendSensorWarning = true;
        }

    }

    //---------------------------------------------------------------------------------------------- Camera Service
    private void startCameraService(){
        Log.d(TAG, "startCameraService: Started");

        rlCameraView = findViewById(R.id.rl_camera_view);
        txvCameraView = findViewById(R.id.camera_view);

        cameraService = new CameraService(mContext,txvCameraView);
        isCameraInit = cameraService.init();
    }


    //---------------------------------------------------------------------------------------------- Data Service

    private void startDataService(){
        Log.d(TAG, "startDataService: Started");

        Bundle extras = getIntent().getExtras();
        final String jobDatabaseName = extras.getString(getString(R.string.KEY_JOB_DATABASE));

        mProject_id = extras.getInt(getString(R.string.KEY_PROJECT_ID));
        mJob_id = extras.getInt(getString(R.string.KEY_JOB_ID));
        mJob_DatabaseName = extras.getString(getString(R.string.KEY_JOB_DATABASE));

        if(!isDataSetup){
            getPOIHelper = new GetPOIHelper(mContext,this);

            rlDataView = findViewById(R.id.pointListRecyclerView);
            getPOIHelper.initViewWidgets(rlDataView);

            ibDataAction1 = findViewById(R.id.points_action_1);
            ibDataAction2 = findViewById(R.id.points_action_2);

            progressBarData = findViewById(R.id.progress_bar_getting_data);
            rlDataWarning = findViewById(R.id.rl_no_data_message);

            ibDataAction1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    card_type = TYPE_INTERNAL_JOB;
                    progressBarData.setVisibility(View.VISIBLE);

                    final Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            boolean results = getPOIHelper.buildInternalJobData(jobDatabaseName);

                            if(results){
                                progressBarData.setVisibility(View.INVISIBLE);
                                rlDataView.setVisibility(View.VISIBLE);
                                rlDataWarning.setVisibility(View.INVISIBLE);
                            }
                        }
                    }, 500);

                }
            });

            ibDataAction2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    card_type = TYPE_EXTERNAL_NGS;
                }
            });



            isDataSetup = true;
        }


    }

    //---------------------------------------------------------------------------------------------- Getters and Setters


    public int getCriteriaGpsFilteredCount() {
        return criteriaGpsFilteredCount;
    }

    public void setCriteriaGpsFilteredCount(int criteriaGpsFilteredCount) {
        this.criteriaGpsFilteredCount = criteriaGpsFilteredCount;
    }

    public int getCriteriaGpsRawCount() {
        return criteriaGpsRawCount;
    }

    public void setCriteriaGpsRawCount(int criteriaGpsRawCount) {
        this.criteriaGpsRawCount = criteriaGpsRawCount;
    }

    //---------------------------------------------------------------------------------------------- Preferences

    private void loadPreferences() {
        Log.d(TAG, "loadPreferences: Started");

        prefUsePredictedLocation = preferenceLoaderHelper.getGPSLocationUsePrediction();
        useFusionSensor = preferenceLoaderHelper.getGPSLocationSensorUseFusion();
        prefUseSensorGMF = preferenceLoaderHelper.getGPSLocationSensorUseGMF();

        updateHudPreferences();

    }


    //---------------------------------------------------------------------------------------------- Activity View Methods
    private void initActivityWidgets(){
        Log.d(TAG, "initActivityWidgets: Started");

        TextView tvAppBarTitle = findViewById(R.id.DrawerLayout);
        tvAppBarTitle.setText(getResources().getText(R.string.gps_title_header));

        ImageButton ibAppBarBackButton = findViewById(R.id.button_back_navigation_pane);
        ibAppBarBackButton.setVisibility(View.VISIBLE);
        ibAppBarBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                locationService.stopGPS();
                finish();
            }
        });

        final DrawerLayout drawerLayout = findViewById(R.id.drawer_in_gps_ar);
        drawerLayout.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {

                if(hasFilteredLocation){
                    if(isDataSetup){
                        getPOIHelper.setCurrentLocation(mCurrentLocation);
                    }

                }

            }

            @Override
            public void onDrawerOpened(View drawerView) {
                isDrawerOpen = true;
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                isDrawerOpen = false;

            }

            @Override
            public void onDrawerStateChanged(int newState) {

            }
        });
        mDrawerLayout = drawerLayout;

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

        //Views
        rlGPSErrorMessage = findViewById(R.id.rl_error_message);

        //Widgets
        ibSettings = findViewById(R.id.settings_btn);
        ibSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(JobGPSSurveyWork.this,ArvTSettings.class));
            }
        });

        initGPSViewWidgets();
        initGPSMeasureWidgets();
        initMapViewWidget();

        //Actions
        initSwitchActions();

    }

    private void initSwitchActions(){
        Log.d(TAG, "onClickListeners: Started");

        LinearLayout llAppBarSwitch = findViewById(R.id.ll_switch);
        swAppBarSwitch = findViewById(R.id.switch_value);
        TextView tvAppBarSwitchOff = findViewById(R.id.switch_lbl_off);
        TextView tvAppBarSwitchOn = findViewById(R.id.switch_lbl_on);

        currentMode = MODE_SURVEY;  // For use by Switch - Sets default view to the collection of survey data.

        tvAppBarSwitchOff.setText(getResources().getText(R.string.gps_app_switch_off));
        tvAppBarSwitchOn.setText(getResources().getText(R.string.gps_app_switch_on));

        swAppBarSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if(!isChecked){
                    currentMode = MODE_SURVEY;

                    incrementMapView();
                    cycleCameraView();
                    showGPSViewSurveyMeasureActions();
                    showGPSViewStakeoutPointInfo();

                    isCameraStarted = cameraService.onStop();
                }else{
                    currentMode = MODE_STAKE;

                    incrementMapView();
                    cycleCameraView();
                    showGPSViewSurveyMeasureActions();
                    isCameraStarted = cameraService.onStart();

                }
            }
        });

    }

    private void showSwitchWidget(){
        Log.d(TAG, "showSwitchWidget: Started");
        LinearLayout llAppBarSwitch = findViewById(R.id.ll_switch);


        if(locationLoadedSuccess){
            llAppBarSwitch.setVisibility(View.VISIBLE);
        }

    }


    //---------------------------------------------------------------------------------------------- View Methods - No Data or API, JUST VIEWS
    private void setupBoardViews(){   //This resets the view for use by the UI.  Forces out of Design mode
        Log.d(TAG, "setupBoardViews: Started");
        view_stage_map = VIEW_MAP_START;
        cycleMapView();
    }



    private void switchBoardViewWidgets(){
        Log.d(TAG, "switchBoardViewWidgets: Started");

        showGPSViewWidgets();
        showGPSMeasureWidgets();
        showMapViewWidgets();


        if(currentMode == MODE_SURVEY){
            setupMapView();

        }else{
            // Place here the init of stake widget views
            incrementMapView();
        }

    }

    //---------------------------------------------------------------------------------------------- View Controller  - No Data or API, JUST VIEWS
    private void setupMapView(){
        Log.d(TAG, "setupMapView: Started");

        view_stage_map = VIEW_MAP_FULL;
        cycleMapView();

    }


    private void showMapViewWidgets(){
        Log.d(TAG, "showMapViewWidgets: Started");

        rlMapOverlayView.setVisibility(View.VISIBLE);

    }


    private void incrementMapView(){
        Log.d(TAG, "viewMapControllerByButton: Started");
        view_stage_map_previous = view_stage_map;

        switch (view_stage_map){

            case VIEW_MAP_START:
                view_stage_map = VIEW_MAP_FULL;
                break;
            case VIEW_MAP_HIDDEN:
                view_stage_map = VIEW_MAP_HALF;
                break;
            case VIEW_MAP_HALF:
                view_stage_map = VIEW_MAP_FULL;
                break;
            case VIEW_MAP_FULL:
                view_stage_map = VIEW_MAP_HALF;
                break;
        }

        if(view_stage_map_previous != view_stage_map){
            cycleMapView();
        }
    }

    private void cycleMapView(){
        Log.d(TAG, "cycleMapView: Started");
        rlMapOverlayView.getLayoutTransition().enableTransitionType(LayoutTransition.CHANGING);
        rlMapOverlayView.getLayoutTransition().addTransitionListener(new LayoutTransition.TransitionListener() {
            @Override
            public void startTransition(LayoutTransition layoutTransition, ViewGroup viewGroup, View view, int i) {

            }

            @Override
            public void endTransition(LayoutTransition layoutTransition, ViewGroup viewGroup, View view, int i) {
                if(i==LayoutTransition.CHANGING){
                    if(view_stage_map == VIEW_MAP_HALF){
                        curveMapView(false);
                    }else{
                        curveMapView(true);
                    }
                }
            }
        });
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) rlMapOverlayView.getLayoutParams();

        switch(view_stage_map){
            case VIEW_MAP_START:
                params.removeRule(RelativeLayout.BELOW);
                params.addRule(RelativeLayout.ABOVE, R.id.appBar);

                break;
            case VIEW_MAP_HALF:
                params.addRule(RelativeLayout.BELOW, R.id.mapShim);
                params.addRule(RelativeLayout.ABOVE, R.id.bottomShim);

                break;
            case VIEW_MAP_FULL:
                params.addRule(RelativeLayout.BELOW, R.id.appBar);
                params.addRule(RelativeLayout.ABOVE, R.id.bottomShim);

                break;

            case VIEW_MAP_HIDDEN:
                params.addRule(RelativeLayout.BELOW, R.id.bottomShim);
                params.removeRule(RelativeLayout.ABOVE);

                break;

        }
    }

    private void cycleCameraView(){
        Log.d(TAG, "cycleCameraView: Started");

        if(currentMode == MODE_SURVEY ){
            rlCameraView.setVisibility(View.INVISIBLE);
        }else{
            rlCameraView.setVisibility(View.VISIBLE);
        }


    }

    private void curveMapView(boolean curveMapView){
        Log.d(TAG, "animateMapView: Started");
        final boolean viewType = curveMapView;
        final com.github.florent37.shapeofview.shapes.ArcView view = findViewById(R.id.view_container_map);

        Log.d(TAG, "curveMapView: " + curveMapView);

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if(viewType){
                    view.setArcHeight((int) getResources().getDimension(R.dimen.custom_arc_full));
                }else{
                    view.setArcHeight((int) getResources().getDimension(R.dimen.custom_arc_none));
                }
            }
        },1000);


    }


    //---------------------------------------------------------------------------------------------- gpsHubView

    private void initGPSViewWidgets(){

        rlGPSHUD = findViewById(R.id.rl_gnss_info);

        rlGPSHUDSat = findViewById(R.id.rl_info_sat);
        llGPSHUDCompass = findViewById(R.id.rl_compass_info);

        rlGPSHUDLocationMetadata = findViewById(R.id.rl_location_metadata);

        rlGPSOptionsSat = findViewById(R.id.rl_option_sat);
        rlGPSOptionsSensor = findViewById(R.id.rl_option_sensor);

        rlGPSStakePointID = findViewById(R.id.rl_destination_info);
        rlGPSStakePointDirection = findViewById(R.id.rl_destination_bearing);

        tvGpsHud_Location_Header = findViewById(R.id.gnss_location_header);
        tvGpsHud_Sensor_Header = findViewById(R.id.compass_bearing_header);

        tvGpsHud_Status_noSatellites = findViewById(R.id.gps_status_noSatellites);
        tvGpsHud_Status_noSatellitesLocked = findViewById(R.id.gps_status_noSatellitesLocked);

        tvGPSHud_Location_Lat = findViewById(R.id.gps_location_lat);
        tvGpsHud_Location_Lon = findViewById(R.id.gps_location_lon);
        tvGpsHud_Location_Alt = findViewById(R.id.gps_location_Alt);

        tvGpsHud_Location_Metadata_Header = findViewById(R.id.location_metadata_lbl);
        tvGpsHud_Location_Metadata = findViewById(R.id.location_metadata_value);

        tvGpsHud_Status_Value = findViewById(R.id.gps_status_value);
        tvGpsHud_Accuracy_Value = findViewById(R.id.gps_accuracy_Value);

        tvGpsHud_Sensor_Orientation = findViewById(R.id.compass_orientation_value);
        tvGpsHud_Sensor_Pitch = findViewById(R.id.compass_pitch_value);
        tvGpsHud_Sensor_Roll = findViewById(R.id.compass_roll_value);

        tvGpsHud_Stakeout_Point_Id = findViewById(R.id.destination_value);

        setGPSHUDOnClickListeners();

        hasGPSHubInit = true;
    }

    private void setGPSHUDOnClickListeners(){
        Log.d(TAG, "setGPSHUDOnClickListeners: Started");

        rlGPSHUDSat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showGPSViewOptionSat();
            }
        });

        llGPSHUDCompass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showGPSViewOptionSensor();
            }
        });

        rlGPSHUDLocationMetadata.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showGPSViewOptionsLocationMetadata();
            }
        });



    }

    private void showGPSViewWidgets(){
        Log.d(TAG, "showGPSViewWidgets: Started");

        rlGPSHUD.setVisibility(View.VISIBLE);


    }


    private void showGPSViewOptionSat(){
        Log.d(TAG, "showGPSViewOptionSat: Started");

        if(!isShownOptionsStatus){  //show view
            rlGPSOptionsSat.setVisibility(View.VISIBLE);
            isShownOptionsStatus = true;
        } else{ //hide view
            rlGPSOptionsSat.setVisibility(View.INVISIBLE);
            isShownOptionsStatus = false;
        }

    }

    private void showGPSViewOptionSensor(){
        Log.d(TAG, "showGPSViewOptionSensor: Started");

        if(!isShownOptionsSensor){  //show view
                rlGPSOptionsSensor.setVisibility(View.VISIBLE);
                isShownOptionsSensor = true;
        }else{ //hide view
            rlGPSOptionsSensor.setVisibility(View.INVISIBLE);
            isShownOptionsSensor = false;
        }

    }

    private void showGPSViewOptionsLocationMetadata(){
        Log.d(TAG, "showGPSViewOptionsLocationMetadata: Started");

        if(cycleHudMetadata == LOCATION_METADATA_SPEED){
            tvGpsHud_Location_Metadata_Header.setText("Bearing");
            cycleHudMetadata = LOCATION_METADATA_BEARING;

        }else{
            tvGpsHud_Location_Metadata_Header.setText("Speed");
            cycleHudMetadata = LOCATION_METADATA_SPEED;
        }



    }

    private void showGPSViewSurveyMeasureActions(){
        Log.d(TAG, "showGPSViewSurveyMeasureActions: Started");

        if(currentMode == MODE_SURVEY){
            gpsMeasureHelper.setViewState(true);
            //rlMeasureFooter.setVisibility(View.VISIBLE);
        }else{
            gpsMeasureHelper.setViewState(false);
            //rlMeasureFooter.setVisibility(View.GONE);
        }

    }



    private void showGPSViewStakeoutPointInfo(){
        Log.d(TAG, "showGPSViewStakeoutPointInfo: Started");

        if(currentMode == MODE_STAKE){
            rlGPSStakePointDirection.setVisibility(View.VISIBLE);
            rlGPSStakePointID.setVisibility(View.VISIBLE);
        }else{
            rlGPSStakePointDirection.setVisibility(View.INVISIBLE);
            rlGPSStakePointID.setVisibility(View.INVISIBLE);
        }
    }


    private void updateHudLocation(boolean type, Location location){
        Log.d(TAG, "updateHudLocation: Started");
        DecimalFormat df2 = StringUtilityHelper.createUSNonBiasDecimalFormatSelect(2);


        if(hasGPSHubInit) {

            if (preferenceLoaderHelper.getGPSLocationUsePrediction() == type) {
                //Latitude & Longitude
                tvGPSHud_Location_Lat.setText(SurveyMathHelper.convertDECtoDMSGeodeticV2(location.getLatitude(), 4));
                tvGpsHud_Location_Lon.setText(SurveyMathHelper.convertDECtoDMSGeodeticV2(location.getLongitude(), 4));

                //Data from Location
                double altitudeMsl = locationService.getAltitudeMsl();
                double locationAccuracy = location.getAccuracy();
                String locationStatus = GPSLocationConverter.getLocationAccuracyStatus(mContext,location);

                switch (preferenceLoaderHelper.getGeneral_over_units()){
                    case 0:  //US Feet
                            altitudeMsl = GPSLocationConverter.convertMetersToValue(altitudeMsl,1);
                            locationAccuracy = GPSLocationConverter.convertMetersToValue(locationAccuracy,1);
                        break;

                    case 1: //Int. Feet
                            altitudeMsl = GPSLocationConverter.convertMetersToValue(altitudeMsl,2);
                            locationAccuracy = GPSLocationConverter.convertMetersToValue(locationAccuracy,2);
                        break;

                    case 2: //Meters
                            altitudeMsl = GPSLocationConverter.convertMetersToValue(altitudeMsl,3);
                            locationAccuracy = GPSLocationConverter.convertMetersToValue(locationAccuracy,3);
                        break;
                }

                tvGpsHud_Location_Alt.setText(df2.format(altitudeMsl));
                tvGpsHud_Status_Value.setText(locationStatus);
                tvGpsHud_Accuracy_Value.setText(df2.format(locationAccuracy));

            }

        }

    }

    private void updateHudGnssStatus(int noSatellites, int noSatellitesLocked){
        Log.d(TAG, "updateHudGnssStatus: Started");

        if(hasGPSHubInit) {

            tvGpsHud_Status_noSatellites.setText(String.valueOf(noSatellites));
            tvGpsHud_Status_noSatellitesLocked.setText(String.valueOf(noSatellitesLocked));

        }

    }

    private void updateHudLocationMetadata(float speed, float bearing){
        Log.d(TAG, "updateHudLocationMetadata: Started");
        double convertedSpeed = 0;

        if(hasGPSHubInit){

            if(isShownOptionsMetadata){

                switch(cycleHudMetadata){
                    case LOCATION_METADATA_SPEED:
                        switch (preferenceLoaderHelper.getGeneral_over_units()){
                            case 0:  //mph
                                convertedSpeed = GPSLocationConverter.convertSpeedMSToValue(speed,1);
                                break;

                            case 1: //mph
                                convertedSpeed = GPSLocationConverter.convertSpeedMSToValue(speed,2);
                                break;

                            case 2: //mph
                                convertedSpeed = GPSLocationConverter.convertSpeedMSToValue(speed,3);
                                break;
                        }

                        tvGpsHud_Location_Metadata.setText(String.valueOf((int) convertedSpeed));

                        break;

                    case LOCATION_METADATA_BEARING:
                        tvGpsHud_Location_Metadata.setText(String.valueOf(bearing));
                        break;
                }

            }
        }
    }

    private void updateHudSensors(int azimuth, float pitch, float roll){
        Log.d(TAG, "updateHudSensors: Started");
        Log.d(TAG, "updateHudSensors: Azimuth: " + azimuth);

        int pitchShown = (int) pitch;  //show pitch without decimal
        int rollShown = (int) roll;

        if(hasGPSHubInit){
            tvGpsHud_Sensor_Orientation.setText(String.valueOf(azimuth));

            if(isShownOptionsSensor){
                tvGpsHud_Sensor_Pitch.setText(String.valueOf(pitchShown));
                tvGpsHud_Sensor_Roll.setText(String.valueOf(rollShown));
            }

        }

    }


    private void updateHudPreferences(){
        Log.d(TAG, "updateHudPreferences: Started");

        if(hasGPSHubInit){
            if(!prefUsePredictedLocation){
                tvGpsHud_Location_Header.setText("Location (Raw)");
            }else{
                tvGpsHud_Location_Header.setText("Location (Filter)");
            }

            if(!prefUseSensorGMF){
                tvGpsHud_Sensor_Header.setText("Compass (Raw)");
            }else{
                tvGpsHud_Sensor_Header.setText("Compass");
            }
        }

    }

    //---------------------------------------------------------------------------------------------- GPS Measure View
    private void initGPSMeasureWidgets(){
        Log.d(TAG, "initGPSMeasureWidgets: Started");

        gpsMeasureHelper = new GPSMeasureHelper(mContext,mJob_DatabaseName,this);
        measuringLocationList = new ArrayList<>();

        hasGPSMeasureInit = true;

    }



    private void showGPSMeasureWidgets(){
        Log.d(TAG, "showGPSMeasureWidgets: Started");
        gpsMeasureHelper.setViewState(true);


    }

    @Override
    public void setViewsForModal(boolean isModal) {
        Log.d(TAG, "setViewsForModal: Started:" + isModal);
        if(isModal){
            Log.d(TAG, "setViewsForModal: True");
            fabMenu.setEnabled(false);
            swAppBarSwitch.setEnabled(false);

            enableDisableViewGroup(rlMapOverlayView,false);
            enableDisableViewGroup(rlGPSHUD,false);


        }else{
            Log.d(TAG, "setViewsForModal: False");
            fabMenu.setEnabled(true);
            swAppBarSwitch.setEnabled(true);

            enableDisableViewGroup(rlMapOverlayView,true);
            enableDisableViewGroup(rlGPSHUD,true);

        }
    }

    @Override
    public void startStopMeasureData(boolean start) {
        Log.d(TAG, "startStopMeasureData: Started");

        if(start){
            measuringLocationList.clear();
            isMeasuringPosition = true;
        }else{

            isMeasuringPosition = false;
        }

    }

    @Override
    public void showResultsDialog(boolean show) {
        Log.d(TAG, "showResultsDialog: Started");
        if(show){
            Log.d(TAG, "showResultsDialog: Results: " + measuringLocationList.size());
            DialogGPSSurveyMeasureResults dialogGPSSurveyMeasureResults = DialogGPSSurveyMeasureResults.newInstance(measuringLocationList,gpsMeasureHelper.getPointNumber(),gpsMeasureHelper.getInstrumentHeight());
            dialogGPSSurveyMeasureResults.show(getFragmentManager(),"results_dialog");
        }
    }

    //---------------------------------------------------------------------------------------------- Map View

    private void initMapViewWidget(){
        Log.d(TAG, "initMapViewWidget: Started");

        rlMapOverlayView = findViewById(R.id.rl_map_overlay_view);
        mMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);

    }

    private void startMapView(){
        Log.d(TAG, "startMapView: Started");

        mMapFragment.getMapAsync(this);


    }

    private void stopMapView(){
        Log.d(TAG, "stopMapView: Started");
    }

    private void mapViewSettings(){
        Log.d(TAG, "mapViewSettings: Started");

        mUiSettings.setZoomControlsEnabled(false);
        mUiSettings.setCompassEnabled(true);
        mUiSettings.setMyLocationButtonEnabled(true);

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d(TAG, "onMapReady: Started");

        mMap = googleMap;
        mUiSettings = mMap.getUiSettings();

        try{
            mapViewSettings();
            mMap.setMyLocationEnabled(false);

            mMap.setOnCameraMoveStartedListener(new GoogleMap.OnCameraMoveStartedListener() {
                @Override
                public void onCameraMoveStarted(int reason) {
                    if(reason == GoogleMap.OnCameraMoveStartedListener.REASON_GESTURE){
                        Log.d(TAG, "onCameraMoveStarted after user's zoom action");

                        isMapViewZoomable = false;
                        if (zoomBlockingTimer != null) {
                            zoomBlockingTimer.cancel();
                        }

                        handlerMapViewOnUIThread = new Handler();

                        TimerTask task = new TimerTask() {
                            @Override
                            public void run() {
                                handlerMapViewOnUIThread.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        zoomBlockingTimer = null;
                                        isMapViewZoomable = true;

                                    }
                                });
                            }
                        };
                        zoomBlockingTimer = new Timer();
                        zoomBlockingTimer.schedule(task, 10 * 1000);
                        Log.d(TAG, "start blocking auto zoom for 10 seconds");
                    }
                }
            });

        }catch (SecurityException ex){
            Log.e(TAG, "onMapReady: " + ex);

        }

        hasMapViewInit = true;
    }


    //---------------------------------------------------------------------------------------------- Map Symbol creator
    private void drawLocationAccuracyCircle(Location location){
        Log.d(TAG, "drawLocationAccuracyCircle: Started");
        if(location.getAccuracy() < 0){
            return;
        }

        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());

        if (this.locationAccuracyCircle == null) {
            this.locationAccuracyCircle = mMap.addCircle(new CircleOptions()
                    .center(latLng)
                    .fillColor(Color.argb(64, 0, 0, 0))
                    .strokeColor(Color.argb(64, 0, 0, 0))
                    .strokeWidth(0.0f)
                    .radius(location.getAccuracy())); //set radius to horizontal accuracy in meter.
        } else {
            this.locationAccuracyCircle.setCenter(latLng);
        }

    }

    private void drawUserPositionMarker(Location location){
        Log.d(TAG, "drawUserPositionMarker: Started");

        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());

        if(this.userPositionMarkerBitmapDescriptor == null){
            userPositionMarkerBitmapDescriptor = BitmapDescriptorFactory.fromResource(R.drawable.user_position_marker);
        }


        if (userPositionMarker == null) {
            userPositionMarker = mMap.addMarker(new MarkerOptions()
                    .position(latLng)
                    .flat(true)
                    .anchor(0.5f, 0.5f)
                    .rotation(mSensorAzimuth)
                    .icon(this.userPositionMarkerBitmapDescriptor));
        } else {
            userPositionMarker.setPosition(latLng);
            //userPositionMarker.setRotation(mSensorAzimuth);
            rotateMarker(userPositionMarker,mSensorAzimuth);
        }

    }

    private void rotateMarker(final Marker marker, final float toRotation) {
        if(!isMarkerRotating){
            final Handler handler = new Handler();
            final long start = SystemClock.uptimeMillis();
            final float startRotation = marker.getRotation();
            final long duration = 100;
            float deltaRotation = Math.abs(toRotation - startRotation) % 360;
            final float rotation = (deltaRotation > 180 ? 360 - deltaRotation : deltaRotation) * ((toRotation - startRotation >= 0 && toRotation - startRotation <= 180)
                    || (toRotation - startRotation <=-180 && toRotation- startRotation>= -360) ? 1 : -1);

            final LinearInterpolator interpolator = new LinearInterpolator();
            handler.post(new Runnable() {
                @Override
                public void run() {
                    isMarkerRotating = true;
                    long elapsed = SystemClock.uptimeMillis() - start;
                    float t = interpolator.getInterpolation((float) elapsed / duration);
                    marker.setRotation((startRotation + t* rotation)%360);
                    if (t < 1.0) {
                        // Post again 16ms later.
                        handler.postDelayed(this, 16);
                    }else {
                        isMarkerRotating = false;
                    }
                }
            });
        }
    }




    //---------------------------------------------------------------------------------------------- Map Zooms
    private void updateMapLocation(boolean type, Location location) {
        Log.d(TAG, "updateMapLocation: Started");

        if (hasMapViewInit) {
            if (prefUsePredictedLocation == type) {
                drawUserPositionMarker(location);
                zoomMapTo(location);
            }
        }
    }

    private CameraPosition setCameraPosition(LatLng latLng){
        Log.d(TAG, "setCameraPosition: " + mCurrentCameraPosition);
        CameraPosition cameraPosition;

        switch(mCurrentCameraPosition){
            case CAMERA_POSITION_NORTH:
                cameraPosition = new CameraPosition.Builder()
                    .zoom(17.5f)                                        // Sets the zoom
                    .target(latLng)
                    .bearing((float) 0)                                 // Sets the orientation of the camera to north
                    .tilt(45)                                           // Sets the tilt of the camera to 30 degrees
                    .build();                                           // Creates a CameraPosition from the builder
                break;

            case CAMERA_POSITION_SENSOR:
                cameraPosition = new CameraPosition.Builder()
                        .zoom(17.5f)                                        // Sets the zoom
                        .target(latLng)
                        .bearing(camera_bearing_sensor)                     // Sets the orientation of the camera to north
                        .tilt(45)                                           // Sets the tilt of the camera to 30 degrees
                        .build();                                           // Creates a CameraPosition from the builder
                break;

            case CAMERA_POSITION_SPEED:
                cameraPosition = new CameraPosition.Builder()
                        .zoom(17.5f)                                        // Sets the zoom
                        .target(latLng)
                        .bearing(camera_bearing_speed)                      // Sets the orientation of the camera to north
                        .tilt(45)                                           // Sets the tilt of the camera to 30 degrees
                        .build();                                           // Creates a CameraPosition from the builder
                break;

            default:
                cameraPosition = new CameraPosition.Builder()
                        .zoom(17.5f)                                        // Sets the zoom
                        .bearing((float) 0)                                 // Sets the orientation of the camera to north
                        .tilt(45)                                           // Sets the tilt of the camera to 30 degrees
                        .build();
                break;

        }

        return cameraPosition;
    }


    private void zoomMapTo(Location location) {
        Log.d(TAG, "zoomMapTo: Started");
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());


        if (!didMapViewInitialZoom) {
            try {
                isMapViewZoomable = false;
                mMap.animateCamera(CameraUpdateFactory.newCameraPosition(setCameraPosition(latLng)),
                        new GoogleMap.CancelableCallback() {
                            @Override
                            public void onFinish() {
                                isMapViewZoomable = true;
                            }

                            @Override
                            public void onCancel() {
                                isMapViewZoomable = true;
                            }
                        });

                didMapViewInitialZoom = true;
                return;
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        if (isMapViewZoomable) {
            try {
                isMapViewZoomable = false;

                mMap.animateCamera(CameraUpdateFactory.newCameraPosition(setCameraPosition(latLng)),
                        new GoogleMap.CancelableCallback() {
                            @Override
                            public void onFinish() {
                                isMapViewZoomable = true;
                            }

                            @Override
                            public void onCancel() {
                                isMapViewZoomable = true;
                            }
                        });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    //---------------------------------------------------------------------------------------------- POIHelperListener Class Listener
    @Override
    public void isPOITargetSet(boolean isTargetSet) {
        if(isTargetSet){

            PointGeodetic targetPoint = getPOIHelper.getTargetLocation();
            tvGpsHud_Stakeout_Point_Id.setText(String.valueOf(targetPoint.getPoint_no()));

            if(currentMode == MODE_STAKE){

                RumbleHelper.init(mContext);
                RumbleHelper.once(50);

                showGPSViewStakeoutPointInfo();
                mDrawerLayout.closeDrawer(GravityCompat.START);

            }

        }
    }

    @Override
    public void openPointViewDialogBox(boolean requestOpen) {
        if(requestOpen){

            PointGeodetic targetPoint = getPOIHelper.getTargetLocation();

            long point_id = targetPoint.getId();
            int pointNo = targetPoint.getPoint_no();

            FragmentManager fm = getSupportFragmentManager();
            android.support.v4.app.DialogFragment pointDialog = DialogJobPointView.newInstance(mProject_id, mJob_id, point_id, pointNo, mJob_DatabaseName,false);
            pointDialog.show(fm,"dialog_point_view");

        }
    }

    //---------------------------------------------------------------------------------------------- INNER CLASS - Loading Screen
    private class LoadGPSDataTask extends AsyncTask<Void, Integer, Void> {

        private ProgressDialog pd;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            pd = new ProgressDialog(mContext);
            pd.setTitle("Looking for GPS Data");
            pd.setMessage("Please wait...");
            pd.setCancelable(false);
            pd.show();

        }


        @Override
        protected Void doInBackground(Void... params) {
            Long t = Calendar.getInstance().getTimeInMillis();
            int i = 0;
            while(!hasFilteredLocation && Calendar.getInstance().getTimeInMillis()-t < 30000){
                try {
                    Thread.sleep(1000);

                    publishProgress(i);
                    i++;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            return null;

        }


        @Override
        protected void onProgressUpdate(Integer... values) {
            Integer secondsGone = values[0];
            if(!hasLocation){
                pd.setMessage("Found " + gpsRawCount + " locations in " + secondsGone + " seconds.");
            }


            if(hasLocation && !hasFilteredLocation){
                pd.setTitle("Looking for Best GPS Data");
                pd.setMessage("Adjusting " + gpsFilteredCount + " locations in " + secondsGone + " seconds.");
                if(!locationService.getGpsLogging()){
                    locationService.startLogging();
                }

            }

        }

        @Override
        protected void onPostExecute(Void result) {

            if(hasFilteredLocation){
                rlGPSErrorMessage.setVisibility(View.GONE);
                locationLoadedSuccess = true;

                switchBoardViewWidgets();
                startMapView();

                if (pd.isShowing()) {
                    pd.dismiss();
                }


            }else{
                locationLoadedSuccess = false;
                rlGPSErrorMessage.setVisibility(View.VISIBLE);
                locationService.stopGPS();
                sensorService.stopSensor();

                if (pd.isShowing()) {
                    pd.dismiss();
                }

            }

            showSwitchWidget();

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

    public static void enableDisableViewGroup(ViewGroup viewGroup, boolean enabled) {
        int childCount = viewGroup.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View view = viewGroup.getChildAt(i);
            view.setEnabled(enabled);
            if (view instanceof ViewGroup) {
                enableDisableViewGroup((ViewGroup) view, enabled);
            }
        }
    }


}
