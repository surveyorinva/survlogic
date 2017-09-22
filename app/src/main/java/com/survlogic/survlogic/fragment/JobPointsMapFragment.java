package com.survlogic.survlogic.fragment;


import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.survlogic.survlogic.R;
import com.survlogic.survlogic.background.BackgroundSurveyPointList;
import com.survlogic.survlogic.background.BackgroundSurveyPointMap;
import com.survlogic.survlogic.dialog.DialogJobPointEntryAdd;
import com.survlogic.survlogic.dialog.DialogJobPointView;
import com.survlogic.survlogic.model.PointSurvey;
import com.survlogic.survlogic.view.SortablePointSurveyTableView;

import de.codecrafters.tableview.listeners.SwipeToRefreshListener;
import de.codecrafters.tableview.listeners.TableDataClickListener;

/**
 * Created by chrisfillmore on 5/2/2017.
 */

public class JobPointsMapFragment extends Fragment {

    private static final String TAG = "JobPointsMapFragment";


    private View v;
    private static final int DELAY_TO_MAP = 300;
    private int project_id, job_id;
    private String jobDatabaseName;



    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.fragment_job_points_map, container, false);


        initViewWidgets(v);
        setOnClickListener(v);


        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: Started...");

        showPlanarMap();


    }

    private void initViewWidgets(View v){
        Log.d(TAG, "initViewWidgets: Starting...");
        Bundle extras = getArguments();
        project_id = extras.getInt(getString(R.string.KEY_PROJECT_ID));
        job_id = extras.getInt(getString(R.string.KEY_JOB_ID));
        jobDatabaseName = extras.getString(getString(R.string.KEY_JOB_DATABASE));
        Log.d(TAG, "Database: " + jobDatabaseName);


    }


    private void setOnClickListener(View v){
        Log.d(TAG, "setOnClickListener: Starting...");


    }

    private void showPlanarMap(){

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                initPlanarMap();
            }
        },DELAY_TO_MAP);

    }

    private void initPlanarMap(){
        Log.d(TAG, "initPlanarMap: Started...");
        BackgroundSurveyPointMap backgroundSurveyPointMap = new BackgroundSurveyPointMap(getActivity(),jobDatabaseName);
        backgroundSurveyPointMap.execute();

    }
    

}