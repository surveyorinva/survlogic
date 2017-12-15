package com.survlogic.survlogic.background;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.Toast;

import com.survlogic.survlogic.adapter.ProjectListAdaptor;
import com.survlogic.survlogic.database.JobDatabaseHandler;
import com.survlogic.survlogic.interf.DatabasePointsFromAsyncListener;
import com.survlogic.survlogic.model.PointSurvey;
import com.survlogic.survlogic.view.PlanarMapView;

import java.util.ArrayList;

/**
 * Created by chrisfillmore on 6/29/2017.
 */

public class BackgroundSurveyPointGetForActivity extends AsyncTask <PointSurvey,PointSurvey,String> {

    private static final String TAG = "BackgroundProjectList";
    private ProgressDialog dialog;

    private Context context;
    private Activity activity;

    private final DatabasePointsFromAsyncListener mListener;
    private String DB_NAME;

    RecyclerView recyclerView;
    ProjectListAdaptor adapter;
    RecyclerView.LayoutManager layoutManager;

    ArrayList<PointSurvey> arrayList = new ArrayList<PointSurvey>();

    PlanarMapView planarMapView;

    public BackgroundSurveyPointGetForActivity(Context context, String DB_NAME, DatabasePointsFromAsyncListener listener) {
        this.context = context;
        this.dialog = new ProgressDialog(context);
        this.DB_NAME = DB_NAME;

        mListener = listener;
        activity = (Activity) context;
    }


    @Override
    protected String doInBackground(PointSurvey... params) {
        try{
            Log.d(TAG, "doInBackground: Connecting to db");

            JobDatabaseHandler jobDb = new JobDatabaseHandler(context, DB_NAME);
            SQLiteDatabase db = jobDb.getReadableDatabase();

            final ArrayList<PointSurvey> points = new ArrayList<PointSurvey>(jobDb.getPointSurveysAll(db));

            for(PointSurvey point:points){
                publishProgress(point);
            }

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

        dialog.setMessage("Retrieving ");
        dialog.setIndeterminate(true);
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setCancelable(false);
        dialog.show();




        super.onPreExecute();

    }

    @Override
    protected void onProgressUpdate(PointSurvey... values) {
        super.onProgressUpdate(values);

        arrayList.add(values[0]);
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);

        if (dialog.isShowing()) {
            dialog.dismiss();
        }


        if (mListener != null){
            Log.d(TAG, "Points Survey Loaded: " + arrayList.size());
            mListener.getPointsSurvey(arrayList);
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
