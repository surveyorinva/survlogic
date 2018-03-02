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

/**
 * Created by chrisfillmore on 2/20/2018.
 */

public class JobPointsFilterPage1Fragment extends Fragment {
    private static final String TAG = "JobPointsNewPointPage1F";

    private View v;
    private Context mContext;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView: Started--------------------->");
        v = inflater.inflate(R.layout.fragment_job_point_filter_page_1,container,false);
        mContext = getActivity();

        return v;
    }



}
