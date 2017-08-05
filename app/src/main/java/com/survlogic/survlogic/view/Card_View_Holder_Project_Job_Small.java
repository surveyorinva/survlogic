package com.survlogic.survlogic.view;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.survlogic.survlogic.R;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by chrisfillmore on 6/30/2017.
 */

public class Card_View_Holder_Project_Job_Small extends RecyclerView.ViewHolder  {

    public TextView txtJobName, txtLastModify;
    public Button btOpen;
    public View mCardView;


    public Card_View_Holder_Project_Job_Small(View itemView) {
        super(itemView);

        mCardView = itemView;
        txtJobName = (TextView) itemView.findViewById(R.id.job_name);
        txtLastModify  = (TextView) itemView.findViewById(R.id.job_created_on);
        btOpen = (Button) itemView.findViewById(R.id.open_job);
    }

}
