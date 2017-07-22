package com.survlogic.survlogic.activity;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.GridView;
import android.widget.ProgressBar;

import com.survlogic.survlogic.R;
import com.survlogic.survlogic.adapter.GalleryImageAdapter;
import com.survlogic.survlogic.adapter.GridImageAdapter;
import com.survlogic.survlogic.database.ProjectDatabaseHandler;
import com.survlogic.survlogic.model.ProjectImages;

import java.util.ArrayList;

/**
 * Created by chrisfillmore on 7/21/2017.
 */

public class PhotoGalleryActivity extends AppCompatActivity {

    private static final String TAG = "PhotoGalleryActivity";
    private AppBarLayout appBarLayout;
    private Toolbar toolbar;
    private Context mContext;

    private GridView gridView;
    private GalleryImageAdapter gridAdapter;

    private int projectId;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_gallery);

        mContext = PhotoGalleryActivity.this;


        initView();
        initViewNavigation();


    }

    @Override
    protected void onResume() {
        super.onResume();

        initGridView();
    }

    private void initView(){
        Bundle extras = getIntent().getExtras();
        projectId = extras.getInt("PROJECT_ID");

        gridView = (GridView) findViewById(R.id.photo_grid_view);

    }

    private void initViewNavigation(){
        appBarLayout = (AppBarLayout) findViewById(R.id.appbar_in_activity_project_details);

        toolbar = (Toolbar) findViewById(R.id.toolbar_in_activity_project_details);
        setSupportActionBar(toolbar);

    }

    private void initGridView(){

        if(getImageCount(projectId)){

            gridAdapter = new GalleryImageAdapter(this, R.layout.layout_grid_imageview, getImageFromProjectData(projectId));
            gridView.setAdapter(gridAdapter);
            gridView.setVisibility(View.VISIBLE);
        }
    }

    private boolean getImageCount(Integer projectId){
        long count = 0;
        boolean results = false;
        ProjectDatabaseHandler projectDb = new ProjectDatabaseHandler(mContext);
        SQLiteDatabase db = projectDb.getReadableDatabase();

        count = ProjectDatabaseHandler.getCountProjectImagesByProjectID(db,projectId);

        if (count !=0){
            results = true;
        }

        db.close();
        return results;
    }

    private ArrayList<ProjectImages> getImageFromProjectData(Integer projectId){

        ProjectDatabaseHandler projectDb = new ProjectDatabaseHandler(mContext);
        SQLiteDatabase db = projectDb.getReadableDatabase();

        ArrayList<ProjectImages> projectImages = new ArrayList<ProjectImages>(projectDb.getProjectImagesbyProjectID(db,projectId));

        return projectImages;

    }

}
