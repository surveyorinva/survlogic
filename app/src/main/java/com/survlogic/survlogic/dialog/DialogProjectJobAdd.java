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
import com.survlogic.survlogic.model.Project;
import com.survlogic.survlogic.model.ProjectJobs;
import com.survlogic.survlogic.utils.StringUtilityHelper;
import com.survlogic.survlogic.utils.SurveyMathHelper;

/**
 * Created by chrisfillmore on 7/22/2017.
 */

public class DialogProjectJobAdd extends DialogFragment {
    private static final String TAG = "DialogProjectDescr";
    private Context mContext;
    private AlertDialog alertDialog;

    private int project_id;
    private ProjectJobs projectJob;

    private EditText etJobName, etDescription;

    private Project mProject;

    public static DialogProjectJobAdd newInstance(Integer mProjectId, Project project) {

        DialogProjectJobAdd frag = new DialogProjectJobAdd();
        Bundle args = new Bundle();

        args.putInt("project_id", mProjectId);
        args.putParcelable("project", project);

        frag.setArguments(args);
        return frag;

    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        project_id = getArguments().getInt("project_id");
        mProject = getArguments().getParcelable("project");

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
        String jobDatabaseName = SurveyMathHelper.getRandomString(8) + ".db";


        if(!StringUtilityHelper.isStringNull(jobName)){

            projectJob = new ProjectJobs();
            projectJob.setProjectId(project_id);
            projectJob.setmJobName(jobName);
            projectJob.setmJobDbName(jobDatabaseName);

            projectJob.setProjection(mProject.getmProjection());
            projectJob.setProjectionString(mProject.getProjectionString());

            projectJob.setProjectionZone(mProject.getmZone());
            projectJob.setZoneString(mProject.getZoneString());

            if(!StringUtilityHelper.isStringNull(description)) {
                projectJob.setmJobDescription(description);
            }

            saveJobInBackground();
            dismiss();

        }else {
            if(StringUtilityHelper.isStringNull(jobName)) {
                etJobName.setError(mContext.getResources().getString(R.string.job_new_validation_job_name_error));

            }

        }


    }

    private void saveJobInBackground(){
        Log.d(TAG, "saveProjectInBackground: Starting");
        // Setup Background Task
        BackgroundProjectJobNew backgroundProjectJobsNew = new BackgroundProjectJobNew(mContext);

        // Execute background task
        backgroundProjectJobsNew.execute(projectJob);

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
