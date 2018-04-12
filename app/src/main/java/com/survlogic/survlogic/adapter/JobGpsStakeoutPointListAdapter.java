package com.survlogic.survlogic.adapter;

import android.content.Context;
import android.location.Location;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.survlogic.survlogic.ARvS.model.ArvSLocationPOI;
import com.survlogic.survlogic.R;
import com.survlogic.survlogic.model.PointGeodetic;
import com.survlogic.survlogic.utils.AnimateHelper;
import com.survlogic.survlogic.utils.StringUtilityHelper;
import com.survlogic.survlogic.utils.SurveyMathHelper;
import com.survlogic.survlogic.view.Card_View_Holder_Job_Gps_Stakeout_Point_List;
import com.survlogic.survlogic.view.Card_View_Holder_Job_Points_List;

import java.text.DecimalFormat;
import java.util.ArrayList;

/**
 * Created by chrisfillmore on 6/30/2017.
 */

public class JobGpsStakeoutPointListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String TAG = "JobGpsStakeoutPointList";

    private ArrayList<PointGeodetic> pointGeodetics = new ArrayList<>();
    private Location mCurrentLocation;

    private final int LIST = 0;

    private int lastPosition = 0;

    private Context mContext;

    //    CONSTRUCTOR!!!!!
    public JobGpsStakeoutPointListAdapter(Context context, ArrayList<PointGeodetic> pointGeodetics){
        this.pointGeodetics = pointGeodetics;
        this.mContext = context;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Log.d(TAG, "onCreateViewHolder: Started...");
        Log.d(TAG, "onCreateViewHolder: ViewType: " + viewType);
        RecyclerView.ViewHolder viewHolder;
        LayoutInflater mInflater = LayoutInflater.from(parent.getContext());

        switch (viewType){
            case LIST:
                View v1 = mInflater.inflate(R.layout.card_job_gps_point_ar_small,parent,false);
                viewHolder = new Card_View_Holder_Job_Gps_Stakeout_Point_List(v1,mContext);
                break;

            default:
                View v = mInflater.inflate(R.layout.card_job_gps_point_ar_small,parent,false);
                viewHolder = new Card_View_Holder_Job_Gps_Stakeout_Point_List(v,mContext);
                break;
        }

        return viewHolder;

    }

    @Override
    public int getItemViewType(int position) {
        Log.d(TAG, "getItemViewType: Started");
        return LIST;

    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        Log.d(TAG, "onBindViewHolder: Started...");
        Log.d(TAG, "onBindViewHolder: ItemViewType: " + holder.getItemViewType());
        switch (holder.getItemViewType()){

            case LIST:
                Card_View_Holder_Job_Gps_Stakeout_Point_List vh1 = (Card_View_Holder_Job_Gps_Stakeout_Point_List) holder;
                configureViewHolderList(vh1,position);
                break;

        }

    }

    @Override
    public int getItemCount() {
        return pointGeodetics == null ? 0 : pointGeodetics.size();
    }

    private void configureViewHolderList(final Card_View_Holder_Job_Gps_Stakeout_Point_List vh1, int position) {

        final PointGeodetic pointGeodetic = pointGeodetics.get(position);
        String pointNumber, pointDescription;

        pointNumber = String.valueOf(pointGeodetic.getPoint_no());
        Log.d(TAG, "configureViewHolderList: Point No: " + pointNumber + ": at position " + position);
        pointDescription = pointGeodetic.getDescription();

        Location target = convertPOIToLocation(pointGeodetic.getDescription(), pointGeodetic.getLatitude(),pointGeodetic.getLongitude(),pointGeodetic.getElevation());
        DecimalFormat df = StringUtilityHelper.createUSNonBiasDecimalFormatSelect(2);

        vh1.tvPointNumber.setText(pointNumber);
        vh1.tvPointDesc.setText(pointDescription);

        if(mCurrentLocation !=null){
            vh1.tvDistanceToTarget.setText(df.format(distanceToTarget(target, mCurrentLocation)));
        }


        vh1.mCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showToast("Geodetic",true);
            }
        });

    }

    //----------------------------------------------------------------------------------------------//
    public void insert(int position, PointGeodetic data){
        pointGeodetics.add(position,data);
        notifyItemInserted(position);
    }

    public void remove (PointGeodetic data){
        int position = pointGeodetics.indexOf(data);
        pointGeodetics.remove(position);
        notifyItemRemoved(position);
    }

    public void swapDataSet (ArrayList<PointGeodetic> newData){
        this.pointGeodetics = newData;

        notifyDataSetChanged();
    }


    private void setAnimationbyHelper(RecyclerView.ViewHolder holder, int position){

        if(position > lastPosition){ // We are scrolling DOWN
            AnimateHelper.animateRecyclerView(holder, true);

        }else{ // We are scrolling UP
            AnimateHelper.animateRecyclerView(holder, false);

        }

        lastPosition = position;
    }

    //----------------------------------------------------------------------------------------------//
    private Location convertPOIToLocation(String name, double lat, double lon, double alt){
        Log.d(TAG, "convertPOIToLocation: Started");

        Location targetLocation = new Location(name);
        targetLocation.setLatitude(lat);
        targetLocation.setLongitude(lon);
        targetLocation.setAltitude(alt);

        return targetLocation;

    }



    private float distanceToTarget(Location target, Location current){
        Log.d(TAG, "distanceToTarget: Started");

        float distanceMetric = current.distanceTo(target);

        Log.d(TAG, "distanceToTarget: " + distanceMetric);
        return distanceMetric;

    }

    private float bearingToTarget(Location target, Location current){
        Log.d(TAG, "bearingToTarget: Started");

        float bearing = current.bearingTo(target);

        Log.d(TAG, "bearingToTarget: " + bearing);
        return bearing;

    }

    public void setCurrentLocation(Location location){
        this.mCurrentLocation = location;
    }

    //----------------------------------------------------------------------------------------------//
    private void showToast(String data, boolean shortTime) {

        if (shortTime) {
            Toast.makeText(mContext, data, Toast.LENGTH_SHORT).show();

        } else{
            Toast.makeText(mContext, data, Toast.LENGTH_LONG).show();

        }
    }



}
