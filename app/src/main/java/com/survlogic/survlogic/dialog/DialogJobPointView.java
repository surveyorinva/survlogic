package com.survlogic.survlogic.dialog;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v4.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.survlogic.survlogic.BuildConfig;
import com.survlogic.survlogic.R;
import com.survlogic.survlogic.activity.JobPointAddSketchActivity;
import com.survlogic.survlogic.activity.PhotoGalleryActivity;
import com.survlogic.survlogic.adapter.PointGridImageAdapter;
import com.survlogic.survlogic.adapter.PointGridSketchAdapter;
import com.survlogic.survlogic.database.JobDatabaseHandler;
import com.survlogic.survlogic.database.ProjectDatabaseHandler;
import com.survlogic.survlogic.model.JobSketch;
import com.survlogic.survlogic.model.PointGeodetic;
import com.survlogic.survlogic.model.ProjectImages;
import com.survlogic.survlogic.utils.SurveyMathHelper;
import com.survlogic.survlogic.utils.PreferenceLoaderHelper;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by chrisfillmore on 8/21/2017.
 */

public class DialogJobPointView extends DialogFragment {
    private static final String TAG = "DialogJobPointView";

    private Context mContext;
    private GridView gridView, sketchGridView;
    private PointGridImageAdapter gridAdapter;
    private PointGridSketchAdapter gridSketchAdapter;

    private static final int REQUEST_TAKE_PHOTO = 2;
    private static final int REQUEST_SELECT_PICTURE = 3;

    private static final int DELAY_TO_SHOW_DATA = 1500;
    private static final int DELAY_TO_DIALOG = 1;
    private static final int DELAY_TO_GRID = 1000;

    private Handler showDataHandler, dialogHandler, gridHandler, sketchHandler;
    private boolean isLoading = false, tryingToExit = false;
    private String mURLSyntex = "file://";

    private int pointNo, projectID, jobId, pointId;
    private String databaseName, mCurrentPhotoPath;
    private Bitmap mBitmap, mBitmapRaw;

    private TextView tvPointNo, tvPointDesc, tvPointClass, tvPointNorth, tvPointEast, tvPointElev,
            tvPointLat, tvPointLong, tvPointEllipsoid, tvPointOrtho,
            tvPointLatHeader, tvPointLongHeader, tvPointEllipsoidHeader;

    private ProgressBar pbProgressCircle;

    private Button btTakePhoto, btAddSketch;

    private SharedPreferences sharedPreferences;
    private PreferenceLoaderHelper preferenceLoaderHelper;

    private static DecimalFormat COORDINATE_FORMATTER, DISTANCE_PRECISION_FORMATTER;


    public static DialogJobPointView newInstance(int projectId, int jobId, long pointId, int pointNo, String databaseName) {
        Log.d(TAG, "newInstance: Starting...");
        DialogJobPointView frag = new DialogJobPointView();
        Bundle args = new Bundle();
        args.putInt("project_id", projectId);
        args.putInt("job_id", jobId);
        args.putLong("point_id", pointId);
        args.putInt("pointNo", pointNo);
        args.putString("databaseName", databaseName);
        frag.setArguments(args);
        return frag;

    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Log.d(TAG, "onCreateDialog: Starting...>");

        projectID = getArguments().getInt("project_id");
        jobId = getArguments().getInt("job_id");
        pointId = (int) getArguments().getLong("point_id");

        pointNo = getArguments().getInt("pointNo");
        databaseName = getArguments().getString("databaseName");


        Log.d(TAG, "onCreateDialog: Project Id: " + projectID );
        Log.d(TAG, "onCreateDialog: Job Id: " + jobId );

        Log.d(TAG, "onCreateDialog: PointID: " + pointId );
        Log.d(TAG, "onCreateDialog: Point No: " + pointNo );


        Log.d(TAG, "onCreateDialog: Database Name:" + databaseName + " Loaded...");

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.DialogExplodeInStyle);
        LayoutInflater inflater = getActivity().getLayoutInflater();

        View v = inflater.inflate(R.layout.dialog_job_point_view,null);
        builder.setView(v);

        builder.create();
        return builder.show();
    }


    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: Started...");
        mContext = getActivity();
        AlertDialog alertDialog = (AlertDialog) getDialog();

        alertDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP){
                    Log.d(TAG, "onKey: Canceling All Handlers");
                    showDataHandler.removeCallbacksAndMessages(null);
                    dialogHandler.removeCallbacksAndMessages(null);
                    gridHandler.removeCallbacksAndMessages(null);

                    return false;
                }
                return false;
            }
        });

        preferenceLoaderHelper = new PreferenceLoaderHelper(mContext);
        loadPreferences();

        initViewWidgets();
        setOnClickListeners();
        showPointData();
        showGridView();
        showSketchGridView();

        showDialogAnimation();

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (this.REQUEST_TAKE_PHOTO == requestCode && resultCode == Activity.RESULT_OK) {
            Uri imageUri = Uri.parse(mCurrentPhotoPath);
            File file = new File(imageUri.getPath());

            try {
                mBitmapRaw=decodeUri(imageUri,400);
                mBitmap = rotateImageIfRequired(mBitmapRaw,imageUri);

                createPhotoDialog(mBitmap);

            } catch (Exception e) {
                showToast("Caught Error: Could not set Photo to Image from Camera",true);

            }
        } else if (this.REQUEST_SELECT_PICTURE == requestCode && resultCode == Activity.RESULT_OK) {
            if (data != null) {


                try {
                    final Uri imageUri = data.getData();
                    File file = new File(imageUri.getPath());

                    if (imageUri !=null){
                        mBitmapRaw=decodeUri(imageUri,400);
                        mBitmap = rotateImageIfRequired(mBitmapRaw,imageUri);
                        createPhotoDialog(mBitmap);

                    }


                } catch (Exception e) {
                    showToast("Caught Error: Could not set Photo to Image from Gallery",true);

                }
            }
        }


    }

    //-------------------------------------------------------------------------------------------------------------------------//

    /**
     * JAVA Methods
     */
    private void showPointData(){

        isLoading = true;
        showDataHandler = new Handler();

        showDataHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                boolean loadSuccess = initValuesFromObject();

                if(loadSuccess){
                    pbProgressCircle.setVisibility(View.GONE);
                }
                isLoading = false;
            }
        },DELAY_TO_SHOW_DATA);
    }

    private void initViewWidgets(){
        Log.d(TAG, "initViewWidgets: Started...");

        gridView = (GridView) getDialog().findViewById(R.id.photo_grid_view);
        sketchGridView = (GridView) getDialog().findViewById(R.id.sketch_grid_view);

        tvPointNo = (TextView) getDialog().findViewById(R.id.pointNoValue);
        tvPointDesc = (TextView) getDialog().findViewById(R.id.pointDescValue);
        tvPointClass = (TextView) getDialog().findViewById(R.id.pointClassValue);

        tvPointNorth = (TextView) getDialog().findViewById(R.id.northingValue);
        tvPointEast = (TextView) getDialog().findViewById(R.id.eastingValue);
        tvPointElev = (TextView) getDialog().findViewById(R.id.elevationValue);

        tvPointLatHeader = (TextView) getDialog().findViewById(R.id.latitudeTitle);
        tvPointLongHeader = (TextView) getDialog().findViewById(R.id.longitudeTitle);
        tvPointEllipsoidHeader = (TextView) getDialog().findViewById(R.id.ellipsoidHeightTitle);

        tvPointLat  = (TextView) getDialog().findViewById(R.id.latitudeValue);
        tvPointLong = (TextView) getDialog().findViewById(R.id.longitudeValue);
        tvPointEllipsoid = (TextView) getDialog().findViewById(R.id.ellipsoidHeightValue);
        tvPointOrtho = (TextView) getDialog().findViewById(R.id.orthoHeightValue);

        pbProgressCircle = (ProgressBar) getDialog().findViewById(R.id.progressBar_Loading_point);

        btTakePhoto = (Button) getDialog().findViewById(R.id.card3_take_photo);
        btAddSketch = (Button) getDialog().findViewById(R.id.card4_add_sketch);



    }

    private void setOnClickListeners(){
        Log.d(TAG, "setOnClickListeners: Started...");

        btTakePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callImageSelectionDialog();
            }
        });

        btAddSketch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callAddNewSketch();
            }
        });

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ProjectImages item = (ProjectImages) parent.getItemAtPosition(position);

                //viewPhotoDialog(item.getProjectId(),imageHelper.convertToBitmap(item.getImage()), position);
                viewPhotoDialog(item.getProjectId(),item.getImagePath(), position);

            }
        });

        sketchGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                JobSketch item = (JobSketch) parent.getItemAtPosition(position);

                viewSketchDialog(item.getImagePath(),position,pointId);


            }
        });





    }

    private boolean initValuesFromObject() {
        Log.d(TAG, "initValuesFromObject: Started");
        boolean results = false;

        try {
            Log.d(TAG, "initValuesFromObject: Connecting to db...");

            JobDatabaseHandler jobDb = new JobDatabaseHandler(mContext, databaseName);
            SQLiteDatabase db = jobDb.getReadableDatabase();

            Log.d(TAG, "initValuesFromObject: Point No: " + pointNo);
            PointGeodetic pointGeodetic = jobDb.getPointByPointNo(db,pointNo);

            double pointNorthing = pointGeodetic.getNorthing();
            String pointNorthingValue = COORDINATE_FORMATTER.format(pointNorthing);

            double pointEasting = pointGeodetic.getEasting();
            String pointEastingValue = COORDINATE_FORMATTER.format(pointEasting);

            double pointElevation = pointGeodetic.getElevation();
            String pointElevationValue = COORDINATE_FORMATTER.format(pointElevation);

            String pointDescription = pointGeodetic.getDescription();

            double pointLatitude = pointGeodetic.getLatitude();
            double pointLongitude = pointGeodetic.getLongitude();

            double pointEllipsoid = pointGeodetic.getEllipsoid();
            String pointEllipsoidValue = COORDINATE_FORMATTER.format(pointEllipsoid);

            tvPointNo.setText(String.valueOf(pointNo));
            tvPointDesc.setText(pointDescription);

            tvPointNorth.setText(pointNorthingValue);
            tvPointEast.setText(pointEastingValue);
            tvPointElev.setText(pointElevationValue);

            tvPointNorth.setVisibility(View.VISIBLE);
            tvPointEast.setVisibility(View.VISIBLE);
            tvPointElev.setVisibility(View.VISIBLE);

            if(pointLatitude !=0){
                tvPointLatHeader.setText(getString(R.string.dialog_point_view_pointLatitude_header));
                tvPointLongHeader.setText(getString(R.string.dialog_point_view_pointLongitude_header));

                tvPointLat.setText(SurveyMathHelper.convertDECtoDMSGeodetic(pointLatitude,3,true));
                tvPointLong.setText(SurveyMathHelper.convertDECtoDMSGeodetic(pointLongitude,3,true));

                tvPointLat.setVisibility(View.VISIBLE);
                tvPointLong.setVisibility(View.VISIBLE);
            }

            if(pointEllipsoid !=0){
                tvPointEllipsoidHeader.setText(getString(R.string.dialog_point_view_pointEllipsoid_header));

                tvPointEllipsoid.setText(pointEllipsoidValue);
                tvPointEllipsoid.setVisibility(View.VISIBLE);
            }

            Log.d(TAG, "initValuesFromObject: Success, Closing Db");
            db.close();
            results = true;
        }catch (Exception e){
            e.printStackTrace();
        }



        return results;
    }

    private void createPhotoDialog(Bitmap bitmap){
        FragmentManager fm = getActivity().getSupportFragmentManager();
        DialogPointPhotoAdd photoDialog = DialogPointPhotoAdd.newInstance(projectID, jobId, pointId, pointNo, bitmap);
        photoDialog.show(fm,"dialog");

    }

    private void loadPreferences(){
        Log.d(TAG, "loadPreferences: Started...");

        COORDINATE_FORMATTER = new DecimalFormat(preferenceLoaderHelper.getValueSystemCoordinatesPrecisionDisplay());
        DISTANCE_PRECISION_FORMATTER = new DecimalFormat(preferenceLoaderHelper.getValueSystemDistancePrecisionDisplay());

    }

    private void showDialogAnimation(){
        isLoading = true;
        dialogHandler = new Handler();

        dialogHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                int width = getResources().getDisplayMetrics().widthPixels;
                int height = getResources().getDisplayMetrics().heightPixels;

                width = width - 10;
                height = height - 100;

                getDialog().getWindow().setLayout(width,height);
                isLoading = false;
            }
        },DELAY_TO_DIALOG);

    }

    private void callAddNewSketch(){
        //Senting to JobPointAddSketchActivity


        Log.d(TAG, "Checking: ProjectID " + projectID);

        Intent i = new Intent(getActivity(), JobPointAddSketchActivity.class);
        i.putExtra("KEY_PROJECT_ID",projectID);
        i.putExtra(getString(R.string.KEY_JOB_ID), jobId);
        i.putExtra(getString(R.string.KEY_POINT_ID), pointId);
        i.putExtra(getString(R.string.KEY_POINT_NO), pointNo);
        i.putExtra(getString(R.string.KEY_JOB_DATABASE), databaseName);


        startActivity(i);
        getActivity().overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);



    }

    //-------------------------------------------------------------------------------------------------------------------------//

    /**
     * Image Objects
     */

    private void callImageSelectionDialog(){
        Log.d(TAG, "callImageSelectionDialog: Started...");
        final CharSequence[] items = { getString(R.string.project_new_dialog_takePhoto), getString(R.string.project_new_dialog_getImage),
                getString(R.string.general_cancel) };

        TextView title = new TextView(getActivity());  //was context not this

        title.setText(getString(R.string.photo_dialog_title));
        title.setBackgroundColor(Color.WHITE);
        title.setPadding(10, 15, 15, 10);
        title.setGravity(Gravity.CENTER);
        title.setTextColor(Color.BLACK);
        title.setTextSize(22);

        android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(getActivity());

        builder.setCustomTitle(title);

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
            showToast("Caught Error: Accessing Camera Exception",true);
        }
    }

    private void startPhotoGallery(){
        Log.d(TAG, "startPhotoGallery: Started...");
        try{
            dispatchPhotoFromGalleryIntent();

        } catch (IOException e){
            showToast("caught Error: Accessing Gallery Exception",true);
        }

    }

    private void dispatchTakePictureIntent() throws IOException {
        Log.d(TAG, "dispatchTakePictureIntent: Started...");
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
                showToast("Caught Error: Could not create file",true);
                return;
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(getActivity(),
                        BuildConfig.APPLICATION_ID + ".provider", createImageFile());

                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }

    private void dispatchPhotoFromGalleryIntent() throws IOException{
        Log.d(TAG, "dispatchPhotoFromGalleryIntent: Started...");
        Intent intent = new Intent(Intent.ACTION_PICK,android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Select Picture"),REQUEST_SELECT_PICTURE);

    }

    private File createImageFile() throws IOException {
        Log.d(TAG, "createImageFile: Started...");
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = "file:" + image.getAbsolutePath();
        return image;
    }

    protected Bitmap decodeUri(Uri selectedImage, int REQUIRED_SIZE) {

        try {

            // Decode image size
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(mContext.getContentResolver().openInputStream(selectedImage), null, o);

            // The new size we want to scale to
            // final int REQUIRED_SIZE =  size;

            // Find the correct scale value. It should be the power of 2.
            int width_tmp = o.outWidth, height_tmp = o.outHeight;
            int scale = 1;
            while (true) {
                if (width_tmp / 2 < REQUIRED_SIZE
                        || height_tmp / 2 < REQUIRED_SIZE) {
                    break;
                }
                width_tmp /= 2;
                height_tmp /= 2;
                scale *= 2;
            }

            // Decode with inSampleSize
            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize = scale;
            return BitmapFactory.decodeStream(getActivity().getContentResolver().openInputStream(selectedImage), null, o2);
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    private Bitmap rotateImageIfRequired(Bitmap img, Uri selectedImage) throws IOException {

        InputStream input = mContext.getContentResolver().openInputStream(selectedImage);
        ExifInterface ei;
        if (Build.VERSION.SDK_INT > 23)
            ei = new ExifInterface(input);
        else
            ei = new ExifInterface(selectedImage.getPath());

        int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                return rotateImage(img, 90);
            case ExifInterface.ORIENTATION_ROTATE_180:
                return rotateImage(img, 180);
            case ExifInterface.ORIENTATION_ROTATE_270:
                return rotateImage(img, 270);
            default:
                return img;
        }
    }

    private static Bitmap rotateImage(Bitmap img, int degree) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        Bitmap rotatedImg = Bitmap.createBitmap(img, 0, 0, img.getWidth(), img.getHeight(), matrix, true);
        img.recycle();
        return rotatedImg;
    }

    //-------------------------------------------------------------------------------------------------------------------------//

    /**
     * Grid View (GV)
     */

    private void showGridView(){
        Log.d(TAG, "showGridView: Started...");
        isLoading = true;

        gridHandler = new Handler();
        gridHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                initGridView();
                isLoading = false;
            }
        },DELAY_TO_GRID);
    }

    private void showSketchGridView(){
        Log.d(TAG, "showSketchGridView: Started...");
        isLoading  = true;

        sketchHandler = new Handler();
        sketchHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                initSketchGridView();
                isLoading = false;
            }
        },DELAY_TO_GRID);
    }

    private void initGridView(){
        Log.d(TAG, "initGridView: Started");

        Log.d(TAG, "Data: ProjectID: " + projectID);
        Log.d(TAG, "Data: JobID: " + jobId);
        Log.d(TAG, "Data: pointID: " + pointId);


        if(getImageCount(projectID, jobId, pointId)){
            Log.d(TAG, "initGridView: getImageCount = True");
            gridAdapter = new PointGridImageAdapter(mContext, R.layout.layout_grid_imageview, mURLSyntex, getImageFromPointData(projectID, jobId, pointId));
            gridView.setAdapter(gridAdapter);
            gridView.setVisibility(View.VISIBLE);
        }
    }

    private void initSketchGridView(){
        Log.d(TAG, "initSketchGridView: Started...");

        Log.d(TAG, "Data: ProjectID: " + projectID);
        Log.d(TAG, "Data: JobID: " + jobId);
        Log.d(TAG, "Data: pointID: " + pointId);

        if(getSketchCount(pointId)){
            Log.d(TAG, "initSketchGridView: getSketchCount = True");
            gridSketchAdapter = new PointGridSketchAdapter(mContext, R.layout.layout_grid_imageview, mURLSyntex, getSketchFromPointData(pointId));
            sketchGridView.setAdapter(gridSketchAdapter);
            sketchGridView.setVisibility(View.VISIBLE);
        }



    }

    private void refreshGridView() {
        if(getImageCount(projectID, jobId, pointId)){

            //Todo GridAdapter on 1st run does not exist.  Need to check and see if gridAdapter has been created, if not, create

            gridAdapter.clear();

            gridAdapter = new PointGridImageAdapter(mContext, R.layout.layout_grid_imageview, mURLSyntex, getImageFromPointData(projectID, jobId, pointId));
            gridView.setAdapter(gridAdapter);
            gridView.setVisibility(View.VISIBLE);


        }
    }


    private ArrayList<ProjectImages> getImageFromPointData(int projectId, int jobId, int pointId){
        Log.d(TAG, "getImageFromProjectData: Connecting to db");
        ProjectDatabaseHandler projectDb = new ProjectDatabaseHandler(mContext);
        SQLiteDatabase db = projectDb.getReadableDatabase();

        ArrayList<ProjectImages> projectImages = new ArrayList<ProjectImages>(projectDb.getProjectImagesbyPointID(db,projectId, jobId, pointId));


        Log.d(TAG, "getImageFromProjectData: Closing DB Connection");
        db.close();

        return projectImages;

    }

    private ArrayList<JobSketch> getSketchFromPointData(int pointId){
        Log.d(TAG, "getSketchFromPointData: Connecting to db");

        JobDatabaseHandler jobDb = new JobDatabaseHandler(mContext, databaseName);
        SQLiteDatabase db = jobDb.getReadableDatabase();

        ArrayList<JobSketch> jobSketches = new ArrayList<>(jobDb.getJobSketchesByPointID(db,pointId));

        Log.d(TAG, "getSketchFromPointData: Closing DB Connection");
        db.close();

        return jobSketches;
    }

    private boolean getImageCount(int projectId, int jobId, int pointId){
        Log.d(TAG, "getImageCount: Connecting to db");
        long count = 0;
        boolean results = false;
        ProjectDatabaseHandler projectDb = new ProjectDatabaseHandler(mContext);
        SQLiteDatabase db = projectDb.getReadableDatabase();

        count = ProjectDatabaseHandler.getCountProjectImagesByPointID(db,projectId, jobId, pointId);
        Log.d(TAG, "getImageCount: Count = " + count);

        if (count !=0){
            results = true;
        }

        Log.d(TAG, "getImageCount: Closing DB Connection");
        db.close();
        Log.d(TAG, "getImageCount: Results: " + results);
        return results;
    }

    private boolean getSketchCount(int pointId){
        Log.d(TAG, "getSketchCount: Connecting to db");
        long count = 0;
        boolean results = false;
        JobDatabaseHandler jobDb = new JobDatabaseHandler(mContext, databaseName);
        SQLiteDatabase db = jobDb.getReadableDatabase();

        count = JobDatabaseHandler.getCountJobSketchByPointID(db,pointId);
        Log.d(TAG, "getSketchCount: Count = " + count);

        if (count !=0){
            results = true;
        }

        Log.d(TAG, "getSketchCount: Closing DB Connection");
        db.close();

        return results;
    }

    private void viewPhotoDialog(Integer project_id, String imagePath, int position){
        if(position == 3){
            Intent intent = new Intent(mContext, PhotoGalleryActivity.class);
            intent.putExtra("PROJECT_ID",projectID);
            startActivity(intent);

        }else{


            FragmentManager fm = getActivity().getSupportFragmentManager();
            DialogFragment viewDialog = DialogJobPointPhotoView.newInstance(projectID, jobId, pointId,mURLSyntex,imagePath);
            viewDialog.show(getFragmentManager(),"dialog_view");

        }

    }

    private void viewSketchDialog(String imagePath, int position, int pointId){
        if(position == 3){
            Intent intent = new Intent(mContext, PhotoGalleryActivity.class);
            intent.putExtra("POINT_ID",pointId);
            startActivity(intent);

        }else{


            FragmentManager fm = getActivity().getSupportFragmentManager();
            DialogFragment viewDialog = DialogJobPointPhotoView.newInstance(projectID, jobId, pointId,mURLSyntex,imagePath);
            viewDialog.show(getFragmentManager(),"dialog_view");

        }
    }




    //-------------------------------------------------------------------------------------------------------------------------//

    /**
     * Method Helpers
     */


    private void showToast(String data, boolean shortTime) {

        if (shortTime) {
            Toast.makeText(getActivity(), data, Toast.LENGTH_SHORT).show();

        } else{
            Toast.makeText(getActivity(), data, Toast.LENGTH_LONG).show();

        }
    }
}
