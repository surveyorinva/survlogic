package com.survlogic.survlogic.view;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.survlogic.survlogic.R;

/**
 * Created by chrisfillmore on 6/30/2017.
 */

public class Card_View_Holder_Job_Gps_Stakeout_Point_List extends RecyclerView.ViewHolder  {
    private static final String TAG = "Card_View_Holder_Job_Ma";
    private Context mContext;

    public TextView tvPointNumber, tvPointDesc, tvDistanceToTarget;
    public View mCardView;



    public Card_View_Holder_Job_Gps_Stakeout_Point_List(View itemView, Context mContext) {
        super(itemView);
        this.mContext = mContext;

        initViewList();
    }

    private void initViewList(){
        mCardView = itemView;
        tvPointNumber = itemView.findViewById(R.id.pointNumber);
        tvPointDesc = itemView.findViewById(R.id.pointDescription);
        tvDistanceToTarget = itemView.findViewById(R.id.distanceToTarget);

    }


    //----------------------------------------------------------------------------------------------//



    //----------------------------------------------------------------------------------------------//

    private void showToast(String data, boolean shortTime) {
        if (shortTime) {
            Toast.makeText(mContext, data, Toast.LENGTH_SHORT).show();

        } else{
            Toast.makeText(mContext, data, Toast.LENGTH_LONG).show();

        }
    }


}
