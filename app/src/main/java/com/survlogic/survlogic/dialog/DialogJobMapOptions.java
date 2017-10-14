package com.survlogic.survlogic.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
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
import com.survlogic.survlogic.adapter.DialogJobMapOptionsMapTypeAdapter;
import com.survlogic.survlogic.interf.JobMapOptionsListener;
import com.survlogic.survlogic.utils.PreferenceLoaderHelper;

import java.util.ArrayList;

/**
 * Created by chrisfillmore on 8/21/2017.
 */

public class DialogJobMapOptions extends DialogFragment {
    private static final String TAG = "DialogJobPointView";

    private Context mContext;

    private boolean pointNoIn, pointElevIn, pointDescIn;
    private boolean pointNoOut = false, pointElevOut = false, pointDescOut = false;

    private boolean selectedValues[];

    private SharedPreferences sharedPreferences;
    private PreferenceLoaderHelper preferenceLoaderHelper;

    private ImageButton ibOptionsPoint, ibClose;

    public static DialogJobMapOptions newInstance(boolean pointNo, boolean pointElev, boolean pointDesc) {
        Log.d(TAG, "newInstance: Starting...");
        DialogJobMapOptions frag = new DialogJobMapOptions();
        Bundle args = new Bundle();
        args.putBoolean("pointNoIn", pointNo);
        args.putBoolean("pointElevIn", pointElev);
        args.putBoolean("pointDescIn", pointDesc);

        frag.setArguments(args);

        return frag;

    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Log.d(TAG, "onCreateDialog: Starting...>");

        pointNoIn = getArguments().getBoolean("pointNoIn");
        pointElevIn = getArguments().getBoolean("pointElevIn");
        pointDescIn = getArguments().getBoolean("pointDescIn");

        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        return dialog;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_job_map_options, container);

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
     * JAVA Methods
     */

    private void initViewWidgets(View view){
        Log.d(TAG, "initViewWidgets: Started...");
        DialogJobMapOptionsMapTypeAdapter tabAdapter = new DialogJobMapOptionsMapTypeAdapter(getChildFragmentManager());
        ViewPager viewPager = (ViewPager) view.findViewById(R.id.pager);
        viewPager.setAdapter(tabAdapter);

        TabLayout tabLayout = (TabLayout) view.findViewById(R.id.tabDots);
        tabLayout.setupWithViewPager(viewPager, true);

        ibOptionsPoint = (ImageButton)  view.findViewById(R.id.optionsPoints);
        ibClose = (ImageButton) view.findViewById(R.id.dialog_close);

    }

    private void setOnClickListeners(){
        Log.d(TAG, "setOnClickListeners: Started...");

        ibOptionsPoint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createDialogForPointOptions();
            }
        });

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


    private void createDialogForPointOptions(){
        AlertDialog dialog;
        final CharSequence[] items;
        // arraylist to keep the selected items
        final ArrayList seletedItems = new ArrayList();
        final boolean[] checkedValues;

        items=getResources().getStringArray(R.array.map_show_point_options);

        checkedValues  = new boolean[items.length];
        checkedValues[0] = pointNoIn; //point no.
        checkedValues[1] = pointElevIn; //elevation
        checkedValues[2] = pointDescIn; //description

        selectedValues = new boolean[items.length];
        selectedValues[0] = pointNoIn; //point no.
        selectedValues[1] = pointElevIn; //elevation
        selectedValues[2] = pointDescIn; //description

        Log.i(TAG, "Receiving In: " + pointNoIn + "/" + pointElevIn + "/" + pointDescIn);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Point Layers to Show");
        builder.setMultiChoiceItems(items, checkedValues,
                new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int indexSelected,
                                        boolean isChecked) {
                        if (isChecked) {

                            selectedValues[indexSelected]=true;

                        }else{
                            selectedValues[indexSelected] = false;
                        }
                    }
                })
                // Set the action buttons
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        setValues();

                        Log.i(TAG, "Sending Out: " + pointNoOut + "/" + pointElevOut + "/" + pointDescOut);

                        JobMapOptionsListener listener = (JobMapOptionsListener) getActivity();
                        listener.onReturnValues(pointNoOut, pointElevOut, pointDescOut);
                        dismiss();

                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {


                    }
                });

        dialog = builder.create();
        dialog.show();
    }




    //-------------------------------------------------------------------------------------------------------------------------//

    /**
     * Method Helpers
     */


    private void setValues(){
        for(int i=0;i<selectedValues.length;i++){
            Log.i(TAG, "setValues: Set Value: " + i + "= " + selectedValues[i]);
            setValueTrueOrFalse(i,selectedValues[i]);
        }
    }

    private void setValueTrueOrFalse(int position, boolean result){
        switch (position){
            case 0:
                pointNoOut = result;
                break;
            case 1:
                pointElevOut = result;
                break;
            case 2:
                pointDescOut = result;
                break;
        }
    }



    private void showToast(String data, boolean shortTime) {

        if (shortTime) {
            Toast.makeText(getActivity(), data, Toast.LENGTH_SHORT).show();

        } else{
            Toast.makeText(getActivity(), data, Toast.LENGTH_LONG).show();

        }
    }
}
