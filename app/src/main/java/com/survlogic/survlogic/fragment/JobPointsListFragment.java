package com.survlogic.survlogic.fragment;



import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
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
import com.survlogic.survlogic.view.SortablePointGeodeticTableView;
import com.survlogic.survlogic.view.SortablePointSurveyTableView;

import java.util.ArrayList;

import de.codecrafters.tableview.listeners.SwipeToRefreshListener;
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

    private ArrayList<PointSurvey> lstPointSurvey = new ArrayList<>();
    private ArrayList<PointGeodetic> lstPointGeodetic = new ArrayList<>();

    private SortablePointSurveyTableView pointSurveyTableView;
    private SortablePointGeodeticTableView pointGeodeticTableView;

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

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.fragment_job_points_list, container, false);

        mContext = getActivity();

        initViewWidgets(v);
        setOnClickListener(v);


        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: Started...");

        showPointsLocal();


    }

    private void initViewWidgets(View v){
        Log.d(TAG, "initViewWidgets: Starting...");
        Bundle extras = getArguments();
        project_id = extras.getInt(getString(R.string.KEY_PROJECT_ID));
        job_id = extras.getInt(getString(R.string.KEY_JOB_ID));
        jobDatabaseName = extras.getString(getString(R.string.KEY_JOB_DATABASE));
        Log.d(TAG, "Database in Point List: " + jobDatabaseName);


        preferenceLoaderHelper = new PreferenceLoaderHelper(mContext);
        jobPointsActivityListener = (JobPointsActivityListener) getActivity();


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

    private void setPointSurveyTableListener(){
        Log.d(TAG, "setPointSurveyTableListener: Started...");
        pointSurveyTableView.setSwipeToRefreshEnabled(true);
        pointSurveyTableView.setSwipeToRefreshListener(new SwipeToRefreshListener() {
            @Override
            public void onRefresh(final RefreshIndicator refreshIndicator) {
                pointSurveyTableView.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        refreshIndicator.hide();
                        refreshPointArray();
                    }
                }, DELAY_TO_REFRESH);
            }
        });

        pointSurveyTableView.addDataClickListener(new PointClickListenerPointSurvey());

        isPointSurveyTableSetup = true;
    }

    private void setPointGeodeticTableListener(){
        Log.d(TAG, "setPointSurveyTableListener: Started...");
        pointGeodeticTableView.setSwipeToRefreshEnabled(true);
        pointGeodeticTableView.setSwipeToRefreshListener(new SwipeToRefreshListener() {
            @Override
            public void onRefresh(final RefreshIndicator refreshIndicator) {
                pointGeodeticTableView.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        refreshIndicator.hide();
                        showToast("I am Geodetic", true);

                    }
                }, DELAY_TO_REFRESH);
            }
        });


        isPointGeodeticTableSetup = true;
    }

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


    private void setTableAdapterPointSurvey(){
        adapterSurvey = new PointSurveyTableDataAdapter(mContext, lstPointSurvey, pointSurveyTableView,12);
        pointSurveyTableView.setDataAdapter(adapterSurvey);

    }


    private void setTableAdapterPointGeodetic(){
        adapterGeodetic = new PointGeodeticTableDataAdapter(mContext, lstPointGeodetic, pointGeodeticTableView, 12);
        pointGeodeticTableView.setDataAdapter(adapterGeodetic);
    }


    public void setArrayListPointSurvey(ArrayList<PointSurvey> lstArray){
        Log.d(TAG, "setArrayListPointGeodetic: Started...");
        lstPointSurvey.clear();

        this.lstPointSurvey = lstArray;
        Log.d(TAG, "setArrayListPointGeodetic: Listen: " + lstPointSurvey.size());
    }

    public void setArrayListPointGeodetic(ArrayList<PointGeodetic> lstArray){
        Log.d(TAG, "setArrayListPointGeodetic: Started...");
        lstPointGeodetic.clear();

        this.lstPointGeodetic = lstArray;
        Log.d(TAG, "setArrayListPointGeodetic: Listen: " + lstPointGeodetic.size());
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
        
        pointSurveyTableView = (SortablePointSurveyTableView) v.findViewById(R.id.tableView_for_Points_Survey);


        if (pointSurveyTableView != null) {

            setTableAdapterPointSurvey();
            //Add Click and Long Click here

        }

        if(!isPointSurveyTableSetup) {
            setPointSurveyTableListener();
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


        pointGeodeticTableView = (SortablePointGeodeticTableView) v.findViewById(R.id.tableView_for_Points_Geodetic);

        if (pointSurveyTableView != null) {

            setTableAdapterPointGeodetic();
            //Add Click and Long Click here

        }

        if(!isPointGeodeticTableSetup) {
            setPointGeodeticTableListener();
        }

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

    private class PointClickListenerPointSurvey implements TableDataClickListener<PointSurvey>{
        @Override
        public void onDataClicked(int rowIndex, PointSurvey clickedData) {
            Log.d(TAG, "onDataClicked: Started...");
            long point_id = clickedData.getId();
            int pointNo = clickedData.getPoint_no();

            Log.d(TAG, "onDataClicked: Point_id: " + point_id);

            android.support.v4.app.DialogFragment pointDialog = DialogJobPointView.newInstance(project_id, job_id, point_id, pointNo, jobDatabaseName);
            pointDialog.show(getFragmentManager(),"dialog");
        }
    }
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

    private void refreshPointArray(){
        jobPointsActivityListener.refreshPointArrays();

        adapterGeodetic.notifyDataSetChanged();
        adapterSurvey.notifyDataSetChanged();


    }

}