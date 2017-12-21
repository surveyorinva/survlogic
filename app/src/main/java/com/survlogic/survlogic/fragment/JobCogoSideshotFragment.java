package com.survlogic.survlogic.fragment;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.survlogic.survlogic.R;
import com.survlogic.survlogic.background.BackgroundPointSurveyNewFromObservation;
import com.survlogic.survlogic.background.BackgroundSurveyPointCheckPointNumber;
import com.survlogic.survlogic.background.BackgroundSurveyPointFindNextNumber;
import com.survlogic.survlogic.dialog.DialogJobSideshotPointList;
import com.survlogic.survlogic.interf.JobCogoFragmentListener;
import com.survlogic.survlogic.interf.JobCogoSideshotPointListener;
import com.survlogic.survlogic.model.Point;
import com.survlogic.survlogic.model.PointSurvey;
import com.survlogic.survlogic.utils.MathHelper;
import com.survlogic.survlogic.utils.PreferenceLoaderHelper;
import com.survlogic.survlogic.utils.StringUtilityHelper;

import java.util.ArrayList;

/**
 * Created by chrisfillmore on 5/2/2017.
 */

public class JobCogoSideshotFragment extends Fragment implements JobCogoSideshotPointListener{

    private static final String TAG = "JobCogoSideshotFragment";

    private PreferenceLoaderHelper preferenceLoaderHelper;
    private View v;
    private Context mContext;

    private static int project_id, job_id;
    private String jobDatabaseName;

    private View viewHAngleDec, viewHAngleDMS, viewHd, viewSdZenith, viewSdVertical, viewSdVDelta;
    private Button btSave;
    private ImageButton ibPointNoAction, ibPointhAngleAction, ibPointDistanceAction;
    private TextView tvHAngleHeader, tvDistanceHeader;
    private TextView tvSwitchTraverse, tvSwitchSideshot;

    private EditText etPointNumber, etTargetHeight, etPointDescription;
    private EditText etHADeg, etHAMin, etHASec, etHADec;
    private EditText etDistanceHD, etDistanceVDZenith, etDistanceVDVertical, etDistanceVDDelta;
    private EditText etVDDeg, etVDMin, etVDSec, etZNDeg, etZNMin, etZNSec, etVDDelta;

    private Switch switchTypeOfSurvey;
    private Boolean isTypeOfSurveySideshot = true;

    private ArrayList<PointSurvey> lstPointSurvey = new ArrayList<>();

    private int mValuePointNo;
    private double mValueHAngleDec, mValueDistance, mValueSlope, mValueVAngleDec, mValueVDelta;
    private double mValueTargetHeight;
    private String mValuePointDesc;

    private boolean doesPointExist = false;
    private int popupMenuOpen = 0;

    private boolean is2dSurvey = false;

    private int horizontalAngleType = 0;
    private int viewDistanceToDisplay = 0;
    private int enteredPointNumber, nextPointNumber;
    private static final int textValidationTwo = 1, textValidationThree = 2, textValidationFour = 4;

    private PointSurvey occupyPointSurvey, backsightPointSurvey;

    private JobCogoSideshotPointListener jobCogoSideshotPointListener;
    private JobCogoFragmentListener jobCogoFragmentListener;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.fragment_job_cogo_sideshot, container, false);

        mContext = getActivity();
        preferenceLoaderHelper = new PreferenceLoaderHelper(mContext);

        initViewWidgets(v);
        initSettingsFromPreferences();
        setOnClickListener(v);


        return v;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Log.d(TAG, "onAttach: Started...");
        jobCogoSideshotPointListener = (JobCogoSideshotPointListener) mContext;


    }

    private void initViewWidgets(View v){
        Log.d(TAG, "initViewWidgets: Started...");

        Bundle extras = getArguments();
        project_id = extras.getInt(getString(R.string.KEY_PROJECT_ID));
        job_id = extras.getInt(getString(R.string.KEY_JOB_ID));
        jobDatabaseName = extras.getString(getString(R.string.KEY_JOB_DATABASE));
        Log.d(TAG, "||Database_fragment_cogo_home|| : " + jobDatabaseName);

        viewHAngleDec = v.findViewById(R.id.layout_ha_dec);
        viewHAngleDMS = v.findViewById(R.id.layout_ha_dms);

        viewHd = v.findViewById(R.id.layout_hd);
        viewSdZenith = v.findViewById(R.id.layout_sd_zenith);
        viewSdVertical = v.findViewById(R.id.layout_sd_vertical_angle);
        viewSdVDelta = v.findViewById(R.id.layout_sd_vertical_difference);

        btSave = (Button) v.findViewById(R.id.Save_button);

        ibPointNoAction = (ImageButton) v.findViewById(R.id.point_Number_action);
        ibPointhAngleAction = (ImageButton) v.findViewById(R.id.typeOfObservation);
        ibPointDistanceAction = (ImageButton) v.findViewById(R.id.distance_action);

        etPointNumber = (EditText) v.findViewById(R.id.point_number);
        etPointNumber.setSelectAllOnFocus(true);

        etTargetHeight = (EditText) v.findViewById(R.id.target_height);
        etTargetHeight.setSelectAllOnFocus(true);

        etPointDescription = (EditText) v.findViewById(R.id.point_Description_value);

        tvHAngleHeader = (TextView) v.findViewById(R.id.hAngle_header);
        tvDistanceHeader = (TextView) v.findViewById(R.id.distance_header);

        tvSwitchTraverse = (TextView) v.findViewById(R.id.switch_traverse_text);
        tvSwitchSideshot = (TextView) v.findViewById(R.id.switch_sideshot_text);

        switchTypeOfSurvey = (Switch) v.findViewById(R.id.type_of_measurement_switch);

        jobCogoFragmentListener = (JobCogoFragmentListener) getActivity();

        initViewHADMS();
        initViewHADEC();

        initViewVDHorizontal();
        initViewVDSlopeZenith();
        initViewVDSlopeVertical();
        initViewVDSlopeDelta();

        etPointNumber.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

                try{
                    String pointNo = null;
                    pointNo = etPointNumber.getText().toString();

                    if(!StringUtilityHelper.isStringNull(pointNo)){
                        mValuePointNo = Integer.parseInt(pointNo);
                        validatePointNumber(mValuePointNo);
                    }

                }catch(NumberFormatException ex){
                    showToast("Error.  Check Number Format", true);

                }
            }
        });

    }

    private void initSettingsFromPreferences() {
        Log.d(TAG, "initSettingsFromPreferences: Started...");

        //determine if dms or dec
        horizontalAngleType = preferenceLoaderHelper.getSurveyFormatAngleHZ();

        if (horizontalAngleType == 0) {
            swapHAngleItems(1);
        } else if (horizontalAngleType == 1){
            swapHAngleItems(2);

        }

        //determine if 2d or 3d survey type.  Set HDistance if in 2d
        int surveyType = preferenceLoaderHelper.getSurveyGeneralType();

        if(surveyType == 0){
            //2d Survey
            preferenceLoaderHelper.setCogoSurveyDistance(1);
            is2dSurvey = true;
            viewDistanceToDisplay = 1;

            swapDistanceItems(1);

        }else{
            //3d Survey
            preferenceLoaderHelper.setCogoSurveyDistance(2);
            is2dSurvey = false;

            //determine if Vertical Angles are to be Zenith/Vertical/Elevation Difference.

            int verticalAngleType = preferenceLoaderHelper.getSurveyFormatAngleVZ();

            switch (verticalAngleType){
                case 0: //Zenith Angle
                    preferenceLoaderHelper.setCogoSurveyDistance(3);
                    viewDistanceToDisplay = 3;

                    swapDistanceItems(3);
                    break;

                case 1: //Vertical Angle
                    preferenceLoaderHelper.setCogoSurveyDistance(4);
                    viewDistanceToDisplay = 4;

                    swapDistanceItems(4);
                    break;

                case 2: //Vertical Difference
                    preferenceLoaderHelper.setCogoSurveyDistance(5);
                    viewDistanceToDisplay = 5;

                    swapDistanceItems(5);
                    break;
            }

        }



    }


    private void setOnClickListener(View v){
        Log.d(TAG, "setOnClickListener: Started...");
        ibPointNoAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupMenuOpen = 1;

                PopupMenu popupMenuPointNo = new PopupMenu(mContext, ibPointNoAction);
                popupMenuPointNo.inflate(R.menu.popup_cogo_sideshot_pointno);

                popupMenuPointNo.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {

                        switch (item.getItemId()){
                            case R.id.sideshot_pointNo_item_1:
                                //showToast("Next Sequential Number",true);

                                getNextPointNumber();

                                break;

                            case R.id.sideshot_pointNo_item_2:
                                //showToast("Showing Point List",true);

                                openPointListView();

                                break;

                        }

                        return true;
                    }
                });

                popupMenuPointNo.show();
            }
        });

        ibPointhAngleAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupMenuOpen = 2;

                PopupMenu popupMenuhAngle = new PopupMenu(mContext,ibPointhAngleAction);
                popupMenuhAngle.inflate(R.menu.popup_cogo_sideshot_hangle);

                if(horizontalAngleType ==0){
                    popupMenuhAngle.getMenu().findItem(R.id.sideshot_hAngle_item_1).setChecked(true);
                }else if(horizontalAngleType == 1){
                    popupMenuhAngle.getMenu().findItem(R.id.sideshot_hAngle_item_2).setChecked(true);
                }

                popupMenuhAngle.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {

                        switch (item.getItemId()){
                            case R.id.sideshot_hAngle_item_1:

                                if(!viewHAngleDMS.isShown()){
                                    swapHAngleItems(1);
                                }

                                break;

                            case R.id.sideshot_hAngle_item_2:
                                if(!viewHAngleDec.isShown()){
                                    swapHAngleItems(2);
                                }
                                break;
                        }

                        return true;
                    }
                });

                popupMenuhAngle.show();
            }
        });

        ibPointDistanceAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupMenuOpen = 3;

                PopupMenu popupMenuDistance = new PopupMenu(mContext, ibPointDistanceAction);
                popupMenuDistance.inflate(R.menu.popup_cogo_sideshot_distance);

                if(is2dSurvey){
                    popupMenuDistance.getMenu().findItem(R.id.sideshot_distance_item_1).setChecked(true);
                    viewDistanceToDisplay = 1;

                }else{
                    popupMenuDistance.getMenu().findItem(R.id.sideshot_distance_item_1).setChecked(false);
                }

                switch (viewDistanceToDisplay){
                    case 3:
                        popupMenuDistance.getMenu().findItem(R.id.sideshot_distance_item_3).setChecked(true);
                        break;

                    case 4:
                        popupMenuDistance.getMenu().findItem(R.id.sideshot_distance_item_4).setChecked(true);
                        break;

                    case 5:
                        popupMenuDistance.getMenu().findItem(R.id.sideshot_distance_item_5).setChecked(true);
                        break;

                    default:
                        //no check marks
                        break;
                }


                popupMenuDistance.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()){
                            case R.id.sideshot_distance_item_1:
                                if(!viewHd.isShown()){
                                    swapDistanceItems(1);
                                }

                                break;


                            case R.id.sideshot_distance_item_3:
                                if(!viewSdVertical.isShown()){
                                    swapDistanceItems(3);
                                }
                                break;

                            case R.id.sideshot_distance_item_4:
                                if(!viewSdVertical.isShown()){
                                    swapDistanceItems(4);
                                }
                                break;

                            case R.id.sideshot_distance_item_5:
                                if(!viewSdVDelta.isShown()){
                                    swapDistanceItems(5);
                                }
                                break;

                            case R.id.sideshot_distance_item_6:
                                showToast("Opening Distance Converter...",true);
                                break;

                        }


                        return true;
                    }
                });

                popupMenuDistance.show();
            }


        });

        switchTypeOfSurvey.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    //sideshot
                    setTextSizeTypeOfSurvey(true);
                    isTypeOfSurveySideshot = true;



                }else{
                    //traverse
                    setTextSizeTypeOfSurvey(false);
                    isTypeOfSurveySideshot = false;

                }
            }
        });

        btSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "btSave_onClick ");
                boolean isFormReady = false;
                boolean isFormInError = validateFormForNull();

                Log.d(TAG, "btSave_onClick:isFormInError: " + isFormInError);

                if(!isFormInError){
                    //No Nulls, now check for consistency
                    isFormReady = validateFormForSetup();
                    Log.d(TAG, "onClick: isFormReady: " + isFormReady);
                }


                if(isFormReady){
                    saveObservation();
                }



            }
        });


    }

    //-------------------------------------------------------------------------------------------------------------------------//
    /**
     * Methods
     */

    private void swapHAngleItems(int viewToShow){
        Log.d(TAG, "swapHAngleItems: Started...");
        switch (viewToShow){

            case 1:
                //Going from DEC to DMS

                if(etHADec !=null){
                    Log.d(TAG, "swapHAngleItems: etHADec not Null");
                    String results =  etHADec.getText().toString();

                    if(!StringUtilityHelper.isStringNull(results)){
                        mValueHAngleDec = Double.parseDouble(results);
                        Log.d(TAG, "swapHAngleItems: mValueHAngleDec: " + mValueHAngleDec);
                    }

                }

                viewHAngleDMS.setVisibility(View.VISIBLE);
                viewHAngleDec.setVisibility(View.GONE);

                horizontalAngleType = 0;

                etTargetHeight.setNextFocusDownId(R.id.hAngle_degree);
                setHAngleFromWidget(1);

                break;

            case 2:
                //showToast("Switching View to Decimal Degrees",true);

                if(etHADeg !=null){
                    Log.d(TAG, "swapHAngleItems: etHADeg: Is Not Null");
                    String resultsDeg = etHADeg.getText().toString();
                    String resultsMin = etHAMin.getText().toString();
                    String resultsSec = etHASec.getText().toString();

                    if(!StringUtilityHelper.isStringNull(resultsDeg) && !StringUtilityHelper.isStringNull(resultsMin) && !StringUtilityHelper.isStringNull(resultsSec)){
                        mValueHAngleDec = MathHelper.convertPartsToDEC(resultsDeg,resultsMin,resultsSec);
                        Log.d(TAG, "swapHAngleItems: mValueHAngleDec: " + mValueHAngleDec);

                    }

                }

                viewHAngleDec.setVisibility(View.VISIBLE);
                viewHAngleDMS.setVisibility(View.GONE);

                horizontalAngleType = 1;

                etTargetHeight.setNextFocusDownId(R.id.hAngle_dec);
                setHAngleFromWidget(2);
        }


    }

    private void swapDistanceItems(int itemToShow){
        Log.d(TAG, "swapDistanceItems: Started");
        Log.d(TAG, "swapDistanceItems: mDistance: " + mValueDistance);
        Log.d(TAG, "swapDistanceItems: Item: " + itemToShow);

        switch (itemToShow){

            case 1:
                //showToast("Switching View to Horizontal Distance Entry",true);

                getDistanceFromWidget();

                viewHd.setVisibility(View.VISIBLE);
                viewSdZenith.setVisibility(View.GONE);
                viewSdVertical.setVisibility(View.GONE);
                viewSdVDelta.setVisibility(View.GONE);

                is2dSurvey = true;
                viewDistanceToDisplay = 1;

                setDistanceToWidget(viewDistanceToDisplay);

                tvDistanceHeader.setText("HD");

                if(horizontalAngleType ==0){
                    etHASec.setNextFocusDownId(R.id.distance_horizontal_value);
                }else{
                    etHADec.setNextFocusDownId(R.id.distance_horizontal_value);
                }

                break;

            case 3:

                getDistanceFromWidget();

                //showToast("Switching View to Slope Distance with Zenith Angle Entry",true);
                viewHd.setVisibility(View.GONE);
                viewSdZenith.setVisibility(View.VISIBLE);
                viewSdVertical.setVisibility(View.GONE);
                viewSdVDelta.setVisibility(View.GONE);

                is2dSurvey=false;
                viewDistanceToDisplay = 3;

                setDistanceToWidget(viewDistanceToDisplay);

                tvDistanceHeader.setText("SD");

                if(horizontalAngleType ==0){
                    etHASec.setNextFocusDownId(R.id.distance_slope_zenith_value);
                }else{
                    etHADec.setNextFocusDownId(R.id.distance_slope_zenith_value);
                }
                
                break;

            case 4:

                getDistanceFromWidget();

                //showToast("Switching View to Slope Distance with Vertical Angle Entry",true);
                viewHd.setVisibility(View.GONE);
                viewSdZenith.setVisibility(View.GONE);
                viewSdVertical.setVisibility(View.VISIBLE);
                viewSdVDelta.setVisibility(View.GONE);

                is2dSurvey=false;
                viewDistanceToDisplay = 4;

                setDistanceToWidget(viewDistanceToDisplay);

                tvDistanceHeader.setText("SD");

                if(horizontalAngleType ==0){
                    etHASec.setNextFocusDownId(R.id.distance_slope_Vertical_value);
                }else{
                    etHADec.setNextFocusDownId(R.id.distance_slope_Vertical_value);
                }

                break;

            case 5:

                getDistanceFromWidget();

                //showToast("Switching View to Slope Distance with Vertical Difference Entry",true);
                viewHd.setVisibility(View.GONE);
                viewSdZenith.setVisibility(View.GONE);
                viewSdVertical.setVisibility(View.GONE);
                viewSdVDelta.setVisibility(View.VISIBLE);

                is2dSurvey=false;
                viewDistanceToDisplay = 5;

                setDistanceToWidget(viewDistanceToDisplay);

                tvDistanceHeader.setText("SD");

                if(horizontalAngleType ==0){
                    etHASec.setNextFocusDownId(R.id.distance_slope_Delta_value);
                }else{
                    etHADec.setNextFocusDownId(R.id.distance_slope_Delta_value);
                }

                
                break;

        }

    }

    private void initViewHADMS(){
        Log.d(TAG, "initViewHADMS: Started...");

        etHADeg = (EditText) v.findViewById(R.id.hAngle_degree);
        etHADeg.setSelectAllOnFocus(true);

        etHAMin = (EditText) v.findViewById(R.id.hAngle_min);
        etHAMin.setSelectAllOnFocus(true);

        etHASec  = (EditText) v.findViewById(R.id.hAngle_sec);
        etHASec.setSelectAllOnFocus(true);

        etHADeg.addTextChangedListener(new TextWatcher() {
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
                    if(val > 359){
                        etHADeg.setError(getResources().getString(R.string.cogo_angle_circle_error_out_of_range));
                        btSave.setClickable(false);
                    }else{
                        btSave.setClickable(true);
                    }


                }catch (NumberFormatException ex){

                }


            }
        });


        etHAMin.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                if(etHAMin.getText().toString().length()==textValidationTwo){
                    etHASec.requestFocus();
                }
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {


            }

            @Override
            public void afterTextChanged(Editable s) {
                try{
                    int val = Integer.parseInt(s.toString());
                    if(val > 59){
                        etHAMin.setError(getResources().getString(R.string.cogo_angle_error_out_of_range));
                        btSave.setClickable(false);
                    }else{
                        btSave.setClickable(true);
                    }


                }catch (NumberFormatException ex){

                }


            }
        });

        etHASec.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                try{
                    double val = Double.parseDouble(s.toString());
                    if(val > 59.999){
                        etHASec.setError(getResources().getString(R.string.cogo_angle_error_out_of_range));
                        btSave.setClickable(false);
                    }else{
                        btSave.setClickable(true);
                    }


                }catch (NumberFormatException ex){

                }
            }
        });





    }

    private void initViewHADEC(){
        Log.d(TAG, "initViewHADEC: Started...");

        etHADec = (EditText) v.findViewById(R.id.hAngle_dec);
        etHADec.setSelectAllOnFocus(true);


    }

    private void initViewVDHorizontal(){
        Log.d(TAG, "initViewVDHorizontal: Started...");
        Log.d(TAG, "swapDistanceItems: mValueDistance (hDistance): " + mValueDistance);
        etDistanceHD = (EditText) v.findViewById(R.id.distance_horizontal_value);
        etDistanceHD.setSelectAllOnFocus(true);

    }

    private void initViewVDSlopeZenith(){
        Log.d(TAG, "initViewVDSlopeZenith: Started...");
        Log.d(TAG, "initViewVDSlopeZenith: mValueDistance(Zenith): " + mValueDistance);

        etDistanceVDZenith = (EditText) v.findViewById(R.id.distance_slope_zenith_value);
        etDistanceVDZenith.setSelectAllOnFocus(true);

        etZNDeg = (EditText) v.findViewById(R.id.vAngle_Zenith_degree);
        etZNDeg.setSelectAllOnFocus(true);

        etZNMin = (EditText) v.findViewById(R.id.vAngle_Zenith_min);
        etZNMin.setSelectAllOnFocus(true);

        etZNSec = (EditText) v.findViewById(R.id.vAngle_Zenith_sec);
        etZNSec.setSelectAllOnFocus(true);

        etZNDeg.addTextChangedListener(new TextWatcher() {
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
                    if(val > 180){
                        etZNDeg.setError(getResources().getString(R.string.cogo_angle_error_out_of_range));
                        btSave.setClickable(false);
                    }else{
                        btSave.setClickable(true);
                    }


                }catch (NumberFormatException ex){

                }
            }
        });

        etZNMin.addTextChangedListener(new TextWatcher() {
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
                    if(val > 59){
                        etZNMin.setError(getResources().getString(R.string.cogo_angle_error_out_of_range));
                        btSave.setClickable(false);
                    }else{
                        btSave.setClickable(true);
                    }


                }catch (NumberFormatException ex){

                }
            }
        });

        etZNSec.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                try{
                    double val = Double.parseDouble(s.toString());
                    if(val > 59.999){
                        etZNSec.setError(getResources().getString(R.string.cogo_angle_error_out_of_range));
                        btSave.setClickable(false);
                    }else{
                        btSave.setClickable(true);
                    }


                }catch (NumberFormatException ex){

                }
            }
        });



    }

    private void initViewVDSlopeVertical(){
        Log.d(TAG, "initViewVDSlopeVertical: Started...");
        Log.d(TAG, "swapDistanceItems: mValueDistance:(Vertical) " + mValueDistance);

        etDistanceVDVertical = (EditText) v.findViewById(R.id.distance_slope_Vertical_value);
        etDistanceVDVertical.setSelectAllOnFocus(true);

        etVDDeg = (EditText) v.findViewById(R.id.vAngle_Vertical_degree);
        etVDDeg.setSelectAllOnFocus(true);

        etVDMin = (EditText) v.findViewById(R.id.vAngle_Vertical_min);
        etVDMin.setSelectAllOnFocus(true);

        etVDSec = (EditText) v.findViewById(R.id.vAngle_Vertical_sec);
        etVDSec.setSelectAllOnFocus(true);

        etVDDeg.addTextChangedListener(new TextWatcher() {
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
                    if(val > 180){
                        etVDDeg.setError(getResources().getString(R.string.cogo_angle_error_out_of_range));
                        btSave.setClickable(false);
                    }else {
                        btSave.setClickable(true);
                    }


                }catch (NumberFormatException ex){

                }
            }
        });

        etVDMin.addTextChangedListener(new TextWatcher() {
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
                    if(val > 59){
                        etVDMin.setError(getResources().getString(R.string.cogo_angle_error_out_of_range));
                        btSave.setClickable(false);
                    }else{
                        btSave.setClickable(true);
                    }


                }catch (NumberFormatException ex){

                }
            }
        });

        etVDSec.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                try {
                    double val = Double.parseDouble(s.toString());
                    if (val > 59.999) {
                        etVDSec.setError(getResources().getString(R.string.cogo_angle_error_out_of_range));
                        btSave.setClickable(false);
                    }else{
                        btSave.setClickable(true);
                    }


                } catch (NumberFormatException ex) {

                }
            }
        });

    }

    private void initViewVDSlopeDelta(){
        Log.d(TAG, "initViewVDSlopeDelta: Started...");
        Log.d(TAG, "initViewVDSlopeDelta: mValueDistance (Delta): " + mValueDistance);

        etDistanceVDDelta = (EditText) v.findViewById(R.id.distance_slope_Delta_value);
        etDistanceVDDelta.setSelectAllOnFocus(true);

        etVDDelta = (EditText) v.findViewById(R.id.vAngle_Vertical_delta);
        etVDDelta.setSelectAllOnFocus(true);

    }


    private void checkPointNumber(int pointNumber){
        Log.d(TAG, "checkPointNumber: Started...");
        BackgroundSurveyPointCheckPointNumber backgroundSurveyPointCheckPointNumber = new BackgroundSurveyPointCheckPointNumber(mContext,jobDatabaseName,this,pointNumber,true);
        backgroundSurveyPointCheckPointNumber.execute();

    }

    private void validatePointNumber(int pointNumber){
        Log.d(TAG, "validatePointNumber: Started...");

        BackgroundSurveyPointCheckPointNumber backgroundSurveyPointCheckPointNumber = new BackgroundSurveyPointCheckPointNumber(mContext,jobDatabaseName,this,pointNumber,false);
        backgroundSurveyPointCheckPointNumber.execute();
    }



    private void getNextPointNumber(){
        String pointNumber;

        if(etPointNumber !=null){
            pointNumber =  etPointNumber.getText().toString();

            if(!StringUtilityHelper.isStringNull(pointNumber)){
                //Point number in field is there
                //1st Increment to the next number

                enteredPointNumber = Integer.parseInt(pointNumber);
                nextPointNumber = enteredPointNumber + 1;

                //2nd Test new number against DB
                checkPointNumber(nextPointNumber);

            }

        }

    }

    private void openPointListView(){
        Log.d(TAG, "openPointListSelect: Started...");

        lstPointSurvey = jobCogoFragmentListener.sendPointSurveyToFragment();

        android.support.v4.app.DialogFragment pointListDialog = DialogJobSideshotPointList.newInstance(project_id, job_id, jobDatabaseName, lstPointSurvey);
        pointListDialog.show(getFragmentManager(),"dialog_list");

    }


    private void setTextSizeTypeOfSurvey(boolean isSideShotOn){

        if(isSideShotOn){

            tvSwitchSideshot.setTextSize(TypedValue.COMPLEX_UNIT_PX,getResources().getDimension(R.dimen.text_size_medium));
            tvSwitchSideshot.setTextColor(getResources().getColor(R.color.blue_primary));

            tvSwitchTraverse.setTextSize(TypedValue.COMPLEX_UNIT_PX,getResources().getDimension(R.dimen.text_size_header));
            tvSwitchTraverse.setTextColor(getResources().getColor(R.color.gray));

        }else{
            tvSwitchTraverse.setTextSize(TypedValue.COMPLEX_UNIT_PX,getResources().getDimension(R.dimen.text_size_medium));
            tvSwitchTraverse.setTextColor(getResources().getColor(R.color.blue_primary));

            tvSwitchSideshot.setTextSize(TypedValue.COMPLEX_UNIT_PX,getResources().getDimension(R.dimen.text_size_header));
            tvSwitchSideshot.setTextColor(getResources().getColor(R.color.gray));

        }

    }

    private void getHAngleFromWidget(){
        Log.d(TAG, "getHAngleFromWidget: Started...");






    }

    private void setHAngleFromWidget(int itemToShow){
        Log.d(TAG, "setHAngleFromWidget: Started");

        switch(itemToShow){
            case 1:
                if(mValueHAngleDec !=0){
                    etHADeg.setText(String.valueOf(MathHelper.convertDECToDegree(mValueHAngleDec)));
                    etHAMin.setText(String.valueOf(MathHelper.convertDECToMinute(mValueHAngleDec)));

                    double resultsSeconds = MathHelper.convertDECToSeconds(mValueHAngleDec);

                    int decimalPlaces = 0;

                    if(decimalPlaces ==0){
                        String mResultSeconds = String.valueOf((int) resultsSeconds);
                        etHASec.setText(mResultSeconds);
                    }else {
                        etHASec.setText(String.valueOf(MathHelper.convertDECToSeconds(mValueHAngleDec, decimalPlaces)));
                    }
                }
                break;
            case 2:
                if(mValueHAngleDec !=0){
                    etHADec.setText(String.valueOf(mValueHAngleDec));
                }

                break;

        }

    }


    private void getDistanceFromWidget(){
        Log.d(TAG, "grabDistance: Started");
        Log.i(TAG, "getDistanceFromWidget: Item To Show: " + viewDistanceToDisplay);

        String results=null;

        switch (viewDistanceToDisplay){
            case 1:
                if(etDistanceHD !=null){
                    results =  etDistanceHD.getText().toString();

                }
                break;

            case 3:
                if(etDistanceVDZenith !=null){
                    results =  etDistanceVDZenith.getText().toString();

                }
                break;

            case 4:
                if(etDistanceVDVertical !=null){
                    results =  etDistanceVDVertical.getText().toString();

                }
                break;

            case 5:
                if(etDistanceVDDelta !=null){
                    results =  etDistanceVDDelta.getText().toString();

                }
                break;

            default:
                results = null;


        }

        if(!StringUtilityHelper.isStringNull(results)){
            mValueDistance = Double.parseDouble(results);
        }

        Log.i(TAG, "getDistanceFromWidget: Distance set to: " + mValueDistance);

    }

    private void setDistanceToWidget(int itemToShow){
        Log.d(TAG, "setDistanceToWidget: Started");
        Log.i(TAG, "setDistanceToWidget: Distance in Memory: " + mValueDistance);
        Log.i(TAG, "getDistanceFromWidget: Item To Show: " + itemToShow);

        switch (itemToShow){
            case 1:
                if(mValueDistance !=0){
                    Log.i(TAG, "setDistanceToWidget: etDistanceHD");
                    etDistanceHD.setText(String.valueOf(mValueDistance));
                }
                break;

            case 3:
                if(mValueDistance !=0){
                    Log.i(TAG, "setDistanceToWidget: etDistanceVDZenith");
                    etDistanceVDZenith.setText(String.valueOf(mValueDistance));
                }
                break;

            case 4:
                if(mValueDistance !=0){
                    Log.i(TAG, "setDistanceToWidget: etDistanceVDVertical");
                    etDistanceVDVertical.setText(String.valueOf(mValueDistance));
                }
                break;

            case 5:
                if(mValueDistance !=0){
                    Log.i(TAG, "setDistanceToWidget: etDistanceVDDelta");
                    etDistanceVDDelta.setText(String.valueOf(mValueDistance));
                }
                break;

        }



    }

    //-------------------------------------------------------------------------------------------------------------------------//

    /**
     * Validating, creating point and then saving points!
     */

    private boolean validateFormForNull(){
        Log.d(TAG, "validateForm: Started...");
        boolean isThereAnError = false;

        //Point No.
        String pointNo = null;
        pointNo = etPointNumber.getText().toString();

        if(!StringUtilityHelper.isStringNull(pointNo)){
            mValuePointNo = Integer.parseInt(pointNo);
        }else{
            etPointNumber.setError(getResources().getString(R.string.cogo_point_no_not_entered));
            isThereAnError = true;
        }

        //Rod Height
        String targetHeight = null;
        targetHeight = etTargetHeight.getText().toString();

        if(!StringUtilityHelper.isStringNull(targetHeight)){
            mValueTargetHeight = Double.parseDouble(targetHeight);
        }else{
            etTargetHeight.setError(getResources().getString(R.string.cogo_point_height_not_entered));
            isThereAnError = true;
        }

        //Horizontal Angle
        switch(horizontalAngleType){
            case 0:  //D-M-S
                String d, m, s;
                d = etHADeg.getText().toString();
                m = etHAMin.getText().toString();
                s = etHASec.getText().toString();

                if(!StringUtilityHelper.isStringNull(d) && !StringUtilityHelper.isStringNull(m) && !StringUtilityHelper.isStringNull(s)){
                    //Save horizontal angle as a decimal degree
                    mValueHAngleDec = MathHelper.convertPartsToDEC(d,m,s);

                }else {
                    if(StringUtilityHelper.isStringNull(d)) {
                        etHADeg.setError(getResources().getString(R.string.cogo_ha_deg_not_entered));
                        isThereAnError = true;
                    }

                    if(StringUtilityHelper.isStringNull(m)) {
                        etHAMin.setError(getResources().getString(R.string.cogo_ha_min_not_entered));
                        isThereAnError = true;

                    }
                    if(StringUtilityHelper.isStringNull(s)) {
                        etHASec.setError(getResources().getString(R.string.cogo_ha_sec_not_entered));
                        isThereAnError = true;
                    }
                }

                break;

            case 1: //DEC
                String dec;
                dec = etHADec.getText().toString();

                if(!StringUtilityHelper.isStringNull(dec)){
                    //Save horizontal angle as a decimal degree
                    mValueHAngleDec = Double.parseDouble(dec);
                }else{
                    etHADec.setError(getResources().getString(R.string.cogo_ha_dec_not_entered));
                    isThereAnError = true;
                }

                break;
        }

        //Distance and Vertical Component

        switch(viewDistanceToDisplay){
            case 1: //Horizontal
                String hDistance;

                hDistance = etDistanceHD.getText().toString();

                if(!StringUtilityHelper.isStringNull(hDistance)){
                    //Save horizontal angle as a decimal degree
                    mValueDistance = Double.parseDouble(hDistance);
                }else{
                    etDistanceHD.setError(getResources().getString(R.string.cogo_distance_not_entered));
                    isThereAnError = true;
                }


                break;

            case 3:  //Zenith
                String sDistanceZenith, zenithDeg, zenithMin, zenithSec;

                sDistanceZenith = etDistanceVDZenith.getText().toString();
                zenithDeg = etZNDeg.getText().toString();
                zenithMin = etZNMin.getText().toString();
                zenithSec = etZNSec.getText().toString();

                if(!StringUtilityHelper.isStringNull(sDistanceZenith)){
                    //Save horizontal angle as a decimal degree
                    mValueDistance = Double.parseDouble(sDistanceZenith);
                }else{
                    etDistanceVDZenith.setError(getResources().getString(R.string.cogo_distance_not_entered));
                    isThereAnError = true;
                }

                if(!StringUtilityHelper.isStringNull(zenithDeg) && !StringUtilityHelper.isStringNull(zenithMin) && !StringUtilityHelper.isStringNull(zenithSec)){
                    //Save horizontal angle as a decimal degree
                    mValueVAngleDec = MathHelper.convertPartsToDEC(zenithDeg,zenithMin,zenithSec);

                }else{

                    if(StringUtilityHelper.isStringNull(zenithDeg)) {
                        etZNDeg.setError(getResources().getString(R.string.cogo_ha_deg_not_entered));
                        isThereAnError = true;
                    }

                    if(StringUtilityHelper.isStringNull(zenithMin)) {
                        etZNMin.setError(getResources().getString(R.string.cogo_ha_min_not_entered));
                        isThereAnError = true;
                    }

                    if(StringUtilityHelper.isStringNull(zenithSec)) {
                        etZNSec.setError(getResources().getString(R.string.cogo_ha_sec_not_entered));
                        isThereAnError = true;
                    }
                }


                break;

            case 4:  //Vertical
                String sDistanceVertical, verticalDeg, verticalMin, verticalSec;

                sDistanceVertical = etDistanceVDVertical.getText().toString();
                verticalDeg = etVDDeg.getText().toString();
                verticalMin = etVDMin.getText().toString();
                verticalSec = etVDSec.getText().toString();

                if(!StringUtilityHelper.isStringNull(sDistanceVertical)){
                    //Save horizontal angle as a decimal degree
                    mValueDistance = Double.parseDouble(sDistanceVertical);
                }else{
                    etDistanceVDVertical.setError(getResources().getString(R.string.cogo_distance_not_entered));
                    isThereAnError = true;
                }

                if(!StringUtilityHelper.isStringNull(verticalDeg) && !StringUtilityHelper.isStringNull(verticalMin) && !StringUtilityHelper.isStringNull(verticalSec)){
                    //Save horizontal angle as a decimal degree
                    mValueVAngleDec = MathHelper.convertPartsToDEC(verticalDeg,verticalMin,verticalSec);

                }else {

                    if(StringUtilityHelper.isStringNull(verticalDeg)) {
                        etVDDeg.setError(getResources().getString(R.string.cogo_ha_deg_not_entered));
                        isThereAnError = true;

                    }

                    if(StringUtilityHelper.isStringNull(verticalMin)) {
                        etVDMin.setError(getResources().getString(R.string.cogo_ha_min_not_entered));
                        isThereAnError = true;

                    }

                    if(StringUtilityHelper.isStringNull(verticalSec)) {
                        etVDSec.setError(getResources().getString(R.string.cogo_ha_sec_not_entered));
                        isThereAnError = true;
                    }
                }

                break;

            case 5:  //Delta
                String sDistanceDelta, verticalDelta;

                sDistanceDelta = etDistanceVDDelta.getText().toString();
                verticalDelta = etVDDelta.getText().toString();

                if(!StringUtilityHelper.isStringNull(sDistanceDelta)){
                    //Save horizontal angle as a decimal degree
                    mValueDistance = Double.parseDouble(sDistanceDelta);
                }else{
                    etDistanceVDDelta.setError(getResources().getString(R.string.cogo_distance_not_entered));
                    isThereAnError = true;
                }

                if(!StringUtilityHelper.isStringNull(verticalDelta)){
                    //Save horizontal angle as a decimal degree
                    mValueVDelta = Double.parseDouble(verticalDelta);
                }else{
                    etVDDelta.setError(getResources().getString(R.string.cogo_ha_dec_not_entered));
                    isThereAnError = true;
                }

                break;



        }

        //Description
        String pointDescription = null;
        pointDescription = etPointDescription.getText().toString();

        if(!StringUtilityHelper.isStringNull(pointDescription)){
            mValuePointDesc = pointDescription;
        }else{
            etPointDescription.setError(getResources().getString(R.string.cogo_description_not_entered));
            isThereAnError = true;
        }

        return  isThereAnError;

    }


    private boolean validateFormForSetup(){
        Log.d(TAG, "validateFormForValues: Started...");
        boolean isTheFormReady = true;

        int occupyPointNo = jobCogoFragmentListener.sendOccupyPointNoToFragment();
        int backsightPointNo = jobCogoFragmentListener.sendBacksightPointNoToFragment();

        if(occupyPointNo == 0){
            showToast(getResources().getString(R.string.cogo_setup_no_occupy_backsight),true);
            isTheFormReady = false;
        }

        return isTheFormReady;

    }

    private void saveObservation(){
        Log.d(TAG, "saveObservation: Started...");
        Point mObservedCoordinates = new Point();

        //Get Occupy Point No. and Backsight
        occupyPointSurvey = jobCogoFragmentListener.sendOccupyPointSurveyToFragment();
        backsightPointSurvey = jobCogoFragmentListener.sendBacksightPointSurveyToFragment();

        //determine hDistance

        switch (viewDistanceToDisplay){
            case 1:
                //nothing to do
                Log.d(TAG, "saveObservation: Case 1");
                mValueVAngleDec = 90d;
                break;

            case 3:  //Zenith
                Log.d(TAG, "saveObservation: Case 3");
                break;

            case 4: //Vertical
                Log.d(TAG, "saveObservation: Case 4");
                mValueVAngleDec = 90d - mValueVAngleDec;
                break;

            case 5:  //Delta Difference
                Log.d(TAG, "saveObservation: Case 5");
                mValueVAngleDec = 90d;

        }

        double targetHeight = Double.parseDouble(etTargetHeight.getText().toString());
        double instrumentHeight = jobCogoFragmentListener.sendOccupyHeightToFragment();

        if(is2dSurvey && targetHeight == 0){
            mObservedCoordinates = MathHelper.solveForCoordinatesFromTurnedAngleAndDistance(occupyPointSurvey,backsightPointSurvey,mValueHAngleDec,mValueDistance,mValueVAngleDec);
        }else{
            mObservedCoordinates = MathHelper.solveForCoordinatesFromTurnedAngleAndDistance(occupyPointSurvey,backsightPointSurvey,mValueHAngleDec,mValueDistance,mValueVAngleDec,instrumentHeight, targetHeight);
        }


        Log.d(TAG, "submitForm: Validation Approved, Saving...");
        // Setup Background Task
        BackgroundPointSurveyNewFromObservation backgroundPointSurveyNew = new BackgroundPointSurveyNewFromObservation(mContext, jobDatabaseName, jobCogoFragmentListener);
        PointSurvey pointSurvey = populateValues(mObservedCoordinates);

        // Execute background task
        backgroundPointSurveyNew.execute(pointSurvey);

        //Clear form
        clearForm();

        //Add one to Point number and check if exists
        Log.d(TAG, "saveObservation: Next Point Number: " + nextPointNumber);
        etPointNumber.setText(String.valueOf(nextPointNumber));

        //Clear variables
        clearVariables();

        if(!isTypeOfSurveySideshot){
            //Is traverse, ask if move up to next station
            checkDialogToMoveStation(pointSurvey, occupyPointSurvey);

        }


    }

    private PointSurvey populateValues(Point point){
        Log.d(TAG, "populateValues: Started...");

        PointSurvey pointSurvey = new PointSurvey();

        int pointNumber = mValuePointNo;
        pointSurvey.setPoint_no(pointNumber);

        double pointNorthing = point.getNorthing();
        pointSurvey.setNorthing(pointNorthing);

        double pointEasting = point.getEasting();
        pointSurvey.setEasting(pointEasting);

        double pointElevation = point.getElevation();
        pointSurvey.setElevation(pointElevation);

        String pointDescription = mValuePointDesc;
        pointSurvey.setDescription(pointDescription);

        pointSurvey.setPointType(3);

        return pointSurvey;
    }

    private void checkDialogToMoveStation(final PointSurvey pointToMoveTo, final PointSurvey pointToBacksight){
        Log.d(TAG, "checkDialogToMoveStation: Started");
        final boolean[] moveStationUp = new boolean[]{false};


        AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
        dialog.setTitle(getString(R.string.cogo_traverse_time_to_move_title));
        dialog.setMessage(getString(R.string.cogo_traverse_time_to_move_message));

        dialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Log.d(TAG, "checkDialogToMoveStation: Return True");
                moveStationUp[0] = true;
                setOccupyAndBacksightInActivity(pointToMoveTo, pointToBacksight);

            }
        });
        dialog.setNegativeButton("No", null);
        dialog.create();
        dialog.show();

        Log.d(TAG, "checkDialogToMoveStation: Returning to finish");

    }

    private void setOccupyAndBacksightInActivity(PointSurvey occupyPoint, PointSurvey backsightPoint){
        Log.d(TAG, "setOccupyAndBacksightInActivity: Started");
        Double occupyPointHeight=0d, backsightPointHeight=0d;

        jobCogoFragmentListener.sendTraverseSetupToMainActivity(occupyPoint.getPoint_no(), backsightPoint.getPoint_no(),occupyPointHeight,backsightPointHeight);



    }

    //-------------------------------------------------------------------------------------------------------------------------//

    /**
     * Method Helpers
     */

    private void clearForm(){
        etPointNumber.setText("");
        etPointDescription.setText("");
        etTargetHeight.setText("0.00");

        etHADeg.setText("");
        etHAMin.setText("");
        etHASec.setText("");
        etHADec.setText("");

        etDistanceHD.setText("");
        etDistanceVDZenith.setText("");
        etDistanceVDVertical.setText("");
        etDistanceVDDelta.setText("");

        etVDDeg.setText("");
        etVDMin.setText("");
        etVDSec.setText("");
        etVDDelta.setText("");

    }

    private void clearVariables(){
        mValuePointNo = 0;
        mValueTargetHeight = 0;

        mValueHAngleDec = 0;
        mValueDistance = 0;
        mValueVAngleDec = 0;
        mValueVDelta = 0;

        mValuePointDesc = null;


    }

    private void showToast(String data, boolean shortTime) {

        if (shortTime) {
            Toast.makeText(getActivity(), data, Toast.LENGTH_SHORT).show();

        } else{
            Toast.makeText(getActivity(), data, Toast.LENGTH_LONG).show();

        }
    }

    //-------------------------------------------------------------------------------------------------------------------------//

    /**
     * Listeners
     */

    @Override
    public void doesPointExist(boolean isPointFound) {
        Log.d(TAG, "doesPointExist: Started");
        if(!isPointFound){
            //If Pass, set number in field
            etPointNumber.setText(String.valueOf(nextPointNumber));
        }else{
            //if Fail, continue to increment until it passes
            Log.d(TAG, "getNextPointNumber: Point Exists");

            BackgroundSurveyPointFindNextNumber backgroundSurveyPointFindNextNumber = new BackgroundSurveyPointFindNextNumber(mContext,jobDatabaseName,this,enteredPointNumber);
            backgroundSurveyPointFindNextNumber.execute();

        }

    }

    @Override
    public void whatIsNextPointNumber(int pointNumber) {
        Log.d(TAG, "whatIsNextPointNumber: Next Point Number Is: " + pointNumber);

        etPointNumber.setText(String.valueOf(pointNumber+1));


    }

    @Override
    public void isPointValidForDatabase(boolean isPointFound) {

        if(isPointFound){
            etPointNumber.setError(getResources().getString(R.string.cogo_point_no_exists));
            btSave.setClickable(false);
        }else
            etPointNumber.setError(null);
            btSave.setClickable(true);
            int pointNumber = Integer.parseInt(etPointNumber.getText().toString());
            nextPointNumber = pointNumber + 1;

    }
}