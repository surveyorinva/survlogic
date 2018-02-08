package com.survlogic.survlogic.background;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.survlogic.survlogic.R;
import com.survlogic.survlogic.adapter.ProjectJobListAdaptor;
import com.survlogic.survlogic.adapter.ProjectListAdaptor;
import com.survlogic.survlogic.database.ProjectDatabaseHandler;
import com.survlogic.survlogic.interf.ProjectDetailsActivityListener;
import com.survlogic.survlogic.model.Project;
import com.survlogic.survlogic.model.ProjectJobs;

import java.util.ArrayList;

/**
 * Created by chrisfillmore on 6/29/2017.
 */

public class BackgroundProjectJobList extends AsyncTask <ProjectJobs,ProjectJobs,String> {

    private static final String TAG = "BackgroundProjectJobLis";
    private ProgressDialog dialog;

    private Context context;
    private Activity activity;

    private ArrayList<ProjectJobs> arrayList = new ArrayList<ProjectJobs>();

    private int project_id;

    public BackgroundProjectJobList(Context context, int project_id) {
        Log.d(TAG, "BackgroundProjectJobList: Started");
        this.context = context;
        this.dialog = new ProgressDialog(context);
        this.project_id = project_id;

        activity = (Activity) context;
    }


    @Override
    protected String doInBackground(ProjectJobs... params) {
        try{
            Log.d(TAG, "doInBackground: Connecting to db");
            ProjectDatabaseHandler projectDb = new ProjectDatabaseHandler(context);
            SQLiteDatabase db = projectDb.getReadableDatabase();

            final ArrayList<ProjectJobs> projectJobs = new ArrayList<ProjectJobs>(projectDb.getProjectJobsByProjectID(db,project_id));

            for(ProjectJobs projectJob:projectJobs){
                publishProgress(projectJob);
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
        super.onPreExecute();

    }

    @Override
    protected void onProgressUpdate(ProjectJobs... values) {
        super.onProgressUpdate(values);

        arrayList.add(values[0]);
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);

        ProjectDetailsActivityListener listener = (ProjectDetailsActivityListener) activity;
        listener.refreshJobBoard(arrayList);

        if (dialog.isShowing()) {
            dialog.dismiss();
        }

        if (result.equals("get_info")) {

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
