package com.survlogic.survlogic.activity;

import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.survlogic.survlogic.R;
import com.survlogic.survlogic.adapter.GalleryImageAdapter;
import com.survlogic.survlogic.adapter.GridImageAdapter;
import com.survlogic.survlogic.database.ProjectDatabaseHandler;
import com.survlogic.survlogic.model.ProjectImages;
import com.survlogic.survlogic.utils.ImageHelper;
import com.survlogic.survlogic.view.DialogProjectPhotoView;

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

    private ImageHelper imageHelper;
    private int projectId;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_gallery);

        mContext = PhotoGalleryActivity.this;
        imageHelper = new ImageHelper(mContext);

        initView();
        initViewNavigation();
        setOnClickListeners();


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


    private void setOnClickListeners(){

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ProjectImages item = (ProjectImages) parent.getItemAtPosition(position);

                viewPhotoDialog(item.getProjectId(),imageHelper.convertToBitmap(item.getImage()), position);

            }
        });

        gridView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                ProjectImages item = (ProjectImages) parent.getItemAtPosition(position);


                deletePhotoDialog(item.getId(), position);
                return true;
            }
        });


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

    private void viewPhotoDialog(Integer project_id, Bitmap bitmap, int position){
            DialogFragment viewDialog = DialogProjectPhotoView.newInstance(project_id,bitmap);
            viewDialog.show(getFragmentManager(),"dialog_view");

    }

    private void deletePhotoDialog(final Integer photo_id, int position) {
        new AlertDialog.Builder(mContext)
                .setMessage(getString(R.string.dialog_delete_image))
                .setPositiveButton(getString(R.string.general_yes), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        ProjectDatabaseHandler projectDb = new ProjectDatabaseHandler(mContext);
                        SQLiteDatabase db = projectDb.getWritableDatabase();

                        projectDb.deleteProjectImageById(db,photo_id);

                        showToast("Deleting Photo", true);
                        refreshGridView();


                    }
                })
                .setNegativeButton(getString(R.string.general_no), null)
                .show();

    }


    private void refreshGridView() {
        if(getImageCount(projectId)){
            gridAdapter.clear();

            gridAdapter = new GalleryImageAdapter(this, R.layout.layout_grid_imageview, getImageFromProjectData(projectId));
            gridView.setAdapter(gridAdapter);
            gridView.setVisibility(View.VISIBLE);


        }
    }

    private void showToast(String data, boolean shortTime) {

        if (shortTime) {
            Toast.makeText(this, data, Toast.LENGTH_SHORT).show();

        } else{
            Toast.makeText(this, data, Toast.LENGTH_LONG).show();

        }

    }

}
