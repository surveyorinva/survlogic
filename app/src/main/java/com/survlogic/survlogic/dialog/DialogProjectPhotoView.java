package com.survlogic.survlogic.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

import com.survlogic.survlogic.R;
import com.survlogic.survlogic.utils.UniversalImageLoader;

/**
 * Created by chrisfillmore on 7/19/2017.
 */

public class DialogProjectPhotoView extends DialogFragment {

    private static final String TAG = "DialogProjectPhotoView";
    private Context mContext;

    private ImageView ivPhoto;
    private String mImagePath, mURLSyntex ;
    private int project_id;


    public static DialogProjectPhotoView newInstance(int mProjectId, String urlSyntex, String mImagePath) {
        DialogProjectPhotoView frag = new DialogProjectPhotoView();
        Bundle args = new Bundle();

        args.putInt("project_id", mProjectId);
        args.putString("URLSyntex", urlSyntex);
        args.putString("imagePath", mImagePath);

        frag.setArguments(args);
        return frag;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Log.d(TAG, "onCreateDialog: Creating Dialog");
        project_id = getArguments().getInt("project_id");
        mURLSyntex = getArguments().getString("URLSyntex");
        mImagePath = getArguments().getString("imagePath");

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.DialogGalleryStyle);
        LayoutInflater inflater = getActivity().getLayoutInflater();

        //View v = inflater.inflate(R.layout.dialog_gallery_photo_single,null);
        View v = inflater.inflate(R.layout.dialog_gallery_photo_single_zoomable,null);

        builder.setView(v);

        builder.create();
        return builder.show();


    }

    @Override
    public void onResume() {
        super.onResume();

        Log.d(TAG, "onResume: Creating Image");
        mContext = getActivity();
        AlertDialog alertDialog = (AlertDialog) getDialog();

        ivPhoto = (ImageView) getDialog().findViewById(R.id.photo_in_dialog_project_picture);

        setImage();
    }




    private void setImage(){
        UniversalImageLoader.setImage(mImagePath,ivPhoto,null, mURLSyntex);
    }

}
