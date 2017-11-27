package com.survlogic.survlogic.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.survlogic.survlogic.R;
import com.survlogic.survlogic.dialog.DialogJobSideshotPointList;
import com.survlogic.survlogic.model.PointSurvey;
import com.survlogic.survlogic.utils.AnimateHelper;
import com.survlogic.survlogic.view.Card_View_Holder_Job_PointSurvey_Small;

import java.util.ArrayList;

/**
 * Created by chrisfillmore on 6/30/2017.
 */

public class JobSideshotPointListAdaptor extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final String TAG = "JobSetupPointListAdapto";

    private DialogJobSideshotPointList dialogJobSideshotPointList;
    private ArrayList<PointSurvey> pointSurveys = new ArrayList<>();
    private final int SMALL = 0;

    private int lastPosition = 0;

    private Context mContext;
    private boolean isOccupyPoint;


//    CONSTRUCTOR!!!!!
    public JobSideshotPointListAdaptor(Context context, ArrayList<PointSurvey> pointSurveys, DialogJobSideshotPointList dialogJobSideshotPointList){
        this.pointSurveys = pointSurveys;

        mContext = context;
        this.isOccupyPoint = isOccupyPoint;
        this.dialogJobSideshotPointList = dialogJobSideshotPointList;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder viewHolder;
        LayoutInflater mInflater = LayoutInflater.from(parent.getContext());

        switch (viewType){
            case SMALL:
                View v1 = mInflater.inflate(R.layout.card_job_point_view_small,parent,false);
                viewHolder = new Card_View_Holder_Job_PointSurvey_Small(v1);
                break;

            default:
                View v = mInflater.inflate(R.layout.card_job_point_view_small,parent,false);
                viewHolder = new Card_View_Holder_Job_PointSurvey_Small(v);
                break;
        }
        return viewHolder;
    }

    @Override
    public int getItemViewType(int position) {
        return SMALL;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
       switch (holder.getItemViewType()){

            case SMALL:
                Card_View_Holder_Job_PointSurvey_Small vh1 = (Card_View_Holder_Job_PointSurvey_Small) holder;
                configureViewHolderSmall(vh1,position);

        }

    }

    @Override
    public int getItemCount() {
        return pointSurveys.size();
    }

    private void configureViewHolderSmall(final Card_View_Holder_Job_PointSurvey_Small vh1, int position) {

        final PointSurvey pointSurvey = pointSurveys.get(position);

        final String pointNo = String.valueOf(pointSurvey.getPoint_no());
        final String pointDesc = pointSurvey.getDescription();
        final String pointNorth = String.valueOf(pointSurvey.getNorthing());
        final String pointEast = String.valueOf(pointSurvey.getEasting());
        final String pointElev = String.valueOf(pointSurvey.getElevation());

        vh1.tvPointNo.setText(pointNo);
        vh1.tvPointDesc.setText(pointDesc);
        vh1.tvPointNorth.setText(pointNorth);
        vh1.tvPointEast.setText(pointEast);
        vh1.tvPointHeight.setText(pointElev);
    }



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





}
