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
import com.survlogic.survlogic.model.PointGeodetic;
import com.survlogic.survlogic.utils.AnimateHelper;
import com.survlogic.survlogic.utils.SurveyMathHelper;
import com.survlogic.survlogic.view.Card_View_Holder_Job_Points_List;

import java.text.DecimalFormat;
import java.util.ArrayList;

/**
 * Created by chrisfillmore on 6/30/2017.
 */

public class PointGeodeticTableDataAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String TAG = "PointSurveyTableDataAda";

    private ArrayList<PointGeodetic> pointGeodetics = new ArrayList<>();
    private final int LIST = 0;

    private int lastPosition = 0;

    private Context mContext;
    private String jobDatabaseName;

    private DecimalFormat COORDINATE_FORMATTER, DISTANCE_PRECISION_FORMATTER;

    private ItemTouchHelper touchHelper;

    //    CONSTRUCTOR!!!!!
    public PointGeodeticTableDataAdapter(Context context, ArrayList<PointGeodetic> pointGeodetics, DecimalFormat COORDINATE_FORMATTER){
        this.pointGeodetics = pointGeodetics;
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
                View v1 = mInflater.inflate(R.layout.card_job_cogo_point_view_small,parent,false);
                viewHolder = new Card_View_Holder_Job_Points_List(v1,mContext);
                break;

            default:
                View v = mInflater.inflate(R.layout.card_job_cogo_point_view_small,parent,false);
                viewHolder = new Card_View_Holder_Job_Points_List(v,mContext);
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
                Card_View_Holder_Job_Points_List vh1 = (Card_View_Holder_Job_Points_List) holder;
                configureViewHolderList(vh1,position);
                break;

        }

    }

    @Override
    public int getItemCount() {
        return pointGeodetics == null ? 0 : pointGeodetics.size();
    }

    private void configureViewHolderList(final Card_View_Holder_Job_Points_List vh1, int position) {

        final PointGeodetic pointGeodetic = pointGeodetics.get(position);
        String pointNumber, pointLatitude, pointLongitude, pointElevation, pointDescription;
        String latPrefix, longPrefix, elevationPrefix;

        pointNumber = String.valueOf(pointGeodetic.getPoint_no());
        Log.d(TAG, "configureViewHolderList: Point No: " + pointNumber + ": at position " + position);
        pointDescription = pointGeodetic.getDescription();

        latPrefix = mContext.getResources().getString(R.string.cogo_mapcheck_observation_latitude_prefix);
        longPrefix = mContext.getResources().getString(R.string.cogo_mapcheck_observation_longitude_prefix);
        elevationPrefix =  mContext.getResources().getString(R.string.cogo_mapcheck_observation_easting_prefix);

        if(pointGeodetic.getNorthing() != 0){
            pointLatitude = SurveyMathHelper.convertDECtoDMSGeodetic(pointGeodetic.getLatitude(),0,true);
        }else{
            pointLatitude = COORDINATE_FORMATTER.format(0d);

        }
        if(pointGeodetic.getEasting() != 0){
            pointLongitude = SurveyMathHelper.convertDECtoDMSGeodetic(pointGeodetic.getLongitude(),0,true);
        }else{
            pointLongitude = COORDINATE_FORMATTER.format(0d);

        }

        if(pointGeodetic.getElevation() != 0){
            pointElevation = COORDINATE_FORMATTER.format(pointGeodetic.getElevation());
        }else{
            pointElevation = COORDINATE_FORMATTER.format(0d);

        }

//        pointLatitude = latPrefix + " " + pointLatitude;
//        pointLongitude = longPrefix + " " + pointLongitude;
//        pointElevation = elevationPrefix + " " + pointElevation;

        vh1.tvPointNumber.setText(pointNumber);
        vh1.tvPointDesc.setText(pointDescription);
        vh1.tvPointNorthing.setText(pointLatitude);
        vh1.tvPointEasting.setText(pointLongitude);
        vh1.tvPointElevation.setText(pointElevation);

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
    private void showToast(String data, boolean shortTime) {

        if (shortTime) {
            Toast.makeText(mContext, data, Toast.LENGTH_SHORT).show();

        } else{
            Toast.makeText(mContext, data, Toast.LENGTH_LONG).show();

        }
    }



}
