package com.survlogic.survlogic.background;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.survlogic.survlogic.R;
import com.survlogic.survlogic.database.JobDatabaseHandler;
import com.survlogic.survlogic.database.ProjectDatabaseHandler;
import com.survlogic.survlogic.model.PointSurvey;
import com.survlogic.survlogic.model.Project;
import com.survlogic.survlogic.model.ProjectJobSettings;
import com.survlogic.survlogic.model.ProjectJobs;

/**
 * Created by chrisfillmore on 6/29/2017.
 */

public class BackgroundProjectJobNew extends AsyncTask <ProjectJobs,ProjectJobs,String> {

    private static final String TAG = "BackgroundProjectJobNew";
    private ProgressDialog dialog;

    private Context context;
    private Activity activity;

    private long project_job_id = 0;
    private long job_setting_id = 0;
    private long default_point_id = 0;

    public BackgroundProjectJobNew(Context context) {
        this.context = context;
        this.dialog = new ProgressDialog(context);

        activity = (Activity) context;
    }

    @Override
    protected String doInBackground(ProjectJobs... params) {
        Log.d(TAG, "doInBackground: Starting Background Async Task");

        //Get Model
        ProjectJobs projectJob = params[0];

        Log.d(TAG, "doInBackground: Models Set.  Creating Database Links...");

        ProjectDatabaseHandler projectDb = new ProjectDatabaseHandler(context);
        SQLiteDatabase dbProject = projectDb.getWritableDatabase();

        Log.d(TAG, "doInBackground: Project Table being Setup");

        JobDatabaseHandler jobDb  = new JobDatabaseHandler(context, projectJob.getmJobDbName());
        SQLiteDatabase dbJob = jobDb.getWritableDatabase();

        Log.d(TAG, "doInBackground: Job Tables being Setup");

        try{
            Log.d(TAG, "doInBackground: Saving Project Job to Project.db");
            //Saving Job to Project Database
            project_job_id = projectDb.addProjectJobToDB(dbProject,projectJob);

            //Get Model
            Log.d(TAG, "doInBackground: Fetching Job Settings Model");
            ProjectJobSettings settings = new ProjectJobSettings(projectJob.getProjectId(), project_job_id, projectJob.getmJobName(),1,1,0,0,0,1,0,2,3,0,0,0,0,0,0,1,1,0,0);
            Log.d(TAG, "doInBackground: Complete fetching Job Setting Model");

            //Saving Job Settings
            Log.d(TAG, "doInBackground: Saving Job Settings to " + projectJob.getmJobDbName());
            job_setting_id = jobDb.addDefaultJobSettingToDB(dbJob,settings,false);
            Log.d(TAG, "doInBackground: Complete saving project settings");


        }catch (Exception e){
            e.printStackTrace();
        }

        Log.d(TAG, "doInBackground: Clean-up");
        dbProject.close();
        dbJob.close();

        if (project_job_id>0){
            return "One Row Inserted";
        }else{
            return "Error Inserting Row";
        }

    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        dialog.setMessage("Saving Job");
        dialog.setIndeterminate(true);
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setCancelable(false);
        dialog.show();

    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);

        if (dialog.isShowing()) {
            dialog.dismiss();
        }

        if (result.equals("One Row Inserted")){
            showToast(context.getString(R.string.job_new_validation_job_save_success),true);

        }else if (result.equals("Error Inserting Row")){
            showToast(context.getString(R.string.job_new_validation_job_save_failed),true);
        }

    }

    @Override
    protected void onProgressUpdate(ProjectJobs... values) {
        super.onProgressUpdate(values);
    }

    private void showToast(String data, boolean shortTime) {

        if (shortTime) {
            Toast.makeText(context, data, Toast.LENGTH_SHORT).show();

        } else{
            Toast.makeText(context, data, Toast.LENGTH_LONG).show();

        }

    }

}
