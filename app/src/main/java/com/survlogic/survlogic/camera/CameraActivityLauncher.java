package com.survlogic.survlogic.camera;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.survlogic.survlogic.R;
import com.survlogic.survlogic.camera.util.BitmapUtils;

import java.io.File;

public class CameraActivityLauncher extends AppCompatActivity{

    private static final String TAG = "CameraActivityLauncher";
    private Context mContext;

    private BitmapUtils bitmapUtils;
    private static final int REQUEST_TAKE_PHOTO = 2;

    private RelativeLayout rlImagePreview;
    private Button btCameraLauncher;
    private ImageView ivPhotoResults;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.camera_activity_launcher);

        mContext = this;
        bitmapUtils = new BitmapUtils(mContext);

        initViewWidgets();
        initOnClickListener();


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (REQUEST_TAKE_PHOTO == requestCode && resultCode == Activity.RESULT_OK) {
            Log.d(TAG, "onActivityResult: Success!");

            File file = (File) data.getExtras().get(getString(R.string.KEY_IMAGE_FILE));
            Uri imageUri = Uri.fromFile(file);

            try {
                Bitmap mBitmapRaw=bitmapUtils.decodeUri(imageUri,400);
                Bitmap mBitmap = bitmapUtils.rotateImageIfRequired(mBitmapRaw,imageUri);

                ivPhotoResults.setImageBitmap(mBitmap);
                rlImagePreview.setVisibility(View.VISIBLE);

            } catch (Exception e) {
                Log.e(TAG, "onActivityResult: Caught Error:  Could not set File to Image from Camera");

            }

        }
        
        
        
        
    }

    //-----------------------------------------------------------------------------------------------//

    private void initViewWidgets(){
        Log.d(TAG, "initViewWidgets: Stated");

        rlImagePreview = findViewById(R.id.rl_container_body);

        btCameraLauncher = findViewById(R.id.image_capture);
        ivPhotoResults = findViewById(R.id.preview_photo_results);

    }

    private void initOnClickListener(){
        Log.d(TAG, "initOnClickListener: Started");

        Intent intent = null;

        btCameraLauncher.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                rlImagePreview.setVisibility(View.GONE);

                Intent takePictureIntent = new Intent(CameraActivityLauncher.this,CaptureImageActivity.class);
                startActivityForResult(takePictureIntent,REQUEST_TAKE_PHOTO);
            }
        });

    }
}
