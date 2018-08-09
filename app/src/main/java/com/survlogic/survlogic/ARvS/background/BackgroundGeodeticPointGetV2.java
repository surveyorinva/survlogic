package com.survlogic.survlogic.ARvS.background;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.Toast;

import com.survlogic.survlogic.ARvS.interf.GeodeticPointsGetter;
import com.survlogic.survlogic.adapter.ProjectListAdaptor;
import com.survlogic.survlogic.database.JobDatabaseHandler;
import com.survlogic.survlogic.interf.JobPointsMapListener;
import com.survlogic.survlogic.model.PointGeodetic;

import java.util.ArrayList;

/**
 * Created by chrisfillmore on 6/29/2017.
 */

public class BackgroundGeodeticPointGetV2 extends AsyncTask <PointGeodetic,PointGeodetic,ArrayList<PointGeodetic>> {

    private static final String TAG = "BackgroundGeodeticPoint";
    private Context context;
    private Activity activity;

    private String DB_NAME;

    private final GeodeticPointsGetter mListener;


    ArrayList<PointGeodetic> arrayList = new ArrayList<PointGeodetic>();


    public BackgroundGeodeticPointGetV2(Context context, String DB_NAME, GeodeticPointsGetter listener) {
        this.context = context;
        this.DB_NAME = DB_NAME;

        mListener = listener;
        activity = (Activity) context;
    }


    @Override
    protected ArrayList<PointGeodetic> doInBackground(PointGeodetic... params) {
        try{
            Log.d(TAG, "doInBackground: Started");

            JobDatabaseHandler jobDb = new JobDatabaseHandler(context, DB_NAME);
            SQLiteDatabase db = jobDb.getReadableDatabase();

            final ArrayList<PointGeodetic> points = new ArrayList<PointGeodetic>(jobDb.getPointGeodeticAll(db));

            for(PointGeodetic point:points){
                publishProgress(point);
            }

            db.close();
            //

        }catch (Exception e){
            e.printStackTrace();
        }
        return arrayList;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

    }

    @Override
    protected void onProgressUpdate(PointGeodetic... values) {
        super.onProgressUpdate(values);

        arrayList.add(values[0]);
    }

    @Override
    protected void onPostExecute(ArrayList<PointGeodetic> result) {
        super.onPostExecute(result);

        if (mListener != null){
            Log.d(TAG, "Points Geodetic Loaded: " + arrayList.size());
            mListener.getPointsGeodetic(arrayList);
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
