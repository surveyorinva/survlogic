package com.survlogic.survlogic.PhotoEditor;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.location.Location;
import android.net.Uri;
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
import com.survlogic.survlogic.PhotoEditor.background.BackgroundProjectImagesSave;
import com.survlogic.survlogic.PhotoEditor.interf.DialogPointPhotoAddV2Listener;
import com.survlogic.survlogic.R;
import com.survlogic.survlogic.activity.ProjectDetailsActivity;
import com.survlogic.survlogic.background.BackgroundProjectImagesSetup;
import com.survlogic.survlogic.model.ProjectImages;
import com.survlogic.survlogic.utils.FileHelper;
import com.survlogic.survlogic.PhotoEditor.utils.ImageHelper;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import mehdi.sakout.fancybuttons.FancyButton;

import static com.survlogic.survlogic.utils.StringUtilityHelper.isStringNull;

public class DialogPointPhotoAddV2 extends DialogFragment {

    private static final String TAG = "DialogProjectPhotoAdd";
    private Context mContext;
    private ImageHelper imageHelper;
    private FileHelper fileHelper;

    //In Variables
    private Bitmap mImageRaw;
    private Location mPhotoLocation;
    private int mPhotoAzimuth;


    private DialogPointPhotoAddV2Listener listener;

    //Method Helpers
    private String mDescription;

    private boolean hasMetaDataIn = false, hasGPSLocation = false, hasAzimuth = false;
    private boolean isContentMenuShown = true, isHiddenMenuShown = false;
    private boolean isWatermarkDescOn = false, isWatermarkCompassOn = false, isWatermarkLocationOn = false;
    private boolean isWatermarkTimestampOn = true;
    private boolean isKeepRawFile = true;

    private Bitmap mImageDefault, mImageProcessed;
    private ArrayList<Bitmap> mImageArray = new ArrayList<>();
    private HashMap<Integer,Bitmap> mImageMap = new HashMap<>();

    private static final Integer KEY_RAW = 0, KEY_POINT_NO = 1, KEY_DESC = 2, KEY_AZIMUTH = 3, KEY_LOCATION = 4, KEY_BACKGROUND = 5;
    private final long DELAY_SHORT = 250, DELAY_MEDIUM = 500, DELAY_LONG = 800;
    //Widgets
    private RelativeLayout rlContentView, rlHiddenView, rlHiddenViewLocation, rlHiddenViewAzimuth;

    private EditText etDescription;
    private Switch swLocation, swCompass, swTimestamp, swRawFile;
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

        autoRunWatermarks();

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
        swTimestamp = getDialog().findViewById(R.id.switch_timestamp_value);
        swRawFile = getDialog().findViewById(R.id.switch_raw_value);

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
                isWatermarkDescOn = true;
                watermarkSetDescription(500);
            }
        });

        fbClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                android.support.v7.app.AlertDialog dialog = checkToClearWatermarkDialog();
                dialog.show();
            }
        });


        swLocation.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if(!isChecked){
                    isWatermarkLocationOn = false;
                    watermarkSetLocationPhoto(DELAY_MEDIUM);
                }else{
                    isWatermarkLocationOn = true;
                    watermarkSetLocationPhoto(DELAY_MEDIUM);
                }
            }
        });

        swCompass.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if(!isChecked){
                    isWatermarkCompassOn = false;
                    watermarkSetCompass(DELAY_MEDIUM);
                }else{
                    isWatermarkCompassOn = true;
                    watermarkSetCompass(DELAY_MEDIUM);
                }
            }
        });

        swTimestamp.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if(!isChecked){
                    isWatermarkTimestampOn = false;
                    watermarkSetTimestamp(DELAY_MEDIUM);
                }else{
                    isWatermarkTimestampOn = true;
                    watermarkSetTimestamp(DELAY_MEDIUM);
                }
            }
        });

        btFinish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                submitForm();
            }
        });

        btCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
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

    private void autoRunWatermarks(){
        watermarkSetPointNumber(DELAY_SHORT);

        watermarkSetTimestamp(DELAY_LONG);

    }


    private void watermarkSetPointNumber(long delayTime){

        progressImage.setVisibility(View.VISIBLE);

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
            String pointNo = String.valueOf(listener.getPointNumber());
            mImageDefault = imageHelper.setWatermarkAtTop(mImageRaw, pointNo, true);
            mImageProcessed = mImageDefault;

            ivPhoto.setImageBitmap(mImageDefault);

            mImageArray.add(mImageDefault);
            mImageMap.put(KEY_POINT_NO,mImageDefault);

            progressImage.setVisibility(View.GONE);

            }
        }, delayTime);

    }

    private void watermarkSetDescription(long delayTime){
        progressImage.setVisibility(View.VISIBLE);

        final Bitmap mBitmapIn = mImageArray.get(mImageArray.size() - 1);
        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                mDescription = etDescription.getText().toString();

                if(!isStringNull(mDescription)) {
                    generateWatermarks();

                    ivPhoto.setImageBitmap(mImageProcessed);
                }

                mImageArray.add(mImageProcessed);
                mImageMap.put(KEY_DESC,mImageProcessed);

                progressImage.setVisibility(View.GONE);
            }
        },delayTime);

    }

    private void watermarkSetCompass(long delayTime){
        progressImage.setVisibility(View.VISIBLE);

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                generateWatermarks();

                ivPhoto.setImageBitmap(mImageProcessed);

                mImageArray.add(mImageProcessed);
                mImageMap.put(KEY_AZIMUTH,mImageProcessed);

                progressImage.setVisibility(View.GONE);

            }
        },delayTime);

    }

    private void watermarkSetLocationPhoto(long delayTime){
        progressImage.setVisibility(View.VISIBLE);

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                generateWatermarks();

                ivPhoto.setImageBitmap(mImageProcessed);

                mImageArray.add(mImageProcessed);
                mImageMap.put(KEY_LOCATION,mImageProcessed);

                progressImage.setVisibility(View.GONE);

            }
        },delayTime);

    }

    private void watermarkSetTimestamp(long delayTime){
        progressImage.setVisibility(View.VISIBLE);

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                generateWatermarks();

                ivPhoto.setImageBitmap(mImageProcessed);

                mImageArray.add(mImageProcessed);
                mImageMap.put(KEY_LOCATION,mImageProcessed);

                progressImage.setVisibility(View.GONE);

            }
        },delayTime);
    }

    private void watermarkClearMetaData(){
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                etDescription.setText("");
                mDescription = "";

                isWatermarkDescOn = false;
                generateWatermarks();

                ivPhoto.setImageBitmap(mImageProcessed);
            }
        },400);

    }


    private void generateWatermarks(){

        Bitmap bitmap;

        if(isWatermarkDescOn && isWatermarkCompassOn){
            imageHelper.setWatermarkDesc(mDescription);

            bitmap = imageHelper.setWatermarkBackgroundAtBottom(mImageDefault,imageHelper.TYPE_COMBINED);
            bitmap = imageHelper.setWatermarkDescription(bitmap, false);
            bitmap = imageHelper.setWatermarkCompass(bitmap,mPhotoAzimuth,false);

        }else if(isWatermarkDescOn){
            imageHelper.setWatermarkDesc(mDescription);

            bitmap = imageHelper.setWatermarkBackgroundAtBottom(mImageDefault,imageHelper.TYPE_DESC_ONLY);
            bitmap = imageHelper.setWatermarkDescription(bitmap, false);

        }else{
            bitmap = mImageDefault;
        }

        if(isWatermarkCompassOn) {
            bitmap = imageHelper.setWatermarkBackgroundAtBottom(bitmap, imageHelper.TYPE_COMPASS_ONLY);

            bitmap = imageHelper.setWatermarkCompass(bitmap, mPhotoAzimuth, false);
        }


        if(isWatermarkLocationOn){
            imageHelper.setPhotoLocation(mPhotoLocation);

            bitmap = imageHelper.setWatermarkLocationUnderPoint(bitmap);

        }

        if(isWatermarkTimestampOn){
            bitmap = imageHelper.setWatermarkTimestamp(bitmap);
        }


        mImageProcessed = bitmap;


    }

    //---------------------------------------------------------------------------------------------- Photo Out
    private void submitForm() {

        String mImagePath;

        Uri uri = fileHelper.saveImageToExternal(mImageProcessed,fileHelper.FOLDER_JOB_PHOTOS);
        mImagePath = uriToString(uri);

        // Create Project model
        int project_id = listener.getProjectId();
        int job_id = listener.getJobId();
        int point_id = listener.getPointId();

        ProjectImages projectImage = new ProjectImages(project_id,job_id,point_id,mImagePath,0,0,0);


        if(hasGPSLocation){
            projectImage.setLocationLat(mPhotoLocation.getLatitude());
            projectImage.setLocationLong(mPhotoLocation.getLongitude());
        }

        if(hasAzimuth){
            projectImage.setBearingAngle(mPhotoAzimuth);

        }

        // Setup Background Task
        BackgroundProjectImagesSave backgroundProjectImagesSave = new BackgroundProjectImagesSave(getActivity());

        // Execute background task
        backgroundProjectImagesSave.execute(projectImage);
        Log.d(TAG, "submitForm: Complete.  Photo with ProjectID: " + project_id + " Saved");


        if(!isKeepRawFile){
            boolean isDeleteSuccess = deleteRawFile();
        }

        listener.requestPhotoRefresh();

        getDialog().dismiss();


    }

    private String uriToString(Uri uri){
        return uri.toString();
    }

    private Uri stringToUri(String stringUri){
        return Uri.parse(stringUri);
    }

    private boolean deleteRawFile(){
        boolean results = false;

        try{
            File rawFile = listener.getPhotoFile();
            results = rawFile.delete();

        }catch (Exception e){

        }

        return results;
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

    private android.support.v7.app.AlertDialog checkToDeleteRawFileDialog(){

        android.support.v7.app.AlertDialog myDialogBox = new android.support.v7.app.AlertDialog.Builder(mContext)
                .setMessage(getResources().getString(R.string.dialog_photo_editor_delete_raw_file))
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
