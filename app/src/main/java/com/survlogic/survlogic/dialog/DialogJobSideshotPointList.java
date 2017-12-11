package com.survlogic.survlogic.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.survlogic.survlogic.R;
import com.survlogic.survlogic.adapter.JobSetupPointListAdaptor;
import com.survlogic.survlogic.adapter.JobSideshotPointListAdaptor;
import com.survlogic.survlogic.interf.JobCogoSetupPointListListener;
import com.survlogic.survlogic.model.PointSurvey;
import com.survlogic.survlogic.utils.PreferenceLoaderHelper;

import java.text.DecimalFormat;
import java.util.ArrayList;


/**
 * Created by chrisfillmore on 8/21/2017.
 */

public class DialogJobSideshotPointList extends DialogFragment {
    private static final String TAG = "DialogJobPointView";

    private Context mContext;

    private int projectID, jobId;
    private String databaseName;

    private ArrayList<PointSurvey> pointList;

    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private JobSideshotPointListAdaptor adapter;

    private ProgressBar progressBar;

    private SharedPreferences sharedPreferences;
    private PreferenceLoaderHelper preferenceLoaderHelper;

    private static DecimalFormat COORDINATE_FORMATTER, DISTANCE_PRECISION_FORMATTER;
    private static final int DELAY_TO_LIST = 50;
    private static final int DELAY_TO_REFRESH = 1000;


    public static DialogJobSideshotPointList newInstance(int projectId, int jobId, String databaseName, ArrayList<PointSurvey> items) {
        Log.d(TAG, "newInstance: Starting...");
        DialogJobSideshotPointList frag = new DialogJobSideshotPointList();
        Bundle args = new Bundle();
        args.putInt("project_id", projectId);
        args.putInt("job_id", jobId);
        args.putString("databaseName", databaseName);
        args.putParcelableArrayList("list",items);
        frag.setArguments(args);
        return frag;

    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Log.d(TAG, "onCreateDialog: Starting...>");

        projectID = getArguments().getInt("project_id");
        jobId = getArguments().getInt("job_id");
        databaseName = getArguments().getString("databaseName");

        pointList = getArguments().getParcelableArrayList("list");

        Log.d(TAG, "onCreateDialog: Project Id: " + projectID );
        Log.d(TAG, "onCreateDialog: Job Id: " + jobId );

        Log.d(TAG, "onCreateDialog: Database Name:" + databaseName + " Loaded...");

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.DialogPointViewStyle);
        LayoutInflater inflater = getActivity().getLayoutInflater();

        View v = inflater.inflate(R.layout.dialog_job_setup_point_list,null);
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

    }

    private void setOnClickListeners(){
        Log.d(TAG, "setOnClickListeners: Started...");


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
                initPointsLocal();
            }
        },DELAY_TO_LIST);

    }

    //-------------------------------------------------------------------------------------------------------------------------//

    /**
     * Database Helpers
     */

    private void initPointsLocal(){
        Log.d(TAG, "initPointsLocal: Started");

        mRecyclerView = (RecyclerView) getDialog().findViewById(R.id.pointListRecyclerView);
        layoutManager = new LinearLayoutManager(mContext);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setHasFixedSize(false);

        adapter = new JobSideshotPointListAdaptor(mContext,pointList, DialogJobSideshotPointList.this, COORDINATE_FORMATTER);
        mRecyclerView.setAdapter(adapter);

        Log.e(TAG,"Complete: initProjectListRecyclerView");


        progressBar.setVisibility(View.GONE);
    }


    //--------------------------------------------------------------------------------------------------------------------------//




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
