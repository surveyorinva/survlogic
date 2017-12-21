package com.survlogic.survlogic.view;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.survlogic.survlogic.R;

/**
 * Created by chrisfillmore on 6/30/2017.
 */

public class Card_View_Holder_Job_Mapcheck_List extends RecyclerView.ViewHolder  {

    public TextView tvPointNo, tvPointDesc, tvPointObservation, tvStepNo;
    public ImageView ivObservation_Type;
    public ImageButton ibOptions_Menu;
    public View mCardView;


    public Card_View_Holder_Job_Mapcheck_List(View itemView) {
        super(itemView);

        mCardView = itemView;
        tvStepNo = (TextView) itemView.findViewById(R.id.stepNumber);
        tvPointObservation = (TextView) itemView.findViewById(R.id.observation);

        ivObservation_Type = (ImageView) itemView.findViewById(R.id.observation_type);
        ibOptions_Menu = (ImageButton) itemView.findViewById(R.id.options_menu);

    }

}
