package com.survlogic.survlogic.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

import com.github.chrisbanes.photoview.PhotoView;
import com.survlogic.survlogic.R;
import com.survlogic.survlogic.utils.UniversalImageLoader;

/**
 * Created by chrisfillmore on 7/19/2017.
 */

public class DialogJobPointPhotoView extends DialogFragment {

    private static final String TAG = "DialogProjectPhotoView";
    private Context mContext;

    private PhotoView ivPhoto;
    private String mImagePath, mURLSyntex ;
    private int project_id;


    public static DialogJobPointPhotoView newInstance(int mProjectId, int mJobId, int mPointId, String urlSyntex, String mImagePath) {
        DialogJobPointPhotoView frag = new DialogJobPointPhotoView();
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

        ivPhoto = (PhotoView) getDialog().findViewById(R.id.photo_in_dialog_project_picture);

        setImage();
    }




    private void setImage(){
        UniversalImageLoader.setImage(mImagePath,ivPhoto,null, mURLSyntex);
    }

}
