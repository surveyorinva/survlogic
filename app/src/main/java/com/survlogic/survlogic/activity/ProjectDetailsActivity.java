package com.survlogic.survlogic.activity;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.survlogic.survlogic.R;
import com.survlogic.survlogic.background.BackgroundProjectDetails;
import com.survlogic.survlogic.database.ProjectDatabaseHandler;
import com.survlogic.survlogic.model.Project;
import com.survlogic.survlogic.utils.MathHelper;
import com.survlogic.survlogic.utils.TimeHelper;

import java.text.SimpleDateFormat;

/**
 * Created by chrisfillmore on 7/2/2017.
 */

public class ProjectDetailsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final String TAG = "ProjectDetailsActivity";

    private CoordinatorLayout rootLayout;
    private Toolbar toolbar;
    private CollapsingToolbarLayout collapsingToolbar;
    private Context mContext = this;
    private GoogleMap mMap;

    private Project project;
    private int projectID;
    private double locationLatitude, locationLongitude;


    private TextView tvProjectName, tvProjectCreated, tvUnits, tvLocationLat, tvLocationLong,
            tvProjection, tvZone, tvStorage;
    private ImageView ivProjectImage;

    private static SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM dd,yyyy");

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_project_details);


        Log.e(TAG, "onCreate: Started");

        initView();
        initViewNavigation();

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        supportFinishAfterTransition();
    }

    private void initView(){

        Log.e(TAG, "initView: Started");

        Bundle extras = getIntent().getExtras();
        projectID = extras.getInt("PROJECT_ID");
        //project = (Project) Parcels.unwrap(getIntent().getParcelableExtra("project"));

        Log.e(TAG, "initView: Bundle Processed");

        tvProjectName = (TextView) findViewById(R.id.project_name_in_card_project_detail);
        tvProjectCreated = (TextView) findViewById(R.id.project_created_date_in_card_project_detail);
        tvStorage = (TextView) findViewById(R.id.project_storage_space_value);

        tvUnits = (TextView) findViewById(R.id.left_item1_value);
        tvProjection = (TextView) findViewById(R.id.left_item2_value);
        tvZone = (TextView) findViewById(R.id.left_item3_value);

        tvLocationLat = (TextView) findViewById(R.id.map_item1_value_lat);
        tvLocationLong = (TextView) findViewById(R.id.map_item1_value_long);

        ivProjectImage = (ImageView) findViewById(R.id.header_image_in_activity_project_details);

        Log.e(TAG, "initView: Views formed");

        boolean results = initValuesFromObject();

        if (results){
            Log.e(TAG, "initView: Populated fields OK");

        }else{
            Log.e(TAG, "initView: Populated fields Failed");
        }

    }

    private boolean initValuesFromObject(){
        boolean results = false;

        try{
            ProjectDatabaseHandler projectDb = new ProjectDatabaseHandler(this);
            SQLiteDatabase db = projectDb.getReadableDatabase();

            Project project = projectDb.getProjectById(db,projectID);

            locationLatitude = project.getmLocationLat();
            locationLongitude = project.getmLocationLong();
            initMapView();

            ivProjectImage.setImageBitmap(convertToBitmap(project.getmImage()));

            tvProjectName.setText(project.getmProjectName());

            int d = project.getmDateCreated();
            String stringDate = TimeHelper.getDateinFormat(d,dateFormat);
            tvProjectCreated.setText(getString(R.string.project_card_last_modified_date_create, stringDate));

            int units_pos = project.getmUnits()-1;
            String [] units_values = getResources().getStringArray(R.array.unit_measure_entries);
            tvUnits.setText(units_values[units_pos]);

            int projection_pos = project.getmProjection();
            String [] projection_values = getResources().getStringArray(R.array.project_projection_titles);
            tvProjection.setText(projection_values[projection_pos]);

            int zone_pos = project.getmZone();
            String [] zone_values = getResources().getStringArray(R.array.project_projection_zones_titles);
            tvZone.setText(zone_values[zone_pos]);

            int storage_pos = project.getmStorage();
            String [] storage_values = getResources().getStringArray(R.array.project_storage_titles);
            tvStorage.setText(storage_values[storage_pos]);

            tvLocationLat.setText(this.getString(R.string.gps_status_lat_value_string,
                    MathHelper.convertDECtoDMS(project.getmLocationLat(),3,true)));
            tvLocationLong.setText(this.getString(R.string.gps_status_long_value_string,
                    MathHelper.convertDECtoDMS(project.getmLocationLong(),3,true)));

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
        collapsingToolbar.setExpandedTitleColor(Color.parseColor("#00FFFFFF"));
        collapsingToolbar.setTitle("Project View");
    }

    private void initMapView(){
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map_container_in_project_details);

        mapFragment.getMapAsync(this);


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

    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;
        mMap.getUiSettings().setMapToolbarEnabled(false);

        LatLng project = new LatLng(locationLatitude, locationLongitude);
        mMap.addMarker(new MarkerOptions().position(project).title("Project"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(project,15));


        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                //do something here
            }
        });


    }
}
