package com.survlogic.survlogic.view;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.survlogic.survlogic.R;
import com.survlogic.survlogic.model.PointMapCheck;

import org.w3c.dom.Text;

import java.util.ArrayList;

/**
 * Created by chrisfillmore on 6/30/2017.
 */

public class Card_View_Holder_Job_Mapcheck_List extends RecyclerView.ViewHolder  {
    private static final String TAG = "Card_View_Holder_Job_Ma";
    private Context mContext;

    public TextView tvPointString, tvPointDesc, tvPointObservation, tvStepNo;
    public ImageView ivObservation_Type;
    public ImageButton ibOptions_Menu;
    public View mCardView;



    public Card_View_Holder_Job_Mapcheck_List(View itemView, Context mContext) {
        super(itemView);
        this.mContext = mContext;

        initViewList();
    }

    private void initViewList(){

        mCardView = itemView;
        tvStepNo = (TextView) itemView.findViewById(R.id.stepNumber);
        tvPointObservation = (TextView) itemView.findViewById(R.id.observation);

        ivObservation_Type = (ImageView) itemView.findViewById(R.id.observation_type);
        ibOptions_Menu = (ImageButton) itemView.findViewById(R.id.options_menu);

        tvPointString = (TextView) itemView.findViewById(R.id.observation_points);

    }


    //----------------------------------------------------------------------------------------------//

    public String getObservationSetupString(int currentPosition, ArrayList<PointMapCheck> pointMapChecks, int observationType){
        String results, pointFrom, pointTo;
        String pointFromPrefix, pointToPrefix;


        if(currentPosition == 0){
            PointMapCheck pointMapCheckTo = pointMapChecks.get(currentPosition);
            pointFrom = mContext.getResources().getString(R.string.general_start);
            pointTo = String.valueOf(pointMapCheckTo.getToPointNo());

        }else{
            PointMapCheck pointMapCheckFrom = pointMapChecks.get(currentPosition - 1);
            PointMapCheck pointMapCheckTo = pointMapChecks.get(currentPosition);

            pointFrom = String.valueOf(pointMapCheckFrom.getToPointNo());

            if(pointMapCheckTo.getToPointNo() == 0){
                pointTo = mContext.getResources().getString(R.string.general_start);
            }else{
                pointTo = String.valueOf(pointMapCheckTo.getToPointNo());
            }

        }

        if(observationType == 0 || observationType == 1 || observationType == 2){
            //This is a line
            pointFromPrefix = "FROM: ";
            pointToPrefix = " TO: ";

        }else{
            //This is a curve
            pointFromPrefix = "PC: ";
            pointToPrefix = " PT: ";
        }

        results = pointFromPrefix + "" + pointFrom + "" + pointToPrefix + "" + pointTo;

        return results;
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
