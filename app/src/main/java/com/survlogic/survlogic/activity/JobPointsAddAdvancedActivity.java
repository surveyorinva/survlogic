package com.survlogic.survlogic.activity;

import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.survlogic.survlogic.R;
import com.survlogic.survlogic.background.BackgroundPointGeodeticNew;
import com.survlogic.survlogic.database.JobDatabaseHandler;
import com.survlogic.survlogic.dialog.DialogJobPointGeodeticEntryAdd;
import com.survlogic.survlogic.dialog.DialogJobPointGridEntryAdd;
import com.survlogic.survlogic.interf.PointGeodeticEntryListener;
import com.survlogic.survlogic.model.Point;
import com.survlogic.survlogic.model.PointGeodetic;
import com.survlogic.survlogic.model.PointSurvey;
import com.survlogic.survlogic.utils.SurveyMathHelper;
import com.survlogic.survlogic.utils.PreferenceLoaderHelper;
import com.survlogic.survlogic.utils.StringUtilityHelper;
import com.survlogic.survlogic.utils.SurveyProjectionHelper;

import java.text.DecimalFormat;

/**
 * Created by chrisfillmore on 8/13/2017.
 */

public class JobPointsAddAdvancedActivity extends AppCompatActivity implements PointGeodeticEntryListener {
    private static final String TAG = "JobPointsAddAdvancedAct";

    private static final int REQUEST_GET_GPS = 1;
    private static final int REQUEST_TAKE_PHOTO = 2;
    private static final int REQUEST_SELECT_PICTURE = 3;

    private Context mContext;

    private SharedPreferences sharedPreferences;
    private PreferenceLoaderHelper preferenceLoaderHelper;
    private SurveyProjectionHelper surveyProjectionHelper;

    private static DecimalFormat COORDINATE_FORMATTER, DISTANCE_PRECISION_FORMATTER;

    private PointSurvey pointSurvey;

    private View vGPSOnly, vProjection;

    private TextInputLayout inputLayoutPointNumber, inputLayoutPointNorthing, inputLayoutPointEasting,
            inputLayoutPointElevation, inputLayoutPointDescription;

    private TextView tvLocation_latitude, tvLocation_longitude, tvLocation_latitude_value,tvLocation_longitude_value,
            tvLocation_height, tvLocation_height_value, tvLocation_ortho, tvLocation_ortho_value;

    private TextView tvLocation_grid_north, tvLocation_grid_north_value, tvLocation_grid_east, tvLocation_grid_east_value;

    private TextView tvHiddenPointNumber, tvHiddenPointNorthing, tvHiddenPointEasting,
            tvHiddenPointElevation, tvHiddenPointDescription;

    private EditText etPointNumber, etPointNorthing, etPointEasting, etPointElevation, etPointDescription;
    private Button btCancel, btSave;
    private ImageButton btGPSAdd, btGPSEntryAdd, btGridAdd;

    private int pointNumber, pointType, projectId, jobId;
    private double pointNorthing,pointEasting,pointElevation;
    private String pointDescription, jobDbName;

    double mLocationLat = 0, mLocationLong = 0, mLocationEllipsoid = 0, mLocationOrtho = 0, mLocationAccuracy = 0;
    double mLocationGridNorth = 0, mLocationGridEast = 0;


    private boolean isGeodeticManualEntry = false, isGridManualEntry = false;
    private boolean isJobWithProjection = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: Started...");
        setContentView(R.layout.activity_job_point_entry_add);

        mContext = JobPointsAddAdvancedActivity.this;
        preferenceLoaderHelper = new PreferenceLoaderHelper(mContext);
        surveyProjectionHelper = new SurveyProjectionHelper(mContext);

        initViewWidgets();
        setOnClickListeners();
        initFocusChangeListeners();

        loadPreferences();

        populateValues();

    }

    private void initViewWidgets(){
        Log.d(TAG, "initViewWidgets: Started...");

        vGPSOnly = findViewById(R.id.layout_area_3_gps_only);
        vProjection = findViewById(R.id.layout_area_3_projection);

        inputLayoutPointNumber = (TextInputLayout) findViewById(R.id.dialog_item_header_pointno);
        inputLayoutPointNorthing = (TextInputLayout) findViewById(R.id.dialog_item_header_northing);
        inputLayoutPointEasting = (TextInputLayout) findViewById(R.id.dialog_item_header_easting);
        inputLayoutPointElevation = (TextInputLayout) findViewById(R.id.dialog_item_header_elevation);
        inputLayoutPointDescription = (TextInputLayout) findViewById(R.id.dialog_item_header_description);

        etPointNumber = (EditText) findViewById(R.id.dialog_item_pointNo);
        etPointNumber.setSelectAllOnFocus(true);

        etPointNorthing = (EditText) findViewById(R.id.dialog_item_northing);
        etPointNorthing.setSelectAllOnFocus(true);

        etPointEasting = (EditText) findViewById(R.id.dialog_item_easting);
        etPointEasting.setSelectAllOnFocus(true);

        etPointElevation = (EditText) findViewById(R.id.dialog_item_elevation);
        etPointElevation.setSelectAllOnFocus(true);

        etPointDescription = (EditText) findViewById(R.id.dialog_item_description);
        etPointDescription.setSelectAllOnFocus(true);

        tvHiddenPointNumber = (TextView) findViewById(R.id.dialog_item_header_pointno_hint);
        tvHiddenPointNorthing = (TextView) findViewById(R.id.dialog_item_header_northing_hint);
        tvHiddenPointEasting = (TextView) findViewById(R.id.dialog_item_header_easting_hint);
        tvHiddenPointElevation = (TextView) findViewById(R.id.dialog_item_header_elevation_hint);
        tvHiddenPointDescription = (TextView) findViewById(R.id.dialog_item_header_description_hint);

        tvLocation_latitude = (TextView) findViewById(R.id.location_latitude);
        tvLocation_latitude_value = (TextView) findViewById(R.id.location_latitude_value);

        tvLocation_longitude = (TextView) findViewById(R.id.location_longitude);
        tvLocation_longitude_value = (TextView) findViewById(R.id.location_longitude_value);

        tvLocation_height = (TextView) findViewById(R.id.location_height);
        tvLocation_height_value = (TextView) findViewById(R.id.location_height_value);

        tvLocation_ortho = (TextView) findViewById(R.id.location_ortho);
        tvLocation_ortho_value = (TextView) findViewById(R.id.location_ortho_value);

        tvLocation_grid_north = (TextView) findViewById(R.id.location_grid_north);
        tvLocation_grid_north_value = (TextView) findViewById(R.id.location_grid_north_value);

        tvLocation_grid_east= (TextView) findViewById(R.id.location_grid_east);
        tvLocation_grid_east_value= (TextView) findViewById(R.id.location_grid_east_value);

        btSave = (Button) findViewById(R.id.Save_button);
        btCancel = (Button) findViewById(R.id.Cancel_button);

        btGPSAdd = (ImageButton) findViewById(R.id.location_get_from_gps_survey);
        btGPSEntryAdd = (ImageButton) findViewById(R.id.location_get_from_user_input);
        btGridAdd = (ImageButton) findViewById(R.id.grid_get_from_user_input);
    }

    private void setOnClickListeners(){
        Log.d(TAG, "setOnClickListeners: Started...");

        btSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitForm(v);
            }
        });

        btCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                overridePendingTransition(R.anim.anim_activity_stay, R.anim.anim_activity_slide_down);
            }
        });

        btGPSAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToGpsSurveyActivity();
            }
        });

        btGPSEntryAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToDialogCoordinateValues();
            }
        });

        btGridAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToDialogGrid();
            }
        });


    }

    private void initFocusChangeListeners(){
        Log.d(TAG, "initFocusChangeListeners: Started...");

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
                    if (etPointNumber.getText().length() > 0)
                        tvHiddenPointNumber.setVisibility(View.VISIBLE);
                    else
                        tvHiddenPointNumber.setVisibility(View.INVISIBLE);
                }
            }
        });

        etPointNorthing.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {

                if (hasFocus) {
                    new Handler().postDelayed(new Runnable() {

                        @Override
                        public void run() {
                            // Show white background behind floating label
                            tvHiddenPointNorthing.setVisibility(View.VISIBLE);
                        }
                    }, 100);
                } else {
                    // Required to show/hide white background behind floating label during focus change
                    if (etPointNorthing.getText().length() > 0) {
                        tvHiddenPointNorthing.setVisibility(View.VISIBLE);
                    }else {
                        tvHiddenPointNorthing.setVisibility(View.INVISIBLE);
                    }

                    //Add Correct Decimal Places After
                    try{
                        String stringValue = null;
                        stringValue = etPointNorthing.getText().toString();

                        if(!StringUtilityHelper.isStringNull(stringValue)){
                            double value = Double.parseDouble(stringValue);

                            etPointNorthing.setText(COORDINATE_FORMATTER.format(value));
                        }

                    }catch(NumberFormatException ex){
                        showToast("Error.  Check Number Format", true);

                    }
                }
            }
        });

        etPointEasting.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {

                if (hasFocus) {
                    new Handler().postDelayed(new Runnable() {

                        @Override
                        public void run() {
                            // Show white background behind floating label
                            tvHiddenPointEasting.setVisibility(View.VISIBLE);
                        }
                    }, 100);
                } else {
                    // Required to show/hide white background behind floating label during focus change
                    if (etPointEasting.getText().length() > 0) {
                        tvHiddenPointEasting.setVisibility(View.VISIBLE);
                    }else{
                        tvHiddenPointEasting.setVisibility(View.INVISIBLE);}

                    //Add Correct Decimal Places After
                    try{
                        String stringValue = null;
                        stringValue = etPointEasting.getText().toString();

                        if(!StringUtilityHelper.isStringNull(stringValue)){
                            double value = Double.parseDouble(stringValue);

                            etPointEasting.setText(COORDINATE_FORMATTER.format(value));
                        }

                    }catch(NumberFormatException ex){
                        showToast("Error.  Check Number Format", true);

                    }


                }
            }
        });

        etPointElevation.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {

                if (hasFocus) {
                    new Handler().postDelayed(new Runnable() {

                        @Override
                        public void run() {
                            // Show white background behind floating label
                            tvHiddenPointElevation.setVisibility(View.VISIBLE);
                        }
                    }, 100);
                } else {
                    // Required to show/hide white background behind floating label during focus change
                    if (etPointElevation.getText().length() > 0) {
                        tvHiddenPointElevation.setVisibility(View.VISIBLE);
                    }else{
                        tvHiddenPointElevation.setVisibility(View.INVISIBLE);}


                    //Add Correct Decimal Places After
                    try{
                        String stringValue = null;
                        stringValue = etPointElevation.getText().toString();

                        if(!StringUtilityHelper.isStringNull(stringValue)){
                            double value = Double.parseDouble(stringValue);

                            etPointElevation.setText(COORDINATE_FORMATTER.format(value));
                        }

                    }catch(NumberFormatException ex){
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
                    if (etPointDescription.getText().length() > 0)
                        tvHiddenPointDescription.setVisibility(View.VISIBLE);
                    else
                        tvHiddenPointDescription.setVisibility(View.INVISIBLE);
                }
            }
        });
    }

    private void populateValues(){
        Log.d(TAG, "populateValues: Started...");

        Bundle extras = getIntent().getExtras();
        projectId = extras.getInt("PROJECT_ID");
        jobId = extras.getInt("JOB_ID");
        jobDbName = extras.getString("JOB_DB_NAME");

        Log.d(TAG, "populateValues: Database Name:" + jobDbName + " Loaded...");

        pointSurvey = getIntent().getParcelableExtra("POINT_ENTRY");
        pointNumber = pointSurvey.getPoint_no();
        pointNorthing = pointSurvey.getNorthing();
        pointEasting = pointSurvey.getEasting();
        pointElevation = pointSurvey.getElevation();
        pointDescription = pointSurvey.getDescription();
        pointType = pointSurvey.getPointType();

        if(pointNumber !=0){
            etPointNumber.setText(String.valueOf(pointNumber));
        }

        if(pointNorthing != 0){
            etPointNorthing.setText(String.valueOf(pointNorthing));
        }

        if (pointEasting !=0) {
            etPointEasting.setText(String.valueOf(pointEasting));
        }

        if (pointEasting !=0){
            etPointElevation.setText(String.valueOf(pointElevation));
        }

        if(!StringUtilityHelper.isStringNull(pointDescription)){
            etPointDescription.setText(String.valueOf(pointDescription));
        }

    }

    private void goToGpsSurveyActivity(){
        Log.d(TAG, "goToGpsSurveyActivity: Started...");
        Intent intent = new Intent(this, GpsSurveyActivity.class);
        startActivityForResult(intent,REQUEST_GET_GPS);

    }

    private void goToDialogCoordinateValues(){
        Log.d(TAG, "goToDialogCoordinateValues: Starting...");
        DialogFragment viewDialog = DialogJobPointGeodeticEntryAdd.newInstance(mLocationLat, mLocationLong, mLocationEllipsoid, mLocationOrtho);
        viewDialog.show(getFragmentManager(),"dialog_geodetic");
    }

    private void goToDialogGrid(){
        Log.d(TAG, "goToDialogGrid: Started");
        DialogFragment viewDialog = DialogJobPointGridEntryAdd.newInstance(mLocationGridNorth, mLocationGridEast);
        viewDialog.show(getFragmentManager(),"dialog_grid");


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult: Started...");

        if (this.REQUEST_GET_GPS == requestCode && resultCode == RESULT_OK) {
            Log.d(TAG, "onActivityResult: Starting...");

            mLocationLat = data.getDoubleExtra(getString(R.string.KEY_POSITION_LATITUDE), 0);
            mLocationLong = data.getDoubleExtra(getString(R.string.KEY_POSITION_LONGITUDE), 0);

            mLocationEllipsoid = data.getDoubleExtra(getString(R.string.KEY_POSITION_ELLIPSOID), 0);
            mLocationOrtho = data.getDoubleExtra(getString(R.string.KEY_POSITION_ORTHO), 0);

            isGeodeticManualEntry = false;

            populateWorldValues();

            if(isJobWithProjection){
                findGridCoordinatesFromWorld();
            }

        }

    }

    private void loadPreferences(){
        Log.d(TAG, "loadPreferences: Started...");


        COORDINATE_FORMATTER = new DecimalFormat(preferenceLoaderHelper.getValueSystemCoordinatesPrecisionDisplay());
        DISTANCE_PRECISION_FORMATTER = new DecimalFormat(preferenceLoaderHelper.getValueSystemDistancePrecisionDisplay());

        initProjection();

    }

    //----------------------------------------------------------------------------------------------//
    private void initProjection(){
        Log.d(TAG, "initProjection: Started");
        String projectionString, zoneString;

        int isProjection = 0;

        isProjection = preferenceLoaderHelper.getGeneral_over_projection();
        projectionString = preferenceLoaderHelper.getGeneral_over_projection_string();
        zoneString = preferenceLoaderHelper.getGeneral_over_zone_string();

        if(isProjection == 1){
            Log.d(TAG, "initProjection: With Projection");
            surveyProjectionHelper.setConfig(projectionString,zoneString);
            isJobWithProjection = true;
            vProjection.setVisibility(View.VISIBLE);
        }

    }

    private void findGridCoordinatesFromWorld(){
        Log.d(TAG, "findGridCoordinatesFromWorld: Started");

        Point worldIN = new Point();
        worldIN.setNorthing(mLocationLat);
        worldIN.setEasting(mLocationLong);

        Point gridOUT = new Point();
        gridOUT = surveyProjectionHelper.calculateGridCoordinates(worldIN);

        mLocationGridNorth = gridOUT.getNorthing();
        mLocationGridEast = gridOUT.getEasting();
        isGridManualEntry = false;

        populateGridValues();


    }

    private void findWorldCoordinatesFromGrid(){
        Log.d(TAG, "findWorldCoordinatesFromGrid: Started");

        Point gridIN = new Point();
        gridIN.setNorthing(mLocationGridNorth);
        gridIN.setEasting(mLocationGridEast);

        Point worldOUT = new Point();
        worldOUT = surveyProjectionHelper.calculateGeodeticCoordinates(gridIN);

        mLocationLat = worldOUT.getNorthing();
        mLocationLong = worldOUT.getEasting();
        isGeodeticManualEntry = false;

        populateWorldValues();


    }


    //----------------------------------------------------------------------------------------------//
    private void submitForm(View v){

        if (validateEntry()){
            Log.d(TAG, "submitForm: Validation Approved, Saving...");
            // Setup Background Task
            BackgroundPointGeodeticNew backgroundPointGeodeticNew = new BackgroundPointGeodeticNew(mContext, jobDbName);

            // Execute background task
            backgroundPointGeodeticNew.execute(createPointGeodetic());

            finish();
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


        int point_No = Integer.parseInt(etPointNumber.getText().toString());

        Log.d(TAG, "validateEntry: Checking Database: " + jobDbName + " for " + point_No);

        JobDatabaseHandler jobDb  = new JobDatabaseHandler(mContext, jobDbName);
        SQLiteDatabase dbJob = jobDb.getReadableDatabase();

        if(jobDb.checkPointNumberExists(dbJob,point_No)){
            Log.d(TAG, "validateEntry: Point Found, prompting...");
            inputLayoutPointNumber.setError(getString(R.string.dialog_job_point_item_error_pointNoExists));
            jobDb.close();
            return false;

        }else{
            Log.d(TAG, "validateEntry: Point not found, continuing...");
            inputLayoutPointNumber.setError(null);


        }


        if (etPointNorthing.getText().toString().isEmpty()){
            Log.d(TAG, "validateEntry: Point Northing not found");
            inputLayoutPointNorthing.setError(getString(R.string.dialog_job_point_item_error_pointNorthing));
            return false;

        }else {
            inputLayoutPointNorthing.setError(null);

        }

        if (etPointEasting.getText().toString().isEmpty()){
            Log.d(TAG, "validateEntry: Point Easting not found");
            inputLayoutPointEasting.setError(getString(R.string.dialog_job_point_item_error_pointEasting));
            return false;

        }else {
            inputLayoutPointEasting.setError(null);

        }

        if (etPointElevation.getText().toString().isEmpty()){
            Log.d(TAG, "validateEntry: Point Elevation not found");
            inputLayoutPointElevation.setError(getString(R.string.dialog_job_point_item_error_pointElevation));
            return false;

        }else {
            inputLayoutPointElevation.setError(null);

        }

        if (etPointDescription.getText().toString().isEmpty()){
            Log.d(TAG, "validateEntry: Point Description not found");
            inputLayoutPointDescription.setError(getString(R.string.dialog_job_point_item_error_pointDescription));
            return false;

        }else {
            inputLayoutPointDescription.setError(null);

        }

        Log.d(TAG, "validateEntry: Cleaning up, closing Database: " + jobDbName);
        jobDb.close();

        Log.d(TAG, "validateEntry: Results: " + results);
        return results;

    }

    private PointGeodetic createPointGeodetic(){
        Log.d(TAG, "createPointGeodetic: Started...");

        PointGeodetic pointGeodetic = new PointGeodetic();

        pointNumber = Integer.parseInt(etPointNumber.getText().toString());
        pointGeodetic.setPoint_no(pointNumber);

        pointNorthing = Double.parseDouble(etPointNorthing.getText().toString());
        pointGeodetic.setNorthing(pointNorthing);

        pointEasting = Double.parseDouble(etPointEasting.getText().toString());
        pointGeodetic.setEasting(pointEasting);

        pointElevation = Double.parseDouble(etPointElevation.getText().toString());
        pointGeodetic.setElevation(pointElevation);
        Log.d(TAG, "createPointGeodetic: Point Elevation: " + pointElevation);

        pointDescription = etPointDescription.getText().toString();
        pointGeodetic.setDescription(pointDescription);

        pointGeodetic.setPointType(1);

        pointGeodetic.setLatitude(mLocationLat);
        pointGeodetic.setLongitude(mLocationLong);
        pointGeodetic.setEllipsoid(mLocationEllipsoid);
        pointGeodetic.setOrtho(mLocationOrtho);


        int geodeticEntry;

        if(isGeodeticManualEntry){
            geodeticEntry = 1;
        }else{
            geodeticEntry = 3;
        }

        pointGeodetic.setPointGeodeticType(geodeticEntry);


        Log.d(TAG, "createPointGeodetic: Finished creating pointGeodetic");
        return pointGeodetic;
    }

    private void showToast(String data, boolean shortTime) {
        Log.d(TAG, "showToast: Started...");
        if (shortTime) {
            Toast.makeText(this, data, Toast.LENGTH_SHORT).show();

        } else{
            Toast.makeText(this, data, Toast.LENGTH_LONG).show();

        }
    }
    //----------------------------------------------------------------------------------------------//

    private void populateGridValues(){
        Log.d(TAG, "populateGridValues: Started");

        tvLocation_grid_north.setText(getString(R.string.dialog_job_point_item_header_grid_north_title));
        tvLocation_grid_north_value.setText(COORDINATE_FORMATTER.format(mLocationGridNorth));
        tvLocation_grid_north_value.setVisibility(View.VISIBLE);

        tvLocation_grid_east.setText(getString(R.string.dialog_job_point_item_header_grid_east_title));
        tvLocation_grid_east_value.setText(COORDINATE_FORMATTER.format(mLocationGridEast));
        tvLocation_grid_east_value.setVisibility(View.VISIBLE);

    }

    private void populateWorldValues(){
        Log.d(TAG, "populateWorldValues: Started");

        String strLatitude = SurveyMathHelper.convertDECtoDMSGeodetic(mLocationLat, 3, false);

        tvLocation_latitude.setText(getString(R.string.project_new_location_latitude_title));
        tvLocation_latitude_value.setText(strLatitude);
        tvLocation_latitude_value.setVisibility(View.VISIBLE);

        isGeodeticManualEntry = true;

        String strLongitude = SurveyMathHelper.convertDECtoDMSGeodetic(mLocationLong,3,true);

        tvLocation_longitude.setText(getString(R.string.project_new_location_longitude_title));
        tvLocation_longitude_value.setText(strLongitude);
        tvLocation_longitude_value.setVisibility(View.VISIBLE);

        if (mLocationEllipsoid != 0){
            String heightEllipsoid = DISTANCE_PRECISION_FORMATTER.format(mLocationEllipsoid);

            tvLocation_height.setText(getString(R.string.project_new_location_height_title));
            tvLocation_height_value.setText(heightEllipsoid);
            tvLocation_height_value.setVisibility(View.VISIBLE);

        }

        if(mLocationOrtho !=0){
            String heightOrtho = DISTANCE_PRECISION_FORMATTER.format(mLocationOrtho);

            tvLocation_ortho.setText(getString(R.string.project_new_location_ortho_title));
            tvLocation_ortho_value.setText(heightOrtho);
            tvLocation_ortho_value.setVisibility(View.VISIBLE);

        }


    }


    //----------------------------------------------------------------------------------------------//
    @Override
    public void onWorldReturnValues(double latOut, double longOut, double heightEllipsOut, double heightOrthoOut) {
        Log.d(TAG, "onWorldReturnValues: Starting...");


        mLocationLat = latOut;
        mLocationLong = longOut;
        mLocationEllipsoid = heightEllipsOut;
        mLocationOrtho = heightOrthoOut;

        if(isJobWithProjection){
            findGridCoordinatesFromWorld();
        }

        populateWorldValues();

    }

    @Override
    public void onGridReturnValues(double gridNorth, double gridEast) {
        mLocationGridNorth = gridNorth;
        mLocationGridEast = gridEast;
        isGridManualEntry = true;

        findWorldCoordinatesFromGrid();

        populateGridValues();
    }
}
