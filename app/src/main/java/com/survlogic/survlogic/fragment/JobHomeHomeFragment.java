package com.survlogic.survlogic.fragment;

import android.animation.LayoutTransition;
import android.content.Context;
import android.media.Image;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.survlogic.survlogic.R;
import com.survlogic.survlogic.interf.JobHomeActivityListener;
import com.survlogic.survlogic.utils.AnimateHelper;
import com.survlogic.survlogic.utils.SurveyProjectionHelper;

/**
 * Created by chrisfillmore on 5/2/2017.
 */

public class JobHomeHomeFragment extends Fragment {
    private static final String TAG = "JobHomeHomeFragment";
    private Context mContext;

    private View v;

    private RelativeLayout rlCardDetails;
    private CardView cardViewDetails;
    private TextView tvItemJobName, tvItem1Units, tvItem2Projection, tvItem3Zone;
    private ImageButton ibCardExpand;

    private Animation animCard_1_down_btn, animCard_1_up_btn;

    private JobHomeActivityListener jobHomeActivityListener;
    private SurveyProjectionHelper surveyProjectionHelper;

    private boolean isProjection = false, isProjectionZone = false;


    private boolean isCardExpanded = false;
    private int CARD_VIEW_START = 100, CARD_VIEW_END = 450;
    private static final int ANIMATE_DURATION_SHORT = 100, ANIMATE_DURATION_LONG = 300, ANIMATE_DURATION_FADE_IN = 400;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.fragment_job_home_home, container, false);
        mContext = getActivity();
        surveyProjectionHelper = new SurveyProjectionHelper(mContext);


        initViewWidgets(v);
        setOnClickListeners(v);


        return v;


    }

    private void initViewWidgets(View v){
        Log.d(TAG, "initViewWidgets: Started");

        jobHomeActivityListener = (JobHomeActivityListener) getActivity();

        cardViewDetails = (CardView) v.findViewById(R.id.card_details);
        rlCardDetails = (RelativeLayout) v.findViewById(R.id.rlLayout_details);

        tvItemJobName = (TextView) v.findViewById(R.id.project_name);
        tvItem1Units = (TextView) v.findViewById(R.id.left_item1_value);
        tvItem2Projection = (TextView) v.findViewById(R.id.left_item2_value);
        tvItem3Zone = (TextView) v.findViewById(R.id.left_item3_value);

        ibCardExpand = (ImageButton) v.findViewById(R.id.button_card_expand);

        animCard_1_down_btn = AnimationUtils.loadAnimation(mContext,R.anim.rotate_card_down);

        animCard_1_down_btn.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                ibCardExpand.setImageResource(R.drawable.ic_keyboard_arrow_up);
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
                ibCardExpand.setImageResource(R.drawable.ic_keyboard_arrow_down);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        AnimateHelper.animateViewFadeNormalVisible(rlCardDetails, false,1);


    }


    private void setOnClickListeners(View v){
        Log.d(TAG, "setOnClickListeners: Started");


        ibCardExpand.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isCardExpanded){
                    ibCardExpand.startAnimation(animCard_1_down_btn);

                    AnimateHelper.ResizeCardAnimation resizeCardAnimationExpand = new AnimateHelper.ResizeCardAnimation(rlCardDetails,CARD_VIEW_END,CARD_VIEW_START);
                    resizeCardAnimationExpand.setDuration(ANIMATE_DURATION_LONG);
                    rlCardDetails.startAnimation(resizeCardAnimationExpand);

                    AnimateHelper.animateViewFadeNormalVisible(rlCardDetails, true,ANIMATE_DURATION_FADE_IN);

                    isCardExpanded = true;
                }else{

                    ibCardExpand.startAnimation(animCard_1_up_btn);

                    AnimateHelper.ResizeCardAnimation resizeCardAnimationContract = new AnimateHelper.ResizeCardAnimation(rlCardDetails,CARD_VIEW_START,CARD_VIEW_END);
                    resizeCardAnimationContract.setDuration(ANIMATE_DURATION_SHORT);
                    rlCardDetails.startAnimation(resizeCardAnimationContract);

                    AnimateHelper.animateViewFadeNormalVisible(rlCardDetails, false,ANIMATE_DURATION_SHORT);

                    isCardExpanded = false;
                }
            }
        });

    }

    private void populateMetadata(){
        Log.d(TAG, "populateMetadata: Started");

        tvItemJobName.setText(jobHomeActivityListener.getJobMetadataJobName());

        int units_pos = jobHomeActivityListener.getJobMetadataJobUnits() - 1;

        if (units_pos == -1){
            units_pos = units_pos + 1;
        }

        Log.d(TAG, "populateMetadata: Unit position: " + units_pos);

        String [] units_values = getResources().getStringArray(R.array.unit_measure_entries);
        tvItem1Units.setText(units_values[units_pos]);

        initProjectionDetails();


    }

    //-------------------------------------------------------------------------------------------------------------------------//

    /**
     * Projection Calculations
     */


    private void initProjectionDetails(){
        Log.d(TAG, "initProjectionDetails: Started");
        String projectionString, zoneString;


        if (jobHomeActivityListener.getJobMetadataIsJobProjection() == 1){
            isProjection = true;
            projectionString = jobHomeActivityListener.getJobMetadataJobProjection();

        }else{
            projectionString = getResources().getString(R.string.projection_none);
        }

        if (jobHomeActivityListener.getJobMetadataIsJobZone() == 1){
            isProjectionZone = true;
            zoneString = jobHomeActivityListener.getJobMetadataJobZone();

        }else{
            zoneString = getResources().getString(R.string.projection_zone_none);
        }

        if(isProjection){
            surveyProjectionHelper.setConfig(projectionString,zoneString);
            tvItem2Projection.setText(surveyProjectionHelper.getProjectionName());
            tvItem3Zone.setText(surveyProjectionHelper.getZoneName());
        }else{
            tvItem2Projection.setText(getResources().getString(R.string.general_none));
            tvItem3Zone.setText(getResources().getString(R.string.general_none));;
        }



    }

    //----------------------------------------------------------------------------------------------//

    public void setMetadata(){
        Log.d(TAG, "setMetadata: Started");

        populateMetadata();
    }

}