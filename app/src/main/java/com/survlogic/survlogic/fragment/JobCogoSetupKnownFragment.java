package com.survlogic.survlogic.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.Image;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.survlogic.survlogic.R;
import com.survlogic.survlogic.dialog.DialogJobMapPointList;
import com.survlogic.survlogic.dialog.DialogJobSetupPointList;
import com.survlogic.survlogic.interf.JobCogoFragmentListener;
import com.survlogic.survlogic.interf.JobCogoSetupPointListListener;
import com.survlogic.survlogic.model.JobInformation;
import com.survlogic.survlogic.model.PointGeodetic;
import com.survlogic.survlogic.model.PointSurvey;
import com.survlogic.survlogic.utils.PreferenceLoaderHelper;
import com.survlogic.survlogic.utils.StringUtilityHelper;

import org.w3c.dom.Text;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by chrisfillmore on 5/2/2017.
 */

public class JobCogoSetupKnownFragment extends Fragment {
    private static final String TAG = "JobCogoSetupKnownFragme";
    View v;
    Context mContext;

    private PointSurvey occupyPoint, backsightPoint;

    private ArrayAdapter<String> pointAdapter;
    private ArrayList<PointSurvey> lstPointSurvey = new ArrayList<>();
    private ArrayList<PointGeodetic> lstPointGeodetic = new ArrayList<>();
    private ArrayList<String> pointListFind = new ArrayList<>();
    private HashMap<String,PointSurvey> pointMap = new HashMap<>();

    private int project_id, job_id, job_settings_id = 1;
    private String jobDatabaseName;

    private PreferenceLoaderHelper preferenceLoaderHelper;
    private JobCogoFragmentListener jobCogoFragmentListener;

    private static DecimalFormat COORDINATE_FORMATTER, DISTANCE_PRECISION_FORMATTER;

    private RelativeLayout rlOccupyMetadataView, rlBacksightMetadataView;

    private AutoCompleteTextView tvOccupyPointNo, tvBacksightPointNo;
    private EditText etOccupyHeight, etBacksightHeight;

    private TextView tvOccupyNorthing, tvOccupyEasting, tvOccupyElevation, tvOccupyDesc;
    private TextView tvBacksightNorthing, tvBacksightEasting, tvBacksightElevation, tvBacksightDesc;

    private ImageButton ibtOccupyFromList, ibtBacksightFromList, ibtCardOccupyExpand, ibtCardBacksightExpand;
    private Button btnSetDirection;

    boolean isPointsLoaded = false;
    boolean isPointOccupyDataVisible = false, isPointBacksightDataVisible = false;

    private Animation animCard_1_down_btn, animCard_1_up_btn, animCard_2_down_btn, animCard_2_up_btn;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.tab_content_cogo_setup_known, container, false);
        mContext = getActivity();

        initViewWidgets(v);
        setOnClickListener(v);



        return v;
    }

    @Override
    public void onResume() {
        super.onResume();

        preferenceLoaderHelper = new PreferenceLoaderHelper(mContext);
        loadPreferences();



    }

    private void initViewWidgets(View v){
        Log.d(TAG, "initViewWidgets: Started...");

        rlOccupyMetadataView = (RelativeLayout) v.findViewById(R.id.llOccupy_data);
        rlBacksightMetadataView = (RelativeLayout) v.findViewById(R.id.llBacksight_data);

        tvOccupyPointNo = (AutoCompleteTextView) v.findViewById(R.id.left_item1_value);
        tvOccupyPointNo.setOnFocusChangeListener(focusListener);

        etOccupyHeight = (EditText) v.findViewById(R.id.right_item1_value);
        etOccupyHeight.setSelectAllOnFocus(true);

        etBacksightHeight = (EditText) v.findViewById(R.id.right_item2_value);
        etBacksightHeight.setSelectAllOnFocus(true);

        tvOccupyNorthing = (TextView) v.findViewById(R.id.occupy_Northing);
        tvOccupyEasting = (TextView) v.findViewById(R.id.occupy_Easting);
        tvOccupyElevation = (TextView) v.findViewById(R.id.occupy_Elevation);
        tvOccupyDesc = (TextView) v.findViewById(R.id.occupy_Desc);

        tvBacksightPointNo = (AutoCompleteTextView) v.findViewById(R.id.left_item2_value);
        tvBacksightPointNo.setOnFocusChangeListener(focusListener);

        tvBacksightNorthing = (TextView) v.findViewById(R.id.backsight_Northing);
        tvBacksightEasting = (TextView) v.findViewById(R.id.backsight_Easting);
        tvBacksightElevation = (TextView) v.findViewById(R.id.backsight_Elevation);
        tvBacksightDesc = (TextView) v.findViewById(R.id.backsight_Desc);

        ibtOccupyFromList = (ImageButton) v.findViewById(R.id.occupy_from_list);
        ibtBacksightFromList = (ImageButton) v.findViewById(R.id.backsight_from_list);

        ibtCardOccupyExpand = (ImageButton) v.findViewById(R.id.card_occupy_expand);
        ibtCardBacksightExpand = (ImageButton) v.findViewById(R.id.card_backsight_expand);

        btnSetDirection = (Button) v.findViewById(R.id.btn_set_direction);

        jobCogoFragmentListener = (JobCogoFragmentListener) getActivity();

        animCard_1_down_btn = AnimationUtils.loadAnimation(mContext,R.anim.rotate_card_down);

        animCard_1_down_btn.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                ibtCardOccupyExpand.setImageResource(R.drawable.ic_keyboard_arrow_up);
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
                ibtCardOccupyExpand.setImageResource(R.drawable.ic_keyboard_arrow_down);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        animCard_2_down_btn = AnimationUtils.loadAnimation(mContext,R.anim.rotate_card_down);
        animCard_2_down_btn.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                ibtCardBacksightExpand.setImageResource(R.drawable.ic_keyboard_arrow_up);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        animCard_2_up_btn = AnimationUtils.loadAnimation(mContext, R.anim.rotate_card_up);
        animCard_2_up_btn.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                ibtCardBacksightExpand.setImageResource(R.drawable.ic_keyboard_arrow_down);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    private void setOnClickListener(View v){
        Log.d(TAG, "setOnClickListener: Started...");

        
        tvOccupyPointNo.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String stringPointNo = pointAdapter.getItem(position);

                setPointSurveyFromPointNo(stringPointNo,true);
                hideKeypadFromAutoCompleteET(tvOccupyPointNo);

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


        ibtCardOccupyExpand.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isPointOccupyDataVisible){
                    ibtCardOccupyExpand.startAnimation(animCard_1_down_btn);

                    rlOccupyMetadataView.setVisibility(View.VISIBLE);
                    isPointOccupyDataVisible = true;

                }else{
                    ibtCardOccupyExpand.startAnimation(animCard_1_up_btn);

                    rlOccupyMetadataView.setVisibility(View.GONE);
                    isPointOccupyDataVisible = false;
                }
            }
        });

        tvBacksightPointNo.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String stringPointNo = pointAdapter.getItem(position);

                setPointSurveyFromPointNo(stringPointNo,false);
                hideKeypadFromAutoCompleteET(tvOccupyPointNo);
            }
        });


        ibtBacksightFromList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!isPointsLoaded){
                    getPointSurveyArray();
                    isPointsLoaded = true;
                }
                openPointListSelect(false);

            }
        });


        ibtCardBacksightExpand.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!isPointBacksightDataVisible){
                    ibtCardBacksightExpand.startAnimation(animCard_2_down_btn);

                    rlBacksightMetadataView.setVisibility(View.VISIBLE);
                    isPointBacksightDataVisible = true;


                }else{
                    ibtCardBacksightExpand.startAnimation(animCard_2_up_btn);

                    rlBacksightMetadataView.setVisibility(View.GONE);
                    isPointBacksightDataVisible = false;

                }
            }
        });

        btnSetDirection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setOccupyAndBacksightInActivity();
            }
        });

    }

    public void setPreDefinedSetup(){
        Log.d(TAG, "initPreDefinedSetup: Started...");
        int occupyPointNo = jobCogoFragmentListener.sendOccupyPointNoToFragment();
        int backsightPointNo = jobCogoFragmentListener.sendBacksightPointNoToFragment();

        double occupyHeight = jobCogoFragmentListener.sendOccupyHeightToFragment();
        double backsightHeight = jobCogoFragmentListener.sendBacksightHeightToFragment();

        Log.d(TAG, "initPreDefinedSetup: Occupy Point No: " + occupyPointNo);
        Log.d(TAG, "initPreDefinedSetup: Backsight Point No: " + backsightPointNo);


        if(!isPointsLoaded){
            getPointSurveyArray();
            isPointsLoaded = true;
        }

        Log.d(TAG, "initPreDefinedSetup: Loaded from Activity: " + lstPointSurvey.size());


        if(occupyPointNo !=0){
            setPointSurveyFromPreDefined(String.valueOf(occupyPointNo),true);
        }

        if(backsightPointNo !=0){
            setPointSurveyFromPreDefined(String.valueOf(backsightPointNo),false);
        }

        if (occupyHeight != 0){
            String occupyHeightValue = COORDINATE_FORMATTER.format(occupyHeight);
            etOccupyHeight.setText(occupyHeightValue);

        }

        if (backsightHeight != 0){
            String backsightHeightValue = COORDINATE_FORMATTER.format(backsightHeight);
            etBacksightHeight.setText(backsightHeightValue);

        }




    }

    private void hideKeypadFromAutoCompleteET (AutoCompleteTextView aedtView) {
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        try{
            imm.hideSoftInputFromWindow(aedtView.getWindowToken(), 0);

        }catch (Exception e){

        }

    }

    private void getPointSurveyArray(){
        Log.d(TAG, "getPointSurveyArray: Started...");

        lstPointSurvey = jobCogoFragmentListener.sendPointSurveyToFragment();

        Log.i(TAG, "getPointSurveyArray: Size: " + lstPointSurvey.size());


        for(int i=0; i<lstPointSurvey.size(); i++) {
            PointSurvey pointSurvey = lstPointSurvey.get(i);

            String pointListPointNo = Integer.toString(pointSurvey.getPoint_no());

            pointMap.put(pointListPointNo, pointSurvey);

            pointListFind.add(pointListPointNo);

        }

        //Set arraylist to adapters

        pointAdapter = new ArrayAdapter<>(getActivity(),android.R.layout.simple_list_item_1,pointListFind);

        tvOccupyPointNo.setThreshold(1);
        tvOccupyPointNo.setAdapter(pointAdapter);

        tvBacksightPointNo.setThreshold(1);
        tvBacksightPointNo.setAdapter(pointAdapter);

    }

    private void openPointListSelect(boolean isPointOccupy){
        Log.d(TAG, "openPointListSelect: Started...");

        JobInformation jobInformation = jobCogoFragmentListener.sendJobInformationToFragment();
        
        project_id = jobInformation.getProject_id();
        job_id = jobInformation.getJob_id();
        jobDatabaseName = jobInformation.getJobDatabaseName();
        
        android.support.v4.app.DialogFragment pointListDialog = DialogJobSetupPointList.newInstance(project_id, job_id, jobDatabaseName, lstPointSurvey, isPointOccupy);
        pointListDialog.show(getFragmentManager(),"dialog_list");
        
    }

    //-------------------------------------------------------------------------------------------------------------------------//
    /**
     * Getters/Setters
     */

    private void setPointSurveyFromPreDefined(String pointNo, boolean isOccupyPoint){
        Log.d(TAG, "setPointSurveyFromPointNo: Started");
        PointSurvey currentPointSurvey;


        if(pointMap.containsKey(pointNo)) {
            currentPointSurvey = pointMap.get(pointNo);

            populateSetup(currentPointSurvey, isOccupyPoint, true);

        }

    }

    private void setPointSurveyFromPointNo(String pointNo, boolean isOccupyPoint){
        Log.d(TAG, "setPointSurveyFromPointNo: Started");
        PointSurvey currentPointSurvey;


        if(pointMap.containsKey(pointNo)) {
            currentPointSurvey = pointMap.get(pointNo);

            populateSetup(currentPointSurvey, isOccupyPoint, false);

        }

    }

    public void setPointSurveyFromPointSurvey(PointSurvey pointSurvey, boolean isOccupyPoint){
        Log.d(TAG, "setPointSurveyFromPointSurvey: Started");

        populateSetup(pointSurvey,isOccupyPoint, true);

    }

    private void setOccupyAndBacksightInActivity(){
        Log.d(TAG, "setOccupyAndBacksightInActivity: Started");
        boolean isOccupySet = false, isBacksightSet = false;
        //verification

        if(occupyPoint !=null){
            jobCogoFragmentListener.setOccupyPointSurveyFromFragment(occupyPoint);
            isOccupySet = true;
        }else{
            showToast(getActivity().getResources().getString(R.string.cogo_setup_occupy_error_no_point),true);
            isOccupySet = false;
        }

        if(backsightPoint !=null){
            jobCogoFragmentListener.setBacksightPointSurveyFromFragment(backsightPoint);
            isBacksightSet = true;
        }else{
            showToast(getActivity().getResources().getString(R.string.cogo_setup_backsight_error_no_point),true);
            isBacksightSet = false;
        }

        Double occupyPointHeight=0d, backsightPointHeight=0d;

        if(!StringUtilityHelper.isStringNull(String.valueOf(etOccupyHeight))){
            occupyPointHeight = Double.parseDouble(etOccupyHeight.getText().toString());
        }

        if(!StringUtilityHelper.isStringNull(String.valueOf(etBacksightHeight))){
            backsightPointHeight = Double.parseDouble(etBacksightHeight.getText().toString());
        }

        jobCogoFragmentListener.setOccupyHeightFromFragment(occupyPointHeight);
        jobCogoFragmentListener.setBacksightHeightFromFragment(backsightPointHeight);

        if(isOccupySet && isBacksightSet){
            jobCogoFragmentListener.sendSetupToMainActivity();
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

            if(isOccupyPoint){

                tvOccupyNorthing.setText(pointNorthingValue);
                tvOccupyEasting.setText(pointEastingValue);
                tvOccupyElevation.setText(pointElevationValue);
                tvOccupyDesc.setText(pointDescription);

                occupyPoint = currentPointSurvey;

                if(showPointNo){
                    tvOccupyPointNo.setText(String.valueOf(currentPointSurvey.getPoint_no()));
                }



            }else{
                if(occupyPoint == currentPointSurvey){
                    showToast(getActivity().getResources().getString(R.string.cogo_setup_backsight_error_same_as_occupy),true);
                }else{
                    tvBacksightNorthing.setText(pointNorthingValue);
                    tvBacksightEasting.setText(pointEastingValue);
                    tvBacksightElevation.setText(pointElevationValue);
                    tvBacksightDesc.setText(pointDescription);

                    backsightPoint = currentPointSurvey;

                    if(showPointNo){
                        tvBacksightPointNo.setText(String.valueOf(currentPointSurvey.getPoint_no()));
                    }


                }

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

    private void loadPreferences(){
        Log.d(TAG, "loadPreferences: Started...");

        COORDINATE_FORMATTER = new DecimalFormat(preferenceLoaderHelper.getValueSystemCoordinatesPrecisionDisplay());
        DISTANCE_PRECISION_FORMATTER = new DecimalFormat(preferenceLoaderHelper.getValueSystemDistancePrecisionDisplay());

    }

    //-------------------------------------------------------------------------------------------------------------------------//
    /**
     * Listeners
     */

    private View.OnFocusChangeListener focusListener = new View.OnFocusChangeListener(){
        public void onFocusChange(View v, boolean hasFocus){
            if (hasFocus){
                if(!isPointsLoaded){
                    getPointSurveyArray();
                    isPointsLoaded = true;
                }
            }
        }
    };



}