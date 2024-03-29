package com.survlogic.survlogic.background;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.survlogic.survlogic.R;
import com.survlogic.survlogic.database.JobDatabaseHandler;
import com.survlogic.survlogic.database.ProjectDatabaseHandler;
import com.survlogic.survlogic.interf.JobPointsActivityListener;
import com.survlogic.survlogic.model.PointSurvey;
import com.survlogic.survlogic.model.ProjectJobSettings;
import com.survlogic.survlogic.model.ProjectJobs;

/**
 * Created by chrisfillmore on 6/29/2017.
 */

public class BackgroundPointSurveyNew extends AsyncTask <PointSurvey,PointSurvey,String> {

    private static final String TAG = "BackgroundProjectJobNew";
    private ProgressDialog dialog;

    private JobPointsActivityListener jobPointsActivityListener;

    private Context context;
    private Activity activity;
    private String jobDbName;
    private long point_id;

    public BackgroundPointSurveyNew(Context context, String jobDbName, JobPointsActivityListener jobPointsActivityListener) {
        this.context = context;
        this.dialog = new ProgressDialog(context);
        this.jobDbName = jobDbName;
        this.jobPointsActivityListener = jobPointsActivityListener;

        activity = (Activity) context;
    }

    @Override
    protected String doInBackground(PointSurvey... params) {
        Log.d(TAG, "doInBackground: Starting Background Async Task");

        //Get Model
        PointSurvey pointSurvey = params[0];

        Log.d(TAG, "doInBackground: Models Set.  Creating Database Links...");

        JobDatabaseHandler jobDb  = new JobDatabaseHandler(context, jobDbName);
        SQLiteDatabase dbJob = jobDb.getWritableDatabase();

        try{
            //Saving Job Settings
            Log.d(TAG, "doInBackground: Saving Point to " + jobDbName);
            point_id = jobDb.addPointSurveyToDB(dbJob, pointSurvey);


        }catch (Exception e){
            e.printStackTrace();
        }

        Log.d(TAG, "doInBackground: Closing Database Connection");
        dbJob.close();

        if (point_id>0){
            return "One Row Inserted";
        }else{
            return "Error Inserting Row";
        }

    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
            dialog.setMessage("Saving Point");
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
            showToast(context.getString(R.string.job_new_validation_point_save_success),true);

        }else if (result.equals("Error Inserting Row")){
            showToast(context.getString(R.string.job_new_validation_point_save_failed),true);
        }

        jobPointsActivityListener.refreshPointArrays();
    }

    @Override
    protected void onProgressUpdate(PointSurvey... values) {
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
