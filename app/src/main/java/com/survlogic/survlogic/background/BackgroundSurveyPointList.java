package com.survlogic.survlogic.background;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.Toast;

import com.survlogic.survlogic.R;
import com.survlogic.survlogic.adapter.PointSurveyTableDataAdapter;
import com.survlogic.survlogic.adapter.ProjectListAdaptor;
import com.survlogic.survlogic.database.JobDatabaseHandler;
import com.survlogic.survlogic.model.PointSurvey;
import com.survlogic.survlogic.view.SortablePointSurveyTableView;

import java.util.ArrayList;

/**
 * Created by chrisfillmore on 6/29/2017.
 */

public class BackgroundSurveyPointList extends AsyncTask <PointSurvey,PointSurvey,String> {

    private static final String TAG = "BackgroundProjectList";
    private ProgressDialog dialog;

    private Context context;
    private Activity activity;

    private String DB_NAME;

    RecyclerView recyclerView;
    ProjectListAdaptor adapter;
    RecyclerView.LayoutManager layoutManager;

    ArrayList<PointSurvey> arrayList = new ArrayList<PointSurvey>();

    public BackgroundSurveyPointList(Context context, String DB_NAME) {
        this.context = context;
        this.dialog = new ProgressDialog(context);
        this.DB_NAME = DB_NAME;

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


        initSurveyPointListRecyclerView();

    }

    private void showToast(String data, boolean shortTime) {

        if (shortTime) {
            Toast.makeText(context, data, Toast.LENGTH_SHORT).show();

        } else{
            Toast.makeText(context, data, Toast.LENGTH_LONG).show();

        }

    }


    private void initSurveyPointListRecyclerView() {

        final SortablePointSurveyTableView pointSurveyTableView;

        pointSurveyTableView = (SortablePointSurveyTableView) activity.findViewById(R.id.tableView_for_Points_Survey);

        if (pointSurveyTableView != null) {

            final PointSurveyTableDataAdapter adapter = new PointSurveyTableDataAdapter(context, arrayList, pointSurveyTableView,12);
            pointSurveyTableView.setDataAdapter(adapter);
            //Add Click and Long Click here


        }
    }


}
