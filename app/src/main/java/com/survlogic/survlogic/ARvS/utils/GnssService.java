package com.survlogic.survlogic.ARvS.utils;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Criteria;
import android.location.GnssMeasurementsEvent;
import android.location.GnssNavigationMessage;
import android.location.GnssStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.location.OnNmeaMessageListener;
import android.os.BatteryManager;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.survlogic.survlogic.interf.GpsSurveyListener;
import com.survlogic.survlogic.model.Dop;
import com.survlogic.survlogic.utils.GpsHelper;

import java.util.ArrayList;

public class GnssService extends Service implements LocationListener {
    private static final String TAG = "ArvSGnss";

    private final LocationServiceBinder binder = new LocationServiceBinder();

    //    Location Managers (GPS)
    private LocationManager mLocationManager;

    // Variables for Getter & Setter
    private Integer gpsFreqInMillis;
    private Integer gpsFreqInDistance;  // in meters

    //System Variables
    private boolean isGpsRunning = false;
    private boolean isGpsLogging = false;
    private int gpsCount;

    //Filter
    private ArrayList<Location> rawLocationList;
    private java.util.ArrayList<Location> filteredLocationList;

    private ArrayList<Location> oldLocationList;
    private ArrayList<Location> noAccuracyLocationList;
    private ArrayList<Location> inaccurateLocationList;
    private ArrayList<Location> kalmanNGLocationList;

    private ArvKalmanLatLong kalmanFilter;
    private long runStartTimeInMillis;
    private float currentSpeed = 0.0f; // meters/second

    //Battery Status
    private ArrayList<Integer> batteryLevelList;
    private ArrayList<Float> batteryLevelScaledList;
    private int batteryScale;

    private float batteryCriticalScaledValue = 0.10f;  //10% battery for warning
    private boolean isBatteryCritical = false;
    private boolean isBatteryWarningSurpressed = false;

    //Listeners
    //    GNSSStatus Call Listeners and CallBacks (GNSS Status - Version N)
    private ArrayList<GpsSurveyListener> mGpsSurveyListener = new ArrayList<GpsSurveyListener>();
    private GnssStatus mGnssStatus;
    private GnssStatus.Callback mGnssStatusListener;
    private GnssMeasurementsEvent.Callback mGnssMeasurementsListener;
    private OnNmeaMessageListener mOnNmeaMessageListener;
    private GnssNavigationMessage.Callback mGnssNavMessageListener;

    //    Satellite Constants
    private long mFixTime;
    private boolean mNavigating, mGotFix;

    //    Satellite Metadata
    private int mSvCount, mPrns[], mConstellationType[], mUsedInFixCount;
    private float mSnrCn0s[], mSvElevations[], mSvAzimuths[];
    private double mOrtho;
    private String mSnrCn0Title;
    private boolean mHasEphemeris[], mHasAlmanac[], mUsedInFix[];

    //    GNSS Settings
    public String mTtff;
    private boolean mLogNmea;

    //Orthometric Height
    private double altitudeMsl = 0;

    //DOP Metadata
    private double pDop = 0, hDop = 0, vDop = 0;

    public GnssService() {

    }

    //----------------------------------------------------------------------------------------------Class Methods
    /**
     * Class methods
     */
    @Override
    public void onCreate() {
        super.onCreate();

        initGnssSettings();
        initBatteryService();
        
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        return Service.START_STICKY;

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind: Started");
        return binder;

    }

    @Override
    public void onRebind(Intent intent) {

    }

    @Override
    public boolean onUnbind(Intent intent) {

        return true;
    }

    @Override
    public void onDestroy() {

    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        this.stopGPS();
        stopSelf();
    }

    //----------------------------------------------------------------------------------------------Binder Class
    /**
     * Binder class
     *
     */
    public class LocationServiceBinder extends Binder {
        public GnssService getService() {
            return GnssService.this;
        }
    }

    //----------------------------------------------------------------------------------------------Public Methods

    public void startGPS(){
        startUpdatingLocation();

        addStatusListener();
        addNmeaListener();
    }

    public void stopGPS(){
        stopUpdatingLocation();

        if(isGpsLogging){
            stopLogging();
        }

        removeStatusListener();
        removeNmeaListener();

        if (GpsHelper.isGnssStatusListenerSupported()) {
            removeNavMessageListener();
        }

        if (GpsHelper.isGnssStatusListenerSupported()) {
            removeGnssMeasurementsListener();
        }

    }

    public void startLogging(){
        isGpsLogging = true;
    }

    public void stopLogging(){
        isGpsLogging = false;

    }

    public boolean getGpsRunning(){
        return isGpsRunning;
    }

    public boolean getGpsLogging(){
        return isGpsLogging;
    }

    public Integer getBatteryLevelStart (){
        return batteryLevelList.get(0);

    }

    public Integer getBatteryLevelCurrent(){
        return batteryLevelList.get(batteryLevelList.size() - 1);
    }

    public float getBatteryLevelScaledStart (){
        return batteryLevelScaledList.get(0);

    }
    public float getBatteryLevelScaledCurrent(){
        return batteryLevelScaledList.get(batteryLevelScaledList.size() - 1);
    }

    public void setBatteryWarningSurpressed(boolean state){
        this.isBatteryWarningSurpressed = state;
    }

    public Integer getGpsFreqInMillis() {
        return gpsFreqInMillis;
    }

    public void setGpsFreqInMillis(Integer gpsFreqInMillis) {
        this.gpsFreqInMillis = gpsFreqInMillis;
    }

    public Integer getGpsFreqInDistance() {
        return gpsFreqInDistance;
    }

    public void setGpsFreqInDistance(Integer gpsFreqInDistance) {
        this.gpsFreqInDistance = gpsFreqInDistance;
    }

    public int howManySatellitesAvailable(){
        return mSvCount;
    }

    public int howManySatellitesUsedInFixCount(){
        return mUsedInFixCount;
    }

    public double getAltitudeMsl() {
        return altitudeMsl;
    }

    //----------------------------------------------------------------------------------------------Location Methods

    private void initGnssSettings(){
        Log.d(TAG, "initGnssSettings: ");

        gpsFreqInMillis = 500;
        gpsFreqInDistance = 0;

        rawLocationList = new ArrayList<>();
        filteredLocationList = new ArrayList<>();
        noAccuracyLocationList = new ArrayList<>();
        oldLocationList = new ArrayList<>();
        inaccurateLocationList = new ArrayList<>();
        kalmanNGLocationList = new ArrayList<>();
        kalmanFilter = new ArvKalmanLatLong(3);
    }
    
    
    private void startUpdatingLocation(){
        Log.d(TAG, "startUpdatingLocation: Started");

        if(!isGpsRunning){

            runStartTimeInMillis = SystemClock.elapsedRealtimeNanos() / 1000000;

            rawLocationList.clear();
            filteredLocationList.clear();
            oldLocationList.clear();
            noAccuracyLocationList.clear();
            inaccurateLocationList.clear();
            kalmanNGLocationList.clear();


            mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

            try {
                Criteria criteria = new Criteria();
                criteria.setAccuracy(Criteria.ACCURACY_FINE);
                criteria.setPowerRequirement(Criteria.POWER_HIGH);
                criteria.setAltitudeRequired(false);
                criteria.setSpeedRequired(false);
                criteria.setCostAllowed(true);
                criteria.setBearingRequired(false);

                //API level 9 and up
                criteria.setHorizontalAccuracy(Criteria.ACCURACY_HIGH);
                criteria.setVerticalAccuracy(Criteria.ACCURACY_HIGH);

                //TODO Add additional Listeners Here

                mLocationManager.requestLocationUpdates(gpsFreqInMillis,gpsFreqInDistance, criteria, this, null);

                gpsCount = 0;

                batteryLevelList.clear();
                batteryLevelScaledList.clear();

                isGpsRunning = true;

            } catch (SecurityException ex) {
                Log.e(TAG, "startLocationUpdates: " + ex.getLocalizedMessage());
            }

        }

    }

    private void stopUpdatingLocation(){
        Log.d(TAG, "stopUpdatingLocation: Started");
        if(isGpsRunning){

            mLocationManager.removeUpdates(this);
            isGpsRunning = false;
        }
    }

    //----------------------------------------------------------------------------------------------Filtering and Adding Methods

    private boolean filterAndAddLocation(Location location){
        Log.d(TAG, "filterAndAddLocation: Started");

        long age = getLocationAge(location);

        if(age > gpsFreqInMillis){
            Log.d(TAG, "filterAndAddLocation: Location is Old");
            oldLocationList.add(location);
            return false;
        }

        if(location.getAccuracy() <= 0){
            Log.d(TAG, "filterAndAddLocation: Latitude and Longitude values are invalid");
            noAccuracyLocationList.add(location);
            return false;
        }

        float horizontalAccuracy = location.getAccuracy();
        if(horizontalAccuracy > 10) {
            Log.d(TAG, "filterAndAddLocation: Accuracy: " + horizontalAccuracy + " exceeds 10m error tolerance");
            inaccurateLocationList.add(location);
            return false;

        }

        //------------------------------------------------------------------------------------------ Kalman Filter
        float qValue;

        long locationTimeInMillis = location.getElapsedRealtimeNanos() / 1000000;
        long elapsedTimeInMillis = locationTimeInMillis - runStartTimeInMillis;

        if(currentSpeed == 0.0f){
            qValue = 3.0f;
        }else{
            qValue = currentSpeed;
        }

        kalmanFilter.Process(location.getLatitude(),location.getLongitude(),location.getAccuracy(),elapsedTimeInMillis,qValue);
        double predictedLat = kalmanFilter.get_lat();
        double predictedLng = kalmanFilter.get_lng();
        float predictedAcc = kalmanFilter.get_accuracy();

        Location predictedLocation = new Location("");
        predictedLocation.setLatitude(predictedLat);
        predictedLocation.setLongitude(predictedLng);
        predictedLocation.setAccuracy(predictedAcc);

        float predictedDeltaInMeters = predictedLocation.distanceTo(location);

        if(predictedDeltaInMeters >60){
            Log.d(TAG, "filterAndAddLocation: Detected mal GPS, remove from track at: " + predictedDeltaInMeters);
            kalmanFilter.consecutiveRejectCount +=1;

            if(kalmanFilter.consecutiveRejectCount > 3){
                kalmanFilter = new ArvKalmanLatLong(3);
            }

            kalmanNGLocationList.add(location);
            return false;
        }else{
            kalmanFilter.consecutiveRejectCount = 0;
        }

        Intent intent = new Intent("PredictLocation");
        intent.putExtra("location",predictedLocation);
        LocalBroadcastManager.getInstance(this.getApplication()).sendBroadcast(intent);

        currentSpeed = location.getSpeed();
        filteredLocationList.add(location);


        Log.d(TAG, "filterAndAddLocation: Added at: " + predictedDeltaInMeters + " -- Location List: " + filteredLocationList.size());

        return true;

    }


    private long getLocationAge(Location newLocation){
        long locationAge;

        long currentTimeInMilli = SystemClock.elapsedRealtimeNanos() / 1000000;
        long locationTimeInMilli = newLocation.getElapsedRealtimeNanos() / 1000000;
        locationAge = currentTimeInMilli - locationTimeInMilli;

        return locationAge;
    }


    //----------------------------------------------------------------------------------------------Location Listener Methods
    /**
     * Location Listener Methods
     */

    @Override
    public void onLocationChanged(Location location) {

        gpsCount++;

        if(isGpsLogging){
            filterAndAddLocation(location);

        }

        Intent intent = new Intent("LocationUpdated");
        intent.putExtra("location", location);
        LocalBroadcastManager.getInstance(this.getApplication()).sendBroadcast(intent);


    }
    //----------------------------------------------------------------------------------------------Location Listener Support Methods

    /**
     * Location Listener Support Methods
     */

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        if (provider.equals(LocationManager.GPS_PROVIDER)) {
            if (status == LocationProvider.OUT_OF_SERVICE) {
                notifyLocationProviderStatusUpdated(false);
            } else {
                notifyLocationProviderStatusUpdated(true);
            }
        }
    }

    @Override
    public void onProviderEnabled(String provider) {
        if(provider.equals(LocationManager.GPS_PROVIDER)){
            notifyLocationProviderStatusUpdated(true);
        }
    }

    @Override
    public void onProviderDisabled(String provider) {
        if (provider.equals(LocationManager.GPS_PROVIDER)) {
            notifyLocationProviderStatusUpdated(false);
        }
    }

    private void notifyLocationProviderStatusUpdated(boolean isLocationProviderAvailable) {
        //Broadcast location provider status change here
    }

    //----------------------------------------------------------------------------------------------// Listener - GNSS Status
    private void addStatusListener() {
        Log.d(TAG, "addStatusListener: Started");

        if (GpsHelper.isGnssStatusListenerSupported()) {
            addGnssStatusListener();

        }

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
                setNavigationMode(true);
            }

            @Override
            public void onStopped() {
                for (GpsSurveyListener listener : mGpsSurveyListener) {
                    listener.onGnssStopped();
                }
                setNavigationMode(false);
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

                updateGnssStatus(status);
                setNavigationMode(true);
            }
        };

        try {
            mLocationManager.registerGnssStatusCallback(mGnssStatusListener);

        }catch (SecurityException ex) {
            Log.e(TAG, "addGnssStatusListener: " + ex);
        }

    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void addGnssMeasurementsListener() {
        Log.d(TAG, "addGnssMeasurementsListener: Started");

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

        try {
            mLocationManager.registerGnssMeasurementsCallback(mGnssMeasurementsListener);

        } catch (SecurityException ex) {
            Log.e(TAG, "addGnssMeasurementsListener: " + ex);
        }
    }

    private void removeStatusListener() {

        if (GpsHelper.isGnssStatusListenerSupported()) {
            removeGnssStatusListener();
        } else {

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


    @RequiresApi(api = Build.VERSION_CODES.N)
    private void removeNavMessageListener() {
        if (mLocationManager != null && mGnssNavMessageListener != null) {
            mLocationManager.unregisterGnssNavigationMessageCallback(mGnssNavMessageListener);
        }
    }

    //----------------------------------------------------------------------------------------------//Listener - NMEA
    private void addNmeaListener() {
        Log.d(TAG, "addNmeaListener: Started");
        if (GpsHelper.isGnssStatusListenerSupported()) {
            addNmeaListenerAndroidN();
        } else {
            //Legacy Info Here
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void addNmeaListenerAndroidN() {
        Log.d(TAG, "addNmeaListenerAndroidN: Started");

        if (mOnNmeaMessageListener == null) {
            mOnNmeaMessageListener = new OnNmeaMessageListener() {
                @Override
                public void onNmeaMessage(String message, long timestamp) {
                    for (GpsSurveyListener listener : mGpsSurveyListener) {
                        listener.onNmeaMessage(message, timestamp);
                    }
                    parseDOPSFromNmeaMessage(message,timestamp);
                    parseOrthometricHeightFromNmeaMesage(message);
                }
            };
        }

        try {
            mLocationManager.addNmeaListener(mOnNmeaMessageListener);

        } catch (SecurityException ex) {
            Log.e(TAG, "addNmeaListenerAndroidN: " + ex);
        }
    }

    private void removeNmeaListener() {
        if (GpsHelper.isGnssStatusListenerSupported()) {
            if (mLocationManager != null && mOnNmeaMessageListener != null) {
                mLocationManager.removeNmeaListener(mOnNmeaMessageListener);
            }
        }
    }


    //----------------------------------------------------------------------------------------------//Gnss Status Listener Methods
    private void setNavigationMode(boolean navigating){
        Log.d(TAG, "setNavigationMode: Started");

        if (navigating != mNavigating) {
            if (navigating) {

            } else {
                mFixTime = 0;

            }
            mNavigating = navigating;
        }

    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void updateGnssStatus(GnssStatus status) {
        //updateFixTime();

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

        Log.d(TAG, "updateGnssStatus: No. Sats: " + mSvCount);
        Log.d(TAG, "updateGnssStatus: Locked: " + mUsedInFixCount);

        Intent intent = new Intent("GnssStatus");
        intent.putExtra("svCount", mSvCount);
        intent.putExtra("usedInFixCount", mUsedInFixCount);
        LocalBroadcastManager.getInstance(this.getApplication()).sendBroadcast(intent);

    }

    //---------------------------------------------------------------------------------------------- Nmea Listener Methods
    private void parseDOPSFromNmeaMessage(String message, long timestamp){
        Log.d(TAG, "getDOPSFromNmeaMessage: Started");

        if (message.startsWith("$GNGSA") || message.startsWith("$GPGSA")) {
            Dop dop = GpsHelper.getDop(message);

            Log.d(TAG, "parseDOPSFromNmeaMessage: dop" + dop);
            Log.d(TAG, "parseDOPSFromNmeaMessage: mNavigating: " + mNavigating);

            if (dop != null && mNavigating) {

                pDop = dop.getPositionDop();
                hDop = dop.getHorizontalDop();
                vDop = dop.getVerticalDop();

            }
        }
    }

    private void parseOrthometricHeightFromNmeaMesage(String message){
        Log.d(TAG, "parseOrthometricHeightFromNmeaMesage: Started");


        if (message.startsWith("$GPGGA") || message.startsWith("$GNGNS")) {
            Double altitudeMsl = GpsHelper.getAltitudeMeanSeaLevel(message);

            if (altitudeMsl != null && mNavigating) {
                this.altitudeMsl = GpsHelper.getAltitudeMeanSeaLevel(message);

            }

        }
    }
    
    //---------------------------------------------------------------------------------------------- Battery Services
    private void initBatteryService(){
        Log.d(TAG, "initBatteryService: Started");

        batteryLevelList = new ArrayList<>();
        batteryLevelScaledList = new ArrayList<>();

        registerReceiver(this.batteryInfoReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));

    }

    private BroadcastReceiver batteryInfoReceiver  = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int batteryLevel = intent.getIntExtra(BatteryManager.EXTRA_LEVEL,0);
            int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE,-1);

            float batteryLevelScaled = batteryLevel/(float) scale;

            batteryLevelList.add(batteryLevel);
            batteryLevelScaledList.add(batteryLevelScaled);
            batteryScale = scale;

            if(batteryLevelScaled <= batteryCriticalScaledValue){
                broadcastBatteryWarning(batteryLevelScaled);
            }

        }
    };

    private void broadcastBatteryWarning(float batteryLevel){
        Log.d(TAG, "broadcastBatteryWarning: Started");

        if(!isBatteryWarningSurpressed){
            Intent intent = new Intent("BatteryWarning");
            intent.putExtra("batteryLevel",batteryLevel);
            LocalBroadcastManager.getInstance(this.getApplication()).sendBroadcast(intent);
        }


    }


}
