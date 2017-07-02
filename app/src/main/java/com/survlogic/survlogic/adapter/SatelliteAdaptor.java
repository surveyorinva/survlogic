package com.survlogic.survlogic.adapter;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.survlogic.survlogic.R;
import com.survlogic.survlogic.model.Satellite;
import com.survlogic.survlogic.utils.GnssType;
import com.survlogic.survlogic.utils.GpsHelper;
import com.survlogic.survlogic.view.Card_View_Holder_Satellite_Small;

import java.util.ArrayList;

/**
 * Created by chrisfillmore on 5/26/2017.
 */

public class SatelliteAdaptor extends RecyclerView.Adapter<RecyclerView.ViewHolder> {


    private ArrayList<Satellite> satellites = new ArrayList<Satellite>();
    private final int SMALL = 0, MEDIUM = 1, LARGE = 2;
    private Context mContext;

    public SatelliteAdaptor(Context context,ArrayList<Satellite> satellites){
        this.satellites = satellites;
        mContext = context;
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        RecyclerView.ViewHolder viewHolder;
        LayoutInflater mInflater = LayoutInflater.from(parent.getContext());

        switch (viewType){
            case SMALL:
                View v1 = mInflater.inflate(R.layout.card_gps_satellite_status_small,parent,false);
                viewHolder = new Card_View_Holder_Satellite_Small(v1);
                break;

            case MEDIUM:
                View v2 = mInflater.inflate(R.layout.card_gps_satellite_status_small,parent,false);
                viewHolder = new Card_View_Holder_Satellite_Small(v2);
                break;

            case LARGE:
                View v3 = mInflater.inflate(R.layout.card_gps_satellite_status_small,parent,false);
                viewHolder = new Card_View_Holder_Satellite_Small(v3);
                break;

            default:
                View v = mInflater.inflate(R.layout.card_gps_satellite_status_small,parent,false);
                viewHolder = new Card_View_Holder_Satellite_Small(v);
                break;
       }
        return viewHolder;
    }


    @Override
    public int getItemViewType(int position) {
        Satellite SAT = satellites.get(position);
        if(SAT.getmCardUseTypeId().equals("SMALL")){
            return SMALL;
        }else if (SAT.getmCardUseTypeId().equals("MEDIUM")){
            return MEDIUM;
        }else if (SAT.getmCardUseTypeId().equals("LARGE")){
            return LARGE;
        }

        return -1;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        switch (holder.getItemViewType()){
            case SMALL:
                Card_View_Holder_Satellite_Small vh1 = (Card_View_Holder_Satellite_Small) holder;
                configureViewHolderSmall(vh1,position);
                break;

            case MEDIUM:

                break;

            case LARGE:
                break;


        }
    }

    @Override
    public int getItemCount() {
        return satellites.size();
    }


    private void configureViewHolderSmall(Card_View_Holder_Satellite_Small vh1, int position) {
        Satellite SAT = satellites.get(position);

        vh1.txtPrn.setText(String.valueOf(SAT.getmPrnNo()));
        vh1.txtSignalStrength.setText(String.valueOf(SAT.getmSnrCn0()));
        vh1.txtAzimuth.setText(String.valueOf(SAT.getmSvAzimuth()));
        vh1.txtElevation.setText(String.valueOf(SAT.getmSvElevation()));

//        Check to see if satellite has an Almanac available
        if (SAT.ismHasAlmanac()) {
            vh1.txtAlmanac.setTextColor(Color.BLACK);

        } else {
            vh1.txtAlmanac.setTextColor(Color.LTGRAY);
        }

//        Check to see if satellite has an Ephemeris available
        if (SAT.ismHasEphemeris()){
            vh1.txtEphemeris.setTextColor(Color.BLACK);
        }else{
            vh1.txtEphemeris.setTextColor(Color.LTGRAY);
        }

//        Check to see if satellite has lock and used
        if(SAT.ismUsedinFix()){
            Glide
                    .with(mContext)
                    .load(R.drawable.ic_gps_sat_lock)
                    .into(vh1.imgLocked);

        }else{
            Glide
                    .with(mContext)
                    .load(R.drawable.ic_gps_sat_lock_no)
                    .into(vh1.imgLocked);
        }

//        Check to see what satellite constellation is being used

        GnssType type;
        if (GpsHelper.isGnssStatusListenerSupported()) {
            type = GpsHelper.getGnssConstellationType(SAT.getmConstellationType());
        } else {
            type = GpsHelper.getGnssType(SAT.getmPrnNo());
        }

        switch (type) {
            case NAVSTAR:
                Glide
                        .with(mContext)
                        .load(R.drawable.ic_flag_usa)
                        .into(vh1.imgConstellation);
                break;
            case GLONASS:
                Glide
                        .with(mContext)
                        .load(R.drawable.ic_flag_russia)
                        .into(vh1.imgConstellation);
                break;
            case QZSS:
                Glide
                        .with(mContext)
                        .load(R.drawable.ic_flag_japan)
                        .into(vh1.imgConstellation);
                break;
            case BEIDOU:
                Glide
                        .with(mContext)
                        .load(R.drawable.ic_flag_china)
                        .into(vh1.imgConstellation);
                break;
            case GALILEO:
                Glide
                        .with(mContext)
                        .load(R.drawable.ic_flag_galileo)
                        .into(vh1.imgConstellation);
                break;
        }

    }

    public void insert(int position, Satellite data){
        satellites.add(position,data);
        notifyItemInserted(position);
    }

    public void remove (Satellite data){
        int position = satellites.indexOf(data);
        satellites.remove(position);
        notifyItemRemoved(position);
    }

    public void swapDataSet (ArrayList<Satellite> newData){
        this.satellites = newData;

        notifyDataSetChanged();
    }

}
