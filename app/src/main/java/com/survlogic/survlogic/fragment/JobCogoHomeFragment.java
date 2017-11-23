package com.survlogic.survlogic.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.survlogic.survlogic.R;
import com.survlogic.survlogic.activity.JobCogoSetupActivity;
import com.survlogic.survlogic.interf.JobCogoHomeFragmentListener;
import com.survlogic.survlogic.model.PointSurvey;

/**
 * Created by chrisfillmore on 5/2/2017.
 */

public class JobCogoHomeFragment extends Fragment {

    private static final String TAG = "JobCogoHomeFragment";
    
    private static int project_id, job_id;
    private String jobDatabaseName;
    
    private LinearLayout llCogoSetup;

    private View v;

    private JobCogoHomeFragmentListener jobCogoHomeFragmentListener;

    private static final int REQUEST_GET_SETUP = 1;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.fragment_job_cogo_home, container, false);

        initViewWidgets(v);
        setOnClickListeners();


        return v;
    }


    private void initViewWidgets(View v){
        Log.d(TAG, "initViewWidgets: Started...");

        Bundle extras = getArguments();
        project_id = extras.getInt(getString(R.string.KEY_PROJECT_ID));
        job_id = extras.getInt(getString(R.string.KEY_JOB_ID));
        jobDatabaseName = extras.getString(getString(R.string.KEY_JOB_DATABASE));
        Log.d(TAG, "||Database_fragment_cogo_home|| : " + jobDatabaseName);


        llCogoSetup = (LinearLayout) v.findViewById(R.id.llActionItemSetup);



    }


    private void setOnClickListeners(){

        llCogoSetup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                openCogoSetupActivity();


            }
        });
    }

    public void openCogoSetupActivity(){
        jobCogoHomeFragmentListener = (JobCogoHomeFragmentListener) getActivity();

        boolean isOccupyPointSet = jobCogoHomeFragmentListener.isOccupyPointSurveySet();
        boolean isBackSightPointSet = jobCogoHomeFragmentListener.isBacksightPointSurveySet();


        Intent i = new Intent(getActivity(), JobCogoSetupActivity.class);
        i.putExtra(getString(R.string.KEY_PROJECT_ID),project_id);
        i.putExtra(getString(R.string.KEY_JOB_ID), job_id);
        i.putExtra(getString(R.string.KEY_JOB_DATABASE), jobDatabaseName);

        if(isOccupyPointSet && isBackSightPointSet){

            PointSurvey occupyPointSurvey = jobCogoHomeFragmentListener.getOccupyPointSurvey();
            PointSurvey backsightPointSurvey = jobCogoHomeFragmentListener.getBacksightPointSurvey();

            i.putExtra(getString(R.string.KEY_SETUP_OCCUPY_PT),occupyPointSurvey.getPoint_no());
            i.putExtra(getString(R.string.KEY_SETUP_BACKSIGHT_PT),backsightPointSurvey.getPoint_no());

            i.putExtra(getString(R.string.KEY_SETUP_OCCUPY_HT), jobCogoHomeFragmentListener.getOccupyHeight());
            i.putExtra(getString(R.string.KEY_SETUP_BACKSIGHT_HT), jobCogoHomeFragmentListener.getBacksightHeight());


        }else{
            i.putExtra(getString(R.string.KEY_SETUP_OCCUPY_PT),0);
            i.putExtra(getString(R.string.KEY_SETUP_BACKSIGHT_PT),0);

            i.putExtra(getString(R.string.KEY_SETUP_OCCUPY_HT), 0);
            i.putExtra(getString(R.string.KEY_SETUP_BACKSIGHT_HT), 0);
        }

        getActivity().startActivityForResult(i,REQUEST_GET_SETUP);
        Log.d(TAG, "onClick: Request_GET_SETUP: " + REQUEST_GET_SETUP);
        getActivity().overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

}