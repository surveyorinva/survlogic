package com.survlogic.survlogic.view;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.survlogic.survlogic.R;

/**
 * Created by chrisfillmore on 5/26/2017.
 */

public class Satellite_Card_View_Holder_Small extends RecyclerView.ViewHolder {
    public CardView cv;
    public ImageView imgLocked, imgConstellation;
    public TextView txtPrn,txtSignalStrength,txtElevation,txtAzimuth,txtAlmanac, txtEphemeris;

    public Satellite_Card_View_Holder_Small(View itemView) {
        super(itemView);

        txtPrn = (TextView) itemView.findViewById(R.id.prnID);
        txtSignalStrength = (TextView) itemView.findViewById(R.id.signal_strength_value);
        txtElevation = (TextView) itemView.findViewById(R.id.satellite_elevation_value);
        txtAzimuth = (TextView) itemView.findViewById(R.id.satellite_azimuth_value);
        txtAlmanac = (TextView) itemView.findViewById(R.id.satellite_almanac_value);
        txtEphemeris = (TextView) itemView.findViewById(R.id.satellite_ephemeris_value);

        imgConstellation = (ImageView) itemView.findViewById(R.id.constellation_flag);
        imgLocked = (ImageView) itemView.findViewById(R.id.satellite_lock);

    }
}
