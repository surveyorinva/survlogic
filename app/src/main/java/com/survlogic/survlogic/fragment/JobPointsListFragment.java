package com.survlogic.survlogic.fragment;



import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.survlogic.survlogic.R;
import com.survlogic.survlogic.adapter.PointGeodeticTableDataAdapter;
import com.survlogic.survlogic.adapter.PointSurveyTableDataAdapter;
import com.survlogic.survlogic.dialog.DialogJobPointEntryAdd;
import com.survlogic.survlogic.dialog.DialogJobPointView;
import com.survlogic.survlogic.interf.JobPointsActivityListener;
import com.survlogic.survlogic.model.PointGeodetic;
import com.survlogic.survlogic.model.PointSurvey;
import com.survlogic.survlogic.utils.PreferenceLoaderHelper;

import java.text.DecimalFormat;
import java.util.ArrayList;

import de.codecrafters.tableview.listeners.TableDataClickListener;

/**
 * Created by chrisfillmore on 5/2/2017.
 */

public class JobPointsListFragment extends Fragment {

    private static final String TAG = "JobPointsListFragment";

    private static final int WORLD = 0;
    private static final int GRID = 1;
    private static final int PLAN = 2;

    private Context mContext;

    private static final int DELAY_TO_LIST = 100;
    private static final int DELAY_TO_REFRESH = 1500;

    View v;

    private int project_id, job_id, job_settings_id = 1;
    private String jobDatabaseName;

    private JobPointsActivityListener jobPointsActivityListener;

    private PointSurveyTableDataAdapter adapterSurvey;
    private PointGeodeticTableDataAdapter adapterGeodetic;
    private RecyclerView.LayoutManager layoutManagerPointSurvey, layoutManagerPointGeodetic;
    private SwipeRefreshLayout swipeRefreshPointSurvey, swipeRefreshPointGeodetic;
    private RecyclerView mRecyclerViewPointSurvey, mRecyclerViewPointGeodetic;

    private ArrayList<PointSurvey> lstPointSurvey = new ArrayList<>();
    private ArrayList<PointGeodetic> lstPointGeodetic = new ArrayList<>();

    private FloatingActionButton fabNewPoint, fabFilter;

    private RelativeLayout rlViewTableWorld, rlViewTablePlanar;
    private LinearLayout linearLayout_Filter_Search, linearLayout_Filter_Group, linearLayout_Filter_Options;

    private ImageButton ibSwitchToWorld, ibSwitchToGrid, ibSwitchToPlanar;
    private ImageButton ibSelectByTouch, ibSelectByFence, ibSelectByZoom;

    private PreferenceLoaderHelper preferenceLoaderHelper;

    private Animation animExitStageLeftPointSurvey, animEnterStageRightPointSurvey;
    private Animation animExitStageLeftPointGeodetic, animEnterStageRightPointGeodetic;
    private Animation animOpen_1, animOpen_2, animOpen_3, animClose_1, animClose_2, animClose_3;
    private Animation animRotateForward, animRotateBackwards;

    private boolean fabExpanded = false;
    private boolean isPointSurveyTableSetup = false, isPointGeodeticTableSetup = false;
    private static DecimalFormat COORDINATE_FORMATTER, DISTANCE_PRECISION_FORMATTER;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.i(TAG, "onCreateView: Started");
        v = inflater.inflate(R.layout.fragment_job_points_list, container, false);

        mContext = getActivity();
        preferenceLoaderHelper = new PreferenceLoaderHelper(mContext);
        jobPointsActivityListener = (JobPointsActivityListener) getActivity();

        initViewWidgets(v);
        loadPreferences();
        setOnClickListener(v);

        initViewList();

        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.i(TAG, "onResume: Started...");

        showPointsLocal();

        if(adapterSurvey != null){
            if(!swipeRefreshPointSurvey.isRefreshing()){
                swipeRefreshPointSurvey.setRefreshing(true);
            }

            adapterSurvey.swapDataSet(lstPointSurvey);

            if(swipeRefreshPointSurvey.isRefreshing()){
                swipeRefreshPointSurvey.setRefreshing(false);
            }
        }

        if(adapterGeodetic !=null){
            adapterGeodetic.swapDataSet(lstPointGeodetic);
        }

    }

    private void initViewWidgets(View v){
        Log.d(TAG, "initViewWidgets: Starting...");
        Bundle extras = getArguments();
        project_id = extras.getInt(getString(R.string.KEY_PROJECT_ID));
        job_id = extras.getInt(getString(R.string.KEY_JOB_ID));
        jobDatabaseName = extras.getString(getString(R.string.KEY_JOB_DATABASE));
        Log.d(TAG, "Database in Point List: " + jobDatabaseName);

        //------------------------------------------------------------------------------------------//
        rlViewTablePlanar = (RelativeLayout) v.findViewById(R.id.rl_layout_point_survey_table);
        rlViewTableWorld = (RelativeLayout) v.findViewById(R.id.rl_layout_point_geodetic_table);

        linearLayout_Filter_Search = (LinearLayout) v.findViewById(R.id.linearLayout_Touch);
        linearLayout_Filter_Group = (LinearLayout) v.findViewById(R.id.linearLayout_Fence);
        linearLayout_Filter_Options = (LinearLayout) v.findViewById(R.id.linearLayout_Search);

        fabNewPoint = (FloatingActionButton) v.findViewById(R.id.fab_in_job_points);
        fabFilter = (FloatingActionButton) v.findViewById(R.id.fabSelect);

        ibSwitchToWorld = (ImageButton) v.findViewById(R.id.switchToWorld);
        ibSwitchToGrid = (ImageButton) v.findViewById(R.id.switchToGrid);
        ibSwitchToPlanar = (ImageButton) v.findViewById(R.id.switchToPlanar);

        ibSelectByTouch = (ImageButton) v.findViewById(R.id.btSelectByTouch);
        ibSelectByFence = (ImageButton) v.findViewById(R.id.btSelectByFence);
        ibSelectByZoom = (ImageButton) v.findViewById(R.id.btSelectByZoom);
        //------------------------------------------------------------------------------------------//
        swipeRefreshPointSurvey = (SwipeRefreshLayout) v.findViewById(R.id.point_survey_swipe_to_refresh);
        swipeRefreshPointGeodetic = (SwipeRefreshLayout) v.findViewById(R.id.point_geodetic_swipe_to_refresh);

        initSwipeToRefreshPointSurvey();
        initSwipeToRefreshPointGeodetic();

        mRecyclerViewPointSurvey = (RecyclerView) v.findViewById(R.id.tableView_for_Points_Survey);
        mRecyclerViewPointGeodetic = (RecyclerView) v.findViewById(R.id.tableView_for_Points_Geodetic);
        layoutManagerPointSurvey = new LinearLayoutManager(mContext);
        layoutManagerPointGeodetic = new LinearLayoutManager(mContext);
        //------------------------------------------------------------------------------------------//
        animExitStageLeftPointSurvey = AnimationUtils.loadAnimation(mContext,R.anim.anim_transition_to_left_hide);
        animEnterStageRightPointSurvey = AnimationUtils.loadAnimation(mContext,R.anim.anim_transition_from_right_show);

        animExitStageLeftPointGeodetic = AnimationUtils.loadAnimation(mContext,R.anim.anim_transition_to_left_hide);
        animEnterStageRightPointGeodetic = AnimationUtils.loadAnimation(mContext,R.anim.anim_transition_from_right_show);

        animOpen_1 = AnimationUtils.loadAnimation(mContext,R.anim.anim_fab_open_1);
        animOpen_2 = AnimationUtils.loadAnimation(mContext,R.anim.anim_fab_open_2);
        animOpen_3 = AnimationUtils.loadAnimation(mContext,R.anim.anim_fab_open_3);

        animClose_1 = AnimationUtils.loadAnimation(mContext,R.anim.anim_fab_close_1);
        animClose_2 = AnimationUtils.loadAnimation(mContext,R.anim.anim_fab_close_2);
        animClose_3 = AnimationUtils.loadAnimation(mContext,R.anim.anim_fab_close_3);

        animRotateForward = AnimationUtils.loadAnimation(mContext,R.anim.rotate_fab_forward);
        animRotateBackwards = AnimationUtils.loadAnimation(mContext, R.anim.rotate_fab_backward);

    }


    private void loadPreferences(){
        Log.d(TAG, "loadPreferences: Started...");

        COORDINATE_FORMATTER = new DecimalFormat(preferenceLoaderHelper.getValueSystemCoordinatesPrecisionDisplay());
        DISTANCE_PRECISION_FORMATTER = new DecimalFormat(preferenceLoaderHelper.getValueSystemDistancePrecisionDisplay());

    }

    private void initSwipeToRefreshPointSurvey(){
        Log.d(TAG, "initSwipeToRefreshPointSurvey: Started");

        if(!isPointSurveyTableSetup){
            try{
                Log.d(TAG, "initSwipeToRefreshPointSurvey: Set");
                swipeRefreshPointSurvey.setColorSchemeResources(R.color.google_blue, R.color.google_green, R.color.google_red, R.color.google_yellow);

                swipeRefreshPointSurvey.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                showPointSurveyRefresh();

                            }
                        }, 1000);
                    }
                });
            }catch (Exception e){
                System.out.println("Error " + e.getMessage());
            }
            
        }

        isPointSurveyTableSetup = true;

    }

    private void initSwipeToRefreshPointGeodetic(){
        Log.d(TAG, "initSwipeToRefreshPointGeodetic: Started");

        if(!isPointGeodeticTableSetup){
            try{
                swipeRefreshPointGeodetic.setColorSchemeResources(R.color.google_blue, R.color.google_green, R.color.google_red, R.color.google_yellow);

                swipeRefreshPointGeodetic.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                showPointGeodeticRefresh();

                            }
                        }, 1000);
                    }
                });
            }catch (Exception e){
                System.out.println("Error " + e.getMessage());
            }
            
        }

        isPointGeodeticTableSetup = true;

    }

    private void setOnClickListener(View v){
        Log.d(TAG, "setOnClickListener: Starting...");
        fabNewPoint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createPointEntry(project_id,job_id,jobDatabaseName);
            }
        });


        ibSwitchToPlanar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPointSurvey(false);

            }
        });

        ibSwitchToWorld.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPointGeodetic(false);
            }
        });

        animExitStageLeftPointSurvey.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {

                rlViewTablePlanar.clearAnimation();

                rlViewTablePlanar.setVisibility(View.GONE);
                rlViewTablePlanar.setClickable(false);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        animEnterStageRightPointSurvey.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                rlViewTablePlanar.setVisibility(View.INVISIBLE);

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                rlViewTablePlanar.setClickable(true);
                rlViewTablePlanar.setFocusable(true);

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        animExitStageLeftPointGeodetic.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                Log.d(TAG, "onAnimationEnd: ExistStateLeft Started");
                rlViewTableWorld.clearAnimation();

                rlViewTableWorld.setVisibility(View.GONE);

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        animEnterStageRightPointGeodetic.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                rlViewTableWorld.setVisibility(View.INVISIBLE);

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                Log.d(TAG, "onAnimationEnd: PointSurveySwipe going to start");
                rlViewTableWorld.setClickable(true);

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        fabFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (fabExpanded){
                    closeSubMenuSelectFab();
                }else{
                    openSubMenuSelectFab();
                }
            }
        });


    }

    //----------------------------------------------------------------------------------------------//

    private void initViewList(){
        setPointSurveyAdapter();
        setPointGeodeticAdapter();
    }

    private void setPointSurveyAdapter(){
        Log.d(TAG, "setPointSurveyAdapter: Started");

        mRecyclerViewPointSurvey.setLayoutManager(layoutManagerPointSurvey);
        mRecyclerViewPointSurvey.setHasFixedSize(false);

        adapterSurvey = new PointSurveyTableDataAdapter(mContext, lstPointSurvey,COORDINATE_FORMATTER, jobPointsActivityListener);

        mRecyclerViewPointSurvey.setAdapter(adapterSurvey);

    }

    private void setPointGeodeticAdapter(){
        Log.d(TAG, "setPointGeodeticAdapter: Started");


        mRecyclerViewPointGeodetic.setLayoutManager(layoutManagerPointGeodetic);
        mRecyclerViewPointGeodetic.setHasFixedSize(false);

        adapterGeodetic = new PointGeodeticTableDataAdapter(mContext, lstPointGeodetic,COORDINATE_FORMATTER);

        mRecyclerViewPointGeodetic.setAdapter(adapterGeodetic);


    }

    private void showPointSurveyRefresh(){
        Log.d(TAG, "showPointSurveyRefresh: Started");

        jobPointsActivityListener.requestPointSurveyArray();

        if(swipeRefreshPointSurvey.isRefreshing()){
            swipeRefreshPointSurvey.setRefreshing(false);
        }

    }

    private void showPointGeodeticRefresh(){
        Log.d(TAG, "showPointGeodeticRefresh: Started");


        if(swipeRefreshPointGeodetic.isRefreshing()){
            swipeRefreshPointGeodetic.setRefreshing(false);
        }
    }



    //----------------------------------------------------------------------------------------------//
    private void createPointEntry(int project_id, int job_id, String databaseName){
        Log.d(TAG, "createPointEntry: Starting...");
        android.support.v4.app.DialogFragment pointDialog = DialogJobPointEntryAdd.newInstance(R.string.dialog_job_point_name, project_id, job_id, databaseName);
        pointDialog.show(getFragmentManager(),"dialog");

    }


    private void showPointsLocal(){

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                showPointSurvey(true);
            }
        },DELAY_TO_LIST);

    }



    public void setArrayListPointSurvey(ArrayList<PointSurvey> lstArray){
        Log.d(TAG, "setArrayListPointGeodetic: Started...");
        lstPointSurvey = lstArray;

        Log.d(TAG, "setArrayListPointSurvey: Listen: " + lstPointSurvey.size());

        if(adapterSurvey != null){
            Log.d(TAG, "setArrayListPointSurvey: Not Null");
            adapterSurvey.swapDataSet(lstPointSurvey);

        }
    }

    public void setArrayListPointGeodetic(ArrayList<PointGeodetic> lstArray){
        Log.d(TAG, "setArrayListPointGeodetic: Started...");

        this.lstPointGeodetic = lstArray;
        Log.d(TAG, "setArrayListPointGeodetic: Listen: " + lstPointGeodetic.size());

        if(adapterGeodetic !=null){
            Log.d(TAG, "setArrayListPointGeodetic: Not Null");
            adapterGeodetic.swapDataSet(lstPointGeodetic);
        }
    }

    public void setArrayListPointGrid(ArrayList<PointSurvey> lstArray){

    }


    private void showPointSurvey(boolean isFirstLoad){
        Log.d(TAG, "setPointSurvey: Started...");

        setTableViewButtons(PLAN);

        if(!isFirstLoad) {
            rlViewTableWorld.startAnimation(animExitStageLeftPointGeodetic);

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    rlViewTablePlanar.startAnimation(animEnterStageRightPointSurvey);
                }
            },200);

        }


    }

    private void showPointGeodetic(boolean isFirstLoad){
        Log.d(TAG, "setPointGeodetic: Started...");

        setTableViewButtons(WORLD);

        rlViewTablePlanar.startAnimation(animExitStageLeftPointSurvey);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                rlViewTableWorld.startAnimation(animEnterStageRightPointGeodetic);
            }
        },200);
        
    }


    private void setTableViewButtons(int value){

        switch (value){
            case WORLD:
                setWorldButtons(true);
                setGridButtons(false);
                setPlanButtons(false);
                break;

            case GRID:
                setWorldButtons(false);
                setGridButtons(true);
                setPlanButtons(false);
                break;

            case PLAN:
                setWorldButtons(false);
                setGridButtons(false);
                setPlanButtons(true);
                break;

        }

    }

    private void setWorldButtons(boolean isActive){

        if(isActive) {
            ibSwitchToWorld.setClickable(false);
            ibSwitchToWorld.setBackgroundResource(R.drawable.button_ripple_semi_transparent_blue);
        }else{
            ibSwitchToWorld.setClickable(true);
            ibSwitchToWorld.setBackgroundResource(R.drawable.button_ripple_semi_transparent_border);
        }
    }

    private void setGridButtons(boolean isActive){

        if(isActive) {
            ibSwitchToGrid.setClickable(false);
            ibSwitchToGrid.setBackgroundResource(R.drawable.button_ripple_semi_transparent_blue);
        }else{
            ibSwitchToGrid.setClickable(true);
            ibSwitchToGrid.setBackgroundResource(R.drawable.button_ripple_semi_transparent_border);
        }
    }

    private void setPlanButtons(boolean isActive){

        if(isActive) {
            ibSwitchToPlanar.setClickable(false);
            ibSwitchToPlanar.setBackgroundResource(R.drawable.button_ripple_semi_transparent_blue);
        }else{
            ibSwitchToPlanar.setClickable(true);
            ibSwitchToPlanar.setBackgroundResource(R.drawable.button_ripple_semi_transparent_border);
        }
    }

    //----------------------------------------------------------------------------------------------//


    //----------------------------------------------------------------------------------------------//

    private void showToast(String data, boolean shortTime) {

        if (shortTime) {
            Toast.makeText(mContext, data, Toast.LENGTH_SHORT).show();

        } else{
            Toast.makeText(mContext, data, Toast.LENGTH_LONG).show();

        }

    }

    private void openSubMenuSelectFab(){
        //this opens the selection menu and the fabs
        fabFilter.startAnimation(animRotateForward);

        linearLayout_Filter_Search.startAnimation(animOpen_1);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                linearLayout_Filter_Group.startAnimation(animOpen_2);
            }
        },100);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                linearLayout_Filter_Options.startAnimation(animOpen_3);
            }
        },150);

        ibSelectByTouch.setClickable(true);
        ibSelectByFence.setClickable(true);
        ibSelectByZoom.setClickable(true);

        fabExpanded = true;

    }

    public void closeSubMenuSelectFab(){
        // When selection menu is open, this closes the selection menu fab items

        fabFilter.startAnimation(animRotateBackwards);

        linearLayout_Filter_Options.startAnimation(animClose_3);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                linearLayout_Filter_Group.startAnimation(animClose_2);
            }
        },50);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                linearLayout_Filter_Search.startAnimation(animClose_1);
            }
        },100);

        ibSelectByTouch.setClickable(false);
        ibSelectByFence.setClickable(false);
        ibSelectByZoom.setClickable(false);

        fabExpanded = false;
    }


}