package com.survlogic.survlogic.utils;

import android.content.Context;
import android.util.Log;

import com.survlogic.survlogic.model.Point;
import com.survlogic.survlogic.model.PointGeodetic;
import com.survlogic.survlogic.model.PointSurvey;

import org.cts.CRSFactory;
import org.cts.IllegalCoordinateException;
import org.cts.crs.CRSException;
import org.cts.crs.CoordinateReferenceSystem;
import org.cts.crs.GeodeticCRS;
import org.cts.crs.ProjectedCRS;
import org.cts.op.CoordinateOperation;
import org.cts.op.CoordinateOperationException;
import org.cts.op.CoordinateOperationFactory;
import org.cts.registry.EPSGRegistry;
import org.cts.registry.RegistryManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created by chrisfillmore on 2/8/2018.
 */

public class SurveyProjectionHelper {
    private static final String TAG = "SurveyProjectionHelper";
    private Context mContext;

    private String[] separatedProjectionValue, separatedZoneValue;

    private static final int METERS = 3, INTFEET = 2, USFEET = 1;
    private String projection_IN_EPSG;
    private String projection_OUT_EPSG;
    private String zone_OUT_EPSG;
    private boolean isProjectionOutAZone = false;
    private int units;
    private static final int NONE = 0, COMPUTED = 1, PROJECTED = 5;


    public SurveyProjectionHelper(Context context) {
        this.mContext = context;

    }

    public void setConfig(String projectionString, String zoneString) {
        Log.d(TAG, "setConfig: Started");
        Log.d(TAG, "setConfig: String Projection: " + projectionString);
        Log.d(TAG, "setConfig: String Zone: " + zoneString);

        separatedProjectionValue = projectionString.split(",");
        separatedZoneValue = zoneString.split(",");

        //-Name of Datum,Country of Origin, Required GeodeticCRS, Zone (Y/N), Zone Designation, Returned Units (3=Meters, 2=Int. Feet, 1=USFeet),this EPSG -->
        projection_IN_EPSG = separatedProjectionValue[2];
        projection_OUT_EPSG = separatedProjectionValue[6];

        isProjectionOutAZone = Boolean.parseBoolean(separatedProjectionValue[3]);
        if(isProjectionOutAZone){
            projection_OUT_EPSG = separatedZoneValue[3];

        }

    }


    public String getProjectionName(){
        return separatedProjectionValue[0];
    }

    public String getZoneName(){
        return separatedZoneValue[0];
    }


    private Point solveGeodeticPointFromGridPoint(double gridNorthIN, double gridEastIN)throws IllegalCoordinateException, CoordinateOperationException, CRSException{
        Log.d(TAG, "solveGeodeticPointFromGridPoint: Started");


        Point pointOut = new Point();

        double[] coord = new double[2];
        coord[0] = gridEastIN;
        coord[1] = gridNorthIN;

        System.out.println("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<Variables Used>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");

        Log.d(TAG, "solveGeodeticPointFromGridPoint: North: " + gridNorthIN);
        Log.d(TAG, "solveGeodeticPointFromGridPoint: East: " + gridEastIN);
        Log.d(TAG, "solveGeodeticPointFromGridPoint: EPSG_OUT: " + projection_OUT_EPSG);
        Log.d(TAG, "solveGeodeticPointFromGridPoint: EPSG_IN: " + projection_IN_EPSG);

        // Create a new CRSFactory, a necessary element to create a CRS without defining one by one all its components
        CRSFactory cRSFactory = new CRSFactory();
        RegistryManager registryManager = cRSFactory.getRegistryManager();

        registryManager.addRegistry(new EPSGRegistry());
        CoordinateReferenceSystem sourceCRS = cRSFactory.getCRS(projection_OUT_EPSG); // Grid
        CoordinateReferenceSystem targetCRS = cRSFactory.getCRS(projection_IN_EPSG);  //WGS

        ProjectedCRS sourceGCRS = (ProjectedCRS) sourceCRS;
        GeodeticCRS targetGCRS = (GeodeticCRS) targetCRS;

        Set<CoordinateOperation> set = CoordinateOperationFactory.createCoordinateOperations(sourceGCRS, targetGCRS);
        List<CoordinateOperation> coordOps = new ArrayList<>();

        coordOps.addAll(set);

        Log.d(TAG, "solveGeodeticPointFromGridPoint: Size: " + coordOps.size());
        if (coordOps.size() != 0) {

            // Test each transformation method (generally, only one method is available)
            for (CoordinateOperation op : coordOps) {
                Log.d(TAG, "solveGeodeticPointFromGridPoint: In");
                // Transform coord using the op CoordinateOperation from crs1 to crs2
                double[] dd = op.transform(coord);

                pointOut.setNorthing(dd[1]);
                pointOut.setEasting(dd[0]);

            }
        }


        return pointOut;
    }


    private Point solveGridPointFromGeodeticPoint(double latIN, double longIN)  throws IllegalCoordinateException, CoordinateOperationException, CRSException {
        Log.d(TAG, "solveGridPointFromGeodeticPoint: ");

        Point pointOut = new Point();

        double[] coord = new double[2];
        coord[0] = longIN; //longitude  -77.548058d
        coord[1] = latIN; // latitude 38.284642d

        System.out.println("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<Variables Used>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");

        Log.d(TAG, "solveGridPointFromGeodeticPoint: Lat: " + latIN);
        Log.d(TAG, "solveGridPointFromGeodeticPoint: Long: " + longIN);

        // Create a new CRSFactory, a necessary element to create a CRS without defining one by one all its components
        CRSFactory cRSFactory = new CRSFactory();
        RegistryManager registryManager = cRSFactory.getRegistryManager();

        registryManager.addRegistry(new EPSGRegistry());
        CoordinateReferenceSystem sourceCRS = cRSFactory.getCRS(projection_IN_EPSG);  //4326 WGS  4269 NAD83
        CoordinateReferenceSystem targetCRS = cRSFactory.getCRS(projection_OUT_EPSG);  //2283  - NAD83(2011)  2924- NAD83(HARN) - VA North

        GeodeticCRS sourceGCRS = (GeodeticCRS) sourceCRS;
        ProjectedCRS targetGCRS = (ProjectedCRS) targetCRS;

        Set<CoordinateOperation> set = CoordinateOperationFactory.createCoordinateOperations(sourceGCRS, targetGCRS);
        List<CoordinateOperation> coordOps = new ArrayList<>();

        coordOps.addAll(set);

        Log.d(TAG, "solveGridPointFromGeodeticPoint: Size: " + coordOps.size());
        if (coordOps.size() != 0) {

            // Test each transformation method (generally, only one method is available)
            for (CoordinateOperation op : coordOps) {
                Log.d(TAG, "solveGridPointFromGeodeticPoint: In");
                // Transform coord using the op CoordinateOperation from crs1 to crs2
                double[] dd = op.transform(coord);

                pointOut.setNorthing(dd[1]);
                pointOut.setEasting(dd[0]);

            }
        }

        return pointOut;

    }

    public Point calculateGridCoordinates(Point pointIn){
        Log.d(TAG, "calculateGridCoordinates: Started");
        Point pointReturn = new Point();

        try{
            pointReturn = solveGridPointFromGeodeticPoint(pointIn.getNorthing(), pointIn.getEasting());

        }catch (Exception e){
            Log.d(TAG, "calculateGridCoordinates: Error");
        }


        return pointReturn;


    }


    public Point calculateGeodeticCoordinates(Point pointIn){
        Log.d(TAG, "calculateGeodeticCoordinates: Started");

        Point pointReturn = new Point();

        try{
            pointReturn = solveGeodeticPointFromGridPoint(pointIn.getNorthing(), pointIn.getEasting());

        }catch (Exception e){
            Log.d(TAG, "calculateGridCoordinates: Error");
        }


        return pointReturn;



    }

    public ArrayList<PointSurvey> generateGridPoints(ArrayList<PointGeodetic> lstPointGeodetic) {
        Log.d(TAG, "generateGridPoints: Started");

        ArrayList<PointSurvey> lstPointGrid = new ArrayList<>();

        for (int i = 0; i < lstPointGeodetic.size(); i++) {

            PointGeodetic pointGeodetic = lstPointGeodetic.get(i);

            Point pointIn = new Point(pointGeodetic.getLatitude(), pointGeodetic.getLongitude());
            Point myProjectionPoint = calculateGridCoordinates(pointIn);


            PointSurvey gridSurvey = new PointSurvey();

            gridSurvey.setPoint_no(pointGeodetic.getPoint_no());
            gridSurvey.setDescription(pointGeodetic.getDescription());
            gridSurvey.setElevation(pointGeodetic.getElevation());
            gridSurvey.setPointType(PROJECTED);

            gridSurvey.setNorthing(myProjectionPoint.getNorthing());
            gridSurvey.setEasting(myProjectionPoint.getEasting());

            lstPointGrid.add(gridSurvey);
        }

        return lstPointGrid;
    }

}
