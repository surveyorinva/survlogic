package com.survlogic.survlogic.activity;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

import com.survlogic.survlogic.R;
import com.survlogic.survlogic.adapter.GalleryImageAdapter;
import com.survlogic.survlogic.database.ProjectDatabaseHandler;
import com.survlogic.survlogic.dialog.DialogProjectPhotoView;
import com.survlogic.survlogic.model.ProjectImages;
import com.survlogic.survlogic.PhotoEditor.utils.ImageHelper;

import java.util.ArrayList;

/**
 * Created by chrisfillmore on 7/21/2017.
 */

public class SketchBackgroundGalleryActivity extends AppCompatActivity {

    private static final String TAG = "SketchBackgroundGallery";
    private AppBarLayout appBarLayout;
    private Toolbar toolbar;
    private Context mContext;

    private int pointNo, projectID, jobId, pointId;
    private String databaseName;

    private GridView gridView;
    private GalleryImageAdapter gridAdapter;

    private ImageHelper imageHelper;
    private String mURLSyntex = "file://";


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: Started----------------->");
        setContentView(R.layout.activity_photo_gallery);

        mContext = SketchBackgroundGalleryActivity.this;
        imageHelper = new ImageHelper(mContext);

        initView();
        initViewNavigation();
        setOnClickListeners();


    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: Started");
        initGridView();
    }

    private void initView(){
        Log.d(TAG, "initView: Started");

        Bundle extras = getIntent().getExtras();

        if (extras ==null){
            Log.d(TAG, "initView: Null Extras Bundle");
        }
        projectID = extras.getInt("KEY_PROJECT_ID");
        jobId = extras.getInt(getString(R.string.KEY_JOB_ID));
        pointId = extras.getInt(getString(R.string.KEY_POINT_ID));
        pointNo = extras.getInt(getString(R.string.KEY_POINT_NO));
        databaseName = extras.getString(getString(R.string.KEY_JOB_DATABASE));

        Log.d(TAG, "Variables- Project ID: " + projectID);
        Log.d(TAG, "Variables- Job ID: " + jobId);
        Log.d(TAG, "Variables- Point ID: " + pointId);
        Log.d(TAG, "Variables- Point No: " + pointNo);
        Log.d(TAG, "Variables- Database: " + databaseName);

        gridView = (GridView) findViewById(R.id.photo_grid_view);

    }

    private void initViewNavigation(){
        Log.d(TAG, "initViewNavigation: Started");
        appBarLayout = (AppBarLayout) findViewById(R.id.appbar_in_activity_project_details);

        toolbar = (Toolbar) findViewById(R.id.toolbar_in_activity_project_details);
        setSupportActionBar(toolbar);

    }

    private void initGridView(){
        Log.d(TAG, "initGridView: Started");
        if(getImageCount(projectID, jobId, pointId)){

            gridAdapter = new GalleryImageAdapter(this, R.layout.layout_grid_imageview, mURLSyntex, getImageFromPointData(projectID, jobId, pointId));
            gridView.setAdapter(gridAdapter);
            gridView.setVisibility(View.VISIBLE);
        }
    }


    private void setOnClickListeners(){
        Log.d(TAG, "setOnClickListeners: Started");
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ProjectImages item = (ProjectImages) parent.getItemAtPosition(position);

                selectPhotoDialog(item.getImagePath(), position);

            }
        });

        gridView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                ProjectImages item = (ProjectImages) parent.getItemAtPosition(position);
                viewPhotoDialog(item.getProjectId(),item.getImagePath(), position);
                return true;
            }
        });


    }
    private boolean getImageCount(int projectId, int jobId, int pointId){
        Log.d(TAG, "getImageCount: Started");
        long count = 0;
        boolean results = false;
        Log.d(TAG, "getImageCount: Connecting to db");
        ProjectDatabaseHandler projectDb = new ProjectDatabaseHandler(mContext);
        SQLiteDatabase db = projectDb.getReadableDatabase();

        count = ProjectDatabaseHandler.getCountProjectImagesByPointID(db,projectId, jobId, pointId);

        Log.d(TAG, "getImageCount: Found: " + count);
        if (count !=0){
            results = true;
        }

        Log.d(TAG, "getImageCount: Closing Connection to db");
        db.close();
        return results;
    }

    private ArrayList<ProjectImages> getImageFromProjectData(Integer projectId){

        Log.d(TAG, "getImageFromProjectData: Connecting to db");
        ProjectDatabaseHandler projectDb = new ProjectDatabaseHandler(mContext);
        SQLiteDatabase db = projectDb.getReadableDatabase();

        ArrayList<ProjectImages> projectImages = new ArrayList<ProjectImages>(projectDb.getProjectImagesbyProjectID(db,projectId));

        Log.d(TAG, "getImageFromProjectData: Closing Connection to db");
        db.close();
        return projectImages;

    }

    private void viewPhotoDialog(Integer project_id, String mImagePath, int position){
        Log.d(TAG, "viewPhotoDialog: Started");
        
        DialogFragment viewDialog = DialogProjectPhotoView.newInstance(project_id,mURLSyntex,mImagePath);
        viewDialog.show(getFragmentManager(),"dialog_view");

    }

    private void selectPhotoDialog(String mImagePath, int position){
        Log.d(TAG, "viewPhotoDialog: Started");

        Intent returnIntent = new Intent();
        returnIntent.putExtra(getString(R.string.KEY_GALLERY_IMAGE_PATH),mImagePath);
        setResult(Activity.RESULT_OK,returnIntent);
        finish();

    }



    private ArrayList<ProjectImages> getImageFromPointData(int projectId, int jobId, int pointId){
        Log.d(TAG, "getImageFromProjectData: Connecting to db");
        ProjectDatabaseHandler projectDb = new ProjectDatabaseHandler(mContext);
        SQLiteDatabase db = projectDb.getReadableDatabase();

        ArrayList<ProjectImages> projectImages = new ArrayList<ProjectImages>(projectDb.getProjectImagesbyPointID(db,projectId, jobId, pointId));


        Log.d(TAG, "getImageFromProjectData: Closing DB Connection");
        db.close();

        return projectImages;

    }

    private void showToast(String data, boolean shortTime) {

        if (shortTime) {
            Toast.makeText(this, data, Toast.LENGTH_SHORT).show();

        } else{
            Toast.makeText(this, data, Toast.LENGTH_LONG).show();

        }

    }

}
