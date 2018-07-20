package com.survlogic.survlogic.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.survlogic.survlogic.R;
import com.survlogic.survlogic.dialog.DialogProjectProjectionsList;
import com.survlogic.survlogic.dialog.DialogProjectZoneList;
import com.survlogic.survlogic.interf.ProjectProjectionListener;
import com.survlogic.survlogic.utils.PreferenceLoaderHelper;
import com.survlogic.survlogic.utils.StringUtilityHelper;

import java.text.DecimalFormat;

/**
 * Created by chrisfillmore on 1/25/2018.
 */

public class ProjectNewProjectionActivity extends AppCompatActivity implements ProjectProjectionListener {
    private static final String TAG = "ProjectNewActivityProje";

    private Context mContext;
    private PreferenceLoaderHelper preferenceLoaderHelper;
    private String[] lstAvailableProjections, lstAvailableUSZones;
    private String selectedProjectionString, selectedZoneString;
    private ArrayAdapter<String> strategyAdapter;
    
    private int selectedStrategy = 0;
    private double selectedProjectionScale, selectedProjectionOriginNorthing, selectedProjectionOriginEasting;

    private RelativeLayout rlGridFramework, rlGridFrameworkZone, rlStrategyScaleFactor, rlStrategyScaleOrigin;
    private TextView tvGridFramework, tvZone;
    private EditText etProjectionScale, etProjectionOriginNorth, etProjectionOriginEast;
    private Spinner spStrategy;
    private ImageButton ibListGrids, ibListZones, ibBack;
    private Button btFinish;

    private Boolean isZone = false, isSPCS = false;
    private static final int CONVENTIONAL = 0, GPS = 1;
    private static DecimalFormat COORDINATE_FORMATTER, DISTANCE_PRECISION_FORMATTER;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_project_new_projection);
        mContext = ProjectNewProjectionActivity.this;
        preferenceLoaderHelper = new PreferenceLoaderHelper(mContext);

        initViewWidgets();
        setOnClickListeners();
        generateProjections();
        populateForm();
    }

    private void initViewWidgets(){
        Log.d(TAG, "initViewWidgets: Started");

        ibBack = findViewById(R.id.button_back);
        btFinish = findViewById(R.id.button_finish);

        rlGridFramework = findViewById(R.id.rlGridDatum);
        rlGridFrameworkZone = findViewById(R.id.rlGridFramework_Zone);
        animateViewSlideVisible(rlGridFrameworkZone,false,1);

        rlStrategyScaleFactor = findViewById(R.id.rl_Ground_Framework_Scale_Factor);
        animateViewSlideVisible(rlStrategyScaleFactor,false,1);
        rlStrategyScaleOrigin = findViewById(R.id.rl_Ground_Framework_Scale_Shift);
        animateViewSlideVisible(rlStrategyScaleOrigin,false,1);

        tvGridFramework = findViewById(R.id.projection_value);
        tvGridFramework.setText(getResources().getString(R.string.general_none));

        tvZone =  findViewById(R.id.zone_value);

        spStrategy = findViewById(R.id.planar_strategy_value);
        initStrategyAdapter();

        ibListGrids =  findViewById(R.id.projection_from_list);
        ibListZones = findViewById(R.id.zone_from_list);

        etProjectionScale =  findViewById(R.id.scale_value);
        etProjectionScale.setSelectAllOnFocus(true);

        etProjectionOriginNorth = findViewById(R.id.modified_north_value);
        etProjectionOriginNorth.setSelectAllOnFocus(true);

        etProjectionOriginEast =  findViewById(R.id.modified_easting_value);
        etProjectionOriginEast.setSelectAllOnFocus(true);
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
                Log.d(TAG, "onClick: Started");
                finishActivity();
            }
        });

        spStrategy.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                evaluateStrategy(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

    }

    private void generateProjections(){
        Log.d(TAG, "generateProjections: Started");
        lstAvailableProjections = getResources().getStringArray(R.array.gridCRS_list_titles);
        lstAvailableUSZones = getResources().getStringArray(R.array.zone_SPCS_list_titles);


    }

    private void setStrategyPreferences(){
        Log.d(TAG, "setPreferences: Started");

        COORDINATE_FORMATTER = new DecimalFormat(preferenceLoaderHelper.getValueSystemCoordinatesPrecisionDisplay());
        DISTANCE_PRECISION_FORMATTER = new DecimalFormat(preferenceLoaderHelper.getValueSystemDistancePrecisionDisplay());


        etProjectionScale.setText(DISTANCE_PRECISION_FORMATTER.format(preferenceLoaderHelper.getDefaultProjectProjectionScaleGridToGround()));
        etProjectionOriginNorth.setText(COORDINATE_FORMATTER.format(preferenceLoaderHelper.getDefaultProjectProjectionOriginNorth()));
        etProjectionOriginEast.setText(COORDINATE_FORMATTER.format(preferenceLoaderHelper.getDefaultProjectProjectionOriginEast()));

    }


    //----------------------------------------------------------------------------------------------//
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

        selectedZoneString = getResources().getString(R.string.projection_zone_none);
        showZone(false);


    }

    //----------------------------------------------------------------------------------------------//

    private void populateForm(){
        Intent intent = getIntent();
        if(intent.hasExtra(getString(R.string.KEY_PROJECTION_ISEDIT))){
            Bundle extras = getIntent().getExtras();

            selectedProjectionString = extras.getString(getString(R.string.KEY_PROJECTION_STRING));
            selectedZoneString = extras.getString(getString(R.string.KEY_PROJECTION_ZONE_STRING));
            selectedStrategy = extras.getInt(getString(R.string.KEY_PROJECTION_STRATEGY));
            selectedProjectionScale = extras.getDouble(getString(R.string.KEY_PROJECTION_STRATEGY_SCALE));
            selectedProjectionOriginNorthing = extras.getDouble(getString(R.string.KEY_PROJECTION_STRATEGY_ORIGIN_NORTHING));
            selectedProjectionOriginEasting = extras.getDouble(getString(R.string.KEY_PROJECTION_STRATEGY_ORIGIN_EASTING));

            splitStringProjectionToPartsForEdit();
            populateFormSwitchBoard(selectedStrategy);

        }else{

            selectedProjectionString = getResources().getString(R.string.projection_none);
            selectedZoneString = getResources().getString(R.string.projection_zone_none);
            selectedStrategy = CONVENTIONAL;

            selectedProjectionScale = (preferenceLoaderHelper.getDefaultProjectProjectionScaleGridToGround());
            selectedProjectionOriginNorthing  = preferenceLoaderHelper.getDefaultProjectProjectionOriginNorth();
            selectedProjectionOriginEasting = preferenceLoaderHelper.getDefaultProjectProjectionOriginEast();

        }
    }

    private void splitStringProjectionToPartsForEdit(){
        Log.d(TAG, "splitStringsForForm: ");

        String[] separatedProjectionValue = selectedProjectionString.split(",");
        String projectionName = separatedProjectionValue[0];

        tvGridFramework.setText(projectionName);

        String[] separatedZoneValue = selectedZoneString.split(",");
        String zoneName = separatedZoneValue[0];

        tvZone.setText(zoneName);

    }

    private void populateFormSwitchBoard(int strategy){
        Log.d(TAG, "populateForm: Started");

        evaluateStringReturnProjection();
        evaluateStrategy(strategy);


    }

    private void evaluateStringReturnProjection(){
        Log.d(TAG, "evaluateStringReturnProjection: Started");

        String[] separatedProjectionValue = selectedProjectionString.split(",");

        //        State Plane Coordinate System of 1983,USA,EPSG:4269,true,SPCS,2,EPSG:0

        String projectionName = separatedProjectionValue[0];
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

        if(projectionName == getResources().getString(R.string.general_none)){
            showZone(false);
        }

    }

    private void evaluateStrategy(int position){
        Log.d(TAG, "evaluateStrategy: position: " + position);
        switch (position){

            case CONVENTIONAL:
                animateViewSlideVisible(rlStrategyScaleFactor,false,300);
                animateViewSlideVisible(rlStrategyScaleOrigin,false,350);
                selectedStrategy = CONVENTIONAL;
                spStrategy.setSelection(CONVENTIONAL);
                break;

            case GPS:
                animateViewSlideVisible(rlStrategyScaleFactor,true,300);
                animateViewSlideVisible(rlStrategyScaleOrigin,true,350);
                selectedStrategy = GPS;
                spStrategy.setSelection(GPS);

                setStrategyPreferences();
                break;

        }
    }

    private void showZone(boolean toShowView){
        if(!toShowView) {
            tvZone.setText(null);
        }

        animateViewSlideVisible(rlGridFrameworkZone,toShowView, 500);

    }

    //----------------------------------------------------------------------------------------------//

    private void initStrategyAdapter(){
        Log.d(TAG, "initProjectionAdapter: ");

        String[] strategyChoices = getResources().getStringArray(R.array.project_projection_strategies_titles);

        strategyAdapter = new ArrayAdapter<String>(ProjectNewProjectionActivity.this,R.layout.view_spinner_item,strategyChoices);
        strategyAdapter.setDropDownViewResource(R.layout.view_spinner_dropdown_item);

        spStrategy.setAdapter(strategyAdapter);

    }


    //----------------------------------------------------------------------------------------------//
    private void cancelActivity(){
        Log.d(TAG, "cancelActivity: Started");
        setResult(Activity.RESULT_CANCELED);
        finish();
        
    }

    private void finishActivity(){
        Log.d(TAG, "finishActivity: Started");
        boolean isErrorInStrategy = false, isErrorInZone;

        //Zone
        isErrorInZone = validateZoneForNull();

        isErrorInStrategy = validateStrategyForNull();

        if(!isErrorInZone && !isErrorInStrategy){
            returnResults();
        }else if(isErrorInZone){
            showToast(getResources().getString(R.string.project_new_projection_validation_zone_not_entered),true);
        }
    }

    private boolean validateProjectionForNull(){
        Log.d(TAG, "validateProjectionForNull: Started");

        boolean isThereAnError = false;
        boolean isThereAProjection = false;


        String[] separatedProjectionValue = selectedProjectionString.split(",");
        String projectionName = separatedProjectionValue[0];
        String displayName = projectionName;

        if(displayName.equals(getResources().getString(R.string.general_none))){
            isThereAnError = true;
        }


        Log.d(TAG, "validateZoneForNull: Error: " + isThereAnError);
        return isThereAnError;

    }

    private boolean validateZoneForNull(){
        Log.d(TAG, "validateProjectionForNull: Started");
        boolean isThereAnError = false;
        boolean isThereAProjection = false;


        String[] separatedProjectionValue = selectedProjectionString.split(",");
        String projectionName = separatedProjectionValue[0];
        String displayName = projectionName;

        if(displayName != getResources().getString(R.string.general_none)){
            isThereAProjection = true;
        }

        Boolean isZone = Boolean.valueOf(separatedProjectionValue[3]);
        if(isZone){
            String[] separatedZoneValue = selectedZoneString.split(",");
            String zoneName = separatedZoneValue[0];

            Log.d(TAG, "validateZoneForNull: zone Name: " + zoneName);

            if(zoneName.equals(getResources().getString(R.string.general_none))){
                isThereAnError = true;
            }

        }
        Log.d(TAG, "validateZoneForNull: Error: " + isThereAnError);
        return isThereAnError;

    }

    private boolean validateStrategyForNull(){
        Log.d(TAG, "validateForm: Started");
        boolean isThereAnError = false;

        //Gather Info if strategy = 1
        switch(selectedStrategy){
            case 0:
                isThereAnError = false;
                break;

            case 1:

                isThereAnError = validateProjectionForNull();

                if(isThereAnError){
                    showToast(getResources().getString(R.string.project_new_projection_validation_project_not_entered_for_strategy),true);
                }

                String projectionScale = null;
                String projectionOriginNorthing = null;
                String projectionOriginEasting = null;

                projectionScale = etProjectionScale.getText().toString();
                projectionOriginNorthing = etProjectionOriginNorth.getText().toString();
                projectionOriginEasting = etProjectionOriginEast.getText().toString();

                if(!StringUtilityHelper.isStringNull(projectionScale)){
                    selectedProjectionScale = Double.parseDouble(projectionScale);
                }else{
                    etProjectionScale.setError(mContext.getResources().getString(R.string.project_new_projection_validation_scale_factor_null));
                    isThereAnError = true;
                }

                if(!StringUtilityHelper.isStringNull(projectionOriginNorthing)){
                    selectedProjectionOriginNorthing = Double.parseDouble(projectionOriginNorthing);
                }else{
                    etProjectionOriginNorth.setError(mContext.getResources().getString(R.string.project_new_projection_validation_origin_northing_null));
                    isThereAnError = true;
                }

                if(!StringUtilityHelper.isStringNull(projectionOriginEasting)){
                    selectedProjectionOriginEasting = Double.parseDouble(projectionOriginEasting);
                }else{
                    etProjectionOriginEast.setError(mContext.getResources().getString(R.string.project_new_projection_validation_origin_easting_null));
                    isThereAnError = true;
                }


                break;
        }

        return isThereAnError;

    }

    private void returnResults(){
        Log.d(TAG, "returnResults: Started");

        Intent returnIntent = new Intent();
        returnIntent.putExtra(getString(R.string.KEY_PROJECTION_STRING),selectedProjectionString);
        returnIntent.putExtra(getString(R.string.KEY_PROJECTION_ZONE_STRING),selectedZoneString);
        returnIntent.putExtra(getString(R.string.KEY_PROJECTION_STRATEGY),selectedStrategy);
        returnIntent.putExtra(getString(R.string.KEY_PROJECTION_STRATEGY_SCALE),selectedProjectionScale);
        returnIntent.putExtra(getString(R.string.KEY_PROJECTION_STRATEGY_ORIGIN_NORTHING),selectedProjectionOriginNorthing);
        returnIntent.putExtra(getString(R.string.KEY_PROJECTION_STRATEGY_ORIGIN_EASTING),selectedProjectionOriginEasting);
        setResult(Activity.RESULT_OK,returnIntent);
        finish();

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


    //----------------------------------------------------------------------------------------------//

    /**
     * Method Helpers
     */


    private void showToast(String data, boolean shortTime) {

        if (shortTime) {
            Toast.makeText(this, data, Toast.LENGTH_SHORT).show();

        } else{
            Toast.makeText(this, data, Toast.LENGTH_LONG).show();

        }
    }


    private boolean isStringEmpty(String string){
        Log.d(TAG, "isStringEmpty: checking string is null or empty (from DB)");

        if (string !=null && !string.isEmpty()) {
            return false;
        }else {
            return true;
        }


    }


    private void animateViewSlideVisible(final View view, boolean toShowView, long duration){
        if(toShowView){
            view.setVisibility(View.VISIBLE);
            view.setAlpha(0.0f);

            view.animate()
                    .setDuration(duration)
                    //.setInterpolator(new BounceInterpolator())
                    .translationY(0)
                    .alpha(1.0f)
                    .setListener(null);
        }else{
            view.animate()
                    .setDuration(duration)
                    .translationY(-view.getHeight())
                    .alpha(0.0f)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);
                            view.setVisibility(View.GONE);
                        }
                    });
        }

    }
}
