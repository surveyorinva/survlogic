package com.survlogic.survlogic.activity;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.TextureView;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.survlogic.survlogic.ARvS.utils.CameraService;
import com.survlogic.survlogic.R;

public class CameraActivity extends AppCompatActivity {

    private static final String TAG = "CameraActivity";

    private Context mContext;

    //---------------------------------------------------------------------------------------------- Camera
    private CameraService cameraService;

    private RelativeLayout rlCameraView;
    private TextureView txvCameraView;
    private boolean isCameraInit = false;
    private boolean isCameraStarted = false;

    //---------------------------------------------------------------------------------------------- Settings
    //App Bar Widget
    private int currentMode;
    private static final int MODE_SURVEY = 0, MODE_STAKE = 1;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        Log.d(TAG, "Activity Started-------------------------------------------->");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gps_ar_v2);

        mContext = CameraActivity.this;

        initViewWidgets();
        startCameraService();



    }
    //---------------------------------------------------------------------------------------------- View Widgets
    private void initViewWidgets(){

        LinearLayout llAppBarSwitch = findViewById(R.id.ll_switch);
        llAppBarSwitch.setVisibility(View.VISIBLE);
        Switch swAppBarSwitch = findViewById(R.id.switch_value);
        TextView tvAppBarSwitchOff = findViewById(R.id.switch_lbl_off);
        TextView tvAppBarSwitchOn = findViewById(R.id.switch_lbl_on);

        currentMode = MODE_SURVEY;  // For use by Switch - Sets default view to the collection of survey data.

        tvAppBarSwitchOff.setText(getResources().getText(R.string.gps_app_switch_off));
        tvAppBarSwitchOn.setText(getResources().getText(R.string.gps_app_switch_on));

        swAppBarSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if(!isChecked){
                    currentMode = MODE_SURVEY;
                    rlCameraView.setVisibility(View.INVISIBLE);
                    isCameraStarted = cameraService.onStop();
                }else{
                    currentMode = MODE_STAKE;
                    rlCameraView.setVisibility(View.VISIBLE);
                    isCameraStarted = cameraService.onStart();

                }
            }
        });



    }

    //---------------------------------------------------------------------------------------------- Camera Service
    private void startCameraService(){
        Log.d(TAG, "startCameraService: Started");

        rlCameraView = findViewById(R.id.rl_camera_view);
        txvCameraView = findViewById(R.id.camera_view);

        cameraService = new CameraService(mContext,txvCameraView);
        isCameraInit = cameraService.init();
    }

    @Override
    protected void onStop() {
        super.onStop();

        if(isCameraStarted){
            cameraService.onStop();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if(isCameraStarted){
            cameraService.onResume();
        }

    }





}
