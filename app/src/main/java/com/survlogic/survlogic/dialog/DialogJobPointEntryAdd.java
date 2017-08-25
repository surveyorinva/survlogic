package com.survlogic.survlogic.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.DialogFragment;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.survlogic.survlogic.R;
import com.survlogic.survlogic.activity.JobPointsActivity;
import com.survlogic.survlogic.activity.JobPointsAddAdvancedActivity;
import com.survlogic.survlogic.background.BackgroundPointSurveyNew;
import com.survlogic.survlogic.background.BackgroundProjectJobNew;
import com.survlogic.survlogic.database.JobDatabaseHandler;
import com.survlogic.survlogic.model.PointSurvey;

/**
 * Created by chrisfillmore on 8/11/2017.
 */

public class DialogJobPointEntryAdd extends DialogFragment {
    private static final String TAG = "DialogJobPointEntryAdd";

    private Context mContext;

    private int project_id, job_id;
    private String databaseName;

    private TextInputLayout inputLayoutPointNumber, inputLayoutPointNorthing, inputLayoutPointEasting,
            inputLayoutPointElevation, inputLayoutPointDescription;

    private EditText etPointNumber, etPointNorthing, etPointEasting, etPointElevation, etPointDescription;

    private Button btAdvancedOptions;

    private int pointNumber, pointType;
    private double pointNorthing,pointEasting,pointElevation;
    private String pointDescription;


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

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.DialogPopupStyle);
        LayoutInflater inflater = getActivity().getLayoutInflater();

        View v = inflater.inflate(R.layout.dialog_job_point_entry_add,null);
        builder.setView(v);

        builder.setTitle(title);

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
        super.onResume();
        Log.d(TAG, "onResume: Started...");
        mContext = getActivity();

        AlertDialog alertDialog = (AlertDialog) getDialog();
        Button btnSave = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitForm(v);
            }
        });

        initViewWidgets();


    }


    private void initViewWidgets(){
        Log.d(TAG, "initViewWidgets: Started...");

        inputLayoutPointNumber = (TextInputLayout) getDialog().findViewById(R.id.dialog_item_header_pointno);
        inputLayoutPointNorthing = (TextInputLayout) getDialog().findViewById(R.id.dialog_item_header_northing);
        inputLayoutPointEasting = (TextInputLayout) getDialog().findViewById(R.id.dialog_item_header_easting);
        inputLayoutPointElevation = (TextInputLayout) getDialog().findViewById(R.id.dialog_item_header_elevation);
        inputLayoutPointDescription = (TextInputLayout) getDialog().findViewById(R.id.dialog_item_header_description);

        etPointNumber = (EditText) getDialog().findViewById(R.id.dialog_item_pointNo);
        etPointNorthing = (EditText) getDialog().findViewById(R.id.dialog_item_northing);
        etPointEasting = (EditText) getDialog().findViewById(R.id.dialog_item_easting);
        etPointElevation = (EditText) getDialog().findViewById(R.id.dialog_item_elevation);
        etPointDescription = (EditText) getDialog().findViewById(R.id.dialog_item_description);

        etPointNumber.addTextChangedListener(new MyTextWatcher(etPointNumber));

        btAdvancedOptions = (Button) getDialog().findViewById(R.id.btnAdvancedSettings);
        btAdvancedOptions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToPointAddNew();
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

        if (validateEntry()){
            Log.d(TAG, "submitForm: Validation Approved, Saving...");
            // Setup Background Task
            BackgroundPointSurveyNew backgroundPointSurveyNew = new BackgroundPointSurveyNew(mContext, databaseName);

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

        Bundle b = new Bundle();
        PointSurvey pointSurvey = populateValuesNull();
        b.putParcelable("POINT_ENTRY", pointSurvey);
        i.putExtras(b);

        startActivity(i);
        getActivity().overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);

        dismiss();


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


}
