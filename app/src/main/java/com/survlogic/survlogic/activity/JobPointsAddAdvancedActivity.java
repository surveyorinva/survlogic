package com.survlogic.survlogic.activity;

import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.survlogic.survlogic.R;
import com.survlogic.survlogic.background.BackgroundPointGeodeticNew;
import com.survlogic.survlogic.database.JobDatabaseHandler;
import com.survlogic.survlogic.dialog.DialogJobPointGeodeticEntryAdd;
import com.survlogic.survlogic.interf.PointGeodeticEntryListener;
import com.survlogic.survlogic.model.PointGeodetic;
import com.survlogic.survlogic.model.PointSurvey;
import com.survlogic.survlogic.utils.MathHelper;
import com.survlogic.survlogic.utils.PreferenceLoaderHelper;
import com.survlogic.survlogic.utils.StringUtilityHelper;

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
    PreferenceLoaderHelper preferenceLoaderHelper;
    private static DecimalFormat COORDINATE_FORMATTER, DISTANCE_PRECISION_FORMATTER;

    private PointSurvey pointSurvey;

    private TextInputLayout inputLayoutPointNumber, inputLayoutPointNorthing, inputLayoutPointEasting,
            inputLayoutPointElevation, inputLayoutPointDescription;

    private TextView tvLocation_latitude, tvLocation_longitude, tvLocation_latitude_value,tvLocation_longitude_value,
            tvLocation_height, tvLocation_height_value, tvLocation_ortho, tvLocation_ortho_value;
    private EditText etPointNumber, etPointNorthing, etPointEasting, etPointElevation, etPointDescription;
    private Button btCancel, btSave;
    private ImageButton btGPSAdd, btGPSEntryAdd;

    private int pointNumber, pointType, projectId, jobId;
    private double pointNorthing,pointEasting,pointElevation;
    private String pointDescription, jobDbName;
    double mLocationLat = 0, mLocationLong = 0, mLocationEllipsoid = 0, mLocationOrtho = 0, mLocationAccuracy = 0;

    private boolean entryGeodetic = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: Started...");
        setContentView(R.layout.activity_job_point_entry_add);

        mContext = JobPointsAddAdvancedActivity.this;
        preferenceLoaderHelper = new PreferenceLoaderHelper(mContext);
        loadPreferences();

        initViewWidgets();
        setOnClickListeners();
        populateValues();

    }

    private void initViewWidgets(){
        Log.d(TAG, "initViewWidgets: Started...");

        inputLayoutPointNumber = (TextInputLayout) findViewById(R.id.dialog_item_header_pointno);
        inputLayoutPointNorthing = (TextInputLayout) findViewById(R.id.dialog_item_header_northing);
        inputLayoutPointEasting = (TextInputLayout) findViewById(R.id.dialog_item_header_easting);
        inputLayoutPointElevation = (TextInputLayout) findViewById(R.id.dialog_item_header_elevation);
        inputLayoutPointDescription = (TextInputLayout) findViewById(R.id.dialog_item_header_description);

        etPointNumber = (EditText) findViewById(R.id.dialog_item_pointNo);
        etPointNorthing = (EditText) findViewById(R.id.dialog_item_northing);
        etPointEasting = (EditText) findViewById(R.id.dialog_item_easting);
        etPointElevation = (EditText) findViewById(R.id.dialog_item_elevation);
        etPointDescription = (EditText) findViewById(R.id.dialog_item_description);

        tvLocation_latitude = (TextView) findViewById(R.id.location_latitude);
        tvLocation_latitude_value = (TextView) findViewById(R.id.location_latitude_value);

        tvLocation_longitude = (TextView) findViewById(R.id.location_longitude);
        tvLocation_longitude_value = (TextView) findViewById(R.id.location_longitude_value);


        tvLocation_height = (TextView) findViewById(R.id.location_height);
        tvLocation_height_value = (TextView) findViewById(R.id.location_height_value);

        tvLocation_ortho = (TextView) findViewById(R.id.location_ortho);
        tvLocation_ortho_value = (TextView) findViewById(R.id.location_ortho_value);

        btSave = (Button) findViewById(R.id.Save_button);
        btCancel = (Button) findViewById(R.id.Cancel_button);

        btGPSAdd = (ImageButton) findViewById(R.id.location_get_from_gps_survey);
        btGPSEntryAdd = (ImageButton) findViewById(R.id.location_get_from_user_input);
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
        viewDialog.show(getFragmentManager(),"dialog");
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
            String heightEllipsoid = DISTANCE_PRECISION_FORMATTER.format(mLocationEllipsoid);

            mLocationOrtho = data.getDoubleExtra(getString(R.string.KEY_POSITION_ORTHO), 0);
            String heightOrtho = DISTANCE_PRECISION_FORMATTER.format(mLocationOrtho);

            String strLatitude = MathHelper.convertDECtoDMSGeodetic(mLocationLat, 3, false);

            tvLocation_latitude.setText(getString(R.string.project_new_location_latitude_title));
            tvLocation_latitude_value.setText(strLatitude);
            tvLocation_latitude_value.setVisibility(View.VISIBLE);

            String strLongitude = MathHelper.convertDECtoDMSGeodetic(mLocationLong, 3, true);

            tvLocation_longitude.setText(getString(R.string.project_new_location_longitude_title));
            tvLocation_longitude_value.setText(strLongitude);
            tvLocation_longitude_value.setVisibility(View.VISIBLE);


            tvLocation_height.setText(getString(R.string.project_new_location_height_title));
            tvLocation_height_value.setText(heightEllipsoid);
            tvLocation_height_value.setVisibility(View.VISIBLE);

            tvLocation_ortho.setText(getString(R.string.project_new_location_ortho_title));
            tvLocation_ortho_value.setText(heightOrtho);
            tvLocation_ortho_value.setVisibility(View.VISIBLE);

            entryGeodetic = false;
        }

    }

    private void loadPreferences(){
        Log.d(TAG, "loadPreferences: Started...");


        COORDINATE_FORMATTER = new DecimalFormat(preferenceLoaderHelper.getValueSystemCoordinatesPrecisionDisplay());
        DISTANCE_PRECISION_FORMATTER = new DecimalFormat(preferenceLoaderHelper.getValueSystemDistancePrecisionDisplay());

    }

    @Override
    public void onReturnValues(double latOut, double longOut, double heightEllipsOut, double heightOrthoOut) {
        Log.d(TAG, "onReturnValues: Starting...");
        if (latOut != 0) {
            mLocationLat = latOut;
            String strLatitude = MathHelper.convertDECtoDMSGeodetic(latOut, 3, false);

            tvLocation_latitude.setText(getString(R.string.project_new_location_latitude_title));
            tvLocation_latitude_value.setText(strLatitude);
            tvLocation_latitude_value.setVisibility(View.VISIBLE);

            entryGeodetic = true;
        }

        if (longOut !=0){
            mLocationLong = longOut;
            String strLongitude = MathHelper.convertDECtoDMSGeodetic(longOut,3,true);

            tvLocation_longitude.setText(getString(R.string.project_new_location_longitude_title));
            tvLocation_longitude_value.setText(strLongitude);
            tvLocation_longitude_value.setVisibility(View.VISIBLE);

        }


        if (heightEllipsOut != 0){
            mLocationEllipsoid = heightEllipsOut;
            String heightEllipsoid = DISTANCE_PRECISION_FORMATTER.format(heightEllipsOut);

            tvLocation_height.setText(getString(R.string.project_new_location_height_title));
            tvLocation_height_value.setText(heightEllipsoid);
            tvLocation_height_value.setVisibility(View.VISIBLE);

        }

        if(heightOrthoOut !=0){
            mLocationOrtho = heightOrthoOut;
            String heightOrtho = DISTANCE_PRECISION_FORMATTER.format(heightOrthoOut);

            tvLocation_ortho.setText(getString(R.string.project_new_location_ortho_title));
            tvLocation_ortho_value.setText(heightOrtho);
            tvLocation_ortho_value.setVisibility(View.VISIBLE);

        }

    }


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

        if(entryGeodetic){
            geodeticEntry = 1;
        }else{
            geodeticEntry = 3;
        }

        pointGeodetic.setPointGeodeticType(geodeticEntry);


        Log.d(TAG, "createPointGeodetic: Finished creating pointGeodetic");
        return pointGeodetic;
    }


}
