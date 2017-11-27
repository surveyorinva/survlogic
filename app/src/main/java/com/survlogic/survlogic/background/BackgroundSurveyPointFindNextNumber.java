package com.survlogic.survlogic.background;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.survlogic.survlogic.database.JobDatabaseHandler;
import com.survlogic.survlogic.interf.JobCogoSideshotPointListener;
import com.survlogic.survlogic.model.PointSurvey;

/**
 * Created by chrisfillmore on 6/29/2017.
 */

public class BackgroundSurveyPointFindNextNumber extends AsyncTask <PointSurvey,PointSurvey,String> {

    private static final String TAG = "BackgroundSurveyPointFi";

    private Context context;

    private final JobCogoSideshotPointListener mListener;
    private String DB_NAME;
    private int pointNumber;
    private int nextPointNo;

    public BackgroundSurveyPointFindNextNumber(Context context, String DB_NAME, JobCogoSideshotPointListener listener, int pointNumber) {
        this.context = context;
        this.DB_NAME = DB_NAME;

        mListener = listener;
        this.pointNumber = pointNumber;
    }


    @Override
    protected String doInBackground(PointSurvey... params) {
        try{
            Log.d(TAG, "doInBackground: Connecting to db");

            JobDatabaseHandler jobDb = new JobDatabaseHandler(context, DB_NAME);
            SQLiteDatabase db = jobDb.getReadableDatabase();

            nextPointNo = jobDb.findNextPointNumber(db,pointNumber);

            Log.d(TAG, "doInBackground: Closing connection to DB");
            db.close();
            //

        }catch (Exception e){
            e.printStackTrace();
        }


        return "get_info";


    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

    }

    @Override
    protected void onProgressUpdate(PointSurvey... values) {
        super.onProgressUpdate(values);

    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        Log.d(TAG, "onPostExecute: Started...");
        if (mListener != null){
            Log.d(TAG, "onPostExecute: mListener Not Null");
            mListener.whatIsNextPointNumber(nextPointNo);
        }


    }

    private void showToast(String data, boolean shortTime) {

        if (shortTime) {
            Toast.makeText(context, data, Toast.LENGTH_SHORT).show();

        } else{
            Toast.makeText(context, data, Toast.LENGTH_LONG).show();

        }

    }



}
