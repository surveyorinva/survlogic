package com.survlogic.survlogic.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
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
import com.survlogic.survlogic.database.JobDatabaseHandler;
import com.survlogic.survlogic.dialog.DialogJobCogoSetup;
import com.survlogic.survlogic.fragment.JobCogoHomeFragment;
import com.survlogic.survlogic.fragment.JobHomeHomeFragment;
import com.survlogic.survlogic.model.ProjectJobSettings;
import com.survlogic.survlogic.utils.BottomNavigationViewHelper;

/**
 * Created by chrisfillmore on 8/2/2017.
 */

public class JobCogoActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private static final String TAG = "JobHomeActivity";

    private Context mContext;
    private Toolbar toolbar;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private NavigationView navigationView;

    private int ACTIVITY_NUM = 0;

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    private BottomNavigationViewEx bottomNavigationViewEx;

    private static int project_id, job_id;
    private String jobDatabaseName;

    private RelativeLayout rlLayout2;
    private ProgressBar progressBar;

    private FrameLayout container;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: Starting................>");
        setContentView(R.layout.activity_job_cogo);
        mContext = JobCogoActivity.this;

        initViewToolbar();
        initViewNavigation();
        initViewWidgets();
        initBottomNavigationView();
        initFragmentContainer(savedInstanceState);


    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: Starting..............>");

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
        project_id = extras.getInt("PROJECT_ID");
        job_id = extras.getInt("JOB_ID");
        jobDatabaseName = extras.getString("JOB_DB_NAME");
        Log.d(TAG, "||Database|| : " + jobDatabaseName);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        rlLayout2 = (RelativeLayout) findViewById(R.id.relLayout_2) ;
        progressBar = (ProgressBar) findViewById(R.id.progressStatus);

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
                        containerFragment1.setArguments(getIntent().getExtras());

                        swapFragment(containerFragment1,false,"HOME");

                        break;

                    case R.id.navigation_item_2:
                        Log.d(TAG, "onNavigationItemSelected: Nav Item 2 - Start");
                        ACTIVITY_NUM = 1;
                        menuItem = menu.getItem(ACTIVITY_NUM);
                        menuItem.setChecked(false);


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
        container = (FrameLayout) findViewById(R.id.container_in_job_cogo);

        if (container != null) {
            if (savedInstanceState != null) {
                return;
            }

            JobCogoHomeFragment containerFragment = new JobCogoHomeFragment();
            containerFragment.setArguments(getIntent().getExtras());

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


}
