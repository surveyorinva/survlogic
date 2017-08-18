package com.survlogic.survlogic.fragment;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.location.GnssMeasurementsEvent;
import android.location.GnssStatus;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.TextViewCompat;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.LocationSource;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.survlogic.survlogic.R;
import com.survlogic.survlogic.activity.GpsSurveyActivity;
import com.survlogic.survlogic.interf.GpsSurveyListener;
import com.survlogic.survlogic.model.Dop;
import com.survlogic.survlogic.model.PointGeodetic;
import com.survlogic.survlogic.utils.GpsHelper;
import com.survlogic.survlogic.utils.LocationConverter;
import com.survlogic.survlogic.utils.MathHelper;


import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static com.survlogic.survlogic.utils.LocationConverter.convertMetersToValue;

/**
 * Created by chrisfillmore on 5/2/2017.
 */

public class GpsSurveyMapFragment extends Fragment implements GpsSurveyListener, OnMapReadyCallback {

    //    Debugging Static Constants
    private static final String TAG = "GpsSurveyMapFragment";

    //    Fragment Constants
    View v;
    private Resources mRes;
    private Context mContext;
    private SupportMapFragment supportMapFragment;

    //    UI Views
    private TextView mLatitudeView, mLongitudeView, mEllipsoidView, mOrthoHeightView,
            mFixTimeView, mTTFFView,
            mAccuracyView, mAccuracyStatus,
            mNumSats, mNumSatsLocked,
            mPdopView, mHdopView, mVdopView, mHeightView,
            mEpochView, mEpochCount,
            mGpsLogValue;

    private ImageView mAccuracyStatusImage;

    private Button mbtnStartGPSLog, mbtnSaveGPSLog, mbtnSaveHeight;

    private ProgressBar progressBarRecording;

    //Array data
    private List<Double> mArrayLat, mArrayLong, mArrayEllipsoid, mArrayOrtho, mArrayAccuracy;


    //    Satellite Metadata
    private int mSvCount, mPrns[], mConstellationType[], mUsedInFixCount;
    private float mSnrCn0s[], mSvElevations[], mSvAzimuths[];
    private double mOrtho;
    private String mSnrCn0Title;
    private boolean mHasEphemeris[], mHasAlmanac[], mUsedInFix[];

    //    Satellite Constants
    private long mFixTime;
    private boolean mNavigating, mGotFix;

    //    Mapping Variables
    private GoogleMap mMap;
    private UiSettings mUiSettings;

    private LatLng mLatLng;
    private MarkerOptions currentPositionMarker = null;
    private Marker currentLocationMarker;

    //    Objects
    private PointGeodetic pointGeodetic;

    //    Formatting
    DecimalFormat mFixedTwoFormat = new DecimalFormat("#.##");

    //    Settings
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    //    System Variables
    // Accuracy
    private static final float mPositionAutonomousMeters = 20;
    private static final float mPositionFloatMeters = 10;
    private static final float mPositionFixedMeters = 5;
    //Map
    private int displayUnits = 3;
    private int mapType = 1;
    private double mHeightRod;
    //Logging
    private boolean bolGPSRecording = false;
    private int intEpochCount = 0;
    private boolean mAutoEpochCount = true;
    private int mEpochMaxCount = 5;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        Log.e(TAG, "Started onCreateView");

        mRes = getResources();

        v = inflater.inflate(R.layout.fragment_gps_survey_map, container, false);

        initView();

        Log.e(TAG, "Starting Listener");

        GpsSurveyActivity.getInstance().addListener(this);

        return v;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mContext = getActivity();

        FragmentManager fm = getActivity().getSupportFragmentManager();
        supportMapFragment = (SupportMapFragment) fm.findFragmentById(R.id.map_container);

        if (supportMapFragment == null) {
            supportMapFragment = SupportMapFragment.newInstance();
            fm.beginTransaction().replace(R.id.map_container, supportMapFragment).commit();
        }

        supportMapFragment.getMapAsync(this);

    }

    @Override
    public void onResume() {
        super.onResume();
        GpsSurveyActivity gsa = GpsSurveyActivity.getInstance();
        setStarted(gsa.gpsRunning);

        checkPreferenceUnits(sharedPreferences);
        checkPreferencesMapType(sharedPreferences);
        checkPreferencesSurveySettings(sharedPreferences);
    }


    private void initView() {
        Log.e(TAG, "Initialized initView");
        mLatitudeView = (TextView) v.findViewById(R.id.gps_position_lat_value);
        mLongitudeView = (TextView) v.findViewById(R.id.gps_position_lon_value);
        mEllipsoidView = (TextView) v.findViewById(R.id.gps_position_ellipsoid_value);
        mOrthoHeightView = (TextView) v.findViewById(R.id.gps_position_msl_value);

        mAccuracyStatus = (TextView) v.findViewById(R.id.gnss_status_Value);
        mAccuracyView = (TextView) v.findViewById(R.id.gps_accuracy_Value);

        mNumSats = (TextView) v.findViewById(R.id.gps_status_noSatellites);
        mNumSatsLocked = (TextView) v.findViewById(R.id.gps_status_noSatellitesLocked);

        mPdopView = (TextView) v.findViewById(R.id.PDOP_value);
        mHdopView = (TextView) v.findViewById(R.id.HDOP_value);
        mVdopView = (TextView) v.findViewById(R.id.VDOP_value);
        mTTFFView = (TextView) v.findViewById(R.id.ttff_value);
        mHeightView = (TextView) v.findViewById(R.id.height_gps_value);

        mEpochView = (TextView) v.findViewById(R.id.gps_epoch_count_lbl);
        mEpochCount = (TextView) v.findViewById(R.id.gps_epoch_count_value);

        mGpsLogValue = (TextView) v.findViewById(R.id.gps_log_value);

        mAccuracyStatusImage = (ImageView) v.findViewById(R.id.gnss_status_img);

        progressBarRecording = (ProgressBar) v.findViewById(R.id.progressBarRecording);

        mbtnStartGPSLog = (Button) v.findViewById(R.id.btnStartGPSLog);

        mbtnSaveGPSLog = (Button) v.findViewById(R.id.btnSaveGPSLog);

        mbtnSaveHeight = (Button) v.findViewById(R.id.height_gps_button);

        initViewSettings();

        setOnClickListeners();

        Log.e(TAG, "Setup Controls Completed");

    }


    private void setOnClickListeners(){

        mbtnStartGPSLog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(!bolGPSRecording){
                    onStartGpsSurvey();

                }else{
                    onStopGpsSurvey();
                }

            }
        });

        mbtnSaveGPSLog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSaveGpsSurvey();

            }
        });

        mbtnSaveHeight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSaveRodHeight();
            }
        });

    }

    private void initViewSettings(){

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());

        double tempMinTime = Double.valueOf(
                sharedPreferences.getString(getString(R.string.pref_key_gps_min_time), getString(R.string.pref_gps_min_time_default_sec)));

        mEpochView.setText(getString(R.string.header_epoch_value,String.valueOf(tempMinTime)));

        displayUnits = Integer.valueOf(
                sharedPreferences.getString(getString(R.string.pref_key_gps_unit_measurement), getString(R.string.pref_gps_unit_measurement_default)));

        mapType = Integer.valueOf(
                sharedPreferences.getString(getString(R.string.pref_key_map_type), getString(R.string.pref_gps_map_type_default)));

        progressBarRecording.setMax(mEpochMaxCount);
        progressBarRecording.setProgress(0);

        mAutoEpochCount = sharedPreferences.getBoolean(getString(R.string.pref_key_auto_stop_epoch), true);

        mEpochMaxCount = Integer.valueOf(
                sharedPreferences.getString(getString(R.string.pref_key_epoch_max), getString(R.string.pref_gps_epoch_default)));


    }


    private void onStartGpsSurvey(){

        mbtnStartGPSLog.setText(R.string.general_stop);
        mbtnStartGPSLog.setBackgroundResource(R.drawable.button_framed_red);

        progressBarRecording.setVisibility(View.VISIBLE);

        mArrayLat = new ArrayList<>();
        mArrayLong = new ArrayList<>();
        mArrayEllipsoid = new ArrayList<>();
        mArrayOrtho = new ArrayList<>();
        mArrayAccuracy = new ArrayList<>();

        bolGPSRecording = true;
    }

    private void onStopGpsSurvey(){
        mbtnStartGPSLog.setText(R.string.general_start);
        mbtnStartGPSLog.setBackgroundResource(R.drawable.button_framed_green);

        progressBarRecording.setVisibility(View.INVISIBLE);

        mbtnSaveGPSLog.setVisibility(View.VISIBLE);

        bolGPSRecording = false;
    }

    private void onSaveGpsSurvey(){

        onPause();

        double averageLat = MathHelper.createAverageValueFromArray(mArrayLat);
        double averageLong = MathHelper.createAverageValueFromArray(mArrayLong);
        double averageEllipsoid = MathHelper.createAverageValueFromArray(mArrayEllipsoid);
        double averageOrtho = MathHelper.createAverageValueFromArray(mArrayOrtho);
        double averageAccuracy = MathHelper.createAverageValueFromArray(mArrayAccuracy);

        pointGeodetic = new PointGeodetic(averageLat,averageLong,averageEllipsoid, averageOrtho, averageAccuracy);

        intEpochCount = 0;

        returnResults(pointGeodetic);
    }

    private void onSaveRodHeight(){

        AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
        dialog.setTitle(getString(R.string.dialog_gps_rod_height_hand_held_title));
        dialog.setMessage(getString(R.string.dialog_gps_rod_height_hand_held_message));


        final LinearLayout layout = new LinearLayout(getActivity());
        layout.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

        params.setMargins(150,0,150,0);

        final EditText etRodHeight = new EditText(getActivity());
        etRodHeight.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        etRodHeight.setText(String.valueOf(mHeightRod));
        etRodHeight.setSelection(etRodHeight.getText().length());

        layout.addView(etRodHeight, params);

        dialog.setView(layout);


        dialog.setPositiveButton("Add", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        double rodHeightValue;

                        String rodHeightSummary = etRodHeight.getText().toString();
                        if(!rodHeightSummary.isEmpty()){
                            try{
                                rodHeightValue = Double.parseDouble(rodHeightSummary);
                                setPreferenceRodHeight(rodHeightValue);

                            }catch (Exception e){
                                e.printStackTrace();
                            }
                        }

                    }
                });
        dialog.setNegativeButton("Cancel", null);
        dialog.create();
        dialog.show();

    }

    private boolean incrementEpoch(){
        boolean checkValue = false;

        intEpochCount++;

        progressBarRecording.setProgress(intEpochCount);

        mEpochCount.setText(""+intEpochCount);

        if (intEpochCount >= mEpochMaxCount){
            checkValue = true;

        }
        return checkValue;
    }

    private void logRawDataGPS(double myLat, double myLon, double myEllipsoid, double myOrtho, double myAccuracy){
        Log.d(TAG, "logRawDataGPS: Ellipsoid IN: " + myEllipsoid);
        Log.d(TAG, "logRawDataGPS: Ortho IN: " + myOrtho);

        double myCorrectedEllipsoid, myCorrectedOrtho;


        switch (displayUnits) {
            case 1:
                myCorrectedEllipsoid = convertMetersToValue(myEllipsoid, 1)- mHeightRod;

                break;

            case 2:
                myCorrectedEllipsoid = convertMetersToValue(myEllipsoid, 2)- mHeightRod;

                break;

            case 3:
                myCorrectedEllipsoid =(myEllipsoid - mHeightRod);

                break;

            default:
                myCorrectedEllipsoid =(myEllipsoid - mHeightRod);

                break;
        }

        myCorrectedOrtho = myOrtho - mHeightRod;
        Log.d(TAG, "logRawDataGPS: Rod Height: " + mHeightRod);
        Log.d(TAG, "logRawDataGPS: Ellipsoid OUT: " + myCorrectedEllipsoid);
        Log.d(TAG, "logRawDataGPS: Ortho OUT: " + myCorrectedOrtho);


        mArrayLat.add(myLat);
        mArrayLong.add(myLon);
        mArrayEllipsoid.add(myCorrectedEllipsoid);
        mArrayOrtho.add(myCorrectedOrtho);
        mArrayAccuracy.add(myAccuracy);
        mGpsLogValue.append("\n " +String.format(this.getString(R.string.gps_log_raw_value), String.valueOf(myLat),String.valueOf(myLon),String.valueOf(myEllipsoid)));

    }


    private void returnResults(PointGeodetic mPointGeodetic){

        Intent returnIntent = new Intent();
        returnIntent.putExtra(getString(R.string.KEY_POSITION_LATITUDE),mPointGeodetic.getLatitude());
        returnIntent.putExtra(getString(R.string.KEY_POSITION_LONGITUDE),mPointGeodetic.getLongitude());
        returnIntent.putExtra(getString(R.string.KEY_POSITION_ELLIPSOID),mPointGeodetic.getEllipsoid());
        returnIntent.putExtra(getString(R.string.KEY_POSITION_ORTHO),mPointGeodetic.getOrtho());
        getActivity().setResult(Activity.RESULT_OK,returnIntent);
        getActivity().finish();

    }

    private void showToast(String data){

        Toast.makeText(getActivity(), data, Toast.LENGTH_SHORT).show();

    }

    private void setAccuracyView(Location mlocation, boolean bolWaiting){
        String mResult;

        if (bolWaiting){
            mResult = getString(R.string.gnss_status_no_fix);

        }else{
            float mAccuarcy = mlocation.getAccuracy();

            if (mAccuarcy > mPositionAutonomousMeters){
                mResult = getString(R.string.gnss_status_no_fix);
                TextViewCompat.setTextAppearance(mAccuracyStatus,R.style.LargeTextForScreen);

                mAccuracyStatusImage.setBackgroundResource(R.drawable.vc_circle_blue);

            }else if(mAccuarcy < mPositionAutonomousMeters && mAccuarcy > mPositionFloatMeters){
                mResult = getString(R.string.gnss_status_autonomous);
                TextViewCompat.setTextAppearance(mAccuracyStatus,R.style.MediumTextForScreen);
                mAccuracyStatusImage.setBackgroundResource(R.drawable.vc_circle_red);

            }else if(mAccuarcy < mPositionFloatMeters && mAccuarcy > mPositionFixedMeters){
                mResult = getString(R.string.gnss_status_float);
                TextViewCompat.setTextAppearance(mAccuracyStatus,R.style.LargeTextForScreen);
                mAccuracyStatusImage.setBackgroundResource(R.drawable.vc_circle_yellow);

            }else {
                mResult = getString(R.string.gnss_status_fix);
                TextViewCompat.setTextAppearance(mAccuracyStatus,R.style.LargeTextForScreen);
                mAccuracyStatusImage.setBackgroundResource(R.drawable.vc_circle_green);
            }

        }

        mAccuracyStatus.setText(mResult);

    }

    private void checkPreferencesSurveySettings(SharedPreferences settings){
        Log.d(TAG, "checkPreferencesSurveySettings: Started...");
        mAutoEpochCount = sharedPreferences.getBoolean(getString(R.string.pref_key_auto_stop_epoch), true);

        mEpochMaxCount = Integer.valueOf(
                sharedPreferences.getString(getString(R.string.pref_key_epoch_max), getString(R.string.pref_gps_epoch_default)));

        Log.d(TAG, "checkPreferencesSurveySettings: Checking Rod Height from Preferences");
        mHeightRod = Double.valueOf(sharedPreferences.getString(getString(R.string.pref_key_gps_rod_height), getString(R.string.pref_gps_height_default)));

        Log.d(TAG, "checkPreferencesSurveySettings: Found Rod Height, Setting Value: " + mHeightRod);
        mHeightView.setText(getString(R.string.gps_height_value, mHeightRod));
    }

    private void checkPreferenceUnits(SharedPreferences settings){
        displayUnits = Integer.valueOf(
                sharedPreferences.getString(getString(R.string.pref_key_gps_unit_measurement), getString(R.string.pref_gps_unit_measurement_default)));

    }

    private void checkPreferencesMapType(SharedPreferences settings){

        int mapTypeNew = Integer.valueOf(
                sharedPreferences.getString(getString(R.string.pref_key_map_type), getString(R.string.pref_gps_map_type_default)));

        if (mapType != mapTypeNew) {
            switch (mapTypeNew){

                case 1:  //Normal
                    mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                    break;

                case 4:  //Hybrid
                    mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                    break;

                case 2: //Satellite
                    mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                    break;

                case 3:  //Terrain
                    mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                    break;

            }
        }
    }

    private void setPreferenceRodHeight(double rodHeight){
        editor = sharedPreferences.edit();

        editor.putString(getString(R.string.pref_key_gps_rod_height), String.valueOf(rodHeight));

        editor.commit();

        checkPreferencesSurveySettings(sharedPreferences);

    }

    @Override
    public void onLocationChanged(Location location) {
        if (!mGotFix) {
            mTTFFView.setText(GpsSurveyActivity.getInstance().mTtff);
            mGotFix = true;
        }

//        Decimal Latitude
        //mLatitudeView.setText(getString(R.string.gps_latitude_value, location.getLatitude()));

//        DMS Latitude
        mLatitudeView.setText(getString(R.string.gps_latitude_value_string, LocationConverter.getLatitudeAsDMS(location, 3)));

//        Decimal Longitude
        //mLongitudeView.setText(getString(R.string.gps_longitude_value, location.getLongitude()));

//        DMS Longitude
        mLongitudeView.setText(getString(R.string.gps_longitude_value_string, LocationConverter.getLongitudeAsDMS(location, 3)));


//        Altitude = Ellipsoid Height
        if (location.hasAltitude()) {

            switch (displayUnits) {
                case 1:
                    mEllipsoidView.setText(getString(R.string.gps_ellipsoid_value_feet, convertMetersToValue(location.getAltitude(),1)));
                    break;

                case 2:
                    mEllipsoidView.setText(getString(R.string.gps_ellipsoid_value_feet, convertMetersToValue(location.getAltitude(),2)));
                    break;

                case 3:
                    mEllipsoidView.setText(getString(R.string.gps_ellipsoid_value_metric, location.getAltitude()));
                    break;

                default:
                    mEllipsoidView.setText(getString(R.string.gps_ellipsoid_value_metric, location.getAltitude()));
                    break;
            }
        } else {
            mEllipsoidView.setText("");
        }


//        Accuracy Model
        if (location.hasAccuracy()) {

            switch (displayUnits){

                case 1:
                    mAccuracyView.setText(getString(R.string.general_unit_feet, convertMetersToValue(location.getAccuracy(),displayUnits)));
                    break;

                case 2:
                    mAccuracyView.setText(getString(R.string.general_unit_feet, convertMetersToValue(location.getAccuracy(),displayUnits)));
                    break;

                case 3:
                    mAccuracyView.setText(getString(R.string.general_unit_meters, convertMetersToValue(location.getAccuracy(),displayUnits)));
                    break;

                default:
                    mAccuracyView.setText(getString(R.string.general_unit_meters, convertMetersToValue(location.getAccuracy(),displayUnits)));
                    break;

            }

            setAccuracyView(location,false);

        } else {
            mAccuracyView.setText("");
            setAccuracyView(location,true);
        }
        updateFixTime();

//        Create Map Instances
        updateCurrentLocationMarker(location);


//        Check for logging

        if (bolGPSRecording){

            boolean bolEpoch = incrementEpoch();

            if (mAutoEpochCount){
                if (!bolEpoch){
                    // Count has not reached max Epoch level on AutoSave
                    logRawDataGPS(location.getLatitude(),location.getLongitude(),location.getAltitude(), mOrtho, location.getAccuracy());
                }else{
                    // Count has reached the max Epoch level.  Shut off
                    onStopGpsSurvey();
                }
            }else{
                logRawDataGPS(location.getLatitude(),location.getLongitude(),location.getAltitude(),mOrtho, location.getAccuracy());
            }
        }

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


    @SuppressLint("NewApi")
    public void gpsStart() {
        mGotFix = false;
    }


    public void gpsStop() {
        //TODO Nothing Here
    }

    @Override
    public void onGpsStatusChanged(int event, GpsStatus status) {
        switch (event) {
            case GpsStatus.GPS_EVENT_STARTED:
                setStarted(true);
                break;

            case GpsStatus.GPS_EVENT_STOPPED:
                setStarted(false);
                break;

            case GpsStatus.GPS_EVENT_FIRST_FIX:
                break;

            case GpsStatus.GPS_EVENT_SATELLITE_STATUS:
                updateLegacyStatus(status);
                break;
        }
    }


    @Override
    public void onGnssFirstFix(int ttffMillis) {
//        TODO Nothing Here
    }


    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onSatelliteStatusChanged(GnssStatus status) {
        updateGnssStatus(status);
    }

    @Override
    public void onGnssStarted() {
        setStarted(true);
    }

    @Override
    public void onGnssStopped() {
        setStarted(false);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onGnssMeasurementsReceived(GnssMeasurementsEvent event) {
//        TODO Nothing Here
    }

    @Override
    public void onOrientationChanged(double orientation, double tilt) {
//        TODO Nothing Here
    }

    @Override
    public void onNmeaMessage(String message, long timestamp) {

        if (!isAdded()) {
            // Do nothing if the Fragment isn't added
            return;
        }
        if (message.startsWith("$GPGGA") || message.startsWith("$GNGNS")) {
            Double altitudeMsl = GpsHelper.getAltitudeMeanSeaLevel(message);
            if (altitudeMsl != null && mNavigating) {

                switch (displayUnits) {
                    case 1:
                        mOrtho = convertMetersToValue(altitudeMsl,1);
                        mOrthoHeightView.setText(getString(R.string.gps_msl_value_feet, convertMetersToValue(altitudeMsl,1)));
                        break;

                    case 2:
                        mOrtho = convertMetersToValue(altitudeMsl,2);
                        mOrthoHeightView.setText(getString(R.string.gps_msl_value_feet, convertMetersToValue(altitudeMsl,2)));
                        break;

                    case 3:
                        mOrtho = altitudeMsl;
                        mOrthoHeightView.setText(getString(R.string.gps_msl_value_metric, altitudeMsl));
                        break;

                    default:
                        mOrtho = altitudeMsl;
                        mOrthoHeightView.setText(getString(R.string.gps_msl_value_metric, altitudeMsl));
                        break;
                }
            }
        }
        if (message.startsWith("$GNGSA") || message.startsWith("$GPGSA")) {
            Dop dop = GpsHelper.getDop(message);
            if (dop != null && mNavigating) {

                mPdopView.setText(getString(R.string.gps_pdop_value, dop.getPositionDop()));
                mHdopView.setText(getString(R.string.gps_hdop_value, dop.getHorizontalDop()));
                mVdopView.setText(getString(R.string.gps_vdop_value, dop.getVerticalDop()));

            }
        }

    }

    private void updateFixTime() {
        //TODO Show elapsed time since TTFF

    }

    private void setStarted(boolean navigating) {
        if (navigating != mNavigating) {
            if (navigating) {

            } else {
                mLatitudeView.setText(getString(R.string.gps_latitude_default));
                mLongitudeView.setText(getString(R.string.gps_longitude_default));
                mEllipsoidView.setText(getString(R.string.gps_ellipsoid_default));
                mOrthoHeightView.setText(getString(R.string.gps_msl_default));

                mFixTime = 0;
                updateFixTime();
                mTTFFView.setText("");

                mAccuracyView.setText("");

                mNumSats.setText(getString(R.string.satellite_default));
                mSvCount = 0;

                mPdopView.setText(getString(R.string.gps_pdop_default));
                mHdopView.setText(getString(R.string.gps_hdop_default));
                mVdopView.setText(getString(R.string.gps_vdop_default));


            }
            mNavigating = navigating;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void updateGnssStatus(GnssStatus status) {
        setStarted(true);
        updateFixTime();

        mSnrCn0Title = mRes.getString(R.string.gps_cn0_label);

        if (mPrns == null) {
            final int MAX_LENGTH = 255;
            mPrns = new int[MAX_LENGTH];
            mSnrCn0s = new float[MAX_LENGTH];
            mSvElevations = new float[MAX_LENGTH];
            mSvAzimuths = new float[MAX_LENGTH];
            mConstellationType = new int[MAX_LENGTH];
            mHasEphemeris = new boolean[MAX_LENGTH];
            mHasAlmanac = new boolean[MAX_LENGTH];
            mUsedInFix = new boolean[MAX_LENGTH];
        }

        final int length = status.getSatelliteCount();
        mSvCount = 0;
        mUsedInFixCount = 0;
        while (mSvCount < length) {
            int prn = status.getSvid(mSvCount);
            mPrns[mSvCount] = prn;
            mConstellationType[mSvCount] = status.getConstellationType(mSvCount);
            mSnrCn0s[mSvCount] = status.getCn0DbHz(mSvCount);
            mSvElevations[mSvCount] = status.getElevationDegrees(mSvCount);
            mSvAzimuths[mSvCount] = status.getAzimuthDegrees(mSvCount);
            mHasEphemeris[mSvCount] = status.hasEphemerisData(mSvCount);
            mHasAlmanac[mSvCount] = status.hasAlmanacData(mSvCount);
            mUsedInFix[mSvCount] = status.usedInFix(mSvCount);
            if (status.usedInFix(mSvCount)) {
                mUsedInFixCount++;
            }

            mSvCount++;
        }

        mNumSats.setText(String.valueOf(mSvCount));
        mNumSatsLocked.setText(String.valueOf(mUsedInFixCount));
    }

    @Deprecated
    private void updateLegacyStatus(GpsStatus status) {
        setStarted(true);
        updateFixTime();

        mSnrCn0Title = mRes.getString(R.string.gps_snr_label);

        Iterator<GpsSatellite> satellites = status.getSatellites().iterator();

        if (mPrns == null) {
            int length = status.getMaxSatellites();
            mPrns = new int[length];
            mSnrCn0s = new float[length];
            mSvElevations = new float[length];
            mSvAzimuths = new float[length];
            // Constellation type isn't used, but instantiate it to avoid NPE in legacy devices
            mConstellationType = new int[length];
            mHasEphemeris = new boolean[length];
            mHasAlmanac = new boolean[length];
            mUsedInFix = new boolean[length];
        }

        mSvCount = 0;
        mUsedInFixCount = 0;
        while (satellites.hasNext()) {
            GpsSatellite satellite = satellites.next();
            int prn = satellite.getPrn();
            mPrns[mSvCount] = prn;
            mSnrCn0s[mSvCount] = satellite.getSnr();
            mSvElevations[mSvCount] = satellite.getElevation();
            mSvAzimuths[mSvCount] = satellite.getAzimuth();
            mHasEphemeris[mSvCount] = satellite.hasEphemeris();
            mHasAlmanac[mSvCount] = satellite.hasAlmanac();
            mUsedInFix[mSvCount] = satellite.usedInFix();
            if (satellite.usedInFix()) {
                mUsedInFixCount++;
            }
            mSvCount++;
        }

        mNumSats.setText(String.valueOf(mSvCount));
        mNumSatsLocked.setText(String.valueOf(mUsedInFixCount));

    }


    @Override
    public void onMapReady(GoogleMap googleMap) {

        Log.e(TAG, "Start: onMapReady");
        mMap = googleMap;
        mUiSettings = mMap.getUiSettings();

        initMapSettings(googleMap,mUiSettings);

        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mMap.setMyLocationEnabled(true);
        mMap.animateCamera(CameraUpdateFactory.zoomTo(19));


            Log.e(TAG, "Complete: onMapReady");
    }

    private void initMapSettings(GoogleMap map, UiSettings settings){

        switch (mapType){

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

        settings.setZoomControlsEnabled(true);  //Zoom Controls Shown
        settings.setCompassEnabled(true); //Compass controls shown


    }


    public void updateCurrentLocationMarker(Location currentLatLng){
        if(mMap !=null){
            LatLng latlng = new LatLng(currentLatLng.getLatitude(),currentLatLng.getLongitude());
            if(currentPositionMarker ==null){
                currentPositionMarker = new MarkerOptions();

                currentPositionMarker.position(latlng);
                currentPositionMarker.title("New Project");
                //currentPositionMarker.icon(BitmapDescriptorFactory.fromResource(R.drawable.button_framed_blue));

                currentLocationMarker = mMap.addMarker(currentPositionMarker);

            }

            if(currentLocationMarker !=null){
                currentLocationMarker.setPosition(latlng);

                mMap.moveCamera(CameraUpdateFactory.newLatLng(latlng));
            }




        }




    }

}
