package com.survlogic.survlogic.activity;

import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.survlogic.survlogic.R;
import com.survlogic.survlogic.dialog.DialogJobPointGeodeticEntryAdd;
import com.survlogic.survlogic.model.PointSurvey;
import com.survlogic.survlogic.utils.MathHelper;
import com.survlogic.survlogic.utils.PreferenceLoaderHelper;
import com.survlogic.survlogic.utils.StringUtilityHelper;

import java.io.File;
import java.text.DecimalFormat;

/**
 * Created by chrisfillmore on 8/13/2017.
 */

public class JobPointsAddAdvancedActivity extends AppCompatActivity {
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
    private Button btCancel, btSave, btTakePhoto;
    private ImageButton btGPSAdd, btGPSEntryAdd;

    private int pointNumber, pointType;
    private double pointNorthing,pointEasting,pointElevation;
    private String pointDescription;
    double mLocationLat = 0, mLocationLong = 0, mLocationEllipsoid = 0, mLocationOrtho = 0, mLocationAccuracy = 0;


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

        btCancel = (Button) findViewById(R.id.Cancel_button);
        btTakePhoto = (Button) findViewById(R.id.photo_camera_get_photo);

        btGPSAdd = (ImageButton) findViewById(R.id.location_get_from_gps_survey);
        btGPSEntryAdd = (ImageButton) findViewById(R.id.location_get_from_user_input);
    }

    private void setOnClickListeners(){
        Log.d(TAG, "setOnClickListeners: Started...");
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

        btTakePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callImageSelectionDialog();
            }
        });


    }
    private void populateValues(){
        Log.d(TAG, "populateValues: Started...");

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

    private void callImageSelectionDialog(){
        Log.d(TAG, "callImageSelectionDialog: Started...");
        final CharSequence[] items = { getString(R.string.project_new_dialog_takePhoto), getString(R.string.project_new_dialog_getImage),
                getString(R.string.general_cancel) };

        TextView title = new TextView(this);  //was context not this

        title.setText(getString(R.string.photo_dialog_title_point));
        title.setBackgroundColor(Color.WHITE);
        title.setPadding(10, 15, 15, 10);
        title.setGravity(Gravity.CENTER);
        title.setTextColor(Color.BLACK);
        title.setTextSize(22);

        AlertDialog.Builder builder = new AlertDialog.Builder(JobPointsAddAdvancedActivity.this);

        builder.setCustomTitle(title);

        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int arg) {

                switch (arg) {

                    case 0: //Camera
                        //startCamera();
                        break;

                    case 1: //Photo
                        //startPhotoGallery();
                        break;

                    case 2: //Cancel

                        dialog.dismiss();
                        break;
                }

            }
        });
        builder.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult: Started...");

        if (this.REQUEST_GET_GPS == requestCode && resultCode == RESULT_OK) {
            Log.d(TAG, "onActivityResult: Starting...");

            mLocationLat = data.getDoubleExtra(getString(R.string.KEY_POSITION_LATITUDE),0);
            mLocationLong = data.getDoubleExtra(getString(R.string.KEY_POSITION_LONGITUDE),0);

            mLocationEllipsoid = data.getDoubleExtra(getString(R.string.KEY_POSITION_ELLIPSOID),0);
            String heightEllipsoid = DISTANCE_PRECISION_FORMATTER.format(mLocationEllipsoid);

            mLocationOrtho = data.getDoubleExtra(getString(R.string.KEY_POSITION_ORTHO),0);
            String heightOrtho = DISTANCE_PRECISION_FORMATTER.format(mLocationOrtho);

            String strLatitude = MathHelper.convertDECtoDMS(mLocationLat,3,false);

            tvLocation_latitude.setText(getString(R.string.project_new_location_latitude_title));
            tvLocation_latitude_value.setText(strLatitude);
            tvLocation_latitude_value.setVisibility(View.VISIBLE);

            String strLongitude = MathHelper.convertDECtoDMS(mLocationLong,3,true);

            tvLocation_longitude.setText(getString(R.string.project_new_location_longitude_title));
            tvLocation_longitude_value.setText(strLongitude);
            tvLocation_longitude_value.setVisibility(View.VISIBLE);


            tvLocation_height.setText(getString(R.string.project_new_location_height_title));
            tvLocation_height_value.setText(heightEllipsoid);
            tvLocation_height_value.setVisibility(View.VISIBLE);

            tvLocation_ortho.setText(getString(R.string.project_new_location_ortho_title));
            tvLocation_ortho_value.setText(heightOrtho);
            tvLocation_ortho_value.setVisibility(View.VISIBLE);


        }else if(this.REQUEST_TAKE_PHOTO == requestCode && resultCode == RESULT_OK){



        }else if(this.REQUEST_SELECT_PICTURE == requestCode && resultCode == RESULT_OK){


        }

    }

    private void loadPreferences(){
        Log.d(TAG, "loadPreferences: Started...");


        COORDINATE_FORMATTER = new DecimalFormat(preferenceLoaderHelper.getValueSystemCoordinatesPrecisionDisplay());
        DISTANCE_PRECISION_FORMATTER = new DecimalFormat(preferenceLoaderHelper.getValueSystemDistancePrecisionDisplay());

    }


}
