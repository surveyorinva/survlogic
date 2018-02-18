package com.survlogic.survlogic.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;
import com.survlogic.survlogic.R;
import com.survlogic.survlogic.background.BackgroundGeodeticPointGet;
import com.survlogic.survlogic.background.BackgroundSurveyPointGet;
import com.survlogic.survlogic.dialog.DialogJobPointView;
import com.survlogic.survlogic.dialog.DialogProjectDescriptionAdd;
import com.survlogic.survlogic.fragment.JobPointsHomeFragment;
import com.survlogic.survlogic.fragment.JobPointsInverseFragment;
import com.survlogic.survlogic.fragment.JobPointsListFragment;
import com.survlogic.survlogic.fragment.JobPointsMapFragment;
import com.survlogic.survlogic.interf.JobMapOptionsListener;
import com.survlogic.survlogic.interf.JobPointsActivityListener;
import com.survlogic.survlogic.interf.JobPointsInversePointListListener;
import com.survlogic.survlogic.interf.JobPointsMapListener;
import com.survlogic.survlogic.model.Point;
import com.survlogic.survlogic.model.PointGeodetic;
import com.survlogic.survlogic.model.PointSurvey;
import com.survlogic.survlogic.model.ProjectJobSettings;
import com.survlogic.survlogic.utils.BottomNavigationViewHelper;
import com.survlogic.survlogic.utils.PreferenceLoaderHelper;
import com.survlogic.survlogic.utils.SurveyProjectionHelper;

import java.util.ArrayList;

/**
 * Created by chrisfillmore on 8/8/2017.
 */

public class JobPointsActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, JobPointsMapListener, JobMapOptionsListener, JobPointsActivityListener, JobPointsInversePointListListener{
    private static final String TAG = "JobPointsActivity";

    private static Context mContext;
    private Toolbar toolbar;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private NavigationView navigationView;

    private int ACTIVITY_NUM = 0;

    private BottomNavigationViewEx bottomNavigationViewEx;

    private JobPointsMapFragment jobPointsMapFragment;
    private JobPointsListFragment jobPointsListFragment;
    private JobPointsInverseFragment jobPointsInverseFragment;

    private ProjectJobSettings jobSettings;
    private int project_id, job_id, job_settings_id = 1;
    private String jobDatabaseName;

    private boolean listenForMapSelectionButtonsActive = false;
    private boolean listenForMapSelectionButtonsOpen = false;

    private FrameLayout container;
    private RelativeLayout rlLayout2;
    private ProgressBar progressBar;

    private ArrayList<PointSurvey> lstPointSurvey = new ArrayList<>();
    private ArrayList<PointGeodetic> lstPointGeodetic = new ArrayList<>();
    private ArrayList<PointSurvey> lstPointGrid = new ArrayList<>();

    private boolean isJobWithProjection = false;

    private PreferenceLoaderHelper preferenceLoaderHelper;
    SurveyProjectionHelper surveyProjectionHelper;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: Starting......>");

        setContentView(R.layout.activity_job_points);
        mContext = JobPointsActivity.this;

        preferenceLoaderHelper = new PreferenceLoaderHelper(mContext);
        surveyProjectionHelper = new SurveyProjectionHelper(mContext);

        initViewToolbar();
        initViewNavigation();
        initViewWidgets();
        initBottomNavigationView();
        initFragmentContainer(savedInstanceState);

        initProjection();
        initPointDataInBackground();
    }

    @Override
    protected void onResume() {
        super.onResume();
        rlLayout2.setVisibility(View.GONE);
    }

    @Override
    public void onBackPressed() {

            if(listenForMapSelectionButtonsOpen && listenForMapSelectionButtonsActive) {
                jobPointsMapFragment.closeActiveSelectionTools();

            }else if(listenForMapSelectionButtonsOpen && !listenForMapSelectionButtonsActive){
                jobPointsMapFragment.closeSubMenuSelectFab();

            }else {
                super.onBackPressed();
            }
    }

    //---------------------------------------------------------------------------------------------//

    /**
     * Toolbar and UI Methods
     */

    private void initViewToolbar(){
        toolbar = (Toolbar) findViewById(R.id.toolbar_in_job_view);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);


    }

    private void initViewNavigation() {
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_in_job);

        navigationView = (NavigationView) findViewById(R.id.navigation_view);
        navigationView.setNavigationItemSelectedListener(JobPointsActivity.this);

        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();

    }


    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Intent intent = new Intent();

        switch (item.getItemId()){
            case R.id.menu_item1_id:
                Intent i = new Intent(this, JobHomeActivity.class);
                i.putExtra(getString(R.string.KEY_PROJECT_ID),project_id);
                i.putExtra(getString(R.string.KEY_JOB_ID), job_id);
                i.putExtra(getString(R.string.KEY_JOB_DATABASE), jobDatabaseName);

                startActivity(i);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);

                break;

            case R.id.menu_item2_id:
                //Go To Points Menu
                Intent j = new Intent(this, JobPointsActivity.class);
                j.putExtra(getString(R.string.KEY_PROJECT_ID),project_id);
                j.putExtra(getString(R.string.KEY_JOB_ID), job_id);
                j.putExtra(getString(R.string.KEY_JOB_DATABASE), jobDatabaseName);

                startActivity(j);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);

                break;

            case R.id.menu_item3_id:
                //Go To Cogo Menu
                Intent k = new Intent(this, JobCogoActivity.class);
                k.putExtra(getString(R.string.KEY_PROJECT_ID),project_id);
                k.putExtra(getString(R.string.KEY_JOB_ID), job_id);
                k.putExtra(getString(R.string.KEY_JOB_DATABASE), jobDatabaseName);

                startActivity(k);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                break;

            case R.id.menu_item4_id:
                //Go To GPS Menu
                Intent l = new Intent(this, JobGpsActivity.class);
                l.putExtra(getString(R.string.KEY_PROJECT_ID),project_id);
                l.putExtra(getString(R.string.KEY_JOB_ID), job_id);
                l.putExtra(getString(R.string.KEY_JOB_DATABASE), jobDatabaseName);

                startActivity(l);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                break;

            case R.id.menu_item5_id:
                intent.setClass(this, SettingsCurrentJobActivity.class);
                startActivity(intent);

                break;

        }

        drawerLayout.closeDrawers();
        return false;

    }

    private void initViewWidgets(){
        Log.d(TAG, "initViewWidgets: Starting...");

        Bundle extras = getIntent().getExtras();
        project_id = extras.getInt(getString(R.string.KEY_PROJECT_ID));
        job_id = extras.getInt(getString(R.string.KEY_JOB_ID));
        jobDatabaseName = extras.getString(getString(R.string.KEY_JOB_DATABASE));
        Log.d(TAG, "||Database|| : " + jobDatabaseName);

        rlLayout2 = (RelativeLayout) findViewById(R.id.relLayout_2) ;
        progressBar = (ProgressBar) findViewById(R.id.progressStatus);

    }

    private void initBottomNavigationView(){
        Log.d(TAG, "initBottomNavigationView: Started");

        bottomNavigationViewEx = (BottomNavigationViewEx) findViewById(R.id.bottomNavViewBar);
        BottomNavigationViewHelper.setupBottomNavigationView(bottomNavigationViewEx);
        enableNavigationHome(mContext, bottomNavigationViewEx);
        Menu menu = bottomNavigationViewEx.getMenu();
        MenuItem menuItem = menu.getItem(ACTIVITY_NUM);
        menuItem.setChecked(false);


    }

    public void enableNavigationHome(final Context mContext, BottomNavigationViewEx view) {
        Log.d(TAG, "enableNavigationHome: Starting....");
        view.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                Menu menu = bottomNavigationViewEx.getMenu();
                MenuItem menuItem;

                switch (item.getItemId()) {

                    case R.id.navigation_item_1:
                        Log.d(TAG, "onNavigationItemSelected: Nav Item 1 - Start");
                        ACTIVITY_NUM = 0;
                        menuItem = menu.getItem(ACTIVITY_NUM);
                        menuItem.setChecked(false);

                        JobPointsHomeFragment containerFragment1 = new JobPointsHomeFragment();
                        containerFragment1.setArguments(getIntent().getExtras());

                        swapFragment(containerFragment1,false,"HOME");

                        break;

                    case R.id.navigation_item_2:
                        Log.d(TAG, "onNavigationItemSelected: Nav Item 2 - Start");
                        ACTIVITY_NUM = 1;
                        menuItem = menu.getItem(ACTIVITY_NUM);
                        menuItem.setChecked(false);

                        JobPointsListFragment containerFragment2 = new JobPointsListFragment();
                        containerFragment2.setArguments(getIntent().getExtras());

                        swapFragment(containerFragment2,false,"LIST");
                        jobPointsListFragment = containerFragment2;

                        jobPointsListFragment.setArrayListPointSurvey(lstPointSurvey);
                        jobPointsListFragment.setArrayListPointGeodetic(lstPointGeodetic);
                        jobPointsListFragment.setArrayListPointGrid(lstPointGrid);

                        break;

                    case R.id.navigation_item_3:
                        ACTIVITY_NUM = 2;
                        menuItem = menu.getItem(ACTIVITY_NUM);
                        menuItem.setChecked(false);


                        JobPointsMapFragment containerFragment3 = new JobPointsMapFragment();
                        containerFragment3.setArguments(getIntent().getExtras());

                        swapFragment(containerFragment3,false,"MAP_VIEW");
                        jobPointsMapFragment = containerFragment3;

                        jobPointsMapFragment.setArrayListPointSurvey(lstPointSurvey);
                        jobPointsMapFragment.setArrayListPointGeodetic(lstPointGeodetic);


                        break;

                    case R.id.navigation_item_4:
                        ACTIVITY_NUM = 3;
                        menuItem = menu.getItem(ACTIVITY_NUM);
                        menuItem.setChecked(false);


                        JobPointsInverseFragment containerFragment4 = new JobPointsInverseFragment();
                        containerFragment4.setArguments(getIntent().getExtras());

                        swapFragment(containerFragment4, false, "INVERSE_VIEW");
                        jobPointsInverseFragment = containerFragment4;

                        jobPointsInverseFragment.setArrayListPointSurvey(lstPointSurvey);
                        jobPointsInverseFragment.setArrayListPointGeodetic(lstPointGeodetic);

                        break;
                }


                return false;
            }
        });
    }


    private void initFragmentContainer(@Nullable Bundle savedInstanceState) {
        Log.d(TAG, "initFragmentContainer: Starting...");
        container = (FrameLayout) findViewById(R.id.container_in_job_points);

        if (container != null) {
            if (savedInstanceState != null) {
                return;
            }

            JobPointsHomeFragment containerFragment = new JobPointsHomeFragment();
            containerFragment.setArguments(getIntent().getExtras());

            FragmentManager fm = getSupportFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();
            ft.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
            ft.add(R.id.container_in_job_points, containerFragment, "HOME");
            ft.commit();


        }

    }
        private void swapFragment(Fragment fragment, boolean addToStack, String tag){
            Log.d(TAG, "swapFragment: Starting..");

            FragmentManager fm = getSupportFragmentManager();
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
            ft.replace(R.id.container_in_job_points,fragment);

            if(addToStack) {
                ft.addToBackStack(tag);
            }

            ft.commit();

        }

    private void initPointDataInBackground(){
        //Point Survey Load
        loadPointSurveyInBackground();

        //Point Geodetic Load
        //loadPointGeodeticInBackground();

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

    //----------------------------------------------------------------------------------------------//
    public void openPointEdit(long point_id, int pointNo){
        Log.d(TAG, "onDataClicked: Point_id: " + point_id);

        FragmentManager fm = getSupportFragmentManager();
        android.support.v4.app.DialogFragment pointDialog = DialogJobPointView.newInstance(project_id, job_id, point_id, pointNo, jobDatabaseName);
        pointDialog.show(fm,"dialog");
    }


    //-------------------------------------------------------------------------------------------------------------------------//


    /**
     * Projections
     */


    private void initProjection(){
        Log.d(TAG, "initProjection: Started");
        String projectionString, zoneString;

        int isProjection = 0;

        isProjection = preferenceLoaderHelper.getGeneral_over_projection();
        projectionString = preferenceLoaderHelper.getGeneral_over_projection_string();
        zoneString = preferenceLoaderHelper.getGeneral_over_zone_string();

        if(isProjection == 1){
            surveyProjectionHelper.setConfig(projectionString,zoneString);
        }


    }







    //----------------------------------------------------------------------------------------------//


    /**
     * Method Helpers
     */


    private void showToast(String data, boolean shortTime) {

        if (shortTime) {
            Toast.makeText(this, data, Toast.LENGTH_SHORT).show();

        } else{
            Toast.makeText(this, data, Toast.LENGTH_LONG).show();

        }
    }



    //----------------------------------------------------------------------------------------------//
    @Override
    public void isMapSelectorActive(boolean isSelected) {
        listenForMapSelectionButtonsActive = isSelected;
    }

    @Override
    public void isMapSelectorOpen(boolean isSelected) {
        listenForMapSelectionButtonsOpen = isSelected;
    }

    @Override
    public void onReturnValues(boolean pointNo, boolean pointElev, boolean pointDesc) {
        Log.d(TAG, "Listener:onReturnValues: Started");
        Log.i(TAG, "onReturnValues: (pointNo/pointElev/pointDesc:" + pointNo + "/" + pointElev + "/" + pointDesc);
        jobPointsMapFragment.getMapPointOptionsFromActivity(pointNo, pointElev, pointDesc);

    }

    @Override
    public void onSetMapType(boolean showPlanarView, int worldMapType) {
        Log.d(TAG, "Listener:onSetMapType: Started");

        jobPointsMapFragment.setMapView(showPlanarView,worldMapType);

    }

    @Override
    public void getPointsGeodetic(ArrayList<PointGeodetic> lstPointGeodetics) {
        Log.d(TAG, "getPointsGeodetic: Started");
        Log.d(TAG, "getPointsGeodetic: Before Size: " + this.lstPointGeodetic.size());

        this.lstPointGeodetic = lstPointGeodetics;

        if(jobPointsListFragment != null){
            jobPointsListFragment.setArrayListPointGeodetic(lstPointGeodetics);
            jobPointsListFragment.setArrayListPointGrid(surveyProjectionHelper.generateGridPoints(lstPointGeodetics));
        }


        Log.d(TAG, "getPointsGeodetic: After Size: " + this.lstPointGeodetic.size());

    }

    @Override
    public void getPointsSurvey(ArrayList<PointSurvey> lstPointSurvey) {
        Log.d(TAG, "getPointsSurvey: Started");
        Log.d(TAG, "getPointsSurvey: Before Size: " + this.lstPointSurvey.size());

        this.lstPointSurvey = lstPointSurvey;
        if(jobPointsListFragment !=null){
            jobPointsListFragment.setArrayListPointSurvey(lstPointSurvey);

        }


        Log.d(TAG, "getPointsSurvey: After Size: " + this.lstPointSurvey.size());
        loadPointGeodeticInBackground();
    }


    @Override
    public void refreshPointArrays() {
        Log.d(TAG, "refreshPointArrays: Started");
        initPointDataInBackground();

    }

    @Override
    public void requestPointSurveyArray() {
        Log.d(TAG, "requestPointSurveyArray: Listener: Request PointSurveyArray Started");
        loadPointSurveyInBackground();
    }

    @Override
    public void requestPointGeodeticArray() {

    }

    @Override
    public void callPointViewDialogBox(long point_id, int pointNo) {
        openPointEdit(point_id, pointNo);

    }

    @Override
    public void onReturnValuesInverseFrom(PointSurvey pointSurvey) {
        Log.d(TAG, "onReturnValuesInverseFrom: Started");
        jobPointsInverseFragment.setPointSurveyFromPointSurvey(pointSurvey,true);
    }

    @Override
    public void onReturnValuesInverseTo(PointSurvey pointSurvey) {
        Log.d(TAG, "onReturnValuesInverseTo: Started");
        jobPointsInverseFragment.setPointSurveyFromPointSurvey(pointSurvey,false);

    }
}
