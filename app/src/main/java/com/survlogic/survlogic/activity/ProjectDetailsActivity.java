package com.survlogic.survlogic.activity;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.FileProvider;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.survlogic.survlogic.BuildConfig;
import com.survlogic.survlogic.R;
import com.survlogic.survlogic.adapter.GridImageAdapter;
import com.survlogic.survlogic.background.BackgroundProjectDetails;
import com.survlogic.survlogic.database.ProjectDatabaseHandler;
import com.survlogic.survlogic.model.Project;
import com.survlogic.survlogic.model.ProjectImages;
import com.survlogic.survlogic.utils.ImageHelper;
import com.survlogic.survlogic.utils.MathHelper;
import com.survlogic.survlogic.utils.TimeHelper;
import com.survlogic.survlogic.utils.UniversalImageLoader;
import com.survlogic.survlogic.view.DialogProjectDescriptionAdd;
import com.survlogic.survlogic.view.DialogProjectJobAdd;
import com.survlogic.survlogic.view.DialogProjectPhotoAdd;
import com.survlogic.survlogic.view.DialogProjectPhotoView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by chrisfillmore on 7/2/2017.
 */

public class ProjectDetailsActivity extends AppCompatActivity implements OnMapReadyCallback, AppBarLayout.OnOffsetChangedListener {

    private static final String TAG = "ProjectDetailsActivity";
    private static final int REQUEST_TAKE_PHOTO = 2;
    private static final int REQUEST_SELECT_PICTURE = 3;

    private static final int DELAY_TO_MAP = 1500;
    private static final int DELAY_TO_GRID = 2000;

    private SwipeRefreshLayout swipeRefreshLayout;
    private CoordinatorLayout rootLayout;
    private AppBarLayout appBarLayout;
    private Toolbar toolbar;
    private CollapsingToolbarLayout collapsingToolbar;
    private GoogleMap mMap;

    private static Context mContext;
    private ImageHelper imageHelper;

    private Project project;
    private int projectID;
    private double locationLatitude, locationLongitude;
    private String mCurrentPhotoPath, projectDescription, mProjectImagePath;
    private Bitmap mBitmap, mBitmapPolished, mBitmapRaw;

    private boolean mProgressBarShow = false;
    private String mURLSyntex = "file://";

    private TextView tvProjectName, tvProjectCreated, tvUnits, tvLocationLat, tvLocationLong,
            tvProjection, tvZone, tvStorage, tvDescription;
    private ImageView ivProjectImage, ivGridImage;
    private Button btTakePhoto, btPostDescription;
    private ProgressBar pbLoading;

    private GridView gridView;
    private GridImageAdapter gridAdapter;

    private ProgressDialog progressDialog;

    private static SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM dd,yyyy");

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_project_details);
        mContext = ProjectDetailsActivity.this;
        imageHelper = new ImageHelper(mContext);

        Log.d(TAG, "onCreate: Started---------------------------->");


        initViewNavigation();
        initView();
        setOnClickListeners();
        showProgressBar();

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        supportFinishAfterTransition();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause: Started");
        appBarLayout.removeOnOffsetChangedListener(this);

    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: Started");
        appBarLayout.addOnOffsetChangedListener(this);
        
        boolean results = initValuesFromObject();

        if (results){
            Log.d(TAG, "initView: Populated fields OK");
            showMapView();
            Log.d(TAG, "showMapView: Started");
            showGridView();
            Log.d(TAG, "initGridView: Started");
            showProgressBar();

        }else{
            Log.e(TAG, "initView: Populated fields Failed");
            showProgressBar();
        }

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (this.REQUEST_TAKE_PHOTO == requestCode && resultCode == RESULT_OK) {
            Uri imageUri = Uri.parse(mCurrentPhotoPath);
            File file = new File(imageUri.getPath());

            try {
                mBitmapRaw=decodeUri(imageUri,400);
                mBitmap = rotateImageIfRequired(mBitmapRaw,imageUri);

                createPhotoDialog(mBitmap);

            } catch (Exception e) {
                showToast("Caught Error: Could not set Photo to Image from Camera",true);

            }
        } else if (this.REQUEST_SELECT_PICTURE == requestCode && resultCode == RESULT_OK) {
            if (data != null) {


                try {
                    final Uri imageUri = data.getData();
                    File file = new File(imageUri.getPath());

                    if (imageUri !=null){
                        mBitmapRaw=decodeUri(imageUri,400);
                        mBitmap = rotateImageIfRequired(mBitmapRaw,imageUri);
                        createPhotoDialog(mBitmap);

                    }


                } catch (Exception e) {
                    showToast("Caught Error: Could not set Photo to Image from Gallery",true);

                }
            }
        }
    }

    @Override
    public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
        swipeRefreshLayout.setEnabled(verticalOffset == 0);
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
                //Create new job
                    createProjectJobDialog(projectID);

                break;

            case R.id.toolbar_project_details_item2:
                //Settings
                break;

            case R.id.toolbar_project_details_item3:
                //Delete Project

                break;

        }

        return super.onOptionsItemSelected(item);
    }

    //-------------------------------------------------------------------------------------------------------------------------//

    private void initView(){

        Log.d(TAG, "initView: Started");

        Bundle extras = getIntent().getExtras();
        projectID = extras.getInt("PROJECT_ID");

        tvProjectName = (TextView) findViewById(R.id.project_name_in_card_project_detail);
        tvProjectCreated = (TextView) findViewById(R.id.project_created_date_in_card_project_detail);
        tvStorage = (TextView) findViewById(R.id.project_storage_space_value);

        tvUnits = (TextView) findViewById(R.id.left_item1_value);
        tvProjection = (TextView) findViewById(R.id.left_item2_value);
        tvZone = (TextView) findViewById(R.id.left_item3_value);

        tvLocationLat = (TextView) findViewById(R.id.map_item1_value_lat);
        tvLocationLong = (TextView) findViewById(R.id.map_item1_value_long);

        tvDescription = (TextView) findViewById(R.id.card4_projectNotes);

        ivProjectImage = (ImageView) findViewById(R.id.header_image_in_activity_project_details);

        btTakePhoto = (Button) findViewById(R.id.card3_take_photo);
        btPostDescription = (Button) findViewById(R.id.card4_post_description);

        gridView = (GridView) findViewById(R.id.photo_grid_view);

        pbLoading = (ProgressBar) findViewById(R.id.progressBar_Loading);

        Log.e(TAG, "initView: Views formed");

    }

    private void setOnClickListeners(){
        Log.d(TAG, "setOnClickListeners: Started");
        btTakePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callImageSelectionDialog();
            }
        });

        btPostDescription.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createProjectNoteDialog(projectID,projectDescription);
            }
        });


        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        showProjectsDetailsLocalRefresh();

                    }
                }, 1000);
            }
        });

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ProjectImages item = (ProjectImages) parent.getItemAtPosition(position);

                //viewPhotoDialog(item.getProjectId(),imageHelper.convertToBitmap(item.getImage()), position);
                viewPhotoDialog(item.getProjectId(),item.getImagePath(), position);

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

    private boolean initValuesFromObject(){
        Log.d(TAG, "initValuesFromObject: Started");
        boolean results = false;

        try{
            Log.d(TAG, "initValuesFromObject: Connecting to db");
            ProjectDatabaseHandler projectDb = new ProjectDatabaseHandler(this);
            SQLiteDatabase db = projectDb.getReadableDatabase();

            Project project = projectDb.getProjectById(db,projectID);

            locationLatitude = project.getmLocationLat();
            locationLongitude = project.getmLocationLong();

            mProjectImagePath = project.getmImagePath();

            Log.d(TAG, "initValuesFromObject: Image Path: " + mProjectImagePath);
            UniversalImageLoader.setImage(mProjectImagePath,ivProjectImage,null, mURLSyntex);

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

            projectDescription = project.getmProjectDescription();
            if(!isStringEmpty(projectDescription)) {
                tvDescription.setText(projectDescription);
                tvDescription.setVisibility(View.VISIBLE);
            }else{
                tvDescription.setVisibility(View.GONE);
            }


            mProgressBarShow = true;
            results = mProgressBarShow;

            Log.d(TAG, "initValuesFromObject: Database Retrieval Complete.  Closing DB Connection");
            db.close();

        }catch (Exception e){
            e.printStackTrace();
        }

        return results;

    }

    private void initValuesfromBackground(){  //Not currently used.  This would be used if and when we want to AsyncTask load.
        BackgroundProjectDetails backgroundProjectList = new BackgroundProjectDetails(this);
        backgroundProjectList.execute(projectID);


    }

    private void initViewNavigation(){
        Log.d(TAG, "initViewNavigation: Started");
        appBarLayout = (AppBarLayout) findViewById(R.id.appbar_in_activity_project_details);

        toolbar = (Toolbar) findViewById(R.id.toolbar_in_activity_project_details);
        setSupportActionBar(toolbar);

        collapsingToolbar = (CollapsingToolbarLayout) findViewById(R.id.collapsingToolbar_in_activity_project_details);
        collapsingToolbar.setExpandedTitleColor(Color.parseColor("#00FFFFFF"));
        collapsingToolbar.setTitle("Project View");

        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_view_in_activity_project_details);
        swipeRefreshLayout.setColorSchemeResources(R.color.google_blue, R.color.google_green, R.color.google_red, R.color.google_yellow);
    }

    //-------------------------------------------------------------------------------------------------------------------------//
    private ArrayList<ProjectImages> getImageFromProjectData(Integer projectId){
        Log.d(TAG, "getImageFromProjectData: Connecting to db");
        ProjectDatabaseHandler projectDb = new ProjectDatabaseHandler(mContext);
        SQLiteDatabase db = projectDb.getReadableDatabase();

        ArrayList<ProjectImages> projectImages = new ArrayList<ProjectImages>(projectDb.getProjectImagesbyProjectID(db,projectId));


        Log.d(TAG, "getImageFromProjectData: Closing DB Connection");
        db.close();

        return projectImages;

    }

    private boolean getImageCount(Integer projectId){

        Log.d(TAG, "getImageCount: Connecting to db");
        long count = 0;
        boolean results = false;
        ProjectDatabaseHandler projectDb = new ProjectDatabaseHandler(mContext);
        SQLiteDatabase db = projectDb.getReadableDatabase();

        count = ProjectDatabaseHandler.getCountProjectImagesByProjectID(db,projectId);

        if (count !=0){
            results = true;
        }

        Log.d(TAG, "getImageCount: Closing DB Connection");
        db.close();
        return results;
    }


    //-------------------------------------------------------------------------------------------------------------------------//

    /**
     *Dialog Inputs (DI)
     */

    private void createPhotoDialog(Bitmap bitmap){
        DialogFragment pointDialog = DialogProjectPhotoAdd.newInstance(projectID, bitmap);
        pointDialog.show(getFragmentManager(),"dialog");

    }

    private void viewPhotoDialog(Integer project_id, String imagePath, int position){
        if(position == 3){
            Intent intent = new Intent(mContext, PhotoGalleryActivity.class);
            intent.putExtra("PROJECT_ID",projectID);
            startActivity(intent);

        }else{

            DialogFragment viewDialog = DialogProjectPhotoView.newInstance(project_id,mURLSyntex,imagePath);
            viewDialog.show(getFragmentManager(),"dialog_view");
        }

    }

    private void deletePhotoDialog(final Integer photo_id, int position){

        if(position == 3){


        }else{
            new AlertDialog.Builder(mContext)
                    .setMessage(getString(R.string.dialog_delete_image))
                    .setPositiveButton(getString(R.string.general_yes), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ProjectDatabaseHandler projectDb = new ProjectDatabaseHandler(mContext);
                            SQLiteDatabase db = projectDb.getWritableDatabase();

                            projectDb.deleteProjectImageById(db,photo_id);

                            showToast("Deleting Photo", true);
                            showProjectDetailsDialogRefresh();
                        }
                    })
                    .setNegativeButton(getString(R.string.general_no), null)
                    .show();


        }

    }

    private void createProjectNoteDialog(final Integer project_id, String description){
        DialogFragment viewDialog = DialogProjectDescriptionAdd.newInstance(projectID,description);
        viewDialog.show(getFragmentManager(),"dialog");
    }



    private void createProjectJobDialog(final Integer project_id){
        DialogFragment viewDialog = DialogProjectJobAdd.newInstance(projectID);
        viewDialog.show(getFragmentManager(),"dialog");


    }
    //-------------------------------------------------------------------------------------------------------------------------//

    /**
     * Map View
     */
    private void showMapView(){
        if(locationLatitude!=0) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    initMapView();
                }
            },DELAY_TO_MAP);
        }else{
            hideMapView();
        }
    }

    private void initMapView(){
        Log.d(TAG, "initMapView: Started");
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map_container_in_project_details);

        mapFragment.getMapAsync(this);


    }

    private void hideMapView(){
        tvLocationLat.setVisibility(View.GONE);
        tvLocationLong.setVisibility(View.GONE);

        View myMap = (View) findViewById(R.id.map_container_in_project_details);
        myMap.setVisibility(View.GONE);


    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d(TAG, "onMapReady: Started");
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

    //-------------------------------------------------------------------------------------------------------------------------//

    /**
     * Grid View (GV)
     */

    private void showGridView(){
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    initGridView();
                }
            },DELAY_TO_GRID);
    }

    private void initGridView(){

        if(getImageCount(projectID)){

            gridAdapter = new GridImageAdapter(this, R.layout.layout_grid_imageview, mURLSyntex, getImageFromProjectData(projectID));
            gridView.setAdapter(gridAdapter);
            gridView.setVisibility(View.VISIBLE);
        }
    }

    private void refreshGridView() {
        if(getImageCount(projectID)){

            //Todo GridAdapter on 1st run does not exist.  Need to check and see if gridAdapter has been created, if not, create

            gridAdapter.clear();

            gridAdapter = new GridImageAdapter(this, R.layout.layout_grid_imageview, mURLSyntex, getImageFromProjectData(projectID));
            gridView.setAdapter(gridAdapter);
            gridView.setVisibility(View.VISIBLE);


        }
    }

    private void showProjectsDetailsLocalRefresh(){
        refreshGridView();

        if(swipeRefreshLayout.isRefreshing()){
            swipeRefreshLayout.setRefreshing(false);
        }

    }

    public void showProjectDetailsDialogRefresh(){
        if(!swipeRefreshLayout.isRefreshing()){
            swipeRefreshLayout.setRefreshing(true);
        }

        refreshGridView();

        if(swipeRefreshLayout.isRefreshing()){
            swipeRefreshLayout.setRefreshing(false);
        }


    }


    //-------------------------------------------------------------------------------------------------------------------------//

    private void showProgressBar(){
        if (mProgressBarShow){
            pbLoading.setVisibility(View.GONE);
        }else{
            pbLoading.setVisibility(View.VISIBLE);
        }

    }

    //-------------------------------------------------------------------------------------------------------------------------//

    /**
     * Image Objects
     */

    private void callImageSelectionDialog(){
        final CharSequence[] items = { getString(R.string.project_new_dialog_takePhoto), getString(R.string.project_new_dialog_getImage),
                getString(R.string.general_cancel) };

        TextView title = new TextView(this);  //was context not this

        title.setText(getString(R.string.photo_dialog_title));
        title.setBackgroundColor(Color.WHITE);
        title.setPadding(10, 15, 15, 10);
        title.setGravity(Gravity.CENTER);
        title.setTextColor(Color.BLACK);
        title.setTextSize(22);

        AlertDialog.Builder builder = new AlertDialog.Builder(ProjectDetailsActivity.this);

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

                    case 2: //Cancel

                        dialog.dismiss();
                        break;
                }

            }
        });
        builder.show();
    }

    private void startCamera() {
        try {
            dispatchTakePictureIntent();

        } catch (IOException e) {
            showToast("Caught Error: Accessing Camera Exception",true);
        }
    }

    private void startPhotoGallery(){

        try{
            dispatchPhotoFromGalleryIntent();

        } catch (IOException e){
            showToast("caught Error: Accessing Gallery Exception",true);
        }

    }

    private void dispatchTakePictureIntent() throws IOException {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
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
        Intent intent = new Intent(Intent.ACTION_PICK,android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Select Picture"),REQUEST_SELECT_PICTURE);

    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);

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
            return BitmapFactory.decodeStream(getContentResolver().openInputStream(selectedImage), null, o2);
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    private static Bitmap rotateImageIfRequired(Bitmap img, Uri selectedImage) throws IOException {

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


    private String uriToString(Uri uri){
        String results = uri.toString();
        return results;
    }

    private Uri stringToUri(String stringUri){
        Uri results = Uri.parse(stringUri);
        return results;
    }

    private boolean isStringNull(String string){
        Log.d(TAG, "isStringNull: checking string is equal to null (User Input).");

        if(string.equals("")){
            return true;
        }
        else{
            return false;
        }
    }

    private boolean isStringEmpty(String string){
        Log.d(TAG, "isStringEmpty: checking string is null or empty (from DB)");

        if (string !=null && !string.isEmpty()) {
            return false;
        }else {
            return true;
        }


    }

}

