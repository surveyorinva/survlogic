package com.survlogic.survlogic.ARvS.dialog;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.survlogic.survlogic.ARvS.interf.GetNationalMapElevationListener;
import com.survlogic.survlogic.R;
import com.survlogic.survlogic.background.BackgroundGetNationalMapElevation;
import com.survlogic.survlogic.background.BackgroundGetSRTMElevation;
import com.survlogic.survlogic.model.PointGeodetic;
import com.survlogic.survlogic.model.PointSurvey;
import com.survlogic.survlogic.utils.GPSLocationConverter;
import com.survlogic.survlogic.utils.MathHelper;
import com.survlogic.survlogic.utils.PreferenceLoaderHelper;
import com.survlogic.survlogic.utils.StringUtilityHelper;
import com.survlogic.survlogic.utils.SurveyMathHelper;
import com.survlogic.survlogic.utils.SurveyProjectionHelper;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import mehdi.sakout.fancybuttons.FancyButton;

/**
 * Created by chrisfillmore on 7/22/2017.
 */

public class DialogGPSSurveyMeasureResults extends DialogFragment implements GetNationalMapElevationListener{
    private static final String TAG = "DialogProjectDescr";
    private Context mContext;

    private AlertDialog alertDialog;
    private PreferenceLoaderHelper preferenceLoaderHelper;
    private SurveyProjectionHelper surveyProjectionHelper;


    //---------------------------------------------------------------------------------------------- In Variables
    private ArrayList<Location> measuredResults;
    private int pointNumber = 0;
    private double instrumentHeight = 0;

    //---------------------------------------------------------------------------------------------- Widgets
    private RelativeLayout rlResultsView;
    private LinearLayout llResultsContainer;
    private NestedScrollView nsResultsContainer;

    private ProgressBar progressBar, progressOrtho;
    private FancyButton fbDialogCancel, fbDialogSave;
    private Button btResultsShow;

    private TextView tvResultsHeader;
    private TextView tvResultLatitude, tvResultLongitude, tvResultEllipsoid;
    private TextView tvResultGridNorth, tvResultGridEast;
    private TextView tvResultLocalNorth, tvResultLocalEast;
    private TextView tvResultOrtho;
    private TextView tvResultEpoch, tvResultAccuracy;
    private TextView tvResultVariance, tvResultStdDev;

    private EditText etPointNumber, etPointHeight, etPointDescription;
    private TextView tvHiddenPointNumber, tvHiddenPointHeight, tvHiddenPointDescription;

    //---------------------------------------------------------------------------------------------- Method Variables
    private boolean isJobWithProjection = false;
    private boolean isResultsShown = false;

    private ArrayList<PointGeodetic> lstPointGeodetic = new ArrayList<>();
    private ArrayList<PointSurvey> lstPointGrid = new ArrayList<>();

    private float mScaleFactor = 0.0F;
    private float mOriginNorth = 0.0F, mOriginEast = 0.0F;

    private final int POI_TARGET = 0, POI_CURRENT = 1;

    //---------------------------------------------------------------------------------------------- Out Variables
    private Location mAverageLocation;
    private PointSurvey mAverageGridPoint, mAverageLocalPoint;


    //---------------------------------------------------------------------------------------------- CODE Start

    public static DialogGPSSurveyMeasureResults newInstance(ArrayList<Location> measured, int pointNumber, double instHeight) {
        Log.d(TAG, "newInstance: showResultsDialog:" + measured.size());
        DialogGPSSurveyMeasureResults frag = new DialogGPSSurveyMeasureResults();
        Bundle args = new Bundle();

        args.putParcelableArrayList("list",measured);
        frag.setArguments(args);

        frag.measuredResults = measured;
        frag.pointNumber = pointNumber;
        frag.instrumentHeight = instHeight;

        return frag;

    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Log.d(TAG, "showResultsDialog: Created");

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(),R.style.DialogExplodeInStyle_NoTouch);
        LayoutInflater inflater = getActivity().getLayoutInflater();

        View v = inflater.inflate(R.layout.dialog_gps_survey_measure_results,null);
        builder.setView(v);

        builder.create();
        return builder.show();


    }

    @Override
    public void onResume() {
        super.onResume();

        mContext = getActivity();
        AlertDialog alertDialog = (AlertDialog) getDialog();

        preferenceLoaderHelper = new PreferenceLoaderHelper(mContext);
        loadPreferences();

        initViewWidgets();
        initFocusChangeListeners();
        loadDataset();

    }

    private void loadPreferences(){
        Log.d(TAG, "loadPreferences: Started...");

    }

    private void initViewWidgets(){
        Log.d(TAG, "initViewWidgets: Started");

        rlResultsView = getDialog().findViewById(R.id.rl_details_results);

        llResultsContainer = getDialog().findViewById(R.id.ll_details_extra);
        nsResultsContainer = getDialog().findViewById(R.id.scrollView_in_measure_point);

        progressBar = getDialog().findViewById(R.id.dialog_progress);

        btResultsShow = getDialog().findViewById(R.id.open_results);
        btResultsShow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showHideResults();
            }
        });

        fbDialogCancel = getDialog().findViewById(R.id.btn_measure_cancel);
        fbDialogCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                closeDialog();
            }
        });
        fbDialogSave = getDialog().findViewById(R.id.btn_measure_save);
        fbDialogSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveMeasurements();
            }
        });

        tvResultsHeader = getDialog().findViewById(R.id.lbl_header_results);

        tvResultLatitude = getDialog().findViewById(R.id.value_latitude);
        tvResultLongitude = getDialog().findViewById(R.id.value_longitude);
        tvResultEllipsoid = getDialog().findViewById(R.id.value_ellipsoid);

        tvResultGridNorth = getDialog().findViewById(R.id.value_grid_north);
        tvResultGridEast = getDialog().findViewById(R.id.value_grid_east);

        tvResultLocalNorth = getDialog().findViewById(R.id.value_local_north);
        tvResultLocalEast = getDialog().findViewById(R.id.value_local_east);

        progressOrtho = getDialog().findViewById(R.id.progress_ortho);
        tvResultOrtho = getDialog().findViewById(R.id.value_ortho);

        tvResultEpoch = getDialog().findViewById(R.id.value_epoch);
        tvResultAccuracy = getDialog().findViewById(R.id.value_accuracy);

        tvResultVariance = getDialog().findViewById(R.id.value_result_variance);
        tvResultStdDev = getDialog().findViewById(R.id.value_result_std_dev);

        etPointNumber = getDialog().findViewById(R.id.pointNumber);
        etPointNumber.setSelectAllOnFocus(true);

        etPointHeight = getDialog().findViewById(R.id.gps_height);
        etPointHeight.setSelectAllOnFocus(true);

        etPointDescription = getDialog().findViewById(R.id.gps_description);
        etPointDescription.setSelectAllOnFocus(true);

    }

    private void initFocusChangeListeners() {
        Log.d(TAG, "initFocusChangeListeners: Started...");
        final DecimalFormat df2 = StringUtilityHelper.createUSNonBiasDecimalFormatSelect(2);

        tvHiddenPointNumber =  getDialog().findViewById(R.id.dialog_item_header_pointno_hint);
        tvHiddenPointHeight = getDialog().findViewById(R.id.dialog_item_header_height_hint);
        tvHiddenPointDescription = getDialog().findViewById(R.id.dialog_item_header_description_hint);


        etPointNumber.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {

                if (hasFocus) {
                    new Handler().postDelayed(new Runnable() {

                        @Override
                        public void run() {
                            // Show white background behind floating label
                            tvHiddenPointNumber.setVisibility(View.VISIBLE);
                        }
                    }, 100);
                } else {
                    // Required to show/hide white background behind floating label during focus change
                    showHideMaterialEditTextBackground(etPointNumber, tvHiddenPointNumber);
                }
            }
        });

        etPointHeight.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {

                if (hasFocus) {
                    new Handler().postDelayed(new Runnable() {

                        @Override
                        public void run() {
                            // Show white background behind floating label
                            tvHiddenPointHeight.setVisibility(View.VISIBLE);
                        }
                    }, 100);
                } else {
                    // Required to show/hide white background behind floating label during focus change

                    showHideMaterialEditTextBackground(etPointHeight, tvHiddenPointHeight);


                    //Add Correct Decimal Places After
                    try {
                        String stringValue = null;
                        stringValue = etPointHeight.getText().toString();

                        if (!StringUtilityHelper.isStringNull(stringValue)) {
                            double value = Double.parseDouble(stringValue);

                            etPointHeight.setText(df2.format(value));
                        }

                    } catch (NumberFormatException ex) {
                        showToast("Error.  Check Number Format", true);

                    }
                }
            }
        });

        etPointDescription.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {

                if (hasFocus) {
                    new Handler().postDelayed(new Runnable() {

                        @Override
                        public void run() {
                            // Show white background behind floating label
                            tvHiddenPointDescription.setVisibility(View.VISIBLE);
                        }
                    }, 100);
                } else {
                    // Required to show/hide white background behind floating label during focus change

                    showHideMaterialEditTextBackground(etPointDescription, tvHiddenPointDescription);

                }
            }
        });
    }


    private void showHideMaterialEditTextBackground(EditText lookAt, TextView background){
        Log.d(TAG, "showHideMaterialEditTextBackground: Started");

        if (lookAt.getText().length() > 0)
            background.setVisibility(View.VISIBLE);
        else
            background.setVisibility(View.INVISIBLE);
    }

//--------------------------------------------------------------------------------------------------// Dataset In Actions
    private void loadDataset(){
        Log.d(TAG, "loadDataset: Started");

        etPointNumber.setText(String.valueOf(pointNumber));
        showHideMaterialEditTextBackground(etPointNumber, tvHiddenPointHeight);


        etPointHeight.setText(String.valueOf(instrumentHeight));
        showHideMaterialEditTextBackground(etPointHeight, tvHiddenPointHeight);


        progressBar.setVisibility(View.VISIBLE);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                findAverageGeodeticPosition(measuredResults);
                findAverageGridPosition(measuredResults);

                progressBar.setVisibility(View.INVISIBLE);
            }
        }, 500);


    }


    //---------------------------------------------------------------------------------------------- Dataset In Methods
    private void findAverageGeodeticPosition(ArrayList<Location> geodeticPosition){
        Log.d(TAG, "findAveragePosition: Started");
        DecimalFormat df1 = StringUtilityHelper.createUSNonBiasDecimalFormatSelect(1);

        if(geodeticPosition.size() > 1){
            List<Double> aLatitudeList = new ArrayList<>();
            List<Double> aLongitudeList = new ArrayList<>();
            List<Double> aEllipsoidList = new ArrayList<>();
            List<Float> aAccuracyList = new ArrayList<>();

                for(Location location : geodeticPosition){
                    aLatitudeList.add(location.getLatitude());
                    aLongitudeList.add(location.getLongitude());
                    aEllipsoidList.add(location.getAltitude());
                    aAccuracyList.add(location.getAccuracy());
                }
            Double meanLatitude = 0.0, meanLongitude = 0.0, meanEllipsoid = 0.0;
            Float meanAccuracy = 0.0F;

            MathHelper mathHelper = new MathHelper(mContext);

            meanLatitude = mathHelper.getMeanDouble(aLatitudeList);

            meanLongitude = mathHelper.getMeanDouble(aLongitudeList);
            meanEllipsoid = mathHelper.getMeanDouble(aEllipsoidList);
            meanAccuracy = mathHelper.getMeanFloat(aAccuracyList);

            mAverageLocation = new Location("");

            mAverageLocation.setLatitude(meanLatitude);
            mAverageLocation.setLongitude(meanLongitude);
            mAverageLocation.setAltitude(meanEllipsoid);
            mAverageLocation.setAccuracy(meanAccuracy);

            //Separate Thread
            boolean findOrthoHeight = findAverageOrthoFromInternet();


            tvResultLatitude.setText(SurveyMathHelper.convertDECtoDMSGeodetic(mAverageLocation.getLatitude(),4,true));
            tvResultLongitude.setText(SurveyMathHelper.convertDECtoDMSGeodetic(mAverageLocation.getLongitude(),4,true));
            tvResultEllipsoid.setText(df1.format(meanEllipsoid));

            double accuracy = 0.0;

            switch (preferenceLoaderHelper.getGeneral_over_units()){
                case 0:  //US Feet
                    accuracy = GPSLocationConverter.convertMetersToValue(meanAccuracy,1);
                    break;

                case 1: //Int. Feet
                    accuracy = GPSLocationConverter.convertMetersToValue(meanAccuracy,2);
                    break;

                case 2: //Meters
                    accuracy = GPSLocationConverter.convertMetersToValue(meanAccuracy,3);
                    break;
            }

            tvResultAccuracy.setText(df1.format(accuracy));

            int epoch = geodeticPosition.size() - 1;
            tvResultEpoch.setText(String.valueOf(epoch));

        }else{
            showToast("Error.  Only " + geodeticPosition.size() + " available.",true);
        }

    }

    private void findAverageGridPosition(ArrayList<Location> geodeticPosition){
        Log.d(TAG, "findAverageGridPosition: Started");

        int intPointNo = 0;
        String stPointDesc = "Avg";


        if(geodeticPosition.size() > 1){
            initProjection();

            for(Location location : geodeticPosition){

                PointGeodetic pointGeodetic = new PointGeodetic();
                pointGeodetic.setPoint_no(intPointNo);
                pointGeodetic.setDescription(stPointDesc);

                pointGeodetic.setLatitude(location.getLatitude());
                pointGeodetic.setLongitude(location.getLongitude());

                lstPointGeodetic.add(pointGeodetic);
            }

            lstPointGrid = surveyProjectionHelper.generateGridPoints(lstPointGeodetic);

            List<Double> aNorthingList = new ArrayList<>();
            List<Double> aEastingList = new ArrayList<>();


            for(PointSurvey pointSurvey : lstPointGrid){
                aNorthingList.add(pointSurvey.getNorthing());
                aEastingList.add(pointSurvey.getEasting());

            }
            Double meanNorthing = 0.0, meanEasting = 0.0;

            MathHelper mathHelper = new MathHelper(mContext);

            meanNorthing = mathHelper.getMeanDouble(aNorthingList);
            meanEasting = mathHelper.getMeanDouble(aEastingList);

            mAverageGridPoint = new PointSurvey();
            mAverageGridPoint.setNorthing(meanNorthing);
            mAverageGridPoint.setEasting(meanEasting);

            mAverageLocalPoint = findAverageLocalPosition(mAverageGridPoint);

            DecimalFormat df2 = StringUtilityHelper.createUSNonBiasDecimalFormatSelect(2);
            tvResultGridNorth.setText(df2.format(meanNorthing));
            tvResultGridEast.setText(df2.format(meanEasting));

            tvResultLocalNorth.setText(df2.format(mAverageLocalPoint.getNorthing()));
            tvResultLocalEast.setText(df2.format(mAverageLocalPoint.getEasting()));

            DecimalFormat df = StringUtilityHelper.createUSNonBiasDecimalFormatSelect(1);

            double aStdDev = Math.sqrt(Math.pow(mathHelper.getStdDevDouble(aNorthingList),2) + Math.pow(mathHelper.getStdDevDouble(aEastingList),2));
            double aVariance = Math.sqrt(Math.pow(mathHelper.getVarianceDouble(aNorthingList),2) + Math.pow(mathHelper.getVarianceDouble(aNorthingList),2));

            switch (preferenceLoaderHelper.getGeneral_over_units()){
                case 0:  //US Feet
                    aStdDev = GPSLocationConverter.convertMetersToValue(aStdDev,1);
                    aVariance = GPSLocationConverter.convertMetersToValue(aVariance,1);
                    break;

                case 1: //Int. Feet
                    aStdDev = GPSLocationConverter.convertMetersToValue(aStdDev,2);
                    aVariance = GPSLocationConverter.convertMetersToValue(aVariance,1);
                    break;

                case 2: //Meters
                    aStdDev = GPSLocationConverter.convertMetersToValue(aStdDev,3);
                    aVariance = GPSLocationConverter.convertMetersToValue(aVariance,1);
                    break;
            }

            tvResultStdDev.setText(df.format(aStdDev));
            tvResultVariance.setText(df.format(aVariance));
        }else{
            showToast("Error.  Only " + lstPointGrid.size() + " available.",true);
        }

    }

    private PointSurvey findAverageLocalPosition(PointSurvey gridPosition){
        Log.d(TAG, "findAverageLocalPosition: Started");

        PointSurvey results = new PointSurvey();
        initScaleToGround();

        double deltaNorthFull = (gridPosition.getNorthing() - mOriginNorth);
        double deltaEastFull = (gridPosition.getEasting() - mOriginEast);

        double azimuthRadians = Math.atan(deltaEastFull/deltaNorthFull);

        double inverseFull = Math.abs(Math.pow(deltaNorthFull,2) + Math.pow(deltaEastFull,2));
        double inverseScaled = inverseFull * mScaleFactor;

        double deltaNorthScale = inverseScaled * Math.cos(azimuthRadians);
        double deltaEastScale = inverseScaled * Math.sin(azimuthRadians);

        if(mScaleFactor == 1){
            results.setNorthing(gridPosition.getNorthing());
            results.setEasting(gridPosition.getEasting());

        }else if(mScaleFactor < 1){
            results.setNorthing(mOriginNorth - deltaNorthScale);
            results.setEasting(mOriginEast - deltaEastScale);

        }else if(mScaleFactor > 1){
            results.setNorthing(mOriginNorth + deltaNorthScale);
            results.setEasting(mOriginEast + deltaEastScale);

        }


        return results;

    }


    private boolean findAverageOrthoFromInternet(){
        Log.d(TAG, "findAverageOrthoFromInternet: Started");

        boolean results = false;


        int elevationServiceToUse = 1;

        switch (elevationServiceToUse) {
            case 0:
                //none
                break;

            case 1: //https://nationalmap.gov/epqs/
                BackgroundGetNationalMapElevation backgroundGetNationalMapElevation = new BackgroundGetNationalMapElevation(mContext, mAverageLocation, this,POI_TARGET);
                backgroundGetNationalMapElevation.execute();
                break;

            case 2: //http://www.geonames.org/export/web-services.html#srtm3
                BackgroundGetSRTMElevation backgroundGetSRTMElevation = new BackgroundGetSRTMElevation(mContext, mAverageLocation, this);
                backgroundGetSRTMElevation.execute();
                break;
        }


        return results;

    }

    @Override
    public void getNationalMapResults(String results, String units, String source, Integer var) {
        Log.d(TAG, "getNationalMapResults: Started");

        DecimalFormat df1 = StringUtilityHelper.createUSNonBiasDecimalFormatSelect(1);

        switch(var){
            case POI_TARGET:
                tvResultOrtho.setText(df1.format(Float.parseFloat(results)));
                progressOrtho.setVisibility(View.GONE);
                tvResultOrtho.setVisibility(View.VISIBLE);

                break;

            case POI_CURRENT:

                break;
        }



    }

    @Override
    public void getResultSRTM(String results) {
        Log.d(TAG, "getResultSRTM: Started");

        DecimalFormat df1 = StringUtilityHelper.createUSNonBiasDecimalFormatSelect(1);
        tvResultOrtho.setText(df1.format(Float.parseFloat(results)));
        progressOrtho.setVisibility(View.GONE);
        tvResultOrtho.setVisibility(View.VISIBLE);
    }

    private void initProjection(){
        Log.d(TAG, "initProjection: Started");
        String projectionString, zoneString;

        int isProjection = 0;

        isProjection = preferenceLoaderHelper.getGeneral_over_projection();
        projectionString = preferenceLoaderHelper.getGeneral_over_projection_string();
        zoneString = preferenceLoaderHelper.getGeneral_over_zone_string();

        if(isProjection == 1){
            Log.d(TAG, "initProjection: With Projection");
            surveyProjectionHelper = new SurveyProjectionHelper(mContext);
            surveyProjectionHelper.setConfig(projectionString,zoneString);
            isJobWithProjection = true;
        }

    }

    private void initScaleToGround(){
        Log.d(TAG, "initScaleToGround: Started");

        mScaleFactor = preferenceLoaderHelper.getDefaultProjectProjectionScaleGridToGround();
        mOriginNorth = preferenceLoaderHelper.getDefaultProjectProjectionOriginNorth();
        mOriginEast = preferenceLoaderHelper.getDefaultProjectProjectionOriginEast();

    }

    //---------------------------------------------------------------------------------------------- View Controls
    private void showHideResults(){
        Log.d(TAG, "showHideResults: Started");

        if (!isResultsShown){
            rlResultsView.setVisibility(View.VISIBLE);

            nsResultsContainer.post(new Runnable() {
                @Override
                public void run() {
                    nsResultsContainer.smoothScrollTo(0,llResultsContainer.getBottom());
                }
            });

            btResultsShow.setText(getResources().getString(R.string.general_results_close));
            isResultsShown = true;
        }else{
            nsResultsContainer.post(new Runnable() {
                @Override
                public void run() {
                    nsResultsContainer.smoothScrollTo(0,llResultsContainer.getTop());
                }
            });
            rlResultsView.setVisibility(View.GONE);
            btResultsShow.setText(getResources().getString(R.string.general_results_open));
            isResultsShown = false;
        }

    }

    private void closeDialog(){
        dismiss();
    }

    //---------------------------------------------------------------------------------------------- Dataset out
    private void saveMeasurements(){
        Log.d(TAG, "saveMeasurements: Started");
        boolean validated = false;

        validated = validateDataset();



    }

    private boolean validateDataset(){
        Log.d(TAG, "validateDataset: Started");
        boolean results = false;







        return results;


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
