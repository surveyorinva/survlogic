package com.survlogic.survlogic.activity;

import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.survlogic.survlogic.R;
import com.survlogic.survlogic.adapter.JobMapCheckObservationsAdaptor;
import com.survlogic.survlogic.background.BackgroundSurveyPointGetForActivity;
import com.survlogic.survlogic.dialog.DialogJobMapCheckPointList;
import com.survlogic.survlogic.dialog.DialogToolsCurveSolve;
import com.survlogic.survlogic.interf.CallCurveSolutionDialogListener;
import com.survlogic.survlogic.interf.DatabasePointsFromAsyncListener;
import com.survlogic.survlogic.interf.JobCogoMapCheckPointListListener;
import com.survlogic.survlogic.interf.MapcheckListener;
import com.survlogic.survlogic.model.CurveSurvey;
import com.survlogic.survlogic.model.PointGeodetic;
import com.survlogic.survlogic.model.PointMapCheck;
import com.survlogic.survlogic.model.PointSurvey;
import com.survlogic.survlogic.utils.PreferenceLoaderHelper;
import com.survlogic.survlogic.utils.SwipeAndDragHelper;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by chrisfillmore on 12/13/2017.
 */

public class JobCogoMapCheckActivity extends AppCompatActivity implements DatabasePointsFromAsyncListener, JobCogoMapCheckPointListListener, MapcheckListener, CallCurveSolutionDialogListener{
    private static final String TAG = "JobCogoMapCheckActivity";
    private Context mContext;

    private int project_id, job_id;
    private String jobDatabaseName;
    private ArrayList<PointSurvey> lstPointSurvey = new ArrayList<>();
    private ArrayList<PointMapCheck> lstPointMapCheck = new ArrayList<>();

    //-Activity-//
    private View vInstructions, vList;
    private RelativeLayout rlOccupyMetadataView;
    private ImageButton ibToolbarBack, ibStartCardExpand, ibtOccupyFromList;
    private Button btToolbarFinish, btAddLegMapCheckStarted, btAddLegMapCheckNew;
    private AutoCompleteTextView tvOccupyPointNo;
    private TextView tvOccupyNorthing, tvOccupyEasting, tvOccupyElevation, tvOccupyDesc;
    private Animation animCard_1_down_btn, animCard_1_up_btn;

    //-List-//
    private RecyclerView mRecyclerViewMapCheck;
    private RecyclerView.LayoutManager layoutManagerMapCheck;
    private JobMapCheckObservationsAdaptor adapterMapCheckListAdd;

    private Button btListAddNewObservation;

    //-Method Variables-//
    private int mValuePointNo;
    private String mValuePointDesc;
    private double mValueDistance;
    private double mValueBearing, mValueAzimuth, mValueTurnedAngle;
    private double mValueCurveDelta, mValueCurveRadius, mValueCurveLength, mValueCurveCB, mValueCurveCH;

    //-Support Variables-//
    boolean isPointOccupyDataVisible = false;
    boolean isPointsLoaded = false;
    boolean isMapcheckStarted = false;
    boolean isFirstTimeLoadNewObservation = true;

    private PointSurvey occupyPoint;
    private HashMap<String,PointSurvey> pointMap = new HashMap<>();
    private ArrayList<String> pointListFind = new ArrayList<>();

    private ArrayAdapter<String> pointAdapter;

    private PreferenceLoaderHelper preferenceLoaderHelper;
    private static DecimalFormat COORDINATE_FORMATTER, DISTANCE_PRECISION_FORMATTER;
    private static final int textValidationOne = 0, textValidationTwo = 1, textValidationThree = 2, textValidationFour = 4;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: Started");
        setContentView(R.layout.activity_job_cogo_mapcheck);
        mContext = JobCogoMapCheckActivity.this;
        preferenceLoaderHelper = new PreferenceLoaderHelper(mContext);

        initViewWidgets();
        initPointDataInBackground();

        setOnClickListeners();
        loadPreferences();

    }


    private void initViewWidgets() {
        Log.d(TAG, "initViewWidgets: Started...");

        Bundle extras = getIntent().getExtras();
        project_id = extras.getInt(getString(R.string.KEY_PROJECT_ID));
        job_id = extras.getInt(getString(R.string.KEY_JOB_ID));
        jobDatabaseName = extras.getString(getString(R.string.KEY_JOB_DATABASE));

        vInstructions = findViewById(R.id.instruction_text);
        vList = findViewById(R.id.listview_mapcheck_items);

        rlOccupyMetadataView = (RelativeLayout) findViewById(R.id.llOccupy_data);

        ibToolbarBack = (ImageButton) findViewById(R.id.button_back);
        ibStartCardExpand = (ImageButton) findViewById(R.id.card_occupy_expand);
        ibtOccupyFromList = (ImageButton) findViewById(R.id.occupy_from_list);

        btToolbarFinish = (Button) findViewById(R.id.button_finish);

        btAddLegMapCheckNew = (Button) findViewById(R.id.add_new_leg);
        btAddLegMapCheckNew.setClickable(false);

        btAddLegMapCheckStarted = (Button) findViewById(R.id.mapcheck_items_btn);

        tvOccupyPointNo = (AutoCompleteTextView) findViewById(R.id.left_item1_value);
        tvOccupyPointNo.setOnFocusChangeListener(focusListener);

        tvOccupyNorthing = (TextView) findViewById(R.id.occupy_Northing);
        tvOccupyEasting = (TextView) findViewById(R.id.occupy_Easting);
        tvOccupyElevation = (TextView) findViewById(R.id.occupy_Elevation);
        tvOccupyDesc = (TextView) findViewById(R.id.occupy_Desc);

        initViewInstructions();
        
        animCard_1_down_btn = AnimationUtils.loadAnimation(mContext,R.anim.rotate_card_down);

        animCard_1_down_btn.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                ibStartCardExpand.setImageResource(R.drawable.ic_keyboard_arrow_up);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        animCard_1_up_btn = AnimationUtils.loadAnimation(mContext, R.anim.rotate_card_up);

        animCard_1_up_btn.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                ibStartCardExpand.setImageResource(R.drawable.ic_keyboard_arrow_down);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

    }

    private void initViewInstructions(){
        Log.d(TAG, "initViewInstructions: Started...");

        Button btAddFirstLeg = (Button) findViewById(R.id.add_new_leg);
        btAddFirstLeg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addFirstLegView();

            }
        });

    }

    private void initViewList(){
        Log.d(TAG, "initViewList: Started...");

        mRecyclerViewMapCheck = (RecyclerView) findViewById(R.id.mapcheck_items_list);
        setMapCheckAdapter();

        btListAddNewObservation = (Button) findViewById(R.id.mapcheck_items_btn);


        if(isFirstTimeLoadNewObservation){
            createArrayListItemToAdd();
            isFirstTimeLoadNewObservation = false;
        }

        btListAddNewObservation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createArrayListItemToAdd();
            }
        });



    }

    private void setOnClickListeners(){
        Log.d(TAG, "setOnClickListeners: Started...");

        ibToolbarBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeActivity();
            }
        });

        ibStartCardExpand.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isPointOccupyDataVisible){
                    ibStartCardExpand.startAnimation(animCard_1_down_btn);

                    rlOccupyMetadataView.setVisibility(View.VISIBLE);
                    isPointOccupyDataVisible = true;

                }else{
                    ibStartCardExpand.startAnimation(animCard_1_up_btn);

                    rlOccupyMetadataView.setVisibility(View.GONE);
                    isPointOccupyDataVisible = false;
                }
            }
        });

        tvOccupyPointNo.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG, "onItemClick: Started...");
                String stringPointNo = pointAdapter.getItem(position);

                setPointSurveyFromPointNo(stringPointNo,true);

                if(isMapcheckStarted){
                    hideKeypadFromAutoCompleteET(tvOccupyPointNo);
                    tvOccupyPointNo.clearFocus();

                    btAddLegMapCheckStarted.requestFocus();

                }else{
                    hideKeypadFromAutoCompleteET(tvOccupyPointNo);
                    tvOccupyPointNo.clearFocus();

                    btAddLegMapCheckNew.requestFocus();
                }

            }
        });

        ibtOccupyFromList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!isPointsLoaded){
                    getPointSurveyArray();
                    isPointsLoaded = true;
                }

                openPointListSelect(true);
            }
        });


        btToolbarFinish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                callFinishActivity();


            }
        });


    }



    //----------------------------------------------------------------------------------------------//

    private void closeActivity(){
        finish();
    }

    private void loadPreferences(){
        Log.d(TAG, "loadPreferences: Started...");

        COORDINATE_FORMATTER = new DecimalFormat(preferenceLoaderHelper.getValueSystemCoordinatesPrecisionDisplay());
        DISTANCE_PRECISION_FORMATTER = new DecimalFormat(preferenceLoaderHelper.getValueSystemDistancePrecisionDisplay());

    }

    private void hideKeypadFromAutoCompleteET (AutoCompleteTextView aedtView) {
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        try{
            imm.hideSoftInputFromWindow(aedtView.getWindowToken(), 0);

        }catch (Exception e){

        }

    }

    private void callFinishActivity(){
        Log.d(TAG, "callFinishActivity: Started");
        //Check to see if start point is setup
        boolean isStartPointLoaded = false, isListLoaded = false;
        boolean isOccupyPointAssigned = validateFormForSetup();

        if(!isOccupyPointAssigned){
            tvOccupyPointNo.setError(getString(R.string.cogo_mapcheck_starting_point_not_set));
        }else{
            isStartPointLoaded  =true;
        }

        //Check to see if lstPointMapCheck has a size
        boolean areThereObjectsInList = validateFormForMapCheckList();

        if(!areThereObjectsInList){
            showToast(getString(R.string.cogo_mapcheck_list_not_added),true);
        }else{
            isListLoaded = true;
        }


        //send to openFinishActivity and let the magic begin!
        if(isStartPointLoaded && isListLoaded) {
            openFinishActivity();
        }

    }

    private void openFinishActivity(){
        Log.d(TAG, "openFinishActivity: Started");
        Intent intentFinish = new Intent(this,JobCogoMapCheckResultsActivity.class);

        intentFinish.putExtra(getString(R.string.KEY_SETUP_OCCUPY_PT),occupyPoint);
        intentFinish.putExtra(getString(R.string.KEY_ARRAY_LIST_MAPCHECK),lstPointMapCheck);
        intentFinish.putExtra(getString(R.string.KEY_PROJECT_ID),project_id);
        intentFinish.putExtra(getString(R.string.KEY_JOB_ID), job_id);
        intentFinish.putExtra(getString(R.string.KEY_JOB_DATABASE), jobDatabaseName);

        startActivity(intentFinish);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }
    //----------------------------------------------------------------------------------------------//

    private void addFirstLegView(){
        vInstructions.setVisibility(View.GONE);
        vList.setVisibility(View.VISIBLE);

        initViewList();
    }

    //----------------------------------------------------------------------------------------------//
    private void initPointDataInBackground(){
        //Point Survey Load
        loadPointSurveyInBackground();


    }

    private void loadPointSurveyInBackground(){
        Log.d(TAG, "loadPointSurveyInBackground: Started...");

        BackgroundSurveyPointGetForActivity backgroundSurveyPointGet = new BackgroundSurveyPointGetForActivity(mContext, jobDatabaseName, this);
        backgroundSurveyPointGet.execute();
    }

    private void getPointSurveyArray(){
        Log.d(TAG, "getPointSurveyArray: Started...");
        Log.i(TAG, "getPointSurveyArray: Size: " + lstPointSurvey.size());


        for(int i=0; i<lstPointSurvey.size(); i++) {
            PointSurvey pointSurvey = lstPointSurvey.get(i);

            String pointListPointNo = Integer.toString(pointSurvey.getPoint_no());

            pointMap.put(pointListPointNo, pointSurvey);

            pointListFind.add(pointListPointNo);

        }

        //Set arraylist to adapters

        pointAdapter = new ArrayAdapter<>(mContext,android.R.layout.simple_list_item_1,pointListFind);

        tvOccupyPointNo.setThreshold(1);
        tvOccupyPointNo.setAdapter(pointAdapter);


    }

    
    private void setMapCheckAdapter(){
        Log.d(TAG, "setMapCheckAdapter: Started");
        layoutManagerMapCheck = new LinearLayoutManager(mContext);
        mRecyclerViewMapCheck.setLayoutManager(layoutManagerMapCheck);
        mRecyclerViewMapCheck.setHasFixedSize(false);

        adapterMapCheckListAdd = new JobMapCheckObservationsAdaptor(mContext,lstPointMapCheck, COORDINATE_FORMATTER, this, this, jobDatabaseName);

        SwipeAndDragHelper swipeAndDragHelper = new SwipeAndDragHelper(adapterMapCheckListAdd);
        ItemTouchHelper touchHelper = new ItemTouchHelper(swipeAndDragHelper);
        adapterMapCheckListAdd.setTouchHelper(touchHelper);
        mRecyclerViewMapCheck.setAdapter(adapterMapCheckListAdd);
        touchHelper.attachToRecyclerView(mRecyclerViewMapCheck);

        Log.e(TAG,"Complete: setMapCheckAdapter");
    }


    //----------------------------------------------------------------------------------------------//
    private void setPointSurveyFromPointNo(String pointNo, boolean isOccupyPoint){
        Log.d(TAG, "setPointSurveyFromPointNo: Started");
        PointSurvey currentPointSurvey;


        if(pointMap.containsKey(pointNo)) {
            currentPointSurvey = pointMap.get(pointNo);

            populateSetup(currentPointSurvey, isOccupyPoint, false);

        }

    }

    private void populateSetup(PointSurvey currentPointSurvey, boolean isOccupyPoint, boolean showPointNo){
        Log.d(TAG, "setPointSurveyFromPointSurvey: Started...");

        double pointNorthing = currentPointSurvey.getNorthing();
        String pointNorthingValue = COORDINATE_FORMATTER.format(pointNorthing);

        double pointEasting = currentPointSurvey.getEasting();
        String pointEastingValue = COORDINATE_FORMATTER.format(pointEasting);

        double pointElevation = currentPointSurvey.getElevation();
        String pointElevationValue = COORDINATE_FORMATTER.format(pointElevation);

        String pointDescription = currentPointSurvey.getDescription();

        if(isOccupyPoint) {

            tvOccupyNorthing.setText(pointNorthingValue);
            tvOccupyEasting.setText(pointEastingValue);
            tvOccupyElevation.setText(pointElevationValue);
            tvOccupyDesc.setText(pointDescription);

            occupyPoint = currentPointSurvey;

            if (showPointNo) {
                tvOccupyPointNo.setText(String.valueOf(currentPointSurvey.getPoint_no()));
                tvOccupyPointNo.setError(null);
            }

            btAddLegMapCheckNew.setClickable(true);
        }


    }

    private void openPointListSelect(boolean isPointOccupy){
        Log.d(TAG, "openPointListSelect: Started...");

        DialogFragment viewDialog = DialogJobMapCheckPointList.newInstance(project_id, job_id, jobDatabaseName, lstPointSurvey, isPointOccupy);
        viewDialog.show(getFragmentManager(),"dialog");


    }

    //----------------------------------------------------------------------------------------------//
    private void createArrayListItemToAdd(){
        Log.d(TAG, "createArrayListItemToAdd: Started");
        Log.d(TAG, "saveObservationToArray: Size: " + lstPointMapCheck.size());

        PointMapCheck mapCheck = new PointMapCheck();

        mapCheck.setObservationType(99);

        saveObservationToArray(mapCheck);

    }

    //-------------------------------------------------------------------------------------------------------------------------//

    /**
     * Validating, creating point and then saving points!
     */
    private boolean validateFormForSetup(){
        Log.d(TAG, "validateFormForSetup: Started...");
        boolean isTheFormReady = true;
        int occupyPointNo = 0;

        if(occupyPoint != null){
            occupyPointNo = occupyPoint.getPoint_no();
        }

        if(occupyPointNo == 0){
            Log.d(TAG, "validateFormForSetup: No Occupy Number, showing toast");
            showToast(getString(R.string.cogo_mapcheck_starting_point_not_set),true);
            isTheFormReady = false;
        }

        return isTheFormReady;

    }

    private boolean validateFormForMapCheckList(){
        Log.d(TAG, "validateFormForMapCheckList: Started");
        boolean isTheFormReady = true;

        int listSize = 0;

        listSize = lstPointMapCheck.size();

        if(listSize == 0){
            isTheFormReady = false;
        }

        return  isTheFormReady;

    }

    private void saveObservationToArray(PointMapCheck mapCheck){
        Log.d(TAG, "saveObservationToArray: Started...");

        lstPointMapCheck.add(mapCheck);
        adapterMapCheckListAdd.notifyDataSetChanged();

        if(!mRecyclerViewMapCheck.isShown() && lstPointMapCheck.size() > 0){
            mRecyclerViewMapCheck.setVisibility(View.VISIBLE);
        }
        Log.d(TAG, "saveObservationToArray: Size: " + lstPointMapCheck.size());


    }

    private void saveObservationToArrayThatExists(PointMapCheck mapCheck, int position){

        lstPointMapCheck.set(position,mapCheck);
        adapterMapCheckListAdd.notifyDataSetChanged();

        if(!mRecyclerViewMapCheck.isShown() && lstPointMapCheck.size() > 0){
            mRecyclerViewMapCheck.setVisibility(View.VISIBLE);
        }
        Log.d(TAG, "saveObservationToArray: Size: " + lstPointMapCheck.size());
    }

    private void cancelObservationFromArrayThatExists(int position){

        lstPointMapCheck.remove(position);
        adapterMapCheckListAdd.notifyDataSetChanged();

    }


    //----------------------------------------------------------------------------------------------//
    private View.OnFocusChangeListener focusListener = new View.OnFocusChangeListener(){
        public void onFocusChange(View v, boolean hasFocus){
            Log.d(TAG, "onFocusChange: Started...");
            if (hasFocus){
                if(!isPointsLoaded){
                    getPointSurveyArray();
                    isPointsLoaded = true;
                }

        }
        }
    };

    @Override
    public void getPointsGeodetic(ArrayList<PointGeodetic> lstPointGeodetics) {

    }

    @Override
    public void getPointsSurvey(ArrayList<PointSurvey> lstPointSurvey) {
        Log.d(TAG, "getPointsSurvey: Started");
        this.lstPointSurvey = lstPointSurvey;
    }

    @Override
    public void onReturnValuesOccupy(PointSurvey pointSurvey) {
        populateSetup(pointSurvey,true,true);
    }

    //MapCheckListener
    @Override
    public void sendNewMapcheckToActivity(PointMapCheck pointMapCheck, int position) {
        Log.d(TAG, "sendNewMapcheckToActivity: Started...");

        saveObservationToArrayThatExists(pointMapCheck, position);

    }

    @Override
    public void deleteNewMapcheckUserCancel(int position) {
        cancelObservationFromArrayThatExists(position);
    }


    @Override
    public void hideKeyboard() {
        hideKeyBoardInFocus();
    }

    @Override
    public ArrayList<PointMapCheck> getPointMapCheck() {
        return lstPointMapCheck;
    }


    @Override
    public void showCurveSolutionDialog(CurveSurvey curveSurvey) {
        DialogToolsCurveSolve dialogToolsCurveSolve = DialogToolsCurveSolve.newInstance(curveSurvey);
        dialogToolsCurveSolve.show(getFragmentManager(),"dialog");
    }

    //----------------------------------------------------------------------------------------------//
    private void showToast(String data, boolean shortTime) {

        if (shortTime) {
            Toast.makeText(mContext, data, Toast.LENGTH_SHORT).show();

        } else{
            Toast.makeText(mContext, data, Toast.LENGTH_LONG).show();

        }
    }

    private void hideKeyBoardInFocus(){
        View view = this.getCurrentFocus();

        if (view != null) {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            try{
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);

            }catch(Exception e){
                showToast("Error-No View Shown",true);
            }
        }
    }


}
