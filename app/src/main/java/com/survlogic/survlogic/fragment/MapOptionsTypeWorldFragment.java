package com.survlogic.survlogic.fragment;

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

public class MapOptionsTypeWorldFragment extends Fragment {


    View v;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.content_map_options_map_type_world, container, false);

        return v;
    }

}