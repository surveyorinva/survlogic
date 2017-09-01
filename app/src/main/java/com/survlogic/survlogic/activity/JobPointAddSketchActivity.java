package com.survlogic.survlogic.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Toast;

import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;
import com.survlogic.survlogic.R;
import com.survlogic.survlogic.utils.BottomNavigationViewHelper;
import com.survlogic.survlogic.view.SketchPointView;

/**
 * Created by chrisfillmore on 8/29/2017.
 */

public class JobPointAddSketchActivity extends AppCompatActivity{
    private static final String TAG = "JobPointAddSketchActivi";

    private static Context mContext;
    private Toolbar toolbar;

    private int ACTIVITY_NUM = 0;

    private BottomNavigationViewEx bottomNavigationViewEx;
    private SketchPointView sketchPointView;

    private int project_id, job_id, job_settings_id = 1;
    private String jobDatabaseName;

    private RelativeLayout rlToolspace, rlToolspaceSettingsPen;
    private ImageButton ibSettingsPen, ibSettingsCanvas, ibSettingsMore;
    private SeekBar brushSizeSeekBar;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: Started...");
        setContentView(R.layout.activity_sketch_point);
        mContext = JobPointAddSketchActivity.this;

        initViewToolbar();
        initViewWidgets();
        initBottomNavigationView();

        setOnClickListeners();
    }

    //---------------------------------------------------------------------------------------------//

    /**
     * Toolbar and UI Methods
     */

    private void initViewToolbar(){
        toolbar = (Toolbar) findViewById(R.id.toolbar_in_job_sketch_view);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_job_points_sketch_settings,menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.toolbar_item1:
                //Clear Canvas
                showDeleteDialog();

                break;


        }

        return super.onOptionsItemSelected(item);
    }


    private void initViewWidgets(){
        Log.d(TAG, "initViewWidgets: Starting...");

//        Bundle extras = getIntent().getExtras();
//        project_id = extras.getInt("PROJECT_ID");
//        job_id = extras.getInt("JOB_ID");
//        jobDatabaseName = extras.getString("JOB_DB_NAME");
//        Log.d(TAG, "||Database|| : " + jobDatabaseName);

        rlToolspace = (RelativeLayout) findViewById(R.id.toolbar_settings);
        rlToolspaceSettingsPen = (RelativeLayout) findViewById(R.id.content_sketch_settings_pen);

        ibSettingsPen = (ImageButton) findViewById(R.id.sketch_menu_top_item_1);

        sketchPointView = (SketchPointView) findViewById(R.id.sketch_canvas);



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


                        break;

                    case R.id.navigation_item_2:
                        Log.d(TAG, "onNavigationItemSelected: Nav Item 2 - Start");
                        ACTIVITY_NUM = 1;
                        menuItem = menu.getItem(ACTIVITY_NUM);
                        menuItem.setChecked(false);

                        sketchPointView.onClickUndo();

                        break;

                    case R.id.navigation_item_3:
                        ACTIVITY_NUM = 2;
                        menuItem = menu.getItem(ACTIVITY_NUM);
                        menuItem.setChecked(false);

                        sketchPointView.onClickRedo();

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

    private void setOnClickListeners(){
        ibSettingsPen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                animToolbarMenu(1);

            }
        });
    }

    private void animToolbarMenu(int content){

        if(rlToolspace.isShown()){

            rlToolspace.setVisibility(View.GONE);

        }else{
            rlToolspace.setVisibility(View.VISIBLE);
            rlToolspace.bringToFront();
            rlToolspace.invalidate();

            switch (content){
                case 1: //Pens
                    rlToolspaceSettingsPen.setVisibility(View.VISIBLE);
                    brushSizeSeekBar = (SeekBar) findViewById(R.id.seek_bar_brush_size);

                    brushSizeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                        int progressChanged = 0;

                        @Override
                        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                            progressChanged = progress;

                        }

                        @Override
                        public void onStartTrackingTouch(SeekBar seekBar) {

                        }

                        @Override
                        public void onStopTrackingTouch(SeekBar seekBar) {
                            Log.d(TAG, "onStopTrackingTouch: Stoped at: " + progressChanged);
                            sketchPointView.setBrushSize(progressChanged);
                            sketchPointView.setLastBrushSize(progressChanged);
                        }
                    });

            }



        }


    }


    //--------------------------------------------------------------------------------------------------------------------------//
    /**
     * Dialog Helpers
     */

    private void showDeleteDialog(){
        AlertDialog.Builder deleteDialog = new AlertDialog.Builder(this);
        deleteDialog.setTitle(getString(R.string.sketch_dialog_delete_title));
        deleteDialog.setMessage(getString(R.string.sketch_dialog_delete_summary));
        deleteDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener(){
            public void onClick(DialogInterface dialog, int which){
                sketchPointView.eraseAll();
                dialog.dismiss();
            }
        });
        deleteDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        deleteDialog.show();




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
