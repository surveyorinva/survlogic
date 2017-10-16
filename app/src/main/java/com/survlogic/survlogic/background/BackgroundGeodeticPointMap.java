package com.survlogic.survlogic.background;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Point;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.Toast;

import com.survlogic.survlogic.R;
import com.survlogic.survlogic.adapter.ProjectListAdaptor;
import com.survlogic.survlogic.database.JobDatabaseHandler;
import com.survlogic.survlogic.interf.JobPointsListener;
import com.survlogic.survlogic.model.PointGeodetic;
import com.survlogic.survlogic.model.PointSurvey;
import com.survlogic.survlogic.view.PlanarMapView;

import java.util.ArrayList;

/**
 * Created by chrisfillmore on 6/29/2017.
 */

public class BackgroundGeodeticPointMap extends AsyncTask <PointGeodetic,PointGeodetic,ArrayList<PointGeodetic>> {

    private static final String TAG = "BackgroundGeodeticPoint";
    private ProgressDialog dialog;

    private Context context;
    private Activity activity;

    private String DB_NAME;

    private final JobPointsListener mListener;

    RecyclerView recyclerView;
    ProjectListAdaptor adapter;
    RecyclerView.LayoutManager layoutManager;

    ArrayList<PointGeodetic> arrayList = new ArrayList<PointGeodetic>();


    public BackgroundGeodeticPointMap(Context context, String DB_NAME, JobPointsListener listener) {
        this.context = context;
        this.dialog = new ProgressDialog(context);
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

            Log.d(TAG, "doInBackground: Closing connection to DB");
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
