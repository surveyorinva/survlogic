package com.survlogic.survlogic.view;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.survlogic.survlogic.R;
import com.survlogic.survlogic.background.BackgroundProjectImagesSetup;
import com.survlogic.survlogic.model.ProjectImages;

import java.io.ByteArrayOutputStream;

/**
 * Created by chrisfillmore on 7/12/2017.
 */

public class DialogProjectPhotoAdd extends DialogFragment {

    private static final String TAG = "DialogProjectPhotoAdd";

    private Bitmap mImageLocal;
    private int project_id;
    private String photoDescription;
    private byte[] mImage;

    private EditText etDescription;

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

        //builder.setTitle(title);


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

        AlertDialog alertDialog = (AlertDialog) getDialog();
        Button btnSave = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);

        ImageView ivPhoto = (ImageView) getDialog().findViewById(R.id.photo_in_dialog_project_picture);

        ivPhoto.setImageBitmap(mImageLocal);


        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitForm(v);
            }
        });
    }

    private void submitForm(View v) {
        boolean result = false;

        etDescription = (EditText) getDialog().findViewById(R.id.photo_description);

        // Validation
                //none at this time
            result = true;

        if (result){
            // Save results
            getValues();

            // Create Project model
            ProjectImages projectImages = new ProjectImages(project_id,0,mImage,0,0,0);

            // Setup Background Task
            BackgroundProjectImagesSetup backgroundProjectImagesSetup = new BackgroundProjectImagesSetup(getActivity());

            // Execute background task
            backgroundProjectImagesSetup.execute(projectImages);
            getDialog().dismiss();
        }


    }


    //Convert bitmap to bytes
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
    private static byte[] convertImageToByte(Bitmap b){

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        b.compress(Bitmap.CompressFormat.PNG, 0, bos);
        return bos.toByteArray();

    }

    private Bitmap convertToBitmap(byte[] b){

        return BitmapFactory.decodeByteArray(b, 0, b.length);

    }

    private void getValues(){
        photoDescription = etDescription.getText().toString();

        mImage = convertImageToByte(mImageLocal);




    }

}
