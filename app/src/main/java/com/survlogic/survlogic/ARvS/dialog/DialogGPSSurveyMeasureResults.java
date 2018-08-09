package com.survlogic.survlogic.ARvS.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.TextInputLayout;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.survlogic.survlogic.ARvS.background.BackgroundGPSPointReserve;
import com.survlogic.survlogic.ARvS.background.BackgroundGPSPointUpdate;
import com.survlogic.survlogic.ARvS.interf.GPSPointBackgroundGetters;
import com.survlogic.survlogic.ARvS.interf.GetNationalMapElevationListener;
import com.survlogic.survlogic.ARvS.interf.PopupDialogBoxListener;
import com.survlogic.survlogic.PhotoEditor.interf.DialogPointPhotoAddV2Listener;
import com.survlogic.survlogic.R;
import com.survlogic.survlogic.background.BackgroundGetNationalMapElevation;
import com.survlogic.survlogic.background.BackgroundGetSRTMElevation;
import com.survlogic.survlogic.camera.CaptureImageActivity;
import com.survlogic.survlogic.camera.util.BitmapUtils;
import com.survlogic.survlogic.database.JobDatabaseHandler;
import com.survlogic.survlogic.PhotoEditor.DialogPointPhotoAddV2;
import com.survlogic.survlogic.model.PointGeodetic;
import com.survlogic.survlogic.model.PointSurvey;
import com.survlogic.survlogic.utils.FileHelper;
import com.survlogic.survlogic.utils.GPSLocationConverter;
import com.survlogic.survlogic.utils.MathHelper;
import com.survlogic.survlogic.utils.PreferenceLoaderHelper;
import com.survlogic.survlogic.utils.StringUtilityHelper;
import com.survlogic.survlogic.utils.SurveyMathHelper;
import com.survlogic.survlogic.utils.SurveyProjectionHelper;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import mehdi.sakout.fancybuttons.FancyButton;

/**
 * Created by chrisfillmore on 7/22/2017.
 */

public class DialogGPSSurveyMeasureResults extends DialogFragment implements    GetNationalMapElevationListener, GPSPointBackgroundGetters,
                                                                                DialogPointPhotoAddV2Listener {
    private static final String TAG = "DialogProjectDescr";
    private Context mContext;

    private PopupDialogBoxListener listener;

    private PreferenceLoaderHelper preferenceLoaderHelper;
    private SurveyProjectionHelper surveyProjectionHelper;
    private BitmapUtils bitmapUtils;

    private FileHelper fileHelper;

    //---------------------------------------------------------------------------------------------- In Variables
    private ArrayList<Location> measuredResults;
    private String mJob_DatabaseName, mCurrentPhotoPath;
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

    private TextInputLayout inputLayoutPointNumber, inputLayoutPointHeight, inputLayoutPointDescription;
    private EditText etPointNumber, etPointHeight, etPointDescription;
    private TextView tvHiddenPointNumber, tvHiddenPointHeight, tvHiddenPointDescription;

    private Button btTakePhoto, btAddSketch;

    //---------------------------------------------------------------------------------------------- Method Variables
    private boolean isJobWithProjection = false;
    private boolean isResultsShown = false;

    private ArrayList<PointGeodetic> lstPointGeodetic = new ArrayList<>();
    private ArrayList<PointSurvey> lstPointGrid = new ArrayList<>();

    private float mScaleFactor = 0.0F;
    private float mOriginNorth = 0.0F, mOriginEast = 0.0F;

    private final int POI_TARGET = 0, POI_CURRENT = 1;

    //---------------------------------------------------------------------------------------------- Camera
    private static final int REQUEST_TAKE_PHOTO = 2;
    private static final int REQUEST_SELECT_PICTURE = 3;

    private Bitmap mBitmap, mBitmapRaw;

    //---------------------------------------------------------------------------------------------- Database Methods
    private long mReservedPoint_id;
    private boolean isReserved = false;

    //---------------------------------------------------------------------------------------------- Out Variables
    private Location mAverageLocation;
    private PointSurvey mAverageGridPoint, mAverageLocalPoint;

    private Bitmap mPhotoBitmap;
    private boolean usePhotoLocation = true, usePhotoSensor = true;
    private Location mPhotoLocation;
    private int mPhotoAzimuth;


    //---------------------------------------------------------------------------------------------- CODE Start

    public static DialogGPSSurveyMeasureResults newInstance(ArrayList<Location> measured, String dbName, int pointNumber, double instHeight, PopupDialogBoxListener listener) {
        Log.d(TAG, "newInstance: showResultsDialog:" + measured.size());
        DialogGPSSurveyMeasureResults frag = new DialogGPSSurveyMeasureResults();
        Bundle args = new Bundle();

        args.putParcelableArrayList("list",measured);
        frag.setArguments(args);

        frag.listener = listener;

        frag.measuredResults = measured;
        frag.mJob_DatabaseName = dbName;
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

        fileHelper = new FileHelper(mContext);
        bitmapUtils = new BitmapUtils(mContext);

        preferenceLoaderHelper = new PreferenceLoaderHelper(mContext);
        loadPreferences();

        initViewWidgets();
        initFocusChangeListeners();
        setOnClickListeners();

        reserveSpotInJobDatabase();

        loadDataset();

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (REQUEST_TAKE_PHOTO == requestCode && resultCode == Activity.RESULT_OK) {
            File file = (File) data.getExtras().get(getString(R.string.KEY_IMAGE_FILE));
            Uri imageUri = Uri.fromFile(file);


            usePhotoLocation = data.getBooleanExtra(getResources().getString(R.string.KEY_POSITION_USE),true);
            double latitude, longitude, ellipsoid;
            float accuracy;

            if(usePhotoLocation){
                latitude = data.getDoubleExtra(getResources().getString(R.string.KEY_POSITION_LATITUDE),0);
                longitude = data.getDoubleExtra(getResources().getString(R.string.KEY_POSITION_LONGITUDE),0);
                ellipsoid = data.getDoubleExtra(getResources().getString(R.string.KEY_POSITION_ELLIPSOID),0);
                accuracy = data.getFloatExtra(getResources().getString(R.string.KEY_POSITION_ACCURACY),0);

                mPhotoLocation = new Location("");
                mPhotoLocation.setLatitude(latitude);
                mPhotoLocation.setLongitude(longitude);
                mPhotoLocation.setAltitude(ellipsoid);
                mPhotoLocation.setAccuracy(accuracy);
            }

            usePhotoSensor = data.getBooleanExtra(getResources().getString(R.string.KEY_SENSOR_USE),true);
            mPhotoAzimuth = data.getIntExtra(getResources().getString(R.string.KEY_SENSOR_AZIMUTH),0);

            try {
                Bitmap mBitmapRaw=bitmapUtils.decodeUri(imageUri,400);
                mPhotoBitmap = bitmapUtils.rotateImageIfRequired(mBitmapRaw,imageUri);

                createPhotoDialog();

            } catch (Exception e) {
                Log.e(TAG, "onActivityResult: Caught Error:  Could not set File to Image from Camera");

            }
        } else if (REQUEST_SELECT_PICTURE == requestCode && resultCode == Activity.RESULT_OK) {
            if (data != null) {

                try {
                    final Uri imageUri = data.getData();
                    File file = new File(imageUri.getPath());

                    if (imageUri !=null){
                        mBitmapRaw=bitmapUtils.decodeUri(imageUri,400);
                        mBitmap = bitmapUtils.rotateImageIfRequired(mBitmapRaw,imageUri);
                        //Todo
                        //createPhotoDialog(mBitmap);

                    }


                } catch (Exception e) {
                    showToast("Caught Error: Could not set Photo to Image from Gallery",true);

                }
            }
        }


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
                android.support.v7.app.AlertDialog dialog = dialogCloseActivity();
                dialog.show();

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
        etPointDescription.requestFocus();

        inputLayoutPointNumber = getDialog().findViewById(R.id.dialog_item_header_pointno);
        inputLayoutPointHeight = getDialog().findViewById(R.id.dialog_item_header_height);
        inputLayoutPointDescription = getDialog().findViewById(R.id.dialog_item_header_description);

        btTakePhoto = (Button) getDialog().findViewById(R.id.card3_take_photo);
        btAddSketch = (Button) getDialog().findViewById(R.id.card4_add_sketch);

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

    private void setOnClickListeners() {
        Log.d(TAG, "setOnClickListeners: Started...");

        btTakePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callImageSelectionDialog();
            }
        });

        btAddSketch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //callAddNewSketch();
            }
        });
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
                mAverageLocalPoint.setElevation(Float.parseFloat(results));

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
    private void reserveSpotInJobDatabase(){
        Log.d(TAG, "savePointInitial: Started");

        PointGeodetic pointGeodeticReserve = new PointGeodetic();
        pointGeodeticReserve.setPoint_no(pointNumber);

        BackgroundGPSPointReserve backgroundGPSPointReserve = new BackgroundGPSPointReserve(mContext, mJob_DatabaseName, this);
        backgroundGPSPointReserve.execute(pointGeodeticReserve);
    }

    @Override
    public void getReservedPointID(long point_id) {
        mReservedPoint_id = point_id;
        isReserved = true;

    }

    private void saveMeasurements(){
        Log.d(TAG, "saveMeasurements: Started");
        boolean validated = false;

        if(isReserved && validateEntry()){
            BackgroundGPSPointUpdate backgroundGPSPointUpdate = new BackgroundGPSPointUpdate(mContext,pointNumber,mJob_DatabaseName,this);
            backgroundGPSPointUpdate.execute(createPointGeodetic());
        }else{
            if(!isReserved){
                showToast("Error Reserving Point!",true);
            }
        }
    }

    private boolean validateEntry(){
        Log.d(TAG, "validateEntry: Starting...");
        boolean results;


        results =  true;
        if (etPointNumber.getText().toString().isEmpty()){
            Log.d(TAG, "validateEntry: No Point Number");
            inputLayoutPointNumber.setError(getString(R.string.dialog_job_point_item_error_pointNo));
            return false;

        }else {
            inputLayoutPointNumber.setError(null);

        }


        if (etPointHeight.getText().toString().isEmpty()){
            Log.d(TAG, "validateEntry: Point Northing not found");
            inputLayoutPointHeight.setError(getString(R.string.dialog_job_point_item_error_pointHeight));
            return false;

        }else {
            inputLayoutPointHeight.setError(null);

        }


        if (etPointDescription.getText().toString().isEmpty()){
            Log.d(TAG, "validateEntry: Point Description not found");
            inputLayoutPointDescription.setError(getString(R.string.dialog_job_point_item_error_pointDescription));
            return false;

        }else {
            setPointDescription();
            inputLayoutPointDescription.setError(null);

        }


        return results;

    }

    private void setPointDescription(){
        Log.d(TAG, "setPointDescription: Started");

        mAverageLocalPoint.setDescription(etPointDescription.getText().toString());

    }

    private PointGeodetic createPointGeodetic(){
        Log.d(TAG, "createPointGeodetic: Started...");

        PointGeodetic pointGeodetic = new PointGeodetic();

        pointGeodetic.setPoint_no(pointNumber);
        pointGeodetic.setNorthing(mAverageLocalPoint.getNorthing());
        pointGeodetic.setEasting(mAverageLocalPoint.getEasting());
        pointGeodetic.setElevation(mAverageLocalPoint.getElevation());

        pointGeodetic.setDescription(mAverageLocalPoint.getDescription());

        pointGeodetic.setLatitude(mAverageLocation.getLatitude());
        pointGeodetic.setLongitude(mAverageLocation.getLongitude());
        pointGeodetic.setEllipsoid(mAverageLocation.getAltitude());
        pointGeodetic.setOrtho(mAverageLocalPoint.getElevation());


        pointGeodetic.setPointType(1);
        pointGeodetic.setPointGeodeticType(3);


        Log.d(TAG, "createPointGeodetic: Finished creating pointGeodetic");
        return pointGeodetic;
    }

    @Override
    public void isPointSaveASuccess(boolean isSuccess) {

        listener.onSave();
        closeDialog();

    }

    private void cancelBeforeSaveMeasurement(){
        Log.d(TAG, "cancelBeforeSaveMeasurement: Started");


        boolean isDeleted = deletePoint();

        if(isDeleted){
            //Todo
            showToast("Success in Deleting", true);
        }else{
            showToast("Failure in Deleting", true);
        }

        closeDialog();

    }

    private boolean deletePoint(){
        Log.d(TAG, "cancelingBeforeSaving: Started");
        boolean isSuccess = false;

            JobDatabaseHandler jobDb = new JobDatabaseHandler(mContext, mJob_DatabaseName);
            SQLiteDatabase db = jobDb.getWritableDatabase();

            try {
                jobDb.deletePointByPointNo(db, pointNumber);
                isSuccess = true;
            }catch (Exception ex){
                Log.d(TAG, "cancelingBeforeSaving: Error Deleting point");
            }

            db.close();

            return isSuccess;
    }

    //-------------------------------------------------------------------------------------------------------------------------//Photo

    private void callImageSelectionDialog(){
        Log.d(TAG, "callImageSelectionDialog: Started...");
        final CharSequence[] items = { getString(R.string.project_new_dialog_takePhoto), getString(R.string.project_new_dialog_getImage),
                getString(R.string.general_cancel) };

        TextView title = new TextView(getActivity());  //was context not this

        title.setText(getString(R.string.photo_dialog_title_point));
        title.setBackgroundColor(Color.WHITE);
        title.setPadding(10, 15, 15, 10);
        title.setGravity(Gravity.CENTER);
        title.setTextColor(Color.BLACK);
        title.setTextSize(22);

        android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(getActivity());

        builder.setCustomTitle(title);

        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int arg) {

                switch (arg) {

                    case 0: //Camera
                        startCamera();
                        break;

                    case 1: //Photo
                        startPhotoGallery();
                        break;

                    case 2: //Cancel

                        dialog.dismiss();
                        break;
                }

            }
        });
        builder.show();
    }

    private void startCamera() {
        try {
            //dispatchTakePictureIntent();

            dispatchCameraIntent();

        } catch (IOException e) {
            showToast("Caught Error: Accessing Camera Exception",true);
        }
    }

    private void startPhotoGallery(){
        Log.d(TAG, "startPhotoGallery: Started...");
        try{
            dispatchPhotoFromGalleryIntent();

        } catch (IOException e){
            showToast("caught Error: Accessing Gallery Exception",true);
        }

    }

    private void dispatchCameraIntent()throws IOException {
        Log.d(TAG, "dispatchCameraIntent: Started");

        Intent takePictureIntent = new Intent(getActivity(), CaptureImageActivity.class);
        startActivityForResult(takePictureIntent,REQUEST_TAKE_PHOTO);
    }


    private void dispatchPhotoFromGalleryIntent() throws IOException{
        Log.d(TAG, "dispatchPhotoFromGalleryIntent: Started...");
        Intent intent = new Intent(Intent.ACTION_PICK,android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Select Picture"),REQUEST_SELECT_PICTURE);

    }

    private void createPhotoDialog(){
        Log.d(TAG, "createPhotoDialog: Started");
        DialogPointPhotoAddV2 photoDialogV2 = DialogPointPhotoAddV2.newInstance(this);
        photoDialogV2.show(getFragmentManager(),"photoDialog");

    }

    //---------------------------------------------------------------------------------------------- Photo Dialog listener

    @Override
    public void requestPhotoRefresh() {

    }

    @Override
    public int getPointNumber() {
        return pointNumber;
    }

    @Override
    public boolean isUsePhotoLocation() {
        return usePhotoLocation;
    }

    @Override
    public boolean isUsePhotoSensor() {
        return usePhotoSensor;
    }

    @Override
    public Bitmap getPhoto() {
        return mPhotoBitmap;
    }

    @Override
    public Location getPhotoLocation() {
        return mPhotoLocation;
    }

    @Override
    public int getPhotoAzimuth() {
        return mPhotoAzimuth;
    }

    //---------------------------------------------------------------------------------------------- Helpers

    private android.support.v7.app.AlertDialog dialogCloseActivity(){

        android.support.v7.app.AlertDialog myDialogBox = new android.support.v7.app.AlertDialog.Builder(mContext)
                .setTitle(getResources().getString(R.string.dialog_point_view_delete_point_title))
                .setMessage(getResources().getString(R.string.dialog_point_view_delete_point_description))
                .setPositiveButton(getResources().getString(R.string.general_yes), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        progressBar.setVisibility(View.VISIBLE);
                        deletePoint();
                        dialog.dismiss();
                        cancelBeforeSaveMeasurement();
                    }
                })
                .setNegativeButton(getResources().getString(R.string.general_no), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })

                .create();

        return myDialogBox;
    }



    private void showToast(String data, boolean shortTime) {

        if (shortTime) {
            Toast.makeText(mContext, data, Toast.LENGTH_SHORT).show();

        } else{
            Toast.makeText(mContext, data, Toast.LENGTH_LONG).show();

        }
    }

}
