package com.survlogic.survlogic.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.survlogic.survlogic.R;
import com.survlogic.survlogic.interf.JobMapOptionsListener;

/**
 * Created by chrisfillmore on 5/2/2017.
 */

public class MapOptionsTypePlanarFragment extends Fragment {
    private static final String TAG = "MapOptionsTypePlanarFra";

    private Context mContext;
    
    private ImageButton ibMapTypePlanarDefault;

    View v;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.content_map_options_map_type_planar, container, false);

        mContext = getActivity();
        initViewWidgets(v);
        setOnClickListener(v);

        return v;
    }


    private void initViewWidgets(View v){
        Log.d(TAG, "initViewWidgets: Starting...");

        ibMapTypePlanarDefault = (ImageButton) v.findViewById(R.id.map_type_planar_default);
        
    }



    private void setOnClickListener(View v) {
        Log.d(TAG, "setOnClickListener: Starting...");


        ibMapTypePlanarDefault.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                JobMapOptionsListener listener = (JobMapOptionsListener) getActivity();
                listener.onSetMapType(true, 1);

            }
        });


    }
    
    

}