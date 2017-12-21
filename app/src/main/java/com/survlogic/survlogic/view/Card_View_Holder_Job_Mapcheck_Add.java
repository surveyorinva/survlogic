package com.survlogic.survlogic.view;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Switch;
import android.widget.TextView;

import com.survlogic.survlogic.R;
import com.survlogic.survlogic.interf.MapcheckListener;
import com.survlogic.survlogic.model.PointMapCheck;
import com.survlogic.survlogic.utils.MathHelper;
import com.survlogic.survlogic.utils.StringUtilityHelper;

import java.util.ArrayList;

/**
 * Created by chrisfillmore on 6/30/2017.
 */

public class Card_View_Holder_Job_Mapcheck_Add extends RecyclerView.ViewHolder  {
    private static final String TAG = "Card_View_Holder_Job_Ma";
    
    public View mCardView;
    private Context mContext;
    private MapcheckListener mapcheckListener;

    //-Add New Observation-//
    private View vListAdd_ha_bearing, vListAdd_ha_turnedAngle, vListAdd_ha_azimuth,
            vListAdd_curve_da_r, vListAdd_curve_da_l, vListAdd_curve_r_l, vListAdd_curve_cb_ch;

    private EditText etMapCheckPointDescription, etMapCheckPointNumber;
    private Switch switchTypeOfObservation;
    private ImageButton ibSwapObservation;
    public Button btSaveObservation, btCancelObservation;

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

    //-Helpers-//
    private static final int textValidationOne = 0, textValidationTwo = 1, textValidationThree = 2, textValidationFour = 4;


    public Card_View_Holder_Job_Mapcheck_Add(View itemView, Context mContext, MapcheckListener mapcheckListener) {
        super(itemView);

        this.mContext = mContext;
        mCardView = itemView;
        this.mapcheckListener = mapcheckListener;
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

        switchTypeOfObservation = (Switch) itemView.findViewById(R.id.type_of_measurement_switch);
        ibSwapObservation = (ImageButton) itemView.findViewById(R.id.typeOfObservation);


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

        etMapCheckPointDescription = (EditText) itemView.findViewById(R.id.point_Description_value);

        etMapCheckPointNumber = (EditText) itemView.findViewById(R.id.point_number);
        etMapCheckPointNumber.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if(actionId == EditorInfo.IME_ACTION_DONE){

                }

                return false;
            }
        });

        btSaveObservation = (Button) itemView.findViewById(R.id.save_observation_button);


        btCancelObservation = (Button) itemView.findViewById(R.id.cancel_observation_button);

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

            btCurveASolve = (Button) itemView.findViewById(R.id.curve_delta_radius_solve);
            isCurveDARInit = true;
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

            btCurveBSolve = (Button) itemView.findViewById(R.id.curve_delta_length_solve);
            isCurveDALInit = true;
        }

    }

    private void initLayoutCurveRadiusAndLength(){

        if(!isCurveRLInit){
            etCurveCRadius = (EditText) itemView.findViewById(R.id.rl_curve_radius);
            etCurveCRadius.setSelectAllOnFocus(true);

            etCurveCLength = (EditText) itemView.findViewById(R.id.rl_curve_length);
            etCurveCLength.setSelectAllOnFocus(true);

            switchCurveCIsRight = (Switch)  itemView.findViewById(R.id.rl_switch_direction);

            btCurveCSolve = (Button) itemView.findViewById(R.id.curve_radius_length_solve);
            isCurveRLInit = true;
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

    public void saveObservation(int position){
        Log.d(TAG, "btSave_onClick ");
        boolean isFormReady = false;
        boolean isFormInError = validateFormForNull();

        Log.d(TAG, "btSave_onClick:isFormInError: " + isFormInError);

        if(!isFormInError){
            saveObservationToArray(position);
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
            etMapCheckPointNumber.setError(mContext.getResources().getString(R.string.cogo_point_no_not_entered));
            isThereAnError = true;
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

    private void saveObservationToArray(int position){
        Log.d(TAG, "saveObservationToArray: Started...");

        PointMapCheck mapCheck = new PointMapCheck();

        switch(viewCurrentTypeToDisplay) {
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

        mapcheckListener.sendNewMapcheckToActivity(mapCheck, position);

    }
    

}
