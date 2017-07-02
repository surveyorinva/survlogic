package com.survlogic.survlogic.fragment;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.location.GnssMeasurementsEvent;
import android.location.GnssStatus;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.survlogic.survlogic.R;
import com.survlogic.survlogic.activity.GpsSurveyActivity;
import com.survlogic.survlogic.adapter.SatelliteAdaptor;
import com.survlogic.survlogic.interf.GpsSurveyListener;
import com.survlogic.survlogic.model.Satellite;
import com.survlogic.survlogic.utils.GnssType;
import com.survlogic.survlogic.utils.GpsHelper;
import com.survlogic.survlogic.utils.LocationConverter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 * Created by chrisfillmore on 5/2/2017.
 */

public class GpsSurveySkyViewFragment extends Fragment implements GpsSurveyListener {

    //Fragment Constants
    Context mContext;
    View rootview;
    private Resources mRes;

    //Debugging Static Constants
    private static final String TAG = "GpsSurveySkyViewFrag";

    //SkyView
    final GpsSurveySkyViewCircleFragment skyViewFragment = new GpsSurveySkyViewCircleFragment();

    //Recycler View Card View and Adapter
    RecyclerView recyclerView;
    SatelliteAdaptor adapter;
    RecyclerView.LayoutManager layoutManager;
    HashMap<Integer,Satellite> hash = new HashMap<Integer, Satellite>();
    ArrayList<Satellite> list = new ArrayList<Satellite>();
    List<Satellite> consolidatedlist = new ArrayList<Satellite>();

    //Grid View Layout
    private GridView gridView;
    private static final int PRN_COLUMN = 0, FLAG_IMAGE_COLUMN = 1, SNR_COLUMN = 2, ELEVATION_COLUMN = 3;
    private static final int AZIMUTH_COLUMN = 4, FLAGS_COLUMN = 5, COLUMN_COUNT = 6;
    private Drawable mFlagUsa, mFlagRussia, mFlagJapan, mFlagChina, mFlagGalileo;

    private SvGridAdapter mAdapter;

    private boolean useRv = true;

    //    Satellite Metadata
    private int mSvCount, mPrns[], mConstellationType[], mUsedInFixCount;
    private float mSnrCn0s[], mSvElevations[], mSvAzimuths[];
    private String mSnrCn0Title;
    private boolean mHasEphemeris[], mHasAlmanac[], mUsedInFix[];

    //    Satellite Constants
    private long mFixTime;
    private boolean mNavigating, mGotFix;

//    Controls
    private TextView mLatitudeView, mLongitudeView,
        mNumSats, mNumSatsLocked;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootview = inflater.inflate(R.layout.fragment_gps_survey_sky_view, container, false);
        mRes = getResources();

        initView();

        initSkyView();

        if(useRv){
            initSatStatusRecyclerView();

        }else {
            initSatStatusGrid();

        }


        GpsSurveyActivity.getInstance().addListener(this);


        return rootview;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        initViewOnComponent();

    }



    private void initView() {
        Log.e(TAG, "Start: initView");

        mContext = getActivity();

        gridView = (GridView) rootview.findViewById(R.id.grid_satellite_status);
        recyclerView = (RecyclerView) rootview.findViewById(R.id.rv_satellite_status);



        Log.e(TAG, "Complete: initView");

    }


    private void initViewOnComponent() {

        if (isFragmentUIActive()) {

            mLatitudeView = (TextView) rootview.findViewById(R.id.skyView_Lat_value);
            mLongitudeView = (TextView) rootview.findViewById(R.id.skyView_Long_value);

            mNumSats = (TextView) rootview.findViewById(R.id.skyView_gps_status_noSatellites);
            mNumSatsLocked = (TextView) rootview.findViewById(R.id.skyView_gps_status_noSatellitesLocked);
        }

    }
    private void initSkyView(){
            Log.e(TAG, "Start: initSkyView");

        FragmentManager mfragmentManager = getChildFragmentManager();
        FragmentTransaction mfragmentTransaction = mfragmentManager.beginTransaction();

        mfragmentTransaction.add(R.id.skyview_container,skyViewFragment);
        mfragmentTransaction.addToBackStack(null);
        mfragmentTransaction.commit();

            Log.e(TAG, "Complete: initSkyView");

    }

    public boolean isFragmentUIActive() {
        return isAdded() && !isDetached() && !isRemoving();
    }


    private void initSatStatusGrid(){
            Log.e(TAG,"Start: initSatStatusGRID");


        mFlagUsa = ResourcesCompat.getDrawable(getResources(),R.drawable.ic_flag_usa,null);
        mFlagChina = ResourcesCompat.getDrawable(getResources(),R.drawable.ic_flag_china,null);
        mFlagGalileo = ResourcesCompat.getDrawable(getResources(),R.drawable.ic_flag_galileo,null);
        mFlagJapan = ResourcesCompat.getDrawable(getResources(),R.drawable.ic_flag_japan,null);
        mFlagRussia = ResourcesCompat.getDrawable(getResources(),R.drawable.ic_flag_russia,null);


        gridView.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);

        mAdapter = new SvGridAdapter(getActivity());

        gridView.setAdapter(mAdapter);
        gridView.setFocusable(false);
        gridView.setFocusableInTouchMode(false);

            Log.e(TAG,"Complete: initSatStatusGRID");

    }

    private void initSatStatusRecyclerView(){

            Log.e(TAG,"Complete: initSatStatusRECYCLER");
        if (isFragmentUIActive()) {
            adapter = new SatelliteAdaptor(mContext,list);

            layoutManager = new LinearLayoutManager(getActivity());
            recyclerView.setLayoutManager(layoutManager);

            recyclerView.setAdapter(adapter);
            recyclerView.setHasFixedSize(false);

            gridView.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
            Log.e(TAG,"Complete: initSatStatusRECYCLER");

    }

    @Override
    public void onResume() {
        super.onResume();

        GpsSurveyActivity gsa = GpsSurveyActivity.getInstance();
        setStarted(gsa.gpsRunning);

    }

    private void setStarted(boolean navigating) {
        if (navigating != mNavigating) {
            if (navigating) {

            } else {

                mFixTime = 0;
                mSvCount = 0;

                if(useRv){

                }else {
                    mAdapter.notifyDataSetChanged();
                }
            }
            mNavigating = navigating;
        }
    }


    @Override
    public void gpsStart() {
        //Reset flag for detecting first fix
        mGotFix = false;

    }

    @Override
    public void gpsStop() {
        //TODO Nothing here

    }

    @Override
    public void onLocationChanged(Location location) {

        mLatitudeView.setText(getString(R.string.gps_status_lat_value_string, LocationConverter.getLatitudeAsDMS(location, 3)));
        mLongitudeView.setText(getString(R.string.gps_status_long_value_string, LocationConverter.getLongitudeAsDMS(location, 3)));


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

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void updateGnssStatus(GnssStatus status) {
        setStarted(true);

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

        if(useRv){

                Log.e(TAG,"Start: Building Recycler View");

            if (isFragmentUIActive()) {
                int intCount = 0;

                for (int nPrn : mPrns) {
                    String CardUSE = "SMALL";

                    if (nPrn != 0) {

                        Satellite satellite = new Satellite(CardUSE, nPrn, mConstellationType[intCount], mSnrCn0s[intCount], mSvElevations[intCount], mSvAzimuths[intCount], mHasEphemeris[intCount], mHasAlmanac[intCount], mUsedInFix[intCount]);

                        if(list.size()==0){
                            hash.put(nPrn,satellite);

                            ArrayList<Satellite> convertedList = new ArrayList<Satellite>(hash.values());
                            list = convertedList;
                            adapter.swapDataSet(list);

                        }else{
                            hash.put(nPrn,satellite);

                            ArrayList<Satellite> convertedList = new ArrayList<Satellite>(hash.values());
                            list = convertedList;
                            adapter.swapDataSet(list);
                        }

                        intCount++;
                    }
                }

                Log.e(TAG, "Complete: Building Recycler View");
            }

        }
    }

    @Deprecated
    private void updateLegacyStatus(GpsStatus status) {
        setStarted(true);

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

        if(useRv){

        }else {
            mAdapter.notifyDataSetChanged();
        }
    }


    private int getListPos(int Value) {
        return list.indexOf(Value);
    }

    private class SvGridAdapter extends BaseAdapter{

        private Context mContext;

        public SvGridAdapter(Context c) {
            mContext = c;

        }

        @Override
        public int getCount() {
            return (mSvCount + 1) * COLUMN_COUNT;
        }

        @Override
        public Object getItem(int position) {
            Log.d(TAG, "getItem(" + position + ")");
            return "foo";
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            TextView textView = null;
            ImageView imageView = null;

            int row = position / COLUMN_COUNT;
            int column = position % COLUMN_COUNT;

            if (convertView != null) {
                if (convertView instanceof ImageView) {
                    imageView = (ImageView) convertView;
                } else if (convertView instanceof TextView) {
                    textView = (TextView) convertView;
                }
            }

            CharSequence text = null;

            if (row == 0) {
                switch (column) {
                    case PRN_COLUMN:
                        text = mRes.getString(R.string.gps_status_prn_header);
                        break;
                    case FLAG_IMAGE_COLUMN:
                        text = mRes.getString(R.string.gps_status_constellation_header);
                        break;
                    case SNR_COLUMN:
                        text = mSnrCn0Title;
                        break;
                    case ELEVATION_COLUMN:
                        text = mRes.getString(R.string.gps_status_elevation_header);
                        break;
                    case AZIMUTH_COLUMN:
                        text = mRes.getString(R.string.gps_status_azimuth_header);
                        break;
                    case FLAGS_COLUMN:
                        text = mRes.getString(R.string.gps_status_flags_header);
                        break;
                }
            } else {
                row--;
                switch (column) {
                    case PRN_COLUMN:
                        text = Integer.toString(mPrns[row]);
                        break;
                    case FLAG_IMAGE_COLUMN:
                        if (imageView == null) {
                            imageView = new ImageView(mContext);
                            imageView.setScaleType(ImageView.ScaleType.FIT_START);
                        }
                        GnssType type;
                        if (GpsHelper.isGnssStatusListenerSupported()) {
                            type = GpsHelper.getGnssConstellationType(mConstellationType[row]);
                        } else {
                            type = GpsHelper.getGnssType(mPrns[row]);
                        }
                        switch (type) {
                            case NAVSTAR:
                                imageView.setImageDrawable(mFlagUsa);
                                break;
                            case GLONASS:
                                imageView.setImageDrawable(mFlagRussia);
                                break;
                            case QZSS:
                                imageView.setImageDrawable(mFlagJapan);
                                break;
                            case BEIDOU:
                                imageView.setImageDrawable(mFlagChina);
                                break;
                            case GALILEO:
                                imageView.setImageDrawable(mFlagGalileo);
                                break;
                        }
                        return imageView;
                    case SNR_COLUMN:
                        if (mSnrCn0s[row] != 0.0f) {
                            text = Float.toString(mSnrCn0s[row]);
                        } else {
                            text = "";
                        }
                        break;
                    case ELEVATION_COLUMN:
                        if (mSvElevations[row] != 0.0f) {
                            text = getString(R.string.gps_status_elevation_value,
                                    Float.toString(mSvElevations[row]));
                        } else {
                            text = "";
                        }
                        break;
                    case AZIMUTH_COLUMN:
                        if (mSvAzimuths[row] != 0.0f) {
                            text = getString(R.string.gps_status_azimuth_value,
                                    Float.toString(mSvAzimuths[row]));
                        } else {
                            text = "";
                        }
                        break;
                    case FLAGS_COLUMN:
                        char[] flags = new char[3];
                        flags[0] = !mHasEphemeris[row] ? ' ' : 'E';
                        flags[1] = !mHasAlmanac[row] ? ' ' : 'A';
                        flags[2] = !mUsedInFix[row] ? ' ' : 'U';
                        text = new String(flags);
                        break;
                }
            }

            if (textView == null) {
                textView = new TextView(mContext);
            }

            textView.setText(text);
            return textView;


        }
    }


}