package com.survlogic.survlogic.dialog;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Typeface;
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
import android.support.v7.widget.CardView;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
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
import com.survlogic.survlogic.interf.JobPointsActivityListener;
import com.survlogic.survlogic.model.JobSketch;
import com.survlogic.survlogic.model.Point;
import com.survlogic.survlogic.model.PointGeodetic;
import com.survlogic.survlogic.model.ProjectImages;
import com.survlogic.survlogic.utils.AnimateBounceInterpolator;
import com.survlogic.survlogic.utils.StringUtilityHelper;
import com.survlogic.survlogic.utils.SurveyMathHelper;
import com.survlogic.survlogic.utils.PreferenceLoaderHelper;
import com.survlogic.survlogic.utils.SurveyProjectionHelper;

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

public class DialogJobPointView extends DialogFragment{
    private static final String TAG = "DialogJobPointView";

    private Context mContext;
    private GridView gridView, sketchGridView;
    private PointGridImageAdapter gridAdapter;
    private PointGridSketchAdapter gridSketchAdapter;

    private static final int REQUEST_TAKE_PHOTO = 2;
    private static final int REQUEST_SELECT_PICTURE = 3;

    private static final int DELAY_TO_SHOW_DATA = 1000;
    private static final int DELAY_TO_DIALOG = 1;
    private static final int DELAY_TO_GRID = 1500;

    private Handler showDataHandler, dialogHandler, gridHandler, sketchHandler;
    private boolean isLoading = false, tryingToExit = false;
    private String mURLSyntex = "file://";

    private int pointNo, projectID, jobId, pointId;
    private String databaseName, mCurrentPhotoPath, pointDescription, pointDescriptionDirty;
    private Bitmap mBitmap, mBitmapRaw;

    private RelativeLayout rlGridView, rlPointOptions,
            rlPlanarViewDetails, rlWorldViewDetails, rlGridViewDetails,
            rlPointCommands, rlPlanarCommands, rlGeodeticCommands, rlGridCommands;

    private CardView cardViewPoint, cardViewPlanar, cardViewWorld, cardViewGrid, cardViewPhotos, cardViewSketchs;

    private TextView tvPointNo, tvPointDesc, tvPointClassPlanar, tvPointNorth, tvPointEast, tvPointElev,
            tvPointLat, tvPointLong, tvPointEllipsoid, tvPointOrtho, tvPointClassWorld,
            tvPointLatHeader, tvPointLongHeader, tvPointEllipsoidHeader, tvPointOrthoHeader,
            tvPointGridNorthHeader, tvPointGridEastHeader,
            tvPointGridNorth, tvPointGridEast, tvPointClassGrid;

    private ImageView ivPointCommands;

    private ImageButton ibPointCardExpand;
    private ImageButton ibPointOption_1, ibPointOption_2;
    private ImageButton ibPointCommand_1, ibPointCommand_2, ibPointCommand_3;
    private ImageButton ibPlanarCommand_1;
    private ImageButton ibGeodeticCommand_1, ibGeodeticCommand_2;
    private ImageButton ibGridCommand_1;

    private ProgressBar pbProgressCircle;

    private Button btTakePhoto, btAddSketch;

    private boolean isPointCardExpanded = false, isPointCommandsShown = false, isProjection = false;
    private boolean isFromPointList = false;

    private PreferenceLoaderHelper preferenceLoaderHelper;
    private SurveyProjectionHelper surveyProjectionHelper;

    private int planarPointType, geodeticPointType, gridPointType;
    private int geodeticPointTypeDirty, gridPointTypeDirty;

    private double planarNorthValue = 0, planarEastValue = 0, planarElevationValue = 0;
    private double planarNorthDirty = 0, planarEastDirty = 0, planarElevationDirty = 0;

    private double latitudeValue = 0, longitudeValue = 0, heightEllipsoidValue = 0, heightOrthoValue = 0;
    private double latitudeDirty = 0, longitudeDirty = 0, heightEllipsoidDirty = 0, heightOrthoDirty = 0;

    private boolean isGridViewShown = false;
    private double gridNorthValue = 0, gridEastValue = 0;
    private double gridNorthDirty = 0, gridEastDirty = 0;

    private int creationDate;

    private boolean isPlanarDirty = false, isGeodeticDirty = false, isGridDirty = false, isPointDescriptionDirty = false;
    private static DecimalFormat COORDINATE_FORMATTER, DISTANCE_PRECISION_FORMATTER;



    public static DialogJobPointView newInstance(int projectId, int jobId, long pointId, int pointNo, String databaseName, boolean isFromPointList) {
        Log.d(TAG, "newInstance: Starting...");

        DialogJobPointView frag = new DialogJobPointView();
        Bundle args = new Bundle();
        args.putInt("project_id", projectId);
        args.putInt("job_id", jobId);
        args.putLong("point_id", pointId);
        args.putInt("pointNo", pointNo);
        args.putString("databaseName", databaseName);
        args.putBoolean("fromPointList",isFromPointList);
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

        isFromPointList = getArguments().getBoolean("fromPointList");

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
                    sketchHandler.removeCallbacks(null);

                    dismissDialogFragment();

                    return true;
                }
                return false;
            }
        });

        preferenceLoaderHelper = new PreferenceLoaderHelper(mContext);
        surveyProjectionHelper = new SurveyProjectionHelper(mContext);

        loadPreferences();

        initViewWidgets();
        setOnClickListeners();
        showPointData();
        initProjection();

        showCardAnimation();

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

        gridView = getDialog().findViewById(R.id.photo_grid_view);
        sketchGridView = getDialog().findViewById(R.id.sketch_grid_view);

        rlPointOptions =  getDialog().findViewById(R.id.rl_point_options);
        rlGridView = getDialog().findViewById(R.id.rlGridView);

        rlPlanarViewDetails = getDialog().findViewById(R.id.rl_planar_view_details);
        rlWorldViewDetails = getDialog().findViewById(R.id.rl_geographic_view_details);
        rlGridViewDetails =  getDialog().findViewById(R.id.rl_grid_view_details);

        rlPointCommands = getDialog().findViewById(R.id.rl_points_commands);
        rlPlanarCommands = getDialog().findViewById(R.id.rl_points_commands_planar);
        rlGeodeticCommands = getDialog().findViewById(R.id.rl_points_commands_geographic);
        rlGridCommands = getDialog().findViewById(R.id.rl_points_commands_grid);

        cardViewPoint = getDialog().findViewById(R.id.layout_area_1);
        cardViewPlanar = getDialog().findViewById(R.id.card_planar_view);
        cardViewWorld = getDialog().findViewById(R.id.card_geographic_view);
        cardViewGrid = getDialog().findViewById(R.id.card_grid_view);
        cardViewPhotos = getDialog().findViewById(R.id.card_photo_view);
        cardViewSketchs =  getDialog().findViewById(R.id.card_sketch_view);

        tvPointNo = getDialog().findViewById(R.id.pointNoValue);
        tvPointDesc = getDialog().findViewById(R.id.pointDescValue);

        tvPointClassPlanar = getDialog().findViewById(R.id.planarPointTypeValue);
        tvPointClassWorld = getDialog().findViewById(R.id.geographicPointTypeValue);
        tvPointClassGrid = getDialog().findViewById(R.id.gridPointTypeValue);

        tvPointNorth =  getDialog().findViewById(R.id.northingValue);
        tvPointEast = getDialog().findViewById(R.id.eastingValue);
        tvPointElev = getDialog().findViewById(R.id.elevationValue);

        tvPointLatHeader =  getDialog().findViewById(R.id.latitudeTitle);
        tvPointLongHeader =  getDialog().findViewById(R.id.longitudeTitle);
        tvPointEllipsoidHeader =  getDialog().findViewById(R.id.ellipsoidHeightTitle);
        tvPointOrthoHeader =  getDialog().findViewById(R.id.orthoHeightTitle);

        tvPointLat  =  getDialog().findViewById(R.id.latitudeValue);
        tvPointLong =  getDialog().findViewById(R.id.longitudeValue);
        tvPointEllipsoid =  getDialog().findViewById(R.id.ellipsoidHeightValue);
        tvPointOrtho =  getDialog().findViewById(R.id.orthoHeightValue);

        tvPointGridNorthHeader =  getDialog().findViewById(R.id.gridNorthTitle);
        tvPointGridEastHeader =  getDialog().findViewById(R.id.gridEastTitle);
        tvPointGridNorth =  getDialog().findViewById(R.id.gridNorthValue);
        tvPointGridEast =  getDialog().findViewById(R.id.gridEastValue);

        ibPointCardExpand =  getDialog().findViewById(R.id.card_point_expand);

        ibPointOption_1 =  getDialog().findViewById(R.id.points_options_1);
        ibPointOption_2 =  getDialog().findViewById(R.id.points_options_2);

        ivPointCommands =  getDialog().findViewById(R.id.survey_image_center);
        ibPointCommand_1 =  getDialog().findViewById(R.id.points_command_1);
        ibPointCommand_2 =  getDialog().findViewById(R.id.points_command_2);
        ibPointCommand_3 =  getDialog().findViewById(R.id.points_command_3);

        ibPlanarCommand_1 =  getDialog().findViewById(R.id.planar_command_1);

        ibGeodeticCommand_1 =  getDialog().findViewById(R.id.geographic_command_1);
        ibGeodeticCommand_2 =  getDialog().findViewById(R.id.geographic_command_2);

        ibGridCommand_1 =  getDialog().findViewById(R.id.grid_command_1);

        pbProgressCircle =  getDialog().findViewById(R.id.progressBar_Loading_point);

        btTakePhoto =  getDialog().findViewById(R.id.card3_take_photo);
        btAddSketch =  getDialog().findViewById(R.id.card4_add_sketch);

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

        ibPointCardExpand.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                animatePointCard();
            }
        });

        ibPointOption_1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToDialogPointDescription();
            }
        });

        ibPointOption_2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               android.support.v7.app.AlertDialog dialog = DialogCloseActivity();
               dialog.show();
            }
        });

        ibPointCommand_3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismissDialogFragment();
            }
        });


        ibPlanarCommand_1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToDialogPlanar();
            }
        });

        ibGeodeticCommand_1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToDialogWorld();
            }
        });

        ibGridCommand_1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToDialogGrid();
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

            planarNorthValue = pointGeodetic.getNorthing();
            planarEastValue = pointGeodetic.getEasting();
            planarElevationValue = pointGeodetic.getElevation();

            pointDescription = pointGeodetic.getDescription();

            //Point Class
            planarPointType = pointGeodetic.getPointType();
            geodeticPointType = pointGeodetic.getPointGeodeticType();


            latitudeValue= pointGeodetic.getLatitude();
            longitudeValue = pointGeodetic.getLongitude();

            heightEllipsoidValue = pointGeodetic.getEllipsoid();
            heightOrthoValue = pointGeodetic.getOrtho();

            creationDate = pointGeodetic.getDateCreated();

            tvPointNo.setText(String.valueOf(pointNo));
            tvPointDesc.setText(pointDescription);

            setPointTypes();

            setPlanarCoordinateText(planarNorthValue,planarEastValue,planarElevationValue,false);

            setGeodeticCoordinateText(latitudeValue,longitudeValue,false);
            setGeodeticEllipsoidText(heightEllipsoidValue,false);
            setGeodeticOrthoText(heightOrthoValue,false );


            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    animatePlanarCard();
                    animatePlanarCommands(true);
                }
            },250);


            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    animateWorldCard();
                    animateGeodeticCommands(true);
                }
            },500);


            Log.d(TAG, "initValuesFromObject: Is projected: " + isProjection);
            if(isProjection){
                rlGridView.setVisibility(View.VISIBLE);
                isGridViewShown = true;

                if(latitudeValue !=0){
                    findGridCoordinatesFromWorld(latitudeValue,longitudeValue);
                }

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        animateGridCard();
                        animateGridCommands(true);
                    }
                },750);

            }


            Log.d(TAG, "initValuesFromObject: Success, Closing Db");

            db.close();
            results = true;
        }catch (Exception e){
            e.printStackTrace();
        }



        return results;
    }

    private void findGridCoordinatesFromWorld(double latitude, double longitude){
        Log.d(TAG, "showMapProjection: Started");

        Point pointIn = new Point(latitude,longitude);
        Point myProjectionPoint = surveyProjectionHelper.calculateGridCoordinates(pointIn);

        if(isGeodeticDirty){
            isGridDirty = true;
            gridNorthDirty = myProjectionPoint.getNorthing();
            gridEastDirty = myProjectionPoint.getEasting();

            setGridCoordinateText(gridNorthValue,gridEastValue,false);
        }else{
            gridNorthValue = myProjectionPoint.getNorthing();
            gridEastValue = myProjectionPoint.getEasting();

            setGridCoordinateText(gridNorthValue,gridEastValue,false);
        }

        geodeticPointType = 1;
        gridPointType = 1;
        setPointTypes();

    }

    private void findWorldCoordinatesFromGrid(double gridNorth, double gridEast){
        Log.d(TAG, "findWorldCoordinatesFromGrid: Started");

        Point gridIN = new Point();
        gridIN.setNorthing(gridNorth);
        gridIN.setEasting(gridEast);

        Point worldOUT;
        worldOUT = surveyProjectionHelper.calculateGeodeticCoordinates(gridIN);

        if(isGridDirty){
            isGeodeticDirty = true;
            latitudeDirty = worldOUT.getNorthing();
            longitudeDirty = worldOUT.getEasting();
            setGeodeticCoordinateText(latitudeDirty,longitudeDirty,true);

            geodeticPointType = 5;
            gridPointType = 5;
            setPointTypes();


        }

    }


    private void deletePoint(){
        Log.d(TAG, "deletePoint: Started");
        JobDatabaseHandler jobDb = new JobDatabaseHandler(mContext, databaseName);
        SQLiteDatabase db = jobDb.getWritableDatabase();

        jobDb.deletePointByPointNo(db,pointNo);

        db.close();

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

    private void dismissDialogFragment(){
        Log.d(TAG, "dismissDialogFragment: ");

        Log.d(TAG, "dismissDialogFragment: What is dirty: " + isPlanarDirty + "," + isGeodeticDirty + ", " + isGridDirty);


        if(isPlanarDirty || isGeodeticDirty || isGridDirty || isPointDescriptionDirty){
            android.support.v7.app.AlertDialog dialog = DialogDirtyActivity();
            dialog.show();
        }else{
            closeThisView();
        }


    }

    /**
     *Creates a Dialog Box to Confirm user to leave the Job Activity
     */

    private android.support.v7.app.AlertDialog DialogCloseActivity(){

        android.support.v7.app.AlertDialog myDialogBox = new android.support.v7.app.AlertDialog.Builder(mContext)
                .setTitle(getResources().getString(R.string.dialog_point_view_delete_point_title))
                .setMessage(getResources().getString(R.string.dialog_point_view_delete_point_description))
                .setPositiveButton(getResources().getString(R.string.general_yes), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        pbProgressCircle.setVisibility(View.VISIBLE);
                        deletePoint();
                        dialog.dismiss();
                        closeThisView();
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

    private android.support.v7.app.AlertDialog DialogDirtyActivity(){

        android.support.v7.app.AlertDialog myDialogBox = new android.support.v7.app.AlertDialog.Builder(mContext)
                .setTitle(getResources().getString(R.string.dialog_point_view_dirty_points_title))
                .setMessage(getResources().getString(R.string.dialog_point_view_dirty_points_description))
                .setPositiveButton(getResources().getString(R.string.general_yes), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        pbProgressCircle.setVisibility(View.VISIBLE);
                        createDirtyPointGeodetic();

                        dialog.dismiss();
                        closeThisView();
                    }
                })
                .setNegativeButton(getResources().getString(R.string.general_no), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        closeThisView();
                    }
                })

                .setNeutralButton(getResources().getString(R.string.general_cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })

                .create();

        return myDialogBox;
    }


    private void closeThisView(){
        Log.d(TAG, "closeThisView: Started");


        if(isFromPointList){
            JobPointsActivityListener jobPointsActivityListener = (JobPointsActivityListener) getActivity();
            jobPointsActivityListener.refreshPointArrays();
        }

        dismiss();


    }

    //-------------------------------------------------------------------------------------------------------------------------//
    /**
     * Edit menu unctions
     */


    private void goToDialogPlanar(){
        Log.d(TAG, "goToDialogPlanar: Started");

        double mPointNorth, mPointEast, mPointElev;

        if(isPlanarDirty){
            mPointNorth = planarNorthDirty;
            mPointEast = planarEastDirty;
            mPointElev = planarElevationDirty;
        }else{
            mPointNorth = planarNorthValue;
            mPointEast = planarEastValue;
            mPointElev = planarElevationValue;
        }


        android.app.FragmentManager fm = getActivity().getFragmentManager();

        DialogJobPointPlanarEntryAdd planarEdit = DialogJobPointPlanarEntryAdd.newInstance(mPointNorth, mPointEast, mPointElev,true);
        planarEdit.setDialogListener(new DialogJobPointPlanarEntryAdd.onUpdatePlanarCoordinatesListener() {
            @Override
            public void onUpdatePlanarCoordinatesSubmit(Point pointOut) {
                checkPlanarCoordinatesFromDialog(pointOut);
            }
        });
        planarEdit.show(fm,"dialog_edit_planar");


    }

    private void goToDialogWorld(){
        Log.d(TAG, "goToDialogCoordinateValues: Starting...");


        double mPointLat, mPointLong, mPointEllipsoid, mPointOrtho;

        if(isGeodeticDirty){
            mPointLat = latitudeDirty;
            mPointLong = longitudeDirty;
            mPointEllipsoid = heightEllipsoidDirty;
            mPointOrtho = heightOrthoDirty;
        }else{
            mPointLat = latitudeValue;
            mPointLong = longitudeValue;
            mPointEllipsoid = heightEllipsoidValue;
            mPointOrtho = heightOrthoValue;
        }


        android.app.FragmentManager fm = getActivity().getFragmentManager();

        DialogJobPointGeodeticEntryAdd geodeticEdit = DialogJobPointGeodeticEntryAdd.newInstance(mPointLat, mPointLong, mPointEllipsoid, mPointOrtho,true);
        geodeticEdit.setDialogListener(new DialogJobPointGeodeticEntryAdd.onUpdateGeodeticCoordinatesListener() {
            @Override
            public void onUpdateGeodeticCoordinatesSubmit(double latOut, double longOut, double heightEllipsOut, double heightOrthoOut) {
                checkGeodeticCoordinatesFromDialog(latOut, longOut, heightEllipsOut, heightOrthoOut);

            }
        });

        geodeticEdit.show(fm,"dialog_edit_geodetic");

    }

    private void goToDialogGrid(){
        Log.d(TAG, "goToDialogGrid: Started");

        double mPointNorth, mPointEast;

        if(isGridDirty){
            mPointNorth = gridNorthDirty;
            mPointEast = gridEastDirty;
        }else{
            mPointNorth = gridNorthValue;
            mPointEast = gridEastValue;

        }

        android.app.FragmentManager fm = getActivity().getFragmentManager();

        DialogJobPointGridEntryAdd gridEdit = DialogJobPointGridEntryAdd.newInstance(mPointNorth, mPointEast,true);
        gridEdit.setDialogListener(new DialogJobPointGridEntryAdd.onUpdateGridCoordinatesListener() {
            @Override
            public void onUpdateGridCoordinatesSubmit(double northOut, double eastOut) {
                checkGridCoordinatesFromDialog(northOut, eastOut);
            }
        });

        gridEdit.show(fm,"dialog_edit_grid");

    }

    private void goToDialogPointDescription(){
        Log.d(TAG, "goToDialogPointDescription: Started");

        String mDescription;

        if(isPointDescriptionDirty){
            mDescription = pointDescriptionDirty;
        }else{
            mDescription = pointDescription;
        }

        android.app.FragmentManager fm = getActivity().getFragmentManager();

        DialogJobPointDescriptionEntryEdit descriptionEdit = DialogJobPointDescriptionEntryEdit.newInstance(mDescription,true);
        descriptionEdit.setDialogListener(new DialogJobPointDescriptionEntryEdit.onUpdateDescriptionListener() {
            @Override
            public void onUpdateDescriptionSubmit(String description) {
                checkPointDescriptionFromDialog(description);
            }
        });

        descriptionEdit.show(fm,"dialog_edit_description");


    }


    //-------------------------------------------------------------------------------------------------------------------------//

    /**
     * Projections
     */


    private void initProjection(){
        Log.d(TAG, "initProjection: Started");
        String projectionString, zoneString;

        int isProjection = 0;

        isProjection = preferenceLoaderHelper.getGeneral_over_projection();
        projectionString = preferenceLoaderHelper.getGeneral_over_projection_string();
        zoneString = preferenceLoaderHelper.getGeneral_over_zone_string();

        if(isProjection == 1){
            Log.d(TAG, "initProjection: With Projection");
            surveyProjectionHelper.setConfig(projectionString,zoneString);
            this.isProjection = true;
        }

    }

    //----------------------------------------------------------------------------------------------//

    private void showCardAnimation(){
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


        showPointCommandsAnimation();
        showPhotoGridView();
        showSketchGridView();

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

    //----------------------------------------------------------------------------------------------//
    private void showPointCommandsAnimation(){
        Log.d(TAG, "showPointCommandsAnimation: Started");

        if(isPointCommandsShown){
            isPointCommandsShown = false;

            animatePointCommands(false);
            animatePlanarCommands(false);
            animateGeodeticCommands(false);

            if(isProjection){
                animateGridCommands(false);
            }

        }else{
            isPointCommandsShown = true;

            animatePointCommands(true);

            Log.d(TAG, "showPointCommandsAnimation: " + isProjection);

        }

    }

    private void animatePointCard(){
        Log.d(TAG, "animatePointCard: Started");

        if(isPointCardExpanded){

            TransitionManager.beginDelayedTransition(cardViewPoint);
            rlPointOptions.setVisibility(View.GONE);

            isPointCardExpanded = false;

        }else{
            TransitionManager.beginDelayedTransition(cardViewPoint);
            rlPointOptions.setVisibility(View.VISIBLE);

            isPointCardExpanded = true;

        }

    }


    private void animatePlanarCard(){
        Log.d(TAG, "animatePlanarCard: Started");

        TransitionManager.beginDelayedTransition(cardViewPlanar);
        rlPlanarViewDetails.setVisibility(View.VISIBLE);

    }

    private void animateWorldCard(){
        Log.d(TAG, "animateWorldCard: Started");

        TransitionManager.beginDelayedTransition(cardViewWorld);
        rlWorldViewDetails.setVisibility(View.VISIBLE);

    }


    private void animateGridCard(){
        Log.d(TAG, "animateGridCard: Started");

        TransitionManager.beginDelayedTransition(cardViewGrid);
        rlGridViewDetails.setVisibility(View.VISIBLE);


    }

    private void animatePhotoCard(){
        Log.d(TAG, "animatePhotoCard: Started");

        TransitionManager.beginDelayedTransition(cardViewPhotos);
        gridView.setVisibility(View.VISIBLE);

    }

    private void animateSketchCard(){
        Log.d(TAG, "animateSketchCard: Started");
        TransitionManager.beginDelayedTransition(cardViewSketchs);
        sketchGridView.setVisibility(View.VISIBLE);

    }

    private void animatePointCommands(boolean isShow){
        Log.d(TAG, "animatePointCommandsShow: Started");


        if(isShow){
            rlPointCommands.setVisibility(View.VISIBLE);

            final Animation mAnimPoints_1 = AnimationUtils.loadAnimation(mContext,R.anim.anim_button_bounce_in);
            final Animation mAnimPoints_2 = AnimationUtils.loadAnimation(mContext,R.anim.anim_button_bounce_in);
            final Animation mAnimPoints_3 = AnimationUtils.loadAnimation(mContext,R.anim.anim_button_bounce_in);

            AnimateBounceInterpolator animateBounceInterpolator = new AnimateBounceInterpolator(0.2,20);
            mAnimPoints_1.setInterpolator(animateBounceInterpolator);
            mAnimPoints_2.setInterpolator(animateBounceInterpolator);
            mAnimPoints_3.setInterpolator(animateBounceInterpolator);


            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    ibPointCommand_3.setVisibility(View.VISIBLE);
                    ibPointCommand_3.startAnimation(mAnimPoints_3);
                }
            },600);
        }else{
            final Animation mAnimPoints_1 = AnimationUtils.loadAnimation(mContext,R.anim.anim_button_bounce_out);
            final Animation mAnimPoints_2 = AnimationUtils.loadAnimation(mContext,R.anim.anim_button_bounce_out);
            final Animation mAnimPoints_3 = AnimationUtils.loadAnimation(mContext,R.anim.anim_button_bounce_out);

            mAnimPoints_1.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    ibPointCommand_1.setVisibility(View.INVISIBLE);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });

            mAnimPoints_2.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    ibPointCommand_2.setVisibility(View.INVISIBLE);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });

            mAnimPoints_3.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    ibPointCommand_3.setVisibility(View.INVISIBLE);
                    rlPointCommands.setVisibility(View.GONE);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    ibPointCommand_1.startAnimation(mAnimPoints_1);

                }
            },200);

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    ibPointCommand_2.startAnimation(mAnimPoints_2);

                }
            },400);

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    ibPointCommand_3.startAnimation(mAnimPoints_3);

                }
            },600);

        }

    }

    private void animatePlanarCommands(boolean isShow){
        Log.d(TAG, "animatePlanarCommandsShow: Started");

        if(isShow){
            rlPlanarCommands.setVisibility(View.VISIBLE);

            final Animation mAnimPlanar_1 = AnimationUtils.loadAnimation(mContext,R.anim.anim_button_bounce_in);

            AnimateBounceInterpolator animateBounceInterpolator = new AnimateBounceInterpolator(0.2,20);
            mAnimPlanar_1.setInterpolator(animateBounceInterpolator);

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    ibPlanarCommand_1.setVisibility(View.VISIBLE);
                    ibPlanarCommand_1.startAnimation(mAnimPlanar_1);

                }
            },200);

        }else {
            final Animation mAnimPlanar_1 = AnimationUtils.loadAnimation(mContext,R.anim.anim_button_bounce_out);

            mAnimPlanar_1.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    ibPlanarCommand_1.setVisibility(View.INVISIBLE);
                    rlPlanarCommands.setVisibility(View.GONE);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    ibPlanarCommand_1.startAnimation(mAnimPlanar_1);

                }
            },200);


        }


    }

    private void animateGeodeticCommands(boolean isShow){
        Log.d(TAG, "animateGeodeticCommandsShow: Started");

        if(isShow){
            rlGeodeticCommands.setVisibility(View.VISIBLE);

            final Animation mAnimGeodetic_1 = AnimationUtils.loadAnimation(mContext,R.anim.anim_button_bounce_in);
            final Animation mAnimGeodetic_2 = AnimationUtils.loadAnimation(mContext,R.anim.anim_button_bounce_in);

            AnimateBounceInterpolator animateBounceInterpolator = new AnimateBounceInterpolator(0.2,20);

            mAnimGeodetic_1.setInterpolator(animateBounceInterpolator);
            mAnimGeodetic_2.setInterpolator(animateBounceInterpolator);

            //--Geodetic Commands
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    ibGeodeticCommand_1.setVisibility(View.VISIBLE);
                    ibGeodeticCommand_1.startAnimation(mAnimGeodetic_1);
                }
            },200);

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    ibGeodeticCommand_2.setVisibility(View.VISIBLE);
                    ibGeodeticCommand_2.startAnimation(mAnimGeodetic_2);
                }
            },400);


        }else {
            final Animation mAnimGeodetic_1 = AnimationUtils.loadAnimation(mContext,R.anim.anim_button_bounce_out);
            final Animation mAnimGeodetic_2 = AnimationUtils.loadAnimation(mContext,R.anim.anim_button_bounce_out);

            mAnimGeodetic_1.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    ibGeodeticCommand_1.setVisibility(View.INVISIBLE);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });

            mAnimGeodetic_2.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    ibGeodeticCommand_2.setVisibility(View.INVISIBLE);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });

            //--Geodetic Commands
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    ibGeodeticCommand_1.startAnimation(mAnimGeodetic_1);
                }
            },200);

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    ibGeodeticCommand_2.startAnimation(mAnimGeodetic_2);
                }
            },400);
        }

    }


    private void animateGridCommands(boolean isShow){
        Log.d(TAG, "animateGridCommandsShow: Started");

        if(isShow){
            rlGridCommands.setVisibility(View.VISIBLE);

            final Animation mAnimGrid_1 = AnimationUtils.loadAnimation(mContext,R.anim.anim_button_bounce_in);

            AnimateBounceInterpolator animateBounceInterpolator = new AnimateBounceInterpolator(0.2,20);

            mAnimGrid_1.setInterpolator(animateBounceInterpolator);

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    ibGridCommand_1.setVisibility(View.VISIBLE);
                    ibGridCommand_1.startAnimation(mAnimGrid_1);
                }
            },200);

        }else{
            final Animation mAnimGrid_1 = AnimationUtils.loadAnimation(mContext,R.anim.anim_button_bounce_in);


            mAnimGrid_1.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    ibGridCommand_1.setVisibility(View.INVISIBLE);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    ibGridCommand_1.startAnimation(mAnimGrid_1);
                }
            },200);

        }

    }


    private void animatePointOptions(boolean isShow){
        Log.d(TAG, "animatePointOptions: Started");

        if(isShow){
            final Animation mAnim_1 = AnimationUtils.loadAnimation(mContext,R.anim.anim_activity_fade_in);
            final Animation mAnim_2 = AnimationUtils.loadAnimation(mContext,R.anim.anim_activity_fade_in);

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    ibPointOption_1.setVisibility(View.VISIBLE);
                    ibPointOption_1.startAnimation(mAnim_1);
                }
            },50);

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    ibPointOption_2.setVisibility(View.VISIBLE);
                    ibPointOption_2.startAnimation(mAnim_2);
                }
            },50);


        }else {
            final Animation mAnim_1 = AnimationUtils.loadAnimation(mContext,R.anim.anim_button_bounce_out);
            final Animation mAnim_2 = AnimationUtils.loadAnimation(mContext,R.anim.anim_button_bounce_out);

            mAnim_1.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    ibPointOption_1.setVisibility(View.INVISIBLE);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });

            mAnim_2.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    ibPointOption_2.setVisibility(View.INVISIBLE);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    ibPointOption_1.startAnimation(mAnim_1);
                }
            },200);

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    ibPointOption_2.startAnimation(mAnim_2);
                }
            },400);


        }


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

        title.setText(getString(R.string.photo_dialog_title_point));
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

    private void showPhotoGridView(){
        Log.d(TAG, "showPhotoGridView: Started...");
        isLoading = true;

        gridHandler = new Handler();
        gridHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                initPhotoGridView();
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

    private void initPhotoGridView(){
        Log.d(TAG, "initPhotoGridView: Started");

        Log.d(TAG, "Data: ProjectID: " + projectID);
        Log.d(TAG, "Data: JobID: " + jobId);
        Log.d(TAG, "Data: pointID: " + pointId);


        if(getImageCount(projectID, jobId, pointId)){
            Log.d(TAG, "initPhotoGridView: getImageCount = True");
            gridAdapter = new PointGridImageAdapter(mContext, R.layout.layout_grid_imageview, mURLSyntex, getImageFromPointData(projectID, jobId, pointId));
            gridView.setAdapter(gridAdapter);
            animatePhotoCard();
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
            animateSketchCard();
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

    //----------------------------------------------------------------------------------------------//

    private void setPointTypes(){
        Log.d(TAG, "setPointTypes: Started");
        String[] type_values = getResources().getStringArray(R.array.point_type_entries);
        String[] grid_type_values = getResources().getStringArray(R.array.point_type_grid_vs_type);

        int planarType_pos = planarPointType;
        tvPointClassPlanar.setText(type_values[planarType_pos]);

        int geodeticType_pos = geodeticPointType;
        tvPointClassWorld.setText(type_values[geodeticType_pos]);
        tvPointClassGrid.setText(grid_type_values[geodeticType_pos]);

        gridPointType = geodeticPointType;

        tvPointClassPlanar.setVisibility(View.VISIBLE);
        tvPointClassWorld.setVisibility(View.VISIBLE);
        tvPointClassGrid.setVisibility(View.VISIBLE);

    }

    private void setPlanarCoordinateText(double north, double east, double elevation, boolean isDirty){
        Log.d(TAG, "setPlanarCoordinateText: Started");
        tvPointNorth.setText(COORDINATE_FORMATTER.format(north));
        tvPointEast.setText(COORDINATE_FORMATTER.format(east));
        tvPointElev.setText(COORDINATE_FORMATTER.format(elevation));

        tvPointNorth.setVisibility(View.VISIBLE);
        tvPointEast.setVisibility(View.VISIBLE);
        tvPointElev.setVisibility(View.VISIBLE);

        if(isDirty){
            tvPointNorth.setTypeface(null, Typeface.ITALIC);
            tvPointEast.setTypeface(null,Typeface.ITALIC);
            tvPointElev.setTypeface(null,Typeface.ITALIC);
        }
    }

    private void setGeodeticCoordinateText(double latValue, double longValue, boolean isDirty) {
        Log.d(TAG, "setGeodeticCoordinateText: Started");

        if (latValue != 0) {
            tvPointLatHeader.setText(getString(R.string.dialog_point_view_pointLatitude_header));
            tvPointLongHeader.setText(getString(R.string.dialog_point_view_pointLongitude_header));

            tvPointLat.setText(SurveyMathHelper.convertDECtoDMSGeodeticV2(latValue, 4));
            tvPointLong.setText(SurveyMathHelper.convertDECtoDMSGeodeticV2(longValue, 4));

            tvPointLat.setVisibility(View.VISIBLE);
            tvPointLong.setVisibility(View.VISIBLE);

        }

        if (isDirty) {
            tvPointLat.setTypeface(null, Typeface.ITALIC);
            tvPointLong.setTypeface(null, Typeface.ITALIC);

        }
    }

    private void setGeodeticEllipsoidText(double heightEllips, boolean isDirty){
        Log.d(TAG, "setGeodeticEllipsoidText: Started");

        if (heightEllips != 0) {
            tvPointEllipsoidHeader.setText(getString(R.string.dialog_point_view_pointEllipsoid_header));

            tvPointEllipsoid.setText(COORDINATE_FORMATTER.format(heightEllips));

            tvPointEllipsoid.setVisibility(View.VISIBLE);
        }

        if (isDirty) {
            tvPointEllipsoid.setTypeface(null, Typeface.ITALIC);
        }
    }

    private void setGeodeticOrthoText(double heightOrtho, boolean isDirty){
        Log.d(TAG, "setGeodeticOrthoText: Started");

        if (heightOrtho != 0) {
            tvPointOrthoHeader.setText(getString(R.string.dialog_point_view_pointOrtho_header));

            tvPointOrtho.setText(COORDINATE_FORMATTER.format(heightOrtho));

            tvPointOrtho.setVisibility(View.VISIBLE);
        }

        if (isDirty) {
            tvPointOrtho.setTypeface(null, Typeface.ITALIC);
        }


    }

    private void setGridCoordinateText(double north,double east,boolean isDirty){
        Log.d(TAG, "setPlanarCoordinateText: Started");


        COORDINATE_FORMATTER = StringUtilityHelper.createUSNonBiasDecimalFormat();

        tvPointGridNorthHeader.setText(getString(R.string.dialog_job_point_item_header_grid_north_title));
        tvPointGridEastHeader.setText(getString(R.string.dialog_job_point_item_header_grid_east_title));

        tvPointGridNorth.setText(COORDINATE_FORMATTER.format(north));
        tvPointGridEast.setText(COORDINATE_FORMATTER.format(east));

        tvPointGridNorth.setVisibility(View.VISIBLE);
        tvPointGridEast.setVisibility(View.VISIBLE);

        if(isDirty){
            tvPointGridNorth.setTypeface(null, Typeface.ITALIC);
            tvPointGridEast.setTypeface(null,Typeface.ITALIC);
        }
    }

    private void setPointDescriptionText(String description, boolean isDirty){
        Log.d(TAG, "setPointDescriptionText: Started");


        tvPointDesc.setText(description);

        if(isDirty){
            tvPointDesc.setTypeface(null, Typeface.ITALIC);
        }

    }


    //----------------------------------------------------------------------------------------------//

    private void checkPlanarCoordinatesFromDialog(Point point){
        Log.d(TAG, "checkPlanarCoordinatesFromDialog: Started");

        if(isPlanarPointDirty(point)){
            isPlanarDirty = true;
            planarNorthDirty = point.getNorthing();
            planarEastDirty = point.getEasting();
            planarElevationDirty = point.getElevation();
            planarPointType = 1;

            setPlanarCoordinateText(planarNorthDirty, planarEastDirty,planarElevationDirty,true);

        }

    }

    private void checkGeodeticCoordinatesFromDialog(double latOut, double longOut, double heightEllipsOut, double heightOrthoOut){
        Log.d(TAG, "checkGeodeticCoordinatesFromDialog: Started");

        if(isGeodeticPointDirty(latOut, longOut, heightEllipsOut, heightOrthoOut)){
            isGeodeticDirty = true;
            latitudeDirty = latOut;
            longitudeDirty = longOut;
            heightEllipsoidDirty = heightEllipsOut;
            heightOrthoDirty = heightOrthoOut;

            setGeodeticCoordinateText(latitudeValue,longitudeValue,true);
            setGeodeticEllipsoidText(heightEllipsoidValue,true);
            setGeodeticOrthoText(heightOrthoValue,true );

            if(latitudeValue !=0){
                findGridCoordinatesFromWorld(latitudeDirty,longitudeDirty);
            }

            if(!isGridViewShown){
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        animateGridCard();
                        animateGridCommands(true);
                    }
                },750);
            }
        }

    }

    private void checkGridCoordinatesFromDialog(double northOut, double eastOut){
        Log.d(TAG, "checkGridCoordinatesFromDialog: Started");

        if(isGridPointDirty(northOut, eastOut)){
            isGridDirty = true;
            gridNorthDirty = northOut;
            gridEastDirty = eastOut;

            setGridCoordinateText(gridNorthDirty, gridEastDirty,true);

            findWorldCoordinatesFromGrid(gridNorthDirty,gridEastDirty);

        }

    }

    private void checkPointDescriptionFromDialog(String descriptionOut){
        Log.d(TAG, "checkPointDescriptionFromDialog: Started");

        if(isPointDescriptionDirty(descriptionOut)){
            isPointDescriptionDirty = true;
            pointDescriptionDirty = descriptionOut;

            setPointDescriptionText(pointDescriptionDirty,true);

        }


    }

    private boolean isPlanarPointDirty(Point point){
        Log.d(TAG, "isPlanarPointsDirty: Started");

        double mNorth, mEast, mElevation;
        mNorth = point.getNorthing();
        mEast = point.getEasting();
        mElevation = point.getElevation();

        Log.d(TAG, "isPlanarPointDirty: North: " + mNorth + " vs " + planarNorthValue);
        Log.d(TAG, "isPlanarPointDirty: East: " + mEast + " vs " + planarEastValue);

        int dNorth, dEast, dElev;

        //Comparing
        if(!isPlanarDirty){
            dNorth = Double.compare(planarNorthValue,mNorth);
            dEast = Double.compare(planarEastValue, mEast);
            dElev = Double.compare(planarElevationValue, mElevation);
        }else{
            dNorth = Double.compare(planarNorthDirty,mNorth);
            dEast = Double.compare(planarEastDirty, mEast);
            dElev = Double.compare(planarElevationDirty, mElevation);
        }


        Log.d(TAG, "isPlanarPointDirty: delta:"  + dNorth + ", " + dEast + ", " + dElev);


        if(dNorth != 0 || dEast != 0 || dElev != 0){
            Log.d(TAG, "isPlanarPointDirty: Dirty");
            return true;
        }else{
            Log.d(TAG, "isPlanarPointDirty: Not Dirty");
            return false;
        }
    }

    private boolean isGeodeticPointDirty(double latValue, double longValue, double heightEllips, double heightOrtho){
        Log.d(TAG, "isGeodeticPointDirty: Started");

        int dLat, dLong, dEllipsoid, dOrtho;

        if(!isGeodeticDirty){
            dLat = Double.compare(latitudeValue,latValue);
            dLong = Double.compare(longitudeValue,longValue);
            dEllipsoid = Double.compare(heightEllipsoidValue,heightEllips);
            dOrtho = Double.compare(heightOrthoValue,heightOrtho);
        }else{
            dLat = Double.compare(latitudeDirty,latValue);
            dLong = Double.compare(longitudeDirty,longValue);
            dEllipsoid = Double.compare(heightEllipsoidDirty,heightEllips);
            dOrtho = Double.compare(heightOrthoValue,heightOrtho);
        }

        if(dLat !=0 || dLong !=0 || dEllipsoid !=0 || dOrtho !=0){
            Log.d(TAG, "isGeodeticPointDirty: Dirty");
            return true;
        }else{
            Log.d(TAG, "isGeodeticPointDirty: Not Dirty");
            return false;
        }

    }

    private boolean isGridPointDirty(double north, double east){
        Log.d(TAG, "isGridPointDirty: Started");

        int dNorth, dEast;

        //Comparing
        if(!isPlanarDirty){
            dNorth = Double.compare(planarNorthValue,north);
            dEast = Double.compare(planarEastValue, east);
        }else{
            dNorth = Double.compare(planarNorthDirty,north);
            dEast = Double.compare(planarEastDirty, east);

        }

        if(dNorth != 0 || dEast != 0){
            Log.d(TAG, "isPlanarPointDirty: Dirty");
            return true;
        }else{
            Log.d(TAG, "isPlanarPointDirty: Not Dirty");
            return false;
        }
    }

    private boolean isPointDescriptionDirty(String description){
        Log.d(TAG, "isPointDescriptionDirty: Started");

        boolean isTheSame;

        //Comparing
        if(!isPointDescriptionDirty){
            isTheSame = description.equals(pointDescription);
        }else{
            isTheSame = description.equals(pointDescriptionDirty);
        }

        if (isTheSame){
            Log.d(TAG, "isPointDescriptionDirty: Not Dirty");
            return false;
        }else{
            Log.d(TAG, "isPointDescriptionDirty: Dirty");
            return true;
        }

    }

    //----------------------------------------------------------------------------------------------//
    private void createDirtyPointGeodetic(){
        Log.d(TAG, "createDirtyPointGeodetic: Started");
        boolean isDirty = false;

        PointGeodetic dirtyPoint = new PointGeodetic();

        dirtyPoint.setPoint_no(pointNo);

        if(isPlanarDirty){
            dirtyPoint.setNorthing(planarNorthDirty);
            dirtyPoint.setEasting(planarEastDirty);
            dirtyPoint.setElevation(planarElevationDirty);
            dirtyPoint.setPointType(planarPointType);
            isDirty = true;

        }else{
            dirtyPoint.setNorthing(planarNorthValue);
            dirtyPoint.setEasting(planarEastValue);
            dirtyPoint.setElevation(planarElevationValue);
            dirtyPoint.setPointType(planarPointType);
        }

        if(isGeodeticDirty){
            dirtyPoint.setLatitude(latitudeDirty);
            dirtyPoint.setLongitude(longitudeDirty);
            dirtyPoint.setEllipsoid(heightEllipsoidDirty);
            dirtyPoint.setOrtho(heightOrthoDirty);
            dirtyPoint.setPointGeodeticType(geodeticPointTypeDirty);
            isDirty = true;

        }else{
            dirtyPoint.setLatitude(latitudeValue);
            dirtyPoint.setLongitude(longitudeValue);
            dirtyPoint.setEllipsoid(heightEllipsoidValue);
            dirtyPoint.setOrtho(heightOrthoValue);
            dirtyPoint.setPointGeodeticType(geodeticPointType);
        }

        if(isPointDescriptionDirty){
            dirtyPoint.setDescription(pointDescriptionDirty);
            isDirty = true;
        }else{
            Log.d(TAG, "createDirtyPointGeodetic: Not Dirty Point Description");
            dirtyPoint.setDescription(pointDescription);
        }

        dirtyPoint.setDateCreated(creationDate);

        dirtyPoint.setDateModified((int) (new Date().getTime()/1000));

        if(isDirty){
            replacePointGeodeticWithDirtyPoint(dirtyPoint);
        }


    }

    private void replacePointGeodeticWithDirtyPoint(PointGeodetic pointGeodeticIN){
        Log.d(TAG, "replacePointGeodeticWithDirtyPoint: Started");

        JobDatabaseHandler jobDb = new JobDatabaseHandler(mContext, databaseName);
        SQLiteDatabase db = jobDb.getWritableDatabase();

        int results = jobDb.updatePointGeodeticByPointNo(db,pointNo,pointGeodeticIN);

        db.close();

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
