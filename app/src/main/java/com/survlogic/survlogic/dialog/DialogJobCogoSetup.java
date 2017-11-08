package com.survlogic.survlogic.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.DialogFragment;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.Toast;

import com.survlogic.survlogic.R;
import com.survlogic.survlogic.adapter.ActivityJobCogoSetupTypeAdapter;
import com.survlogic.survlogic.utils.PreferenceLoaderHelper;

/**
 * Created by chrisfillmore on 8/21/2017.
 */

public class DialogJobCogoSetup extends DialogFragment {
    private static final String TAG = "DialogJobCogoSetup";

    private Context mContext;


    private SharedPreferences sharedPreferences;
    private PreferenceLoaderHelper preferenceLoaderHelper;

    private ImageButton ibClose;


    public static DialogJobCogoSetup newInstance() {
        Log.d(TAG, "newInstance: Starting...");
        DialogJobCogoSetup frag = new DialogJobCogoSetup();
        Bundle args = new Bundle();

        frag.setArguments(args);

        return frag;

    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Log.d(TAG, "onCreateDialog: Starting...>");

        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        return dialog;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_job_cogo_setup, container);

        initViewWidgets(view);
        setOnClickListeners();

        return view;
    }


    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: Started...");
        mContext = getActivity();

        preferenceLoaderHelper = new PreferenceLoaderHelper(mContext);
        loadPreferences();



    }

    //-------------------------------------------------------------------------------------------------------------------------//

    /**
     * JAVA Met`hods
     */

    private void initViewWidgets(View view){
        Log.d(TAG, "initViewWidgets: Started...");
        ActivityJobCogoSetupTypeAdapter tabAdapter = new ActivityJobCogoSetupTypeAdapter(getChildFragmentManager());
        ViewPager viewPager = (ViewPager) view.findViewById(R.id.pager);
        viewPager.setAdapter(tabAdapter);

        TabLayout tabLayout = (TabLayout) view.findViewById(R.id.tabDots);
        tabLayout.setupWithViewPager(viewPager, true);

        ibClose = (ImageButton) view.findViewById(R.id.dialog_close);

    }

    private void setOnClickListeners(){
        Log.d(TAG, "setOnClickListeners: Started...");


        ibClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });


    }


    private void loadPreferences(){
        Log.d(TAG, "loadPreferences: Started...");


    }



    //-------------------------------------------------------------------------------------------------------------------------//

    /**
     * Dialog Helpers
     */







    //-------------------------------------------------------------------------------------------------------------------------//

    /**
     * Method Helpers
     */

    private void showToast(String data, boolean shortTime) {

        if (shortTime) {
            Toast.makeText(getActivity(), data, Toast.LENGTH_SHORT).show();

        } else{
            Toast.makeText(getActivity(), data, Toast.LENGTH_LONG).show();

        }
    }
}
