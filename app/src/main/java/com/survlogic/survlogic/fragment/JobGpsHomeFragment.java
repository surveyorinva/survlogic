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

import com.survlogic.survlogic.ARvS.JobGPSSurveyWork;
import com.survlogic.survlogic.R;

/**
 * Created by chrisfillmore on 5/2/2017.
 */

public class JobGpsHomeFragment extends Fragment {

    private static final String TAG = "JobGpsHomeFragment";

    private static int project_id, job_id;
    private String jobDatabaseName;

    private LinearLayout llStakeGPSPoint;


    private View v;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.fragment_job_gps_home, container, false);

        initViewWidgets(v);
        setOnClickListeners();

        return v;
    }

    private void initViewWidgets(View v){
        Log.d(TAG, "initViewWidgets: Started");

        Bundle extras = getArguments();
        project_id = extras.getInt(getString(R.string.KEY_PROJECT_ID));
        job_id = extras.getInt(getString(R.string.KEY_JOB_ID));
        jobDatabaseName = extras.getString(getString(R.string.KEY_JOB_DATABASE));
        Log.d(TAG, "||Database_fragment_cogo_home|| : " + jobDatabaseName);


        llStakeGPSPoint = v.findViewById(R.id.ll_stake_points);


    }

    private void setOnClickListeners(){
        Log.d(TAG, "setOnClickListeners: Started");

        llStakeGPSPoint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openGpsStakeoutActivity();
            }
        });

    }

    private void openGpsStakeoutActivity(){
        Log.d(TAG, "openGpsStakeoutActivity: Started");

        Intent i = new Intent(getActivity(), JobGPSSurveyWork.class);
        i.putExtra(getString(R.string.KEY_PROJECT_ID),project_id);
        i.putExtra(getString(R.string.KEY_JOB_ID), job_id);
        i.putExtra(getString(R.string.KEY_JOB_DATABASE), jobDatabaseName);

        getActivity().startActivity(i);
        getActivity().overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }


}