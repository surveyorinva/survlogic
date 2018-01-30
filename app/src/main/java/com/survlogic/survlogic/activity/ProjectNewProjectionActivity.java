package com.survlogic.survlogic.activity;

import android.app.DialogFragment;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.survlogic.survlogic.R;
import com.survlogic.survlogic.dialog.DialogProjectProjectionsList;
import com.survlogic.survlogic.dialog.DialogProjectZoneList;
import com.survlogic.survlogic.interf.ProjectProjectionListener;

/**
 * Created by chrisfillmore on 1/25/2018.
 */

public class ProjectNewProjectionActivity extends AppCompatActivity implements ProjectProjectionListener {
    private static final String TAG = "ProjectNewActivityProje";

    private Context mContext;
    private String[] lstAvailableProjections, lstAvailableUSZones;
    private String selectedProjectionString, selectedZoneString;

    private RelativeLayout rlGridFrameworkZone;
    private TextView tvGridFramework, tvZone, tvStrategy;
    private ImageButton ibListGrids, ibListZones, ibListStrategy, ibBack;
    private Button btFinish;

    private Boolean isZone = false, isSPCS = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_project_new_projection);
        mContext = ProjectNewProjectionActivity.this;

        initViewWidgets();
        setOnClickListeners();
        generateProjections();
    }

    private void initViewWidgets(){
        Log.d(TAG, "initViewWidgets: Started");

        ibBack = (ImageButton) findViewById(R.id.button_back);
        btFinish = (Button) findViewById(R.id.button_finish);

        rlGridFrameworkZone = (RelativeLayout) findViewById(R.id.rlGridFramework_Zone);

        tvGridFramework = (TextView) findViewById(R.id.projection_value);
        tvZone = (TextView) findViewById(R.id.zone_value);
        tvStrategy = (TextView) findViewById(R.id.planar_strategy_value);

        ibListGrids = (ImageButton) findViewById(R.id.projection_from_list);
        ibListZones = (ImageButton) findViewById(R.id.zone_from_list);
        ibListStrategy = (ImageButton) findViewById(R.id.planar_from_list);


    }

    private void setOnClickListeners(){
        Log.d(TAG, "setOnClickListeners: Started");
        ibListGrids.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openProjectionListSelect();
            }
        });

        ibListZones.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openZoneListSelect();
            }
        });

        ibBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancelActivity();
            }
        });

        btFinish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finishActivity();
            }
        });

    }

    private void generateProjections(){
        Log.d(TAG, "generateProjections: Started");
        lstAvailableProjections = getResources().getStringArray(R.array.gridCRS_list_titles);
        lstAvailableUSZones = getResources().getStringArray(R.array.zone_SPCS_list_titles);


    }

    private void openProjectionListSelect(){
        Log.d(TAG, "openPointListSelect: Started...");

        clearZoneList();

        DialogFragment viewDialog = DialogProjectProjectionsList.newInstance(lstAvailableProjections);
        viewDialog.show(getFragmentManager(),"dialog");


    }

    private void openZoneListSelect(){
        Log.d(TAG, "openZoneListSelect: Started...");

        if(isSPCS){
            DialogFragment viewDialog = DialogProjectZoneList.newInstance(lstAvailableUSZones,isSPCS);
            viewDialog.show(getFragmentManager(),"dialog");

        }

    }

    private void clearZoneList(){
        Log.d(TAG, "clearZoneList: Started...");

        selectedZoneString = null;
        showZone(false);


    }

    //----------------------------------------------------------------------------------------------//

    private void evaluateStringReturnProjection(){
        Log.d(TAG, "evaluateStringReturnProjection: Started");

        String[] separatedProjectionValue = selectedProjectionString.split(",");

        //        State Plane Coordinate System of 1983,USA,EPSG:4269,true,SPCS,2,EPSG:0

        isZone = Boolean.valueOf(separatedProjectionValue[3]);

        if(isZone){
            String zoneName = separatedProjectionValue[4];

            switch(zoneName){
                case "SPCS":
                    showZone(true);
                    isSPCS = true;
            }

        }else{
            showZone(false);
        }

    }

    private void showZone(boolean toShowView){
        if(toShowView){
            rlGridFrameworkZone.setVisibility(View.VISIBLE);
        }else{
            rlGridFrameworkZone.setVisibility(View.GONE);
            tvZone.setText(null);
        }


    }

    //----------------------------------------------------------------------------------------------//

    private void cancelActivity(){
        Log.d(TAG, "cancelActivity: Started");

        finish();


    }

    private void finishActivity(){
        Log.d(TAG, "finishActivity: Started");



    }

    //----------------------------------------------------------------------------------------------//

    @Override
    public void onReturnValueProjection(String stringProjection, String shortName) {
        tvGridFramework.setText(shortName);
        this.selectedProjectionString = stringProjection;

        evaluateStringReturnProjection();

    }

    @Override
    public void onReturnValueZone(String stringZone, String shortName) {
        tvZone.setText(shortName);
        this.selectedZoneString = stringZone;

    }
}
