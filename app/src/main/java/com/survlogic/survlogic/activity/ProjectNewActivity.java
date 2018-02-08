package com.survlogic.survlogic.activity;

import android.annotation.TargetApi;
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
import android.support.annotation.Nullable;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.survlogic.survlogic.BuildConfig;
import com.survlogic.survlogic.R;
import com.survlogic.survlogic.background.BackgroundProjectNew;
import com.survlogic.survlogic.model.Project;
import com.survlogic.survlogic.utils.FileHelper;
import com.survlogic.survlogic.utils.SurveyMathHelper;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by chrisfillmore on 6/12/2017.
 */

public class ProjectNewActivity extends AppCompatActivity {

    private static final String TAG = "ProjectNewActivity";
    private static final int ACTIVITY_KEY = 1;
    private static final int REQUEST_TAKE_PHOTO= 2;
    private static final int REQUEST_SELECT_PICTURE=3;
    private static final int REQUEST_PROJECTION = 4;
    private static final int KEY_PRJ_NEW = 0, KEY_PRJ_ADD = 1;
    private static Context mContext;
    private FileHelper fileHelper;

    private Project project = new Project();


    private EditText etProjectName;
    private TextView tvLocation_latitude, tvLocation_longitude, tvLocation_latitude_value,tvLocation_longitude_value ;
    private Spinner spStorage, spUnits, spProjection, spZone;
    private ImageView ivPreview;
    private Button btnPhoto_Camera_Get_Photo, btnSave, btnCancel;
    private ImageButton btnlocation_get_from_gps_survey;

    private String mCurrentPhotoPath, mImagePath;
    private Bitmap mBitmap, mBitmapPolished, mBitmapRaw;

    String mProjectName;
    private int mId, mStorage, mUnits;
    private int mProjection, mZone;
    private int mImageSystem = 0; //system generated value to determine if user took picture or use internal picture 0 = internal 1=user
    private double mLocationLat = 0, mLocationLong = 0;

    private String mSelectedProjectionString, mSelectedZoneString;
    private int mSelectedStrategy = 0;
    private double mSelectedProjectionScale, mSelectedProjectionOriginNorthing, mSelectedProjectionOriginEasting;

    private boolean isThereAProjection = false, isThereAZone = false;
    private String[] projectionChoices, projectionAdded;
    private ArrayAdapter<String> projectionAdapterNew, projectionAdapterAdded;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_project_new);
        mContext = ProjectNewActivity.this;
        fileHelper = new FileHelper(mContext);
        Log.d(TAG, "Started onCreateView");

        initView();

    }


    private void initView(){
        Log.d(TAG, "initView: Started...");
        etProjectName = (EditText) findViewById(R.id.project_name_in_project_new);
        etProjectName.setSelectAllOnFocus(true);

        spStorage = (Spinner) findViewById(R.id.storage_prompt_in_project_new);
        spUnits = (Spinner) findViewById(R.id.units_prompt_in_project_new);
        spProjection = (Spinner) findViewById(R.id.projection_prompt_in_project_new);
        spZone = (Spinner) findViewById(R.id.projection_zone_prompt_in_project_new);

        tvLocation_latitude = (TextView) findViewById(R.id.location_latitude);
        tvLocation_latitude_value = (TextView) findViewById(R.id.location_latitude_value);

        tvLocation_longitude = (TextView) findViewById(R.id.location_longitude);
        tvLocation_longitude_value = (TextView) findViewById(R.id.location_longitude_value);

        ivPreview = (ImageView) findViewById(R.id.photo_camera_image);

        btnlocation_get_from_gps_survey = (ImageButton) findViewById(R.id.location_get_from_gps_survey);
        btnPhoto_Camera_Get_Photo = (Button) findViewById(R.id.photo_camera_get_photo);

        btnSave = (Button) findViewById(R.id.Save_button);
        btnCancel = (Button) findViewById(R.id.Cancel_button);


        initProjectionAdapterNew();
        setProjectionSpinnerAdapter(KEY_PRJ_NEW);

        setOnClickListeners();
        populateProjectWithProjection();

    }

    private void setOnClickListeners(){
        Log.d(TAG, "setOnClickListeners: Started...");
        btnlocation_get_from_gps_survey.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToGpsSurveyActivity();
            }
        });

        btnPhoto_Camera_Get_Photo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callImageSelectionDialog();
                //startCamera();
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean result;

                // Validation
                //Required Items - 1
                result = verifyDataset(1);

                if (result){
                    // Save results
                    getValues();

                    // Create Project model
                    project.setmSystemImage(mImageSystem);
                    project.setmImagePath(mImagePath);

                    // Setup Background Task
                    BackgroundProjectNew backgroundProjectNew = new BackgroundProjectNew(ProjectNewActivity.this);

                    // Execute background task
                    backgroundProjectNew.execute(project);
                    returnResults();
                }

            }
        });
    }

    private void returnResults(){
        Log.d(TAG, "returnResults: Started");
        Intent returnIntent = new Intent();
        returnIntent.putExtra(getString(R.string.KEY_PROJECT_NAME),mProjectName);

        setResult(Activity.RESULT_OK,returnIntent);
        finish();

    }


    private void dispatchPhotoFromGalleryIntent() throws IOException{
        Intent intent = new Intent(Intent.ACTION_PICK,android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Select Picture"),REQUEST_SELECT_PICTURE);

    }

    //----------------------------------------------------------------------------------------------//

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult: RequestCode: " + requestCode + "ResultCode: " + resultCode);
        if (ACTIVITY_KEY == requestCode) {
            if(resultCode == Activity.RESULT_OK){

                mLocationLat = data.getDoubleExtra(getString(R.string.KEY_POSITION_LATITUDE),0);
                mLocationLong = data.getDoubleExtra(getString(R.string.KEY_POSITION_LONGITUDE),0);

                project.setmLocationLat(mLocationLat);
                project.setmLocationLong(mLocationLong);

                String strLatitude = SurveyMathHelper.convertDECtoDMSGeodetic(mLocationLat,3,false);
                //tvLocation_latitude.setText(getString(R.string.project_new_location_latitude_value,strLatitude));
                tvLocation_latitude.setText(getString(R.string.project_new_location_latitude_title));
                tvLocation_latitude_value.setText(strLatitude);
                tvLocation_latitude_value.setVisibility(View.VISIBLE);

                String strLongitude = SurveyMathHelper.convertDECtoDMSGeodetic(mLocationLong,3,true);
                //tvLocation_longitude.setText(getString(R.string.project_new_location_longitude_value,strLongitude));
                tvLocation_longitude.setText(getString(R.string.project_new_location_longitude_title));
                tvLocation_longitude_value.setText(strLongitude);
                tvLocation_longitude_value.setVisibility(View.VISIBLE);

            }
        }else if(REQUEST_TAKE_PHOTO == requestCode && resultCode == RESULT_OK){
            Uri imageUri = Uri.parse(mCurrentPhotoPath);
            File file = new File(imageUri.getPath());
            try {
                mBitmapRaw=decodeUri(imageUri,400);
                mBitmap = rotateImageIfRequired(mBitmapRaw,imageUri);
                ivPreview.setImageBitmap(mBitmap);

                Uri uri = fileHelper.saveImageToExternal(mBitmap);
                mImagePath = fileHelper.uriToString(uri);

                mImageSystem = 1;

            } catch (Exception e) {
                showToast("Caught Error: Could not set Photo to Image from Camera",true);

            }

        }else if(REQUEST_SELECT_PICTURE == requestCode && resultCode == RESULT_OK){
            if (data != null) {


                try {
                    final Uri imageUri = data.getData();
                    File file = new File(imageUri.getPath());
//                    final InputStream imageStream = getContentResolver().openInputStream(imageUri);
//                    final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);

                    if (imageUri !=null){
                        mBitmapRaw=decodeUri(imageUri,400);
                        mBitmap = rotateImageIfRequired(mBitmapRaw,imageUri);
                        ivPreview.setImageBitmap(mBitmap);

                        Uri uri = fileHelper.saveImageToExternal(mBitmap);
                        mImagePath = fileHelper.uriToString(uri);

                        mImageSystem = 1;
                        project.setmSystemImage(mImageSystem);
                    }


                } catch (Exception e) {
                    showToast("Caught Error: Could not set Photo to Image from Gallery",true);

                }
            }
        }else if(REQUEST_PROJECTION == requestCode){
            if(resultCode == RESULT_CANCELED){
                Log.d(TAG, "onActivityResult: Canceled");

                if(!isThereAProjection){
                    setProjectionSpinnerAdapter(KEY_PRJ_NEW);
                }else{
                    spProjection.setSelection(0);
                }


            }else if(resultCode == RESULT_OK){
                Log.d(TAG, "onActivityResult: Success");
                String displayName;

                mSelectedProjectionString = data.getStringExtra(getString(R.string.KEY_PROJECTION_STRING));
                String[] separatedProjectionValue = mSelectedProjectionString.split(",");
                String projectionName = separatedProjectionValue[0];
                displayName = projectionName;

                if(!projectionName.equals(getResources().getString(R.string.general_none))){
                    isThereAProjection = true;
                }
                Boolean isZone = Boolean.valueOf(separatedProjectionValue[3]);


                if(isZone){
                    mSelectedZoneString = data.getStringExtra(getString(R.string.KEY_PROJECTION_ZONE_STRING));
                    String[] separatedZoneValue = mSelectedZoneString.split(",");
                    String zoneName = separatedZoneValue[0];
                    displayName = zoneName;

                    isThereAZone = true;
                }else{
                    mSelectedZoneString = getResources().getString(R.string.projection_zone_none);

                    isThereAZone = false;
                }

                mSelectedStrategy = data.getIntExtra(getString(R.string.KEY_PROJECTION_STRATEGY),0);
                mSelectedProjectionScale = data.getDoubleExtra(getString(R.string.KEY_PROJECTION_STRATEGY_SCALE),0);
                mSelectedProjectionOriginNorthing= data.getDoubleExtra(getString(R.string.KEY_PROJECTION_STRATEGY_ORIGIN_NORTHING),0);
                mSelectedProjectionOriginEasting = data.getDoubleExtra(getString(R.string.KEY_PROJECTION_STRATEGY_ORIGIN_EASTING),0);

                projectionAdded = getResources().getStringArray(R.array.projection_added_titles);
                projectionAdded[0]=displayName;

                initProjectionAdapterAdded();
                setProjectionSpinnerAdapter(KEY_PRJ_ADD);
                populateProjectWithProjection();
            }
        }
    }

    //----------------------------------------------------------------------------------------------//

    private void initProjectionAdapterNew(){
        Log.d(TAG, "initProjectionAdapterNew: ");
        projectionChoices = getResources().getStringArray(R.array.projection_new_titles);


        projectionAdapterNew = new ArrayAdapter<String>(ProjectNewActivity.this,android.R.layout.simple_spinner_item,projectionChoices);
        projectionAdapterNew.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spProjection.setAdapter(projectionAdapterNew);
        spProjection.setSelection(0);


    }

    private void initProjectionAdapterAdded(){
        Log.d(TAG, "initProjectionAdapterAdded: ");

        projectionAdapterAdded = new ArrayAdapter<String>(ProjectNewActivity.this,android.R.layout.simple_spinner_item,projectionAdded);
        projectionAdapterAdded.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);


    }

    private void setProjectionSpinnerAdapter(int type){
        Log.d(TAG, "setProjectionSpinnerAdapter: ");

        spProjection.setOnItemSelectedListener(null);

        switch (type){
            case KEY_PRJ_NEW: //New
                spProjection.setAdapter(projectionAdapterNew);

                spProjection.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                        if(projectionChoices[position].equals(getResources().getString(R.string.general_none))){
                            //Do something here


                        }else if(projectionChoices[position].equals(getResources().getString(R.string.project_new_projection_spinner_Select_New_Projection))){
                            openNewProjectionActivity();
                        }

                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });
                break;

            case KEY_PRJ_ADD: //Added
                spProjection.setAdapter(projectionAdapterAdded);

                spProjection.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        if(projectionAdded[position].equals(getResources().getString(R.string.general_clear))){
                            clearProjectionFromActivity();


                        }else if(projectionAdded[position].equals(getResources().getString(R.string.general_edit))){
                            openEditProjectionActivity();
                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });

        }
    }

    private void openNewProjectionActivity(){
        Log.d(TAG, "openNewProjectionActivity: ");

        Intent i = new Intent(this, ProjectNewProjectionActivity.class);
        startActivityForResult(i,REQUEST_PROJECTION);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);

    }

    private void openEditProjectionActivity(){
        Log.d(TAG, "openEditProjectionActivity: ");

        Intent i = new Intent(this, ProjectNewProjectionActivity.class);
        i.putExtra(getString(R.string.KEY_PROJECTION_ISEDIT), true);
        i.putExtra(getString(R.string.KEY_PROJECTION_STRING),mSelectedProjectionString);
        i.putExtra(getString(R.string.KEY_PROJECTION_ZONE_STRING),mSelectedZoneString);
        i.putExtra(getString(R.string.KEY_PROJECTION_STRATEGY),mSelectedStrategy);
        i.putExtra(getString(R.string.KEY_PROJECTION_STRATEGY_SCALE),mSelectedProjectionScale);
        i.putExtra(getString(R.string.KEY_PROJECTION_STRATEGY_ORIGIN_NORTHING),mSelectedProjectionOriginNorthing);
        i.putExtra(getString(R.string.KEY_PROJECTION_STRATEGY_ORIGIN_EASTING),mSelectedProjectionOriginEasting);

        startActivityForResult(i,REQUEST_PROJECTION);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);


    }


    private void clearProjectionFromActivity(){
        Log.d(TAG, "clearProjectionFromActivity: Started");

        setProjectionSpinnerAdapter(KEY_PRJ_NEW);
        mSelectedProjectionString = getResources().getString(R.string.projection_none);
        mSelectedZoneString = getResources().getString(R.string.projection_zone_none);


    }

    //----------------------------------------------------------------------------------------------//
    private void callImageSelectionDialog(){
        final CharSequence[] items = { getString(R.string.project_new_dialog_takePhoto), getString(R.string.project_new_dialog_getImage),
                getString(R.string.general_cancel) };

        TextView title = new TextView(this);  //was context not this

            title.setText(getString(R.string.project_new_dialog_title));
            title.setBackgroundColor(Color.WHITE);
            title.setPadding(10, 15, 15, 10);
            title.setGravity(Gravity.CENTER);
            title.setTextColor(Color.BLACK);
            title.setTextSize(22);

        AlertDialog.Builder builder = new AlertDialog.Builder(ProjectNewActivity.this);

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



    protected Bitmap decodeUri(Uri selectedImage, int REQUIRED_SIZE) {

        try {

            // Decode image size
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(getContentResolver().openInputStream(selectedImage), null, o);

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

    //Convert bitmap to bytes
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
    private byte[] convertImageToByte(Bitmap b){

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        b.compress(Bitmap.CompressFormat.PNG, 0, bos);
        return bos.toByteArray();

    }




    //----------------------------------------------------------------------------------------------//

    private void goToGpsSurveyActivity(){
        Intent intent = new Intent(this, GpsSurveyActivity.class);
        startActivityForResult(intent,ACTIVITY_KEY);

    }





    //----------------------------------------------------------------------------------------------//
    private boolean verifyDataset(int controlValue){
        boolean results = false;

        switch(controlValue){
            case 1: //etProjectName
                if(etProjectName.getText().toString().trim().equals("")){
                    results = false;
                    String txtVerification = getString(R.string.project_new_validation_project_name_error);
                    showToast(txtVerification,true);
                }else{
                    results = true;
                    project.setmProjectName(etProjectName.getText().toString());
                }

                break;

            //2-spStorage
            //3-spUnits
            //4-spProjection
            //5-spZone

            case 6: //6-mLocationLat & mLocationLong

                if (mLocationLat ==0 & mLocationLat==0){
                    results = false;
                }else{
                    results = true;
                }
                break;

            //7-mBitmap

        }

        return results;
    }

    private void populateProjectWithProjection(){
        Log.d(TAG, "populateProjectWithProjection: Started");

        if(isThereAProjection){
            project.setmProjection(1);
        }else{
            project.setmProjection(0);
        }

        project.setProjectionString(mSelectedProjectionString);

        if(isThereAZone){
            project.setmZone(1);
        }else{
            project.setmZone(0);
        }

        project.setZoneString(mSelectedZoneString);

        project.setSurveyStrategy(mSelectedStrategy);
        project.setProjectionScale(mSelectedProjectionScale);
        project.setProjectionOriginNorth(mSelectedProjectionOriginNorthing);
        project.setProjectionOriginEast(mSelectedProjectionOriginEasting);

    }
    
    private void getValues(){

        //1
        mProjectName = etProjectName.getText().toString();

        //2
        int storage_pos = spStorage.getSelectedItemPosition();
        String [] storage_values = getResources().getStringArray(R.array.project_storage_values);
        mStorage = Integer.valueOf(storage_values[storage_pos]);
        project.setmStorage(mStorage);

        //3
        int units_pos = spUnits.getSelectedItemPosition();
        String[] units_values = getResources().getStringArray(R.array.unit_measure_values);
        mUnits = Integer.valueOf(units_values[units_pos]);
        project.setmUnits(mUnits);

        //6
        if (mImageSystem == 1){

        }else{
            //convert system bitmap for project to bitmap/byte
            Bitmap icon = BitmapFactory.decodeResource(getResources(), R.drawable.ic_project_icon_32);

            mImagePath = null;

        }

        //7
        //GPS Values already exist from
        //mLocationLat & mLocationLong

    }


    //----------------------------------------------------------------------------------------------//

    private void showToast(String data, boolean shortTime) {

        if (shortTime) {
            Toast.makeText(this, data, Toast.LENGTH_SHORT).show();

        } else{
            Toast.makeText(this, data, Toast.LENGTH_LONG).show();

        }

    }


}
