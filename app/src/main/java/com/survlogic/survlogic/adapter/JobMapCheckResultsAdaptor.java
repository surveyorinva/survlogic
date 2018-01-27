package com.survlogic.survlogic.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.survlogic.survlogic.R;
import com.survlogic.survlogic.model.PointMapCheck;
import com.survlogic.survlogic.utils.AnimateHelper;
import com.survlogic.survlogic.view.Card_View_Holder_Job_Mapcheck_Results;

import java.text.DecimalFormat;
import java.util.ArrayList;

/**
 * Created by chrisfillmore on 6/30/2017.
 */

public class JobMapCheckResultsAdaptor extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final String TAG = "JobMapCheckResultsAdapt";

    private ArrayList<PointMapCheck> pointMapChecks = new ArrayList<>();
    private final int LIST = 0;

    private int lastPosition = 0;

    private Context mContext;
    private String jobDatabaseName;

    private DecimalFormat COORDINATE_FORMATTER, DISTANCE_PRECISION_FORMATTER;

    private ItemTouchHelper touchHelper;

//    CONSTRUCTOR!!!!!
    public JobMapCheckResultsAdaptor(Context context, ArrayList<PointMapCheck> pointMapChecks, DecimalFormat COORDINATE_FORMATTER){
        this.pointMapChecks = pointMapChecks;
        this.mContext = context;
        this.COORDINATE_FORMATTER = COORDINATE_FORMATTER;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Log.d(TAG, "onCreateViewHolder: Started...");
        Log.d(TAG, "onCreateViewHolder: ViewType: " + viewType);
        RecyclerView.ViewHolder viewHolder;
        LayoutInflater mInflater = LayoutInflater.from(parent.getContext());

        switch (viewType){
            case LIST:
                View v1 = mInflater.inflate(R.layout.card_job_cogo_mapcheck_results_small,parent,false);
                viewHolder = new Card_View_Holder_Job_Mapcheck_Results(v1,mContext);
                break;

            default:
                View v = mInflater.inflate(R.layout.card_job_cogo_mapcheck_results_small,parent,false);
                viewHolder = new Card_View_Holder_Job_Mapcheck_Results(v,mContext);
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
                Card_View_Holder_Job_Mapcheck_Results vh1 = (Card_View_Holder_Job_Mapcheck_Results) holder;
                configureViewHolderList(vh1,position);
                break;

        }

    }

    @Override
    public int getItemCount() {
        return pointMapChecks == null ? 0 : pointMapChecks.size();
    }

    private void configureViewHolderList(final Card_View_Holder_Job_Mapcheck_Results vh1, int position) {

        final PointMapCheck pointMapCheck = pointMapChecks.get(position);
        String pointNumber, pointNorthing, pointEasting, pointDescription;
        String northingPrefix, eastingPrefix;

        pointNumber = String.valueOf(pointMapCheck.getToPointNo());
        Log.d(TAG, "configureViewHolderList: Point No: " + pointNumber + ": at position " + position);
        pointDescription = pointMapCheck.getPointDescription();

        northingPrefix = mContext.getResources().getString(R.string.cogo_mapcheck_observation_northing_prefix);
        eastingPrefix = mContext.getResources().getString(R.string.cogo_mapcheck_observation_easting_prefix);

        if(pointMapCheck.getToPointNorth() != 0){
            pointNorthing = COORDINATE_FORMATTER.format(pointMapCheck.getToPointNorth());
        }else{
            pointNorthing = String.valueOf(0d);

        }
        if(pointMapCheck.getToPointEast() != 0){
            pointEasting = COORDINATE_FORMATTER.format(pointMapCheck.getToPointEast());
        }else{
            pointEasting = String.valueOf(0d);

        }

        pointNorthing = northingPrefix + " " + pointNorthing;
        pointEasting = eastingPrefix + " " + pointEasting;

        vh1.tvPointNumber.setText(pointNumber);
        vh1.tvPointDesc.setText(pointDescription);
        vh1.tvPointNorthing.setText(pointNorthing);
        vh1.tvPointEasting.setText(pointEasting);

    }

    //----------------------------------------------------------------------------------------------//
    public void insert(int position, PointMapCheck data){
        pointMapChecks.add(position,data);
        notifyItemInserted(position);
    }

    public void remove (PointMapCheck data){
        int position = pointMapChecks.indexOf(data);
        pointMapChecks.remove(position);
        notifyItemRemoved(position);
    }

    public void swapDataSet (ArrayList<PointMapCheck> newData){
        this.pointMapChecks = newData;

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

    public void setTouchHelper(ItemTouchHelper touchHelper){
        this.touchHelper = touchHelper;
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
