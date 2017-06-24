package com.survlogic.survlogic.activity;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
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
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.survlogic.survlogic.BuildConfig;
import com.survlogic.survlogic.R;
import com.survlogic.survlogic.utils.MathHelper;

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

    private TextView mlocation_latitude, mlocation_longitude;
    private ImageView ivPreview;

    private Button btnPhoto_Camera_Get_Photo, btnCancel;
    private ImageButton btnlocation_get_from_gps_survey;

    private String mCurrentPhotoPath;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_project_new);

        Log.e(TAG, "Started onCreateView");

        initView();


    }


    private void initView(){
        mlocation_latitude = (TextView) findViewById(R.id.location_latitude);
        mlocation_longitude = (TextView) findViewById(R.id.location_longitude);

        ivPreview = (ImageView) findViewById(R.id.photo_camera_image);


        btnlocation_get_from_gps_survey = (ImageButton) findViewById(R.id.location_get_from_gps_survey);
        btnPhoto_Camera_Get_Photo = (Button) findViewById(R.id.photo_camera_get_photo);

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
            showToast("Caught Error: Accessing Camera Exception");
        }
    }

    private void startPhotoGallery(){

        try{
            dispatchPhotoFromGalleryIntent();

        } catch (IOException e){
            showToast("caught Error: Accessing Gallery Exception");
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
                showToast("Caught Error: Could not create file");
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

                double mlatitude = data.getDoubleExtra(getString(R.string.KEY_POSITION_LATITUDE),0);
                double mlongitude = data.getDoubleExtra(getString(R.string.KEY_POSITION_LONGITUDE),0);

                String strLatitude = MathHelper.convertDECtoDMS(mlatitude,3,false);
                mlocation_latitude.setText(getString(R.string.project_new_location_latitude_value,strLatitude));

                String strLongitude = MathHelper.convertDECtoDMS(mlongitude,3,true);
                mlocation_longitude.setText(getString(R.string.project_new_location_longitude_value,strLongitude));

            }
        }else if(this.REQUEST_TAKE_PHOTO == requestCode && resultCode == RESULT_OK){
            Uri imageUri = Uri.parse(mCurrentPhotoPath);
            File file = new File(imageUri.getPath());
            try {
                InputStream ims = new FileInputStream(file);
                ivPreview.setImageBitmap(BitmapFactory.decodeStream(ims));

            } catch (FileNotFoundException e) {
                showToast("Caught Error: Could not set Photo to Image from Camera");
                return;
            }

        }else if(this.REQUEST_SELECT_PICTURE == requestCode && resultCode == RESULT_OK){
            if (data != null) {


                try {
                    final Uri imageUri = data.getData();
                    final InputStream imageStream = getContentResolver().openInputStream(imageUri);
                    final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                    ivPreview.setImageBitmap(selectedImage);

                } catch (Exception e) {
                    showToast("Caught Error: Could not set Photo to Image from Gallery");
                    return;
                }
            }
        }
    }

    private void goToGpsSurveyActivity(){
        Intent intent = new Intent(this, GpsSurveyActivity.class);
        startActivityForResult(intent,ACTIVITY_KEY);

    }

    private void showToast(String data){

        Toast.makeText(this, data, Toast.LENGTH_SHORT).show();

    }


}
