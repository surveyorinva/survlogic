package com.survlogic.survlogic.activity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.survlogic.survlogic.R;
import com.survlogic.survlogic.adapter.ActivityViewPagerAdapter;
import com.survlogic.survlogic.background.BackgroundProjectList;
import com.survlogic.survlogic.fragment.MainHomeFragment;
import com.survlogic.survlogic.fragment.MainToolsFragment;
import com.survlogic.survlogic.utils.UniversalImageLoader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, View.OnClickListener {

    private static final String TAG = "MainActivity";

    private Context mContext;
    private Toolbar toolbar;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private NavigationView navigationView;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private ActivityViewPagerAdapter viewPagerAdapter;
    public FloatingActionButton fab;
    private long previousTime;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = MainActivity.this;
        Log.d(TAG, "onCreate: Started---------------------------->");

        initView();
        initViewPager();
        initViewNavigation();
        initImageLoader();
        //initPermissions();

    }

    private void initView(){
        toolbar = (Toolbar) findViewById(R.id.toolbar_in_app_bar_layout);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);

        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_in_main);
        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();

        fab = (FloatingActionButton) findViewById(R.id.fab_in_app_bar_layout);
        fab.setOnClickListener(this);

    }


    private void initViewPager(){
        tabLayout = (TabLayout) findViewById(R.id.tab_in_app_bar_layout);
        viewPager = (ViewPager) findViewById(R.id.viewpager_in_app_bar_layout);

        viewPagerAdapter = new ActivityViewPagerAdapter(getSupportFragmentManager());
        viewPagerAdapter.addFragments(new MainHomeFragment(),"Home");
        viewPagerAdapter.addFragments(new MainToolsFragment(),"Tools");

        viewPager.setAdapter(viewPagerAdapter);
        tabLayout.setupWithViewPager(viewPager);

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (position == 0) {
                    fab.show();
                } else {
                    fab.hide();
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });


    }

    private void initViewNavigation(){
        navigationView = (NavigationView) findViewById(R.id.nav_in_main);
        navigationView.setNavigationItemSelectedListener(this);

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
                intent.setClass(this, GpsSurveyActivity.class);
                startActivity(intent);
                break;

            case R.id.menu_item4_id:
                intent.setClass(this,ProjectDetailsActivity.class);
                startActivity(intent);
                break;

            case R.id.menu_item5_id:

        }

        drawerLayout.closeDrawers();
        return false;
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);

        } else if (2000 + previousTime > (previousTime = System.currentTimeMillis())) {
            super.onBackPressed();

        } else {
            Toast.makeText(getBaseContext(), getString(R.string.general_exit_app), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_main_menu,menu);

        return true;
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        actionBarDrawerToggle.syncState();

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.toolbar_main_item1:
                goToSettingsMenu();
                break;

        }

        return super.onOptionsItemSelected(item);
    }

    private void goToSettingsMenu() {
        Intent i = new Intent(this,SettingsActivity.class);
        startActivity(i);

    }

    private void goToNewProjectForm(){
        Intent i = new Intent(this, ProjectNewActivity.class);
        startActivity(i);
    }

    public FloatingActionButton getFloatingActionButton() {
        return fab;
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.fab_in_app_bar_layout:
                goToNewProjectForm();
                break;


        }
    }

    private void initImageLoader(){
        Log.d(TAG, "initImageLoader: setting configuration");

        UniversalImageLoader universalImageLoader = new UniversalImageLoader(mContext);
        ImageLoader.getInstance().init(universalImageLoader.getConfig());
    }

}
