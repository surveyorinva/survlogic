package com.survlogic.survlogic.view;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.survlogic.survlogic.R;
import com.survlogic.survlogic.activity.ProjectDetailsActivity;
import com.survlogic.survlogic.background.BackgroundProjectImagesSetup;
import com.survlogic.survlogic.model.ProjectImages;
import com.survlogic.survlogic.utils.FileHelper;
import com.survlogic.survlogic.utils.ImageHelper;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by chrisfillmore on 7/12/2017.
 */

public class DialogProjectPhotoAdd extends DialogFragment {

    private static final String TAG = "DialogProjectPhotoAdd";
    private Context mContext;
    private ImageHelper imageHelper;
    private FileHelper fileHelper;

    private Bitmap mImageLocal, mImageWatermark;
    private String mCurrentPhotoPath;

    private int project_id;

    private boolean mAddWatermark = false;

    private EditText etDescription;
    private Button btnAddWatermark, btnRemoveWatermark;

    public static DialogProjectPhotoAdd newInstance(Integer mProjectId, Bitmap mBitmap) {

        DialogProjectPhotoAdd frag = new DialogProjectPhotoAdd();
        Bundle args = new Bundle();

        args.putInt("project_id", mProjectId);
        args.putParcelable("image", mBitmap);

        frag.setArguments(args);
        return frag;

    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        project_id = getArguments().getInt("project_id");
        mImageLocal = getArguments().getParcelable("image");

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(),R.style.DialogPopupStyle);
        LayoutInflater inflater = getActivity().getLayoutInflater();

        View v = inflater.inflate(R.layout.dialog_project_picture,null);
        builder.setView(v);

        builder.setPositiveButton(R.string.general_save,null);

        builder.setNegativeButton(R.string.general_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.create();
        return builder.show();


    }

    @Override
    public void onResume() {
        super.onResume();


        mContext = getActivity();
        imageHelper = new ImageHelper(mContext);
        fileHelper = new FileHelper(mContext);

        initView();




    }

    private void initView(){

        AlertDialog alertDialog = (AlertDialog) getDialog();
        etDescription = (EditText) getDialog().findViewById(R.id.photo_description);

        final ImageView ivPhoto = (ImageView) getDialog().findViewById(R.id.photo_in_dialog_project_picture);
        ivPhoto.setImageBitmap(mImageLocal);

        Button btnSave = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitForm(v);
            }
        });

        btnAddWatermark = (Button) getDialog().findViewById(R.id.btn_add_watermark);
        btnAddWatermark.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String description = etDescription.getText().toString();
                if(!isStringNull(description)){
                    mAddWatermark = true;

                    mImageWatermark = imageHelper.setWatermark(mImageLocal, description, true);

                    ivPhoto.setImageBitmap(mImageWatermark);

                    Log.d(TAG, "btnAddWatermark: Added Watermark ");
                }

            }
        });

        btnRemoveWatermark = (Button) getDialog().findViewById(R.id.btn_revert_watermark);
        btnRemoveWatermark.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                etDescription.setText("");
                mAddWatermark = false;
                ivPhoto.setImageBitmap(mImageLocal);
                Log.d(TAG, "btnAddWatermark: Cleared Watermark  ");
            }
        });


    }
    private void submitForm(View v) {

        byte[] mImage;
        String mImagePath;

            if (mAddWatermark){
                Uri uri = fileHelper.saveImageToExternal(mImageWatermark);
                mImagePath = uriToString(uri);

                mImage = imageHelper.convertImageToByte(mImageWatermark);
            }else{
                Uri uri = fileHelper.saveImageToExternal(mImageWatermark);
                mImagePath = uriToString(uri);

                mImage = imageHelper.convertImageToByte(mImageLocal);
            }

            // Create Project model
        //ProjectImages projectImages = new ProjectImages(project_id,0,mImage,0,0,0);
        ProjectImages projectImages = new ProjectImages(project_id,0,mImagePath,0,0,0);

            // Setup Background Task
        BackgroundProjectImagesSetup backgroundProjectImagesSetup = new BackgroundProjectImagesSetup(getActivity());

            // Execute background task
        backgroundProjectImagesSetup.execute(projectImages);
        Log.d(TAG, "submitForm: Complete.  Photo with ProjectID: " + project_id + " Saved");

        ((ProjectDetailsActivity) getActivity()).showProjectDetailsDialogRefresh();

        getDialog().dismiss();


    }



    private boolean isStringNull(String string){
        Log.d(TAG, "isStringNull: checking string if null.");

        if(string.equals("")){
            return true;
        }
        else{
            return false;
        }
    }

    private String uriToString(Uri uri){
        return uri.toString();
    }

    private Uri stringToUri(String stringUri){
        return Uri.parse(stringUri);
    }


}
