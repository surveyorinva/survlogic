package com.survlogic.survlogic.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.survlogic.survlogic.R;

/**
 * Created by chrisfillmore on 7/21/2017.
 */

public class SplashActivity extends AppCompatActivity {

    private static final String TAG = "SplashActivity";
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    private boolean bolFirstRun; //First time app is run to check permissions

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: Started---------------------------->");

        checkPreferences();


    }


    private void checkPreferences(){

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        bolFirstRun = sharedPreferences.getBoolean(getString(R.string.pref_first_run),true);

        if(bolFirstRun){
            editor = sharedPreferences.edit();
            editor.putBoolean(getString(R.string.pref_first_run),false);
            editor.apply();

            startWelcomeActivity();

        }else{
            startHomeActivity();
        }


    }
    private void startWelcomeActivity(){
        Intent intent = new Intent(this, WelcomeActivity.class);
        startActivity(intent);
        finish();
    }

    private void startHomeActivity(){
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
