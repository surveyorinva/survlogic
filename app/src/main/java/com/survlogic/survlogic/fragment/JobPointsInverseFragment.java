package com.survlogic.survlogic.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.survlogic.survlogic.R;
import com.survlogic.survlogic.model.PointGeodetic;
import com.survlogic.survlogic.model.PointSurvey;

import java.util.ArrayList;

/**
 * Created by chrisfillmore on 5/2/2017.
 */

public class JobPointsInverseFragment extends Fragment {
    private static final String TAG = "JobPointsInverseFragmen";
    View v;
    Context mContext;

    private ArrayList<PointSurvey> lstSelectedPoints = new ArrayList<>();
    private ArrayList<PointSurvey> lstPointSurvey = new ArrayList<>();
    private ArrayList<PointGeodetic> lstPointGeodetic = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.fragment_job_points_inverse, container, false);


        mContext = getActivity();

        initViewWidgets(v);
        setOnClickListener(v);
        
        
        return v;
    }
    
    private void initViewWidgets(View v){
        Log.d(TAG, "initViewWidgets: Started");
    }
    
    private void setOnClickListener(View v){
        Log.d(TAG, "setOnClickListener: Started");
    }



    //---------------------------------------------------------------------------------------------------------//
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