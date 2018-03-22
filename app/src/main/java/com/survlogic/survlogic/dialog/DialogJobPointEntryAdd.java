package com.survlogic.survlogic.dialog;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.DialogFragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.survlogic.survlogic.R;
import com.survlogic.survlogic.activity.JobPointsAddAdvancedActivity;
import com.survlogic.survlogic.background.BackgroundPointSurveyNew;
import com.survlogic.survlogic.database.JobDatabaseHandler;
import com.survlogic.survlogic.interf.JobPointsActivityListener;
import com.survlogic.survlogic.model.PointSurvey;
import com.survlogic.survlogic.utils.PreferenceLoaderHelper;
import com.survlogic.survlogic.utils.StringUtilityHelper;

import java.text.DecimalFormat;

import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;

/**
 * Created by chrisfillmore on 8/11/2017.
 */

public class DialogJobPointEntryAdd extends DialogFragment {
    private static final String TAG = "DialogJobPointEntryAdd";

    private Context mContext;
    private PreferenceLoaderHelper preferenceLoaderHelper;

    private int project_id, job_id;
    private String databaseName;

    private TextView tvButtonText, tvHiddenPointNumber, tvHiddenPointNorthing, tvHiddenPointEasting,
                    tvHiddenPointElevation, tvHiddenPointDescription;

    private TextInputLayout inputLayoutPointNumber, inputLayoutPointNorthing, inputLayoutPointEasting,
            inputLayoutPointElevation, inputLayoutPointDescription;

    private EditText etPointNumber, etPointNorthing, etPointEasting, etPointElevation, etPointDescription;

    private FrameLayout btn_AdvancedSettings;

    private ProgressBar progressBar_AdvancedSettings;

    private int pointNumber, pointType;
    private double pointNorthing,pointEasting,pointElevation;
    private String pointDescription;

    private JobPointsActivityListener jobPointsActivityListener;

    private DecimalFormat COORDINATE_FORMATTER, DISTANCE_PRECISION_FORMATTER;


    public static DialogJobPointEntryAdd newInstance(int title, int project_id, int job_id, String databaseName) {
        Log.d(TAG, "newInstance: Starting...");
        DialogJobPointEntryAdd frag = new DialogJobPointEntryAdd();
        Bundle args = new Bundle();
        args.putInt("title", title);
        args.putInt("project_id", project_id);
        args.putInt("job_id", job_id);
        args.putString("databaseName", databaseName);
        frag.setArguments(args);
        return frag;

    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Log.d(TAG, "onCreateDialog: Starting...>");

        int title = getArguments().getInt("title");
        project_id = getArguments().getInt("project_id");
        job_id = getArguments().getInt("job_id");
        databaseName = getArguments().getString("databaseName");

        Log.d(TAG, "onCreateDialog: Database Name:" + databaseName + " Loaded...");

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.DialogPopupStyleV2);
        LayoutInflater inflater = getActivity().getLayoutInflater();

        View v = inflater.inflate(R.layout.dialog_job_point_entry_add,null);
        builder.setView(v);

        //builder.setTitle(title);

        builder.setPositiveButton(R.string.general_save,null);

        builder.setNegativeButton(R.string.general_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.create();
        return builder.show();
    }

    @Override
    public void onResume() {
        getDialog().getWindow().setLayout( ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT );

        super.onResume();
        Log.d(TAG, "onResume: Started...");
        mContext = getActivity();
        preferenceLoaderHelper = new PreferenceLoaderHelper(mContext);

        initPreferences();

        AlertDialog alertDialog = (AlertDialog) getDialog();
        Button btnSave = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitForm(v);
            }
        });

        initViewWidgets();
        initFocusChangeListeners();

    }

    private void initPreferences(){
        Log.d(TAG, "initPreferences: Started");

        COORDINATE_FORMATTER = new DecimalFormat(preferenceLoaderHelper.getValueSystemCoordinatesPrecisionDisplay());
        DISTANCE_PRECISION_FORMATTER = new DecimalFormat(preferenceLoaderHelper.getValueSystemDistancePrecisionDisplay());

    }
    private void initViewWidgets(){
        Log.d(TAG, "initViewWidgets: Started...");

        btn_AdvancedSettings = (FrameLayout) getDialog().findViewById(R.id.btn_AdvancedSettings);

        tvButtonText = (TextView) getDialog().findViewById(R.id.button_text);

        tvHiddenPointNumber = (TextView) getDialog().findViewById(R.id.dialog_item_header_pointno_hint);
        tvHiddenPointNorthing = (TextView) getDialog().findViewById(R.id.dialog_item_header_northing_hint);
        tvHiddenPointEasting = (TextView) getDialog().findViewById(R.id.dialog_item_header_easting_hint);
        tvHiddenPointElevation = (TextView) getDialog().findViewById(R.id.dialog_item_header_elevation_hint);
        tvHiddenPointDescription = (TextView) getDialog().findViewById(R.id.dialog_item_header_description_hint);

        progressBar_AdvancedSettings = (ProgressBar) getDialog().findViewById(R.id.progress_bar);

        inputLayoutPointNumber = (TextInputLayout) getDialog().findViewById(R.id.dialog_item_header_pointno);
        inputLayoutPointNorthing = (TextInputLayout) getDialog().findViewById(R.id.dialog_item_header_northing);
        inputLayoutPointEasting = (TextInputLayout) getDialog().findViewById(R.id.dialog_item_header_easting);
        inputLayoutPointElevation = (TextInputLayout) getDialog().findViewById(R.id.dialog_item_header_elevation);
        inputLayoutPointDescription = (TextInputLayout) getDialog().findViewById(R.id.dialog_item_header_description);

        etPointNumber = (EditText) getDialog().findViewById(R.id.dialog_item_pointNo);
        etPointNumber.setSelectAllOnFocus(true);

        etPointNorthing = (EditText) getDialog().findViewById(R.id.dialog_item_northing);
        etPointNorthing.setSelectAllOnFocus(true);

        etPointEasting = (EditText) getDialog().findViewById(R.id.dialog_item_easting);
        etPointEasting.setSelectAllOnFocus(true);

        etPointElevation = (EditText) getDialog().findViewById(R.id.dialog_item_elevation);
        etPointElevation.setSelectAllOnFocus(true);

        etPointDescription = (EditText) getDialog().findViewById(R.id.dialog_item_description);
        etPointElevation.setSelectAllOnFocus(true);

        etPointNumber.addTextChangedListener(new MyTextWatcher(etPointNumber));

        btn_AdvancedSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadAdvancedView();
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


    private PointSurvey populateValues(){
        Log.d(TAG, "populateValues: Started...");

        PointSurvey pointSurvey = new PointSurvey();

        pointNumber = Integer.parseInt(etPointNumber.getText().toString());
        pointSurvey.setPoint_no(pointNumber);

        pointNorthing = Double.parseDouble(etPointNorthing.getText().toString());
        pointSurvey.setNorthing(pointNorthing);

        pointEasting = Double.parseDouble(etPointEasting.getText().toString());
        pointSurvey.setEasting(pointEasting);

        pointElevation = Double.parseDouble(etPointElevation.getText().toString());
        pointSurvey.setElevation(pointElevation);
        Log.d(TAG, "populateValues: Point Elevation: " + pointElevation);

        pointDescription = etPointDescription.getText().toString();
        pointSurvey.setDescription(pointDescription);

        pointSurvey.setPointType(0);

        return pointSurvey;
    }

    private PointSurvey populateValuesNull(){
        Log.d(TAG, "populateValuesNull: Started...");

        PointSurvey pointSurvey = new PointSurvey();

        Log.d(TAG, "populateValuesNull: Point number");

        if(!etPointNumber.getText().toString().matches("")) {
            pointNumber = Integer.parseInt(etPointNumber.getText().toString());
            pointSurvey.setPoint_no(pointNumber);
        }

        Log.d(TAG, "populateValuesNull: Point Northing");

        if(!etPointNorthing.getText().toString().matches("")) {
            pointNorthing = Double.parseDouble(etPointNorthing.getText().toString());
            pointSurvey.setNorthing(pointNorthing);
        }

        Log.d(TAG, "populateValuesNull: Point Easting");
        if(!etPointEasting.getText().toString().matches("")) {
            pointEasting = Double.parseDouble(etPointEasting.getText().toString());
            pointSurvey.setEasting(pointEasting);
        }

        Log.d(TAG, "populateValuesNull: Point Elevation");
        if(!etPointElevation.getText().toString().matches("")) {
            pointElevation = Double.parseDouble(etPointElevation.getText().toString());
            pointSurvey.setElevation(pointElevation);
        }

        Log.d(TAG, "populateValuesNull: Point Description");
        if(!etPointDescription.getText().toString().matches("")) {
            pointDescription = etPointDescription.getText().toString();
            pointSurvey.setDescription(pointDescription);
        }

        pointSurvey.setPointType(0);

        return pointSurvey;
    }

    private boolean validateEntry(){
        Log.d(TAG, "validateEntry: Starting...");
        boolean results;


        results =  true;
        if (etPointNumber.getText().toString().isEmpty()){
            Log.d(TAG, "validateEntry: No Point No.");
            inputLayoutPointNumber.setError(getString(R.string.dialog_job_point_item_error_pointNo));
            return false;

        }else {
            inputLayoutPointNumber.setError(null);

        }


        int point_No = Integer.parseInt(etPointNumber.getText().toString());

        Log.d(TAG, "validateEntry: Checking Database: " + databaseName + " for " + point_No);

        JobDatabaseHandler jobDb  = new JobDatabaseHandler(mContext, databaseName);
        SQLiteDatabase dbJob = jobDb.getReadableDatabase();

        if(jobDb.checkPointNumberExists(dbJob,point_No)){
            Log.d(TAG, "validateEntry: Existing Point Number");
            inputLayoutPointNumber.setError(getString(R.string.dialog_job_point_item_error_pointNoExists));
            jobDb.close();
            return false;

        }else{
            inputLayoutPointNumber.setError(null);


        }


        if (etPointNorthing.getText().toString().isEmpty()){
            Log.d(TAG, "validateEntry: No Northing");
            inputLayoutPointNorthing.setError(getString(R.string.dialog_job_point_item_error_pointNorthing));
            return false;

        }else {
            inputLayoutPointNorthing.setError(null);

        }

        if (etPointEasting.getText().toString().isEmpty()){
            Log.d(TAG, "validateEntry: No Easting");
            inputLayoutPointEasting.setError(getString(R.string.dialog_job_point_item_error_pointEasting));
            return false;

        }else {
            inputLayoutPointEasting.setError(null);

        }

        if (etPointElevation.getText().toString().isEmpty()){
            Log.d(TAG, "validateEntry: No Elevation");
            inputLayoutPointElevation.setError(getString(R.string.dialog_job_point_item_error_pointElevation));
            return false;

        }else {
            inputLayoutPointElevation.setError(null);

        }

        if (etPointDescription.getText().toString().isEmpty()){
            Log.d(TAG, "validateEntry: No Point Description");
            inputLayoutPointDescription.setError(getString(R.string.dialog_job_point_item_error_pointDescription));
            return false;

        }else {
            inputLayoutPointDescription.setError(null);

        }

        jobDb.close();
        Log.d(TAG, "validateEntry: Returning: " + results);
        return true;

    }

    private void submitForm(View v){

        JobPointsActivityListener jobPointsActivityListener = (JobPointsActivityListener) getActivity();


        if (validateEntry()){
            Log.d(TAG, "submitForm: Validation Approved, Saving...");
            // Setup Background Task
            BackgroundPointSurveyNew backgroundPointSurveyNew = new BackgroundPointSurveyNew(mContext, databaseName, jobPointsActivityListener);

            // Execute background task
            backgroundPointSurveyNew.execute(populateValues());


            getDialog().dismiss();
        }


    }


    private void goToPointAddNew(){
        Log.d(TAG, "goToPointAddNew: Starting...");

        Intent i = new Intent(mContext, JobPointsAddAdvancedActivity.class);
        i.putExtra("PROJECT_ID",project_id);
        i.putExtra("JOB_ID", job_id);
        i.putExtra("JOB_DB_NAME", databaseName);
        i.putExtra("IS_EDIT", false);

        Bundle b = new Bundle();
        PointSurvey pointSurvey = populateValuesNull();
        b.putParcelable("POINT_ENTRY", pointSurvey);
        i.putExtras(b);

        startActivityForResult(i,1);
        getActivity().overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                dismiss();

            }
        }, 400);
    }



    private void showToast(String data, boolean shortTime) {
        Log.d(TAG, "showToast: Started...");
        if (shortTime) {
            Toast.makeText(getActivity(), data, Toast.LENGTH_SHORT).show();

        } else{
            Toast.makeText(getActivity(), data, Toast.LENGTH_LONG).show();

        }
    }



    private class MyTextWatcher implements TextWatcher {

        private View view;

        private MyTextWatcher(View view) {
            this.view = view;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            Log.d(TAG, "afterTextChanged: Started...");
            switch (view.getId()){
                case R.id.dialog_item_header_pointno:
                    Log.d(TAG, "afterTextChanged: Point No. Found...");
                    if (s.length() != 0) {
                        Log.d(TAG, "afterTextChanged: Length over 0");
                        inputLayoutPointNumber.setError(null);
                    }
                    break;
            }
        }
    }


    //----------------------------------------------------------------------------------------------//

    private void loadAdvancedView(){
        Log.d(TAG, "loadAdvancedView: Started");

        animateButtonWidth();

        fadeOutTextAndShowProgressDialog();

        nextActions();

    }

    private void animateButtonWidth() {
        ValueAnimator anim = ValueAnimator.ofInt(btn_AdvancedSettings.getMeasuredWidth(), getFabWidth());

        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                int val = (Integer) valueAnimator.getAnimatedValue();
                ViewGroup.LayoutParams layoutParams = btn_AdvancedSettings.getLayoutParams();
                layoutParams.width = val;
                btn_AdvancedSettings.requestLayout();
            }
        });
        anim.setDuration(250);
        anim.start();
    }

    private void fadeOutTextAndShowProgressDialog() {
        tvButtonText.animate().alpha(0f)
                .setDuration(250)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        showProgressDialog();
                    }
                })
                .start();
    }

    private void showProgressDialog() {
        progressBar_AdvancedSettings.setAlpha(1f);
        progressBar_AdvancedSettings
                .getIndeterminateDrawable()
                .setColorFilter(Color.parseColor("#ffffff"), PorterDuff.Mode.SRC_IN);
        progressBar_AdvancedSettings.setVisibility(VISIBLE);
    }

    private void nextActions() {

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                fadeOutProgressDialog();
                delayedStartAdvancedActivity();

            }
        }, 700);

    }



    private void fadeOutProgressDialog() {
        progressBar_AdvancedSettings.animate()
                .alpha(0f)
                .setDuration(200)
                .setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);

            }
        }).start();
    }

    private void delayedStartAdvancedActivity(){
        Log.d(TAG, "delayedStartAdvancedActivity: Started");

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                goToPointAddNew();

            }
        }, 100);



    }

    private int getFabWidth() {
        return (int) getResources().getDimension(R.dimen.fab_size);
    }


}
