package com.survlogic.survlogic.activity;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.survlogic.survlogic.R;
import com.survlogic.survlogic.adapter.JobMapCheckObservationsAdaptor;
import com.survlogic.survlogic.adapter.JobMapCheckResultsAdaptor;
import com.survlogic.survlogic.background.BackgroundPointSurveyNew;
import com.survlogic.survlogic.background.BackgroundPointSurveyNewMultiple;
import com.survlogic.survlogic.model.Point;
import com.survlogic.survlogic.model.PointMapCheck;
import com.survlogic.survlogic.model.PointSurvey;
import com.survlogic.survlogic.utils.MathHelper;
import com.survlogic.survlogic.utils.PreferenceLoaderHelper;
import com.survlogic.survlogic.utils.SwipeAndDragHelper;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;

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
    private ArrayList<PointMapCheck> lstPointMapCheckResults = new ArrayList<>();
    private PointSurvey occupyPoint;
    private PointSurvey closingPoint = new PointSurvey();

    private double distanceTraveled = 0;
    private double areaCurveToRight = 0, areaCurveToLeft = 0;

    private RecyclerView.LayoutManager layoutManagerMapCheck;
    private JobMapCheckResultsAdaptor adaptorJobMapCheckResults;

    private RecyclerView mRecyclerViewMapCheck;
    private TextView tvDistanceTraveled, tvClosingErrorDirection, tvClosingErrorDistance,
                    tvClosingPrecision, tvCloseAreaA, tvCloseAreaB;
    private ImageButton ibBack, ibSave;

    private static DecimalFormat COORDINATE_FORMATTER, DISTANCE_PRECISION_FORMATTER;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: Started");
        setContentView(R.layout.activity_job_cogo_mapcheck_results);

        mContext = JobCogoMapCheckResultsActivity.this;
        preferenceLoaderHelper = new PreferenceLoaderHelper(mContext);

        initViewWidget();
        loadPreferences();
        setOnClickListeners();
        initViewList();

        populateListWithCoordinates();


    }

    private void loadPreferences(){
        Log.d(TAG, "loadPreferences: Started...");

        COORDINATE_FORMATTER = new DecimalFormat(preferenceLoaderHelper.getValueSystemCoordinatesPrecisionDisplay());
        DISTANCE_PRECISION_FORMATTER = new DecimalFormat(preferenceLoaderHelper.getValueSystemDistancePrecisionDisplay());

    }

    private void initViewWidget(){
        Log.d(TAG, "initViewWidget: Started...");

        Bundle extras = getIntent().getExtras();
        project_id = extras.getInt(getString(R.string.KEY_PROJECT_ID));
        job_id = extras.getInt(getString(R.string.KEY_JOB_ID));
        jobDatabaseName = extras.getString(getString(R.string.KEY_JOB_DATABASE));

        lstPointMapCheck = extras.getParcelableArrayList(getString(R.string.KEY_ARRAY_LIST_MAPCHECK));
        lstPointMapCheckResults = new ArrayList<>();

        Log.d(TAG, "initViewWidget: lstPointMapCheck Size: " + lstPointMapCheck.size());

        occupyPoint = extras.getParcelable(getString(R.string.KEY_SETUP_OCCUPY_PT));

        ibBack = (ImageButton) findViewById(R.id.button_back);
        ibSave = (ImageButton) findViewById(R.id.button_save);

        tvDistanceTraveled = (TextView) findViewById(R.id.closure_distance_traveled);
        tvClosingErrorDirection = (TextView) findViewById(R.id.closure_error_direction);
        tvClosingErrorDistance = (TextView) findViewById(R.id.closure_error_distance);
        tvClosingPrecision = (TextView) findViewById(R.id.closure_error_precision);

        tvCloseAreaA = (TextView) findViewById(R.id.closure_area_1);
        tvCloseAreaB = (TextView) findViewById(R.id.closure_area_2);
    }

    private void setOnClickListeners(){

        ibBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        ibSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveToDatabase();
            }
        });

    }

    //----------------------------------------------------------------------------------------------//
    private void initViewList(){
        Log.d(TAG, "initViewList: Started...");

        mRecyclerViewMapCheck = (RecyclerView) findViewById(R.id.mapcheck_items_list);
        setMapCheckAdapter();

    }

    private void setMapCheckAdapter(){
        Log.d(TAG, "setMapCheckAdapter: Started");
        layoutManagerMapCheck = new LinearLayoutManager(mContext);
        mRecyclerViewMapCheck.setLayoutManager(layoutManagerMapCheck);
        mRecyclerViewMapCheck.setHasFixedSize(false);

        adaptorJobMapCheckResults = new JobMapCheckResultsAdaptor(mContext,lstPointMapCheckResults, COORDINATE_FORMATTER);

        mRecyclerViewMapCheck.setAdapter(adaptorJobMapCheckResults);


        Log.e(TAG,"Complete: setMapCheckAdapter");
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
        double deltaAngle, radius, arcLength;
        boolean isCurveToRight;

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

            int previousPositionObservationType = lstPointMapCheck.get(position-1).getObservationType();

            if(previousPositionObservationType == 0 | previousPositionObservationType == 1 | previousPositionObservationType == 2){
                Log.d(TAG, "calculateCoordinates: Previous Position is a Line!");
                //if previous observation was a line then:
                backAzNorthing = lstPointMapCheck.get(position-2).getToPointNorth();
                backAzEasting = lstPointMapCheck.get(position-2).getToPointEast();

            }else if(previousPositionObservationType == 3 | previousPositionObservationType == 4 | previousPositionObservationType == 5){
                //else if previous observation was a curve, need to calculate the PI
                //determine if it is right or left to use PI
                Log.d(TAG, "calculateCoordinates: Previous Position is a Curve!");
                double previousCurveDeltaDEC = lstPointMapCheck.get(position-1).getCurveDelta();
                double previousCurveRadius = lstPointMapCheck.get(position-1).getCurveRadius();
                boolean isPreviousCurveToRight = lstPointMapCheck.get(position - 1).isCurveToRight();

                double pcCoordNorth = lstPointMapCheck.get(position-2).getToPointNorth();
                double pcCoordEast = lstPointMapCheck.get(position-2).getToPointEast();

                Point previousCurvePC = new Point(pcCoordNorth,pcCoordEast);
                Point previousCurvePT = new Point(startNorthing,startEasting);
                Point previousCurvePI = MathHelper.solveForCurvePIForBackTangent(previousCurvePC,previousCurvePT,previousCurveDeltaDEC,previousCurveRadius, isPreviousCurveToRight);

                backAzNorthing = previousCurvePI.getNorthing();
                backAzEasting = previousCurvePI.getEasting();
            }else{
                backAzNorthing = 0;
                backAzEasting = 0;
            }


            backAzPoint.setNorthing(backAzNorthing);
            backAzPoint.setEasting(backAzEasting);

            Log.d(TAG, "calculateCoordinates: Starting Coords:" + startNorthing + "," + startEasting);
        }

        startPoint.setNorthing(startNorthing);
        startPoint.setEasting(startEasting);

        //Observation Type
        mapCheck = lstPointMapCheck.get(position);

        int observationType = mapCheck.getObservationType();
        Log.d(TAG, "calculateCoordinates: observation Type: " + observationType);

        switch (observationType){
            case 0://Bearing
                Log.d(TAG, "calculateCoordinates: Bearing");
                mValueAngle = mapCheck.getLineAngle();
                mValueDistance = mapCheck.getLineDistance();

                endPoint = MathHelper.solveForCoordinatesFromBearing(startPoint,mValueAngle,mValueDistance,90d);
                distanceTraveled = distanceTraveled + mValueDistance;

                break;

            case 1://Azimuth
                Log.d(TAG, "calculateCoordinates: Azimuth");
                mValueAngle = mapCheck.getLineAngle();
                mValueDistance = mapCheck.getLineDistance();

                endPoint = MathHelper.solveForCoordinatesFromAzimuth(startPoint,mValueAngle,mValueDistance,90d);
                distanceTraveled = distanceTraveled + mValueDistance;

                break;

            case 2: //Turned Angle
                Log.d(TAG, "calculateCoordinates: Turned Angle");
                mValueAngle = mapCheck.getLineAngle();
                mValueDistance = mapCheck.getLineDistance();

                endPoint = MathHelper.solveForCoordinatesFromTurnedAngleAndDistance(startPoint, backAzPoint, mValueAngle,mValueDistance,90d);
                distanceTraveled = distanceTraveled + mValueDistance;
                break;

            case 3: //Delta-Radius
                Log.d(TAG, "calculateCoordinates: Curve: Delta and Radius");
                deltaAngle = mapCheck.getCurveDelta();
                radius = mapCheck.getCurveRadius();
                isCurveToRight = mapCheck.isCurveToRight();

                mValueDistance = MathHelper.solveForCurveChordDistance(deltaAngle,radius);
                mValueAngle = MathHelper.solveForCurveChordDirectionAzimuthDEC(backAzPoint,startPoint,deltaAngle,isCurveToRight);

                Log.d(TAG, "calculateCoordinates: Delta Angle: " + deltaAngle);
                Log.d(TAG, "calculateCoordinates: Radius: " + radius);
                Log.d(TAG, "calculateCoordinates: Chord Distance: " + mValueDistance);
                Log.d(TAG, "calculateCoordinates: Chord Azimuth: " + mValueAngle);

                endPoint = MathHelper.solveForCoordinatesFromAzimuth(startPoint,mValueAngle,mValueDistance,90d);

                arcLength = MathHelper.solveForCurveLength(deltaAngle,radius);
                distanceTraveled = distanceTraveled + arcLength;

                if(isCurveToRight){
                    areaCurveToRight = areaCurveToRight + MathHelper.solveForCurveSegmentArea(deltaAngle,radius);
                }else{
                    areaCurveToLeft = areaCurveToLeft + MathHelper.solveForCurveSegmentArea(deltaAngle,radius);
                }

                break;


            case 4: //Delta-Length
                deltaAngle = mapCheck.getCurveDelta();
                arcLength = mapCheck.getCurveLength();
                isCurveToRight = mapCheck.isCurveToRight();

                radius = MathHelper.solveForCurveRadius(deltaAngle,arcLength);

                mValueDistance = MathHelper.solveForCurveChordDistance(deltaAngle,radius);
                mValueAngle = MathHelper.solveForCurveChordDirectionAzimuthDEC(backAzPoint,startPoint,deltaAngle,isCurveToRight);

                endPoint = MathHelper.solveForCoordinatesFromAzimuth(startPoint,mValueAngle,mValueDistance,90d);

                distanceTraveled = distanceTraveled + arcLength;
                if(isCurveToRight){
                    areaCurveToRight = areaCurveToRight + MathHelper.solveForCurveSegmentArea(deltaAngle,radius);
                }else{
                    areaCurveToLeft = areaCurveToLeft + MathHelper.solveForCurveSegmentArea(deltaAngle,radius);
                }

                break;

            case 5: //Radius-Length
                radius = mapCheck.getCurveRadius();
                arcLength = mapCheck.getCurveLength();
                isCurveToRight = mapCheck.isCurveToRight();

                deltaAngle = MathHelper.solveForCurveDeltaAngle(radius,arcLength);

                mValueDistance = MathHelper.solveForCurveChordDistance(deltaAngle,radius);
                mValueAngle = MathHelper.solveForCurveChordDirectionAzimuthDEC(backAzPoint,startPoint,deltaAngle,isCurveToRight);

                endPoint = MathHelper.solveForCoordinatesFromAzimuth(startPoint,mValueAngle,mValueDistance,90d);
                distanceTraveled = distanceTraveled + arcLength;
                if(isCurveToRight){
                    areaCurveToRight = areaCurveToRight + MathHelper.solveForCurveSegmentArea(deltaAngle,radius);
                }else{
                    areaCurveToLeft = areaCurveToLeft + MathHelper.solveForCurveSegmentArea(deltaAngle,radius);
                }
                break;


            default:
                Log.d(TAG, "calculateCoordinates: Bearing");
                mValueAngle = mapCheck.getLineAngle();
                mValueDistance = mapCheck.getLineDistance();

                endPoint = MathHelper.solveForCoordinatesFromBearing(startPoint,mValueAngle,mValueDistance,90d);
                distanceTraveled = distanceTraveled + mValueDistance;
                break;
        }

        lstPointMapCheck.get(position).setToPointNorth(endPoint.getNorthing());
        lstPointMapCheck.get(position).setToPointEast(endPoint.getEasting());

        if(!mapCheck.isClosingPoint()){
            Log.d(TAG, "calculateCoordinates: Add Point to Results");
            lstPointMapCheckResults.add(mapCheck);
            adaptorJobMapCheckResults.notifyDataSetChanged();
        }

        if(mapCheck.isClosingPoint()){
            Log.d(TAG, "Closing Point: " + endPoint.getNorthing() + ", " + endPoint.getEasting());
            closingPoint.setNorthing(endPoint.getNorthing());
            closingPoint.setEasting(endPoint.getEasting());
            prepareClosingReport();

        }else{
            prepareOpenEndedReport();
        }



        Log.d(TAG, "calculateCoordinates: Point Northing: " + endPoint.getNorthing());
        Log.d(TAG, "calculateCoordinates: Point Easting: " + endPoint.getEasting());


    }

    private void prepareClosingReport(){
        Log.d(TAG, "prepareClosingReport: Started");

        // Closing Error module
        solveClosingPoint();

        // Distance Traveled Module
        tvDistanceTraveled.setText(DISTANCE_PRECISION_FORMATTER.format(distanceTraveled));

        // Area Module
        tvCloseAreaA.setText(DISTANCE_PRECISION_FORMATTER.format(solveAreaofPolygon()));

    }

    private void prepareOpenEndedReport(){
        Log.d(TAG, "prepareOpenEndedReport: Started");

        //Distance Traveled Module
        tvDistanceTraveled.setText(DISTANCE_PRECISION_FORMATTER.format(distanceTraveled));

    }

    private void solveClosingPoint(){
        Log.d(TAG, "solveClosingPoint: Started");


        double inverseDirection = MathHelper.inverseBearingFromPointSurvey(occupyPoint,closingPoint);
        String inverseBearing = MathHelper.convertDECtoDMSBearing(inverseDirection,0);

        double inverseDistance = MathHelper.inverseDistanceFromPointSurvey(occupyPoint,closingPoint);

        tvClosingErrorDirection.setText(inverseBearing);
        tvClosingErrorDistance.setText(DISTANCE_PRECISION_FORMATTER.format(inverseDistance));

        //precision
        double  closingPrecision = (int)(distanceTraveled/inverseDistance);
        String precision = NumberFormat.getNumberInstance(Locale.getDefault()).format(closingPrecision);

        String closingPrecisionPrefix = "1:";

        precision = closingPrecisionPrefix + "" + precision;

        tvClosingPrecision.setText(precision);


    }

    private double solveAreaofPolygon(){
        ArrayList<Double> northings = new ArrayList<>();
        ArrayList<Double> eastings = new ArrayList<>();

        northings.add(occupyPoint.getNorthing());
        eastings.add(occupyPoint.getEasting());

        for(int i=0; i<lstPointMapCheck.size(); i++) {
            PointMapCheck occupy = lstPointMapCheck.get(i);

            northings.add(occupy.getToPointNorth());
            eastings.add(occupy.getToPointEast());

        }

        double area = 0;
        int numPoints = northings.size();
        int j = numPoints - 1;

        Log.d(TAG, "solveAreaofPolygon: num of points:" + numPoints + " j=" + j);

        for(int k=0;k<numPoints;k++){
            area = area + (eastings.get(j) + eastings.get(k)) *(northings.get(j) - northings.get(k));
            j = k;
        }

        area = area/2;

        //area above is point to point and does not include area of segment curve.
        //if area is positive, clockwise, if area is negative, counterclockwise

        Log.d(TAG, "solveAreaofPolygon: Area of Polygon: " + area);
        Log.d(TAG, "solveAreaofPolygon: Sum of Area to Right: " + areaCurveToRight);
        Log.d(TAG, "solveAreaofPolygon: Sum of Area to Left: " + areaCurveToLeft);


        if(area > 0){
            area = Math.abs(area) + areaCurveToRight - areaCurveToLeft;
        }else{
            area = Math.abs(area) - areaCurveToRight + areaCurveToLeft;
        }


        Log.d(TAG, "solveAreaofPolygon: Area: " + area);
        return area;
    }

    private void saveToDatabase(){
        Log.d(TAG, "saveToDatabase: Started...");
        for(int i=0; i<lstPointMapCheckResults.size(); i++) {
            PointMapCheck pointMapCheck = lstPointMapCheckResults.get(i);
            PointSurvey pointSurvey = new PointSurvey();

            pointSurvey.setPoint_no(pointMapCheck.getToPointNo());
            pointSurvey.setNorthing(pointMapCheck.getToPointNorth());
            pointSurvey.setEasting(pointMapCheck.getToPointEast());
            pointSurvey.setElevation(0);
            pointSurvey.setDescription(pointMapCheck.getPointDescription());
            pointSurvey.setPointType(1);

            lstPointSurvey.add(pointSurvey);
        }

        Log.d(TAG, "saveToDatabase: Size: " + lstPointSurvey.size());
        if (lstPointSurvey.size() > 0){
            BackgroundPointSurveyNewMultiple backgroundPointSurveyNew = new BackgroundPointSurveyNewMultiple(mContext, jobDatabaseName);
            backgroundPointSurveyNew.execute(lstPointSurvey);

        }

        finish();


    }

}
