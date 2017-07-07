package com.survlogic.survlogic.activity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.survlogic.survlogic.R;
import com.survlogic.survlogic.background.BackgroundProjectDetails;
import com.survlogic.survlogic.model.Project;
import com.survlogic.survlogic.utils.TimeHelper;

import org.parceler.Parcels;

import java.text.SimpleDateFormat;

/**
 * Created by chrisfillmore on 7/2/2017.
 */

public class ProjectDetailsActivity extends AppCompatActivity {

    private static final String TAG = "ProjectDetailsActivity";

    private CoordinatorLayout rootLayout;
    private Toolbar toolbar;
    private CollapsingToolbarLayout collapsingToolbar;

    private Project project;
    private int projectID;

    private TextView tvProjectName, tvProjectCreated, tvUnits;
    private ImageView ivProjectImage;

    private static SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM dd,yyyy");

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_project_details);

        initView();
        initViewNavigation();

    }

    private void initView(){

        Bundle extras = getIntent().getExtras();
        projectID = extras.getInt("PROJECT_ID");
        project = (Project) Parcels.unwrap(getIntent().getParcelableExtra("project"));

        tvProjectName = (TextView) findViewById(R.id.project_name_in_card_project_detail);
        tvProjectCreated = (TextView) findViewById(R.id.project_created_date_in_card_project_detail);
        tvUnits = (TextView) findViewById(R.id.item1_value);

        ivProjectImage = (ImageView) findViewById(R.id.header_image_in_activity_project_details);

        boolean results = initValuesFromObject();

        if (results){
            //Do something here

        }else{
            //Don't do something here
        }

    }

    private boolean initValuesFromObject(){
        boolean results = false;

        try{
            ivProjectImage.setImageBitmap(convertToBitmap(project.getmImage()));

            tvProjectName.setText(project.getmProjectName());

            int d = project.getmDateCreated();
            String stringDate = TimeHelper.getDateinFormat(d,dateFormat);
            tvProjectCreated.setText(getString(R.string.project_card_last_modified_date_create, stringDate));

            int units_pos = project.getmUnits()-1;
            String [] units_values = getResources().getStringArray(R.array.unit_measure_entries);
            tvUnits.setText(units_values[units_pos]);

            results = true;

        }catch (Exception e){
            e.printStackTrace();
        }

        return results;

    }

    private void initValuesfromBackground(){
        BackgroundProjectDetails backgroundProjectList = new BackgroundProjectDetails(this);
        backgroundProjectList.execute(projectID);


    }

    private void initViewNavigation(){
        toolbar = (Toolbar) findViewById(R.id.toolbar_in_activity_project_details);
        setSupportActionBar(toolbar);

        collapsingToolbar = (CollapsingToolbarLayout) findViewById(R.id.collapsingToolbar_in_activity_project_details);
        collapsingToolbar.setTitle(" ");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_project_details_menu,menu);

        return true;
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);


    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.toolbar_project_details_item1:
                //some action here
                break;

            case R.id.toolbar_project_details_item2:
                //some action here
                break;

            case R.id.toolbar_project_details_item3:
                //some action here
                break;

        }

        return super.onOptionsItemSelected(item);
    }


    private Bitmap convertToBitmap(byte[] b){
        return BitmapFactory.decodeByteArray(b, 0, b.length);

    }

    private void showToast(String data, boolean shortTime) {

        if (shortTime) {
            Toast.makeText(this, data, Toast.LENGTH_SHORT).show();

        } else{
            Toast.makeText(this, data, Toast.LENGTH_LONG).show();

        }

    }

}
