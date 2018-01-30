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
import com.survlogic.survlogic.dialog.DialogProjectZoneList;
import com.survlogic.survlogic.interf.ProjectProjectionListener;
import com.survlogic.survlogic.utils.AnimateHelper;
import com.survlogic.survlogic.view.Card_View_Holder_Project_Projections_List;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by chrisfillmore on 6/30/2017.
 */

public class ProjectZonesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String TAG = "ProjectZonesAdapter";

    private Context mContext;

    private DialogProjectZoneList dialogProjectZoneList;
    private ProjectProjectionListener projectProjectionListener;
    private String[] lstAvailableZones;
    private ArrayList<String> lstZones;
    private boolean isSPCS = false;

    private final int LIST = 0;
    private int lastPosition = 0;

    //    CONSTRUCTOR!!!!!
    public ProjectZonesAdapter(Context context, DialogProjectZoneList dialogProjectZoneList, ProjectProjectionListener projectProjectionListener, String[] lstAvailableZones, boolean isSPCS){
        this.lstAvailableZones = lstAvailableZones;
        this.mContext = context;
        this.dialogProjectZoneList = dialogProjectZoneList;
        this.projectProjectionListener = projectProjectionListener;

        lstZones = new ArrayList<>(Arrays.asList(lstAvailableZones));


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
        return lstZones == null ? 0 : lstZones.size();
    }

    private void configureViewHolderList(final Card_View_Holder_Project_Projections_List vh1, int position) {

        final String stringZone = lstZones.get(position);
        String[] separatedZoneValue = stringZone.split(",");

//        <!- ZONE**** Name of Zone, Zone No, Required GeodeticCRS,this EPSG -->

        final String zoneName = separatedZoneValue[0];
        String zoneID = separatedZoneValue[1];

        vh1.tvProjectionName.setText(zoneName);
        vh1.tvProjectionCountry.setText(zoneID);

        vh1.mCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                projectProjectionListener.onReturnValueZone(stringZone,zoneName);
                dialogProjectZoneList.dismiss();
            }
        });

    }

    //----------------------------------------------------------------------------------------------//
    public void insert(int position, String data){
        lstZones.add(position,data);
        notifyItemInserted(position);
    }

    public void remove (String data){
        int position = lstZones.indexOf(data);
        lstZones.remove(position);
        notifyItemRemoved(position);
    }

    public void swapDataSet (ArrayList<String> newData){
        this.lstZones = newData;

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
