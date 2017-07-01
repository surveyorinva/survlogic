package com.survlogic.survlogic.activity;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.survlogic.survlogic.BuildConfig;
import com.survlogic.survlogic.R;
import com.survlogic.survlogic.background.BackgroundProjectSetup;
import com.survlogic.survlogic.model.Project;
import com.survlogic.survlogic.utils.MathHelper;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
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

    private EditText etProjectName;
    private TextView tvLocation_latitude, tvLocation_longitude, tvLocation_latitude_value,tvLocation_longitude_value ;
    private Spinner spStorage, spUnits, spProjection, spZone;
    private ImageView ivPreview;
    private Button btnPhoto_Camera_Get_Photo, btnSave, btnCancel;
    private ImageButton btnlocation_get_from_gps_survey;

    private String mCurrentPhotoPath;
    private Bitmap mBitmap;

    String mProjectName;
    int mId, mStorage, mUnits;
    int mProjection, mZone;
    int mImageSystem = 0; //system generated value to determine if user took picture or use internal picture 0 = internal 1=user
    double mLocationLat = 0, mLocationLong = 0;
    byte[] mImage;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_project_new);

        Log.e(TAG, "Started onCreateView");

        initView();


    }


    private void initView(){

        etProjectName = (EditText) findViewById(R.id.project_name_in_project_new);

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

        setOnClickListeners();

    }

    private void setOnClickListeners(){

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
                    Project project = new Project(mProjectName,mStorage,mUnits,mProjection,mZone,mLocationLat,mLocationLong,mImageSystem,mImage);

                    // Setup Background Task
                    BackgroundProjectSetup backgroundProjectSetup = new BackgroundProjectSetup(ProjectNewActivity.this);

                    // Execute background task
                    backgroundProjectSetup.execute(project);
                    finish();
                }

            }
        });

    }


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

        // builder.setTitle("Add Photo!");
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


    private void dispatchPhotoFromGalleryIntent() throws IOException{
        Intent intent = new Intent(Intent.ACTION_PICK,android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Select Picture"),REQUEST_SELECT_PICTURE);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (this.ACTIVITY_KEY == requestCode) {
            if(resultCode == Activity.RESULT_OK){

                mLocationLat = data.getDoubleExtra(getString(R.string.KEY_POSITION_LATITUDE),0);
                mLocationLong = data.getDoubleExtra(getString(R.string.KEY_POSITION_LONGITUDE),0);

                String strLatitude = MathHelper.convertDECtoDMS(mLocationLat,3,false);
                //tvLocation_latitude.setText(getString(R.string.project_new_location_latitude_value,strLatitude));
                tvLocation_latitude.setText(getString(R.string.project_new_location_latitude_title));
                tvLocation_latitude_value.setText(strLatitude);
                tvLocation_latitude_value.setVisibility(View.VISIBLE);

                String strLongitude = MathHelper.convertDECtoDMS(mLocationLong,3,true);
                //tvLocation_longitude.setText(getString(R.string.project_new_location_longitude_value,strLongitude));
                tvLocation_longitude.setText(getString(R.string.project_new_location_longitude_title));
                tvLocation_longitude_value.setText(strLongitude);
                tvLocation_longitude_value.setVisibility(View.VISIBLE);

            }
        }else if(this.REQUEST_TAKE_PHOTO == requestCode && resultCode == RESULT_OK){
            Uri imageUri = Uri.parse(mCurrentPhotoPath);
            File file = new File(imageUri.getPath());
            try {
                InputStream ims = new FileInputStream(file);
                ivPreview.setImageBitmap(BitmapFactory.decodeStream(ims));
                mBitmap=decodeUri(imageUri,400);
                mImageSystem = 1;

            } catch (FileNotFoundException e) {
                showToast("Caught Error: Could not set Photo to Image from Camera",true);
                return;
            }

        }else if(this.REQUEST_SELECT_PICTURE == requestCode && resultCode == RESULT_OK){
            if (data != null) {


                try {
                    final Uri imageUri = data.getData();
//                    final InputStream imageStream = getContentResolver().openInputStream(imageUri);
//                    final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);

                    if (imageUri !=null){
                        mBitmap=decodeUri(imageUri,400);
                    }

                    ivPreview.setImageBitmap(mBitmap);

                } catch (Exception e) {
                    showToast("Caught Error: Could not set Photo to Image from Gallery",true);
                    return;
                }
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

    //Convert bitmap to bytes
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
    private byte[] convertImageToByte(Bitmap b){

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        b.compress(Bitmap.CompressFormat.PNG, 0, bos);
        return bos.toByteArray();

    }

    private void goToGpsSurveyActivity(){
        Intent intent = new Intent(this, GpsSurveyActivity.class);
        startActivityForResult(intent,ACTIVITY_KEY);

    }

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
            case 7:
                if (mBitmap == null){
                    results = false;
                }else{
                    results = true;
                }
        }

        return results;
    }

    private void getValues(){

        //1
        mProjectName = etProjectName.getText().toString();

        //2
        int storage_pos = spStorage.getSelectedItemPosition();
        String [] storage_values = getResources().getStringArray(R.array.project_storage_values);
        mStorage = Integer.valueOf(storage_values[storage_pos]);

        //3
        int units_pos = spUnits.getSelectedItemPosition();
        String [] units_values = getResources().getStringArray(R.array.unit_measure_values);
        mUnits = Integer.valueOf(units_values[units_pos]);

        //4
        int projection_pos = spProjection.getSelectedItemPosition();
        String [] projection_values = getResources().getStringArray(R.array.project_projection_values);
        mProjection = Integer.valueOf(projection_values[projection_pos]);

        //5
        int zone_pos = spZone.getSelectedItemPosition();
        String [] zone_values = getResources().getStringArray(R.array.project_projection_zones_values);
        mZone = Integer.valueOf(zone_values[zone_pos]);

        //6
        if (mImageSystem == 1){
            mImage = convertImageToByte(mBitmap);
        }else{
            //convert system bitmap for project to bitmap/byte
            Bitmap icon = BitmapFactory.decodeResource(getResources(), R.drawable.ic_project_icon_32);
            mImage = convertImageToByte(icon);
        }

        //7
        //GPS Values already exist from
        //mLocationLat & mLocationLong

    }


    private void showToast(String data, boolean shortTime) {

        if (shortTime) {
            Toast.makeText(this, data, Toast.LENGTH_SHORT).show();

        } else{
            Toast.makeText(this, data, Toast.LENGTH_LONG).show();

        }

    }


}
