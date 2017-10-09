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

public class BackgroundSurveyPointListFromMap extends AsyncTask <PointSurvey,PointSurvey,String> {

    private static final String TAG = "BackgroundProjectList";
    private ProgressDialog dialog;

    private Context context;
    private Activity activity;

    private String DB_NAME;

    RecyclerView recyclerView;
    ProjectListAdaptor adapter;
    RecyclerView.LayoutManager layoutManager;

    private ArrayList<PointSurvey> points = new ArrayList<>();
    private ArrayList<PointSurvey> arrayList = new ArrayList<>();

    public BackgroundSurveyPointListFromMap(Context context, ArrayList<PointSurvey> arrayList) {
        this.context = context;
        this.dialog = new ProgressDialog(context);
        this.points = arrayList;

        activity = (Activity) context;
    }


    @Override
    protected String doInBackground(PointSurvey... params) {
        Log.d(TAG, "doInBackground: Started.");
        try{

            for(int i=0; i< points.size(); i++) {
                Log.d(TAG, "doInBackground: Point Added...");
                PointSurvey pointSurvey = points.get(i);
                publishProgress(pointSurvey);
            }



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
        Log.d(TAG, "onProgressUpdate: Addeding to table Array");
        arrayList.add(values[0]);
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        Log.d(TAG, "onPostExecute: Started");
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
        Log.d(TAG, "initSurveyPointListRecyclerView: Started...");
        
        final SortablePointSurveyTableView pointSurveyTableView;

        pointSurveyTableView = (SortablePointSurveyTableView) activity.findViewById(R.id.tableView_for_Points);

        if (pointSurveyTableView != null) {
            Log.d(TAG, "initSurveyPointListRecyclerView: Table View Not Null");
            final PointSurveyTableDataAdapter adapter = new PointSurveyTableDataAdapter(context, arrayList, pointSurveyTableView);
            pointSurveyTableView.setDataAdapter(adapter);
            //Add Click and Long Click here


        }else{
            Log.d(TAG, "initSurveyPointListRecyclerView: Table View Null");
        }
    }


}
