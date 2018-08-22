package com.survlogic.survlogic.ARvS.utils;

import android.app.Activity;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.support.v7.widget.CardView;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.View;
import android.widget.GridView;
import android.widget.Toast;

import com.survlogic.survlogic.R;
import com.survlogic.survlogic.adapter.PointGridImageAdapter;
import com.survlogic.survlogic.adapter.PointGridSketchAdapter;
import com.survlogic.survlogic.database.JobDatabaseHandler;
import com.survlogic.survlogic.database.ProjectDatabaseHandler;
import com.survlogic.survlogic.model.JobSketch;
import com.survlogic.survlogic.model.PointSurvey;
import com.survlogic.survlogic.model.ProjectImages;
import com.survlogic.survlogic.model.ProjectJobs;

import java.util.ArrayList;

public class DialogPhotoGridHelper {
    private static final String TAG = "DialogPhotoGridHelper";

    private Context mContext;

    //In Variables
    private ProjectJobs projectJob;
    private PointSurvey pointSurvey;

    //
    private int mJobId, mProjectId;
    private int mPointId;
    private String mJobDbName;

    //Widgets
    private GridView mGridView, sketchGridView;
    private CardView mCardViewPhotos;
    private PointGridImageAdapter gridAdapter;
    private PointGridSketchAdapter gridSketchAdapter;


    //Method Helpers
    private boolean isGridViewShown = false;

    private static final int DELAY_TO_GRID = 1500;

    private Handler gridHandler, sketchHandler;
    private boolean isLoading = false;

    private String mURLSyntex = "file://";

    public DialogPhotoGridHelper(Context context) {
        this.mContext = context;

    }

    //---------------------------------------------------------------------------------------------- getter/setter

    public void setProjectJob(ProjectJobs projectJob, GridView gridView, CardView container) {
        this.projectJob = projectJob;

        mJobId = projectJob.getId();
        mProjectId = projectJob.getProjectId();
        mJobDbName = projectJob.getmJobDbName();

        mGridView = gridView;
        mCardViewPhotos = container;
    }

    public void setPointSurveyPoint(long pointId) {
        mPointId = (int) pointId;

    }


    public void buildGridViews(){
        showPhotoGridView();
    }

    public void refreshGridViews(){
        refreshGridView();

    }

    //---------------------------------------------------------------------------------------------- Photo and Sketch Recovery

    /**
     * Grid View (GV)
     */

    public void showPhotoGridView(){
        Log.d(TAG, "showPhotoGridView: Started...");
        isLoading = true;

        gridHandler = new Handler();
        gridHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                initPhotoGridView();
                isLoading = false;
            }
        },DELAY_TO_GRID);
    }

    public void showSketchGridView(){
        Log.d(TAG, "showSketchGridView: Started...");
        isLoading  = true;

        sketchHandler = new Handler();
        sketchHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                initSketchGridView();
                isLoading = false;
            }
        },DELAY_TO_GRID);
    }


    //----------------------------------------------------------------------------------------------//
    private void initPhotoGridView(){
        Log.d(TAG, "initPhotoGridView: Started");

        if(getImageCount(mProjectId, mJobId, mPointId)){
            gridAdapter = new PointGridImageAdapter(mContext, R.layout.layout_grid_imageview, mURLSyntex, getImageFromPointData(mProjectId, mJobId, mPointId));
            mGridView.setAdapter(gridAdapter);
        }
    }

    private void initSketchGridView(){
        Log.d(TAG, "initSketchGridView: Started...");

        if(getSketchCount(mPointId)){
            gridSketchAdapter = new PointGridSketchAdapter(mContext, R.layout.layout_grid_imageview, mURLSyntex, getSketchFromPointData(mPointId));
            sketchGridView.setAdapter(gridSketchAdapter);

        }

    }

    private void refreshGridView() {
        showToast("Refreshing Grid...", true);

        if(getImageCount(mProjectId, mJobId, mPointId)){
            showToast("Found new images!", true);
            gridAdapter.clear();

            gridAdapter = new PointGridImageAdapter(mContext, R.layout.layout_grid_imageview, mURLSyntex, getImageFromPointData(mProjectId, mJobId, mPointId));
            mGridView.setAdapter(gridAdapter);


            TransitionManager.beginDelayedTransition(mCardViewPhotos);
            mGridView.setVisibility(View.VISIBLE);

        }
    }


    private ArrayList<ProjectImages> getImageFromPointData(int projectId, int jobId, int pointId){
        Log.d(TAG, "getImageFromProjectData: Connecting to db");
        ProjectDatabaseHandler projectDb = new ProjectDatabaseHandler(mContext);
        SQLiteDatabase db = projectDb.getReadableDatabase();

        ArrayList<ProjectImages> projectImages = new ArrayList<ProjectImages>(projectDb.getProjectImagesbyPointID(db,projectId, jobId, pointId));


        Log.d(TAG, "getImageFromProjectData: Closing DB Connection");
        db.close();

        return projectImages;

    }

    private ArrayList<JobSketch> getSketchFromPointData(int pointId){
        Log.d(TAG, "getSketchFromPointData: Connecting to db");

        JobDatabaseHandler jobDb = new JobDatabaseHandler(mContext, mJobDbName);
        SQLiteDatabase db = jobDb.getReadableDatabase();

        ArrayList<JobSketch> jobSketches = new ArrayList<>(jobDb.getJobSketchesByPointID(db,pointId));

        Log.d(TAG, "getSketchFromPointData: Closing DB Connection");
        db.close();

        return jobSketches;
    }

    private boolean getImageCount(int projectId, int jobId, int pointId){
        Log.d(TAG, "getImageCount: Connecting to db");
        long count = 0;
        boolean results = false;
        ProjectDatabaseHandler projectDb = new ProjectDatabaseHandler(mContext);
        SQLiteDatabase db = projectDb.getReadableDatabase();

        count = ProjectDatabaseHandler.getCountProjectImagesByPointID(db,projectId, jobId, pointId);
        Log.d(TAG, "getImageCount: Count = " + count);

        if (count !=0){
            results = true;
        }

        Log.d(TAG, "getImageCount: Closing DB Connection");
        db.close();
        Log.d(TAG, "getImageCount: Results: " + results);
        return results;
    }

    private boolean getSketchCount(int pointId){
        Log.d(TAG, "getSketchCount: Connecting to db");
        long count = 0;
        boolean results = false;
        JobDatabaseHandler jobDb = new JobDatabaseHandler(mContext, mJobDbName);
        SQLiteDatabase db = jobDb.getReadableDatabase();

        count = JobDatabaseHandler.getCountJobSketchByPointID(db,pointId);
        Log.d(TAG, "getSketchCount: Count = " + count);

        if (count !=0){
            results = true;
        }

        Log.d(TAG, "getSketchCount: Closing DB Connection");
        db.close();

        return results;
    }

    //----------------------------------------------------------------------------------------------//


    private void showToast(String data, boolean shortTime) {

        if (shortTime) {
            Toast.makeText(mContext, data, Toast.LENGTH_SHORT).show();

        } else{
            Toast.makeText(mContext, data, Toast.LENGTH_LONG).show();

        }
    }

}
