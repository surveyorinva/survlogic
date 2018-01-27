package com.survlogic.survlogic.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.survlogic.survlogic.R;
import com.survlogic.survlogic.interf.JobPointsActivityListener;
import com.survlogic.survlogic.model.PointSurvey;
import com.survlogic.survlogic.utils.AnimateHelper;
import com.survlogic.survlogic.view.Card_View_Holder_Job_Points_List;

import java.text.DecimalFormat;
import java.util.ArrayList;

/**
 * Created by chrisfillmore on 6/30/2017.
 */

public class PointSurveyTableDataAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String TAG = "PointSurveyTableDataAda";

    private Context mContext;
    private String jobDatabaseName;

    private JobPointsActivityListener jobPointsActivityListener;
    private ArrayList<PointSurvey> pointSurveys = new ArrayList<>();

    private final int LIST = 0;
    private int lastPosition = 0;

    private DecimalFormat COORDINATE_FORMATTER, DISTANCE_PRECISION_FORMATTER;


    //    CONSTRUCTOR!!!!!
    public PointSurveyTableDataAdapter(Context context, ArrayList<PointSurvey> pointSurvey, DecimalFormat COORDINATE_FORMATTER, JobPointsActivityListener jobPointsActivityListener){
        this.pointSurveys = pointSurvey;
        this.mContext = context;
        this.COORDINATE_FORMATTER = COORDINATE_FORMATTER;
        this.jobPointsActivityListener = jobPointsActivityListener;
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
        return pointSurveys == null ? 0 : pointSurveys.size();
    }

    private void configureViewHolderList(final Card_View_Holder_Job_Points_List vh1, int position) {
        final PointSurvey pointSurvey = pointSurveys.get(position);
        String pointNumber, pointNorthing, pointEasting, pointElevation, pointDescription;
        String northingPrefix, eastingPrefix, elevationPrefix;

        pointNumber = String.valueOf(pointSurvey.getPoint_no());
        Log.d(TAG, "configureViewHolderList: Point No: " + pointNumber + ": at position " + position);
        pointDescription = pointSurvey.getDescription();

        if(pointSurvey.getNorthing() != 0){
            pointNorthing = COORDINATE_FORMATTER.format(pointSurvey.getNorthing());
        }else{
            pointNorthing = COORDINATE_FORMATTER.format(0d);

        }
        if(pointSurvey.getEasting() != 0){
            pointEasting = COORDINATE_FORMATTER.format(pointSurvey.getEasting());
        }else{
            pointEasting = COORDINATE_FORMATTER.format(0d);

        }

        if(pointSurvey.getElevation() != 0){
            pointElevation = COORDINATE_FORMATTER.format(pointSurvey.getElevation());
        }else{
            pointElevation = COORDINATE_FORMATTER.format(0d);

        }

        vh1.tvPointNumber.setText(pointNumber);
        vh1.tvPointDesc.setText(pointDescription);
        vh1.tvPointNorthing.setText(pointNorthing);
        vh1.tvPointEasting.setText(pointEasting);
        vh1.tvPointElevation.setText(pointElevation);

        vh1.mCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                long point_id = pointSurvey.getId();
                int pointNo = pointSurvey.getPoint_no();

                jobPointsActivityListener.callPointViewDialogBox(point_id,pointNo);

            }
        });

    }

    //----------------------------------------------------------------------------------------------//
    public void insert(int position, PointSurvey data){
        pointSurveys.add(position,data);
        notifyItemInserted(position);
    }

    public void remove (PointSurvey data){
        int position = pointSurveys.indexOf(data);
        pointSurveys.remove(position);
        notifyItemRemoved(position);
    }

    public void swapDataSet (ArrayList<PointSurvey> newData){
        this.pointSurveys = newData;

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
