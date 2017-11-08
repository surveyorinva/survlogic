package com.survlogic.survlogic.view;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.survlogic.survlogic.R;

/**
 * Created by chrisfillmore on 6/30/2017.
 */

public class Card_View_Holder_Job_PointSurvey_Small extends RecyclerView.ViewHolder  {

    public TextView tvPointNo, tvPointDesc, tvPointNorth, tvPointEast, tvPointHeight;
    public View mCardView;


    public Card_View_Holder_Job_PointSurvey_Small(View itemView) {
        super(itemView);

        mCardView = itemView;
        tvPointNo = (TextView) itemView.findViewById(R.id.pointNumber);
        tvPointDesc = (TextView) itemView.findViewById(R.id.pointDescription);
        tvPointNorth = (TextView) itemView.findViewById(R.id.pointNorthing);
        tvPointEast = (TextView) itemView.findViewById(R.id.pointEasting);
        tvPointHeight = (TextView) itemView.findViewById(R.id.pointElevation);

    }

}
