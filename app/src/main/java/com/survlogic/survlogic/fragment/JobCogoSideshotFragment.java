package com.survlogic.survlogic.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.survlogic.survlogic.R;
import com.survlogic.survlogic.background.BackgroundSurveyPointCheckPointNumber;
import com.survlogic.survlogic.background.BackgroundSurveyPointFindNextNumber;
import com.survlogic.survlogic.dialog.DialogJobSideshotPointList;
import com.survlogic.survlogic.interf.JobCogoFragmentListener;
import com.survlogic.survlogic.interf.JobCogoSideshotPointListener;
import com.survlogic.survlogic.model.PointSurvey;
import com.survlogic.survlogic.utils.MathHelper;
import com.survlogic.survlogic.utils.PreferenceLoaderHelper;
import com.survlogic.survlogic.utils.StringUtilityHelper;

import org.w3c.dom.Text;

import java.util.ArrayList;

/**
 * Created by chrisfillmore on 5/2/2017.
 */

public class JobCogoSideshotFragment extends Fragment implements JobCogoSideshotPointListener{

    private static final String TAG = "JobCogoSideshotFragment";

    private PreferenceLoaderHelper preferenceLoaderHelper;
    private View v;
    private Context mContext;

    private JobCogoFragmentListener jobCogoFragmentListener;

    private static int project_id, job_id;
    private String jobDatabaseName;

    private View viewHAngleDec, viewHAngleDMS, viewHd, viewSdZenith, viewSdVertical, viewSdVDelta;
    private ImageButton ibPointNoAction, ibPointhAngleAction, ibPointDistanceAction;
    private TextView tvHAngleHeader, tvDistanceHeader;

    private EditText etPointNumber, etTargetHeight;
    private EditText etHADeg, etHAMin, etHASec, etHADec;

    private ArrayList<PointSurvey> lstPointSurvey = new ArrayList<>();

    private double mValueHAngleDec;
    private boolean doesPointExist = false;
    private int popupMenuOpen = 0;
    private boolean is2dSurvey = false;
    private int horizontalAngleType = 0;
    private int viewDistanceToDisplay = 0;
    private int enteredPointNumber, nextPointNumber;
    private static final int textValidationTwo = 1, textValidationThree = 2, textValidationFour = 4;

    private JobCogoSideshotPointListener jobCogoSideshotPointListener;

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

        ibPointNoAction = (ImageButton) v.findViewById(R.id.point_Number_action);
        ibPointhAngleAction = (ImageButton) v.findViewById(R.id.hAngle_action);
        ibPointDistanceAction = (ImageButton) v.findViewById(R.id.distance_action);

        etPointNumber = (EditText) v.findViewById(R.id.point_number);
        etPointNumber.setSelectAllOnFocus(true);

        etTargetHeight = (EditText) v.findViewById(R.id.target_height);
        etTargetHeight.setSelectAllOnFocus(true);

        tvHAngleHeader = (TextView) v.findViewById(R.id.hAngle_header);
        tvDistanceHeader = (TextView) v.findViewById(R.id.distance_header);

        jobCogoFragmentListener = (JobCogoFragmentListener) mContext;

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

    }

    //-------------------------------------------------------------------------------------------------------------------------//
    /**
     * Methods
     */

    private void swapHAngleItems(int viewToShow){
        switch (viewToShow){

            case 1:
                //showToast("Switching View to Degrees-Minutes-Seconds",true);

                //Retrieve data if it is there.
                //Going from DEC to DMS

                if(etHADec !=null){
                    String results =  etHADec.getText().toString();

                    if(!StringUtilityHelper.isStringNull(results)){
                        mValueHAngleDec = Double.parseDouble(results);
                    }

                }

                viewHAngleDMS.setVisibility(View.VISIBLE);
                viewHAngleDec.setVisibility(View.GONE);

                horizontalAngleType = 0;

                initViewHADMS();

                break;

            case 2:
                //showToast("Switching View to Decimal Degrees",true);

                if(etHADeg !=null){
                    String resultsDeg = etHADeg.getText().toString();
                    String resultsMin = etHAMin.getText().toString();
                    String resultsSec = etHASec.getText().toString();

                    if(!StringUtilityHelper.isStringNull(resultsDeg) && !StringUtilityHelper.isStringNull(resultsMin) && !StringUtilityHelper.isStringNull(resultsSec)){
                        mValueHAngleDec = MathHelper.convertPartsToDEC(resultsDeg,resultsMin,resultsSec);

                    }

                }

                viewHAngleDec.setVisibility(View.VISIBLE);
                viewHAngleDMS.setVisibility(View.GONE);

                horizontalAngleType = 1;

                initViewHADEC();
        }


    }

    private void swapDistanceItems(int itemToShow){
        switch (itemToShow){

            case 1:
                //showToast("Switching View to Horizontal Distance Entry",true);
                viewHd.setVisibility(View.VISIBLE);
                viewSdZenith.setVisibility(View.GONE);
                viewSdVertical.setVisibility(View.GONE);
                viewSdVDelta.setVisibility(View.GONE);

                is2dSurvey = true;
                viewDistanceToDisplay = 1;

                tvDistanceHeader.setText("HD");

                break;

            case 3:
                //showToast("Switching View to Slope Distance with Zenith Angle Entry",true);
                viewHd.setVisibility(View.GONE);
                viewSdZenith.setVisibility(View.VISIBLE);
                viewSdVertical.setVisibility(View.GONE);
                viewSdVDelta.setVisibility(View.GONE);

                is2dSurvey=false;
                viewDistanceToDisplay = 3;

                tvDistanceHeader.setText("SD");

                break;

            case 4:
                //showToast("Switching View to Slope Distance with Vertical Angle Entry",true);
                viewHd.setVisibility(View.GONE);
                viewSdZenith.setVisibility(View.GONE);
                viewSdVertical.setVisibility(View.VISIBLE);
                viewSdVDelta.setVisibility(View.GONE);

                is2dSurvey=false;
                viewDistanceToDisplay = 4;

                tvDistanceHeader.setText("SD");

                break;

            case 5:
                //showToast("Switching View to Slope Distance with Vertical Difference Entry",true);
                viewHd.setVisibility(View.GONE);
                viewSdZenith.setVisibility(View.GONE);
                viewSdVertical.setVisibility(View.GONE);
                viewSdVDelta.setVisibility(View.VISIBLE);

                is2dSurvey=false;
                viewDistanceToDisplay = 5;

                tvDistanceHeader.setText("SD");

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
                        //s.replace(0,s.length(),"359",0,3);

                        showToast("Error.  Enter between 0 - 360",true);
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
                        s.replace(0,s.length(),"59",0,2);
                        showToast("Error.  Enter between 0 - 59",true);
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
                    int val = Integer.parseInt(s.toString());
                    if(val > 60){
                        s.replace(0,s.length(),"59",0,2);
                        showToast("Error.  Enter between 0 - 59",true);
                    }


                }catch (NumberFormatException ex){

                }
            }
        });


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

        etTargetHeight.setNextFocusDownId(R.id.hAngle_degree);

    }

    private void initViewHADEC(){
        Log.d(TAG, "initViewHADEC: Started...");

        etHADec = (EditText) v.findViewById(R.id.hAngle_dec);

        if(mValueHAngleDec !=0){
            etHADec.setText(String.valueOf(mValueHAngleDec));
        }

        etTargetHeight.setNextFocusDownId(R.id.hAngle_dec);

    }

    private void checkPointNumber(int pointNumber){
        Log.d(TAG, "checkPointNumber: Started...");
        BackgroundSurveyPointCheckPointNumber backgroundSurveyPointCheckPointNumber = new BackgroundSurveyPointCheckPointNumber(mContext,jobDatabaseName,this,pointNumber);
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

        etPointNumber.setText(String.valueOf(pointNumber));

    }
}