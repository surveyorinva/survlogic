package com.survlogic.survlogic.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.Image;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;
import com.sdsmdg.harjot.vectormaster.VectorMasterView;
import com.sdsmdg.harjot.vectormaster.models.PathModel;
import com.survlogic.survlogic.R;
import com.survlogic.survlogic.interf.SketchInterfaceListener;
import com.survlogic.survlogic.utils.BottomNavigationViewHelper;
import com.survlogic.survlogic.utils.ImageHelper;
import com.survlogic.survlogic.view.SketchPointView;

/**
 * Created by chrisfillmore on 8/29/2017.
 */

public class JobPointAddSketchActivity extends AppCompatActivity{
    private static final String TAG = "JobPointAddSketchActivi";
    private static final int REQUEST_GET_POINT_PHOTO = 1;
    private static final int REQUEST_TAKE_PHOTO = 2;
    private static final int REQUEST_SELECT_PICTURE = 3;

    private static Context mContext;
    private Toolbar toolbar;

    private int ACTIVITY_NUM = 3;

    private BottomNavigationViewEx bottomNavigationViewEx;
    private SketchPointView sketchPointView;

    SketchInterfaceListener mListener;

    private int pointNo, projectID, jobId, pointId;
    private String databaseName;

    private RelativeLayout rlToolspace, rlToolspaceSettingsPen, rlToolspaceSettingsCanvas;
    private ImageButton ibSettingsPen, ibSettingsCanvas, ibSettingsMore,
        ibSketchPen, ibSketchEraser,
        ibColorBlue, ibColorGreen, ibColorRed, ibColorBlack,
        ibGetPhoto;

    private SeekBar brushSizeSeekBar;

    private VectorMasterView sketchExample;
    private PathModel outline;

    private int oldContentItem = 0;

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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult: Started...");

        if (this.REQUEST_GET_POINT_PHOTO == requestCode && resultCode == RESULT_OK) {
            Log.d(TAG, "onActivityResult: Starting...");

            String imagePath = data.getStringExtra(getString(R.string.KEY_GALLERY_IMAGE_PATH));

            sketchPointView.setBackgroundImage(imagePath);

        }


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

        Bundle extras = getIntent().getExtras();
        projectID = extras.getInt(getString(R.string.KEY_PROJECT_ID));
        jobId = extras.getInt(getString(R.string.KEY_JOB_ID));
        pointId = extras.getInt(getString(R.string.KEY_POINT_ID));
        pointNo = extras.getInt(getString(R.string.KEY_POINT_NO));
        databaseName = extras.getString(getString(R.string.KEY_JOB_DATABASE));

        Log.d(TAG, "Variables- Project ID: " + projectID);
        Log.d(TAG, "Variables- Job ID: " + jobId);
        Log.d(TAG, "Variables- Point ID: " + pointId);
        Log.d(TAG, "Variables- Point No: " + pointNo);
        Log.d(TAG, "Variables- Database: " + databaseName);

        rlToolspace = (RelativeLayout) findViewById(R.id.toolbar_settings);
        rlToolspaceSettingsPen = (RelativeLayout) findViewById(R.id.content_sketch_settings_pen);
        rlToolspaceSettingsCanvas = (RelativeLayout) findViewById(R.id.content_sketch_settings_canvas);

        ibSettingsPen = (ImageButton) findViewById(R.id.sketch_menu_top_item_1);
        ibSettingsCanvas = (ImageButton) findViewById(R.id.sketch_menu_top_item_2);

        ibSketchPen = (ImageButton) findViewById(R.id.sketch_pen);
        ibSketchEraser = (ImageButton) findViewById(R.id.sketch_eraser);

        ibColorBlue = (ImageButton) findViewById(R.id.sketch_color_blue);
        ibColorGreen = (ImageButton) findViewById(R.id.sketch_color_green);
        ibColorRed = (ImageButton) findViewById(R.id.sketch_color_red);
        ibColorBlack = (ImageButton) findViewById(R.id.sketch_color_black);

        ibGetPhoto = (ImageButton) findViewById(R.id.sketch_canvas_photo);

        sketchPointView = (SketchPointView) findViewById(R.id.sketch_canvas);
        sketchExample = (VectorMasterView) findViewById(R.id.canvas_brush);

        outline = sketchExample.getPathModelByName("outline");


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


                        if(sketchPointView.isTouchable()){
                            sketchPointView.setTouchable(false);
                            menuItem = menu.getItem(ACTIVITY_NUM);
                            menuItem.setChecked(true);

                        }else{
                            sketchPointView.setTouchable(true);
                            menuItem = menu.getItem(3);
                            menuItem.setChecked(true);
                        }

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

        ibSettingsCanvas.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                animToolbarMenu(2);
            }
        });

        ibSketchPen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setSketchPen();
            }
        });

        ibSketchEraser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setSketchEraser();
            }
        });

        ibColorBlue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setBrushColor(1);
            }
        });

        ibColorGreen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setBrushColor(2);
            }
        });

        ibColorRed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setBrushColor(3);
            }
        });

        ibColorBlack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setBrushColor(4);
            }
        });

        ibGetPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callImageSelectionDialog();
            }
        });

    }

    private void animToolbarMenu(int content){
        boolean toolbarShown = rlToolspace.isShown();
        boolean showContent = true;
        boolean contentShown = rlToolspaceSettingsPen.isShown() || rlToolspaceSettingsCanvas.isShown();

        if(toolbarShown){
            if(oldContentItem == content){
                //user clicked the same toolbar item, closing it
                rlToolspace.setVisibility(View.GONE);

                if(!sketchPointView.isTouchable()){
                    sketchPointView.setTouchable(true);
                }

                showContent = false;

            }else{
                //user clicked a different toolbar item, switch to that tab
                showContent = true;

            }
        }else{
            rlToolspace.setVisibility(View.VISIBLE);
            if(sketchPointView.isTouchable()){
                sketchPointView.setTouchable(false);
            }

        }
        if(showContent){

            switch (content){
                case 1: //Pens
                    oldContentItem = 1;
                    rlToolspaceSettingsPen.setVisibility(View.VISIBLE);
                    rlToolspaceSettingsCanvas.setVisibility(View.GONE);

                    //Paint Brush Size
                    setBrushSize();

                    //Sketch Example
                    switchSketchExample();
                    break;

                case 2: //Canvas
                    oldContentItem = 2;

                    rlToolspaceSettingsCanvas.setVisibility(View.VISIBLE);
                    rlToolspaceSettingsPen.setVisibility(View.GONE);
                    break;
            }
        }
    }


    private void switchSketchExample(){
        Log.d(TAG, "switchSketchExample: Started");
        outline.setStrokeWidth(sketchPointView.getCurrentBrushSize());

        outline.setStrokeColor(sketchPointView.getCurrentBrushColor());
        outline.setFillColor(sketchPointView.getCurrentBrushColor());

        outline.updatePaint();

    }

    private void refreshSketchExample(){
        Log.d(TAG, "refreshSketchExample: Started...");

        sketchExample.invalidate();
        switchSketchExample();

    }

    private void setBrushSize(){
        brushSizeSeekBar = (SeekBar) findViewById(R.id.seek_bar_brush_size);
        int currentBrushSize = sketchPointView.getCurrentBrushSize();

        brushSizeSeekBar.setProgress(currentBrushSize);
        Log.d(TAG, "animToolbarMenu: Seek START at: " + currentBrushSize);


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
                Log.d(TAG, "onStopTrackingTouch: Seek STOP at: " + progressChanged);
                sketchPointView.setBrushSize(progressChanged);
                sketchPointView.setLastBrushSize(progressChanged);

                refreshSketchExample();

            }
        });
    }

    private void setBrushColor(int color){

        switch (color){
            case 1: //blue
                sketchPointView.setCurrentBrushColor(Color.BLUE);

                break;

            case 2: //green
                sketchPointView.setCurrentBrushColor(Color.GREEN);
                break;

            case 3: //red
                sketchPointView.setCurrentBrushColor(Color.RED);
                break;

            case 4: //black
                sketchPointView.setCurrentBrushColor(Color.BLACK);
                break;
        }

        refreshSketchExample();
    }


    private void setSketchEraser(){
        sketchPointView.setCurrentBrushColor(Color.WHITE);

    }

    private void setSketchPen(){

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


    private void callImageSelectionDialog(){
        Log.d(TAG, "callImageSelectionDialog: Started...");
        final CharSequence[] items = { getString(R.string.project_new_dialog_takePhoto), getString(R.string.project_new_dialog_getImage),
                getString(R.string.project_new_dialog_getPointImage), getString(R.string.general_cancel) };

        TextView title = new TextView(mContext);  //was context not this

        title.setText(getString(R.string.photo_dialog_title_sketch));
        title.setBackgroundColor(Color.WHITE);
        title.setPadding(10, 15, 15, 10);
        title.setGravity(Gravity.CENTER);
        title.setTextColor(Color.BLACK);
        title.setTextSize(22);

        android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(mContext);

        builder.setCustomTitle(title);

        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int arg) {

                switch (arg) {

                    case 0: //Camera
                        //startCamera();
                        break;

                    case 1: //Photo
                        //startPhotoGallery();
                        break;

                    case 2: //Job Point
                        Intent intent = new Intent(mContext, SketchBackgroundGalleryActivity.class);
                        intent.putExtra(getString(R.string.KEY_PROJECT_ID),projectID);
                        intent.putExtra(getString(R.string.KEY_JOB_ID), jobId);
                        intent.putExtra(getString(R.string.KEY_JOB_DATABASE), databaseName);
                        intent.putExtra(getString(R.string.KEY_POINT_ID), pointId);
                        intent.putExtra(getString(R.string.KEY_POINT_NO), pointNo);

                        startActivityForResult(intent, REQUEST_GET_POINT_PHOTO);
                        break;

                    case 3: //cancel
                        dialog.dismiss();
                        break;
                }

            }
        });
        builder.show();
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
