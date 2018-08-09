package com.survlogic.survlogic.ARvS.background;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.survlogic.survlogic.ARvS.interf.GPSPointBackgroundGetters;
import com.survlogic.survlogic.R;
import com.survlogic.survlogic.database.JobDatabaseHandler;
import com.survlogic.survlogic.model.PointGeodetic;

/**
 * Created by chrisfillmore on 6/29/2017.
 */

public class BackgroundGPSPointUpdate extends AsyncTask <PointGeodetic,PointGeodetic,String> {

    private static final String TAG = "BackgroundPointGeodetic";
    private Context context;
    private Activity activity;

    private GPSPointBackgroundGetters listener;

    private String jobDbName;
    private long point_update;
    private int point_number;


    public BackgroundGPSPointUpdate(Context context, int point_number, String jobDbName, GPSPointBackgroundGetters listener) {
        this.context = context;
        this.jobDbName = jobDbName;
        this.point_number = point_number;
        this.listener = listener;

        activity = (Activity) context;
    }

    @Override
    protected String doInBackground(PointGeodetic... params) {
        Log.d(TAG, "doInBackground: Starting Background Async Task");

        //Get Model
        PointGeodetic pointGeodetic = params[0];

        Log.d(TAG, "doInBackground: Models Set.  Creating Database Links...");

        JobDatabaseHandler jobDb  = new JobDatabaseHandler(context, jobDbName);
        SQLiteDatabase dbJob = jobDb.getWritableDatabase();

        try{
            //Saving Job Settings
            Log.d(TAG, "doInBackground: Saving Point to " + jobDbName);
            point_update  = jobDb.updatePointGeodeticByPointNo(dbJob, point_number, pointGeodetic);


        }catch (Exception e){
            e.printStackTrace();
        }

        Log.d(TAG, "doInBackground: Closing Database Connection");
        dbJob.close();

        if (point_update>0){
            return "Success";
        }else{
            return "Error";
        }

    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();


    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        if (result.equals("Success")){
            listener.isPointSaveASuccess(true);

        }else if (result.equals("Error")){
            listener.isPointSaveASuccess(false);
        }

    }

    @Override
    protected void onProgressUpdate(PointGeodetic... values) {
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
