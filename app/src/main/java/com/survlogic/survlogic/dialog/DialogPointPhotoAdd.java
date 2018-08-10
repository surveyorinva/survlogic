package com.survlogic.survlogic.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import com.survlogic.survlogic.R;
import com.survlogic.survlogic.background.BackgroundProjectImagesSetup;
import com.survlogic.survlogic.model.ProjectImages;
import com.survlogic.survlogic.utils.FileHelper;
import com.survlogic.survlogic.PhotoEditor.utils.ImageHelper;

/**
 * Created by chrisfillmore on 7/12/2017.
 */

public class DialogPointPhotoAdd extends DialogFragment {

    private static final String TAG = "DialogProjectPhotoAdd";
    private Context mContext;
    private ImageHelper imageHelper;
    private FileHelper fileHelper;

    private Bitmap mImageLocal, mImageWatermark;
    private String mCurrentPhotoPath;

    private int project_id, job_id, point_id, point_no;

    private boolean mAddWatermark = false;

    private EditText etDescription;
    private Button btnAddWatermark, btnRemoveWatermark;

    public static DialogPointPhotoAdd newInstance(int mProjectId, int mJobId, int mPointId, int mPointNo, Bitmap mBitmap) {

        DialogPointPhotoAdd frag = new DialogPointPhotoAdd();
        Bundle args = new Bundle();

        args.putInt("project_id", mProjectId);
        args.putInt("job_id", mJobId);
        args.putInt("point_id", mPointId);
        args.putInt("point_no", mPointNo);
        args.putParcelable("image", mBitmap);

        frag.setArguments(args);
        return frag;

    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        project_id = getArguments().getInt("project_id");
        job_id = getArguments().getInt("job_id");
        point_id = getArguments().getInt("point_id");
        point_no = getArguments().getInt("point_no");
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
        Log.d(TAG, "initView: Starting Top View Add");

        String pointNo = String.valueOf(point_no);

        mImageLocal = imageHelper.setWatermarkAtTop(mImageLocal, pointNo, true);
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

                    mImageWatermark = imageHelper.setWatermarkAtBottom(mImageLocal, description, true);

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

        String mImagePath;

            if (mAddWatermark){
                Uri uri = fileHelper.saveImageToExternal(mImageWatermark);
                mImagePath = uriToString(uri);

            }else{
                Uri uri = fileHelper.saveImageToExternal(mImageLocal);
                mImagePath = uriToString(uri);

            }

            // Create Project model
        ProjectImages projectImages = new ProjectImages(project_id,job_id,point_id,mImagePath,0,0,0);

            // Setup Background Task
        BackgroundProjectImagesSetup backgroundProjectImagesSetup = new BackgroundProjectImagesSetup(getActivity());

            // Execute background task
        backgroundProjectImagesSetup.execute(projectImages);
        Log.d(TAG, "submitForm: Complete.  Photo with ProjectID: " + project_id + " Saved");


        //Todo refresh gridview for DialogJobPointView

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
