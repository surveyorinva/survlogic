package com.survlogic.survlogic.background;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.Toast;

import com.survlogic.survlogic.R;
import com.survlogic.survlogic.adapter.ProjectListAdaptor;
import com.survlogic.survlogic.database.ProjectDatabaseHandler;
import com.survlogic.survlogic.interf.ProjectListListener;
import com.survlogic.survlogic.model.Project;

import java.util.ArrayList;

/**
 * Created by chrisfillmore on 6/29/2017.
 */

public class BackgroundProjectListFromActivity extends AsyncTask <Project,Project,ArrayList<Project>> {

    private static final String TAG = "BackgroundProjectList";
    private ProgressDialog dialog;

    private Context context;
    private Activity activity;

    RecyclerView recyclerView;
    ProjectListAdaptor adapter;
    RecyclerView.LayoutManager layoutManager;

    ProjectListListener projectListListener;
    ArrayList<Project> arrayList = new ArrayList<Project>();

    public BackgroundProjectListFromActivity(Context context, ProjectListListener projectListListener) {
        this.context = context;

        activity = (Activity) context;
        this.projectListListener = projectListListener;
    }


    @Override
    protected ArrayList<Project> doInBackground(Project... params) {
        try{
            Log.d(TAG, "doInBackground: Connecting to db");
            ProjectDatabaseHandler projectDb = new ProjectDatabaseHandler(context);
            SQLiteDatabase db = projectDb.getReadableDatabase();

            final ArrayList<Project> projects = new ArrayList<Project>(projectDb.getProjectsAll(db));

            for(Project project:projects){
                publishProgress(project);
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
    protected void onProgressUpdate(Project... values) {
        super.onProgressUpdate(values);

        arrayList.add(values[0]);

    }

    @Override
    protected void onPostExecute(ArrayList<Project> result) {
        super.onPostExecute(result);

        if (projectListListener != null){
            Log.d(TAG, "Points Geodetic Loaded: " + arrayList.size());
            projectListListener.getProjectList(arrayList);
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
