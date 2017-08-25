package com.survlogic.survlogic.activity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.GnssMeasurementsEvent;
import android.location.GnssNavigationMessage;
import android.location.GnssStatus;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.location.OnNmeaMessageListener;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.survlogic.survlogic.R;
import com.survlogic.survlogic.adapter.ActivityViewPagerAdapter;
import com.survlogic.survlogic.fragment.GpsSurveyMapFragment;
import com.survlogic.survlogic.fragment.GpsSurveySkyViewFragment;
import com.survlogic.survlogic.interf.GpsSurveyListener;
import com.survlogic.survlogic.utils.GpsHelper;

import java.util.ArrayList;

/**
 * Created by chrisfillmore on 5/16/2017.
 */

public class GpsSurveyActivity extends AppCompatActivity implements LocationListener {

    //    UI View Tools
    private Toolbar toolbar;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private ActivityViewPagerAdapter viewPagerAdapter;
    private ProgressBar progressBar;

    //    Fragment Elements
    private static GpsSurveyActivity sInstance;


    //    Debugging Static Constrants
    private static final String TAG = "GPSSurveyActivity";

    //    Location Managers (GPS)
    private LocationManager mLocationManager;
    private LocationProvider mProvider;
    private Location mLastLocation;

    //    GNSSStatus Call Listeners and CallBacks (GNSS Status - Version N)
    private ArrayList<GpsSurveyListener> mGpsSurveyListener = new ArrayList<GpsSurveyListener>();
    private GnssStatus mGnssStatus;
    private GnssStatus.Callback mGnssStatusListener;
    private GnssMeasurementsEvent.Callback mGnssMeasurementsListener;
    private OnNmeaMessageListener mOnNmeaMessageListener;
    private GnssNavigationMessage.Callback mGnssNavMessageListener;

    //    GNSS Settings
    public String mTtff;
    private boolean mLogNmea;

    //    GPSStatus Call Listeners and CallBacks (GPS Status - < Version N)
    private GpsStatus mLegacyStatus;
    private GpsStatus.Listener mLegacyStatusListener;
    private GpsStatus.NmeaListener mLegacyNmeaListener;


    //    Settings
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    private long minTime; //needs to be in milliseconds
    private float minDistance; //in meters
    private boolean autoStartGPS; //hot start GPS when GPS Activity is Loaded
    private boolean useVersionNApi; //Use N APIs for GNSS Status

    private int displayUnits = 3; //Units to display

    //    Constants
    private static final int SECONDS_TO_MILLISECONDS = 1000;
    public boolean gpsRunning; //gps service is running (True) or not running (false)


    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gps_survey);

        sInstance = this;

            Log.e(TAG, "Start: onCreateView");

//        Initialize UI Views
        initView();

//        Initialize GPS
        initGNSS();

//        Initialize Settings
        initSettings();

//        Initialize Fragment
        initViewPager();

            Log.e(TAG, "Complete: onCreateView");

    }

    @Override
    protected void onResume() {
        super.onResume();

            Log.e(TAG, "Start: onResume - add Status Listener");


        addStatusListener();

            Log.e(TAG, "Complete: add Status Listener");
            Log.e(TAG, "Start: onResume - add Nmea Listener");

        addNmeaListener();

            Log.e(TAG, "Complete: add Nmea Listener");

        if (!mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            promptEnableGps();
        }
            Log.e(TAG, "Complete: onResume");

        checkPreferenceScreenOn(sharedPreferences);
        checkPreferenceInterval(sharedPreferences);
        checkPreferenceUnits(sharedPreferences);

    }

    @Override
    protected void onPause() {

            Log.e(TAG, "Start: onPause");

        removeStatusListener();
        removeNmeaListener();

        if (GpsHelper.isGnssStatusListenerSupported()) {
            removeNavMessageListener();
        }

        if (GpsHelper.isGnssStatusListenerSupported()) {
            removeGnssMeasurementsListener();
        }

        super.onPause();

            Log.e(TAG, "Complete: onPause");

    }

    @Override
    protected void onDestroy() {

            Log.e(TAG, "Start: onDestroy");

        mLocationManager.removeUpdates(this);
        super.onDestroy();

            Log.e(TAG, "Complete: onDestroy");
    }


    private void initView() {
            Log.e(TAG, "Start: initView");

        toolbar = (Toolbar) findViewById(R.id.toolbar_in_gps_survey_layout);
        toolbar.setTitle("GPS Survey");
        setSupportActionBar(toolbar);

        progressBar = (ProgressBar) findViewById(R.id.progressBar_Loading_gps);

            Log.e(TAG, "Complete: initView");
    }


    private void initViewPager() {

            Log.e(TAG, "Start: initViewPager");

        tabLayout = (TabLayout) findViewById(R.id.tab_in_gps_survey_layout);
        viewPager = (ViewPager) findViewById(R.id.viewpager_in_gps_survey_layout);

        viewPagerAdapter = new ActivityViewPagerAdapter(getSupportFragmentManager());
        viewPagerAdapter.addFragments(new GpsSurveyMapFragment(), "Survey");
        //viewPagerAdapter.addFragments(new template_Fragment(), "GPS Status");
        viewPagerAdapter.addFragments(new GpsSurveySkyViewFragment(), "Sky View");

            Log.e(TAG, "Start: initViewPager - viewPagerAdapter");
        viewPager.setAdapter(viewPagerAdapter);
        tabLayout.setupWithViewPager(viewPager);

            Log.e(TAG, "Complete: initViewPager - viewPagerAdapter");

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });


    }


    private void initGNSS() {
            Log.e(TAG, "Start: initGNSS");

        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        mProvider = mLocationManager.getProvider(LocationManager.GPS_PROVIDER);

            Log.e(TAG, "Complete: initView");

        if (mProvider == null) {
            Log.e(TAG, "Unable to get GPS_Provider");

            Toast.makeText(this, getString(R.string.debug_error_gps_not_found), Toast.LENGTH_SHORT).show();

            finish();
        }
    }

    private void initSettings() {
            Log.e(TAG, "Start: initSettings");

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        boolean keepScreenOn = sharedPreferences.getBoolean(getString(R.string.pref_key_keep_screen_on),true);

        checkPreferenceScreenOn(sharedPreferences);

        double tempMinTime = Double.valueOf(
                sharedPreferences.getString(getString(R.string.pref_key_gps_min_time), getString(R.string.pref_gps_min_time_default_sec)));

        minTime = (long) (tempMinTime * SECONDS_TO_MILLISECONDS);

        minDistance = 0;
        autoStartGPS = true;
        useVersionNApi = true;


        displayUnits = Integer.valueOf(
                sharedPreferences.getString(getString(R.string.pref_key_gps_unit_measurement), getString(R.string.pref_gps_unit_measurement_default)));



        Log.e(TAG, "Complete: Assign Variables, Checking AutoStart GPS");

        if (autoStartGPS) {
            warmBootGPS();
        }

            Log.e(TAG, "Complete: initSettings");
    }


    private void checkPreferenceScreenOn(SharedPreferences settings){
        boolean keepScreenOn = settings.getBoolean(getString(R.string.pref_key_keep_screen_on),true);

        if (keepScreenOn){
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }else{
            getWindow().clearFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }

    }

    private void checkPreferenceInterval(SharedPreferences settings) {
        double tempMinTimeDouble = Double
                .valueOf(settings.getString(getString(R.string.pref_key_gps_min_time), "1"));
        long minTimeLong = (long) (tempMinTimeDouble * SECONDS_TO_MILLISECONDS);

        if (minTime != minTimeLong ||
                minDistance != Float.valueOf(
                        settings.getString(getString(R.string.pref_key_gps_min_distance), "0"))) {
            // User changed preference values, get the new ones
            minTime = minTimeLong;

            // If the GPS is started, reset the location listener with the new values

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                promptEnableGps();
                return;
            }

            if (gpsRunning) {
                mLocationManager.requestLocationUpdates(mProvider.getName(), minTime, minDistance, this);
                Toast.makeText(this, String.format(getString(R.string.gps_warm_boot_go),
                        String.valueOf((double) minTime / SECONDS_TO_MILLISECONDS),
                        String.valueOf(minDistance)), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void checkPreferenceUnits(SharedPreferences settings){
        displayUnits = Integer.valueOf(
                sharedPreferences.getString(getString(R.string.pref_key_gps_unit_measurement), getString(R.string.pref_gps_unit_measurement_default)));

    }


    public static GpsSurveyActivity getInstance() {
            Log.e(TAG, "Start: GpsSurveyActivity - getInstance");

        return sInstance;

    }

    public void addListener(GpsSurveyListener listener) {
            Log.e(TAG, "Start: GpsSurveyActivity - addListener");

        mGpsSurveyListener.add(listener);

    }

    private synchronized void warmBootGPS() {
            Log.e(TAG, "Start: warmBootGPS1");

        if (!gpsRunning) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                promptEnableGps();
                return;
            }

            Log.e(TAG, "Start: warmBoot - requestLocationUpdates2");
            mLocationManager.requestLocationUpdates(mProvider.getName(), minTime, minDistance, this);
            gpsRunning = true;

            Log.e(TAG, "Complete: warmBoot - requestLocationUpdates3");

            Toast.makeText(this, String.format(getString(R.string.gps_warm_boot_go),
                    String.valueOf((double) minTime / SECONDS_TO_MILLISECONDS),
                    String.valueOf(minDistance)), Toast.LENGTH_SHORT).show();

        }

            Log.e(TAG, "Start: warmBootGPS - Set Listener4");

        for (GpsSurveyListener listener : mGpsSurveyListener) {
            listener.gpsStart();

        }
            Log.e(TAG, "Complete: warmBootGPS - Set Listener5");

    }

    private synchronized void stopGPSService() {
        if (gpsRunning) {
            mLocationManager.removeUpdates(this);
            gpsRunning = false;

            Toast.makeText(this, (getString(R.string.gps_stop)), Toast.LENGTH_SHORT).show();

        }

        for (GpsSurveyListener listener : mGpsSurveyListener) {
            listener.gpsStop();
        }
    }

    private boolean sendExtraCommand(String command) {
        return mLocationManager.sendExtraCommand(LocationManager.GPS_PROVIDER, command, null);
    }

    private void addStatusListener() {
//        Uses 7.0 GNSS Status Listener in lieu of any legacy listeners.

            Log.e(TAG, "Start: addStatusListener");

        if (GpsHelper.isGnssStatusListenerSupported() && useVersionNApi) {
            Log.e(TAG, "Adding: addStatusListener - GNSSStatusListener");

            addGnssStatusListener();

        } else {
            Log.e(TAG, "Adding: addStatusListener - LegacyStatusListener");

            addLegacyStatusListener();

        }

            Log.e(TAG, "Complete: addStatusListener");
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private void addGnssStatusListener() {

            Log.e(TAG, "Start: addGnssStatusListener");

        mGnssStatusListener = new GnssStatus.Callback() {
            @Override
            public void onStarted() {
                for (GpsSurveyListener listener : mGpsSurveyListener) {
                    listener.onGnssStarted();
                }
            }

            @Override
            public void onStopped() {
                for (GpsSurveyListener listener : mGpsSurveyListener) {
                    listener.onGnssStopped();
                }
            }

            @Override
            public void onFirstFix(int ttffMillis) {
                if (ttffMillis == 0) {
                    mTtff = "";
                } else {
                    ttffMillis = (ttffMillis + 500) / 1000;
                    mTtff = Integer.toString(ttffMillis) + " sec";
                }
                for (GpsSurveyListener listener : mGpsSurveyListener) {
                    listener.onGnssFirstFix(ttffMillis);
                }
            }

            @Override
            public void onSatelliteStatusChanged(GnssStatus status) {
                mGnssStatus = status;
                for (GpsSurveyListener listener : mGpsSurveyListener) {
                    listener.onSatelliteStatusChanged(mGnssStatus);
                }
            }
        };
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            promptEnableGps();
            return;
        }

            Log.e(TAG, "Start: addGnssListener - Register Callback");

        mLocationManager.registerGnssStatusCallback(mGnssStatusListener);

            Log.e(TAG, "Complete: addGnssListener - Register Callback");
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void addGnssMeasurementsListener() {
        mGnssMeasurementsListener = new GnssMeasurementsEvent.Callback() {
            @Override
            public void onGnssMeasurementsReceived(GnssMeasurementsEvent event) {
                for (GpsSurveyListener listener : mGpsSurveyListener) {
                    listener.onGnssMeasurementsReceived(event);
                }
            }

            @Override
            public void onStatusChanged(int status) {

            }
        };
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            promptEnableGps();
            return;
        }
        mLocationManager.registerGnssMeasurementsCallback(mGnssMeasurementsListener);
    }

    private void addLegacyStatusListener() {
        mLegacyStatusListener = new GpsStatus.Listener() {
            @Override
            public void onGpsStatusChanged(int event) {
                if (ActivityCompat.checkSelfPermission(GpsSurveyActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    promptEnableGps();
                    return;
                }
                mLegacyStatus = mLocationManager.getGpsStatus(mLegacyStatus);

                switch (event) {
                    case GpsStatus.GPS_EVENT_STARTED:
                        break;
                    case GpsStatus.GPS_EVENT_STOPPED:
                        break;
                    case GpsStatus.GPS_EVENT_FIRST_FIX:
                        int ttff = mLegacyStatus.getTimeToFirstFix();
                        if (ttff == 0) {
                            mTtff = "";
                        } else {
                            ttff = (ttff + 500) / 1000;
                            mTtff = Integer.toString(ttff) + " sec";
                        }
                        break;
                    case GpsStatus.GPS_EVENT_SATELLITE_STATUS:
                        // Stop progress bar after the first status information is obtained
                        setSupportProgressBarIndeterminateVisibility(Boolean.FALSE);
                        break;
                }

                for (GpsSurveyListener listener : mGpsSurveyListener) {
                    listener.onGpsStatusChanged(event, mLegacyStatus);
                }
            }
        };
        mLocationManager.addGpsStatusListener(mLegacyStatusListener);
    }


    private void removeStatusListener() {

        if (GpsHelper.isGnssStatusListenerSupported() && useVersionNApi) {
            removeGnssStatusListener();
        } else {
            removeLegacyStatusListener();
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private void removeGnssStatusListener() {
        mLocationManager.unregisterGnssStatusCallback(mGnssStatusListener);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void removeGnssMeasurementsListener() {
        if (mLocationManager != null && mGnssMeasurementsListener != null) {
            mLocationManager.unregisterGnssMeasurementsCallback(mGnssMeasurementsListener);
        }
    }

    private void removeLegacyStatusListener() {
        if (mLocationManager != null && mLegacyStatusListener != null) {
            mLocationManager.removeGpsStatusListener(mLegacyStatusListener);
        }
    }

    private void addNmeaListener() {
        if (GpsHelper.isGnssStatusListenerSupported()) {
            addNmeaListenerAndroidN();
        } else {
            //Legacy Info Here
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void addNmeaListenerAndroidN() {
        if (mOnNmeaMessageListener == null) {
            mOnNmeaMessageListener = new OnNmeaMessageListener() {
                @Override
                public void onNmeaMessage(String message, long timestamp) {
                    for (GpsSurveyListener listener : mGpsSurveyListener) {
                        listener.onNmeaMessage(message, timestamp);
                    }
                }
            };
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            promptEnableGps();
            return;
        }
        mLocationManager.addNmeaListener(mOnNmeaMessageListener);
    }

    private void removeNmeaListener() {
        if (GpsHelper.isGnssStatusListenerSupported()) {
            if (mLocationManager != null && mOnNmeaMessageListener != null) {
                mLocationManager.removeNmeaListener(mOnNmeaMessageListener);
            }
        } else {
            addLegacyNmeaListener();
        }
    }


    private void addLegacyNmeaListener() {
        if (mLegacyNmeaListener == null) {
            mLegacyNmeaListener = new GpsStatus.NmeaListener() {
                @Override
                public void onNmeaReceived(long timestamp, String nmea) {
                    for (GpsSurveyListener listener : mGpsSurveyListener) {
                        listener.onNmeaMessage(nmea, timestamp);
                    }
                }
            };
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            promptEnableGps();
            return;
        }
        mLocationManager.addNmeaListener(mLegacyNmeaListener);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void addNavMessageListener() {
        if (mGnssNavMessageListener == null) {
            mGnssNavMessageListener = new GnssNavigationMessage.Callback() {
                @Override
                public void onGnssNavigationMessageReceived(GnssNavigationMessage event) {
                    //Write Message to Log
                }

                @Override
                public void onStatusChanged(int status) {

                }
            };
        }
        mLocationManager.registerGnssNavigationMessageCallback(mGnssNavMessageListener);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void removeNavMessageListener() {
        if (mLocationManager != null && mGnssNavMessageListener != null) {
            mLocationManager.unregisterGnssNavigationMessageCallback(mGnssNavMessageListener);
        }
    }

    private void promptEnableGps() {
        new AlertDialog.Builder(this)
                .setMessage(getString(R.string.enable_gps_message))
                .setPositiveButton(getString(R.string.general_yes),
                        new DialogInterface.OnClickListener() {

                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = new Intent(
                                        Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                startActivity(intent);
                            }
                        }
                )
                .setNegativeButton(getString(R.string.general_no),
                        new DialogInterface.OnClickListener() {

                            public void onClick(DialogInterface dialog, int which) {
                            }
                        }
                )
                .show();
    }



    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;

        for (GpsSurveyListener listener : mGpsSurveyListener){
            listener.onLocationChanged(location);
        }

        if(progressBar.isShown()){
            progressBar.setVisibility(View.GONE);
        }

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        for (GpsSurveyListener listener : mGpsSurveyListener){
            listener.onStatusChanged(provider,status,extras);

        }
    }

    @Override
    public void onProviderEnabled(String provider) {
        for (GpsSurveyListener listener : mGpsSurveyListener){
            listener.onProviderEnabled(provider);

        }

    }

    @Override
    public void onProviderDisabled(String provider) {
        for (GpsSurveyListener listener : mGpsSurveyListener) {
            listener.onProviderDisabled(provider);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_gps_survey_menu,menu);

        return true;
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);


    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.toolbar_gps_survey_item1:
                goToSettingsMenu();
                break;

            case R.id.toolbar_gps_survey_item2:
                //some action here
                break;

        }

        return super.onOptionsItemSelected(item);
    }


    private void goToSettingsMenu() {
        Intent i = new Intent(this,SettingsGpsSurveyActivity.class);
        startActivity(i);

    }


}
