package com.survlogic.survlogic.activity;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;

import com.survlogic.survlogic.R;
import com.survlogic.survlogic.model.Point;
import com.survlogic.survlogic.model.PointMapCheck;
import com.survlogic.survlogic.model.PointSurvey;
import com.survlogic.survlogic.utils.MathHelper;
import com.survlogic.survlogic.utils.PreferenceLoaderHelper;

import java.util.ArrayList;

/**
 * Created by chrisfillmore on 12/29/2017.
 */

public class JobCogoMapCheckResultsActivity extends AppCompatActivity {

    private static final String TAG = "JobCogoMapCheckResultsA";
    private Context mContext;
    private PreferenceLoaderHelper preferenceLoaderHelper;

    private int project_id, job_id;
    private String jobDatabaseName;
    private ArrayList<PointSurvey> lstPointSurvey = new ArrayList<>();
    private ArrayList<PointMapCheck> lstPointMapCheck = new ArrayList<>();
    private PointSurvey occupyPoint;

    ImageButton ibBack, ibSave;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: Started");
        setContentView(R.layout.activity_job_cogo_mapcheck_results);

        mContext = JobCogoMapCheckResultsActivity.this;
        preferenceLoaderHelper = new PreferenceLoaderHelper(mContext);

        initViewWidget();
        setOnClickListeners();
        populateListWithCoordinates();

    }

    private void initViewWidget(){
        Log.d(TAG, "initViewWidget: Started...");

        Bundle extras = getIntent().getExtras();
        project_id = extras.getInt(getString(R.string.KEY_PROJECT_ID));
        job_id = extras.getInt(getString(R.string.KEY_JOB_ID));
        jobDatabaseName = extras.getString(getString(R.string.KEY_JOB_DATABASE));

        lstPointMapCheck = extras.getParcelableArrayList(getString(R.string.KEY_ARRAY_LIST_MAPCHECK));
        Log.d(TAG, "initViewWidget: lstPointMapCheck Size: " + lstPointMapCheck.size());

        occupyPoint = extras.getParcelable(getString(R.string.KEY_SETUP_OCCUPY_PT));

        ibBack = (ImageButton) findViewById(R.id.button_back);
        ibSave = (ImageButton) findViewById(R.id.button_save);


    }

    private void setOnClickListeners(){

        ibBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    //----------------------------------------------------------------------------------------------//

    private void populateListWithCoordinates(){
        for(int i=0; i<lstPointMapCheck.size(); i++) {
            PointMapCheck pointMapCheck = lstPointMapCheck.get(i);

            calculateCoordinates(i,pointMapCheck);
        }
    }

    private void calculateCoordinates(int position, PointMapCheck currentItem){
        Log.d(TAG, "calculateCoordinates: Started...");
        double startNorthing, startEasting, backAzNorthing, backAzEasting;
        double mValueAngle, mValueDistance;
        Point startPoint = new Point(), backAzPoint = new Point(), endPoint;
        PointMapCheck mapCheck;


        //Starting Point
        if(position == 0) {
            Log.d(TAG, "calculateCoordinates: Position 0");
            //1st position, get start coordinates from occupyPointNo
            startNorthing = occupyPoint.getNorthing();
            startEasting = occupyPoint.getEasting();

            Log.d(TAG, "calculateCoordinates: Starting Coords:" + startNorthing + "," + startEasting);

        }else if(position == 1){
            Log.d(TAG, "calculateCoordinates: Position: 1 ");
            startNorthing = lstPointMapCheck.get(position-1).getToPointNorth();
            startEasting = lstPointMapCheck.get(position-1).getToPointEast();

            backAzNorthing = occupyPoint.getNorthing();
            backAzEasting = occupyPoint.getEasting();

            backAzPoint.setNorthing(backAzNorthing);
            backAzPoint.setEasting(backAzEasting);

        }else{
            //get from previous mapCheckItem
            Log.d(TAG, "calculateCoordinates: Position: " + position);

            startNorthing = lstPointMapCheck.get(position-1).getToPointNorth();
            startEasting = lstPointMapCheck.get(position-1).getToPointEast();

            backAzNorthing = lstPointMapCheck.get(position-2).getToPointNorth();
            backAzEasting = lstPointMapCheck.get(position-2).getToPointEast();

            backAzPoint.setNorthing(backAzNorthing);
            backAzPoint.setEasting(backAzEasting);

            Log.d(TAG, "calculateCoordinates: Starting Coords:" + startNorthing + "," + startEasting);
        }

        startPoint.setNorthing(startNorthing);
        startPoint.setEasting(startEasting);

        //Observation Type
        mapCheck = lstPointMapCheck.get(position);

        int observationType = mapCheck.getObservationType();

        switch (observationType){
            case 0://Bearing
                Log.d(TAG, "calculateCoordinates: Bearing");
                mValueAngle = mapCheck.getLineAngle();
                mValueDistance = mapCheck.getLineDistance();

                endPoint = MathHelper.solveForCoordinatesFromBearing(startPoint,mValueAngle,mValueDistance,90d);

                break;

            case 1://Azimuth
                Log.d(TAG, "calculateCoordinates: Azimuth");
                mValueAngle = mapCheck.getLineAngle();
                mValueDistance = mapCheck.getLineDistance();

                endPoint = MathHelper.solveForCoordinatesFromAzimuth(startPoint,mValueAngle,mValueDistance,90d);
                break;

            case 2: //Turned Angle
                Log.d(TAG, "calculateCoordinates: Turned Angle");
                mValueAngle = mapCheck.getLineAngle();
                mValueDistance = mapCheck.getLineDistance();

                endPoint = MathHelper.solveForCoordinatesFromTurnedAngleAndDistance(startPoint, backAzPoint, mValueAngle,mValueDistance,90d);
                break;

            default:
                Log.d(TAG, "calculateCoordinates: Bearing");
                mValueAngle = mapCheck.getLineAngle();
                mValueDistance = mapCheck.getLineDistance();

                endPoint = MathHelper.solveForCoordinatesFromBearing(startPoint,mValueAngle,mValueDistance,90d);
                break;
        }

        lstPointMapCheck.get(position).setToPointNorth(endPoint.getNorthing());
        lstPointMapCheck.get(position).setToPointEast(endPoint.getEasting());

    }

}
