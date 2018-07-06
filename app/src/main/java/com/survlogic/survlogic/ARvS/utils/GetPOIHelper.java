package com.survlogic.survlogic.ARvS.utils;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.Context;
import android.location.Location;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import com.survlogic.survlogic.ARvS.adapter.JobGPSSurveyGeodeticPointsAdapter;
import com.survlogic.survlogic.ARvS.background.BackgroundGeodeticPointGetV2;
import com.survlogic.survlogic.ARvS.interf.GeodeticPointsGetter;
import com.survlogic.survlogic.ARvS.interf.POIHelperListener;
import com.survlogic.survlogic.ARvS.interf.SetActiveTargetPOIGeodetic;
import com.survlogic.survlogic.dialog.DialogJobPointView;
import com.survlogic.survlogic.model.PointGeodetic;

import java.util.ArrayList;

public class GetPOIHelper  implements GeodeticPointsGetter, SetActiveTargetPOIGeodetic{

    private static final String TAG = "GetPOIHelper";
    private Context mContext;
    POIHelperListener listener;

    //----------------------------------------------------------------------------------------------Datasets
    //Geodetic Points from Job
    private ArrayList<PointGeodetic> lstPointsGeodetic = new ArrayList<>();

    //Loading Job Data from Activity
    private String jobDatabaseName;
    private boolean hasCurrentLocation = false;
    private Location mCurrentLocation;

    //Navigation View Controls
    private RecyclerView rvPointList;
    private RecyclerView.LayoutManager layoutManagerPointGeodetic;
    private JobGPSSurveyGeodeticPointsAdapter adapterGeodetic;

    //Method checks
    private boolean isRecyclerSet = false;

    //Method Constants
    private int card_type = TYPE_NONE;
    private static final int TYPE_NONE = 0, TYPE_INTERNAL_JOB = 1, TYPE_EXTERNAL_NGS = 2;

    //Target Location
    PointGeodetic mTargetPointGeodetic;

    public GetPOIHelper(Context context, POIHelperListener listener) {
        this.mContext = context;
        this.listener = listener;
    }

    public void initViewWidgets(RecyclerView rvPointList){
        this.rvPointList = rvPointList;

        isRecyclerSet = true;

    }

    //---------------------------------------------------------------------------------------------- Dataset Builder

    public boolean buildInternalJobData(String jobDatabaseName){
        Log.d(TAG, "buildInternalJobData: Started");

        if(isRecyclerSet){
            card_type = TYPE_INTERNAL_JOB;
            this.jobDatabaseName = jobDatabaseName;
            loadPointGeodeticInBackground();

            return true;
        }else{
            return false;
        }


    }
    public boolean buildExternalNGSData(){
        Log.d(TAG, "buildExternalNGSData: Started");
        card_type = TYPE_EXTERNAL_NGS;

        if(isRecyclerSet){
            return true;
        }else{
            return false;
        }

    }

    //---------------------------------------------------------------------------------------------- Setters/Getters

    public boolean isRecyclerSet() {
        return isRecyclerSet;
    }

    public ArrayList<PointGeodetic> getPointsGeodeticList() {
        return lstPointsGeodetic;
    }

    public Location getCurrentLocation() {
        return mCurrentLocation;
    }

    public void setCurrentLocation(Location currentLocation) {
        this.mCurrentLocation = currentLocation;

        hasCurrentLocation = true;

        if(adapterGeodetic != null){
            adapterGeodetic.setCurrentLocation(mCurrentLocation);
            adapterGeodetic.notifyDataSetChanged();
        }

    }

    public PointGeodetic getTargetLocation() {
        return mTargetPointGeodetic;
    }

    //---------------------------------------------------------------------------------------------- Dataset Methods
    //Job Points

    private void loadPointGeodeticInBackground() {
        Log.d(TAG, "loadPointGeodeticInBackground: Started...");
        BackgroundGeodeticPointGetV2 backgroundGeodeticPointGet = new BackgroundGeodeticPointGetV2(mContext, jobDatabaseName, this);
        backgroundGeodeticPointGet.execute();

    }

    private void setPointGeodeticAdapter() {
        Log.d(TAG, "setPointGeodeticAdapter: Started");
        Log.d(TAG, "setPointGeodeticAdapter: Size: " + lstPointsGeodetic.size());
        Log.d(TAG, "setPointGeodeticAdapter: Card Type: " + card_type);

        layoutManagerPointGeodetic = new LinearLayoutManager(mContext);
        rvPointList.setLayoutManager(layoutManagerPointGeodetic);
        
        rvPointList.setHasFixedSize(false);

        adapterGeodetic = new JobGPSSurveyGeodeticPointsAdapter(mContext, lstPointsGeodetic,card_type,this);

        rvPointList.setAdapter(adapterGeodetic);

        if(hasCurrentLocation){
            adapterGeodetic.setCurrentLocation(mCurrentLocation);
            adapterGeodetic.notifyDataSetChanged();
        }

    }

    //---------------------------------------------------------------------------------------------- Geodetic Points Listener
    @Override
    public void getPointsGeodetic(ArrayList<PointGeodetic> lstPointGeodetics) {
        this.lstPointsGeodetic = lstPointGeodetics;

        setPointGeodeticAdapter();

    }

    //---------------------------------------------------------------------------------------------- Set Active Target Listener

    @Override
    public void setTargetLocation(PointGeodetic pointGeodetic) {
        Log.d(TAG, "setTargetLocation: Target Set");
        //Sets the activeTarget for use by the UI for tracking by long press
        this.mTargetPointGeodetic = pointGeodetic;

        listener.isPOITargetSet(true);

    }

    @Override
    public void callPointViewDialogBox(PointGeodetic pointGeodetic) {
        Log.d(TAG, "callPointViewDialogBox: Calling Point Info Screen");
        //Sets the activeTarget for use by the UI for opening point information screen
        this.mTargetPointGeodetic = pointGeodetic;
        listener.openPointViewDialogBox(true);

    }
}
