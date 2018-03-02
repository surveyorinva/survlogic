package com.survlogic.survlogic.fragment;



import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.survlogic.survlogic.R;
import com.survlogic.survlogic.adapter.ActivityViewPagerAdapter;
import com.survlogic.survlogic.adapter.PointGeodeticTableDataAdapter;
import com.survlogic.survlogic.adapter.PointGridTableDataAdapter;
import com.survlogic.survlogic.adapter.PointSurveyTableDataAdapter;
import com.survlogic.survlogic.dialog.DialogJobPointEntryAdd;
import com.survlogic.survlogic.interf.JobPointsActivityListener;
import com.survlogic.survlogic.model.PointGeodetic;
import com.survlogic.survlogic.model.PointGrid;
import com.survlogic.survlogic.model.PointSurvey;
import com.survlogic.survlogic.utils.PreferenceLoaderHelper;
import com.survlogic.survlogic.utils.SurveyProjectionHelper;

import java.text.DecimalFormat;
import java.util.ArrayList;

import static android.graphics.Color.TRANSPARENT;
import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;

/**
 * Created by chrisfillmore on 5/2/2017.
 */

public class JobPointsListFragment extends Fragment {

    private static final String TAG = "JobPointsListFragment";

    private static final int WORLD = 0;
    private static final int GRID = 1;
    private static final int PLAN = 2;

    private int viewInFocus;

    private Context mContext;

    private static final int DELAY_TO_LIST = 100;
    private static final int DELAY_TO_REFRESH = 1500;

    private View v;

    private int project_id, job_id, job_settings_id = 1;
    private String jobDatabaseName;

    private JobPointsActivityListener jobPointsActivityListener;

    private PointSurveyTableDataAdapter adapterSurvey;
    private PointGeodeticTableDataAdapter adapterGeodetic;
    private PointGridTableDataAdapter adapterGrid;

    private RecyclerView.LayoutManager layoutManagerPointSurvey, layoutManagerPointGeodetic, layoutManagerPointGrid;
    private SwipeRefreshLayout swipeRefreshPointSurvey, swipeRefreshPointGeodetic, swipeRefreshPointGrid;
    private RecyclerView mRecyclerViewPointSurvey, mRecyclerViewPointGeodetic, mRecyclerViewPointGrid;

    private ArrayList<PointSurvey> lstPointSurvey = new ArrayList<>();
    private ArrayList<PointGeodetic> lstPointGeodetic = new ArrayList<>();
    private ArrayList<PointSurvey> lstPointGrid = new ArrayList<>();

    private FloatingActionButton fabOpenFilter, fabOpenNewPoint;

    private RelativeLayout rlViewTableWorld, rlViewTablePlanar, rlViewTableGrid;
    private LinearLayout linearLayout_Filter_Search, linearLayout_Filter_Group, linearLayout_Filter_Options;

    private TextView tvHeaderNorth, tvHeaderEast;

    private ImageButton ibSwitchToWorld, ibSwitchToGrid, ibSwitchToPlanar;
    private ImageButton ibSelectByTouch, ibSelectByFence, ibSelectByZoom;

    private PreferenceLoaderHelper preferenceLoaderHelper;
    private SurveyProjectionHelper surveyProjectionHelper;

    //----------------------------------------------------------------------------------------------//
    private DisplayMetrics mDisplayMetrics;

    private View vReveal, vRevealNew, vContentFilter, vBottomListBackground, vSheetTop;
    private ImageView ivCancel, ivSaveFilter;

    private TabLayout tabHeader;
    private ViewPager vpFilterContent;
    private ActivityViewPagerAdapter viewPagerAdapter;

    //-------------------------------------------------//

    JobPointsFilterPage1Fragment newPointFilterPage1Fragment;

    //-------------------------------------------------//

    private float mStartX;
    private float mStartY;
    private int mBottomY, mBottomYNew;
    private int mBottomX;

    private boolean mIsCancel;
    private float mBottomListStartY;
    private boolean resetBottomList;

    //----------------------------------------------------------------------------------------------//
    private Animation animExitStageLeftPointSurvey, animExitStageLeftPointSurveyHidden, animEnterStageRightPointSurvey;
    private Animation animExitStageLeftPointGeodetic, animExitStageLeftPointGeodeticHidden, animEnterStageRightPointGeodetic;
    private Animation animExitStageLeftPointGrid, animExitStageLeftPointGridHidden, animEnterStageRightPointGrid;
    private Animation animOpen_1, animOpen_2, animOpen_3, animClose_1, animClose_2, animClose_3;
    private Animation animRotateForward, animRotateBackwards;

    private boolean fabExpanded = false;
    private boolean isPointSurveyTableSetup = false, isPointGeodeticTableSetup = false, isPointGridTableSetup = false;
    private static DecimalFormat COORDINATE_FORMATTER, DISTANCE_PRECISION_FORMATTER;

    private int isProjection = 0, isProjectionZone = 0;
    private String mProjectionString, mProjectionZoneString;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.i(TAG, "onCreateView: Started");
        v = inflater.inflate(R.layout.fragment_job_points_list, container, false);

        mContext = getActivity();
        preferenceLoaderHelper = new PreferenceLoaderHelper(mContext);
        surveyProjectionHelper = new SurveyProjectionHelper(mContext);
        jobPointsActivityListener = (JobPointsActivityListener) getActivity();

        setDisplayMetrics();

        initViewWidgets(v);
        loadPreferences();
        setOnClickListener(v);

        initViewList();
        initViewPager();

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
        rlViewTableGrid = (RelativeLayout) v.findViewById(R.id.rl_layout_point_grid_table);

        linearLayout_Filter_Search = (LinearLayout) v.findViewById(R.id.linearLayout_Touch);
        linearLayout_Filter_Group = (LinearLayout) v.findViewById(R.id.linearLayout_Fence);
        linearLayout_Filter_Options = (LinearLayout) v.findViewById(R.id.linearLayout_Search);

        tvHeaderNorth = (TextView) v.findViewById(R.id.observation_Northing);
        tvHeaderEast = (TextView) v.findViewById(R.id.observation_Easting);

        fabOpenFilter = (FloatingActionButton) v.findViewById(R.id.fab_in_job_points);
        fabOpenFilter.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getActivity(), R.color.colorAccent)));

        fabOpenNewPoint = (FloatingActionButton) v.findViewById(R.id.fabSelect);

        ibSwitchToWorld = (ImageButton) v.findViewById(R.id.switchToWorld);
        ibSwitchToGrid = (ImageButton) v.findViewById(R.id.switchToGrid);
        ibSwitchToPlanar = (ImageButton) v.findViewById(R.id.switchToPlanar);

        ibSelectByTouch = (ImageButton) v.findViewById(R.id.btSelectByTouch);
        ibSelectByFence = (ImageButton) v.findViewById(R.id.btSelectByFence);
        ibSelectByZoom = (ImageButton) v.findViewById(R.id.btSelectByZoom);

        //------------------------------------------------------------------------------------------//
        vBottomListBackground = v.findViewById(R.id.bottom_list_background);
        Drawable d = vBottomListBackground.getBackground();
        final GradientDrawable gd = (GradientDrawable) d;
        gd.setCornerRadius(0f);

        vReveal = v.findViewById(R.id.reveal_container);
        vRevealNew = v.findViewById(R.id.revealNew);

        vContentFilter = v.findViewById(R.id.content_container);
        vSheetTop = v.findViewById(R.id.sheetTop);

        tabHeader = (TabLayout) v.findViewById(R.id.tabScroll_layout);
        vpFilterContent = (ViewPager) v.findViewById(R.id.viewpager_in_content_container);

        ivSaveFilter = (ImageView) v.findViewById(R.id.iv_add_new_point);
        ivCancel = (ImageView) v.findViewById(R.id.iv_cancel);

        //------------------------------------------------------------------------------------------//
        swipeRefreshPointSurvey = (SwipeRefreshLayout) v.findViewById(R.id.point_survey_swipe_to_refresh);
        swipeRefreshPointGeodetic = (SwipeRefreshLayout) v.findViewById(R.id.point_geodetic_swipe_to_refresh);
        swipeRefreshPointGrid = (SwipeRefreshLayout) v.findViewById(R.id.point_grid_swipe_to_refresh);

        initSwipeToRefreshPointSurvey();
        initSwipeToRefreshPointGeodetic();
        initSwipeToRefreshPointGrid();

        mRecyclerViewPointSurvey = (RecyclerView) v.findViewById(R.id.tableView_for_Points_Survey);
        mRecyclerViewPointGeodetic = (RecyclerView) v.findViewById(R.id.tableView_for_Points_Geodetic);
        mRecyclerViewPointGrid = (RecyclerView) v.findViewById(R.id.tableView_for_Points_Grid);

        layoutManagerPointSurvey = new LinearLayoutManager(mContext);
        layoutManagerPointGeodetic = new LinearLayoutManager(mContext);
        layoutManagerPointGrid = new LinearLayoutManager(mContext);
        //------------------------------------------------------------------------------------------//
        animExitStageLeftPointSurvey = AnimationUtils.loadAnimation(mContext,R.anim.anim_transition_to_left_hide);
        animExitStageLeftPointSurveyHidden= AnimationUtils.loadAnimation(mContext,R.anim.anim_transition_to_left_hide_hidden);
        animEnterStageRightPointSurvey = AnimationUtils.loadAnimation(mContext,R.anim.anim_transition_from_right_show);

        animExitStageLeftPointGeodetic = AnimationUtils.loadAnimation(mContext,R.anim.anim_transition_to_left_hide);
        animExitStageLeftPointGeodeticHidden= AnimationUtils.loadAnimation(mContext,R.anim.anim_transition_to_left_hide_hidden);
        animEnterStageRightPointGeodetic = AnimationUtils.loadAnimation(mContext,R.anim.anim_transition_from_right_show);

        animExitStageLeftPointGrid = AnimationUtils.loadAnimation(mContext,R.anim.anim_transition_to_left_hide);
        animExitStageLeftPointGridHidden= AnimationUtils.loadAnimation(mContext,R.anim.anim_transition_to_left_hide_hidden);
        animEnterStageRightPointGrid = AnimationUtils.loadAnimation(mContext,R.anim.anim_transition_from_right_show);

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

        isProjection = preferenceLoaderHelper.getGeneral_over_projection();
        isProjectionZone = preferenceLoaderHelper.getGeneral_over_zone();

        if(isProjection == 1){
            mProjectionString = preferenceLoaderHelper.getGeneral_over_projection_string();
        }else{
            mProjectionString = getResources().getString(R.string.projection_none);
        }

        if(isProjectionZone == 1){
            mProjectionZoneString = preferenceLoaderHelper.getGeneral_over_zone_string();
        }else{
            mProjectionZoneString = getResources().getString(R.string.projection_zone_none);
        }


        if(isProjection == 1){
            Log.d(TAG, "loadPreferences: ");
            initProjection();
        }

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

    private void initSwipeToRefreshPointGrid(){
        Log.d(TAG, "initSwipeToRefreshPointGrid: Started");

        if(!isPointGridTableSetup){
            try{
                swipeRefreshPointGrid.setColorSchemeResources(R.color.google_blue, R.color.google_green, R.color.google_red, R.color.google_yellow);

                swipeRefreshPointGrid.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                showPointGridRefresh();

                            }
                        }, 1000);
                    }
                });
            }catch (Exception e){
                System.out.println("Error " + e.getMessage());
            }

        }

        isPointGridTableSetup = true;

    }

    private void setOnClickListener(View v){
        Log.d(TAG, "setOnClickListener: Starting...");
        fabOpenFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                animateNewPointView(v);
            }
        });

        ivSaveFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                acceptNewPoint(v);
            }
        });

        ivCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancelNewPoint(v);
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

        ibSwitchToGrid.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPointGrid(false);
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

        animExitStageLeftPointSurveyHidden.setAnimationListener(new Animation.AnimationListener() {
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

        animExitStageLeftPointGeodeticHidden.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                rlViewTableWorld.clearAnimation();

                rlViewTableWorld.setVisibility(View.GONE);
                rlViewTableWorld.setClickable(false);
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

        animExitStageLeftPointGrid.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                rlViewTableGrid.clearAnimation();
                rlViewTableGrid.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        animExitStageLeftPointGridHidden.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                rlViewTableGrid.clearAnimation();

                rlViewTableGrid.setVisibility(View.GONE);
                rlViewTableGrid.setClickable(false);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });


        animEnterStageRightPointGrid.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                rlViewTableGrid.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                rlViewTableGrid.setClickable(true);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });



        fabOpenNewPoint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                revealNewPointActions();
            }
        });


    }


    private void clearViews(){
        rlViewTableGrid.setVisibility(View.INVISIBLE);
        rlViewTablePlanar.setVisibility(View.INVISIBLE);
        rlViewTableWorld.setVisibility(View.INVISIBLE);
    }


    //----------------------------------------------------------------------------------------------//

    private void initViewPager(){

        viewPagerAdapter = new ActivityViewPagerAdapter(getChildFragmentManager());

        //------------------------------------------------------------------------------------------//

        newPointFilterPage1Fragment = new JobPointsFilterPage1Fragment();

        //------------------------------------------------------------------------------------------//
        viewPagerAdapter.addFragments(newPointFilterPage1Fragment,getResources().getString(R.string.add_new_point_tab_header_1));
        viewPagerAdapter.addFragments(new MainToolsFragment(),getResources().getString(R.string.add_new_point_tab_header_2));
        viewPagerAdapter.addFragments(new MainToolsFragment(),getResources().getString(R.string.add_new_point_tab_header_3));

        vpFilterContent.setAdapter(viewPagerAdapter);
        tabHeader.setupWithViewPager(vpFilterContent);

    }

    //----------------------------------------------------------------------------------------------//

    private void initViewList(){
        setPointSurveyAdapter();
        setPointGeodeticAdapter();
        setPointGridAdapter();
    }


    private void initProjection(){
        Log.d(TAG, "initProjection: Started");

        ibSwitchToGrid.setVisibility(View.VISIBLE);
        surveyProjectionHelper.setConfig(mProjectionString,mProjectionZoneString);


    }

    private void setPointSurveyAdapter(){
        Log.d(TAG, "setPointSurveyAdapter: Started");

        mRecyclerViewPointSurvey.setLayoutManager(layoutManagerPointSurvey);
        mRecyclerViewPointSurvey.setHasFixedSize(false);

        adapterSurvey = new PointSurveyTableDataAdapter(mContext, lstPointSurvey,COORDINATE_FORMATTER, jobPointsActivityListener);

        mRecyclerViewPointSurvey.setAdapter(adapterSurvey);
        runLayoutAnimationFallDown(mRecyclerViewPointSurvey);
    }

    private void setPointGeodeticAdapter(){
        Log.d(TAG, "setPointGeodeticAdapter: Started");


        mRecyclerViewPointGeodetic.setLayoutManager(layoutManagerPointGeodetic);
        mRecyclerViewPointGeodetic.setHasFixedSize(false);

        adapterGeodetic = new PointGeodeticTableDataAdapter(mContext, lstPointGeodetic,COORDINATE_FORMATTER);

        mRecyclerViewPointGeodetic.setAdapter(adapterGeodetic);
        runLayoutAnimationFallDown(mRecyclerViewPointGeodetic);

    }

    private void setPointGridAdapter(){
        Log.d(TAG, "setPointGridAdapter: Started");

        mRecyclerViewPointGrid.setLayoutManager(layoutManagerPointGrid);
        mRecyclerViewPointGrid.setHasFixedSize(true);

        adapterGrid = new PointGridTableDataAdapter(mContext,lstPointGrid,COORDINATE_FORMATTER, jobPointsActivityListener);
        mRecyclerViewPointGrid.setAdapter(adapterGrid);
        runLayoutAnimationFallDown(mRecyclerViewPointGrid);


    }

    private void runLayoutAnimationFallDown(final RecyclerView recyclerView) {
        final Context context = recyclerView.getContext();
        final LayoutAnimationController controller =
                AnimationUtils.loadLayoutAnimation(context, R.anim.anim_recycler_layout_fall_down);

        recyclerView.setLayoutAnimation(controller);
        recyclerView.getAdapter().notifyDataSetChanged();
        recyclerView.scheduleLayoutAnimation();
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

        jobPointsActivityListener.requestPointSurveyArray();

        if(swipeRefreshPointGeodetic.isRefreshing()){
            swipeRefreshPointGeodetic.setRefreshing(false);
        }
    }

    private void showPointGridRefresh(){
        Log.d(TAG, "showPointGridRefresh: Started");

        jobPointsActivityListener.requestPointSurveyArray();

        if(swipeRefreshPointGrid.isRefreshing()){
            swipeRefreshPointGrid.setRefreshing(false);
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

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    runLayoutAnimationFallDown(mRecyclerViewPointSurvey);
                }
            },200);


        }
    }

    public void setArrayListPointGeodetic(ArrayList<PointGeodetic> lstArray){
        Log.d(TAG, "setArrayListPointGeodetic: Started...");

        lstPointGeodetic = lstArray;
        Log.d(TAG, "setArrayListPointGeodetic: Listen: " + lstPointGeodetic.size());

        if(adapterGeodetic !=null){
            Log.d(TAG, "setArrayListPointGeodetic: Not Null");
            adapterGeodetic.swapDataSet(lstPointGeodetic);

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    runLayoutAnimationFallDown(mRecyclerViewPointGeodetic);
                }
            },200);


        }
    }

    public void setArrayListPointGrid(ArrayList<PointSurvey> lstArray){
        Log.d(TAG, "setArrayListPointGrid: Started...");

        lstPointGrid = lstArray;

        if(adapterGrid !=null){
            Log.d(TAG, "setArrayListPointGrid: Not Null");
            adapterGrid.swapDataSet(lstPointGrid);

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    runLayoutAnimationFallDown(mRecyclerViewPointGrid);
                }
            },200);
        }


    }


    private void showPointSurvey(boolean isFirstLoad){
        Log.d(TAG, "setPointSurvey: Started...");

        if(!isFirstLoad) {

            if(viewInFocus == WORLD){
                rlViewTableWorld.startAnimation(animExitStageLeftPointGeodetic);
                rlViewTableGrid.startAnimation(animExitStageLeftPointGridHidden);

            }else if(viewInFocus == GRID){
                rlViewTableWorld.startAnimation(animExitStageLeftPointGeodeticHidden);
                rlViewTableGrid.startAnimation(animExitStageLeftPointGrid);
            }


            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    rlViewTablePlanar.startAnimation(animEnterStageRightPointSurvey);

                }
            },200);

        }

        setTableViewButtons(PLAN);

    }

    private void showPointGeodetic(boolean isFirstLoad){
        Log.d(TAG, "setPointGeodetic: Started...");

        if(viewInFocus == PLAN){
            rlViewTablePlanar.startAnimation(animExitStageLeftPointSurvey);
            rlViewTableGrid.startAnimation(animExitStageLeftPointGridHidden);

        }else if(viewInFocus == GRID){
            rlViewTablePlanar.startAnimation(animExitStageLeftPointSurveyHidden);
            rlViewTableGrid.startAnimation(animExitStageLeftPointGrid);
        }

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                rlViewTableWorld.startAnimation(animEnterStageRightPointGeodetic);
            }
        },200);

        setTableViewButtons(WORLD);

    }

    private void showPointGrid(boolean isFirstLoad){
        Log.d(TAG, "setPointGeodetic: Started...");

        if(viewInFocus == WORLD){
            rlViewTablePlanar.startAnimation(animExitStageLeftPointSurveyHidden);
            rlViewTableWorld.startAnimation(animExitStageLeftPointGeodetic);

        }else if(viewInFocus == PLAN) {
            rlViewTablePlanar.startAnimation(animExitStageLeftPointSurvey);
            rlViewTableWorld.startAnimation(animExitStageLeftPointGeodeticHidden);

        }

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                rlViewTableGrid.startAnimation(animEnterStageRightPointGrid);
            }
        },200);

        setTableViewButtons(GRID);

    }


    private void setTableViewButtons(int value){

        switch (value){
            case WORLD:
                viewInFocus = WORLD;
                setHeaderValues(WORLD);
                setWorldButtons(true);
                setGridButtons(false);
                setPlanButtons(false);
                break;

            case GRID:
                viewInFocus = GRID;
                setHeaderValues(GRID);
                setWorldButtons(false);
                setGridButtons(true);
                setPlanButtons(false);
                break;

            case PLAN:
                viewInFocus = PLAN;
                setHeaderValues(PLAN);
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

    private void setHeaderValues(int viewInFocus){

        switch (viewInFocus){
            case PLAN:
                tvHeaderNorth.setText(getResources().getString(R.string.dialog_job_point_item_header_northing));
                tvHeaderEast.setText(getResources().getString(R.string.dialog_job_point_item_header_easting));
                break;

            case WORLD:
                tvHeaderNorth.setText(getResources().getString(R.string.dialog_job_point_item_header_latitude));
                tvHeaderEast.setText(getResources().getString(R.string.dialog_job_point_item_header_longitude));
                break;

            case GRID:
                tvHeaderNorth.setText(getResources().getString(R.string.dialog_job_point_item_header_grid_north));
                tvHeaderEast.setText(getResources().getString(R.string.dialog_job_point_item_header_grid_east));
                break;
        }
    }

    //----------------------------------------------------------------------------------------------//

    public void animateNewPointView(View view) {
        Log.d(TAG, "animateNewPointView: Started");

        if (!mIsCancel) {
            if (mStartX == 0.0f) {
                mStartX = view.getX();
                mStartY = view.getY();

                mBottomX = getBottomFilterXPosition();
                mBottomY = getBottomFilterYPosition();

                mBottomYNew = (int) ivCancel.getY();

                mBottomListStartY = vBottomListBackground.getY();
            }

            final int x = getFinalXPosition();
            final int y = getFinalYPosition();


            ValueAnimator animator = ValueAnimator.ofFloat(0f, 1f);

            animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    float v = (float) animation.getAnimatedValue();

                    fabOpenFilter.setX(
                            x + (mStartX - x - ((mStartX - x) * v))
                    );

                    fabOpenFilter.setY(
                            y + (mStartY - y - ((mStartY - y) * (v * v)))
                    );
                }
            });
            animator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);

                    removeFabBackground();

                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {

                            fabOpenNewPoint.hide();

                            fabOpenFilter.animate()
                                    .y(mBottomY)  //was mBottomY
                                    .setDuration(200)
                                    .start();

                        }
                    },50);
                    
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            ivCancel.setVisibility(VISIBLE);
                            ivCancel.setTranslationX(-(mBottomX - x));

                            ivCancel.animate()
                                    .translationXBy(mBottomX - x)
                                    .setDuration(200)
                                    .start();

                            fabOpenFilter.animate()
                                    .x(mBottomX)
                                    .setDuration(200)
                                    .start();

                            fabOpenFilter.animate()
                                    .x(mBottomX)
                                    .setDuration(200)
                                    .start();

                            vSheetTop.setScaleY(0f);
                            vSheetTop.setVisibility(VISIBLE);

                            vSheetTop.animate()
                                    .scaleY(1f)
                                    .setListener(new AnimatorListenerAdapter() {
                                        @Override
                                        public void onAnimationEnd(Animator animation) {
                                            super.onAnimationEnd(animation);
                                            tabHeader.setVisibility(VISIBLE);
                                        }
                                    })
                                    .setDuration(200)
                                    .start();
                        }
                    }, 200);

                    if (resetBottomList) {
                        Log.d(TAG, "onAnimationEnd: Reset");
                        resetBottomListBackground();
                    }


                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            vBottomListBackground.animate()
                                    .alpha(1f)
                                    .setDuration(500)
                                    .setListener(new AnimatorListenerAdapter() {
                                        @Override
                                        public void onAnimationEnd(Animator animation) {
                                            super.onAnimationEnd(animation);
                                            fabOpenFilter.setImageResource(R.drawable.ic_action_cancel);
                                            fabOpenFilter.setVisibility(INVISIBLE);
                                            fabOpenFilter.setX(ivCancel.getX() - mDisplayMetrics.density * 4);
                                            fabOpenFilter.setY(getBottomFilterYPosition());
                                            ivSaveFilter.setVisibility(VISIBLE);
                                        }
                                    })
                                    .start();
                        }
                    }, 200);

                    revealFilterSheet(y);
                }
            });

            animator.start();
        } else {

            fabOpenFilter.setImageResource(R.drawable.ic_action_filter_dark);
            fabOpenFilter.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getActivity(), R.color.colorAccent)));
            mIsCancel = false;

            fabOpenNewPoint.show();

        }
    }


    private int getBottomFilterYPosition() {
        return (int) (
                ivSaveFilter.getY()
                        + (mDisplayMetrics.heightPixels - getStatusBarHeight() - mDisplayMetrics.density * 64)
                        - mDisplayMetrics.density * 4);
    }

    private int getBottomFilterXPosition() {
        return (int) (
                ivSaveFilter.getX()
                        + mDisplayMetrics.widthPixels / 2
                        - mDisplayMetrics.density * 4);
    }


    public int getFinalXPosition() {
        return mDisplayMetrics.widthPixels / 2 - getFabSize() / 2;
    }

    public int getFinalYPosition() {
        int marginFromBottom = getFinalYPositionFromBottom();
        return mDisplayMetrics.heightPixels - marginFromBottom + getFabSize() / 2;
    }

    public int getFinalYPositionFromBottom() {
        return (int) (mDisplayMetrics.density * 250);
    }

    public int getFabSize() {
        return (int) (mDisplayMetrics.density * 56);
    }

    private void removeFabBackground() {
       fabOpenFilter.setBackgroundTintList(ColorStateList.valueOf(TRANSPARENT));

        fabOpenFilter.setElevation(0f);
    }

    private void resetBottomListBackground() {
        resetBottomList = false;

        vBottomListBackground.setVisibility(VISIBLE);
        Drawable d = vBottomListBackground.getBackground();
        final GradientDrawable gd = (GradientDrawable) d;
        vBottomListBackground.setAlpha(0f);
        gd.setCornerRadius(0f);


        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) vBottomListBackground.getLayoutParams();
        params.width = -1;
        params.height = (int) (mDisplayMetrics.density * 64);
        vBottomListBackground.setY(mBottomListStartY + mDisplayMetrics.density * 8);
        vBottomListBackground.requestLayout();
    }

    private void revealFilterSheet(int y) {
        vReveal.setVisibility(VISIBLE);

        Animator a = ViewAnimationUtils.createCircularReveal(
                vReveal,
                mDisplayMetrics.widthPixels / 2,
                (int) (y - vReveal.getY()) + getFabSize() / 2,
                getFabSize() / 2,
                vReveal.getHeight() * .7f);
        a.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);

                vContentFilter.setVisibility(VISIBLE);
            }
        });
        a.start();
    }

    public int getStatusBarHeight() {
        return (int) (mDisplayMetrics.density * 24);
    }


    private void acceptNewPoint(View view){
        Log.d(TAG, "acceptNewPoint: Started");

        fabOpenFilter.setVisibility(VISIBLE);
        vContentFilter.setVisibility(INVISIBLE);
        tabHeader.setVisibility(INVISIBLE);

        mIsCancel = true;
        final int x = getFinalXPosition();
        final int y = getFinalYPosition();


        ivSaveFilter.setVisibility(INVISIBLE);
        ivCancel.setVisibility(INVISIBLE);

        final int startX = (int) fabOpenFilter.getX();
        final int startY = (int) fabOpenFilter.getY();

        vSheetTop.setVisibility(INVISIBLE);
        Animator reveal = ViewAnimationUtils.createCircularReveal(
                vReveal,
                mDisplayMetrics.widthPixels / 2,
                (int) (y - vReveal.getY()) + getFabSize() / 2,
                vReveal.getHeight() * .5f,
                getFabSize() / 2);

        reveal.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                vReveal.setVisibility(INVISIBLE);
                fabOpenFilter.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getActivity(), R.color.colorAccent)));
                fabOpenFilter.setElevation(mDisplayMetrics.density * 4);

            }
        });
        reveal.start();

        animateBottomSheet();

        ValueAnimator animator = ValueAnimator.ofFloat(0f, 1f);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float v = (float) animation.getAnimatedValue();

                fabOpenFilter.setX(
                        x - (x - startX - ((x - startX) * v))
                );

                fabOpenFilter.setY(
                        y + (startY - y - ((startY - y) * (v * v)))
                );


            }
        });
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                fabOpenFilter.animate()
                        .rotationBy(360)
                        .setDuration(1000)
                        .start();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        returnFabToInitialPosition();
                        vBottomListBackground.setVisibility(INVISIBLE);
                    }
                }, 1000);
            }
        });
        animator.start();

        fabOpenFilter.setImageResource(R.drawable.ic_action_filter_dark);
        fabOpenFilter.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getActivity(), R.color.colorAccent)));
        mIsCancel = false;

        fabOpenNewPoint.show();

    }

    private void cancelNewPoint(View view){
        Log.d(TAG, "acceptNewPoint: Started");

        fabOpenFilter.setVisibility(VISIBLE);
        vContentFilter.setVisibility(INVISIBLE);
        tabHeader.setVisibility(INVISIBLE);

        mIsCancel = true;
        final int x = getFinalXPosition();
        final int y = getFinalYPosition();


        ivSaveFilter.setVisibility(INVISIBLE);
        ivCancel.setVisibility(INVISIBLE);

        final int startX = (int) fabOpenFilter.getX();
        final int startY = (int) fabOpenFilter.getY();

        vSheetTop.setVisibility(INVISIBLE);
        Animator reveal = ViewAnimationUtils.createCircularReveal(
                vReveal,
                mDisplayMetrics.widthPixels / 2,
                (int) (y - vReveal.getY()) + getFabSize() / 2,
                vReveal.getHeight() * .5f,
                getFabSize() / 2);

        reveal.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                vReveal.setVisibility(INVISIBLE);
                fabOpenFilter.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getActivity(), R.color.colorAccent)));
                fabOpenFilter.setElevation(mDisplayMetrics.density * 4);

            }
        });
        reveal.start();

        animateBottomSheet();

        ValueAnimator animator = ValueAnimator.ofFloat(0f, 1f);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float v = (float) animation.getAnimatedValue();

                fabOpenFilter.setX(
                        x - (x - startX - ((x - startX) * v))
                );

                fabOpenFilter.setY(
                        y + (startY - y - ((startY - y) * (v * v)))
                );


            }
        });
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                fabOpenFilter.animate()
                        .rotationBy(360)
                        .setDuration(1000)
                        .start();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        returnFabToInitialPosition();
                        vBottomListBackground.setVisibility(INVISIBLE);
                    }
                }, 1000);
            }
        });
        animator.start();

        fabOpenFilter.setImageResource(R.drawable.ic_action_filter_dark);
        fabOpenFilter.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getActivity(), R.color.colorPrimary)));
        mIsCancel = false;

        fabOpenNewPoint.show();

    }



    private void animateBottomSheet() {
        Drawable d = vBottomListBackground.getBackground();
        final GradientDrawable gd = (GradientDrawable) d;


        final RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)
                vBottomListBackground.getLayoutParams();

        final int startWidth = vBottomListBackground.getWidth();
        final int startHeight = vBottomListBackground.getHeight();
        final int startY = (int) vBottomListBackground.getY();


        ValueAnimator animator = ValueAnimator.ofFloat(0f, 1f);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float v = (float) animation.getAnimatedValue();
                gd.setCornerRadius(mDisplayMetrics.density * 50 * v);

                int i = (int) (startWidth - (startWidth - getFabSize()) * v);
                params.width = i;
                params.height = (int) (startHeight - (startHeight - getFabSize()) * v);
                vBottomListBackground.setY(getFinalYPosition() + (startY
                        - getFinalYPosition()) - ((startY - getFinalYPosition()) * v));

                vBottomListBackground.requestLayout();
            }
        });
        animator.start();
    }

    private void returnFabToInitialPosition() {
        final int x = getFinalXPosition();
        final int y = getFinalYPosition();
        resetBottomList = true;


        ValueAnimator animator = ValueAnimator.ofFloat(0f, 1f);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float v = (float) animation.getAnimatedValue();

                fabOpenFilter.setX(
                        x + ((mStartX - x) * v)
                );

                fabOpenFilter.setY(
                        (float) (y + (mStartY - y) * (Math.pow(v, .5f)))
                );
            }
        });
        animator.start();
    }

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
        fabOpenNewPoint.startAnimation(animRotateForward);

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

        fabOpenNewPoint.startAnimation(animRotateBackwards);

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

    //----------------------------------------------------------------------------------------------//

    public void setDisplayMetrics() {
        //mDisplayMetrics = getResources().getDisplayMetrics();

        mDisplayMetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(mDisplayMetrics);

    }

    //----------------------------------------------------------------------------------------------//

    private void revealNewPointActions(){
        Log.d(TAG, "revealNewPointActions: Started");

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                delayedStartNextActivity();
            }
        }, 50);

    }

    private void revealButton() {
        fabOpenNewPoint.setElevation(0f);

        vRevealNew.setVisibility(VISIBLE);

        int cx = vRevealNew.getWidth();
        int cy = vRevealNew.getHeight();


        int x = (int) (getFabWidth() / 2 + fabOpenNewPoint.getX());
        int y = (int) (getFabWidth() / 2 + fabOpenNewPoint.getY());

        float finalRadius = Math.max(cx, cy) * 1.2f;

        Animator reveal = ViewAnimationUtils
                .createCircularReveal(vRevealNew, x, y, getFabWidth(), finalRadius);

        reveal.setDuration(400);
        reveal.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                reset(animation);

            }

            private void reset(Animator animation) {
                super.onAnimationEnd(animation);
                vRevealNew.setVisibility(INVISIBLE);

            }
        });

        reveal.start();
    }

    private void delayedStartNextActivity() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                createPointEntry(project_id,job_id,jobDatabaseName);

            }
        }, 100);
    }


    private int getFabWidth() {
        return (int) getResources().getDimension(R.dimen.fab_size);
    }

}