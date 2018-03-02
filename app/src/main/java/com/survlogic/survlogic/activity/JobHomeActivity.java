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
import com.survlogic.survlogic.fragment.JobHomeHomeFragment;
import com.survlogic.survlogic.interf.JobHomeActivityListener;
import com.survlogic.survlogic.model.ProjectJobSettings;
import com.survlogic.survlogic.utils.BottomNavigationViewHelper;
import com.survlogic.survlogic.utils.PreferenceLoaderHelper;

/**
 * Created by chrisfillmore on 8/2/2017.
 */

public class JobHomeActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, JobHomeActivityListener {
    private static final String TAG = "JobHomeActivity";

    private Context mContext;
    private Toolbar toolbar;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private NavigationView navigationView;

    private int ACTIVITY_NUM = 0;

    private static final int DELAY_TO_LOAD_SETTINGS = 1000;
    private static final int DELAY_TO_SAVE_SETTINGS = 1000;

    private static final int DELAY_TO_SHOW_DRAWER_LAYOUT = 1500;
    private final int MESSAGE_SHOW_DRAWER_LAYOUT = 0x001;

    private final int MESSAGE_SHOW_START_PAGE = 0x002;

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    private PreferenceLoaderHelper preferenceLoaderHelper;

    private BottomNavigationViewEx bottomNavigationViewEx;

    private ProjectJobSettings jobSettings;
    private static int project_id, job_id, job_settings_id = 1;
    private String jobDatabaseName;

    private RelativeLayout rlLayout2;
    private ProgressBar progressBar;

    private FrameLayout container;

    private JobHomeHomeFragment jobHomeFragment;

    private String mJobName, mProjectionString, mProjectionZoneString;
    private int mJobUnits, mIsProjection, mIsZone;


    public Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case MESSAGE_SHOW_DRAWER_LAYOUT:
                    drawerLayout.openDrawer(GravityCompat.START);
                    break;
            }
        }
    };



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: Starting................>");
        setContentView(R.layout.activity_job_home);
        mContext = JobHomeActivity.this;
        preferenceLoaderHelper = new PreferenceLoaderHelper(mContext);


        initViewToolbar();
        initViewNavigation();
        initViewWidgets();
        initBottomNavigationView();
        initFragmentContainer(savedInstanceState);

        loadPreferencesFromDb();
        checkPreferences();

    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: Starting..............>");

        Log.d(TAG, "onResume: Saving Preferences...");
        savePreferences();

    }

    @Override
    public void onBackPressed() {
        AlertDialog dialog = DialogCloseActivity();
        dialog.show();
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
        navigationView.setNavigationItemSelectedListener(JobHomeActivity.this);

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
        Log.d(TAG, "||Database in Home Activity|| : " + jobDatabaseName);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        rlLayout2 = (RelativeLayout) findViewById(R.id.relLayout_2) ;
        progressBar = (ProgressBar) findViewById(R.id.progressStatus);

    }


    //----------------------------------------------------------------------------------------------//

    private void loadPreferencesFromDb(){

        rlLayout2.setVisibility(View.VISIBLE);
        initJobSettingsFromDb();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                if(jobHomeFragment != null){
                    jobHomeFragment.setMetadata();
                }

                rlLayout2.setVisibility(View.GONE);
            }
        },DELAY_TO_LOAD_SETTINGS);
    }

    private void loadPreferences(){

        rlLayout2.setVisibility(View.VISIBLE);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                if(jobHomeFragment != null){
                    jobHomeFragment.setMetadata();
                }

                rlLayout2.setVisibility(View.GONE);
            }
        },DELAY_TO_LOAD_SETTINGS);
    }



    private void savePreferences(){

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                initJobSettingsToDb();

            }
        }, DELAY_TO_SAVE_SETTINGS);
    }


    /**
     * Take preferences from database and populate current job preferences with the dataset.
     */


    private boolean initJobSettingsFromDb(){
        Log.d(TAG, "initJobSettingsFromObject: Started...");
        boolean results = false;

        Log.d(TAG, "initJobSettingsFromObject: Connecting to db");
        JobDatabaseHandler jobDb = new JobDatabaseHandler(this,jobDatabaseName);
        SQLiteDatabase db = jobDb.getReadableDatabase();


        try {
            Log.d(TAG, "initJobSettingsFromDb: Trying to get Job Settings from DB");
            jobSettings = jobDb.getJobSettingsById(db,job_settings_id);

            Log.d(TAG, "initJobSettingsFromDb: Job Database: " + jobDatabaseName);
            editor = sharedPreferences.edit();

            Log.d(TAG, "initJobSettingsFromDb: Job Settings Loading, Populating General Preferences");
            //General
            editor.putString(getString(R.string.pref_key_current_job_name),jobSettings.getJobName());

            mJobName = jobSettings.getJobName();
            Log.d(TAG, "initJobSettingsFromDb: Job Name: " + jobSettings.getJobName());

            editor.putString(getString(R.string.pref_key_current_job_type), String.valueOf(jobSettings.getDefaultJobType()));
            editor.putString(getString(R.string.pref_key_current_job_over_projection), String.valueOf(jobSettings.getOverrideProjection()));
            editor.putString(getString(R.string.pref_key_current_job_over_projection_string), String.valueOf(jobSettings.getOverrideProjectionString()));
            editor.putString(getString(R.string.pref_key_current_job_over_zone), String.valueOf(jobSettings.getOverrideZone()));
            editor.putString(getString(R.string.pref_key_current_job_over_zone_string), String.valueOf(jobSettings.getOverrideZoneString()));
            editor.putString(getString(R.string.pref_key_current_job_over_units), String.valueOf(jobSettings.getOverrideUnits()));

            mIsProjection = jobSettings.getOverrideProjection();
            mIsZone = jobSettings.getOverrideZone();
            mProjectionString = jobSettings.getOverrideProjectionString();
            mProjectionZoneString = jobSettings.getOverrideZoneString();
            mJobUnits = jobSettings.getOverrideUnits();

            Log.d(TAG, "initJobSettingsFromDb: Loading Notes Preferences");
            //Notes
            editor.putString(getString(R.string.pref_key_current_job_attr_client),jobSettings.getAttClient());
            editor.putString(getString(R.string.pref_key_current_job_attr_mission),jobSettings.getAttMission());
            editor.putString(getString(R.string.pref_key_current_job_attr_weather_general),jobSettings.getAttWeatherGeneral());
            editor.putString(getString(R.string.pref_key_current_job_attr_weather_temp),jobSettings.getAttWeatherTemp());
            editor.putString(getString(R.string.pref_key_current_job_attr_weather_pres),jobSettings.getAttWeatherPress());
            editor.putString(getString(R.string.pref_key_current_job_attr_staff_chief),jobSettings.getAttStaffLeader());
            editor.putString(getString(R.string.pref_key_current_job_attr_staff_iman),jobSettings.getAttStaff_1());
            editor.putString(getString(R.string.pref_key_current_job_attr_staff_rman),jobSettings.getAttStaff_2());
            editor.putString(getString(R.string.pref_key_current_job_attr_staff_other),jobSettings.getAttStaffOther());

            Log.d(TAG, "initJobSettingsFromDb: Loading Display Preferences");
            //Display
            editor.putString(getString(R.string.pref_key_current_job_system_distance_display), String.valueOf(jobSettings.getSystemDistanceDisplay()));
            editor.putString(getString(R.string.pref_key_current_job_system_distance_precision_display), String.valueOf(jobSettings.getSystemDistancePrecisionDisplay()));
            editor.putString(getString(R.string.pref_key_current_job_system_coordinates_precision_display), String.valueOf(jobSettings.getSystemCoordinatesPrecisionDisplay()));
            editor.putString(getString(R.string.pref_key_current_job_system_angle_display), String.valueOf(jobSettings.getSystemAngleDisplay()));

            editor.putString(getString(R.string.pref_key_current_job_format_coord_entry), String.valueOf(jobSettings.getFormatCoordinatesEntry()));
            editor.putString(getString(R.string.pref_key_current_job_format_angle_hz_display), String.valueOf(jobSettings.getFormatAngleHorizontalDisplay()));
            editor.putString(getString(R.string.pref_key_current_job_format_angle_hz_obsrv_entry), String.valueOf(jobSettings.getFormatDistanceHorizontalObsDisplay()));
            editor.putString(getString(R.string.pref_key_current_job_format_angle_vz_obsrv_display), String.valueOf(jobSettings.getFormatAngleVerticalObsDisplay()));
            editor.putString(getString(R.string.pref_key_current_job_format_distance_hz_obsrv_display), String.valueOf(jobSettings.getFormatDistanceHorizontalObsDisplay()));

            Log.d(TAG, "initJobSettingsFromDb: Loading Raw Data Preferences");
            //Raw Data
            if(jobSettings.getOptionsRawFile() == 0){
                editor.putBoolean(getString(R.string.pref_key_current_job_options_raw_file), false);
            }else{
                editor.putBoolean(getString(R.string.pref_key_current_job_options_raw_file), true);
            }

            if( jobSettings.getOptionsRawTimeStamp() == 0){
                editor.putBoolean(getString(R.string.pref_key_current_job_options_raw_time_stamp),false);
            }else{
                editor.putBoolean(getString(R.string.pref_key_current_job_options_raw_time_stamp),true);
            }

            if(jobSettings.getOptionsGpsAttribute() == 0){
                editor.putBoolean(getString(R.string.pref_key_current_job_options_gps_attribute),false);
            }else{
                editor.putBoolean(getString(R.string.pref_key_current_job_options_gps_attribute),true);
            }

            if(jobSettings.getOptionsCodeTable() == 0){
                editor.putBoolean(getString(R.string.pref_key_current_job_options_code_table),false);
            }else{
                editor.putBoolean(getString(R.string.pref_key_current_job_options_code_table),true);
            }

            Log.d(TAG, "initJobSettingsFromDb: Loading Options Preferences");
            //Options

            if(jobSettings.getUiDrawerState() == 0){
                editor.putBoolean(getString(R.string.pref_key_current_job_drawer_open), false);
            }else{
                editor.putBoolean(getString(R.string.pref_key_current_job_drawer_open), true);
            }

            //editor.putInt(getString(R.string.pref_key_current_job_first_start), jobSettings.getUiFirstStart());//

            Log.d(TAG, "initJobSettingsFromDb: Applying Preferences");
            editor.apply();

            results = true;
            Log.d(TAG, "initJobSettingsFromObject: Database Retrieval Complete.  Closing DB Connection");
            db.close();
        }catch (Exception e) {
            e.printStackTrace();
        }


        return results;

        }


        private boolean initJobSettingsToDb(){
            Log.d(TAG, "initJobSettingsToDb: Starting...");
            boolean results = false;

            Log.d(TAG, "initJobSettingsToDb: Saving General Data...");
            //General
            String general_name = sharedPreferences.getString(getString(R.string.pref_key_current_job_name),getString(R.string.general_default));
            int general_type = Integer.parseInt(sharedPreferences.getString(getString(R.string.pref_key_current_job_type),"0"));
            int general_over_projection = Integer.parseInt(sharedPreferences.getString(getString(R.string.pref_key_current_job_over_projection),"0"));
            String general_over_projection_string = sharedPreferences.getString(getString(R.string.pref_key_current_job_over_projection_string),getString(R.string.general_blank));
            int general_over_zone = Integer.parseInt(sharedPreferences.getString(getString(R.string.pref_key_current_job_over_zone),"0"));
            String general_over_zone_string = sharedPreferences.getString(getString(R.string.pref_key_current_job_over_zone_string),getString(R.string.general_blank));
            int general_over_units = Integer.parseInt(sharedPreferences.getString(getString(R.string.pref_key_current_job_over_units),"0"));

            Log.d(TAG, "initJobSettingsToDb: Saving Notes Data...");
            //Notes
            String attr_client = sharedPreferences.getString(getString(R.string.pref_key_current_job_attr_client),getString(R.string.general_blank));
            String attr_mission = sharedPreferences.getString(getString(R.string.pref_key_current_job_attr_mission),getString(R.string.general_blank));
            String attr_weather_general = sharedPreferences.getString(getString(R.string.pref_key_current_job_attr_weather_general),getString(R.string.general_blank));
            String attr_weather_temp = sharedPreferences.getString(getString(R.string.pref_key_current_job_attr_weather_temp),getString(R.string.general_blank));
            String attr_weather_pres = sharedPreferences.getString(getString(R.string.pref_key_current_job_attr_weather_pres),getString(R.string.general_blank));
            String attr_staff_chief = sharedPreferences.getString(getString(R.string.pref_key_current_job_attr_staff_chief),getString(R.string.general_blank));
            String attr_staff_iman = sharedPreferences.getString(getString(R.string.pref_key_current_job_attr_staff_iman),getString(R.string.general_blank));
            String attr_staff_rman = sharedPreferences.getString(getString(R.string.pref_key_current_job_attr_staff_rman),getString(R.string.general_blank));
            String attr_staff_other = sharedPreferences.getString(getString(R.string.pref_key_current_job_attr_staff_other),getString(R.string.general_blank));

            Log.d(TAG, "initJobSettingsToDb: Save Display Data...");
            //Display
            int system_distance_display = Integer.parseInt(sharedPreferences.getString(getString(R.string.pref_key_current_job_system_distance_display),"0"));
            int system_distance_precision_display = Integer.parseInt(sharedPreferences.getString(getString(R.string.pref_key_current_job_system_distance_precision_display),"0"));
            int system_coordinate_precision_display = Integer.parseInt(sharedPreferences.getString(getString(R.string.pref_key_current_job_system_coordinates_precision_display),"0"));
            int system_angle_display = Integer.parseInt(sharedPreferences.getString(getString(R.string.pref_key_current_job_system_angle_display),"0"));

            int format_coord_entry = Integer.parseInt(sharedPreferences.getString(getString(R.string.pref_key_current_job_format_coord_entry),"0"));
            int format_angle_hz_display = Integer.parseInt(sharedPreferences.getString(getString(R.string.pref_key_current_job_format_angle_hz_display),"0"));
            int format_angle_hz_obsrv_entry = Integer.parseInt(sharedPreferences.getString(getString(R.string.pref_key_current_job_format_angle_hz_obsrv_entry),"0"));
            int format_angle_vz_obsrv_display = Integer.parseInt(sharedPreferences.getString(getString(R.string.pref_key_current_job_format_angle_vz_obsrv_display),"0"));
            int format_distance_hz_obsrv_display = Integer.parseInt(sharedPreferences.getString(getString(R.string.pref_key_current_job_format_distance_hz_obsrv_display),"0"));

            Log.d(TAG, "initJobSettingsToDb: Save Raw Data...");
            //Raw Data

            int raw_file = 0;
            if(sharedPreferences.getBoolean(getString(R.string.pref_key_current_job_options_raw_file),true)){
                raw_file = 1;
            }

            int raw_file_timestamp = 0;
            if(sharedPreferences.getBoolean(getString(R.string.pref_key_current_job_options_raw_time_stamp),true)){
                raw_file_timestamp = 1;
            }

            int raw_gps_attribute = 0;
            if(sharedPreferences.getBoolean(getString(R.string.pref_key_current_job_options_gps_attribute),true)){
                raw_gps_attribute = 1;
            }

            int raw_desc_code_list = 0;
            if(sharedPreferences.getBoolean(getString(R.string.pref_key_current_job_options_code_table),true)){
                raw_desc_code_list = 1;
            }

            Log.d(TAG, "initJobSettingsToDb: Save Options...");
            //Options
            int options_drawer_state = 0;
            if(sharedPreferences.getBoolean(getString(R.string.pref_key_current_job_drawer_open),true)){
                options_drawer_state = 1;
            }

            int options_first_start = 0;

            Log.d(TAG, "initJobSettingsToDb: Creating Settings model...");
            ProjectJobSettings settings = new ProjectJobSettings(project_id, job_id, general_name,
                    options_first_start, options_drawer_state,
                    general_type, general_over_projection, general_over_zone, general_over_units,
                    system_distance_display, system_distance_precision_display, system_coordinate_precision_display, system_angle_display,
                    format_angle_hz_display, format_angle_vz_obsrv_display, format_distance_hz_obsrv_display, format_coord_entry, format_angle_hz_obsrv_entry,
                    raw_file, raw_file_timestamp, raw_gps_attribute, raw_desc_code_list,attr_client, attr_mission, attr_weather_general, attr_weather_temp, attr_weather_pres,
                    attr_staff_chief, attr_staff_iman, attr_staff_rman, attr_staff_other);


            settings.setOverrideProjectionString(general_over_projection_string);
            settings.setOverrideZoneString(general_over_zone_string);

            Log.d(TAG, "initJobSettingsToDb: Options_Drawer_State: " + options_drawer_state);
            Log.d(TAG, "initJobSettingsToDb: Starting Database Exchange");
            JobDatabaseHandler jobDb  = new JobDatabaseHandler(mContext,jobDatabaseName);
            SQLiteDatabase dbJob = jobDb.getWritableDatabase();

            int row = 0;
            try{
                Log.d(TAG, "initJobSettingsToDb: Trying to Save Row...");
                row = jobDb.updateJobSettings(dbJob, settings);

            }catch (Exception e){
                e.printStackTrace();
            }

            Log.d(TAG, "initJobSettingsToDb: Closing DB Connection...");
            dbJob.close();


            if (row>0){
                Log.d(TAG, "initJobSettingsToDb: Successful in Updating Settings...");
                return true;
            }else {
                Log.d(TAG, "initJobSettingsToDb: Something Went Wrong...");
                return false;
            }



        }


        private void checkPreferences(){
            Log.d(TAG, "checkPreferences: Started...");
            if (sharedPreferences.getBoolean(getString(R.string.pref_key_current_job_drawer_open),false)){
                Log.d(TAG, "checkPreferences: Drawer State = True");
                mHandler.sendEmptyMessageDelayed(MESSAGE_SHOW_DRAWER_LAYOUT, DELAY_TO_SHOW_DRAWER_LAYOUT);
            }

        }

    //---------------------------------------------------------------------------------------------//

    /**
     *Creates a Dialog Box to Confirm user to leave the Job Activity
     */

    private AlertDialog DialogCloseActivity(){

        AlertDialog myDialogBox = new AlertDialog.Builder(this)
                .setTitle("Exit Job")
                .setMessage("Are you sure you want to leave this Job?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        savePreferences();
                        preferenceLoaderHelper.clearCogoSettings();
                        finish();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })

                .create();

        return myDialogBox;
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


                        jobHomeFragment.setArguments(getIntent().getExtras());

                        swapFragment(jobHomeFragment,false,"HOME");
                        loadPreferences();

                        break;


                }


                return false;
            }
        });
    }


    private void initFragmentContainer(@Nullable Bundle savedInstanceState) {
        Log.d(TAG, "initFragmentContainer: Starting...");
        container = (FrameLayout) findViewById(R.id.container_in_job_home);

        if (container != null) {
            if (savedInstanceState != null) {
                return;
            }

            jobHomeFragment = new JobHomeHomeFragment();
            jobHomeFragment.setArguments(getIntent().getExtras());

            FragmentManager fm = getSupportFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();
            ft.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
            ft.add(R.id.container_in_job_home, jobHomeFragment, "HOME");
            ft.commit();


        }

    }
    private void swapFragment(Fragment fragment, boolean addToStack, String tag){
        Log.d(TAG, "swapFragment: Starting..");

        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
        ft.replace(R.id.container_in_job_home,fragment);

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


    @Override
    public String getJobMetadataJobName() {
        return mJobName;
    }

    @Override
    public int getJobMetadataJobUnits() {
        return mJobUnits;
    }

    @Override
    public int getJobMetadataIsJobProjection() {
        return mIsProjection;
    }

    @Override
    public int getJobMetadataIsJobZone() {
        return mIsZone;
    }

    @Override
    public String getJobMetadataJobProjection() {
        return mProjectionString;
    }

    @Override
    public String getJobMetadataJobZone() {
        return mProjectionZoneString;
    }
}
