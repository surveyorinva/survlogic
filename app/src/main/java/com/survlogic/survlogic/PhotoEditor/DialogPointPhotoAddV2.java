package com.survlogic.survlogic.PhotoEditor;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.Toast;

import com.github.chrisbanes.photoview.PhotoView;
import com.survlogic.survlogic.PhotoEditor.interf.DialogPointPhotoAddV2Listener;
import com.survlogic.survlogic.R;
import com.survlogic.survlogic.utils.FileHelper;
import com.survlogic.survlogic.PhotoEditor.utils.ImageHelper;

import mehdi.sakout.fancybuttons.FancyButton;

import static com.survlogic.survlogic.utils.StringUtilityHelper.isStringNull;

public class DialogPointPhotoAddV2 extends DialogFragment {

    private static final String TAG = "DialogProjectPhotoAdd";
    private Context mContext;
    private ImageHelper imageHelper;
    private FileHelper fileHelper;

    //In Variables
    private Bitmap mImageRaw, mImageDefault, mImageProcessed;
    private Location mPhotoLocation;
    private int mPhotoAzimuth;


    private DialogPointPhotoAddV2Listener listener;

    //Method Helpers
    private boolean hasMetaDataIn = false, hasGPSLocation, hasAzimuth;
    private boolean isContentMenuShown = true, isHiddenMenuShown = false;
    private boolean mAddWatermark = false, mAddCompass = false;

    //Widgets
    private RelativeLayout rlContentView, rlHiddenView, rlHiddenViewLocation, rlHiddenViewAzimuth;

    private EditText etDescription;
    private Switch swLocation, swCompass;
    private PhotoView ivPhoto;

    private FancyButton fbAdd, fbClear;
    private ImageButton ibExpandHidden, ibExpandContent;
    private Button btFinish, btCancel;

    private ProgressBar progressImage;


    public static DialogPointPhotoAddV2 newInstance(DialogPointPhotoAddV2Listener listener) {

        DialogPointPhotoAddV2 frag = new DialogPointPhotoAddV2();
        frag.listener = listener;

        return frag;

    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Log.d(TAG, "onCreateDialog: Started");

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(),R.style.DialogPopupStyle);
        LayoutInflater inflater = getActivity().getLayoutInflater();

        View v = inflater.inflate(R.layout.dialog_photo_editor_point,null);
        builder.setView(v);

        builder.create();
        return builder.show();


    }

    @Override
    public void onResume() {
        super.onResume();
        mContext = getActivity();
        imageHelper = new ImageHelper(mContext);
        fileHelper = new FileHelper(mContext);

        initViewWidget();
        setOnClickListeners();

        populateVariables();

        showHideMetadataOptions();

        watermarkSetPointNumber();

    }

    private void initViewWidget(){
        Log.d(TAG, "initView: Starting Top View Add");

        rlContentView = getDialog().findViewById(R.id.rl_footer_content);
        rlHiddenView = getDialog().findViewById(R.id.rl_footer_hidden);

        rlHiddenViewLocation = getDialog().findViewById(R.id.rl_footer_hidden_location);
        rlHiddenViewAzimuth = getDialog().findViewById(R.id.rl_footer_hidden_compass);

        ivPhoto = getDialog().findViewById(R.id.picture);
        etDescription = getDialog().findViewById(R.id.photo_description);

        swLocation = getDialog().findViewById(R.id.switch_location_value);
        swCompass = getDialog().findViewById(R.id.switch_sensor_value);

        btFinish = getDialog().findViewById(R.id.button_save);
        btCancel = getDialog().findViewById(R.id.button_cancel);

        fbAdd = getDialog().findViewById(R.id.btn_add_watermark);
        fbClear = getDialog().findViewById(R.id.btn_clear_watermark);

        ibExpandContent = getDialog().findViewById(R.id.card_expand_content);
        ibExpandHidden = getDialog().findViewById(R.id.card_expand_hidden);

        progressImage = getDialog().findViewById(R.id.picture_progress);

    }

    private void setOnClickListeners(){
        Log.d(TAG, "setOnClickListeners: Started");

        ibExpandContent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showHideMenuContent();
            }
        });

        ibExpandHidden.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showHideMenuOptions();
            }
        });

        fbAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                watermarkSetDescription();
            }
        });

        fbClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                android.support.v7.app.AlertDialog dialog = checkToClearWatermarkDialog();
                dialog.show();
            }
        });


        swCompass.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if(!isChecked){
                    mAddCompass = false;

                }else{
                    mAddCompass = true;
                    watermarkSetCompass();
                }
            }
        });

    }

    private void populateVariables(){
        mImageRaw = listener.getPhoto();

        hasGPSLocation = listener.isUsePhotoLocation();
        hasAzimuth = listener.isUsePhotoSensor();

        if(hasGPSLocation){
            mPhotoLocation = listener.getPhotoLocation();
        }

        if (hasAzimuth){
            mPhotoAzimuth = listener.getPhotoAzimuth();

        }

    }

    //---------------------------------------------------------------------------------------------- View Controls

    private void showHideMetadataOptions(){

        if(hasGPSLocation || hasAzimuth){
            hasMetaDataIn = true;
            ibExpandHidden.setVisibility(View.VISIBLE);
        }

        if(!hasGPSLocation){
            rlHiddenViewLocation.setVisibility(View.GONE);
        }

        if(!hasAzimuth){
           rlHiddenViewAzimuth.setVisibility(View.GONE);
        }


    }


    private void showHideMenuContent(){
        if(!isContentMenuShown){
            rlContentView.setVisibility(View.VISIBLE);
            isContentMenuShown = true;

        }else{
            if(isHiddenMenuShown){
                rlHiddenView.setVisibility(View.GONE);
                isHiddenMenuShown = false;
            }

            rlContentView.setVisibility(View.GONE);

            isContentMenuShown = false;
        }
    }


    private void showHideMenuOptions(){
        if(!isHiddenMenuShown){

            rlHiddenView.setVisibility(View.VISIBLE);
            isHiddenMenuShown = true;

        }else{
            rlHiddenView.setVisibility(View.GONE);
            isHiddenMenuShown = false;
        }

    }

    //---------------------------------------------------------------------------------------------- Watermarks

    private void watermarkSetPointNumber(){

        progressImage.setVisibility(View.VISIBLE);

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
            String pointNo = String.valueOf(listener.getPointNumber());
            mImageDefault = imageHelper.setWatermarkAtTop(mImageRaw, pointNo, true);
            ivPhoto.setImageBitmap(mImageDefault);

            progressImage.setVisibility(View.GONE);

            }
        }, 400);

    }

    private void watermarkSetDescription(){
        progressImage.setVisibility(View.VISIBLE);

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                String description = etDescription.getText().toString();
                if(!isStringNull(description)) {
                    mAddWatermark = true;
                    mImageProcessed = imageHelper.setWatermarkAtBottom(mImageDefault, description, true);
                    ivPhoto.setImageBitmap(mImageProcessed);
                }

                progressImage.setVisibility(View.GONE);
            }
        },400);

    }

    private void watermarkSetCompass(){
        progressImage.setVisibility(View.VISIBLE);

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {

                mImageProcessed = imageHelper.setWatermarkCompass(mImageDefault,mPhotoAzimuth,true);
                ivPhoto.setImageBitmap(mImageProcessed);
                progressImage.setVisibility(View.GONE);
            }
        },400);

    }

    private void watermarkClearMetaData(){
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                etDescription.setText("");
                mAddWatermark = false;
                ivPhoto.setImageBitmap(mImageDefault);
            }
        },400);

    }


    //---------------------------------------------------------------------------------------------- Modal Prompts
    private android.support.v7.app.AlertDialog checkToClearWatermarkDialog(){

        android.support.v7.app.AlertDialog myDialogBox = new android.support.v7.app.AlertDialog.Builder(mContext)
                .setMessage(getResources().getString(R.string.dialog_photo_editor_clear_watermark))
                .setPositiveButton(getResources().getString(R.string.general_yes), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        watermarkClearMetaData();
                        dialog.dismiss();
                    }
                })
                .setNegativeButton(getResources().getString(R.string.general_no), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();

                    }
                })

                .create();

        return myDialogBox;
    }

    private void showToast(String data, boolean shortTime) {

        if (shortTime) {
            Toast.makeText(mContext, data, Toast.LENGTH_SHORT).show();

        } else{
            Toast.makeText(mContext, data, Toast.LENGTH_LONG).show();

        }
    }

}
