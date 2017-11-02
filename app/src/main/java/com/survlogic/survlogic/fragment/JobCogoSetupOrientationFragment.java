package com.survlogic.survlogic.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.survlogic.survlogic.R;

/**
 * Created by chrisfillmore on 5/2/2017.
 */

public class JobCogoSetupOrientationFragment extends Fragment {

    View v;
    Context mContext;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.tab_content_cogo_setup_orientation, container, false);


        mContext = getActivity();

        initViewWidgets(v);
        setOnClickListener(v);


        return v;
    }

    private void initViewWidgets(View v){

    }

    private void setOnClickListener(View v){

    }

}