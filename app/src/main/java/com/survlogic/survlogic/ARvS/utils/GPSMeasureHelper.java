package com.survlogic.survlogic.ARvS.utils;

import android.app.Activity;
import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.survlogic.survlogic.ARvS.interf.GPSMeasureHelperListener;
import com.survlogic.survlogic.R;
import com.survlogic.survlogic.background.BackgroundSurveyPointExistInDatabase;
import com.survlogic.survlogic.interf.DatabaseDoesPointExistFromAsyncListener;
import com.survlogic.survlogic.utils.KeyboardUtils;
import com.survlogic.survlogic.utils.PreferenceLoaderHelper;
import com.survlogic.survlogic.utils.RumbleHelper;
import com.survlogic.survlogic.utils.StringUtilityHelper;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class GPSMeasureHelper implements DatabaseDoesPointExistFromAsyncListener {
    private static final String TAG = "GPSMeasureHelper";

    private Context mContext;
    private Activity mActivity;

    private PreferenceLoaderHelper preferenceLoaderHelper;
    private GPSMeasureHelperListener listener;
    //---------------------------------------------------------------------------------------------- GNSS Measure
    private RelativeLayout rlMeasure, rlMeasureFooter, rlMeasureMenuSetup, rlFilter;
    private mehdi.sakout.fancybuttons.FancyButton ibGpsMeasure_Measure, ibGpsMeasure_Setup_Close;

    private EditText etPointNumber, etGpsHeight;
    private Spinner spRate;

    private String mJob_DatabaseName;

    private boolean hasGPSMeasureInit = false;
    private boolean isGPSMeasureSetup = false;
    private boolean isGPSMeasureSetupMenuOpen = false;
    private boolean isPointNoSet = false;

    //----------------------------------------------------------------------------------------------Method Variables
    private int mValuePointNo;


    public GPSMeasureHelper(Context mContext, String jobDatabaseName, GPSMeasureHelperListener listener) {
        this.mContext = mContext;
        this.mActivity = (Activity) mContext;

        this.mJob_DatabaseName = jobDatabaseName;
        this.listener = listener;

        preferenceLoaderHelper = new PreferenceLoaderHelper(mContext);
        initGPSMeasureWidgets();
    }


    //---------------------------------------------------------------------------------------------- Getters and Setters

    public void setViewState(boolean toShow){
        Log.d(TAG, "setViewState: Started:" + toShow);
        if(toShow){
            rlMeasure.setVisibility(View.VISIBLE);
        }else{
            rlMeasure.setVisibility(View.GONE);
        }
    }

    public boolean isGPSMeasureSetupMenuOpen() {
        return isGPSMeasureSetupMenuOpen;
    }

    public void requestSetupMenuClose(){
        closeMeasureDialogBox();
    }

    //---------------------------------------------------------------------------------------------- Methods

    private void initGPSMeasureWidgets() {
        Log.d(TAG, "initGPSMeasureWidgets: Started");

        rlMeasure = mActivity.findViewById(R.id.rl_gnss_measure);
        rlFilter = mActivity.findViewById(R.id.background_filter);

        rlMeasureFooter = mActivity.findViewById(R.id.rl_floating_footer);
        rlMeasureMenuSetup = mActivity.findViewById(R.id.rl_setup);

        ibGpsMeasure_Measure = mActivity.findViewById(R.id.btn_measure);
        ibGpsMeasure_Setup_Close = mActivity.findViewById(R.id.btn_settings_save);

        etPointNumber = mActivity.findViewById(R.id.pointNumber);
        etPointNumber.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                try {
                    String pointNo = null;
                    pointNo = etPointNumber.getText().toString();

                    if (!StringUtilityHelper.isStringNull(pointNo)) {
                        mValuePointNo = Integer.parseInt(pointNo);
                        checkPointNumberFromDatabase(mValuePointNo);

                    }

                } catch (NumberFormatException ex) {
                    showToast("Error.  Check Number Format", true);

                }
            }
        });

        etGpsHeight = mActivity.findViewById(R.id.gps_height);

        spRate = mActivity.findViewById(R.id.spinner_rate);
        setRateSpinner();

        setGPSMeasureOnClickListeners();
        hasGPSMeasureInit = true;

    }

    private void setRateSpinner() {
        Log.d(TAG, "setRateSpinner: Started");

        spRate.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
            // On selecting a spinner item
                String item = spRate.getItemAtPosition(position).toString();

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

    }


    private void setGPSMeasureOnClickListeners(){
        ibGpsMeasure_Measure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(!isGPSMeasureSetup){
                    openMeasureDialogBox();
                }else{
                    measureSpatialPosition();
                }

            }
        });

        ibGpsMeasure_Measure.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                RumbleHelper.init(mContext);
                RumbleHelper.once(50);

                openMeasureDialogBox();

                return true;
            }
        });

        ibGpsMeasure_Setup_Close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isGPSMeasureSetupMenuOpen){
                    closeMeasureDialogBox();
                }
            }
        });

    }

    private void openMeasureDialogBox(){
        Log.d(TAG, "openMeasureDialogBox: Started");
        DecimalFormat df = StringUtilityHelper.createUSNonBiasDecimalFormatSelect(2);

        isGPSMeasureSetupMenuOpen = true;

        double height = preferenceLoaderHelper.getGpsSurveyHeight();
        etGpsHeight.setText(df.format(height));

        rlFilter.setVisibility(View.VISIBLE);
        listener.setViewsForModal(true);
        rlMeasureMenuSetup.setVisibility(View.VISIBLE);


        if(!isGPSMeasureSetup){
            isGPSMeasureSetup = true;
        }

    }

    private void closeMeasureDialogBox(){
        Log.d(TAG, "closeMeasureDialogBox: Started");

        if(isPointNoSet){

            KeyboardUtils.hideKeyboard(mActivity);

            listener.setViewsForModal(false);
            rlFilter.setVisibility(View.GONE);
            rlMeasureMenuSetup.setVisibility(View.GONE);

            preferenceLoaderHelper.setGpsSurveyHeight(Float.valueOf(etGpsHeight.getText().toString()),true);

            isGPSMeasureSetupMenuOpen = false;
        }else{
            RumbleHelper.init(mContext);
            RumbleHelper.once(100);

            etPointNumber.setError(mContext.getResources().getString(R.string.cogo_point_no_not_entered));

        }


    }

    private void measureSpatialPosition(){
        Log.d(TAG, "measureSpatialPosition: Started");

        showToast("Measuring Here",true);

    }

    //---------------------------------------------------------------------------------------------- Point Entry Check
    private void checkPointNumberFromDatabase(int pointNumber){
        Log.d(TAG, "checkPointNumber: Started...");
        BackgroundSurveyPointExistInDatabase dbChecker = new BackgroundSurveyPointExistInDatabase(
                mContext,
                mJob_DatabaseName,
                this,
                pointNumber);

        dbChecker.execute();

    }

    //---------------------------------------------------------------------------------------------- DatabaseDoesPointExistFromAsyncListener
    @Override
    public void doesPointExist(int pointNumber, boolean isPointFoundInDatabase) {
        if(isPointFoundInDatabase){
            //point exists in either the database, throw flag
            etPointNumber.setError(mContext.getResources().getString(R.string.cogo_point_no_exists));
            Log.d(TAG, "does point exist:btSaveObservation: Locked");
            ibGpsMeasure_Setup_Close.setClickable(false);
            isPointNoSet = false;
        }else{
            etPointNumber.setError(null);
            Log.d(TAG, "does point exist:btSaveObservation: UnLocked");
            ibGpsMeasure_Setup_Close.setClickable(true);
            isPointNoSet = true;
        }

    }

    //---------------------------------------------------------------------------------------------- Helpers
    private void showToast(String data, boolean shortTime) {

        if (shortTime) {
            Toast.makeText(mContext, data, Toast.LENGTH_SHORT).show();

        } else{
            Toast.makeText(mContext, data, Toast.LENGTH_LONG).show();

        }
    }

}
