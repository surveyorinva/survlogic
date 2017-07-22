package com.survlogic.survlogic.view;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import com.survlogic.survlogic.R;

/**
 * Created by chrisfillmore on 7/19/2017.
 */

public class DialogProjectPhotoView extends DialogFragment {

    private Context mContext;

    private Bitmap mImageLocal;
    private int project_id;



    public static DialogProjectPhotoView newInstance(int mProjectId, Bitmap mBitmap) {
        DialogProjectPhotoView frag = new DialogProjectPhotoView();
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

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.DialogGalleryStyle);
        LayoutInflater inflater = getActivity().getLayoutInflater();

        View v = inflater.inflate(R.layout.dialog_gallery_photo_single,null);
        builder.setView(v);

        builder.create();
        return builder.show();


    }

    @Override
    public void onResume() {
        super.onResume();


        mContext = getActivity();
        AlertDialog alertDialog = (AlertDialog) getDialog();

        final ImageView ivPhoto = (ImageView) getDialog().findViewById(R.id.photo_in_dialog_project_picture);
        ivPhoto.setImageBitmap(mImageLocal);

    }


}
