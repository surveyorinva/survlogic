package com.survlogic.survlogic.activity;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.content.FileProvider;
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
import com.survlogic.survlogic.BuildConfig;
import com.survlogic.survlogic.R;
import com.survlogic.survlogic.background.BackgroundProjectSketchNew;
import com.survlogic.survlogic.model.JobSketch;
import com.survlogic.survlogic.utils.BottomNavigationViewHelper;
import com.survlogic.survlogic.utils.FileHelper;
import com.survlogic.survlogic.view.SketchPointView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by chrisfillmore on 8/29/2017.
 */

public class JobPointAddSketchActivity extends AppCompatActivity{
    private static final String TAG = "JobPointAddSketchActivi";
    private static final int REQUEST_GET_POINT_PHOTO = 1;
    private static final int REQUEST_TAKE_PHOTO = 2;
    private static final int REQUEST_SELECT_PICTURE = 3;
    private int ACTIVITY_NUM = 3;

    private static Context mContext;
    FileHelper fileHelper;
    private Toolbar toolbar;

    private int pointNo, projectID, jobId, pointId;
    private String databaseName;

    private BottomNavigationViewEx bottomNavigationViewEx;
    private SketchPointView sketchPointView;

    private RelativeLayout rlToolspace, rlToolspaceSettingsPen, rlToolspaceSettingsCanvas;
    private ImageButton ibSettingsPen, ibSettingsCanvas, ibSettingsMore,
        ibSketchPen, ibSketchEraser,
        ibColorBlue, ibColorGreen, ibColorRed, ibColorBlack,
        ibGetPhoto, ibGetColor, ibGetGrid, ibGetTriangle, ibGetCircle;

    private SeekBar brushSizeSeekBar, alphaValueSeekBar;

    private VectorMasterView sketchExample;
    private PathModel outline;

    private int oldContentItem = 0;

    private File savedImageFile;
    private Uri savedImageURI;

    private boolean forceViewLock = false;
    private boolean usingBitmapBackground = false;
    private boolean isBitmapBackgroundSaved = false;

    private String mCurrentPhotoPath;
    private Bitmap mBitmap, mBitmapRaw;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: Started...");
        setContentView(R.layout.activity_sketch_point);
        mContext = JobPointAddSketchActivity.this;
        fileHelper =new FileHelper(mContext);

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
            usingBitmapBackground = true;
            isBitmapBackgroundSaved = true;

        }else if (this.REQUEST_TAKE_PHOTO == requestCode && resultCode == Activity.RESULT_OK) {
            Uri imageUri = Uri.parse(mCurrentPhotoPath);
            File file = new File(imageUri.getPath());

            try {
                mBitmapRaw=decodeUri(imageUri,400);
                mBitmap = rotateImageIfRequired(mBitmapRaw,imageUri);

                sketchPointView.setBackgroundImage(mBitmap);
                usingBitmapBackground = true;
                isBitmapBackgroundSaved = false;

            } catch (Exception e) {
                showToast("Caught Error: Could not set Photo to Image from Camera",true);

            }
        } else if (this.REQUEST_SELECT_PICTURE == requestCode && resultCode == Activity.RESULT_OK) {
            if (data != null) {


                try {
                    final Uri imageUri = data.getData();
                    File file = new File(imageUri.getPath());

                    if (imageUri !=null){
                        mBitmapRaw=decodeUri(imageUri,400);
                        mBitmap = rotateImageIfRequired(mBitmapRaw,imageUri);

                        sketchPointView.setBackgroundImage(mBitmap);
                        usingBitmapBackground = true;
                        isBitmapBackgroundSaved = false;

                    }


                } catch (Exception e) {
                    showToast("Caught Error: Could not set Photo to Image from Gallery",true);

                }
            }
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

        ibGetColor = (ImageButton) findViewById(R.id.sketch_canvas_color);
        ibGetGrid  = (ImageButton) findViewById(R.id.sketch_canvas_grid);
        ibGetTriangle = (ImageButton) findViewById(R.id.sketch_canvas_triangle);
        ibGetCircle = (ImageButton) findViewById(R.id.sketch_canvas_circle);
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

                            forceViewLock = true;

                        }else{
                            sketchPointView.setTouchable(true);
                            menuItem = menu.getItem(3);
                            menuItem.setChecked(true);

                            forceViewLock = false;
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

                        showSaveSketchDialog();

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

        ibGetColor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sketchPointView.setBackgroundColor();
                usingBitmapBackground = false;
                sketchPointView.resetBackground();
            }
        });

        ibGetGrid.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sketchPointView.setBackgroundGrid();
                usingBitmapBackground = false;
                sketchPointView.resetBackground();
            }
        });

        ibGetTriangle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sketchPointView.setBackgroundTriangle();
                usingBitmapBackground = false;
                sketchPointView.resetBackground();
            }
        });

        ibGetCircle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sketchPointView.setBackgroundCircle();
                usingBitmapBackground = false;
                sketchPointView.resetBackground();
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

                if(!forceViewLock) {
                    if (!sketchPointView.isTouchable()) {
                        sketchPointView.setTouchable(true);
                    }
                }


                topMenuChangeIcons(false,false);

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

                    //Set icon on menu
                    topMenuChangeIcons(true,false);

                    //default options setup
                    brushMenuBrush(true);

                    break;

                case 2: //Canvas
                    oldContentItem = 2;
                    setAlphaValueBitmap(false);

                    if (usingBitmapBackground) {
                        //Alpha of Bitmap
                        setAlphaValueBitmap(true);
                    }

                    rlToolspaceSettingsCanvas.setVisibility(View.VISIBLE);
                    rlToolspaceSettingsPen.setVisibility(View.GONE);

                    //Set icon on menu
                    topMenuChangeIcons(false,true);

                    break;
            }
        }
    }


    private void brushMenuBrush(boolean active){
        int sizeInDpOpen = 1;
        float scaleOpen = getResources().getDisplayMetrics().density;
        int dpAsPixelsOpen = (int) (sizeInDpOpen*scaleOpen + 0.5f);
        int sizeInDpClosed = 1;
        float scaleClosed = getResources().getDisplayMetrics().density;
        int dpAsPixelsClosed = (int) (sizeInDpClosed*scaleClosed + 0.5f);

        if (active){
            //Brush active
            ibSketchPen.setImageDrawable(getResources().getDrawable(R.drawable.circle_image_sketch_pen, getApplicationContext().getTheme()));


        }else{
            //Brush inactive
            ibSketchPen.setImageDrawable(getResources().getDrawable(R.drawable.ic_general_edit, getApplicationContext().getTheme()));

        }
    }

    private void brushMenuEraser(boolean active){
        int sizeInDpOpen = 1;
        float scaleOpen = getResources().getDisplayMetrics().density;
        int dpAsPixelsOpen = (int) (sizeInDpOpen*scaleOpen + 0.5f);
        int sizeInDpClosed = 1;
        float scaleClosed = getResources().getDisplayMetrics().density;
        int dpAsPixelsClosed = (int) (sizeInDpClosed*scaleClosed + 0.5f);

        if (active){
            //Brush active
            ibSketchEraser.setImageDrawable(getResources().getDrawable(R.drawable.circle_image_sketch_eraser, getApplicationContext().getTheme()));


        }else{
            //Brush inactive
            ibSketchEraser.setImageDrawable(getResources().getDrawable(R.drawable.ic_sketch_eraser, getApplicationContext().getTheme()));

        }
    }

    private void topMenuChangeIcons (boolean brushMenu, boolean canvasMenu){

        int sizeInDpOpen = 1;
        float scaleOpen = getResources().getDisplayMetrics().density;
        int dpAsPixelsOpen = (int) (sizeInDpOpen*scaleOpen + 0.5f);

        int sizeInDpClosed = 5;
        float scaleClosed = getResources().getDisplayMetrics().density;
        int dpAsPixelsClosed = (int) (sizeInDpClosed*scaleClosed + 0.5f);

        if (brushMenu){
            //Brush menu called
            ibSettingsPen.setImageDrawable(getResources().getDrawable(R.drawable.vc_action_brush_box, getApplicationContext().getTheme()));
            ibSettingsPen.setPadding(dpAsPixelsOpen/2, dpAsPixelsOpen/2, dpAsPixelsOpen/2, dpAsPixelsOpen/2);

        }else{
            //Brush menu closed
            ibSettingsPen.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_brush, getApplicationContext().getTheme()));
            ibSettingsPen.setPadding(dpAsPixelsClosed/2, dpAsPixelsClosed/2, dpAsPixelsClosed/2, dpAsPixelsClosed/2);
        }

        if (canvasMenu){
            //Canvas menu called
            ibSettingsCanvas.setImageDrawable(getResources().getDrawable(R.drawable.vc_action_canvas_box, getApplicationContext().getTheme()));
            ibSettingsCanvas.setPadding(dpAsPixelsOpen/2, dpAsPixelsOpen/2, dpAsPixelsOpen/2, dpAsPixelsOpen/2);

        }else{
            //Brush menu closed
            ibSettingsCanvas.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_canvas, getApplicationContext().getTheme()));
            ibSettingsCanvas.setPadding(dpAsPixelsClosed/2, dpAsPixelsClosed/2, dpAsPixelsClosed/2, dpAsPixelsClosed/2);
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
        Log.d(TAG, "setBrushSize: Started");
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

    private void setAlphaValueBitmap(boolean initialize){
        Log.d(TAG, "setAlphaValueBitmap: Started");
        alphaValueSeekBar = (SeekBar) findViewById(R.id.seek_bar_background_alpha);
        int currentAlphaValue = sketchPointView.getCurrentAlphaValue();

        alphaValueSeekBar.setProgress(currentAlphaValue);

        Log.d(TAG, "setAlphaValueBitmap: Seek START at " + currentAlphaValue);


        if(!initialize){
            alphaValueSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
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
                    Log.d(TAG, "onStopTrackingTouch: Seek STOP at " + progressChanged);
                    sketchPointView.setAlphaValue(progressChanged);

                }
            });
        }



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

        brushMenuEraser(true);
        brushMenuBrush(false);
    }

    private void setSketchPen(){
        brushMenuBrush(true);
        brushMenuEraser(false);

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


    private void showSaveSketchDialog(){
        AlertDialog.Builder deleteDialog = new AlertDialog.Builder(this);
        deleteDialog.setTitle(getString(R.string.sketch_dialog_save_title));
        deleteDialog.setMessage(getString(R.string.sketch_dialog_save_summary));
        deleteDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener(){
            public void onClick(DialogInterface dialog, int which){
                boolean pathToSketch = saveSketch();

                if (pathToSketch){
                    String path = fileHelper.uriToString(savedImageURI);
                    saveSketchToDatabase(path);
                }

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
                        startCamera();
                        break;

                    case 1: //Photo
                        startPhotoGallery();
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

    private void startCamera() {
        try {
            dispatchTakePictureIntent();

        } catch (IOException e) {
            showToast("Caught Error: Accessing Camera Exception",true);
        }
    }

    private void startPhotoGallery(){
        Log.d(TAG, "startPhotoGallery: Started...");
        try{
            dispatchPhotoFromGalleryIntent();

        } catch (IOException e){
            showToast("caught Error: Accessing Gallery Exception",true);
        }

    }

    private void dispatchTakePictureIntent() throws IOException {
        Log.d(TAG, "dispatchTakePictureIntent: Started...");
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(this.getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
                showToast("Caught Error: Could not create file",true);
                return;
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        BuildConfig.APPLICATION_ID + ".provider", createImageFile());

                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }

    private void dispatchPhotoFromGalleryIntent() throws IOException{
        Log.d(TAG, "dispatchPhotoFromGalleryIntent: Started...");
        Intent intent = new Intent(Intent.ACTION_PICK,android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Select Picture"),REQUEST_SELECT_PICTURE);

    }

    private File createImageFile() throws IOException {
        Log.d(TAG, "createImageFile: Started...");
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = this.getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = "file:" + image.getAbsolutePath();
        return image;
    }

    protected Bitmap decodeUri(Uri selectedImage, int REQUIRED_SIZE) {

        try {

            // Decode image size
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(mContext.getContentResolver().openInputStream(selectedImage), null, o);

            // The new size we want to scale to
            // final int REQUIRED_SIZE =  size;

            // Find the correct scale value. It should be the power of 2.
            int width_tmp = o.outWidth, height_tmp = o.outHeight;
            int scale = 1;
            while (true) {
                if (width_tmp / 2 < REQUIRED_SIZE
                        || height_tmp / 2 < REQUIRED_SIZE) {
                    break;
                }
                width_tmp /= 2;
                height_tmp /= 2;
                scale *= 2;
            }

            // Decode with inSampleSize
            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize = scale;
            return BitmapFactory.decodeStream(this.getContentResolver().openInputStream(selectedImage), null, o2);
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    private Bitmap rotateImageIfRequired(Bitmap img, Uri selectedImage) throws IOException {

        InputStream input = mContext.getContentResolver().openInputStream(selectedImage);
        ExifInterface ei;
        if (Build.VERSION.SDK_INT > 23)
            ei = new ExifInterface(input);
        else
            ei = new ExifInterface(selectedImage.getPath());

        int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                return rotateImage(img, 90);
            case ExifInterface.ORIENTATION_ROTATE_180:
                return rotateImage(img, 180);
            case ExifInterface.ORIENTATION_ROTATE_270:
                return rotateImage(img, 270);
            default:
                return img;
        }
    }

    private static Bitmap rotateImage(Bitmap img, int degree) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        Bitmap rotatedImg = Bitmap.createBitmap(img, 0, 0, img.getWidth(), img.getHeight(), matrix, true);
        img.recycle();
        return rotatedImg;
    }



    //---------------------------------------------------------------------------------------------------------------------------//

    private boolean saveSketch(){
        Log.d(TAG, "saveSketch: Started");
        boolean results = false;

//        String path = Environment.getExternalStorageDirectory().toString();
//        path = path  +"/"+ getString(R.string.app_name) + "/sketches";

        String path = fileHelper.getPathToFolder(1);

        Log.d(TAG, "Path: " + path);

        File dir = new File(path);
        //save drawing
        sketchPointView.setDrawingCacheEnabled(true);

        //attempt to save
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imTitle = "SKETCH_" + timeStamp + ".png";

        String imgSaved = MediaStore.Images.Media.insertImage(
                getContentResolver(), sketchPointView.getDrawingCache(),
                imTitle, "Sketch");

        Log.d(TAG, "Image Path: " + imgSaved);

        try {
            if (!dir.isDirectory()|| !dir.exists()) {
                dir.mkdirs();
            }
            sketchPointView.setDrawingCacheEnabled(true);

            savedImageFile = new File(dir, imTitle);
            savedImageURI = Uri.parse(savedImageFile.getAbsolutePath());

            FileOutputStream fOut = new FileOutputStream(savedImageFile);
            Bitmap bm =  sketchPointView.getDrawingCache();
            bm.compress(Bitmap.CompressFormat.PNG, 100, fOut);
            results = true;

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            AlertDialog.Builder alert = new AlertDialog.Builder(this);
            alert.setTitle("Uh Oh!");
            alert.setMessage("Oops! Image could not be saved. Do you have enough space in your device?1");
            alert.setPositiveButton("OK", null);
            alert.show();


        } catch (Exception e) {
            Toast unsavedToast = Toast.makeText(getApplicationContext(),
                    "Oops! Image could not be saved. Do you have enough space in your device2?", Toast.LENGTH_SHORT);
            unsavedToast.show();
            e.printStackTrace();

        }

        if(imgSaved!=null){
            Toast savedToast = Toast.makeText(getApplicationContext(),
                    "Sketch saved", Toast.LENGTH_SHORT);
            savedToast.show();
        }

        sketchPointView.destroyDrawingCache();

        return results;
    }

    private void saveSketchToDatabase(String path){
        Log.d(TAG, "saveSketchToDatabase: Started...");
        Log.d(TAG, "saveSketchToDatabase: " + path);


        JobSketch jobSketch = new JobSketch(pointId, path);


        // Setup Background Task
        BackgroundProjectSketchNew backgroundProjectSketchNew = new BackgroundProjectSketchNew(mContext, databaseName);

        // Execute background task
        backgroundProjectSketchNew.execute(jobSketch);
        Log.d(TAG, "submitForm: Complete.");



    }



}
