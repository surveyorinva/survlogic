package com.survlogic.survlogic.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.survlogic.survlogic.R;
import com.survlogic.survlogic.dialog.DialogProjectProjectionsList;
import com.survlogic.survlogic.interf.JobPointsActivityListener;
import com.survlogic.survlogic.interf.ProjectProjectionListener;
import com.survlogic.survlogic.model.PointSurvey;
import com.survlogic.survlogic.utils.AnimateHelper;
import com.survlogic.survlogic.view.Card_View_Holder_Job_Points_List;
import com.survlogic.survlogic.view.Card_View_Holder_Project_Projections_List;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by chrisfillmore on 6/30/2017.
 */

public class ProjectProjectionsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String TAG = "ProjectProjectionsAdapt";

    private Context mContext;

    private DialogProjectProjectionsList dialogProjectProjectionsList;
    private ProjectProjectionListener projectProjectionListener;
    private String[] lstAvailableProjections;
    private ArrayList<String> lstProjections;

    private final int LIST = 0;
    private int lastPosition = 0;

    //    CONSTRUCTOR!!!!!
    public ProjectProjectionsAdapter(Context context, DialogProjectProjectionsList dialogProjectProjectionsList, ProjectProjectionListener projectProjectionListener, String[] lstAvailableProjections){
        this.lstAvailableProjections = lstAvailableProjections;
        this.mContext = context;
        this.dialogProjectProjectionsList = dialogProjectProjectionsList;
        this.projectProjectionListener = projectProjectionListener;

        lstProjections = new ArrayList<>(Arrays.asList(lstAvailableProjections));


    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Log.d(TAG, "onCreateViewHolder: Started...");
        Log.d(TAG, "onCreateViewHolder: ViewType: " + viewType);
        RecyclerView.ViewHolder viewHolder;
        LayoutInflater mInflater = LayoutInflater.from(parent.getContext());

        switch (viewType){
            case LIST:
                View v1 = mInflater.inflate(R.layout.card_project_projection_view_small,parent,false);
                viewHolder = new Card_View_Holder_Project_Projections_List(v1,mContext);
                break;

            default:
                View v = mInflater.inflate(R.layout.card_project_projection_view_small,parent,false);
                viewHolder = new Card_View_Holder_Project_Projections_List(v,mContext);
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
                Card_View_Holder_Project_Projections_List vh1 = (Card_View_Holder_Project_Projections_List) holder;
                configureViewHolderList(vh1,position);
                break;

        }

    }

    @Override
    public int getItemCount() {
        return lstProjections == null ? 0 : lstProjections.size();
    }

    private void configureViewHolderList(final Card_View_Holder_Project_Projections_List vh1, int position) {

        final String stringProjection = lstProjections.get(position);
        String[] separatedProjectionValue = stringProjection.split(",");

//        State Plane Coordinate System of 1983,USA,EPSG:4269,true,SPCS,2,EPSG:0

        final String projectionName = separatedProjectionValue[0];
        String projectionCountry = separatedProjectionValue[1];

        vh1.tvProjectionName.setText(projectionName);
        vh1.tvProjectionCountry.setText(projectionCountry);

        vh1.mCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                projectProjectionListener.onReturnValueProjection(stringProjection,projectionName);
                dialogProjectProjectionsList.dismiss();
            }
        });

    }

    //----------------------------------------------------------------------------------------------//
    public void insert(int position, String data){
        lstProjections.add(position,data);
        notifyItemInserted(position);
    }

    public void remove (String data){
        int position = lstProjections.indexOf(data);
        lstProjections.remove(position);
        notifyItemRemoved(position);
    }

    public void swapDataSet (ArrayList<String> newData){
        this.lstProjections = newData;

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
