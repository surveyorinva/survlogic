package com.survlogic.survlogic.dialog;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.survlogic.survlogic.R;
import com.survlogic.survlogic.background.BackgroundProjectJobNew;
import com.survlogic.survlogic.model.ProjectJobs;
import com.survlogic.survlogic.utils.MathHelper;

/**
 * Created by chrisfillmore on 7/22/2017.
 */

public class DialogProjectJobAdd extends DialogFragment {
    private static final String TAG = "DialogProjectDescr";
    private Context mContext;
    private AlertDialog alertDialog;

    private int project_id;
    private ProjectJobs projectJobs;

    private EditText etJobName, etDescription;

    public static DialogProjectJobAdd newInstance(Integer mProjectId) {

        DialogProjectJobAdd frag = new DialogProjectJobAdd();
        Bundle args = new Bundle();

        args.putInt("project_id", mProjectId);

        frag.setArguments(args);
        return frag;

    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        project_id = getArguments().getInt("project_id");

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(),R.style.DialogPopupStyle);
        LayoutInflater inflater = getActivity().getLayoutInflater();

        View v = inflater.inflate(R.layout.dialog_project_job_new,null);
        builder.setView(v);

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

        mContext = getActivity();
        AlertDialog alertDialog = (AlertDialog) getDialog();

        etJobName = (EditText) getDialog().findViewById(R.id.job_name);
        etDescription = (EditText) getDialog().findViewById(R.id.job_description);

        Button btnSave = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitForm(v);
            }
        });

    }

    private void submitForm(View v) {
        Log.d(TAG, "submitForm: Starting");

        String jobName = etJobName.getText().toString();
        String description = etDescription.getText().toString();
        String jobDatabaseName = MathHelper.getRandomString(8) + ".db";


        //Todo convert the isStringNull to the StringUtilityHelper class for more control
        if(!isStringNull(jobName)){
            Log.d(TAG, "submitForm: Creating ProjectJobs with Description");
            if(!isStringNull(description)){
                projectJobs = new ProjectJobs(project_id,jobName,jobDatabaseName,description);

            }else{
                Log.d(TAG, "submitForm: Creating ProjectJobs without a Description");
                projectJobs = new ProjectJobs(project_id,jobName,jobDatabaseName);

            }

            saveJobInBackground();

            //Todo Send User to Job Activity from Here or refresh list on Project Detail Activity

            Log.d(TAG, "submitForm: Closing Dialog");
            dismiss();
        }else{
            Log.d(TAG, "submitForm: Closing Dialog");
            String txtVerification = getString(R.string.job_new_validation_job_name_error);
            showToast(txtVerification,true);
        }

    }

    private void saveJobInBackground(){
        Log.d(TAG, "saveProjectInBackground: Starting");
        // Setup Background Task
        BackgroundProjectJobNew backgroundProjectJobsNew = new BackgroundProjectJobNew(mContext);

        // Execute background task
        backgroundProjectJobsNew.execute(projectJobs);

    }

    private boolean isStringNull(String string){
        Log.d(TAG, "isStringNull: checking string if null.");

        if(string.equals("")){
            return true;
        }
        else{
            return false;
        }
    }

    private void showToast(String data, boolean shortTime) {

        if (shortTime) {
            Toast.makeText(getActivity(), data, Toast.LENGTH_SHORT).show();

        } else{
            Toast.makeText(getActivity(), data, Toast.LENGTH_LONG).show();

        }
    }

}
