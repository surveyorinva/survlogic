package com.survlogic.survlogic.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;

import com.survlogic.survlogic.R;
import com.survlogic.survlogic.adapter.ActivityJobCogoSetupTypeAdapter;
import com.survlogic.survlogic.background.BackgroundGeodeticPointGet;
import com.survlogic.survlogic.background.BackgroundSurveyPointGet;
import com.survlogic.survlogic.fragment.JobCogoSetupKnownFragment;
import com.survlogic.survlogic.fragment.JobCogoSetupOrientationFragment;
import com.survlogic.survlogic.interf.JobCogoFragmentListener;
import com.survlogic.survlogic.interf.JobCogoSetupPointListListener;
import com.survlogic.survlogic.interf.JobPointsMapListener;
import com.survlogic.survlogic.model.JobInformation;
import com.survlogic.survlogic.model.PointGeodetic;
import com.survlogic.survlogic.model.PointSurvey;
import com.survlogic.survlogic.model.ProjectJobSettings;

import java.util.ArrayList;

/**
 * Created by chrisfillmore on 11/2/2017.
 */

public class JobCogoSetupActivity extends AppCompatActivity implements JobPointsMapListener, JobCogoFragmentListener, JobCogoSetupPointListListener {
    private static final String TAG = "JobCogoFragmentSetupAct";
    private Context mContext;
    private ActivityJobCogoSetupTypeAdapter tabAdapter;

    private ArrayList<PointSurvey> lstPointSurvey = new ArrayList<>();
    private ArrayList<PointGeodetic> lstPointGeodetic = new ArrayList<>();

    private PointSurvey occupyPointSurvey, backsightPointSurvey;
    private double occupyHeight, backsightHeight;
    private ProjectJobSettings jobSettings;
    private JobInformation jobInformation = new JobInformation();
    private int occupyPointNo, backsightPointNo;

    private int project_id, job_id;
    private String jobDatabaseName;

    private ImageButton ibClose;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: Starting.>");
        setContentView(R.layout.activity_job_cogo_setup);
        mContext = JobCogoSetupActivity.this;

        initViewWidgets();

        initPointDataInBackground();

        initTabView();
        setOnClickListener();
    }

    private void initViewWidgets() {
        Log.d(TAG, "initViewWidgets: Started...");

        Bundle extras = getIntent().getExtras();
        project_id = extras.getInt(getString(R.string.KEY_PROJECT_ID));
        job_id = extras.getInt(getString(R.string.KEY_JOB_ID));
        jobDatabaseName = extras.getString(getString(R.string.KEY_JOB_DATABASE));
        occupyPointNo = extras.getInt("KEY_SETUP_OCCUPY_PT");
        backsightPointNo = extras.getInt("KEY_SETUP_BACKSIGHT_PT");
        occupyHeight = extras.getDouble("KEY_SETUP_OCCUPY_HT");
        backsightHeight = extras.getDouble("KEY_SETUP_BACKSIGHT_HT");

        Log.d(TAG, "||Project_ID in Cogo Setup|| : " + project_id);
        Log.d(TAG, "||Job_ID in Cogo Setup|| : " + job_id);
        Log.d(TAG, "||Database in Cogo Setup|| : " + jobDatabaseName);

        Log.d(TAG, "||Occupy Point No|| " + occupyPointNo);
        Log.d(TAG, "||Backsight Point No|| " + backsightPointNo);
        Log.d(TAG, "||Occupy Height|| " + occupyHeight);
        Log.d(TAG, "||Backsight Height|| " + backsightHeight);

        jobInformation.setProject_id(project_id);
        jobInformation.setJob_id(job_id);
        jobInformation.setJobDatabaseName(jobDatabaseName);

        ibClose = (ImageButton) findViewById(R.id.dialog_close);

    }

    private void initTabView(){
        tabAdapter = new ActivityJobCogoSetupTypeAdapter(getSupportFragmentManager());
        ViewPager viewPager = (ViewPager) findViewById(R.id.pager);
        viewPager.setAdapter(tabAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabDots);
        tabLayout.setupWithViewPager(viewPager, true);


    }


    private void setOnClickListener(){
        Log.d(TAG, "setOnClickListener: Started...");

        ibClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


    }

    private void initPointDataInBackground(){
        //Point Survey Load
        loadPointSurveyInBackground();

        //Point Geodetic Load
        loadPointGeodeticInBackground();

    }

    private void loadPointGeodeticInBackground(){
        Log.d(TAG, "loadPointGeodeticInBackground: Started...");
        BackgroundGeodeticPointGet backgroundGeodeticPointGet = new BackgroundGeodeticPointGet(mContext, jobDatabaseName, this);
        backgroundGeodeticPointGet.execute();

    }

    private void loadPointSurveyInBackground(){
        Log.d(TAG, "loadPointSurveyInBackground: Started...");

        BackgroundSurveyPointGet backgroundSurveyPointGet = new BackgroundSurveyPointGet(mContext, jobDatabaseName, this);
        backgroundSurveyPointGet.execute();
    }

    //-----------------------------------------------------------------------------------------------//
    private void returnResults(){

        Intent returnIntent = new Intent();
        returnIntent.putExtra(getString(R.string.KEY_SETUP_OCCUPY_PT),occupyPointSurvey.getPoint_no());
        returnIntent.putExtra(getString(R.string.KEY_SETUP_BACKSIGHT_PT),backsightPointSurvey.getPoint_no());
        returnIntent.putExtra(getString(R.string.KEY_SETUP_OCCUPY_HT),occupyHeight);
        returnIntent.putExtra(getString(R.string.KEY_SETUP_BACKSIGHT_HT),backsightHeight);


        setResult(Activity.RESULT_OK,returnIntent);
        finish();

    }


    //-----------------------------------------------------------------------------------------------//
    @Override
    public void getPointsGeodetic(ArrayList<PointGeodetic> lstPointGeodetics) {
        this.lstPointGeodetic = lstPointGeodetics;

    }

    @Override
    public void getPointsSurvey(ArrayList<PointSurvey> lstPointSurvey) {
        Log.d(TAG, "getPointsSurvey: Started");
        this.lstPointSurvey = lstPointSurvey;

        Log.d(TAG, "initPreDefinedSetup:getPointsSurvey: Size: " + lstPointSurvey.size());

        JobCogoSetupKnownFragment jobCogoSetupKnownFragment = tabAdapter.getJobCogoSetupKnownFragment();
        JobCogoSetupOrientationFragment jobCogoSetupOrientationFragment = tabAdapter.getJobCogoSetupOrientationFragment();

        if(jobCogoSetupKnownFragment !=null) {
            Log.d(TAG, "initPreDefinedSetup:jobCogoSetupKnownFragment: Not Null");
            jobCogoSetupKnownFragment.setPreDefinedSetup();
        }

        if(jobCogoSetupOrientationFragment !=null){
            Log.d(TAG, "jobCogoSetupOrientationFragment: Not Null");
        }

    }

    @Override
    public void isMapSelectorActive(boolean isSelected) {

    }

    @Override
    public void isMapSelectorOpen(boolean isSelected) {

    }

    @Override
    public ArrayList<PointSurvey> sendPointSurveyToFragment() {
        Log.d(TAG, "sendPointSurveyToFragment: Started...");
        return lstPointSurvey;
    }

    @Override
    public JobInformation sendJobInformationToFragment() {
        return jobInformation;
    }


    @Override
    public PointSurvey sendOccupyPointSurveyToFragment() {
        return null;
    }

    @Override
    public PointSurvey sendBacksightPointSurveyToFragment() {
        return null;
    }

    @Override
    public PointSurvey sendOccupyPointSurveyToFragment(PointSurvey pointSurvey) {
        return pointSurvey;
    }

    @Override
    public PointSurvey sendBacksightPointSurveyToFragment(PointSurvey pointSurvey) {
        return pointSurvey;
    }

    @Override
    public int sendOccupyPointNoToFragment() {
        return this.occupyPointNo;
    }

    @Override
    public int sendBacksightPointNoToFragment() {
        return this.backsightPointNo;
    }

    @Override
    public double sendOccupyHeightToFragment() {
        return this.occupyHeight;
    }

    @Override
    public double sendBacksightHeightToFragment() {
        return this.backsightHeight;
    }

    @Override
    public void setOccupyPointSurveyFromFragment(PointSurvey pointSurvey) {
        this.occupyPointSurvey = pointSurvey;
    }

    @Override
    public void setBacksightPointSurveyFromFragment(PointSurvey pointSurvey) {
        this.backsightPointSurvey = pointSurvey;
    }

    @Override
    public void setOccupyHeightFromFragment(double measureUp) {
        this.occupyHeight = measureUp;
    }

    @Override
    public void setBacksightHeightFromFragment(double measureUp) {
        this.backsightHeight = measureUp;
    }

    @Override
    public void sendSetupToMainActivity() {
        returnResults();
    }

    @Override
    public void onReturnValuesOccupy(PointSurvey pointSurvey) {
        Log.d(TAG, "onReturnValuesOccupy: Started..>");

        JobCogoSetupKnownFragment jobCogoSetupKnownFragment = tabAdapter.getJobCogoSetupKnownFragment();
        JobCogoSetupOrientationFragment jobCogoSetupOrientationFragment = tabAdapter.getJobCogoSetupOrientationFragment();

        if(jobCogoSetupKnownFragment !=null) {
            Log.d(TAG, "jobCogoSetupKnownFragment: Not Null");
            jobCogoSetupKnownFragment.setPointSurveyFromPointSurvey(pointSurvey,true);
        }

        if(jobCogoSetupOrientationFragment !=null){
            Log.d(TAG, "jobCogoSetupOrientationFragment: Not Null");
        }

    }

    @Override
    public void onReturnValuesBacksight(PointSurvey pointSurvey) {
        Log.d(TAG, "onReturnValuesBacksight: Started...");

        JobCogoSetupKnownFragment jobCogoSetupKnownFragment = tabAdapter.getJobCogoSetupKnownFragment();
        JobCogoSetupOrientationFragment jobCogoSetupOrientationFragment = tabAdapter.getJobCogoSetupOrientationFragment();

        if(jobCogoSetupKnownFragment !=null) {
            Log.d(TAG, "jobCogoSetupKnownFragment: Not Null");
            jobCogoSetupKnownFragment.setPointSurveyFromPointSurvey(pointSurvey,false);
        }

        if(jobCogoSetupOrientationFragment !=null){
            Log.d(TAG, "jobCogoSetupOrientationFragment: Not Null");
        }

    }

    @Override
    public void sendTraverseSetupToMainActivity(int occupyPointNo, int backsightPointNo, double occupyHI, double backsightHI) {

    }

    @Override
    public void invalidatePointSurveyList() {

    }
}
