package com.survlogic.survlogic.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.survlogic.survlogic.R;
import com.survlogic.survlogic.background.BackgroundProjectList;

/**
 * Created by chrisfillmore on 5/2/2017.
 */

public class MainHomeFragment extends Fragment {

    View v;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.fragment_main_home,container,false);

        return v;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        showProjectsLocal();

    }


    private void showProjectsLocal(){
        BackgroundProjectList backgroundProjectList = new BackgroundProjectList(getActivity());
        backgroundProjectList.execute();


    }


}
