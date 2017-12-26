package com.survlogic.survlogic.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.survlogic.survlogic.R;
import com.survlogic.survlogic.interf.MapcheckListener;
import com.survlogic.survlogic.model.PointMapCheck;
import com.survlogic.survlogic.utils.AnimateHelper;
import com.survlogic.survlogic.utils.MathHelper;
import com.survlogic.survlogic.view.Card_View_Holder_Job_Mapcheck_Add;
import com.survlogic.survlogic.view.Card_View_Holder_Job_Mapcheck_List;

import java.text.DecimalFormat;
import java.util.ArrayList;

/**
 * Created by chrisfillmore on 6/30/2017.
 */

public class JobMapCheckObservationsAdaptor extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final String TAG = "JobMapCheckObservations";

    private MapcheckListener mapcheckListener;
    private ArrayList<PointMapCheck> pointMapChecks = new ArrayList<>();
    private final int LIST = 0, ADD = 1;

    private int lastPosition = 0;

    private Context mContext;

    private static DecimalFormat COORDINATE_FORMATTER, DISTANCE_PRECISION_FORMATTER;

//    CONSTRUCTOR!!!!!
    public JobMapCheckObservationsAdaptor(Context context, ArrayList<PointMapCheck> pointMapChecks, DecimalFormat COORDINATE_FORMATTER, MapcheckListener mapcheckListener){
        this.pointMapChecks = pointMapChecks;
        this.mContext = context;
        this.mapcheckListener = mapcheckListener;

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
                View v1 = mInflater.inflate(R.layout.card_job_cogo_mapcheck_view_small,parent,false);
                viewHolder = new Card_View_Holder_Job_Mapcheck_List(v1,mContext);
                break;

            case ADD:
                View v2 = mInflater.inflate(R.layout.card_job_cogo_mapcheck_add,parent,false);
                viewHolder = new Card_View_Holder_Job_Mapcheck_Add(v2, mContext, mapcheckListener);
                break;

            default:
                View v = mInflater.inflate(R.layout.card_job_cogo_mapcheck_view_small,parent,false);
                viewHolder = new Card_View_Holder_Job_Mapcheck_List(v,mContext);
                break;
        }

        return viewHolder;

    }

    @Override
    public int getItemViewType(int position) {
        Log.d(TAG, "getItemViewType: Started");
        PointMapCheck pointMapCheck = pointMapChecks.get(position);
        
        int observationType = pointMapCheck.getObservationType();
        Log.d(TAG, "getItemViewType: observationType: " + observationType);
        if(observationType == 99){
            Log.d(TAG, "getItemViewType: Returning ADD");
            return ADD;
        }else{
            Log.d(TAG, "getItemViewType: Returning LIST");
            return LIST;
        }
    
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        Log.d(TAG, "onBindViewHolder: Started...");
        Log.d(TAG, "onBindViewHolder: ItemViewType: " + holder.getItemViewType());
        switch (holder.getItemViewType()){

            case LIST:
                Card_View_Holder_Job_Mapcheck_List vh1 = (Card_View_Holder_Job_Mapcheck_List) holder;
                configureViewHolderList(vh1,position);
                break;

           case ADD:
               Card_View_Holder_Job_Mapcheck_Add vh2 = (Card_View_Holder_Job_Mapcheck_Add) holder;
               configureViewHolderAdd(vh2,position);

        }

    }

    @Override
    public int getItemCount() {
        return pointMapChecks.size();
    }

    private void configureViewHolderList(final Card_View_Holder_Job_Mapcheck_List vh1, int position) {

        final PointMapCheck pointMapCheck = pointMapChecks.get(position);
        int recordPosition = position + 1;

        final String stepNo = String.valueOf(recordPosition);
        final String observation;

        vh1.tvStepNo.setText(stepNo);

        switch(pointMapCheck.getObservationType()){
            case 0:  //Bearing

                String bearing = MathHelper.convertDECtoDMSBearing(pointMapCheck.getLineAngle(),0);
                String distance = COORDINATE_FORMATTER.format(pointMapCheck.getLineDistance());

                observation = String.valueOf(bearing + " " + distance);

                vh1.ivObservation_Type.setImageDrawable(mContext.getResources().getDrawable(R.drawable.vd_mapcheck_line));

                break;

            default:
                observation = "";
                break;

        }

        vh1.tvPointObservation.setText(observation);

        final int myPosition = position;

        vh1.ibOptions_Menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popupMenuObsType = new PopupMenu(mContext, vh1.ibOptions_Menu);
                popupMenuObsType.inflate(R.menu.popup_cogo_mapcheck_list_options);

                popupMenuObsType.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {

                        switch (item.getItemId()){
                            case R.id.mapcheck_item_1:
                                showToast("Edit Position " + myPosition, true);
                                break;

                            case R.id.mapcheck_item_2:
                                showToast("Delete Position " + myPosition, true);
                                break;

                        }

                        return true;
                    }
                });

                popupMenuObsType.show();
            }
        });

    }
    private void configureViewHolderAdd(final Card_View_Holder_Job_Mapcheck_Add vh2, final int position) {
        Log.d(TAG, "configureViewHolderAdd: Started...");

        vh2.btSaveObservation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                vh2.saveObservation(position,true);
            }
        });

       vh2.etMapCheckPointNumber.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if(actionId == EditorInfo.IME_ACTION_DONE){
                    vh2.saveObservation(position,false);

                }

                return false;
            }
        });

        vh2.btCancelObservation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                vh2.cancelObservation(position);
            }
        });

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

    //----------------------------------------------------------------------------------------------//
    private void showToast(String data, boolean shortTime) {

        if (shortTime) {
            Toast.makeText(mContext, data, Toast.LENGTH_SHORT).show();

        } else{
            Toast.makeText(mContext, data, Toast.LENGTH_LONG).show();

        }
    }




}
