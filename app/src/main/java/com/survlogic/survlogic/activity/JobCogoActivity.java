package com.survlogic.survlogic.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
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
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;
import com.survlogic.survlogic.R;
import com.survlogic.survlogic.background.BackgroundGeodeticPointGet;
import com.survlogic.survlogic.background.BackgroundSurveyPointGet;
import com.survlogic.survlogic.fragment.JobCogoHomeFragment;
import com.survlogic.survlogic.fragment.JobCogoSideshotFragment;
import com.survlogic.survlogic.interf.JobCogoFragmentListener;
import com.survlogic.survlogic.interf.JobCogoHomeFragmentListener;
import com.survlogic.survlogic.interf.JobPointsMapListener;
import com.survlogic.survlogic.model.JobInformation;
import com.survlogic.survlogic.model.PointGeodetic;
import com.survlogic.survlogic.model.PointSurvey;
import com.survlogic.survlogic.utils.BottomNavigationViewHelper;
import com.survlogic.survlogic.utils.MathHelper;
import com.survlogic.survlogic.utils.PreferenceLoaderHelper;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by chrisfillmore on 8/2/2017.
 */

public class JobCogoActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, JobPointsMapListener, JobCogoHomeFragmentListener, JobCogoFragmentListener {
    private static final String TAG = "JobHomeActivity";

    private Context mContext;
    private Toolbar toolbar;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private NavigationView navigationView;

    private int ACTIVITY_NUM = 0;
    private static final int REQUEST_GET_SETUP = 1;

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    private BottomNavigationViewEx bottomNavigationViewEx;

    private static int project_id, job_id;
    private String jobDatabaseName;

    private PointSurvey occupyPointSurvey, backsightPointSurvey;
    double occupyPointHeight, backsightPointHeight;
    int occupyPointNo, backsightPointNo;

    private ArrayList<PointSurvey> lstPointSurvey = new ArrayList<>();
    private ArrayList<PointGeodetic> lstPointGeodetic = new ArrayList<>();
    private HashMap<String,PointSurvey> pointMap = new HashMap<>();

    private RelativeLayout rlLayout2;
    private ProgressBar progressBar;

    private FrameLayout container;

    private TextView tvPointOccupy, tvPointBacksight, tvPointDirection, tvOccupyHeight, tvBacksightHeight;

    private PreferenceLoaderHelper preferenceLoaderHelper;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: Starting................>");
        setContentView(R.layout.activity_job_cogo);
        mContext = JobCogoActivity.this;
        preferenceLoaderHelper = new PreferenceLoaderHelper(mContext);


        initViewToolbar();
        initViewNavigation();
        initViewWidgets();
        initBottomNavigationView();

        initPointDataInBackground();

        initFragmentContainer(savedInstanceState);



    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: Starting..............>");

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult: Started...");


        if (this.REQUEST_GET_SETUP == requestCode && resultCode == RESULT_OK){
            Log.d(TAG, "onActivityResult-Setup: Started...");

            occupyPointNo = data.getIntExtra(getString(R.string.KEY_SETUP_OCCUPY_PT), 0);
            backsightPointNo = data.getIntExtra(getString(R.string.KEY_SETUP_BACKSIGHT_PT), 0);

            occupyPointHeight = data.getDoubleExtra(getString(R.string.KEY_SETUP_OCCUPY_HT), 0);
            backsightPointHeight = data.getDoubleExtra(getString(R.string.KEY_SETUP_BACKSIGHT_HT), 0);

            Log.d(TAG, "onActivityResult: Occupy at: " + occupyPointNo);
            Log.d(TAG, "onActivityResult: Backsight at: " + backsightPointNo);
            Log.d(TAG, "onActivityResult: Occupy Height: " + occupyPointHeight);
            Log.d(TAG, "onActivityResult: Backsight Height " + backsightPointHeight);

            loadSetup(occupyPointNo,backsightPointNo, occupyPointHeight, backsightPointHeight);

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
        navigationView.setNavigationItemSelectedListener(JobCogoActivity.this);

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

        Log.d(TAG, "initView: Started");

        Bundle extras = getIntent().getExtras();
        project_id = extras.getInt(getString(R.string.KEY_PROJECT_ID));
        job_id = extras.getInt(getString(R.string.KEY_JOB_ID));
        jobDatabaseName = extras.getString(getString(R.string.KEY_JOB_DATABASE));
        Log.d(TAG, "||Database in Cogo Activity|| : " + jobDatabaseName);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        rlLayout2 = (RelativeLayout) findViewById(R.id.relLayout_2) ;
        progressBar = (ProgressBar) findViewById(R.id.progressStatus);

        tvPointOccupy = (TextView) findViewById(R.id.tv_value_occupy);
        tvPointBacksight = (TextView) findViewById(R.id.tv_value_backsight);
        tvPointDirection = (TextView) findViewById(R.id.tv_value_direction);
        tvOccupyHeight = (TextView) findViewById(R.id.tv_value_Occupy_Height);
        tvBacksightHeight = (TextView) findViewById(R.id.tv_value_Backsight_Height);

    }




//---------------------------------------------------------------------------------------------//
    /**
     * Bottom Navigation View
     */
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

                        JobCogoHomeFragment containerFragment1 = new JobCogoHomeFragment();

                        containerFragment1.setArguments(getExtrasFromVariables());

                        swapFragment(containerFragment1,false,"HOME");

                        break;

                    case R.id.navigation_item_2:
                        Log.d(TAG, "onNavigationItemSelected: Nav Item 2 - Start");
                        ACTIVITY_NUM = 1;
                        menuItem = menu.getItem(ACTIVITY_NUM);
                        menuItem.setChecked(false);

                        openCogoSetupActivity();

                        break;

                    case R.id.navigation_item_3:
                        ACTIVITY_NUM = 2;
                        menuItem = menu.getItem(ACTIVITY_NUM);
                        menuItem.setChecked(false);


                        JobCogoSideshotFragment containerFragment2 = new JobCogoSideshotFragment();

                        containerFragment2.setArguments(getExtrasFromVariables());

                        swapFragment(containerFragment2,false,"SIDE_SHOT");


                        break;

                    case R.id.navigation_item_4:
                        ACTIVITY_NUM = 3;
                        menuItem = menu.getItem(ACTIVITY_NUM);
                        menuItem.setChecked(false);



                        break;
                }


                return false;
            }
        });
    }


    private void initFragmentContainer(@Nullable Bundle savedInstanceState) {
        Log.d(TAG, "initFragmentContainer: Starting...");
        container = (FrameLayout) findViewById(R.id.container_in_job_cogo);

        if (container != null) {
            if (savedInstanceState != null) {
                return;
            }

            JobCogoHomeFragment containerFragment = new JobCogoHomeFragment();
            containerFragment.setArguments(getExtrasFromVariables());

            FragmentManager fm = getSupportFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();
            ft.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
            ft.add(R.id.container_in_job_cogo, containerFragment, "HOME_COGO");
            ft.commit();


        }

    }
    private void swapFragment(Fragment fragment, boolean addToStack, String tag){
        Log.d(TAG, "swapFragment: Starting..");

        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
        ft.replace(R.id.container_in_job_cogo,fragment);

        if(addToStack) {
            ft.addToBackStack(tag);
        }

        ft.commit();

    }

    //-------------------------------------------------------------------------------------------------------------------------//

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



    private Bundle getExtrasFromVariables(){
        Log.d(TAG, "getExtrasFromVariables: Started...");
        Bundle extras = new Bundle();
        extras.putInt(getString(R.string.KEY_PROJECT_ID),project_id);
        extras.putInt(getString(R.string.KEY_JOB_ID),job_id);
        extras.putString(getString(R.string.KEY_JOB_DATABASE),jobDatabaseName);

        return extras;
    }


    //-------------------------------------------------------------------------------------------------------------------------//

    /**
     * Methods
     */

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

    private void loadSetup(int occupyPoint, int backsightPoint, double occupyHeight, double backsightHeight){
        Log.d(TAG, "loadSetup: Started");
        boolean isPointListValid = true;

        for(int i=0; i<lstPointSurvey.size(); i++) {
            PointSurvey pointSurvey = lstPointSurvey.get(i);

            String pointListPointNo = Integer.toString(pointSurvey.getPoint_no());

            pointMap.put(pointListPointNo, pointSurvey);

        }

        //find occupy PointSurvey

        if(pointMap.containsKey(String.valueOf(occupyPoint))) {
            occupyPointSurvey = pointMap.get(String.valueOf(occupyPoint));
            Log.i(TAG, "Occupy Point Loaded... ");
        }else{
            Log.d(TAG, "loadSetup: Occupy Point Does not exists");
            isPointListValid = false;


        }

        //find backsight PointSurvey
        if(pointMap.containsKey(String.valueOf(backsightPoint))) {
            backsightPointSurvey = pointMap.get(String.valueOf(backsightPoint));

            Log.i(TAG, "Backsight Point Loaded... ");
        }else{
            Log.d(TAG, "loadSetup: Backsight Point Does not exists");
            isPointListValid = false;

        }

        //Determine azimuth
        if(isPointListValid){
            Log.d(TAG, "loadSetup: Point Valid");
            double inverseAzimuth = MathHelper.inverseAzimuthFromPointSurvey(occupyPointSurvey,backsightPointSurvey);
            double inverseBearing = MathHelper.inverseBearingFromPointSurvey(occupyPointSurvey,backsightPointSurvey);

            Log.d(TAG, "loadSetup: Azimuth: " + inverseAzimuth);
            Log.d(TAG, "loadSetup: Bearing: " + inverseBearing);

            tvPointOccupy.setText(String.valueOf(occupyPointSurvey.getPoint_no()));
            tvPointBacksight.setText(String.valueOf(backsightPointSurvey.getPoint_no()));

            //Direction
            //Azimuth

            //Bearing
            tvPointDirection.setText(MathHelper.convertDECtoDMSBearing(inverseBearing,0));

            tvOccupyHeight.setText(String.valueOf(occupyHeight));
            tvBacksightHeight.setText(String.valueOf(backsightHeight));

            //Load setup into Preferences for future use
            preferenceLoaderHelper.setCogoOccupyPoint(occupyPointSurvey.getPoint_no());
            preferenceLoaderHelper.setCogoBacksightPoint(backsightPointSurvey.getPoint_no());

            preferenceLoaderHelper.setCogoOccupyHeight(occupyHeight);
            preferenceLoaderHelper.setCogoBacksightHeight(backsightHeight);
        }else{
            //point list is not valid.  call
            Log.d(TAG, "loadSetup: Point Not Valid");
            updatePointList();
        }

    }


    private void initSetupFromPreferences(){
        Log.d(TAG, "initSetupFromPreferences: Started...");
        int occupyPoint = preferenceLoaderHelper.getCogoOccupyPoint();
        int backsightPoint = preferenceLoaderHelper.getCogoBacksightPoint();

        double occupyPointHeight = preferenceLoaderHelper.getCogoOccupyHeight();
        double backsightPointHeight = preferenceLoaderHelper.getCogoBacksightHeight();

        Log.d(TAG, "initSetupFromPreferences: Occupy Point: " + occupyPoint);

        if (occupyPoint !=0){
            loadSetup(occupyPoint,backsightPoint,occupyPointHeight,backsightPointHeight);

        }

    }

    public void openCogoSetupActivity(){

        boolean isOccupyPointSet = isOccupyPointSurveySet();
        boolean isBackSightPointSet = isBacksightPointSurveySet();


        Intent i = new Intent(mContext, JobCogoSetupActivity.class);
        i.putExtra(getString(R.string.KEY_PROJECT_ID),project_id);
        i.putExtra(getString(R.string.KEY_JOB_ID), job_id);
        i.putExtra(getString(R.string.KEY_JOB_DATABASE), jobDatabaseName);

        if(isOccupyPointSet && isBackSightPointSet){

            PointSurvey occupyPointSurvey = getOccupyPointSurvey();
            PointSurvey backsightPointSurvey = getBacksightPointSurvey();

            i.putExtra(getString(R.string.KEY_SETUP_OCCUPY_PT),occupyPointSurvey.getPoint_no());
            i.putExtra(getString(R.string.KEY_SETUP_BACKSIGHT_PT),backsightPointSurvey.getPoint_no());

            i.putExtra(getString(R.string.KEY_SETUP_OCCUPY_HT), getOccupyHeight());
            i.putExtra(getString(R.string.KEY_SETUP_BACKSIGHT_HT), getBacksightHeight());


        }else{
            i.putExtra(getString(R.string.KEY_SETUP_OCCUPY_PT),0);
            i.putExtra(getString(R.string.KEY_SETUP_BACKSIGHT_PT),0);

            i.putExtra(getString(R.string.KEY_SETUP_OCCUPY_HT), 0);
            i.putExtra(getString(R.string.KEY_SETUP_BACKSIGHT_HT), 0);
        }

        startActivityForResult(i,REQUEST_GET_SETUP);
        Log.d(TAG, "onClick: Request_GET_SETUP: " + REQUEST_GET_SETUP);
        //overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    //------------------------------------------------------------------------------------------------------------------------//
    private void updatePointList(){
        Log.d(TAG, "updatePointList: Started...");
        loadPointSurveyInBackground();


    }

    //------------------------------------------------------------------------------------------------------------------------//


    /**
     * Listeners
     */
    

    @Override
    public void getPointsGeodetic(ArrayList<PointGeodetic> lstPointGeodetics) {
        this.lstPointGeodetic = lstPointGeodetics;

    }

    @Override
    public void getPointsSurvey(ArrayList<PointSurvey> lstPointSurvey) {
        this.lstPointSurvey = lstPointSurvey;

        initSetupFromPreferences();
    }

    @Override
    public void isMapSelectorActive(boolean isSelected) {

    }

    @Override
    public void isMapSelectorOpen(boolean isSelected) {

    }

    //JobCogoHomeFragmentListener
    @Override
    public PointSurvey getOccupyPointSurvey() {
        return occupyPointSurvey;
    }

    @Override
    public PointSurvey getBacksightPointSurvey() {
        return backsightPointSurvey;
    }

    @Override
    public boolean isOccupyPointSurveySet() {

        if(occupyPointSurvey !=null){
            return true;
        }else {
            return false;
        }
    }

    @Override
    public boolean isBacksightPointSurveySet() {
        if(backsightPointSurvey !=null){
            return true;
        }else {
            return false;
        }
    }

    @Override
    public double getOccupyHeight() {
        return occupyPointHeight;
    }

    @Override
    public double getBacksightHeight() {
        return backsightPointHeight;
    }


    @Override
    public ArrayList<PointSurvey> sendPointSurveyToFragment() {
        return lstPointSurvey;
    }

    @Override
    public JobInformation sendJobInformationToFragment() {
        return null;
    }

    @Override
    public PointSurvey sendOccupyPointSurveyToFragment() {
        return occupyPointSurvey;
    }

    @Override
    public PointSurvey sendBacksightPointSurveyToFragment() {
        return backsightPointSurvey;
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
        return occupyPointNo;
    }

    @Override
    public int sendBacksightPointNoToFragment() {
        return backsightPointNo;
    }

    @Override
    public double sendOccupyHeightToFragment() {
        return 0;
    }

    @Override
    public double sendBacksightHeightToFragment() {
        return 0;
    }

    @Override
    public void setOccupyPointSurveyFromFragment(PointSurvey pointSurvey) {

    }

    @Override
    public void setBacksightPointSurveyFromFragment(PointSurvey pointSurvey) {

    }

    @Override
    public void setOccupyHeightFromFragment(double measureUp) {

    }

    @Override
    public void setBacksightHeightFromFragment(double measureUp) {

    }

    @Override
    public void sendSetupToMainActivity() {

    }

    @Override
    public void sendTraverseSetupToMainActivity(int occupyPointNo, int backsightPointNo, double occupyHI, double backsightHI) {
        Log.d(TAG, "sendTraverseSetupToMainActivity: Started...");

        this.occupyPointNo = occupyPointNo;
        this.backsightPointNo = backsightPointNo;
        this.occupyPointHeight = occupyHI;
        this.backsightPointHeight = backsightHI;

        Log.d(TAG, "sendTraverseSetupToMainActivity: Occupy at: " + occupyPointNo);
        Log.d(TAG, "sendTraverseSetupToMainActivity: Backsight at: " + backsightPointNo);
        Log.d(TAG, "sendTraverseSetupToMainActivity: Occupy Height: " + occupyPointHeight);
        Log.d(TAG, "sendTraverseSetupToMainActivity: Backsight Height " + backsightPointHeight);

        loadSetup(occupyPointNo,backsightPointNo, occupyHI, backsightHI);
    }

    @Override
    public void invalidatePointSurveyList() {
        Log.d(TAG, "invalidatePointSurveyList: Started...");
        updatePointList();

    }
}
