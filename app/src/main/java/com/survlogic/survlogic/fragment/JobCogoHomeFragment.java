package com.survlogic.survlogic.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.survlogic.survlogic.R;
import com.survlogic.survlogic.dialog.DialogJobCogoSetup;

/**
 * Created by chrisfillmore on 5/2/2017.
 */

public class JobCogoHomeFragment extends Fragment {

    private LinearLayout llCogoSetup;

    View v;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.fragment_job_cogo_home, container, false);

        initViewWidgets(v);
        setOnClickListeners();


        return v;
    }


    private void initViewWidgets(View v){

        llCogoSetup = (LinearLayout) v.findViewById(R.id.llActionItemSetup);



    }


    private void setOnClickListeners(){

        llCogoSetup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                android.support.v4.app.DialogFragment pointCogoSetup = DialogJobCogoSetup.newInstance();
                pointCogoSetup.show(getFragmentManager(),"dialog_cogo_setup");
            }
        });
    }

}