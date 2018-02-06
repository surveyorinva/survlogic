package com.survlogic.survlogic.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.animation.BounceInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.survlogic.survlogic.R;

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
import org.w3c.dom.Text;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created by chrisfillmore on 1/11/2018.
 */

public class TestActivity extends AppCompatActivity {
    private static final String TAG = "Started";
    private Context mContext;
    private EditText latIn, longIn;
    private TextView tvnorthingOut, tveastingOut, tvTestAnimation;
    private Button btSolve;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.testactivity);
        mContext = TestActivity.this;

        initViewWidgets();
    }

    private void initViewWidgets(){
        Log.d(TAG, "initViewWidgets: ");
        latIn = (EditText) findViewById(R.id.latitude_test);
        longIn = (EditText) findViewById(R.id.longitude_test);

        tvnorthingOut = (TextView) findViewById(R.id.northing_test);
        tveastingOut = (TextView) findViewById(R.id.easting_test);
        tvTestAnimation = (TextView) findViewById(R.id.test_animation);
        animateViewSlideVisible(tvTestAnimation,200);

        btSolve = (Button) findViewById(R.id.button_solve_test);
        btSolve.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                solve();

            }
        });
    }
    
    private void solve(){
        Log.d(TAG, "solveFromGeodetic: ");
        try{
            solveFromGeodetic();
        }catch (Exception e){
            Log.d(TAG, "solveFromGeodetic: Error");
        }

        animateViewSlideVisible(tvTestAnimation, 300);
    }
    
    private void solveFromGeodetic()  throws IllegalCoordinateException, CoordinateOperationException, CRSException {
        Log.d(TAG, "solveFromGeodetic: ");
        double[] coord = new double[2];
        coord[0] = -77.548058d; //longitude
        coord[1] = 38.284642d; // latitude

        latIn.setText(String.valueOf(coord[1]));
        longIn.setText(String.valueOf(coord[0]));

        // Create a new CRSFactory, a necessary element to create a CRS without defining one by one all its components
        CRSFactory cRSFactory = new CRSFactory();
        RegistryManager registryManager = cRSFactory.getRegistryManager();

        registryManager.addRegistry(new EPSGRegistry());
        CoordinateReferenceSystem sourceCRS = cRSFactory.getCRS("EPSG:4269");  //4326 WGS  4269 NAD83
        CoordinateReferenceSystem targetCRS = cRSFactory.getCRS("EPSG:2283");  //2283  - NAD83(2011)  2924- NAD83(HARN) - VA North
        GeodeticCRS sourceGCRS = (GeodeticCRS) sourceCRS;
        ProjectedCRS targetGCRS = (ProjectedCRS) targetCRS;

        Set<CoordinateOperation> set = CoordinateOperationFactory.createCoordinateOperations(sourceGCRS, targetGCRS);
        List<CoordinateOperation> coordOps = new ArrayList<>();

        coordOps.addAll(set);

        Log.d(TAG, "solveFromGeodetic: Size: " + coordOps.size());
        if (coordOps.size() != 0) {

            // Test each transformation method (generally, only one method is available)
            for (CoordinateOperation op : coordOps) {
                Log.d(TAG, "solveFromGeodetic: In");
                // Transform coord using the op CoordinateOperation from crs1 to crs2
                DecimalFormat precision = new DecimalFormat("0.00");

                double[] dd = op.transform(coord);
                tveastingOut.setText(new BigDecimal(dd[0]).toPlainString());

                //tveastingOut.setText(String.valueOf(dd[0]));
                tvnorthingOut.setText(String.valueOf(dd[1]));


            }
        }

    }

    private void animateViewSlideVisible(final TextView view, long duration){
        if(view.getVisibility() == View.GONE){
            view.setVisibility(View.VISIBLE);
            view.setAlpha(0.0f);

            view.animate()
                    .translationY(view.getHeight())
                    .alpha(1.0f)
                    .setListener(null);
        }else{
            view.animate()
                    .translationY(0)
                    .alpha(0.0f)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);
                            view.setVisibility(View.GONE);
                        }
                    });
        }


    }


    private void animateViewSlideVisibleOriginal(final View view, boolean toShowView, long duration){
        if(toShowView){
            view.setVisibility(View.VISIBLE);
            view.setAlpha(0.0f);

            view.animate()
                    .setDuration(duration)
                    .setInterpolator(new BounceInterpolator())
                    .translationY(view.getHeight())
                    .alpha(1.0f)
                    .setListener(null);
        }else{
            view.animate()
                    .setDuration(duration)
                    .setInterpolator(new BounceInterpolator())
                    .translationY(0)
                    .alpha(0.0f)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);
                            view.setVisibility(View.GONE);
                        }
                    });
        }

    }




}
