package com.survlogic.survlogic.ARvS.utils;

import android.app.Activity;
import android.content.Context;
import android.location.GnssMeasurementsEvent;
import android.location.GnssNavigationMessage;
import android.location.GnssStatus;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.location.OnNmeaMessageListener;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.util.Log;
import com.survlogic.survlogic.interf.GpsSurveyListener;
import com.survlogic.survlogic.model.Dop;
import com.survlogic.survlogic.utils.GpsHelper;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by chrisfillmore on 3/22/2018.
 */

public class ArvSGnss implements LocationListener {
    private static final String TAG = "ArvSGnss";

    private Context mContext;
    private Activity mActivity;

    private GpsSurveyListener gnssListener;

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


    // Variables for Getter & Setter
    private long minTime; //needs to be in milliseconds
    private float minDistance; //in meters
    private boolean autoStartGPS; //hot start GPS when GPS Activity is Loaded
    private boolean useVersionNApi; //Use N APIs for GNSS Status

    private int displayUnits = 3; //Units to display

    //    Constants
    private static final int SECONDS_TO_MILLISECONDS = 1000;
    public boolean gpsRunning = false; //gps service is running (True) or not running (false)

    //Location
    private Location mLocation;
    private double mLatitude, mLongitude, mAccuracy;

    //    Satellite Metadata
    private int mSvCount, mPrns[], mConstellationType[], mUsedInFixCount;
    private float mSnrCn0s[], mSvElevations[], mSvAzimuths[];
    private double mOrtho;
    private String mSnrCn0Title;
    private boolean mHasEphemeris[], mHasAlmanac[], mUsedInFix[];

    //Orthometric Height
    private double altitudeMsl = 0;

    //DOP Metadata
    private double pDop = 0, hDop = 0, vDop = 0;


    //    Satellite Constants
    private long mFixTime;
    private boolean mNavigating, mGotFix;


    //Getters
    private int howManySatellitesAvailable = 0, howManySatellitesLocked = 0;


    private static ArvSGnss sInstance;

    public ArvSGnss(GpsSurveyListener gnssListener) {
        this.gnssListener = gnssListener;
        sInstance = this;
    }

    public synchronized void buildGnssClient(Context context){
        Log.d(TAG, "buildGoogleGnssClient: Started");

        mContext = context;
        mActivity = (Activity) mContext;

        mLocationManager = (LocationManager) mActivity.getSystemService(Context.LOCATION_SERVICE);
        mProvider = mLocationManager.getProvider(LocationManager.GPS_PROVIDER);

        initDummySettings();

    }

    //----------------------------------------------------------------------------------------------//

    public void startGPS(){
        Log.d(TAG, "startGPS: Started");

        addStatusListener();
        addNmeaListener();

        warmBootGPS();

    }

    public void stopGPS(){
        Log.d(TAG, "stopGPS: Started");

        removeStatusListener();
        removeNmeaListener();

        if (GpsHelper.isGnssStatusListenerSupported()) {
            removeNavMessageListener();
        }

        if (GpsHelper.isGnssStatusListenerSupported()) {
            removeGnssMeasurementsListener();
        }

        mLocationManager.removeUpdates(this);

    }

    public static ArvSGnss getInstance() {
        Log.e(TAG, "Start: GpsSurveyActivity - getInstance");

        return sInstance;

    }

    //----------------------------------------------------------------------------------------------//

    public int getHowManySatellitesAvailable() {
        return howManySatellitesAvailable;
    }

    public int getHowManySatellitesLocked() {
        return howManySatellitesLocked;
    }

    public double getpDop() {
        return pDop;
    }

    public double gethDop() {
        return hDop;
    }

    public double getvDop() {
        return vDop;
    }

    public Location getLocation() {
        return mLocation;
    }

    public double getLatitude() {
        return mLatitude;
    }

    public double getLongitude() {
        return mLongitude;
    }

    public double getAccuracy() {
        return mAccuracy;
    }

    public double getAltitudeMsl() {
        return altitudeMsl;
    }


//----------------------------------------------------------------------------------------------//

    private void initDummySettings(){
        Log.d(TAG, "initDummySettings: Started");

        minTime = (long) (0.5 * SECONDS_TO_MILLISECONDS);

        minDistance = 0;
        autoStartGPS = true;
        useVersionNApi = true;

    }

    //----------------------------------------------------------------------------------------------//
    public void addListener(GpsSurveyListener listener) {
        Log.e(TAG, "Start: GpsSurveyActivity - addListener");

        mGpsSurveyListener.add(listener);

    }

    private void addStatusListener() {
        Log.d(TAG, "addStatusListener: Started");
//        Uses 7.0 GNSS Status Listener in lieu of any legacy listeners.

        if (GpsHelper.isGnssStatusListenerSupported() && useVersionNApi) {
            addGnssStatusListener();

        } else {
            addLegacyStatusListener();

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

    private void addLegacyStatusListener() {
        Log.d(TAG, "addLegacyStatusListener: Started");
        mLegacyStatusListener = new GpsStatus.Listener() {
            @Override
            public void onGpsStatusChanged(int event) {

                try {
                    mLegacyStatus = mLocationManager.getGpsStatus(mLegacyStatus);

                } catch (SecurityException ex) {
                    Log.e(TAG, "addLegacyStatusListener: " + ex);
                }

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

                        break;
                }

                for (GpsSurveyListener listener : mGpsSurveyListener) {
                    listener.onGpsStatusChanged(event, mLegacyStatus);
                }

                gpsEvents(event,mLegacyStatus);

            }
        };


        try {
            mLocationManager.addGpsStatusListener(mLegacyStatusListener);

        } catch (SecurityException ex) {
            Log.e(TAG, "addLegacyStatusListener: " + ex);
        }
    }

    //----------------------------------------------------------------------------------------------//
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

    //----------------------------------------------------------------------------------------------//
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
        } else {
            addLegacyNmeaListener();
        }
    }


    private void addLegacyNmeaListener() {
        Log.d(TAG, "addLegacyNmeaListener: Started");
        if (mLegacyNmeaListener == null) {
            mLegacyNmeaListener = new GpsStatus.NmeaListener() {
                @Override
                public void onNmeaReceived(long timestamp, String nmea) {
                    Log.d(TAG, "onNmeaReceived: Started");
                    for (GpsSurveyListener listener : mGpsSurveyListener) {
                        listener.onNmeaMessage(nmea, timestamp);
                    }

                    parseDOPSFromNmeaMessage(nmea,timestamp);
                    parseOrthometricHeightFromNmeaMesage(nmea);
                }
            };
        }

        try {
            mLocationManager.addNmeaListener(mLegacyNmeaListener);
            
        } catch (SecurityException ex) {
            Log.e(TAG, "addLegacyNmeaListener: " + ex);
        }
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

    //----------------------------------------------------------------------------------------------//

    private synchronized void warmBootGPS() {
        Log.e(TAG, "Start: warmBootGPS");

        if (!gpsRunning) {
            try {

                mLocationManager.requestLocationUpdates(mProvider.getName(), minTime, minDistance, this);
                gpsRunning = true;

            } catch (SecurityException ex) {
                Log.e(TAG, "startLocationUpdates: " + ex);
            }

        }

        Log.e(TAG, "Start: warmBootGPS - Set Listener");

        for (GpsSurveyListener listener : mGpsSurveyListener) {
            listener.gpsStart();
            mGotFix = false;

        }

    }

    private synchronized void stopGPSService() {
        Log.d(TAG, "stopGPSService: Started");
        if (gpsRunning) {
            mLocationManager.removeUpdates(this);
            gpsRunning = false;

        }

        for (GpsSurveyListener listener : mGpsSurveyListener) {
            listener.gpsStop();
        }
    }


    //----------------------------------------------------------------------------------------------//
    @Override
    public void onLocationChanged(Location location) {
        Log.d(TAG, "onLocationChanged: Started");
        mLastLocation = location;

        for (GpsSurveyListener listener : mGpsSurveyListener){
            listener.onLocationChanged(location);
        }

        mLocation = location;
        mLatitude = location.getLatitude();
        mLongitude = location.getLongitude();
        mAccuracy = location.getAccuracy();


    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        Log.d(TAG, "onStatusChanged: Started");

        for (GpsSurveyListener listener : mGpsSurveyListener){
            listener.onStatusChanged(provider,status,extras);

        }
    }

    @Override
    public void onProviderEnabled(String provider) {
        Log.d(TAG, "onProviderEnabled: Started");

        for (GpsSurveyListener listener : mGpsSurveyListener){
            listener.onProviderEnabled(provider);

        }

    }

    @Override
    public void onProviderDisabled(String provider) {
        Log.d(TAG, "onProviderDisabled: Started");

        for (GpsSurveyListener listener : mGpsSurveyListener) {
            listener.onProviderDisabled(provider);
        }
    }

    //----------------------------------------------------------------------------------------------//
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

    private void getLocation(Location location){
        Log.d(TAG, "getLocation: Started");

        mLatitude = location.getLatitude();
        mLongitude = location.getLongitude();
        mAccuracy = location.getAccuracy();




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

        howManySatellitesAvailable = mSvCount;
        howManySatellitesLocked = mUsedInFixCount;

    }

    @Deprecated
    private void updateLegacyStatus(GpsStatus status) {
        //updateFixTime();

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

        howManySatellitesAvailable = mSvCount;
        howManySatellitesLocked = mUsedInFixCount;

    }

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

    private void gpsEvents(int event, GpsStatus status){
        Log.d(TAG, "gpsEvents: Started");

        Log.d(TAG, "onGpsStatusChanged: Started");
        switch (event) {
            case GpsStatus.GPS_EVENT_STARTED:
                setNavigationMode(true);
                break;

            case GpsStatus.GPS_EVENT_STOPPED:
                setNavigationMode(false);
                break;

            case GpsStatus.GPS_EVENT_FIRST_FIX:
                break;

            case GpsStatus.GPS_EVENT_SATELLITE_STATUS:
                updateLegacyStatus(status);
                break;
        }

    }

}
