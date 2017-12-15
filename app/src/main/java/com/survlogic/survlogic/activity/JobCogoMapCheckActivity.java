package com.survlogic.survlogic.activity;

import android.app.DialogFragment;
import android.content.Context;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
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

import com.survlogic.survlogic.R;
import com.survlogic.survlogic.background.BackgroundSurveyPointGet;
import com.survlogic.survlogic.background.BackgroundSurveyPointGetForActivity;
import com.survlogic.survlogic.dialog.DialogJobMapCheckPointList;
import com.survlogic.survlogic.dialog.DialogJobPointGeodeticEntryAdd;
import com.survlogic.survlogic.dialog.DialogJobSetupPointList;
import com.survlogic.survlogic.interf.DatabasePointsFromAsyncListener;
import com.survlogic.survlogic.interf.JobCogoMapCheckPointListListener;
import com.survlogic.survlogic.model.JobInformation;
import com.survlogic.survlogic.model.PointGeodetic;
import com.survlogic.survlogic.model.PointSurvey;
import com.survlogic.survlogic.utils.PreferenceLoaderHelper;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by chrisfillmore on 12/13/2017.
 */

public class JobCogoMapCheckActivity extends AppCompatActivity implements DatabasePointsFromAsyncListener, JobCogoMapCheckPointListListener{
    private static final String TAG = "JobCogoMapCheckActivity";
    private Context mContext;

    private int project_id, job_id;
    private String jobDatabaseName;
    private ArrayList<PointSurvey> lstPointSurvey = new ArrayList<>();
    private ArrayList<PointGeodetic> lstPointGeodetic = new ArrayList<>();

    private View vInstructions, vList;
    private RelativeLayout rlOccupyMetadataView;
    private ImageButton ibToolbarBack, ibStartCardExpand, ibtOccupyFromList;
    private Button btToolbarFinish, btAddLegMapCheckStarted, btAddLegMapCheckNew;
    private AutoCompleteTextView tvOccupyPointNo;
    private TextView tvOccupyNorthing, tvOccupyEasting, tvOccupyElevation, tvOccupyDesc;
    private Animation animCard_1_down_btn, animCard_1_up_btn;

    boolean isPointOccupyDataVisible = false;
    boolean isPointsLoaded = false;
    boolean isMapcheckStarted = false;

    private PointSurvey occupyPoint;
    private HashMap<String,PointSurvey> pointMap = new HashMap<>();
    private ArrayList<String> pointListFind = new ArrayList<>();

    private ArrayAdapter<String> pointAdapter;

    private PreferenceLoaderHelper preferenceLoaderHelper;
    private static DecimalFormat COORDINATE_FORMATTER, DISTANCE_PRECISION_FORMATTER;

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
        Button btAddFirstLeg = (Button) findViewById(R.id.add_new_leg);
        btAddFirstLeg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addFirstLegView();

            }
        });

    }

    private void initViewList(){




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
            }


        }

    }

    private void openPointListSelect(boolean isPointOccupy){
        Log.d(TAG, "openPointListSelect: Started...");

        DialogFragment viewDialog = DialogJobMapCheckPointList.newInstance(project_id, job_id, jobDatabaseName, lstPointSurvey, isPointOccupy);
        viewDialog.show(getFragmentManager(),"dialog");


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
}
