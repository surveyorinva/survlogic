package com.survlogic.survlogic.fragment;



import android.app.DialogFragment;
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
import com.survlogic.survlogic.dialog.DialogJobPointEntryAdd;
import com.survlogic.survlogic.dialog.DialogJobPointView;
import com.survlogic.survlogic.model.PointSurvey;
import com.survlogic.survlogic.view.SortablePointSurveyTableView;

import de.codecrafters.tableview.listeners.SwipeToRefreshListener;
import de.codecrafters.tableview.listeners.TableDataClickListener;

/**
 * Created by chrisfillmore on 5/2/2017.
 */

public class JobPointsListFragment extends Fragment {

    private static final String TAG = "JobPointsListFragment";
    private static final int DELAY_TO_LIST = 300;
    private static final int DELAY_TO_REFRESH = 2000;

    View v;

    private int project_id, job_id, job_settings_id = 1;
    private String jobDatabaseName;

    private SortablePointSurveyTableView pointSurveyTableView;
    private FloatingActionButton fabNewPoint;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.fragment_job_points_list, container, false);


        initViewWidgets(v);
        setOnClickListener(v);


        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: Started...");

        showPointsLocal();


    }

    private void initViewWidgets(View v){
        Log.d(TAG, "initViewWidgets: Starting...");
        Bundle extras = getArguments();
        project_id = extras.getInt("PROJECT_ID");
        job_id = extras.getInt("JOB_ID");
        jobDatabaseName = extras.getString("JOB_DB_NAME");
        Log.d(TAG, "Database: " + jobDatabaseName);


        pointSurveyTableView = (SortablePointSurveyTableView) v.findViewById(R.id.tableView_for_Points);
        fabNewPoint = (FloatingActionButton) v.findViewById(R.id.fab_in_job_points);


    }


    private void setOnClickListener(View v){
        Log.d(TAG, "setOnClickListener: Starting...");
        fabNewPoint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createPointEntry(project_id,job_id,jobDatabaseName);
            }
        });


        pointSurveyTableView.setSwipeToRefreshEnabled(true);
        pointSurveyTableView.setSwipeToRefreshListener(new SwipeToRefreshListener() {
            @Override
            public void onRefresh(final RefreshIndicator refreshIndicator) {
                pointSurveyTableView.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        refreshIndicator.hide();

                    }
                }, DELAY_TO_REFRESH);
            }
        });

        pointSurveyTableView.addDataClickListener(new PointClickListener());


    }

    private void createPointEntry(int project_id, int job_id, String databaseName){
        Log.d(TAG, "createPointEntry: Starting...");
        android.support.v4.app.DialogFragment pointDialog = DialogJobPointEntryAdd.newInstance(R.string.dialog_job_point_name, project_id, job_id, databaseName);
        pointDialog.show(getFragmentManager(),"dialog");

    }


    private void showPointsLocal(){

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                initPointsLocal();
            }
        },DELAY_TO_LIST);

    }

    private void initPointsLocal(){

        BackgroundSurveyPointList backgroundSurveyPointList = new BackgroundSurveyPointList(getActivity(),jobDatabaseName);
        backgroundSurveyPointList.execute();

    }

    private class PointClickListener implements TableDataClickListener<PointSurvey>{
        @Override
        public void onDataClicked(int rowIndex, PointSurvey clickedData) {
            Log.d(TAG, "onDataClicked: Started...");
            long point_id = clickedData.getId();
            int pointNo = clickedData.getPoint_no();

            Log.d(TAG, "onDataClicked: Point_id: " + point_id);

            android.support.v4.app.DialogFragment pointDialog = DialogJobPointView.newInstance(project_id, job_id, point_id, pointNo, jobDatabaseName);
            pointDialog.show(getFragmentManager(),"dialog");
        }
    }


}