package com.survlogic.survlogic.fragment;


import android.content.Context;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Region;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.RelativeLayout;

import com.survlogic.survlogic.R;
import com.survlogic.survlogic.background.BackgroundSurveyPointMap;
import com.survlogic.survlogic.dialog.DialogJobMapOptions;
import com.survlogic.survlogic.dialog.DialogJobPointListView;
import com.survlogic.survlogic.dialog.DialogJobPointView;
import com.survlogic.survlogic.interf.MapZoomListener;
import com.survlogic.survlogic.model.PointSurvey;
import com.survlogic.survlogic.view.PlanarMapScaleView;
import com.survlogic.survlogic.view.PlanarMapView;
import com.survlogic.survlogic.view.ZoomableMapGroup;

import java.util.ArrayList;

/**
 * Created by chrisfillmore on 5/2/2017.
 */

public class JobPointsMapFragment extends Fragment {

    private static final String TAG = "JobPointsMapFragment";
    private Context mContext;

    private View v;
    private static final int DELAY_TO_MAP = 300;
    private static final int NONE = 0;
    private static final int TOUCH = 1;
    private static final int FENCE = 2;
    private static final int ACTION = 3;

    private int project_id, job_id;
    private String jobDatabaseName;

    private RelativeLayout relPointActions;

    private FloatingActionButton fabClose;
    private FloatingActionButton fabOptions;
    private FloatingActionButton fabSelect, fabSelectByTouch, fabSelectByFence, fabSelectBySearch;

    private Button btAction1, btAction2, btAction3;

    private CardView cardFabSelectByPoint, cardFabSelectByFence, cardFabSelectBySearch;

    private PlanarMapView planarMapView;
    private PlanarMapScaleView planarScaleMapView;
    private ZoomableMapGroup zoomableMapGroup;

    private Animation animOpen, animClose, animRotateForward, animRotateBackwards,
            transitionLeft, transitionRight, transitionRightOffScreen,
            transitionToLeft, transitionToRight;

    private ArrayList<PointSurvey> lstSelectedPoints = new ArrayList<>();

    private boolean fabExpanded = false;
    private boolean fabSelectionPoint = false, fabSelectionFence = false;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.fragment_job_points_map, container, false);

        mContext = getActivity();
        initViewWidgets(v);
        setOnClickListener(v);


        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: Started...");

        showPlanarMap();


    }

    private void initViewWidgets(View v){
        Log.d(TAG, "initViewWidgets: Starting...");
        Bundle extras = getArguments();
        project_id = extras.getInt(getString(R.string.KEY_PROJECT_ID));
        job_id = extras.getInt(getString(R.string.KEY_JOB_ID));
        jobDatabaseName = extras.getString(getString(R.string.KEY_JOB_DATABASE));
        Log.i(TAG, "Database: " + jobDatabaseName);

        relPointActions = (RelativeLayout) v.findViewById(R.id.fabRelativePointActions);

        fabClose = (FloatingActionButton) v.findViewById(R.id.layout_close);

        fabOptions = (FloatingActionButton) v.findViewById(R.id.layout_options);

        fabSelect = (FloatingActionButton) v.findViewById(R.id.fabSelect);
        fabSelectByTouch = (FloatingActionButton) v.findViewById(R.id.fabSelectByTouch);
        fabSelectByFence = (FloatingActionButton) v.findViewById(R.id.fabSelectByFence);
        fabSelectBySearch = (FloatingActionButton) v.findViewById(R.id.fabSelectBySearch);

        btAction1 = (Button) v.findViewById(R.id.point_action_1);
        btAction2 = (Button) v.findViewById(R.id.point_action_2);
        btAction3 = (Button) v.findViewById(R.id.point_action_extra);

        cardFabSelectByPoint = (CardView) v.findViewById(R.id.cardSelectByTouch);
        cardFabSelectByFence = (CardView) v.findViewById(R.id.cardSelectByFence);
        cardFabSelectBySearch = (CardView) v.findViewById(R.id.cardSelectBySearch);

        zoomableMapGroup = (ZoomableMapGroup) v.findViewById(R.id.zoomableMapView);
        planarMapView = (PlanarMapView) v.findViewById(R.id.map_view);
        planarScaleMapView = (PlanarMapScaleView) v.findViewById(R.id.legendScale);

        animOpen = AnimationUtils.loadAnimation(mContext,R.anim.anim_fab_open);
        animClose = AnimationUtils.loadAnimation(mContext,R.anim.anim_fab_close);

        animRotateForward = AnimationUtils.loadAnimation(mContext,R.anim.rotate_fab_forward);
        animRotateBackwards = AnimationUtils.loadAnimation(mContext, R.anim.rotate_fab_backward);

        transitionLeft = AnimationUtils.loadAnimation(mContext,R.anim.anim_transition_to_left_hide);
        transitionRight = AnimationUtils.loadAnimation(mContext,R.anim.anim_transition_to_right_hide);
        transitionRightOffScreen = AnimationUtils.loadAnimation(mContext,R.anim.anim_transition_to_right_offscreen_hide);

        transitionToLeft = AnimationUtils.loadAnimation(mContext,R.anim.anim_transition_from_left_show);
        transitionToRight = AnimationUtils.loadAnimation(mContext,R.anim.anim_transition_from_right_show);
    }


    private void setOnClickListener(View v){
        Log.d(TAG, "setOnClickListener: Starting...");

        fabOptions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: Pushed");
                openOptionsMenu();


            }
        });

        fabSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (fabExpanded){
                    closeSubMenuSelectFab();
                }else{
                    openSubMenuSelectFab();
                }
            }
        });


        zoomableMapGroup.setOnMapZoomListener(new MapZoomListener() {
            @Override
            public void onReturnValues(Rect zoomRect) {
                if(planarScaleMapView.getVisibility() == View.GONE){
                    planarScaleMapView.setVisibility(View.VISIBLE);
                }

                float planMapScaleDistance = planarMapView.getMapScaleDistance(zoomRect);
                planarScaleMapView.setScale((int) (planMapScaleDistance * 0.5f));

                planarMapView.setObjectSize(planMapScaleDistance);
                planarMapView.invalidate();
            }

            @Override
            public void onTouchOnPoint(float x, float y) {
                Log.d(TAG, "From Listener: " + x + ", " + y);

                if (fabSelectionPoint) {
                    setPointActionItems(TOUCH);

                    //Single Point Check and only allow 1 point to be selected
                    if(lstSelectedPoints!=null && lstSelectedPoints.size()==0) {
                        lstSelectedPoints = planarMapView.checkPointForTouch(x, y);
                        Log.i(TAG, "onTouchOnPoint: Single Point Touch at: " + x + ", " + y);
                    }

                    if(lstSelectedPoints.size()==1){
                        lstSelectedPoints.clear();

                        planarMapView.eraseAll();
                        lstSelectedPoints = planarMapView.checkPointForTouch(x, y);
                    }


                    if(lstSelectedPoints!=null && lstSelectedPoints.size()>0){
                        showMenuForPointAction();
                    }else if(relPointActions.isShown()){
                        hideMenuForPointAction();
                    }


                }
            }

            @Override
            public void onFenceAround(Path fencePath, RectF fenceRect) {
                Log.d(TAG, "onFenceAround: Started...");

                if(fabSelectionFence) {

                    if (lstSelectedPoints != null && lstSelectedPoints.size() == 0) {
                        Region region = new Region();
                        region.setPath(fencePath, new Region((int) fenceRect.left, (int) fenceRect.top, (int) fenceRect.right, (int) fenceRect.bottom));

                        lstSelectedPoints = planarMapView.checkPointForFence(region);

                        if(lstSelectedPoints !=null && lstSelectedPoints.size() ==1){
                            setPointActionItems(TOUCH);
                        }else if(lstSelectedPoints !=null && lstSelectedPoints.size() > 1){
                            setPointActionItems(FENCE);
                        }

                        zoomableMapGroup.clearFenceSelection();

                    } else if (lstSelectedPoints != null && lstSelectedPoints.size() >= 1) {
                        Region region = new Region();
                        region.setPath(fencePath, new Region((int) fenceRect.left, (int) fenceRect.top, (int) fenceRect.right, (int) fenceRect.bottom));

                        lstSelectedPoints = planarMapView.checkPointForFence(region);

                        if(lstSelectedPoints !=null && lstSelectedPoints.size() ==1){
                            setPointActionItems(TOUCH);
                        }else if(lstSelectedPoints !=null && lstSelectedPoints.size() >1){
                            setPointActionItems(FENCE);
                        }

                        zoomableMapGroup.clearFenceSelection();

                    }


                    if (lstSelectedPoints != null && lstSelectedPoints.size() > 0) {
                        showMenuForPointAction();
                    } else if (relPointActions.isShown()) {
                        hideMenuForPointAction();
                    }

                }

            }
        });

        fabSelectByTouch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideMenuForSelect();
                fabSelectionPoint = true;
                fabSelectionFence = false;
            }
        });

        fabSelectByFence.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                zoomableMapGroup.setTouchable(false);
                hideMenuForSelect();
                fabSelectionFence = true;
                fabSelectionPoint = false;
            }
        });


        fabClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMenuForSelect();
                zoomableMapGroup.clearFenceSelection();
                zoomableMapGroup.setTouchable(true);
            }
        });

        btAction1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(fabSelectionPoint){
                    Log.d(TAG, "onClick: Fence Selection");
                    infoSinglePoint();
                }else if(fabSelectionFence){
                    Log.d(TAG, "onClick: Fence Selection");
                    infoMultiplePoints();
                }
            }
        });



    }

    private void showPlanarMap(){

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                initPlanarMap();
            }
        },DELAY_TO_MAP);

    }

    private void initPlanarMap(){
        Log.d(TAG, "initPlanarMap: Started...");
        BackgroundSurveyPointMap backgroundSurveyPointMap = new BackgroundSurveyPointMap(getActivity(),jobDatabaseName);
        backgroundSurveyPointMap.execute();

    }

    private void openOptionsMenu(){

        android.support.v4.app.DialogFragment pointMapOptions = DialogJobMapOptions.newInstance(project_id, job_id, jobDatabaseName);
        pointMapOptions.show(getFragmentManager(),"dialog_map_options");

    }



    private void closeSubMenuSelectFab(){
        // When selection menu is open, this closes the selection menu fab items

        fabSelect.startAnimation(animRotateBackwards);

        fabSelectByTouch.startAnimation(animClose);
        fabSelectByFence.startAnimation(animClose);
        fabSelectBySearch.startAnimation(animClose);

        cardFabSelectByPoint.startAnimation(animClose);
        cardFabSelectByFence.startAnimation(animClose);
        cardFabSelectBySearch.startAnimation(animClose);

        fabSelectByTouch.setClickable(false);
        fabSelectByFence.setClickable(false);
        fabSelectBySearch.setClickable(false);
        fabClose.setClickable(false);


        fabSelect.setImageResource(R.drawable.ic_action_crop_info);

        fabExpanded = false;
    }

    private void openSubMenuSelectFab(){
        //this opens the selection menu and the fabs
        fabSelect.startAnimation(animRotateForward);

        cardFabSelectByPoint.startAnimation(animOpen);
        fabSelectByTouch.startAnimation(animOpen);

        fabSelectByFence.startAnimation(animOpen);
        cardFabSelectByFence.startAnimation(animOpen);

        fabSelectBySearch.startAnimation(animOpen);
        cardFabSelectBySearch.startAnimation(animOpen);

        fabSelectByTouch.setClickable(true);
        fabSelectByFence.setClickable(true);
        fabSelectBySearch.setClickable(true);
        fabClose.setClickable(true);

        fabSelect.setImageResource(R.drawable.ic_close_white_24dp);
        fabExpanded = true;

    }

    private void hideMenuForSelect(){
        // Hides the selection and shows the close button

        fabOptions.startAnimation(transitionLeft);

        fabSelect.startAnimation(transitionRight);

        fabSelectByTouch.startAnimation(transitionRight);
        fabSelectByFence.startAnimation(transitionRight);
        fabSelectBySearch.startAnimation(transitionRight);

        cardFabSelectByPoint.startAnimation(transitionRight);
        cardFabSelectByFence.startAnimation(transitionRight);
        cardFabSelectBySearch.startAnimation(transitionRight);

        fabClose.startAnimation(transitionRightOffScreen);
        fabClose.setClickable(true);

    }

    private void showMenuForSelect(){
        // Returns to select menu

        fabOptions.startAnimation(transitionToLeft);

        fabSelect.startAnimation(transitionToRight);

        fabSelectByTouch.startAnimation(transitionToRight);
        fabSelectByFence.startAnimation(transitionToRight);
        fabSelectBySearch.startAnimation(transitionToRight);

        cardFabSelectByPoint.startAnimation(transitionToRight);
        cardFabSelectByFence.startAnimation(transitionToRight);
        cardFabSelectBySearch.startAnimation(transitionToRight);

        fabClose.startAnimation(transitionLeft);
        fabClose.setClickable(false);

        hideMenuForPointAction();
    }

    private void showMenuForPointAction(){
        relPointActions.setVisibility(View.VISIBLE);

    }

    private void setPointActionItems(int mode){

        switch (mode){
            case TOUCH:
                btAction1.setText(getResources().getString(R.string.map_action_touch_action_1));
                btAction2.setText(getResources().getString(R.string.map_action_touch_action_2));
                btAction3.setText(getResources().getString(R.string.map_action_touch_action_3));
                break;

            case FENCE:
                btAction1.setText(getResources().getString(R.string.map_action_fence_action_1));
                btAction2.setText(getResources().getString(R.string.map_action_fence_action_2));
                btAction3.setText(getResources().getString(R.string.map_action_fence_action_3));
        }

    }
    private void hideMenuForPointAction(){

        planarMapView.eraseAll();

        relPointActions.setVisibility(View.INVISIBLE);
        lstSelectedPoints.clear();

    }

    private void infoSinglePoint(){
        Log.d(TAG, "infoSinglePoint: Started...");
        PointSurvey pointSurvey;

        for(int i=0; i<lstSelectedPoints.size(); i++) {
            pointSurvey = lstSelectedPoints.get(i);

            long point_id = pointSurvey.getId();
            int pointNo = pointSurvey.getPoint_no();

            android.support.v4.app.DialogFragment pointDialog = DialogJobPointView.newInstance(project_id, job_id, point_id, pointNo, jobDatabaseName);
            pointDialog.show(getFragmentManager(),"dialog");


        }


    }

    private void infoMultiplePoints(){
        Log.d(TAG, "infoMultiplePoints: Started...");

        android.support.v4.app.DialogFragment pointListDialog = DialogJobPointListView.newInstance(project_id, job_id, jobDatabaseName, lstSelectedPoints);
        pointListDialog.show(getFragmentManager(),"dialog_list");


    }


}