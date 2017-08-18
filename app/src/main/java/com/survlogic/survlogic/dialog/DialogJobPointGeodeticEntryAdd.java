package com.survlogic.survlogic.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.survlogic.survlogic.R;
import com.survlogic.survlogic.utils.PreferenceLoaderHelper;
import com.survlogic.survlogic.utils.StringUtilityHelper;

import java.text.DecimalFormat;

/**
 * Created by chrisfillmore on 8/11/2017.
 */

public class DialogJobPointGeodeticEntryAdd extends DialogFragment {
    private static final String TAG = "DialogJobPointEntryAdd";

    private static DecimalFormat COORDINATE_FORMATTER;
    private static final int textValidationTwo = 1, textValidationThree = 2, textValidationFour = 4;

    private Context mContext;
    private SharedPreferences sharedPreferences;
    PreferenceLoaderHelper preferenceLoaderHelper;

    private EditText etLatDegree, etLatMinute, etLatSecond,
            etLongDegree, etLongMinute, etLongSecond,
            etHeightEllipse, etHeightOrtho;

    private Spinner sLatDirection, sLongDirection;

    private double latitudeValue = 0, longitudeValue = 0, heightEllipsoidValue = 0, heightOrthoValue = 0;

    public static DialogJobPointGeodeticEntryAdd newInstance(double latitude, double longitude, double heightEllipsoid, double heightOrtho) {
        Log.d(TAG, "newInstance: Starting...");
        DialogJobPointGeodeticEntryAdd frag = new DialogJobPointGeodeticEntryAdd();
        Bundle args = new Bundle();
        args.putDouble("latitude", latitude);
        args.putDouble("longitude", longitude);
        args.putDouble("ellipsoid", heightEllipsoid);
        args.putDouble("ortho", heightOrtho);

        frag.setArguments(args);
        return frag;

    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Log.d(TAG, "onCreateDialog: Starting...>");

        latitudeValue = getArguments().getDouble("latitude");
        longitudeValue = getArguments().getDouble("longitude");
        heightEllipsoidValue = getArguments().getDouble("ellipsoid");
        heightOrthoValue = getArguments().getDouble("ortho");

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.DialogPopupStyle);
        LayoutInflater inflater = getActivity().getLayoutInflater();

        View v = inflater.inflate(R.layout.dialog_job_point_geodetic_entry,null);
        builder.setView(v);

        builder.setPositiveButton(R.string.general_save,null);

        builder.setNegativeButton(R.string.general_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.create();
        return builder.show();
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: Started...");
        mContext = getActivity();

        AlertDialog alertDialog = (AlertDialog) getDialog();
        Button btnSave = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        initViewWidgets();
        setOnClickListeners();
        preferenceLoaderHelper = new PreferenceLoaderHelper(mContext);
        loadPreferences();

        populateItems();


    }

    private void loadPreferences(){
        Log.d(TAG, "loadPreferences: Started...");

        COORDINATE_FORMATTER = new DecimalFormat(preferenceLoaderHelper.getValueSystemDistancePrecisionDisplay());


    }

    private void initViewWidgets(){
        Log.d(TAG, "initViewWidgets: Started...");

        etLatDegree = (EditText) getDialog().findViewById(R.id.lat_degree);
        etLatMinute = (EditText) getDialog().findViewById(R.id.lat_min);
        etLatSecond = (EditText) getDialog().findViewById(R.id.lat_sec);

        etLongDegree = (EditText) getDialog().findViewById(R.id.long_degree);
        etLongMinute = (EditText) getDialog().findViewById(R.id.long_minute);
        etLongSecond = (EditText) getDialog().findViewById(R.id.long_second);

        etHeightEllipse = (EditText) getDialog().findViewById(R.id.height_ellipsoid);
        etHeightOrtho = (EditText) getDialog().findViewById(R.id.height_ortho);


    }

    private void setOnClickListeners(){
        Log.d(TAG, "setOnClickListeners: Starting...");
        etLatDegree.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                String myValue;

                if(!StringUtilityHelper.isStringNull(etLatDegree.getText().toString())){
                    myValue = etLatDegree.getText().toString();

                    if(myValue.contains("-")){
                        if(etLatDegree.getText().toString().length()==textValidationThree)
                        {
                            etLatMinute.requestFocus();
                        }
                    }else{
                        if(etLatDegree.getText().toString().length()==textValidationTwo)
                        {
                            etLatMinute.requestFocus();
                        }
                    }
                }

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        etLatMinute.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                if(etLatMinute.getText().toString().length()==textValidationTwo){
                    etLatSecond.requestFocus();
                }
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });


        etLongDegree.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                String myValue;

                if(!StringUtilityHelper.isStringNull(etLongDegree.getText().toString())){
                    myValue = etLongDegree.getText().toString();

                    if(myValue.contains("-")){
                        if(etLongDegree.getText().toString().length()==textValidationFour)
                        {
                            etLongMinute.requestFocus();
                        }
                    }else{
                        if(etLongDegree.getText().toString().length()==textValidationThree)
                        {
                            etLongMinute.requestFocus();
                        }
                    }
                }
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        etLongMinute.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                if(etLongMinute.getText().toString().length()==textValidationTwo)     //size as per your requirement
                {
                    etLongSecond.requestFocus();
                }
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });


    }


    private void populateItems(){
        Log.d(TAG, "populateItems: Starting...");

        if(latitudeValue !=0){
            Log.d(TAG, "Latitude Values: Started: " + latitudeValue);
            String degValue = Location.convert(latitudeValue,Location.FORMAT_DEGREES);
            String minuteValue = Location.convert(latitudeValue,Location.FORMAT_MINUTES);
            String secondValue = Location.convert(latitudeValue,Location.FORMAT_SECONDS);

            etLatDegree.setText(degValue);
            etLatMinute.setText(minuteValue);
            etLatSecond.setText(secondValue);

        }

        if(longitudeValue !=0){
            Log.d(TAG, "Longitude Values: Started:  " + longitudeValue);
        }

        if(heightEllipsoidValue !=0){
            Log.d(TAG, "Ellipsoid Values: Started:  " + heightEllipsoidValue);
            etHeightEllipse.setText(String.valueOf(heightEllipsoidValue));

        }

        if(heightOrthoValue !=0){
            Log.d(TAG, "Ortho Values: Started:  " + heightOrthoValue);
            etHeightOrtho.setText(String.valueOf(heightOrthoValue));


        }


    }


    private void showToast(String data, boolean shortTime) {
        Log.d(TAG, "showToast: Started...");
        if (shortTime) {
            Toast.makeText(getActivity(), data, Toast.LENGTH_SHORT).show();

        } else{
            Toast.makeText(getActivity(), data, Toast.LENGTH_LONG).show();

        }
    }






}
