package com.survlogic.survlogic.view;

import android.app.Activity;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.survlogic.survlogic.R;
import com.survlogic.survlogic.background.BackgroundSurveyPointExistInDatabase;
import com.survlogic.survlogic.interf.CallCurveSolutionDialogListener;
import com.survlogic.survlogic.interf.DatabaseDoesPointExistFromAsyncListener;
import com.survlogic.survlogic.interf.MapcheckListener;
import com.survlogic.survlogic.model.CurveSurvey;
import com.survlogic.survlogic.model.PointMapCheck;
import com.survlogic.survlogic.utils.MathHelper;
import com.survlogic.survlogic.utils.StringUtilityHelper;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by chrisfillmore on 6/30/2017.
 */

public class Card_View_Holder_Job_Mapcheck_Add extends RecyclerView.ViewHolder implements DatabaseDoesPointExistFromAsyncListener {
    private static final String TAG = "Card_View_Holder_Job_Ma";

    public View mCardView;
    private Context mContext;
    private MapcheckListener mapcheckListener;
    private CallCurveSolutionDialogListener callCurveSolutionDialogListener;

    private String jobDatabaseName;

    //-Add New Observation-//
    private View vListAdd_ha_bearing, vListAdd_ha_turnedAngle, vListAdd_ha_azimuth,
            vListAdd_curve_da_r, vListAdd_curve_da_l, vListAdd_curve_r_l, vListAdd_curve_cb_ch;

    private EditText etMapCheckPointDescription;
    public EditText etMapCheckPointNumber;

    public CheckBox cbIsClosingPoint;

    private ImageButton ibSwapObservation;
    public Button btSaveObservation, btCancelObservation;

    private int viewCurrentTypeToDisplay = 0;
    private int popupMenuOpen = 0;

    private boolean isHaAzimuthInit = false, isHaBearingInit = false, isHaTurnedInit = false;
    private boolean isCurveDARInit = false, isCurveDALInit = false, isCurveRLInit = false;
    private boolean isCurveCBCHInit = false;

    private boolean formClosingPoint = false;

    private TextView tvIsClosingPoint, tvPointNumberHeader;
    private TextView tvCurveARight, tvCurveALeft;
    private TextView tvCurveBRight, tvCurveBLeft;
    private TextView tvCurveCRight, tvCurveCLeft;
    private TextView tvCurveDRight, tvCurveDLeft;

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

    private boolean isSwitchCurveAIsRight = true;
    private boolean isSwitchCurveBIsRight = true;
    private boolean isSwitchCurveCIsRight = true;
    private boolean isSwitchCurveDIsRight = true;

    private static final int CURVE_A = 0, CURVE_B = 1, CURVE_C = 2, CURVE_D = 3;

    private Button btCurveASolve, btCurveBSolve, btCurveCSolve, btCurveDSolve;

    //-Method Variables-//
    private int mValuePointNo;
    private String mValuePointDesc;
    private double mValueDistance;
    private double mValueBearing, mValueAzimuth, mValueTurnedAngle;
    private double mValueCurveDelta, mValueCurveRadius, mValueCurveLength, mValueCurveCB, mValueCurveCH;

    //-Helpers-//
    private static final int textValidationOne = 0, textValidationTwo = 1, textValidationThree = 2, textValidationFour = 4;

    //-From Activity-//
    private ArrayList<PointMapCheck> lstPointMapCheck = new ArrayList<>();
    private int listPosition = 0;

    public Card_View_Holder_Job_Mapcheck_Add(View itemView, Context mContext, MapcheckListener mapcheckListener, CallCurveSolutionDialogListener callCurveSolutionDialogListener, String jobDatabaseName) {
        super(itemView);

        this.mContext = mContext;
        this.mCardView = itemView;
        this.mapcheckListener = mapcheckListener;
        this.callCurveSolutionDialogListener = callCurveSolutionDialogListener;
        this.jobDatabaseName = jobDatabaseName;

        initVewListAddNewObservation();

    }

    private void initVewListAddNewObservation(){
        Log.d(TAG, "initVewListAddNewObservation: Started");

        vListAdd_ha_bearing = itemView.findViewById(R.id.layout_ha_bearing);
        vListAdd_ha_turnedAngle = itemView.findViewById(R.id.layout_ha_turned_angle);
        vListAdd_ha_azimuth = itemView.findViewById(R.id.layout_ha_azimuth);

        vListAdd_curve_da_r = itemView.findViewById(R.id.layout_curve_delta_radius);
        vListAdd_curve_da_l = itemView.findViewById(R.id.layout_curve_delta_length);
        vListAdd_curve_r_l = itemView.findViewById(R.id.layout_curve_radius_length);
        vListAdd_curve_cb_ch = itemView.findViewById(R.id.layout_curve_bearing_chord);

        ibSwapObservation = (ImageButton) itemView.findViewById(R.id.typeOfObservation);


        Log.d(TAG, "initVewListAddNewObservation: position: " + listPosition);

        setPopupMenuForObservationType(0);


        etMapCheckPointDescription = (EditText) itemView.findViewById(R.id.point_Description_value);

        tvPointNumberHeader = (TextView) itemView.findViewById(R.id.point_number_header);
        etMapCheckPointNumber = (EditText) itemView.findViewById(R.id.point_number);


        etMapCheckPointNumber.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(!formClosingPoint){
                    try{
                        String pointNo = null;
                        pointNo = etMapCheckPointNumber.getText().toString();

                        if(!StringUtilityHelper.isStringNull(pointNo)){
                            mValuePointNo = Integer.parseInt(pointNo);
                            checkPointNumberFromDatabase(mValuePointNo);
                        }

                    }catch(NumberFormatException ex){
                        showToast("Error.  Check Number Format", true);

                    }
                }

            }
        });




        cbIsClosingPoint = (CheckBox) itemView.findViewById(R.id.is_closing_point);
        tvIsClosingPoint = (TextView) itemView.findViewById(R.id.is_closing_point_desc);

        btSaveObservation = (Button) itemView.findViewById(R.id.save_observation_button);
        btCancelObservation = (Button) itemView.findViewById(R.id.cancel_observation_button);

        //------------------------------------------------------------------------------------------//
        viewCurrentTypeToDisplay = 0;

        swapTypeOfObservation(0);


    }


    private void initLayoutHaBearing(){

        if(!isHaBearingInit){
            etBearingQuadrant = (EditText) itemView.findViewById(R.id.hAngle_bearing_quadrant);
            etBearingQuadrant.setSelectAllOnFocus(true);

            etBearingDeg = (EditText) itemView.findViewById(R.id.hAngle_bearing_degree);
            etBearingDeg.setSelectAllOnFocus(true);

            etBearingMin = (EditText) itemView.findViewById(R.id.hAngle_bearing_min);
            etBearingMin.setSelectAllOnFocus(true);

            etBearingSec = (EditText) itemView.findViewById(R.id.hAngle_bearing_sec);
            etBearingSec.setSelectAllOnFocus(true);

            etBearingDistance = (EditText) itemView.findViewById(R.id.ha_bearing_distance);
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
                            etBearingQuadrant.setError(mContext.getResources().getString(R.string.cogo_mapcheck_quandarnt_out_of_range));
                            Log.d(TAG, "btSaveObservation: Locked");
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
                            etBearingDeg.setError(mContext.getResources().getString(R.string.cogo_angle_error_out_of_range));
                            Log.d(TAG, "btSaveObservation: Locked");
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
                            etBearingMin.setError(mContext.getResources().getString(R.string.cogo_angle_error_out_of_range));
                            Log.d(TAG, "btSaveObservation: Locked");
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
                            etBearingSec.setError(mContext.getString(R.string.cogo_angle_error_out_of_range));
                            Log.d(TAG, "btSaveObservation: Locked");
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
            etAzimuthDeg = (EditText) itemView.findViewById(R.id.hAngle_azimuth_degree);
            etAzimuthDeg.setSelectAllOnFocus(true);

            etAzimuthMin = (EditText) itemView.findViewById(R.id.hAngle_azimuth_min);
            etAzimuthMin.setSelectAllOnFocus(true);

            etAzimuthSec = (EditText) itemView.findViewById(R.id.hAngle_azimuth_sec);
            etAzimuthSec.setSelectAllOnFocus(true);

            etAzimuthDistance = (EditText) itemView.findViewById(R.id.ha_azimuth_distance);
            etAzimuthDistance.setSelectAllOnFocus(true);
            etAzimuthDistance.setNextFocusDownId(R.id.point_Description_value);

            isHaAzimuthInit = true;

            etAzimuthDeg.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    if(etAzimuthDeg.getText().toString().length()==textValidationThree){
                        etAzimuthMin.requestFocus();
                    }
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    try{
                        int val = Integer.parseInt(s.toString());
                        if(val > 359){
                            etAzimuthDeg.setError(mContext.getResources().getString(R.string.cogo_angle_circle_error_out_of_range));
                            Log.d(TAG, "btSaveObservation: Locked");
                            btSaveObservation.setClickable(false);
                        }else{
                            btSaveObservation.setClickable(true);
                        }


                    }catch (NumberFormatException ex){

                    }
                }
            });

            etAzimuthMin.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    if(etAzimuthMin.getText().toString().length()==textValidationTwo){
                        etAzimuthSec.requestFocus();
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
                            etAzimuthMin.setError(mContext.getResources().getString(R.string.cogo_angle_error_out_of_range));
                            Log.d(TAG, "btSaveObservation: Locked");
                            btSaveObservation.setClickable(false);
                        }else{
                            btSaveObservation.setClickable(true);
                        }


                    }catch (NumberFormatException ex){

                    }
                }
            });

            etAzimuthSec.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {

                }
            });

            etAzimuthSec.addTextChangedListener(new TextWatcher() {
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
                            etAzimuthSec.setError(mContext.getString(R.string.cogo_angle_error_out_of_range));
                            Log.d(TAG, "btSaveObservation: Locked");
                            btSaveObservation.setClickable(false);
                        }else{
                            btSaveObservation.setClickable(true);
                        }


                    }catch (NumberFormatException ex){

                    }
                }

            });


        }

    }

    private void initLayoutHaTurnedAngle(){

        if(!isHaTurnedInit){
            etTurnedDeg = (EditText) itemView.findViewById(R.id.hAngle_tangle_degree);
            etTurnedDeg.setSelectAllOnFocus(true);

            etTurnedMin = (EditText) itemView.findViewById(R.id.hAngle_tangle_min);
            etTurnedMin.setSelectAllOnFocus(true);

            etTurnedSec = (EditText) itemView.findViewById(R.id.hAngle_tangle_sec);
            etTurnedSec.setSelectAllOnFocus(true);

            etTurnedDistance = (EditText) itemView.findViewById(R.id.ha_tangle_distance);
            etTurnedDistance.setSelectAllOnFocus(true);
            etTurnedDistance.setNextFocusDownId(R.id.point_Description_value);

            isHaTurnedInit = true;

            etTurnedDeg.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    if(etTurnedDeg.getText().toString().length()==textValidationThree){
                        etTurnedMin.requestFocus();
                    }
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    try{
                        int val = Integer.parseInt(s.toString());
                        if(val > 359){
                            etTurnedDeg.setError(mContext.getString(R.string.cogo_angle_circle_error_out_of_range));
                            Log.d(TAG, "btSaveObservation: Locked");
                            btSaveObservation.setClickable(false);
                        }else{
                            btSaveObservation.setClickable(true);
                        }


                    }catch (NumberFormatException ex){

                    }
                }
            });

            etTurnedMin.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    if(etTurnedMin.getText().toString().length()==textValidationTwo){
                        etTurnedSec.requestFocus();
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
                            etTurnedMin.setError(mContext.getString(R.string.cogo_angle_error_out_of_range));
                            Log.d(TAG, "btSaveObservation: Locked");
                            btSaveObservation.setClickable(false);
                        }else{
                            btSaveObservation.setClickable(true);
                        }


                    }catch (NumberFormatException ex){

                    }
                }
            });

            etTurnedSec.addTextChangedListener(new TextWatcher() {
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
                            etTurnedSec.setError(mContext.getString(R.string.cogo_angle_error_out_of_range));
                            Log.d(TAG, "btSaveObservation: Locked");
                            btSaveObservation.setClickable(false);
                        }else{
                            btSaveObservation.setClickable(true);
                        }


                    }catch (NumberFormatException ex){

                    }
                }
            });



        }

    }

    private void initLayoutCurveDeltaAndRadius(){

        if(!isCurveDARInit){
            etCurveADeltaDeg = (EditText) itemView.findViewById(R.id.dr_delta_degree);
            etCurveADeltaDeg.setSelectAllOnFocus(true);

            etCurveADeltaMin = (EditText) itemView.findViewById(R.id.dr_delta_min);
            etCurveADeltaMin.setSelectAllOnFocus(true);

            etCurveADeltaSec = (EditText) itemView.findViewById(R.id.dr_delta_sec);
            etCurveADeltaSec.setSelectAllOnFocus(true);

            etCurveARadius = (EditText) itemView.findViewById(R.id.dr_curve_radius);
            etCurveARadius.setSelectAllOnFocus(true);

            switchCurveAIsRight = (Switch)  itemView.findViewById(R.id.dr_switch_direction);
            tvCurveARight = (TextView) itemView.findViewById(R.id.switch_curve_a_right);
            tvCurveALeft = (TextView) itemView.findViewById(R.id.switch_curve_a_left);

            btCurveASolve = (Button) itemView.findViewById(R.id.curve_delta_radius_solve);
            btCurveASolve.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    solveCurve();
                }
            });



            isCurveDARInit = true;

            etCurveADeltaDeg.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    if(etCurveADeltaDeg.getText().toString().length()==textValidationThree){
                        etCurveADeltaMin.requestFocus();
                    }
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    try{
                        int val = Integer.parseInt(s.toString());
                        if(val > 359){
                            etCurveADeltaDeg.setError(mContext.getString(R.string.cogo_angle_circle_error_out_of_range));
                            Log.d(TAG, "btSaveObservation: Locked");
                            btSaveObservation.setClickable(false);
                        }else{
                            btSaveObservation.setClickable(true);
                        }


                    }catch (NumberFormatException ex){

                    }
                }
            });

            etCurveADeltaMin.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    if(etCurveADeltaMin.getText().toString().length()==textValidationTwo){
                        etCurveADeltaSec.requestFocus();
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
                            etCurveADeltaMin.setError(mContext.getString(R.string.cogo_angle_error_out_of_range));
                            Log.d(TAG, "btSaveObservation: Locked");
                            btSaveObservation.setClickable(false);
                        }else{
                            btSaveObservation.setClickable(true);
                        }


                    }catch (NumberFormatException ex){

                    }
                }
            });

            etCurveADeltaSec.addTextChangedListener(new TextWatcher() {
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
                            etCurveADeltaSec.setError(mContext.getString(R.string.cogo_angle_error_out_of_range));
                            Log.d(TAG, "btSaveObservation: Locked");
                            btSaveObservation.setClickable(false);
                        }else{
                            Log.d(TAG, "btSaveObservation: UnLocked");
                            btSaveObservation.setClickable(true);
                        }


                    }catch (NumberFormatException ex){

                    }
                }
            });

            switchCurveAIsRight.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if(isChecked){
                        //curve to Right
                        setTextSizeCurveDirection(true,CURVE_A);
                        isSwitchCurveAIsRight = true;



                    }else{
                        //curve to left
                        setTextSizeCurveDirection(false,CURVE_A);
                        isSwitchCurveAIsRight = false;

                    }
                }
            });

        }

    }

    private void initLayoutCurveDeltaAndLength(){

        if(!isCurveDALInit){
            etCurveBDeltaDeg = (EditText) itemView.findViewById(R.id.dl_delta_degree);
            etCurveBDeltaDeg.setSelectAllOnFocus(true);

            etCurveBDeltaMin = (EditText) itemView.findViewById(R.id.dl_delta_min);
            etCurveBDeltaMin.setSelectAllOnFocus(true);

            etCurveBDeltaSec = (EditText) itemView.findViewById(R.id.dl_delta_sec);
            etCurveBDeltaSec.setSelectAllOnFocus(true);

            etCurveBLength = (EditText) itemView.findViewById(R.id.dl_curve_length);
            etCurveBLength.setSelectAllOnFocus(true);

            switchCurveBIsRight = (Switch)  itemView.findViewById(R.id.dl_switch_direction);
            tvCurveBRight = (TextView) itemView.findViewById(R.id.switch_curve_b_right);
            tvCurveBLeft = (TextView) itemView.findViewById(R.id.switch_curve_b_left);

            btCurveBSolve = (Button) itemView.findViewById(R.id.curve_delta_length_solve);
            btCurveBSolve.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    solveCurve();
                }
            });



            isCurveDALInit = true;

            etCurveBDeltaDeg.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    if(etCurveBDeltaDeg.getText().toString().length()==textValidationThree){
                        etCurveBDeltaMin.requestFocus();
                    }
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    try{
                        int val = Integer.parseInt(s.toString());
                        if(val > 359){
                            etCurveBDeltaDeg.setError(mContext.getString(R.string.cogo_angle_circle_error_out_of_range));
                            Log.d(TAG, "btSaveObservation: Locked");
                            btSaveObservation.setClickable(false);
                        }else{
                            Log.d(TAG, "btSaveObservation: UnLocked");
                            btSaveObservation.setClickable(true);
                        }


                    }catch (NumberFormatException ex){

                    }
                }
            });

            etCurveBDeltaMin.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    if(etCurveBDeltaMin.getText().toString().length()==textValidationTwo){
                        etCurveBDeltaSec.requestFocus();
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
                            etCurveBDeltaMin.setError(mContext.getString(R.string.cogo_angle_error_out_of_range));
                            Log.d(TAG, "btSaveObservation: Locked");
                            btSaveObservation.setClickable(false);
                        }else{
                            Log.d(TAG, "btSaveObservation: UnLocked");
                            btSaveObservation.setClickable(true);
                        }


                    }catch (NumberFormatException ex){

                    }
                }
            });

            etCurveBDeltaSec.addTextChangedListener(new TextWatcher() {
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
                            etCurveBDeltaSec.setError(mContext.getString(R.string.cogo_angle_error_out_of_range));
                            Log.d(TAG, "btSaveObservation: Locked");
                            btSaveObservation.setClickable(false);
                        }else{
                            Log.d(TAG, "btSaveObservation: UnLocked");
                            btSaveObservation.setClickable(true);
                        }


                    }catch (NumberFormatException ex){

                    }
                }
            });

            switchCurveBIsRight.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if(isChecked){
                        //curve to Right
                        setTextSizeCurveDirection(true,CURVE_B);
                        isSwitchCurveBIsRight = true;



                    }else{
                        //curve to left
                        setTextSizeCurveDirection(false,CURVE_B);
                        isSwitchCurveBIsRight = false;

                    }
                }
            });
        }

    }

    private void initLayoutCurveRadiusAndLength(){

        if(!isCurveRLInit){
            etCurveCRadius = (EditText) itemView.findViewById(R.id.rl_curve_radius);
            etCurveCRadius.setSelectAllOnFocus(true);

            etCurveCLength = (EditText) itemView.findViewById(R.id.rl_curve_length);
            etCurveCLength.setSelectAllOnFocus(true);

            switchCurveCIsRight = (Switch)  itemView.findViewById(R.id.rl_switch_direction);
            tvCurveCRight = (TextView) itemView.findViewById(R.id.switch_curve_c_right);
            tvCurveCLeft = (TextView) itemView.findViewById(R.id.switch_curve_c_left);

            btCurveCSolve = (Button) itemView.findViewById(R.id.curve_radius_length_solve);
            btCurveCSolve.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    solveCurve();
                }
            });
            isCurveRLInit = true;


            switchCurveCIsRight.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if(isChecked){
                        //curve to Right
                        setTextSizeCurveDirection(true,CURVE_C);
                        isSwitchCurveCIsRight = true;



                    }else{
                        //curve to left
                        setTextSizeCurveDirection(false,CURVE_C);
                        isSwitchCurveCIsRight = false;

                    }
                }
            });

        }

    }

    private void initLayoutCurveBearingAndDistance(){

        if(!isCurveCBCHInit){
            etCurveDCBQuadrant = (EditText) itemView.findViewById(R.id.cb_curve_bearing_quadrant);
            etCurveDCBQuadrant.setSelectAllOnFocus(true);

            etCurveDCBDeg = (EditText) itemView.findViewById(R.id.cb_curve_bearing_deg);
            etCurveDCBDeg.setSelectAllOnFocus(true);

            etCurveDCBMin = (EditText) itemView.findViewById(R.id.cb_curve_bearing_min);
            etCurveDCBMin.setSelectAllOnFocus(true);

            etCurveDCBSec = (EditText) itemView.findViewById(R.id.cb_curve_bearing_sec);
            etCurveDCBSec.setSelectAllOnFocus(true);

            etCurveDCH = (EditText) itemView.findViewById(R.id.cb_curve_chord_distance);
            etCurveDCH.setSelectAllOnFocus(true);

            switchCurveDIsRight = (Switch)  itemView.findViewById(R.id.cb_switch_direction);

            btCurveDSolve = (Button) itemView.findViewById(R.id.curve_cb_ch_solve);
            isCurveCBCHInit = true;
        }

    }

    public void setPopupMenuForObservationType(int position){
        Log.d(TAG, "setPopupMenu: Started...");
        if(listPosition == position ){
            ibSwapObservation.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    popupMenuOpen = 1;

                    PopupMenu popupMenuObsType = new PopupMenu(mContext, ibSwapObservation);
                    popupMenuObsType.inflate(R.menu.popup_cogo_mapcheck_observation_type_position_zero);

                    switch(viewCurrentTypeToDisplay){

                        case 0:
                            popupMenuObsType.getMenu().findItem(R.id.mapcheck_item_1).setChecked(true);
                            break;

                        case 1:
                            popupMenuObsType.getMenu().findItem(R.id.mapcheck_item_2).setChecked(true);
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
                                    ////Swap to Line by Azimuth
                                    viewCurrentTypeToDisplay = 1;
                                    swapTypeOfObservation(1);
                                    break;

                            }

                            return true;
                        }
                    });

                    popupMenuObsType.show();
                }
            });
        }else{
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
                                    ////Swap to Line by Azimuth
                                    viewCurrentTypeToDisplay = 1;
                                    swapTypeOfObservation(1);
                                    break;

                                case R.id.mapcheck_item_3:
                                    //Swap to Line by Turned Angle
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

                            }

                            return true;
                        }
                    });

                    popupMenuObsType.show();
                }
            });
        }
    }


    //----------------------------------------------------------------------------------------------//
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
                vListAdd_ha_azimuth.setVisibility(View.VISIBLE);
                initLayoutHaAzimuth();
                break;

            case 2:
                vListAdd_ha_turnedAngle.setVisibility(View.VISIBLE);
                initLayoutHaTurnedAngle();
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

    public void swapPointClosingCheckBox(boolean amIToShow){

        if(amIToShow){
            tvIsClosingPoint.setVisibility(View.VISIBLE);
            cbIsClosingPoint.setVisibility(View.VISIBLE);
            cbIsClosingPoint.setChecked(false);

        }else{
            tvIsClosingPoint.setVisibility(View.GONE);
            cbIsClosingPoint.setVisibility(View.GONE);
            cbIsClosingPoint.setChecked(false);
        }

    }


    public void swapPointClosing(boolean isClosingPoint){
        Log.d(TAG, "swapPointClosing: Started...");
        if(isClosingPoint){
            Log.d(TAG, "swapPointClosing: Closing Point Checked, setting stage");
            //Turn off Point Number
            //Set point Number to 0
            int falsePointNumber = 0;

            etMapCheckPointNumber.setVisibility(View.GONE);
            tvPointNumberHeader.setVisibility(View.GONE);
            etMapCheckPointNumber.setText(String.valueOf(falsePointNumber));
            mValuePointNo = 0;
            formClosingPoint = true;

            Log.d(TAG, "btSaveObservation: UnLocked");
            btSaveObservation.setClickable(true);



        }else{
            Log.d(TAG, "swapPointClosing: Closing Point Not Checked, back to normal");
            //Turn on Point Number
            //Allow user to set point number
            tvPointNumberHeader.setVisibility(View.VISIBLE);
            etMapCheckPointNumber.setVisibility(View.VISIBLE);

            etMapCheckPointNumber.setText("");
            formClosingPoint = false;
            Log.d(TAG, "btSaveObservation: Locked");
            btSaveObservation.setClickable(false);

        }

    }

    private void clearAllViews(){
        Log.d(TAG, "clearAllViews: Clearing All Views");
        clearHaBearing();
        clearHaAzimuth();
        clearHaTurnedAng();

        clearCurveADaR();
        clearCurveBDaL();
        clearCurveCRL();

        clearPointData();
        clearMethodVariables();
    }

    private void clearPointData(){
        etMapCheckPointDescription.setText("");
        etMapCheckPointNumber.setText("");
    }

    private void clearHaBearing(){
        Log.d(TAG, "clearHaBearing: Clearing Text");

        if(isHaBearingInit){
            etBearingQuadrant.setText("");
            etBearingDeg.setText("");
            etBearingMin.setText("");
            etBearingSec.setText("");
            etBearingDistance.setText("");

            etBearingQuadrant.requestFocus();
        }

    }

    private void clearHaAzimuth(){
        Log.d(TAG, "clearHaAzimuth: Clearing Text");

       if(isHaAzimuthInit){
           etAzimuthDeg.setText("");
           etAzimuthMin.setText("");
           etAzimuthSec.setText("");
           etAzimuthDistance.setText("");

           etAzimuthDeg.requestFocus();
       }
    }

    private void clearHaTurnedAng(){
        Log.d(TAG, "clearHaTurnedAng: Clearing Text");

        if(isHaTurnedInit){
            etTurnedDeg.setText("");
            etTurnedMin.setText("");
            etTurnedSec.setText("");
            etTurnedDistance.setText("");

            etTurnedDeg.requestFocus();
        }
    }

    private void clearCurveADaR(){
        Log.d(TAG, "clearCurveADaR: Clearing Text");

        if(isCurveDARInit){
            etCurveADeltaDeg.setText("");
            etCurveADeltaMin.setText("");
            etCurveADeltaSec.setText("");
            etCurveARadius.setText("");

            etCurveADeltaDeg.requestFocus();
        }

    }

    private void clearCurveBDaL(){
        Log.d(TAG, "clearCurvebDaL: Clearing Text");

        if(isCurveDALInit){
            etCurveBDeltaDeg.setText("");
            etCurveBDeltaMin.setText("");
            etCurveBDeltaSec.setText("");
            etCurveBLength.setText("");

            etCurveBDeltaDeg.requestFocus();
        }

    }

    private void clearCurveCRL(){
        Log.d(TAG, "clearCurvebDaL: Clearing Text");

        if(isCurveRLInit){
            etCurveCRadius.setText("");
            etCurveCLength.setText("");

            etCurveCRadius.requestFocus();
        }

    }

    private void clearMethodVariables(){
        Log.d(TAG, "clearMethodVariables: Clearing Variables");
        mValuePointNo = 0;
        mValuePointDesc = null;
        mValueDistance = 0;
        mValueBearing = 0;
        mValueAzimuth = 0;
        mValueTurnedAngle = 0;
        mValueCurveDelta = 0;
        mValueCurveRadius = 0;
        mValueCurveLength = 0;
        mValueCurveCB = 0;
        mValueCurveCH = 0;


    }


    private void setTextSizeCurveDirection(boolean isCurveRight, int whichCurve){
        switch (whichCurve){
            case CURVE_A:
                if(isCurveRight){
                    tvCurveARight.setTextSize(TypedValue.COMPLEX_UNIT_PX,mContext.getResources().getDimension(R.dimen.text_size_medium));
                    tvCurveARight.setTextColor(mContext.getResources().getColor(R.color.blue_primary));

                    tvCurveALeft.setTextSize(TypedValue.COMPLEX_UNIT_PX,mContext.getResources().getDimension(R.dimen.text_size_header));
                    tvCurveALeft.setTextColor(mContext.getResources().getColor(R.color.gray));

                }else{
                    tvCurveALeft.setTextSize(TypedValue.COMPLEX_UNIT_PX,mContext.getResources().getDimension(R.dimen.text_size_medium));
                    tvCurveALeft.setTextColor(mContext.getResources().getColor(R.color.blue_primary));

                    tvCurveARight.setTextSize(TypedValue.COMPLEX_UNIT_PX,mContext.getResources().getDimension(R.dimen.text_size_header));
                    tvCurveARight.setTextColor(mContext.getResources().getColor(R.color.gray));

                }


                break;

            case CURVE_B:
                if(isCurveRight){
                    tvCurveBRight.setTextSize(TypedValue.COMPLEX_UNIT_PX,mContext.getResources().getDimension(R.dimen.text_size_medium));
                    tvCurveBRight.setTextColor(mContext.getResources().getColor(R.color.blue_primary));

                    tvCurveBLeft.setTextSize(TypedValue.COMPLEX_UNIT_PX,mContext.getResources().getDimension(R.dimen.text_size_header));
                    tvCurveBLeft.setTextColor(mContext.getResources().getColor(R.color.gray));

                }else{
                    tvCurveBLeft.setTextSize(TypedValue.COMPLEX_UNIT_PX,mContext.getResources().getDimension(R.dimen.text_size_medium));
                    tvCurveBLeft.setTextColor(mContext.getResources().getColor(R.color.blue_primary));

                    tvCurveBRight.setTextSize(TypedValue.COMPLEX_UNIT_PX,mContext.getResources().getDimension(R.dimen.text_size_header));
                    tvCurveBRight.setTextColor(mContext.getResources().getColor(R.color.gray));
                }

                break;

            case CURVE_C:
                if(isCurveRight){
                    tvCurveCRight.setTextSize(TypedValue.COMPLEX_UNIT_PX,mContext.getResources().getDimension(R.dimen.text_size_medium));
                    tvCurveCRight.setTextColor(mContext.getResources().getColor(R.color.blue_primary));

                    tvCurveCLeft.setTextSize(TypedValue.COMPLEX_UNIT_PX,mContext.getResources().getDimension(R.dimen.text_size_header));
                    tvCurveCLeft.setTextColor(mContext.getResources().getColor(R.color.gray));

                }else{
                    tvCurveCLeft.setTextSize(TypedValue.COMPLEX_UNIT_PX,mContext.getResources().getDimension(R.dimen.text_size_medium));
                    tvCurveCLeft.setTextColor(mContext.getResources().getColor(R.color.blue_primary));

                    tvCurveCRight.setTextSize(TypedValue.COMPLEX_UNIT_PX,mContext.getResources().getDimension(R.dimen.text_size_header));
                    tvCurveCRight.setTextColor(mContext.getResources().getColor(R.color.gray));

                }

        }


    }

    //-------------------------------------------------------------------------------------------------------------------------//

    /**
     * Validating, creating point and then saving points!
     */

    public void saveObservation(int position, boolean isFromButton){
        Log.d(TAG, "btSave_onClick ");
        boolean isFormReady = false;
        boolean isFormInError = validateFormForNull();

        Log.d(TAG, "btSave_onClick:isFormInError: " + isFormInError);

        if(!isFormInError){
            if(isFromButton) {
                saveObservationToArray(position);
            }else{
                saveObservationToArray(position);

            }
        }
    }

    public void solveCurve(){
        Log.d(TAG, "solveCurve: Started");
        boolean isCurveReady = false;
        boolean isCurveFormInError = validateCurveForNull();

        Log.d(TAG, "solveCurve: isCurveFormInError: " + isCurveFormInError);

        if(!isCurveFormInError){
            showDialogCurveSolution(solveForCurve());
        }


    }

    private boolean validateCurveForNull(){
        Log.d(TAG, "validateCurveForNull: Started");
        boolean isThereAnError = false;

        switch(viewCurrentTypeToDisplay){
            case 3:  //Delta - Radius
                String dCurveA, mCurveA, sCurveA;

                dCurveA = etCurveADeltaDeg.getText().toString();
                mCurveA = etCurveADeltaMin.getText().toString();
                sCurveA = etCurveADeltaSec.getText().toString();

                if(!StringUtilityHelper.isStringNull(dCurveA) && !StringUtilityHelper.isStringNull(mCurveA) && !StringUtilityHelper.isStringNull(sCurveA)){
                    //Save horizontal angle as a decimal degree
                    mValueCurveDelta = MathHelper.convertPartsToDEC(dCurveA,mCurveA,sCurveA);

                }else {
                    if(StringUtilityHelper.isStringNull(dCurveA)) {
                        etCurveADeltaDeg.setError(mContext.getResources().getString(R.string.cogo_ha_deg_not_entered));
                        isThereAnError = true;
                    }

                    if(StringUtilityHelper.isStringNull(mCurveA)) {
                        etCurveADeltaMin.setError(mContext.getResources().getString(R.string.cogo_ha_min_not_entered));
                        isThereAnError = true;

                    }
                    if(StringUtilityHelper.isStringNull(sCurveA)) {
                        etCurveADeltaSec.setError(mContext.getResources().getString(R.string.cogo_ha_sec_not_entered));
                        isThereAnError = true;
                    }
                }

                String hCurveARadius;
                hCurveARadius = etCurveARadius.getText().toString();

                if(!StringUtilityHelper.isStringNull(hCurveARadius)){
                    //Save horizontal angle as a decimal degree
                    mValueCurveRadius = Double.parseDouble(hCurveARadius);
                }else{
                    etCurveARadius.setError(mContext.getResources().getString(R.string.cogo_distance_not_entered));
                    isThereAnError = true;
                }

                break;

            case 4:  //Delta - Length
                String dCurveB, mCurveB, sCurveB;

                dCurveB = etCurveBDeltaDeg.getText().toString();
                mCurveB = etCurveBDeltaMin.getText().toString();
                sCurveB = etCurveBDeltaSec.getText().toString();

                if(!StringUtilityHelper.isStringNull(dCurveB) && !StringUtilityHelper.isStringNull(mCurveB) && !StringUtilityHelper.isStringNull(sCurveB)){
                    //Save horizontal angle as a decimal degree
                    mValueCurveDelta = MathHelper.convertPartsToDEC(dCurveB,mCurveB,sCurveB);

                }else {
                    if(StringUtilityHelper.isStringNull(dCurveB)) {
                        etCurveBDeltaDeg.setError(mContext.getResources().getString(R.string.cogo_ha_deg_not_entered));
                        isThereAnError = true;
                    }

                    if(StringUtilityHelper.isStringNull(mCurveB)) {
                        etCurveBDeltaMin.setError(mContext.getResources().getString(R.string.cogo_ha_min_not_entered));
                        isThereAnError = true;

                    }
                    if(StringUtilityHelper.isStringNull(sCurveB)) {
                        etCurveBDeltaSec.setError(mContext.getResources().getString(R.string.cogo_ha_sec_not_entered));
                        isThereAnError = true;
                    }
                }

                String hCurveBLength;
                hCurveBLength = etCurveBLength.getText().toString();

                if(!StringUtilityHelper.isStringNull(hCurveBLength)){
                    //Save horizontal angle as a decimal degree
                    mValueCurveLength = Double.parseDouble(hCurveBLength);
                }else{
                    etCurveBLength.setError(mContext.getResources().getString(R.string.cogo_distance_not_entered));
                    isThereAnError = true;
                }
                break;

            case 5:  //Curve - Radius and Length

                String hCurveCRadius;
                hCurveCRadius = etCurveCRadius.getText().toString();

                if(!StringUtilityHelper.isStringNull(hCurveCRadius)){
                    //Save horizontal angle as a decimal degree
                    mValueCurveRadius = Double.parseDouble(hCurveCRadius);
                }else{
                    etCurveCRadius.setError(mContext.getResources().getString(R.string.cogo_distance_not_entered));
                    isThereAnError = true;
                }

                String hCurveCLength;
                hCurveCLength = etCurveCLength.getText().toString();

                if(!StringUtilityHelper.isStringNull(hCurveCLength)){
                    //Save horizontal angle as a decimal degree
                    mValueCurveLength = Double.parseDouble(hCurveCLength);
                }else{
                    etCurveCLength.setError(mContext.getResources().getString(R.string.cogo_distance_not_entered));
                    isThereAnError = true;
                }

                break;

            case 6:

                break;

            default:
                break;

        }

        Log.d(TAG, "validateFormForNull: Results: " + isThereAnError);

        return isThereAnError;
    }

    private boolean validateFormForNull() {
        Log.d(TAG, "validateForm: Started...");
        boolean isThereAnError = false;

        //Point No.
        
        if(!formClosingPoint){
            Log.d(TAG, "validateFormForNull: Closing Point False");
            String pointNo = null;
            pointNo = etMapCheckPointNumber.getText().toString();

            if(!StringUtilityHelper.isStringNull(pointNo)){
                mValuePointNo = Integer.parseInt(pointNo);
            }else{
                etMapCheckPointNumber.setError(mContext.getResources().getString(R.string.cogo_point_no_not_entered));
                isThereAnError = true;
            }
        }
        
        //Description
        String pointDescription = null;
        pointDescription = etMapCheckPointDescription.getText().toString();

        if(!StringUtilityHelper.isStringNull(pointDescription)){
            mValuePointDesc = pointDescription;
        }else{
            etMapCheckPointDescription.setError(mContext.getResources().getString(R.string.cogo_description_not_entered));
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
                        etBearingQuadrant.setError(mContext.getResources().getString(R.string.cogo_ha_quadrant_not_entered));
                        isThereAnError = true;
                    }


                    if(StringUtilityHelper.isStringNull(d)) {
                        etBearingDeg.setError(mContext.getResources().getString(R.string.cogo_ha_deg_not_entered));
                        isThereAnError = true;
                    }

                    if(StringUtilityHelper.isStringNull(m)) {
                        etBearingMin.setError(mContext.getResources().getString(R.string.cogo_ha_min_not_entered));
                        isThereAnError = true;

                    }
                    if(StringUtilityHelper.isStringNull(s)) {
                        etBearingSec.setError(mContext.getResources().getString(R.string.cogo_ha_sec_not_entered));
                        isThereAnError = true;
                    }
                }

                String hDistance;
                hDistance = etBearingDistance.getText().toString();

                if(!StringUtilityHelper.isStringNull(hDistance)){
                    //Save horizontal angle as a decimal degree
                    mValueDistance = Double.parseDouble(hDistance);
                }else{
                    etBearingDistance.setError(mContext.getResources().getString(R.string.cogo_distance_not_entered));
                    isThereAnError = true;
                }

                break;

            case 1:  //Azimuth
                String dAz, mAz, sAz;

                dAz = etAzimuthDeg.getText().toString();
                mAz = etAzimuthMin.getText().toString();
                sAz = etAzimuthSec.getText().toString();

                if(!StringUtilityHelper.isStringNull(dAz) && !StringUtilityHelper.isStringNull(mAz) && !StringUtilityHelper.isStringNull(sAz)){
                    //Save horizontal angle as a decimal degree
                    mValueAzimuth = MathHelper.convertPartsToDEC(dAz,mAz,sAz);

                }else {
                    if(StringUtilityHelper.isStringNull(dAz)) {
                        etAzimuthDeg.setError(mContext.getResources().getString(R.string.cogo_ha_deg_not_entered));
                        isThereAnError = true;
                    }

                    if(StringUtilityHelper.isStringNull(mAz)) {
                        etAzimuthMin.setError(mContext.getResources().getString(R.string.cogo_ha_min_not_entered));
                        isThereAnError = true;

                    }
                    if(StringUtilityHelper.isStringNull(sAz)) {
                        etAzimuthSec.setError(mContext.getResources().getString(R.string.cogo_ha_sec_not_entered));
                        isThereAnError = true;
                    }
                }

                String hAzDistance;
                hAzDistance = etAzimuthDistance.getText().toString();

                if(!StringUtilityHelper.isStringNull(hAzDistance)){
                    //Save horizontal angle as a decimal degree
                    mValueDistance = Double.parseDouble(hAzDistance);
                }else{
                    etAzimuthDistance.setError(mContext.getResources().getString(R.string.cogo_distance_not_entered));
                    isThereAnError = true;
                }

                break;

            case 2: //Turned Angle
                String dTurn, mTurn, sTurn;

                dTurn = etTurnedDeg.getText().toString();
                mTurn = etTurnedMin.getText().toString();
                sTurn = etTurnedSec.getText().toString();

                if(!StringUtilityHelper.isStringNull(dTurn) && !StringUtilityHelper.isStringNull(mTurn) && !StringUtilityHelper.isStringNull(sTurn)){
                    //Save horizontal angle as a decimal degree
                    mValueTurnedAngle = MathHelper.convertPartsToDEC(dTurn,mTurn,sTurn);

                }else {
                    if(StringUtilityHelper.isStringNull(dTurn)) {
                        etTurnedDeg.setError(mContext.getResources().getString(R.string.cogo_ha_deg_not_entered));
                        isThereAnError = true;
                    }

                    if(StringUtilityHelper.isStringNull(mTurn)) {
                        etTurnedMin.setError(mContext.getResources().getString(R.string.cogo_ha_min_not_entered));
                        isThereAnError = true;

                    }
                    if(StringUtilityHelper.isStringNull(sTurn)) {
                        etTurnedSec.setError(mContext.getResources().getString(R.string.cogo_ha_sec_not_entered));
                        isThereAnError = true;
                    }
                }

                String hTurnDistance;
                hTurnDistance = etTurnedDistance.getText().toString();

                if(!StringUtilityHelper.isStringNull(hTurnDistance)){
                    //Save horizontal angle as a decimal degree
                    mValueDistance = Double.parseDouble(hTurnDistance);
                }else{
                    etTurnedDistance.setError(mContext.getResources().getString(R.string.cogo_distance_not_entered));
                    isThereAnError = true;
                }

                break;

            case 3:  //Delta - Radius
                String dCurveA, mCurveA, sCurveA;

                dCurveA = etCurveADeltaDeg.getText().toString();
                mCurveA = etCurveADeltaMin.getText().toString();
                sCurveA = etCurveADeltaSec.getText().toString();

                if(!StringUtilityHelper.isStringNull(dCurveA) && !StringUtilityHelper.isStringNull(mCurveA) && !StringUtilityHelper.isStringNull(sCurveA)){
                    //Save horizontal angle as a decimal degree
                    mValueCurveDelta = MathHelper.convertPartsToDEC(dCurveA,mCurveA,sCurveA);

                }else {
                    if(StringUtilityHelper.isStringNull(dCurveA)) {
                        etCurveADeltaDeg.setError(mContext.getResources().getString(R.string.cogo_ha_deg_not_entered));
                        isThereAnError = true;
                    }

                    if(StringUtilityHelper.isStringNull(mCurveA)) {
                        etCurveADeltaMin.setError(mContext.getResources().getString(R.string.cogo_ha_min_not_entered));
                        isThereAnError = true;

                    }
                    if(StringUtilityHelper.isStringNull(sCurveA)) {
                        etCurveADeltaSec.setError(mContext.getResources().getString(R.string.cogo_ha_sec_not_entered));
                        isThereAnError = true;
                    }
                }

                String hCurveARadius;
                hCurveARadius = etCurveARadius.getText().toString();

                if(!StringUtilityHelper.isStringNull(hCurveARadius)){
                    //Save horizontal angle as a decimal degree
                    mValueCurveRadius = Double.parseDouble(hCurveARadius);
                }else{
                    etCurveARadius.setError(mContext.getResources().getString(R.string.cogo_distance_not_entered));
                    isThereAnError = true;
                }

                break;

            case 4:  //Delta - Length
                String dCurveB, mCurveB, sCurveB;

                dCurveB = etCurveBDeltaDeg.getText().toString();
                mCurveB = etCurveBDeltaMin.getText().toString();
                sCurveB = etCurveBDeltaSec.getText().toString();

                if(!StringUtilityHelper.isStringNull(dCurveB) && !StringUtilityHelper.isStringNull(mCurveB) && !StringUtilityHelper.isStringNull(sCurveB)){
                    //Save horizontal angle as a decimal degree
                    mValueCurveDelta = MathHelper.convertPartsToDEC(dCurveB,mCurveB,sCurveB);

                }else {
                    if(StringUtilityHelper.isStringNull(dCurveB)) {
                        etCurveBDeltaDeg.setError(mContext.getResources().getString(R.string.cogo_ha_deg_not_entered));
                        isThereAnError = true;
                    }

                    if(StringUtilityHelper.isStringNull(mCurveB)) {
                        etCurveBDeltaMin.setError(mContext.getResources().getString(R.string.cogo_ha_min_not_entered));
                        isThereAnError = true;

                    }
                    if(StringUtilityHelper.isStringNull(sCurveB)) {
                        etCurveBDeltaSec.setError(mContext.getResources().getString(R.string.cogo_ha_sec_not_entered));
                        isThereAnError = true;
                    }
                }

                String hCurveBLength;
                hCurveBLength = etCurveBLength.getText().toString();

                if(!StringUtilityHelper.isStringNull(hCurveBLength)){
                    //Save horizontal angle as a decimal degree
                    mValueCurveLength = Double.parseDouble(hCurveBLength);
                }else{
                    etCurveBLength.setError(mContext.getResources().getString(R.string.cogo_distance_not_entered));
                    isThereAnError = true;
                }
                break;

            case 5:  //Curve - Radius and Length

                String hCurveCRadius;
                hCurveCRadius = etCurveCRadius.getText().toString();

                if(!StringUtilityHelper.isStringNull(hCurveCRadius)){
                    //Save horizontal angle as a decimal degree
                    mValueCurveRadius = Double.parseDouble(hCurveCRadius);
                }else{
                    etCurveCRadius.setError(mContext.getResources().getString(R.string.cogo_distance_not_entered));
                    isThereAnError = true;
                }

                String hCurveCLength;
                hCurveCLength = etCurveCLength.getText().toString();

                if(!StringUtilityHelper.isStringNull(hCurveCLength)){
                    //Save horizontal angle as a decimal degree
                    mValueCurveLength = Double.parseDouble(hCurveCLength);
                }else{
                    etCurveCLength.setError(mContext.getResources().getString(R.string.cogo_distance_not_entered));
                    isThereAnError = true;
                }

                break;

            case 6:

                break;

            default:
                break;

        }

        Log.d(TAG, "validateFormForNull: Results: " + isThereAnError);
        return isThereAnError;
    }

    private void saveObservationToArray(int position){
        Log.d(TAG, "saveObservationToArray: Started...");

        PointMapCheck mapCheck = new PointMapCheck();

        switch(viewCurrentTypeToDisplay) {
            case 0: // Bearing:
                mapCheck.setObservationType(0);
                mapCheck.setLineAngle(mValueBearing);
                mapCheck.setLineDistance(mValueDistance);
                break;

            case 1: //Azimuth
                mapCheck.setObservationType(1);
                mapCheck.setLineAngle(mValueAzimuth);
                mapCheck.setLineDistance(mValueDistance);
                break;

            case 2: //Turned Angle
                mapCheck.setObservationType(2);
                mapCheck.setLineAngle(mValueTurnedAngle);
                mapCheck.setLineDistance(mValueDistance);
                break;

            case 3: //Curve Delta/Radius
                mapCheck.setObservationType(3);
                mapCheck.setCurveToRight(isSwitchCurveAIsRight);
                mapCheck.setCurveDelta(mValueCurveDelta);
                mapCheck.setCurveRadius(mValueCurveRadius);

                mapCheck.setCurveLength(MathHelper.solveForCurveLength(mValueCurveDelta,mValueCurveRadius));
                mapCheck.setCurveChord(MathHelper.solveForCurveChordDistance(mValueCurveDelta,mValueCurveRadius));

                break;

            case 4: //Curve Delta/Length
                mapCheck.setObservationType(4);
                mapCheck.setCurveToRight(isSwitchCurveBIsRight);

                mapCheck.setCurveDelta(mValueCurveDelta);
                mValueCurveRadius = MathHelper.solveForCurveRadius(mValueCurveDelta,mValueCurveLength);

                mapCheck.setCurveRadius(mValueCurveRadius);
                mapCheck.setCurveLength(mValueCurveLength);

                mapCheck.setCurveChord(MathHelper.solveForCurveChordDistance(mValueCurveDelta,mValueCurveRadius));


                break;

            case 5: //Curve Radius/Length
                mapCheck.setObservationType(5);
                mapCheck.setCurveToRight(isSwitchCurveCIsRight);

                mValueCurveDelta = MathHelper.solveForCurveDeltaAngle(mValueCurveRadius, mValueCurveLength);
                mapCheck.setCurveDelta(mValueCurveDelta);

                mapCheck.setCurveRadius(mValueCurveRadius);
                mapCheck.setCurveLength(mValueCurveLength);

                mapCheck.setCurveChord(MathHelper.solveForCurveChordDistance(mValueCurveDelta,mValueCurveRadius));

                break;

            default:
                break;
        }


        mapCheck.setPointDescription(mValuePointDesc);
        mapCheck.setToPointNo(mValuePointNo);
        mapCheck.setToPointNorth(0d);
        mapCheck.setToPointEast(0d);
        mapCheck.setClosingPoint(formClosingPoint);

        mapcheckListener.sendNewMapcheckToActivity(mapCheck, position);
        clearAllViews();

        mapcheckListener.hideKeyboard();

    }

    public void cancelObservation(int position){
        mapcheckListener.deleteNewMapcheckUserCancel(position);
    }

    //----------------------------------------------------------------------------------------------//

    /**
     * Point Numbers
     *
     */

    private boolean checkPointNumberFromArrayList(int pointNumber){
        boolean doesPointExist;
        HashMap pointMap = new HashMap();

        lstPointMapCheck = mapcheckListener.getPointMapCheck();

        for(int i=0; i<lstPointMapCheck.size(); i++) {
            PointMapCheck pointMapCheck = lstPointMapCheck.get(i);

            String pointListPointNo = Integer.toString(pointMapCheck.getToPointNo());

            pointMap.put(pointListPointNo, pointMapCheck);

        }

        if(pointMap.containsKey(String.valueOf(pointNumber))) {
            doesPointExist = true;
        }else{
            Log.d(TAG, "loadSetup: Point Does not exists");
            doesPointExist = false;

        }


        return doesPointExist;
    }

    private void checkPointNumberFromDatabase(int pointNumber){
        Log.d(TAG, "checkPointNumber: Started...");
        BackgroundSurveyPointExistInDatabase backgroundSurveyPointExistInDatabase = new BackgroundSurveyPointExistInDatabase(mContext,jobDatabaseName,this,pointNumber);
        backgroundSurveyPointExistInDatabase.execute();

    }

    //----------------------------------------------------------------------------------------------//

    private void showToast(String data, boolean shortTime) {

        if (shortTime) {
            Toast.makeText(mContext, data, Toast.LENGTH_SHORT).show();

        } else{
            Toast.makeText(mContext, data, Toast.LENGTH_LONG).show();

        }
    }

    private void hideKeypadFromET (EditText etView) {
        Log.d(TAG, "hideKeypadFromET: Started...");
        InputMethodManager imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
        try{
            Log.d(TAG, "hideKeypadFromET: Edit Text Found!");
            imm.hideSoftInputFromWindow(etView.getWindowToken(), 0);

        }catch (Exception e){

        }

    }

    private void hideKeypadFromUI(Context context, View view){
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Activity.INPUT_METHOD_SERVICE);

        try{
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);

        }catch (Exception e){

        }

    }

    //----------------------------------------------------------------------------------------------//
    /**
     * Listeners
     */

    @Override
    public void doesPointExist(int pointNumber, boolean isPointFoundInDatabase) {
        boolean isPointFoundInArray = false;

        if(!isPointFoundInDatabase){
            //point is not found, look in array and see if it exists
            isPointFoundInArray = checkPointNumberFromArrayList(pointNumber);
        }

        if(isPointFoundInDatabase || isPointFoundInArray){
            //point exists in either the database or array, throw flag
                etMapCheckPointNumber.setError(mContext.getResources().getString(R.string.cogo_point_no_exists));
            Log.d(TAG, "does point exist:btSaveObservation: Locked");
                btSaveObservation.setClickable(false);
            }else{
            etMapCheckPointNumber.setError(null);
            Log.d(TAG, "does point exist:btSaveObservation: UnLocked");
            btSaveObservation.setClickable(true);

        }

        if(formClosingPoint){
            Log.d(TAG, "does point exist:btSaveObservation: UnLocked");
            btSaveObservation.setClickable(true);
        }

    }

    //----------------------------------------------------------------------------------------------//
    private CurveSurvey solveForCurve(){
        CurveSurvey curveSurvey = new CurveSurvey();

        switch (viewCurrentTypeToDisplay){
            case 3: //delta-radius
                curveSurvey.setDeltaDEC(mValueCurveDelta);
                curveSurvey.setRadius(mValueCurveRadius);
                curveSurvey.setLength(MathHelper.solveForCurveLength(mValueCurveDelta,mValueCurveRadius));
                curveSurvey.setTangent(MathHelper.solveForCurveTangent(mValueCurveDelta,mValueCurveRadius));
                curveSurvey.setChord(MathHelper.solveForCurveChordDistance(mValueCurveDelta,mValueCurveRadius));

                break;

            case 4: //delta-length
                mValueCurveRadius = MathHelper.solveForCurveRadius(mValueCurveDelta,mValueCurveLength);

                curveSurvey.setDeltaDEC(mValueCurveDelta);
                curveSurvey.setRadius(mValueCurveRadius);
                curveSurvey.setLength(mValueCurveLength);
                curveSurvey.setTangent(MathHelper.solveForCurveTangent(mValueCurveDelta,mValueCurveRadius));
                curveSurvey.setChord(MathHelper.solveForCurveChordDistance(mValueCurveDelta,mValueCurveRadius));

                break;

            case 5: //radius-length
                mValueCurveDelta = MathHelper.solveForCurveDeltaAngle(mValueCurveRadius, mValueCurveLength);
                curveSurvey.setDeltaDEC(mValueCurveDelta);
                curveSurvey.setRadius(mValueCurveRadius);
                curveSurvey.setLength(mValueCurveLength);
                curveSurvey.setTangent(MathHelper.solveForCurveTangent(mValueCurveDelta,mValueCurveRadius));
                curveSurvey.setChord(MathHelper.solveForCurveChordDistance(mValueCurveDelta,mValueCurveRadius));

                break;

        }

        return curveSurvey;

    }

    private void showDialogCurveSolution(CurveSurvey curveSurvey){
        Log.d(TAG, "showToolsCurveDialog: Started...");

        callCurveSolutionDialogListener.showCurveSolutionDialog(curveSurvey);

    }

    //----------------------------------------------------------------------------------------------//

    public int getListPosition() {
        return listPosition;
    }

    public void setListPosition(int listPosition) {
        this.listPosition = listPosition;
    }
}
