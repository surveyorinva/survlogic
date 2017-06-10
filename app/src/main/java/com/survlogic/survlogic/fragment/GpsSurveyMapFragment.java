package com.survlogic.survlogic.fragment;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.location.GnssMeasurementsEvent;
import android.location.GnssStatus;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.TextViewCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.LocationSource;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.survlogic.survlogic.R;
import com.survlogic.survlogic.activity.GpsSurveyActivity;
import com.survlogic.survlogic.interf.GpsSurveyListener;
import com.survlogic.survlogic.model.Dop;
import com.survlogic.survlogic.utils.GpsHelper;
import com.survlogic.survlogic.utils.LocationConverter;


import java.text.DecimalFormat;
import java.util.Iterator;

/**
 * Created by chrisfillmore on 5/2/2017.
 */

public class GpsSurveyMapFragment extends Fragment implements GpsSurveyListener, OnMapReadyCallback {

    //    Fragment Constants
    View v;

    //    Debugging Static Constants
    private static final String TAG = "GpsSurveyMapFragment";

    private Resources mRes;
    private Context mContext;
    private SupportMapFragment supportMapFragment;

    //    UI Views
    private TextView mLatitudeView, mLongitudeView, mEllipsoidView, mOrthoHeightView,
            mFixTimeView, mTTFFView,
            mAccuracyView, mAccuracyStatus,
            mNumSats, mNumSatsLocked,
            mPdopView, mHdopView, mVdopView;

    private ImageView mAccuracyStatusImage;

    private ProgressBar progressBarRecording;

    //    Satellite Metadata
    private int mSvCount, mPrns[], mConstellationType[], mUsedInFixCount;
    private float mSnrCn0s[], mSvElevations[], mSvAzimuths[];
    private String mSnrCn0Title;
    private boolean mHasEphemeris[], mHasAlmanac[], mUsedInFix[];

    //    Satellite Constants
    private long mFixTime;
    private boolean mNavigating, mGotFix;

    //    Mapping Variables
    private GoogleMap mMap;
    private LatLng mLatLng;
    private MarkerOptions currentPositionMarker = null;
    private Marker currentLocationMarker;

    //    Formatting
    DecimalFormat mFixedTwoFormat = new DecimalFormat("#.##");

    //    System Variables
    private static final float mPositionAutonomousMeters = 20;
    private static final float mPositionFloatMeters = 10;
    private static final float mPositionFixedMeters = 5;

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


        mAccuracyStatusImage = (ImageView) v.findViewById(R.id.gnss_status_img);

        progressBarRecording = (ProgressBar) v.findViewById(R.id.progressBarRecording);

        Log.e(TAG, "Setup Controls Completed");

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
            mEllipsoidView.setText(getString(R.string.gps_ellipsoid_value_metric, location.getAltitude()));
        } else {
            mEllipsoidView.setText("");
        }


//        Accuracy Model
        if (location.hasAccuracy()) {

            mAccuracyView.setText(mFixedTwoFormat.format(location.getAccuracy()));
            setAccuracyView(location,false);

        } else {
            mAccuracyView.setText("");
            setAccuracyView(location,true);
        }
        updateFixTime();

//        Create Map Instances
        updateCurrentLocationMarker(location);

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
                mOrthoHeightView.setText(getString(R.string.gps_msl_value_metric, altitudeMsl));
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

        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mMap.setMyLocationEnabled(true);
        mMap.animateCamera(CameraUpdateFactory.zoomTo(15));


            Log.e(TAG, "Complete: onMapReady");
    }

    public void updateCurrentLocationMarker(Location currentLatLng){
        if(mMap !=null){
            LatLng latlng = new LatLng(currentLatLng.getLatitude(),currentLatLng.getLongitude());
            if(currentPositionMarker ==null){
                currentPositionMarker = new MarkerOptions();

                currentPositionMarker.position(latlng);
                currentPositionMarker.title("I am a Title");
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
