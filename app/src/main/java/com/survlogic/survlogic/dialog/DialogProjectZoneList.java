package com.survlogic.survlogic.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.survlogic.survlogic.R;
import com.survlogic.survlogic.adapter.ProjectProjectionsAdapter;
import com.survlogic.survlogic.adapter.ProjectZonesAdapter;
import com.survlogic.survlogic.interf.JobCogoMapCheckPointListListener;
import com.survlogic.survlogic.interf.ProjectProjectionListener;
import com.survlogic.survlogic.model.PointSurvey;
import com.survlogic.survlogic.utils.PreferenceLoaderHelper;

import java.text.DecimalFormat;


/**
 * Created by chrisfillmore on 8/21/2017.
 */

public class DialogProjectZoneList extends DialogFragment {
    private static final String TAG = "DialogJobPointView";

    private Context mContext;

    private String[] zoneList;
    private boolean isSPCS;


    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private ProjectZonesAdapter adapter;

    private ProgressBar progressBar;
    private Button btClose;

    private SharedPreferences sharedPreferences;
    private PreferenceLoaderHelper preferenceLoaderHelper;

    private static DecimalFormat COORDINATE_FORMATTER, DISTANCE_PRECISION_FORMATTER;
    private static final int DELAY_TO_LIST = 50;
    private static final int DELAY_TO_REFRESH = 1000;


    public static DialogProjectZoneList newInstance(String[] lstZones, boolean isSPCS) {
        Log.d(TAG, "newInstance: Starting...");
        DialogProjectZoneList frag = new DialogProjectZoneList();
        Bundle args = new Bundle();
        args.putStringArray("list",lstZones);
        args.putBoolean("SPCS",isSPCS);
        frag.setArguments(args);
        return frag;

    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Log.d(TAG, "onCreateDialog: Starting...>");

        zoneList = getArguments().getStringArray("list");
        isSPCS = getArguments().getBoolean("SPCS");

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.DialogPointViewStyle);
        LayoutInflater inflater = getActivity().getLayoutInflater();

        View v = inflater.inflate(R.layout.dialog_projection_zone_list,null);
        builder.setView(v);

        builder.create();
        return builder.show();
    }


    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: Started...");
        mContext = getActivity();
        AlertDialog alertDialog = (AlertDialog) getDialog();

        alertDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP){
                    Log.d(TAG, "onKey: Canceling All Handlers");


                    return false;
                }
                return false;
            }
        });

        preferenceLoaderHelper = new PreferenceLoaderHelper(mContext);
        loadPreferences();

        initViewWidgets();
        setOnClickListeners();

        setupPointsLocalTable();

    }

    //-------------------------------------------------------------------------------------------------------------------------//

    /**
     * JAVA Methods
     */

    private void initViewWidgets(){
        Log.d(TAG, "initViewWidgets: Started...");

        progressBar = (ProgressBar) getDialog().findViewById(R.id.progressBar_Loading);
        btClose = (Button) getDialog().findViewById(R.id.dialog_close);
    }

    private void setOnClickListeners(){
        Log.d(TAG, "setOnClickListeners: Started...");

        btClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
    }


    private void loadPreferences(){
        Log.d(TAG, "loadPreferences: Started...");

        COORDINATE_FORMATTER = new DecimalFormat(preferenceLoaderHelper.getValueSystemCoordinatesPrecisionDisplay());
        DISTANCE_PRECISION_FORMATTER = new DecimalFormat(preferenceLoaderHelper.getValueSystemDistancePrecisionDisplay());

    }


    private void setupPointsLocalTable(){

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                initZonesLocal();
            }
        },DELAY_TO_LIST);

    }

    //-------------------------------------------------------------------------------------------------------------------------//

    /**
     * Database Helpers
     */

    private void initZonesLocal(){
        Log.d(TAG, "initProjectionsLocal: Started");

        DialogProjectZoneList dialogProjectProjectionsList = this;
        ProjectProjectionListener listener = (ProjectProjectionListener) getActivity();

        mRecyclerView = (RecyclerView) getDialog().findViewById(R.id.zoneListRecyclerView);
        layoutManager = new LinearLayoutManager(mContext);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setHasFixedSize(false);

        adapter = new ProjectZonesAdapter(mContext,dialogProjectProjectionsList,listener,zoneList,isSPCS);
        mRecyclerView.setAdapter(adapter);

        Log.e(TAG,"Complete: initProjectListRecyclerView");


        progressBar.setVisibility(View.GONE);
    }


    //--------------------------------------------------------------------------------------------------------------------------//

    public void setPointSurveyData(PointSurvey pointSurvey){
        Log.d(TAG, "setPointSurveyData: Point No. Clicked:" + String.valueOf(pointSurvey.getPoint_no()));

        JobCogoMapCheckPointListListener listener = (JobCogoMapCheckPointListListener) getActivity();

        listener.onReturnValuesOccupy(pointSurvey);

    }


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
