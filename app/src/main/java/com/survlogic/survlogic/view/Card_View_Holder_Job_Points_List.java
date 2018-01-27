package com.survlogic.survlogic.view;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.survlogic.survlogic.R;
import com.survlogic.survlogic.dialog.DialogJobPointView;

/**
 * Created by chrisfillmore on 6/30/2017.
 */

public class Card_View_Holder_Job_Points_List extends RecyclerView.ViewHolder  {
    private static final String TAG = "Card_View_Holder_Job_Ma";
    private Context mContext;

    public TextView tvPointNumber, tvPointDesc, tvPointNorthing, tvPointEasting, tvPointElevation;
    public View mCardView;



    public Card_View_Holder_Job_Points_List(View itemView, Context mContext) {
        super(itemView);
        this.mContext = mContext;

        initViewList();
    }

    private void initViewList(){
        mCardView = itemView;
        tvPointNumber = (TextView) itemView.findViewById(R.id.pointNumber);
        tvPointDesc = (TextView) itemView.findViewById(R.id.point_description);
        tvPointNorthing = (TextView) itemView.findViewById(R.id.observation_Northing);
        tvPointEasting = (TextView) itemView.findViewById(R.id.observation_Easting);
        tvPointElevation = (TextView) itemView.findViewById(R.id.observation_Elevation);

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
