package com.survlogic.survlogic.activity;

import android.app.DialogFragment;
import android.content.Context;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.survlogic.survlogic.R;
import com.survlogic.survlogic.adapter.JobMapCheckObservationsAdaptor;
import com.survlogic.survlogic.adapter.JobMapCheckObservationsListAdaptor;
import com.survlogic.survlogic.adapter.JobSideshotPointListAdaptor;
import com.survlogic.survlogic.background.BackgroundSurveyPointGet;
import com.survlogic.survlogic.background.BackgroundSurveyPointGetForActivity;
import com.survlogic.survlogic.dialog.DialogJobMapCheckPointList;
import com.survlogic.survlogic.dialog.DialogJobPointGeodeticEntryAdd;
import com.survlogic.survlogic.dialog.DialogJobSetupPointList;
import com.survlogic.survlogic.dialog.DialogJobSideshotPointList;
import com.survlogic.survlogic.dialog.DialogProjectDescriptionAdd;
import com.survlogic.survlogic.interf.DatabasePointsFromAsyncListener;
import com.survlogic.survlogic.interf.JobCogoMapCheckPointListListener;
import com.survlogic.survlogic.interf.MapcheckListener;
import com.survlogic.survlogic.model.JobInformation;
import com.survlogic.survlogic.model.PointGeodetic;
import com.survlogic.survlogic.model.PointMapCheck;
import com.survlogic.survlogic.model.PointSurvey;
import com.survlogic.survlogic.utils.MathHelper;
import com.survlogic.survlogic.utils.PreferenceLoaderHelper;
import com.survlogic.survlogic.utils.StringUtilityHelper;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by chrisfillmore on 12/13/2017.
 */

public class JobCogoMapCheckActivity extends AppCompatActivity implements DatabasePointsFromAsyncListener, JobCogoMapCheckPointListListener, MapcheckListener{
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
    private Button btToolbarFinish, btAddLegMapCheckStarted, btAddLegMapCheckNew, btCancelLegMapCheckNew;
    private AutoCompleteTextView tvOccupyPointNo;
    private TextView tvOccupyNorthing, tvOccupyEasting, tvOccupyElevation, tvOccupyDesc;
    private Animation animCard_1_down_btn, animCard_1_up_btn;

    //-List-//
    private RecyclerView mRecyclerViewMapCheck;
    private RecyclerView.LayoutManager layoutManagerMapCheck;
    private JobMapCheckObservationsListAdaptor adapterMapCheck;
    private JobMapCheckObservationsAdaptor adapterMapCheckListAdd;

    private View vListAddNewObservation;
    private Button btListAddNewObservation;


    //-Add New Observation-//
    private View vListAdd_ha_bearing, vListAdd_ha_turnedAngle, vListAdd_ha_azimuth,
                vListAdd_curve_da_r, vListAdd_curve_da_l, vListAdd_curve_r_l, vListAdd_curve_cb_ch;

    private EditText etMapCheckPointDescription, etMapCheckPointNumber;
    private Switch switchTypeOfObservation;
    private ImageButton ibSwapObservation;
    private Button btSaveObservation, btCancelObservation;


    private int viewCurrentTypeToDisplay = 0;
    private int popupMenuOpen = 0;

    private boolean isHaAzimuthInit = false, isHaBearingInit = false, isHaTurnedInit = false;
    private boolean isCurveDARInit = false, isCurveDALInit = false, isCurveRLInit = false;
    private boolean isCurveCBCHInit = false;

    private EditText etBearingQuadrant, etBearingDeg, etBearingMin, etBearingSec, etBearingDistance;
    private EditText etAzimuthDeg, etAzimuthMin, etAzimuthSec, etAzimuthDistance;
    private EditText etTurnedDeg, etTurnedMin, etTurnedSec, etTurnedDistance;
    private EditText etCurveADeltaDeg, etCurveADeltaMin, etCurveADeltaSec, etCurveARadius;
    private EditText etCurveBDeltaDeg,etCurveBDeltaMin, etCurveBDeltaSec, etCurveBLength;
    private EditText etCurveCRadius, etCurveCLength;
    private EditText etCurveDCBQuadrant, etCurveDCBDeg, etCurveDCBMin, etCurveDCBSec, etCurveDCH;

    private Switch switchCurveAIsRight;
    private Switch switchCurveBIsRight;
    private Switch switchCurveCIsRight;
    private Switch switchCurveDIsRight;

    private Button btCurveASolve, btCurveBSolve, btCurveCSolve, btCurveDSolve;

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
        setMapCheckListAdapter();
        //setMapCheckAdapter();

        vListAddNewObservation = findViewById(R.id.new_leg);

        btListAddNewObservation = (Button) findViewById(R.id.mapcheck_items_btn);

        if(isFirstTimeLoadNewObservation){
            showAddObservation(true);
        }

        btListAddNewObservation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddObservation(true);
            }
        });



    }

    private void initVewListAddNewObservation(){
        Log.d(TAG, "initVewListAddNewObservation: Started");

        vListAdd_ha_bearing = findViewById(R.id.layout_ha_bearing);
        vListAdd_ha_turnedAngle = findViewById(R.id.layout_ha_turned_angle);
        vListAdd_ha_azimuth = findViewById(R.id.layout_ha_azimuth);

        vListAdd_curve_da_r = findViewById(R.id.layout_curve_delta_radius);
        vListAdd_curve_da_l = findViewById(R.id.layout_curve_delta_length);
        vListAdd_curve_r_l = findViewById(R.id.layout_curve_radius_length);
        vListAdd_curve_cb_ch = findViewById(R.id.layout_curve_bearing_chord);
        
        switchTypeOfObservation = (Switch) findViewById(R.id.type_of_measurement_switch);
        ibSwapObservation = (ImageButton) findViewById(R.id.typeOfObservation);


        ibSwapObservation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupMenuOpen = 1;

                PopupMenu popupMenuObsType = new PopupMenu(mContext, ibSwapObservation);
                popupMenuObsType.inflate(R.menu.popup_cogo_mapcheck_observation_type);

                switch(viewCurrentTypeToDisplay){

                    case 0:
                        popupMenuObsType.getMenu().findItem(R.id.mapcheck_item_1).setChecked(true);
                        break;

                    case 1:
                        popupMenuObsType.getMenu().findItem(R.id.mapcheck_item_2).setChecked(true);
                        break;

                    case 2:
                        popupMenuObsType.getMenu().findItem(R.id.mapcheck_item_3).setChecked(true);
                        break;

                    case 3:
                        popupMenuObsType.getMenu().findItem(R.id.mapcheck_item_4).setChecked(true);
                        break;

                    case 4:
                        popupMenuObsType.getMenu().findItem(R.id.mapcheck_item_5).setChecked(true);
                        break;

                    case 5:
                        popupMenuObsType.getMenu().findItem(R.id.mapcheck_item_6).setChecked(true);
                        break;

                    case 6:
                        popupMenuObsType.getMenu().findItem(R.id.mapcheck_item_7).setChecked(true);
                        break;

                    default:
                        break;

                }


                popupMenuObsType.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {

                        switch (item.getItemId()){
                            case R.id.mapcheck_item_1:
                                //Swap to Line by Bearing
                                viewCurrentTypeToDisplay = 0;
                                swapTypeOfObservation(0);

                                break;

                            case R.id.mapcheck_item_2:
                                //Swap to Line by Turned Angle
                                viewCurrentTypeToDisplay = 1;
                                swapTypeOfObservation(1);
                                break;

                            case R.id.mapcheck_item_3:
                                //Swap to Line by Azimuth
                                viewCurrentTypeToDisplay = 2;
                                swapTypeOfObservation(2);
                                break;

                            case R.id.mapcheck_item_4:
                                //Swap to Curve Delta-Radius
                                viewCurrentTypeToDisplay = 3;
                                swapTypeOfObservation(3);
                                break;

                            case R.id.mapcheck_item_5:
                                //Swap to Curve Delta-Arc Length
                                viewCurrentTypeToDisplay = 4;
                                swapTypeOfObservation(4);

                                break;

                            case R.id.mapcheck_item_6:
                                //Swap to Curve Radius-Arc Length
                                viewCurrentTypeToDisplay = 5;
                                swapTypeOfObservation(5);

                                break;

                            case R.id.mapcheck_item_7:
                                //Swap to Curve Chord Bearing-Chord Distance
                                viewCurrentTypeToDisplay = 6;
                                swapTypeOfObservation(6);

                                break;
                        }

                        return true;
                    }
                });

                popupMenuObsType.show();
            }
        });

        etMapCheckPointDescription = (EditText) findViewById(R.id.point_Description_value);

        etMapCheckPointNumber = (EditText) findViewById(R.id.point_number);
        etMapCheckPointNumber.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if(actionId == EditorInfo.IME_ACTION_DONE){
                    saveObservation();
                }

                return false;
            }
        });

        btSaveObservation = (Button) findViewById(R.id.save_observation_button);
        btSaveObservation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveObservation();
            }
        });

        btCancelObservation = (Button) findViewById(R.id.cancel_observation_button);
        btCancelObservation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearHaBearing();
                showAddObservation(false);
            }
        });
    }

    private void initLayoutHaBearing(){

        if(!isHaBearingInit){
            etBearingQuadrant = (EditText) findViewById(R.id.hAngle_bearing_quadrant);
            etBearingQuadrant.setSelectAllOnFocus(true);

            etBearingDeg = (EditText) findViewById(R.id.hAngle_bearing_degree);
            etBearingDeg.setSelectAllOnFocus(true);

            etBearingMin = (EditText) findViewById(R.id.hAngle_bearing_min);
            etBearingMin.setSelectAllOnFocus(true);

            etBearingSec = (EditText) findViewById(R.id.hAngle_bearing_sec);
            etBearingSec.setSelectAllOnFocus(true);

            etBearingDistance = (EditText) findViewById(R.id.ha_bearing_distance);
            etBearingDistance.setNextFocusDownId(R.id.point_Description_value);
            etBearingDistance.setSelectAllOnFocus(true);

            isHaBearingInit = true;

            etBearingQuadrant.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    if(etBearingQuadrant.getText().toString().length()==textValidationOne){
                        etBearingDeg.requestFocus();
                    }
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    try{
                        int val = Integer.parseInt(s.toString());
                        if(val > 4){
                            etBearingQuadrant.setError(getResources().getString(R.string.cogo_mapcheck_quandarnt_out_of_range));
                            btSaveObservation.setClickable(false);
                        }else{
                            btSaveObservation.setClickable(true);
                        }


                    }catch (NumberFormatException ex){

                    }
                }
            });

            etBearingDeg.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    if(etBearingDeg.getText().toString().length()==textValidationTwo){
                        etBearingMin.requestFocus();
                    }
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    try{
                        int val = Integer.parseInt(s.toString());
                        if(val > 90){
                            etBearingDeg.setError(getResources().getString(R.string.cogo_angle_error_out_of_range));
                            btSaveObservation.setClickable(false);
                        }else{
                            btSaveObservation.setClickable(true);
                        }


                    }catch (NumberFormatException ex){

                    }
                }
            });

            etBearingMin.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    if(etBearingMin.getText().toString().length()==textValidationTwo){
                        etBearingSec.requestFocus();
                    }
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    try{
                        int val = Integer.parseInt(s.toString());
                        if(val > 59.999){
                            etBearingMin.setError(getResources().getString(R.string.cogo_angle_error_out_of_range));
                            btSaveObservation.setClickable(false);
                        }else{
                            btSaveObservation.setClickable(true);
                        }


                    }catch (NumberFormatException ex){

                    }
                }
            });

            etBearingSec.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    try{
                        int val = Integer.parseInt(s.toString());
                        if(val > 59.999){
                            etBearingSec.setError(getResources().getString(R.string.cogo_angle_error_out_of_range));
                            btSaveObservation.setClickable(false);
                        }else{
                            btSaveObservation.setClickable(true);
                        }


                    }catch (NumberFormatException ex){

                    }
                }
            });
        }else{
            clearHaBearing();

        }


    }

    private void initLayoutHaAzimuth(){

        if(!isHaAzimuthInit){
            etAzimuthDeg = (EditText) findViewById(R.id.hAngle_azimuth_degree);
            etAzimuthDeg.setSelectAllOnFocus(true);

            etAzimuthMin = (EditText) findViewById(R.id.hAngle_azimuth_min);
            etAzimuthMin.setSelectAllOnFocus(true);

            etAzimuthSec = (EditText) findViewById(R.id.hAngle_azimuth_sec);
            etAzimuthSec.setSelectAllOnFocus(true);

            etAzimuthDistance = (EditText) findViewById(R.id.ha_azimuth_distance);
            etAzimuthDistance.setSelectAllOnFocus(true);
            etAzimuthDistance.setNextFocusDownId(R.id.point_Description_value);

            isHaAzimuthInit = true;
        }

    }

    private void initLayoutHaTurnedAngle(){

        if(!isHaTurnedInit){
            etTurnedDeg = (EditText) findViewById(R.id.hAngle_tangle_degree);
            etTurnedDeg.setSelectAllOnFocus(true);

            etTurnedMin = (EditText) findViewById(R.id.hAngle_tangle_min);
            etTurnedMin.setSelectAllOnFocus(true);

            etTurnedSec = (EditText) findViewById(R.id.hAngle_tangle_sec);
            etTurnedSec.setSelectAllOnFocus(true);

            etTurnedDistance = (EditText) findViewById(R.id.ha_tangle_distance);
            etTurnedDistance.setSelectAllOnFocus(true);
            etTurnedDistance.setNextFocusDownId(R.id.point_Description_value);

            isHaTurnedInit = true;
        }

    }

    private void initLayoutCurveDeltaAndRadius(){

        if(!isCurveDARInit){
            etCurveADeltaDeg = (EditText) findViewById(R.id.dr_delta_degree);
            etCurveADeltaDeg.setSelectAllOnFocus(true);

            etCurveADeltaMin = (EditText) findViewById(R.id.dr_delta_min);
            etCurveADeltaMin.setSelectAllOnFocus(true);

            etCurveADeltaSec = (EditText) findViewById(R.id.dr_delta_sec);
            etCurveADeltaSec.setSelectAllOnFocus(true);

            etCurveARadius = (EditText) findViewById(R.id.dr_curve_radius);
            etCurveARadius.setSelectAllOnFocus(true);

            switchCurveAIsRight = (Switch)  findViewById(R.id.dr_switch_direction);

            btCurveASolve = (Button) findViewById(R.id.curve_delta_radius_solve);
            isCurveDARInit = true;
        }

    }

    private void initLayoutCurveDeltaAndLength(){

        if(!isCurveDALInit){
            etCurveBDeltaDeg = (EditText) findViewById(R.id.dl_delta_degree);
            etCurveBDeltaDeg.setSelectAllOnFocus(true);

            etCurveBDeltaMin = (EditText) findViewById(R.id.dl_delta_min);
            etCurveBDeltaMin.setSelectAllOnFocus(true);

            etCurveBDeltaSec = (EditText) findViewById(R.id.dl_delta_sec);
            etCurveBDeltaSec.setSelectAllOnFocus(true);

            etCurveBLength = (EditText) findViewById(R.id.dl_curve_length);
            etCurveBLength.setSelectAllOnFocus(true);

            switchCurveBIsRight = (Switch)  findViewById(R.id.dl_switch_direction);

            btCurveBSolve = (Button) findViewById(R.id.curve_delta_length_solve);
            isCurveDALInit = true;
        }

    }

    private void initLayoutCurveRadiusAndLength(){

        if(!isCurveRLInit){
            etCurveCRadius = (EditText) findViewById(R.id.rl_curve_radius);
            etCurveCRadius.setSelectAllOnFocus(true);

            etCurveCLength = (EditText) findViewById(R.id.rl_curve_length);
            etCurveCLength.setSelectAllOnFocus(true);

            switchCurveCIsRight = (Switch)  findViewById(R.id.rl_switch_direction);

            btCurveCSolve = (Button) findViewById(R.id.curve_radius_length_solve);
            isCurveRLInit = true;
        }

    }

    private void initLayoutCurveBearingAndDistance(){

        if(!isCurveCBCHInit){
            etCurveDCBQuadrant = (EditText) findViewById(R.id.cb_curve_bearing_quadrant);
            etCurveDCBQuadrant.setSelectAllOnFocus(true);

            etCurveDCBDeg = (EditText) findViewById(R.id.cb_curve_bearing_deg);
            etCurveDCBDeg.setSelectAllOnFocus(true);

            etCurveDCBMin = (EditText) findViewById(R.id.cb_curve_bearing_min);
            etCurveDCBMin.setSelectAllOnFocus(true);

            etCurveDCBSec = (EditText) findViewById(R.id.cb_curve_bearing_sec);
            etCurveDCBSec.setSelectAllOnFocus(true);

            etCurveDCH = (EditText) findViewById(R.id.cb_curve_chord_distance);
            etCurveDCH.setSelectAllOnFocus(true);

            switchCurveDIsRight = (Switch)  findViewById(R.id.cb_switch_direction);

            btCurveDSolve = (Button) findViewById(R.id.curve_cb_ch_solve);
            isCurveCBCHInit = true;
        }

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


    private void setMapCheckListAdapter(){
        Log.d(TAG, "setMapCheckListAdapter: Started...");

        layoutManagerMapCheck = new LinearLayoutManager(mContext);
        mRecyclerViewMapCheck.setLayoutManager(layoutManagerMapCheck);
        mRecyclerViewMapCheck.setHasFixedSize(false);

        adapterMapCheck = new JobMapCheckObservationsListAdaptor(mContext,lstPointMapCheck, COORDINATE_FORMATTER);
        mRecyclerViewMapCheck.setAdapter(adapterMapCheck);

        Log.e(TAG,"Complete: setMapCheckListAdapter");

    }
    
    private void setMapCheckAdapter(){
        Log.d(TAG, "setMapCheckAdapter: Started");
        layoutManagerMapCheck = new LinearLayoutManager(mContext);
        mRecyclerViewMapCheck.setLayoutManager(layoutManagerMapCheck);
        mRecyclerViewMapCheck.setHasFixedSize(false);

        adapterMapCheckListAdd = new JobMapCheckObservationsAdaptor(mContext,lstPointMapCheck, COORDINATE_FORMATTER, this);
        mRecyclerViewMapCheck.setAdapter(adapterMapCheckListAdd);

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
    private void showAddObservation(boolean showView){

        //change
        if(showView){
            vListAddNewObservation.setVisibility(View.VISIBLE);
        }else{
            vListAddNewObservation.setVisibility(View.GONE);
        }

        if(isFirstTimeLoadNewObservation){
            initVewListAddNewObservation();

            isFirstTimeLoadNewObservation = false;
        }

    }


    private void swapTypeOfObservation(int viewToShow){
        Log.d(TAG, "swapTypeOfObservation: Started");

        vListAdd_ha_bearing.setVisibility(View.GONE);
        vListAdd_ha_turnedAngle.setVisibility(View.GONE);
        vListAdd_ha_azimuth.setVisibility(View.GONE);
        vListAdd_curve_da_r.setVisibility(View.GONE);
        vListAdd_curve_da_l.setVisibility(View.GONE);
        vListAdd_curve_r_l.setVisibility(View.GONE);
        vListAdd_curve_cb_ch.setVisibility(View.GONE);

        switch (viewToShow){
            case 0:
                vListAdd_ha_bearing.setVisibility(View.VISIBLE);
                initLayoutHaBearing();
                break;

            case 1:
                vListAdd_ha_turnedAngle.setVisibility(View.VISIBLE);
                initLayoutHaTurnedAngle();
                break;

            case 2:
                vListAdd_ha_azimuth.setVisibility(View.VISIBLE);
                initLayoutHaAzimuth();
                break;

            case 3:
                vListAdd_curve_da_r.setVisibility(View.VISIBLE);
                initLayoutCurveDeltaAndRadius();
                break;


            case 4:
                vListAdd_curve_da_l.setVisibility(View.VISIBLE);
                initLayoutCurveDeltaAndLength();
                break;

            case 5:
                vListAdd_curve_r_l.setVisibility(View.VISIBLE);
                initLayoutCurveRadiusAndLength();
                break;

            case 6:
                vListAdd_curve_cb_ch.setVisibility(View.VISIBLE);
                initLayoutCurveBearingAndDistance();
                break;

        }

    }

    private void clearHaBearing(){
        etBearingQuadrant.setText("");
        etBearingDeg.setText("");
        etBearingMin.setText("");
        etBearingSec.setText("");
        etBearingDistance.setText("");

        etBearingQuadrant.requestFocus();
    }

    //-------------------------------------------------------------------------------------------------------------------------//

    /**
     * Validating, creating point and then saving points!
     */

    private void saveObservation(){
        Log.d(TAG, "btSave_onClick ");
        boolean isFormReady = false;
        boolean isFormInError = validateFormForNull();

        Log.d(TAG, "btSave_onClick:isFormInError: " + isFormInError);

        if(!isFormInError){
            saveObservationFromValues();
        }
    }

    private boolean validateFormForNull() {
        Log.d(TAG, "validateForm: Started...");
        boolean isThereAnError = false;

        //Point No.
        String pointNo = null;
        pointNo = etMapCheckPointNumber.getText().toString();

        if(!StringUtilityHelper.isStringNull(pointNo)){
            mValuePointNo = Integer.parseInt(pointNo);
        }else{
            etMapCheckPointNumber.setError(getResources().getString(R.string.cogo_point_no_not_entered));
            isThereAnError = true;
        }

        //Description
        String pointDescription = null;
        pointDescription = etMapCheckPointDescription.getText().toString();

        if(!StringUtilityHelper.isStringNull(pointDescription)){
            mValuePointDesc = pointDescription;
        }else{
            etMapCheckPointDescription.setError(getResources().getString(R.string.cogo_description_not_entered));
            isThereAnError = true;
        }

        //Observation
        switch(viewCurrentTypeToDisplay){

            case 0: // Bearing: D-M-S
                String q, d, m, s;

                q = etBearingQuadrant.getText().toString();
                d = etBearingDeg.getText().toString();
                m = etBearingMin.getText().toString();
                s = etBearingSec.getText().toString();

                if(!StringUtilityHelper.isStringNull(d) && !StringUtilityHelper.isStringNull(m) && !StringUtilityHelper.isStringNull(s)){
                    //Save horizontal angle as a decimal degree
                    mValueBearing = MathHelper.convertPartsToDEC(d,m,s);
                    mValueBearing = mValueBearing + (Double.parseDouble(q) * 100);

                }else {
                    if(StringUtilityHelper.isStringNull(q)) {
                        etBearingQuadrant.setError(getResources().getString(R.string.cogo_ha_quadrant_not_entered));
                        isThereAnError = true;
                    }


                    if(StringUtilityHelper.isStringNull(d)) {
                        etBearingDeg.setError(getResources().getString(R.string.cogo_ha_deg_not_entered));
                        isThereAnError = true;
                    }

                    if(StringUtilityHelper.isStringNull(m)) {
                        etBearingMin.setError(getResources().getString(R.string.cogo_ha_min_not_entered));
                        isThereAnError = true;

                    }
                    if(StringUtilityHelper.isStringNull(s)) {
                        etBearingSec.setError(getResources().getString(R.string.cogo_ha_sec_not_entered));
                        isThereAnError = true;
                    }
                }

                String hDistance;
                hDistance = etBearingDistance.getText().toString();

                if(!StringUtilityHelper.isStringNull(hDistance)){
                    //Save horizontal angle as a decimal degree
                    mValueDistance = Double.parseDouble(hDistance);
                }else{
                    etBearingDistance.setError(getResources().getString(R.string.cogo_distance_not_entered));
                    isThereAnError = true;
                }

                break;

            case 1:

                break;

            case 2:

                break;

            case 3:

                break;

            case 4:

                break;

            case 5:

                break;

            case 6:

                break;

            default:
                break;





        }


        return isThereAnError;
    }

    private boolean validateFormForSetup(){
        Log.d(TAG, "validateFormForSetup: Started...");
        boolean isTheFormReady = true;
        int occupyPointNo = 0;

        if(occupyPoint != null){
            occupyPointNo = occupyPoint.getPoint_no();
        }

        if(occupyPointNo == 0){
            Log.d(TAG, "validateFormForSetup: No Occupy Number, showing toast");
            showToast(getString(R.string.cogo_setup_no_occupy_backsight),true);
            isTheFormReady = false;
        }

        return isTheFormReady;

    }


    private void saveObservationFromValues() {
        Log.d(TAG, "saveObservationToArray: Started...");
        Log.d(TAG, "saveObservationToArray: Size: " + lstPointMapCheck.size());

        PointMapCheck mapCheck = new PointMapCheck();

        switch (viewCurrentTypeToDisplay) {
            case 0: // Bearing: D-M-S
                mapCheck.setObservationType(0);
                mapCheck.setLineAngle(mValueBearing);
                mapCheck.setLineDistance(mValueDistance);
                mapCheck.setPointDescription(mValuePointDesc);
                mapCheck.setNewPointNo(mValuePointNo);

                break;

            default:
                break;
        }

        saveObservationToArray(mapCheck);

    }
    private void saveObservationToArray(PointMapCheck mapCheck){
        Log.d(TAG, "saveObservationToArray: Started...");

        lstPointMapCheck.add(mapCheck);
        adapterMapCheck.notifyDataSetChanged();

        if(!mRecyclerViewMapCheck.isShown() && lstPointMapCheck.size() > 0){
            mRecyclerViewMapCheck.setVisibility(View.VISIBLE);
        }
        Log.d(TAG, "saveObservationToArray: Size: " + lstPointMapCheck.size());

        showAddObservation(false);
    }

    private void saveObservationToArrayThatExists(PointMapCheck mapCheck, int position){

        lstPointMapCheck.set(position,mapCheck);
        adapterMapCheck.notifyDataSetChanged();

        if(!mRecyclerViewMapCheck.isShown() && lstPointMapCheck.size() > 0){
            mRecyclerViewMapCheck.setVisibility(View.VISIBLE);
        }
        Log.d(TAG, "saveObservationToArray: Size: " + lstPointMapCheck.size());
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

    //----------------------------------------------------------------------------------------------//
    private void showToast(String data, boolean shortTime) {

        if (shortTime) {
            Toast.makeText(mContext, data, Toast.LENGTH_SHORT).show();

        } else{
            Toast.makeText(mContext, data, Toast.LENGTH_LONG).show();

        }
    }
}
