package com.survlogic.survlogic.fragment;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.BounceInterpolator;
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
import com.survlogic.survlogic.dialog.DialogJobInversePointList;
import com.survlogic.survlogic.model.PointGeodetic;
import com.survlogic.survlogic.model.PointSurvey;
import com.survlogic.survlogic.utils.SurveyMathHelper;
import com.survlogic.survlogic.utils.PreferenceLoaderHelper;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by chrisfillmore on 5/2/2017.
 */

public class JobPointsInverseFragment extends Fragment {
    private static final String TAG = "JobPointsInverseFragmen";

    private View v;
    private Context mContext;
    private PreferenceLoaderHelper preferenceLoaderHelper;

    private int project_id, job_id, job_settings_id = 1;
    private String jobDatabaseName;

    private ArrayAdapter<String> pointAdapter;
    private ArrayList<PointSurvey> lstSelectedPoints = new ArrayList<>();
    private ArrayList<PointSurvey> lstPointSurvey = new ArrayList<>();
    private ArrayList<PointGeodetic> lstPointGeodetic = new ArrayList<>();

    private PointSurvey storedFromPoint, storedToPoint;

    private ArrayList<String> pointListFind = new ArrayList<>();
    private HashMap<String,PointSurvey> pointMap = new HashMap<>();

    private RelativeLayout rlResults;
    private AutoCompleteTextView tvFromPointNo, tvToPointNo;
    private Button btnCalculate;
    private ImageButton ibtFromList, ibtToList;
    private TextView tvFromNorthing, tvFromEasting;
    private TextView tvToNorthing, tvToEasting;

    private TextView tvDirection, tvHzDistance, tvSlDistance, tvVtDelta;
    private TextView tvDeltaNorth, tvDeltaEast;

    boolean isPointsLoaded = false, isFromPointLoaded = false, isToPointLoaded = false;
    private static DecimalFormat COORDINATE_FORMATTER, DISTANCE_PRECISION_FORMATTER;
    private int DISPLAY_DIRECTION;

    private static final int display_type_azimuth = 1, display_type_bearing = 0;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.fragment_job_points_inverse, container, false);

        mContext = getActivity();
        preferenceLoaderHelper = new PreferenceLoaderHelper(mContext);

        initViewWidgets(v);
        setOnClickListener(v);
        loadPreferences();
        
        return v;
    }
    
    private void initViewWidgets(View v){
        Log.d(TAG, "initViewWidgets: Started");

        Bundle extras = getArguments();
        project_id = extras.getInt(getString(R.string.KEY_PROJECT_ID));
        job_id = extras.getInt(getString(R.string.KEY_JOB_ID));
        jobDatabaseName = extras.getString(getString(R.string.KEY_JOB_DATABASE));
        Log.d(TAG, "Database in Point List: " + jobDatabaseName);

        rlResults = (RelativeLayout) v.findViewById(R.id.rlResults);
        animateViewSlideVisible(rlResults,false,1);

        tvFromPointNo = (AutoCompleteTextView) v.findViewById(R.id.left_item1_value);
        tvFromPointNo.setSelectAllOnFocus(true);
        tvFromPointNo.setOnFocusChangeListener(focusListener);

        tvFromNorthing = (TextView) v.findViewById(R.id.right_extra1_value);
        tvFromEasting = (TextView) v.findViewById(R.id.right_extra1a_value);

        tvToPointNo = (AutoCompleteTextView) v.findViewById(R.id.left_item2_value);
        tvToPointNo.setSelectAllOnFocus(true);
        tvToPointNo.setOnFocusChangeListener(focusListener);

        tvToNorthing = (TextView) v.findViewById(R.id.right_extra4_value);
        tvToEasting = (TextView) v.findViewById(R.id.right_extra4a_value);

        btnCalculate = (Button) v.findViewById(R.id.calculate_inverse);

        ibtFromList = (ImageButton) v.findViewById(R.id.inverse_from_list);
        ibtToList = (ImageButton) v.findViewById(R.id.inverse_to_list);

        tvDirection = (TextView) v.findViewById(R.id.left_results1_value);
        tvHzDistance = (TextView) v.findViewById(R.id.left_results2_value);
        tvSlDistance = (TextView) v.findViewById(R.id.left_results3_value);
        tvVtDelta = (TextView) v.findViewById(R.id.left_results4_value);

        tvDeltaNorth = (TextView) v.findViewById(R.id.right_extra5_value);
        tvDeltaEast = (TextView) v.findViewById(R.id.right_extra5a_value);

    }
    
    private void setOnClickListener(View v){
        Log.d(TAG, "setOnClickListener: Started");

        tvFromPointNo.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String stringPointNo = pointAdapter.getItem(position);

                setPointSurveyFromPointNo(stringPointNo,true);
                hideKeypadFromAutoCompleteET(tvFromPointNo);
                isFromPointLoaded = true;

            }
        });

        ibtFromList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!isPointsLoaded){
                    getPointSurveyArray();
                    isPointsLoaded = true;
                }

                openPointListSelect(true);
            }
        });

        tvToPointNo.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String stringPointNo = pointAdapter.getItem(position);

                setPointSurveyFromPointNo(stringPointNo,false);
                hideKeypadFromAutoCompleteET(tvToPointNo);
                isToPointLoaded = true;
            }
        });


        ibtToList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!isPointsLoaded){
                    getPointSurveyArray();
                    isPointsLoaded = true;
                }
                openPointListSelect(false);

            }
        });

        btnCalculate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(isFromPointLoaded && isFromPointLoaded){
                    calculateInverse();
                }else if(!isFromPointLoaded){
                    showToast(getResources().getString(R.string.points_inverse_from_error_not_entered),true);
                }else if(!isToPointLoaded){
                    showToast(getResources().getString(R.string.points_inverse_to_error_not_entered),true);
                }

            }
        });
    }

    //----------------------------------------------------------------------------------------------//
    private void loadPreferences(){
        Log.d(TAG, "loadPreferences: Started...");

        COORDINATE_FORMATTER = new DecimalFormat(preferenceLoaderHelper.getValueSystemCoordinatesPrecisionDisplay());
        DISTANCE_PRECISION_FORMATTER = new DecimalFormat(preferenceLoaderHelper.getValueSystemDistancePrecisionDisplay());

        DISPLAY_DIRECTION = preferenceLoaderHelper.getFormatAngleHzDisplay();


    }

    private void hideKeypadFromAutoCompleteET (AutoCompleteTextView aedtView) {
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        try{
            imm.hideSoftInputFromWindow(aedtView.getWindowToken(), 0);

        }catch (Exception e){

        }

    }

    private void viewResults(boolean showResults){
        Log.d(TAG, "viewResults: Started...");

        if(showResults){
            animateViewSlideVisible(rlResults,true,300);

        }else{
            animateViewSlideVisible(rlResults,false,300);
        }


    }

    //----------------------------------------------------------------------------------------------//
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

        pointAdapter = new ArrayAdapter<>(getActivity(),android.R.layout.simple_list_item_1,pointListFind);

        tvFromPointNo.setThreshold(1);
        tvFromPointNo.setAdapter(pointAdapter);

        tvToPointNo.setThreshold(1);
        tvToPointNo.setAdapter(pointAdapter);

    }

    private void openPointListSelect(boolean isPointFrom){
        Log.d(TAG, "openPointListSelect: Started...");

        android.support.v4.app.DialogFragment pointListDialog = DialogJobInversePointList.newInstance(project_id, job_id, jobDatabaseName, lstPointSurvey, isPointFrom);
        pointListDialog.show(getFragmentManager(),"dialog_list");

    }

    //-------------------------------------------------------------------------------------------------------------------------//
    /**
     * Getters/Setters
     */

    private void setPointSurveyFromPreDefined(String pointNo, boolean isPointFrom){
        Log.d(TAG, "setPointSurveyFromPointNo: Started");
        PointSurvey currentPointSurvey;


        if(pointMap.containsKey(pointNo)) {
            currentPointSurvey = pointMap.get(pointNo);

            populateSetup(currentPointSurvey, isPointFrom, true);

        }

    }

    private void setPointSurveyFromPointNo(String pointNo, boolean isPointFrom){
        Log.d(TAG, "setPointSurveyFromPointNo: Started");
        PointSurvey currentPointSurvey;


        if(pointMap.containsKey(pointNo)) {
            currentPointSurvey = pointMap.get(pointNo);

            populateSetup(currentPointSurvey, isPointFrom, false);

        }

    }

    public void setPointSurveyFromPointSurvey(PointSurvey pointSurvey, boolean isPointFrom){
        Log.d(TAG, "setPointSurveyFromPointSurvey: Started");

        populateSetup(pointSurvey,isPointFrom, true);

    }

    private void populateSetup(PointSurvey currentPointSurvey, boolean isPointFrom, boolean showPointNo){
        Log.d(TAG, "setPointSurveyFromPointSurvey: Started...");

        double pointNorthing = currentPointSurvey.getNorthing();
        String pointNorthingValue = COORDINATE_FORMATTER.format(pointNorthing);

        double pointEasting = currentPointSurvey.getEasting();
        String pointEastingValue = COORDINATE_FORMATTER.format(pointEasting);


        if(isPointFrom){

            tvFromNorthing.setText(pointNorthingValue);
            tvFromEasting.setText(pointEastingValue);

            storedFromPoint = currentPointSurvey;

            if(showPointNo){
                tvFromPointNo.setText(String.valueOf(currentPointSurvey.getPoint_no()));
            }

            isFromPointLoaded = true;


        }else{
            if(storedFromPoint == currentPointSurvey){
                showToast(getActivity().getResources().getString(R.string.points_inverse_to_error_same_as_from),true);
            }else{
                tvToNorthing.setText(pointNorthingValue);
                tvToEasting.setText(pointEastingValue);

                storedToPoint = currentPointSurvey;

                if(showPointNo){
                    tvToPointNo.setText(String.valueOf(currentPointSurvey.getPoint_no()));
                }

                isToPointLoaded = true;

            }

        }

    }
    //----------------------------------------------------------------------------------------------//
    private void showToast(String data, boolean shortTime) {

        if (shortTime) {
            Toast.makeText(getActivity(), data, Toast.LENGTH_SHORT).show();

        } else{
            Toast.makeText(getActivity(), data, Toast.LENGTH_LONG).show();

        }
    }

    private void animateViewSlideVisible(final View view, boolean toShowView, long duration){
        if(toShowView){
            view.setVisibility(View.VISIBLE);
            view.setAlpha(0.0f);

            view.animate()
                    .setDuration(duration)
                    .setInterpolator(new BounceInterpolator())
                    .translationY(0)
                    .alpha(1.0f)
                    .setListener(null);
        }else{
            view.animate()
                    .setDuration(duration)
                    .translationY(-view.getHeight())
                    .alpha(0.0f)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);
                            view.setVisibility(View.GONE);
                        }
                    });
        }

    }

    //----------------------------------------------------------------------------------------------//
    private void calculateInverse(){
        Log.d(TAG, "calculateInverse: Started");

        double inverseAzimuth = SurveyMathHelper.inverseAzimuthFromPointSurvey(storedFromPoint,storedToPoint);
        double inverseBearing = SurveyMathHelper.inverseBearingFromPointSurvey(storedFromPoint,storedToPoint);

        if(DISPLAY_DIRECTION == display_type_bearing){
            tvDirection.setText(SurveyMathHelper.convertDECtoDMSBearing(inverseBearing,0));
        }else {
            tvDirection.setText(SurveyMathHelper.convertDECtoDMSAzimuth(inverseAzimuth,0));
        }

        double inverseHzDistance = SurveyMathHelper.inverseHzDistanceFromPointSurvey(storedFromPoint, storedToPoint);
        tvHzDistance.setText(DISTANCE_PRECISION_FORMATTER.format(inverseHzDistance));

        double inverseSlDistance = SurveyMathHelper.inverseSlDistanceFromPointSurvey(storedFromPoint, storedToPoint);
        tvSlDistance.setText(DISTANCE_PRECISION_FORMATTER.format(inverseSlDistance));

        tvDeltaNorth.setText(DISTANCE_PRECISION_FORMATTER.format(SurveyMathHelper.inverseCoordinateDeltas(storedFromPoint,storedToPoint,1)));
        tvDeltaEast.setText(DISTANCE_PRECISION_FORMATTER.format(SurveyMathHelper.inverseCoordinateDeltas(storedFromPoint,storedToPoint,2)));
        tvVtDelta.setText(DISTANCE_PRECISION_FORMATTER.format(SurveyMathHelper.inverseCoordinateDeltas(storedFromPoint,storedToPoint,3)));

        viewResults(true);

    }
    //----------------------------------------------------------------------------------------------//
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


    public void setArrayListPointGeodetic(ArrayList<PointGeodetic> lstArray){
        Log.d(TAG, "setArrayListPointGeodetic: Started...");
        lstPointGeodetic.clear();

        this.lstPointGeodetic = lstArray;
        Log.d(TAG, "setArrayListPointGeodetic: Listen: " + lstPointGeodetic.size());
    }

    public void setArrayListPointSurvey(ArrayList<PointSurvey> lstArray){
        Log.d(TAG, "setArrayListPointGeodetic: Started...");
        lstPointSurvey.clear();

        this.lstPointSurvey = lstArray;
        Log.d(TAG, "setArrayListPointGeodetic: Listen: " + lstPointSurvey.size());
    }


}