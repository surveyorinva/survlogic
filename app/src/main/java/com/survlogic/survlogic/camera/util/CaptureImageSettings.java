package com.survlogic.survlogic.camera.util;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.Switch;

import com.survlogic.survlogic.R;
import com.survlogic.survlogic.utils.PreferenceLoaderHelper;

public class CaptureImageSettings extends AppCompatActivity {

    private Context mContext;

    //Preferences
    private PreferenceLoaderHelper preferenceLoaderHelper;

    //Widgets
    private Switch swLocationUseGPS;
    private boolean isGPSOn = false;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.camera_settings);

        mContext = CaptureImageSettings.this;
        preferenceLoaderHelper = new PreferenceLoaderHelper(mContext);

        initViewWidgets();
        initPreferences();

    }

    private void initPreferences(){
        isGPSOn = preferenceLoaderHelper.getCameraLocationUse();

        if(isGPSOn){
            swLocationUseGPS.setChecked(true);
        }else{
            swLocationUseGPS.setChecked(false);
        }

    }

    private void initViewWidgets(){
        ImageButton ibBack = findViewById(R.id.button_back);

        swLocationUseGPS = findViewById(R.id.switch_location_value);
        swLocationUseGPS.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if(!isChecked){
                    isGPSOn = false;
                    setTurnGPSOnOff();

                }else{
                    isGPSOn = true;
                    setTurnGPSOnOff();

                }
            }
        });
    }

    private void setTurnGPSOnOff(){
        preferenceLoaderHelper.setCameraLocationUse(isGPSOn,true);
    }



}
