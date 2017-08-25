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
import com.survlogic.survlogic.fragment.JobPointsHomeFragment;
import com.survlogic.survlogic.fragment.JobPointsListFragment;
import com.survlogic.survlogic.model.ProjectJobSettings;
import com.survlogic.survlogic.utils.BottomNavigationViewHelper;

/**
 * Created by chrisfillmore on 8/8/2017.
 */

public class JobPointsActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{
    private static final String TAG = "JobPointsActivity";

    private static Context mContext;
    private Toolbar toolbar;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private NavigationView navigationView;

    private int ACTIVITY_NUM = 0;

    private BottomNavigationViewEx bottomNavigationViewEx;

    private ProjectJobSettings jobSettings;
    private int project_id, job_id, job_settings_id = 1;
    private String jobDatabaseName;

    private FrameLayout container;
    private RelativeLayout rlLayout2;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: Starting......>");

        setContentView(R.layout.activity_job_points);
        mContext = JobPointsActivity.this;

        initViewToolbar();
        initViewNavigation();
        initViewWidgets();
        initBottomNavigationView();
        initFragmentContainer(savedInstanceState);


    }

    @Override
    protected void onResume() {
        super.onResume();
        rlLayout2.setVisibility(View.GONE);
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

                break;

            case R.id.menu_item2_id:
                //Action Here

                break;

            case R.id.menu_item3_id:
                //Action here
                break;

            case R.id.menu_item4_id:
                //Action here
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
        project_id = extras.getInt("PROJECT_ID");
        job_id = extras.getInt("JOB_ID");
        jobDatabaseName = extras.getString("JOB_DB_NAME");
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

                        swapFragment(containerFragment1,true,"HOME");

                        break;

                    case R.id.navigation_item_2:
                        Log.d(TAG, "onNavigationItemSelected: Nav Item 2 - Start");
                        ACTIVITY_NUM = 1;
                        menuItem = menu.getItem(ACTIVITY_NUM);
                        menuItem.setChecked(false);

                        JobPointsListFragment containerFragment2 = new JobPointsListFragment();
                        containerFragment2.setArguments(getIntent().getExtras());

                        swapFragment(containerFragment2,false,"LIST");

                        break;

                    case R.id.navigation_item_3:
                        ACTIVITY_NUM = 2;
                        menuItem = menu.getItem(ACTIVITY_NUM);
                        menuItem.setChecked(false);

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



    //----------------------------------------------------------------------------------------------//

}
