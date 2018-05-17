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
import android.hardware.SensorManager;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.survlogic.survlogic.ARvS.utils.GnssService;
import com.survlogic.survlogic.ARvS.utils.SensorService;
import com.survlogic.survlogic.R;
import com.survlogic.survlogic.utils.GPSLocationConverter;
import com.survlogic.survlogic.utils.PreferenceLoaderHelper;
import com.survlogic.survlogic.utils.StringUtilityHelper;
import com.survlogic.survlogic.utils.SurveyMathHelper;

import java.text.DecimalFormat;
import java.util.Calendar;

public class JobGPSSurveyArvTActivity extends AppCompatActivity {
    private static final String TAG = "JobGPSSurveyArvTActivit";

    private Context mContext;
    private PreferenceLoaderHelper preferenceLoaderHelper;

    //Location Service
    public GnssService locationService;
    private BroadcastReceiver locationUpdateReceiver;
    private BroadcastReceiver predictedLocationReceiver;
    private BroadcastReceiver gnssStatusReceiver;
    private BroadcastReceiver batteryCriticalReceiver;

    //Sensor Service
    public SensorService sensorService;
    private BroadcastReceiver sensorDataReceiver;
    private BroadcastReceiver sensorAccuracyReceiver;

    //Location Variables
    private boolean hasLocation = false, hasFilteredLocation = false;
    private int gpsRawCount=0, gpsFilteredCount =0;
    private boolean locationLoadedSuccess = false;

    //Sensor Variables
    private boolean useFusionSensor = true;
    private float mSensorAlpha = 0.1f;

    //App Bar Widget
    private int currentMode;
    private static final int MODE_SURVEY = 0, MODE_STAKE = 1;

    //Widgets
    private RelativeLayout rlGPSErrorMessage;

    //---------------------------------------------------------------------------------------------- View Controller
    private int view_stage_map = VIEW_MAP_START, view_stage_map_previous;
    private static final int VIEW_MAP_FULL = 1, VIEW_MAP_HALF = 2, VIEW_MAP_HIDDEN = 0, VIEW_MAP_START = -1;

    //---------------------------------------------------------------------------------------------- GPS Hub View
    //System Variables
    private int criteriaGpsFilteredCount = 3, criteriaGpsRawCount = 5;
    private boolean hasGPSHubInit = false;
    private boolean isShownOptionsStatus = false, isShownOptionsSensor = false;
    private static final int LOCATION_RAW = 0, LOCATION_PREDICT = 1;
    //Widgets
    private RelativeLayout rlGPSHUD, rlGPSHUDSat, rlGPSOptionsSat, rlGPSOptionsSensor;
    private LinearLayout llGPSHUDCompass;
    private TextView tvGpsHud_Status_noSatellites, tvGpsHud_Status_noSatellitesLocked;
    private TextView tvGPSHud_Location_Lat, tvGpsHud_Location_Lon, tvGpsHud_Location_Alt;
    private TextView tvGpsHud_Status_Value, tvGpsHud_Accuracy_Value;
    private TextView tvGpsHud_Sensor_Orientation, tvGpsHud_Sensor_Pitch;

    //---------------------------------------------------------------------------------------------- Map Overlay View
    //System Variables
    private boolean hasMapViewInit = false;
    //Widgets
    private RelativeLayout rlMapOverlayView;



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        Log.d(TAG, "Activity Started-------------------------------------------->");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gps_ar_v2);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        mContext = JobGPSSurveyArvTActivity.this;
        preferenceLoaderHelper = new PreferenceLoaderHelper(mContext);

        bindLocationService();
        bindSensorService();

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
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
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
                }

            }
        };

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
                    updateHudLocation(LOCATION_PREDICT,predicatedLocation);
                }

            }
        };

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
                locationUpdateReceiver,
                new IntentFilter("LocationUpdated")
        );

        LocalBroadcastManager.getInstance(this).registerReceiver(
                predictedLocationReceiver,
                new IntentFilter("PredictLocation")
        );

        LocalBroadcastManager.getInstance(this).registerReceiver(
                gnssStatusReceiver,
                new IntentFilter("GnssStatus")
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

    private void registerSensorReceiver(){
        Log.d(TAG, "registerSensorReceiver: Started");

        sensorDataReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                int sensorOrientation = 0;
                float sensorPitch = 0;

                sensorOrientation = intent.getIntExtra("azimuth",sensorOrientation);
                sensorPitch = intent.getFloatExtra("pitch",sensorPitch);

                Log.d(TAG, "onReceive: Sensor Azimuth:" + sensorOrientation);
                Log.d(TAG, "onReceive: Sensor Pitch:" + sensorPitch);
                Log.d(TAG, "onReceive: Filtered Location: " + hasFilteredLocation);

                if(hasFilteredLocation){
                    updateHudSensors(sensorOrientation,sensorPitch);
                }

            }
        };

        LocalBroadcastManager.getInstance(this).registerReceiver(
                sensorDataReceiver,
                new IntentFilter("SensorData")
        );

        sensorAccuracyReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                String mSensor = intent.getStringExtra("sensor");
                int mSensorAccuracy = intent.getIntExtra("accuracy",-1);

                if(hasFilteredLocation){
                    evaluateSensorAccuracy(mSensor,mSensorAccuracy);
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

    private void evaluateSensorAccuracy(String sensor, int sensorAccuracy){
        Log.d(TAG, "evaluateSensorAccuracy: Started");

        switch (sensorAccuracy){
            case SensorManager.SENSOR_STATUS_UNRELIABLE:
                Log.i(TAG, "evaluateSensorAccuracy: Unreliable Sensor Reading on " + sensor);
                break;

            case SensorManager.SENSOR_STATUS_ACCURACY_LOW:
                Log.i(TAG, "evaluateSensorAccuracy: Low Sensor Reading on " + sensor);
                break;

            case SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM:
                Log.i(TAG, "evaluateSensorAccuracy: Medium Sensor Reading on " + sensor);
                break;

            case SensorManager.SENSOR_STATUS_ACCURACY_HIGH:
                Log.i(TAG, "evaluateSensorAccuracy: High Sensor Reading on " + sensor);
                break;

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

    //---------------------------------------------------------------------------------------------- Activity View Methods
    private void initActivityWidgets(){
        Log.d(TAG, "initActivityWidgets: Started");

        TextView tvAppBarTitle = findViewById(R.id.DrawerLayout);
        tvAppBarTitle.setText(getResources().getText(R.string.gps_title_header));

        ImageButton ibAppBarBackButton = findViewById(R.id.button_back);
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

        FloatingActionButton fabMenu = findViewById(R.id.fab_menu);
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
        initGPSViewWidgets();
        initMapViewWidget();

        //Actions
        initSwitchActions();

    }

    private void initSwitchActions(){
        Log.d(TAG, "onClickListeners: Started");

        LinearLayout llAppBarSwitch = findViewById(R.id.ll_switch);
        Switch swAppBarSwitch = findViewById(R.id.switch_value);
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
                }else{
                    currentMode = MODE_STAKE;
                    incrementMapView();
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

        rlGPSOptionsSat = findViewById(R.id.rl_option_sat);
        rlGPSOptionsSensor = findViewById(R.id.rl_option_sensor);

        tvGpsHud_Status_noSatellites = findViewById(R.id.gps_status_noSatellites);
        tvGpsHud_Status_noSatellitesLocked = findViewById(R.id.gps_status_noSatellitesLocked);

        tvGPSHud_Location_Lat = findViewById(R.id.gps_location_lat);
        tvGpsHud_Location_Lon = findViewById(R.id.gps_location_lon);
        tvGpsHud_Location_Alt = findViewById(R.id.gps_location_Alt);

        tvGpsHud_Status_Value = findViewById(R.id.gps_status_value);
        tvGpsHud_Accuracy_Value = findViewById(R.id.gps_accuracy_Value);

        tvGpsHud_Sensor_Orientation = findViewById(R.id.compass_orientation_value);
        tvGpsHud_Sensor_Pitch = findViewById(R.id.compass_pitch_value);

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

    private void updateHudLocation(int type, Location location){
        Log.d(TAG, "updateHudLocation: Started");
        DecimalFormat df2 = StringUtilityHelper.createUSNonBiasDecimalFormatSelect(2);


        if(hasGPSHubInit) {
            if (preferenceLoaderHelper.getGPSLocationUsePrediction() == type) {
                //Latitude & Longtiude
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

    private void updateHudSensors(int azimuth, float pitch){
        Log.d(TAG, "updateHudSensors: Started");
        Log.d(TAG, "updateHudSensors: Azimuth: " + azimuth);

        int pitchShown = (int) pitch;  //show pitch without decimal

        if(hasGPSHubInit){
            tvGpsHud_Sensor_Orientation.setText(String.valueOf(azimuth));

            if(isShownOptionsSensor){
                tvGpsHud_Sensor_Pitch.setText(String.valueOf(pitchShown));
            }

        }

    }






    //---------------------------------------------------------------------------------------------- Map View

    private void initMapViewWidget(){
        Log.d(TAG, "initMapViewWidget: Started");

        rlMapOverlayView = findViewById(R.id.rl_map_overlay_view);


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
                locationLoadedSuccess = true;
                rlGPSErrorMessage.setVisibility(View.GONE);
                switchBoardViewWidgets();

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


}
