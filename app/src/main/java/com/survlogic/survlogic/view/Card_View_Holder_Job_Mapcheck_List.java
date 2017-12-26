package com.survlogic.survlogic.view;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.survlogic.survlogic.R;

/**
 * Created by chrisfillmore on 6/30/2017.
 */

public class Card_View_Holder_Job_Mapcheck_List extends RecyclerView.ViewHolder  {
    private static final String TAG = "Card_View_Holder_Job_Ma";
    private Context mContext;

    public TextView tvPointNo, tvPointDesc, tvPointObservation, tvStepNo;
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



    }

    public void popUpOnClickListener(int position){
        Log.d(TAG, "popUpOnClickListener: Started...");
        final String sPosition = String.valueOf(position);

        ibOptions_Menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popupMenuObsType = new PopupMenu(mContext, ibOptions_Menu);
                popupMenuObsType.inflate(R.menu.popup_cogo_mapcheck_list_options);

                popupMenuObsType.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {

                        switch (item.getItemId()){
                            case R.id.mapcheck_item_1:
                                    showToast("Edit Position " + sPosition, true);
                                break;

                            case R.id.mapcheck_item_2:
                                showToast("Delete Position " + sPosition, true);
                                break;

                        }

                        return true;
                    }
                });

                popupMenuObsType.show();
            }
        });
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
