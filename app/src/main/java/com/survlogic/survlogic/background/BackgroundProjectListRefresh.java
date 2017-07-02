package com.survlogic.survlogic.background;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.Toast;

import com.survlogic.survlogic.R;
import com.survlogic.survlogic.adapter.ProjectListAdaptor;
import com.survlogic.survlogic.database.ProjectDatabaseHandler;
import com.survlogic.survlogic.model.Project;

import java.util.ArrayList;

/**
 * Created by chrisfillmore on 6/29/2017.
 */

public class BackgroundProjectListRefresh extends AsyncTask <Project,Project,String> {

    private String TAG = getClass().getSimpleName();

    private Context context;
    private Activity activity;

    RecyclerView recyclerView;
    ProjectListAdaptor adapter;
    RecyclerView.LayoutManager layoutManager;

    SwipeRefreshLayout swipeRefreshLayout;

    ArrayList<Project> arrayList = new ArrayList<Project>();

    public BackgroundProjectListRefresh(Context context, SwipeRefreshLayout swipeRefreshLayout) {
        this.context = context;
        this.swipeRefreshLayout = swipeRefreshLayout;

        activity = (Activity) context;

    }


    @Override
    protected String doInBackground(Project... params) {
        try{
            ProjectDatabaseHandler projectDb = new ProjectDatabaseHandler(context);
            SQLiteDatabase db = projectDb.getReadableDatabase();

            final ArrayList<Project> projects = new ArrayList<Project>(projectDb.getProjectsAll(db));

            for(Project project:projects){
                publishProgress(project);
            }


            //

        }catch (Exception e){
            e.printStackTrace();
        }


        return "get_info";


    }

    @Override
    protected void onPreExecute() {
        initProjectListRecyclerView();

        if(!swipeRefreshLayout.isRefreshing()){
            swipeRefreshLayout.setRefreshing(true);
        }


        super.onPreExecute();

    }

    @Override
    protected void onProgressUpdate(Project... values) {
        super.onProgressUpdate(values);

        arrayList.add(values[0]);
        adapter.notifyDataSetChanged();
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);

        if(swipeRefreshLayout.isRefreshing()){
            swipeRefreshLayout.setRefreshing(false);
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


    private void initProjectListRecyclerView(){

        Log.e(TAG,"Complete: initProjectListRecyclerView");

        recyclerView = (RecyclerView) activity.findViewById(R.id.recycler_view_in_content_project_view);

        layoutManager = new LinearLayoutManager(context);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(false);

        adapter = new ProjectListAdaptor(context,arrayList);
        recyclerView.setAdapter(adapter);

        Log.e(TAG,"Complete: initProjectListRecyclerView");

    }

}
