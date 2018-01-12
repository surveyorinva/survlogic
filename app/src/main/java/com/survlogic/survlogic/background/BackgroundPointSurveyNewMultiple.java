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
import com.survlogic.survlogic.model.PointMapCheck;
import com.survlogic.survlogic.model.PointSurvey;

import java.util.ArrayList;

/**
 * Created by chrisfillmore on 6/29/2017.
 */

public class BackgroundPointSurveyNewMultiple extends AsyncTask <ArrayList<PointSurvey>,ArrayList<PointSurvey>,String> {

    private static final String TAG = "BackgroundProjectJobNew";
    private ProgressDialog dialog;

    private Context context;
    private String jobDbName;
    private long point_id;

    public BackgroundPointSurveyNewMultiple(Context context, String jobDbName) {
        this.context = context;
        this.dialog = new ProgressDialog(context);
        this.jobDbName = jobDbName;

    }

    @Override
    protected String doInBackground(ArrayList<PointSurvey>... params) {
        Log.d(TAG, "doInBackground: Starting Background Async Task");
        int recordCount = 0;
        //Get Model
        ArrayList<PointSurvey> lstPoints = params[0];

        Log.d(TAG, "doInBackground: Models Set.  Creating Database Links...");

        JobDatabaseHandler jobDb  = new JobDatabaseHandler(context, jobDbName);
        SQLiteDatabase dbJob = jobDb.getWritableDatabase();

        Log.d(TAG, "doInBackground: Size: " + lstPoints.size());

        for(int i=0; i<lstPoints.size(); i++) {
            Log.d(TAG, "doInBackground: i: " + i);
            PointSurvey pointSurvey = lstPoints.get(i);

            try{
                Log.d(TAG, "doInBackground: trying to save");
                //Saving Job Settings
                Log.d(TAG, "doInBackground: Saving Point to " + jobDbName);
                point_id = jobDb.addPointSurveyToDB(dbJob, pointSurvey);
                recordCount = recordCount + 1;

                Log.d(TAG, "doInBackground: record count: " + recordCount);
            }catch (Exception e){
                e.printStackTrace();
            }

        }

        Log.d(TAG, "doInBackground: Closing Database Connection");
        dbJob.close();

        if (point_id>0){
            return recordCount + " Row(s) Inserted";
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



    }

    @Override
    protected void onProgressUpdate(ArrayList<PointSurvey>... values) {
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
