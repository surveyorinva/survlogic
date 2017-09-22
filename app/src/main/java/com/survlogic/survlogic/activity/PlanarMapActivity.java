package com.survlogic.survlogic.activity;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.survlogic.survlogic.R;

/**
 * Created by chrisfillmore on 9/13/2017.
 */

public class PlanarMapActivity extends AppCompatActivity {
    private static final String TAG = "PlanarMapActivity";
    private Context mContext;

    private int pointNo, projectID, jobId, pointId;
    private String databaseName;
    
    
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_job_points_map);
        mContext = PlanarMapActivity.this;
        
        initViewWidgets();

    }

    private void initViewWidgets(){
        Bundle extras = getIntent().getExtras();
        projectID = extras.getInt(getString(R.string.KEY_PROJECT_ID));
        jobId = extras.getInt(getString(R.string.KEY_JOB_ID));
        databaseName = extras.getString(getString(R.string.KEY_JOB_DATABASE));

        Log.d(TAG, "Variables- Project ID: " + projectID);
        Log.d(TAG, "Variables- Job ID: " + jobId);
        Log.d(TAG, "Variables- Point ID: " + pointId);
        Log.d(TAG, "Variables- Point No: " + pointNo);
        Log.d(TAG, "Variables- Database: " + databaseName);
        
        
        
    }
}
