package com.survlogic.survlogic.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.survlogic.survlogic.R;
import com.survlogic.survlogic.interf.PointGeodeticEntryListener;
import com.survlogic.survlogic.model.Point;
import com.survlogic.survlogic.utils.SurveyMathHelper;
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
    private PreferenceLoaderHelper preferenceLoaderHelper;

    private TextView tvAppBarTitle;
    private ImageButton ibAppBarBack;
    private EditText etLatDegree, etLatMinute, etLatSecond,
            etLongDegree, etLongMinute, etLongSecond,
            etHeightEllipse, etHeightOrtho;

    private Spinner sLatDirection, sLongDirection;

    private boolean isEdit = false;
    private double latitudeValue = 0, longitudeValue = 0, heightEllipsoidValue = 0, heightOrthoValue = 0;

    private onUpdateGeodeticCoordinatesListener listener;

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

    public static DialogJobPointGeodeticEntryAdd newInstance(double latitude, double longitude, double heightEllipsoid, double heightOrtho, boolean isEdit) {
        Log.d(TAG, "newInstance: Starting...");
        DialogJobPointGeodeticEntryAdd frag = new DialogJobPointGeodeticEntryAdd();
        Bundle args = new Bundle();
        args.putDouble("latitude", latitude);
        args.putDouble("longitude", longitude);
        args.putDouble("ellipsoid", heightEllipsoid);
        args.putDouble("ortho", heightOrtho);
        args.putBoolean("isEdit",isEdit);
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
        isEdit = getArguments().getBoolean("isEdit");

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.DialogPopupStyleExpolodingOut);
        LayoutInflater inflater = getActivity().getLayoutInflater();

        View v = inflater.inflate(R.layout.dialog_job_point_geodetic_entry,null);
        builder.setView(v);

        builder.setPositiveButton(R.string.general_save,null);

        builder.setNegativeButton(R.string.general_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        if(isEdit){
            builder.setNeutralButton(R.string.general_clear, null);
        }


        builder.create();
        return builder.show();
    }

    public void setDialogListener(onUpdateGeodeticCoordinatesListener listener){
        this.listener = listener;
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
                boolean results = checkFormPosition(v);

                if(results){
                    submitForm(v);
                }

            }
        });

        if (isEdit) {
            Button btnClear = alertDialog.getButton(AlertDialog.BUTTON_NEUTRAL);
            btnClear.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    clearResults();
                }
            });
        }

        initViewWidgets();
        setOnClickListeners();
        setOnFocusChangeListeners();

        preferenceLoaderHelper = new PreferenceLoaderHelper(mContext);
        loadPreferences();

        populateItems();


    }

    private void loadPreferences(){
        Log.d(TAG, "loadPreferences: Started...");

        COORDINATE_FORMATTER = new DecimalFormat(preferenceLoaderHelper.getValueSystemCoordinatesPrecisionDisplay());


    }

    private void initViewWidgets(){
        Log.d(TAG, "initViewWidgets: Started...");

        tvAppBarTitle = (TextView) getDialog().findViewById(R.id.app_bar_title);
        ibAppBarBack = (ImageButton) getDialog().findViewById(R.id.button_back);

        tvAppBarTitle.setText(getResources().getString(R.string.dialog_job_point_geodetic_app_title));
        ibAppBarBack.setVisibility(View.GONE);

        etLatDegree = (EditText) getDialog().findViewById(R.id.lat_degree);
        etLatDegree.setSelectAllOnFocus(true);

        etLatMinute = (EditText) getDialog().findViewById(R.id.lat_min);
        etLatMinute.setSelectAllOnFocus(true);

        etLatSecond = (EditText) getDialog().findViewById(R.id.lat_sec);
        etLatSecond.setSelectAllOnFocus(true);

        etLongDegree = (EditText) getDialog().findViewById(R.id.long_degree);
        etLongDegree.setSelectAllOnFocus(true);

        etLongMinute = (EditText) getDialog().findViewById(R.id.long_minute);
        etLongMinute.setSelectAllOnFocus(true);

        etLongSecond = (EditText) getDialog().findViewById(R.id.long_second);
        etLongSecond.setSelectAllOnFocus(true);

        etHeightEllipse = (EditText) getDialog().findViewById(R.id.height_ellipsoid);
        etHeightEllipse.setSelectAllOnFocus(true);

        etHeightOrtho = (EditText) getDialog().findViewById(R.id.height_ortho);
        etHeightOrtho.setSelectAllOnFocus(true);

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

    private void setOnFocusChangeListeners(){
        Log.d(TAG, "setOnFocusChangeListeners: Started");

        etLatDegree.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(!hasFocus){
                    try{
                        String angle = null;
                        angle = etLatDegree.getText().toString();

                        if(StringUtilityHelper.isStringNull(angle)){
                            etLatDegree.setText("00");
                        }

                    }catch(NumberFormatException ex){
                        showToast("Error.  Check Number Format", true);

                    }
                }
            }
        });

        etLatMinute.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(!hasFocus){
                    try{
                        String angle = null;
                        angle = etLatMinute.getText().toString();

                        if(StringUtilityHelper.isStringNull(angle)){
                            etLatMinute.setText("00");
                        }

                    }catch(NumberFormatException ex){
                        showToast("Error.  Check Number Format", true);

                    }
                }
            }
        });

        etLatSecond.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(!hasFocus){
                    try{
                        String angle = null;
                        angle = etLatSecond.getText().toString();

                        if(StringUtilityHelper.isStringNull(angle)){
                            etLatSecond.setText("00.000");
                        }

                    }catch(NumberFormatException ex){
                        showToast("Error.  Check Number Format", true);

                    }
                }
            }
        });

        etLongDegree.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(!hasFocus){
                    try{
                        String angle = null;
                        angle = etLongDegree.getText().toString();

                        if(StringUtilityHelper.isStringNull(angle)){
                            etLongDegree.setText("00");
                        }

                    }catch(NumberFormatException ex){
                        showToast("Error.  Check Number Format", true);

                    }
                }
            }
        });

        etLongMinute.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(!hasFocus){
                    try{
                        String angle = null;
                        angle = etLongMinute.getText().toString();

                        if(StringUtilityHelper.isStringNull(angle)){
                            etLongMinute.setText("00");
                        }

                    }catch(NumberFormatException ex){
                        showToast("Error.  Check Number Format", true);

                    }
                }
            }
        });

        etLongSecond.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(!hasFocus){
                    try{
                        String angle = null;
                        angle = etLongSecond.getText().toString();

                        if(StringUtilityHelper.isStringNull(angle)){
                            etLongSecond.setText("00.000");                      }

                    }catch(NumberFormatException ex){
                        showToast("Error.  Check Number Format", true);

                    }
                }
            }
        });

        etHeightEllipse.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(!hasFocus){
                    try{
                        String angle = null;
                        angle = etHeightEllipse.getText().toString();

                        if(StringUtilityHelper.isStringNull(angle)){
                            etHeightEllipse.setText("00.00");
                        }

                    }catch(NumberFormatException ex){
                        showToast("Error.  Check Number Format", true);

                    }
                }
            }
        });

        etHeightOrtho.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(!hasFocus){
                    try{
                        String angle = null;
                        angle = etHeightOrtho.getText().toString();

                        if(StringUtilityHelper.isStringNull(angle)){
                            etHeightOrtho.setText("00.00");
                        }

                    }catch(NumberFormatException ex){
                        showToast("Error.  Check Number Format", true);

                    }
                }
            }
        });
    }

    private void populateItems(){
        Log.d(TAG, "populateItems: Starting...");


        if(latitudeValue !=0){
            Log.d(TAG, "Latitude Values: Started: " + latitudeValue);
            String degValue = String.valueOf(SurveyMathHelper.convertDECToDegree(latitudeValue));
            String minuteValue = String.valueOf(SurveyMathHelper.convertDECToMinute(latitudeValue));
            String secondValue = String.valueOf(StringUtilityHelper.createUSGeodeticDecimalFormat().format(SurveyMathHelper.convertDECToSeconds(latitudeValue)));

            etLatDegree.setText(degValue);
            etLatMinute.setText(minuteValue);
            etLatSecond.setText(secondValue);


        }

        if(longitudeValue !=0){
            Log.d(TAG, "Longitude Values: Started:  " + longitudeValue);
            String degValue = String.valueOf(SurveyMathHelper.convertDECToDegree(longitudeValue));
            String minuteValue = String.valueOf(SurveyMathHelper.convertDECToMinute(longitudeValue));
            String secondValue = String.valueOf(StringUtilityHelper.createUSGeodeticDecimalFormat().format(SurveyMathHelper.convertDECToSeconds(longitudeValue)));

            etLongDegree.setText(degValue);
            etLongMinute.setText(minuteValue);
            etLongSecond.setText(secondValue);
        }

        if(heightEllipsoidValue !=0){
            Log.d(TAG, "Ellipsoid Values: Started:  " + heightEllipsoidValue);
            etHeightEllipse.setText(String.valueOf(COORDINATE_FORMATTER.format(heightEllipsoidValue)));

        }

        if(heightOrthoValue !=0){
            Log.d(TAG, "Ortho Values: Started:  " + heightOrthoValue);
            etHeightOrtho.setText(String.valueOf(COORDINATE_FORMATTER.format(heightOrthoValue)));


        }


    }


    private boolean checkFormPosition(View v) {
        Log.d(TAG, "submitForm: Starting");
        boolean results = false;

        String latDegree = etLatDegree.getText().toString();
        String latMinute = etLatMinute.getText().toString();
        String latSecond = etLatSecond.getText().toString();

        String longDegree = etLongDegree.getText().toString();
        String longMinute = etLongMinute.getText().toString();
        String longSecond = etLongSecond.getText().toString();

        if(!StringUtilityHelper.isStringNull(latDegree)) {
            Log.d(TAG, "submitForm: Latitude has been entered correctly, checking Longitude");
            if (StringUtilityHelper.isStringNull(longDegree)) {
                showToast(getString(R.string.dialog_job_point_geodetic_lat_missing_long),true);
                return false;
            }
            results = true;
        }

        if(!StringUtilityHelper.isStringNull(longDegree)) {
            Log.d(TAG, "submitForm: Longitude has been entered correctly, checking Latitude");
            if (StringUtilityHelper.isStringNull(latDegree)) {
                showToast(getString(R.string.dialog_job_point_geodetic_long_missing_lat),true);
                return false;
            }
            results = true;
        }

        if(!StringUtilityHelper.isStringNull(latDegree)) {
            Log.d(TAG, "submitForm: Latitude has been entered correctly, checking Min/Sec");
            if (StringUtilityHelper.isStringNull(latMinute) || StringUtilityHelper.isStringNull(latSecond)) {
                showToast(getString(R.string.dialog_job_point_geodetic_lat_missing_min_sec),true);
                return false;
            }
            results = true;
        }

        if(!StringUtilityHelper.isStringNull(longDegree)) {
            Log.d(TAG, "submitForm: Longitude has been entered correctly, checking Min/Sec");
            if (StringUtilityHelper.isStringNull(longMinute) || StringUtilityHelper.isStringNull(longSecond)) {
                showToast(getString(R.string.dialog_job_point_geodetic_long_missing_min_sec),true);
                return false;
            }
            results = true;
        }
        return results;
    }


    private void submitForm(View v){
        Log.d(TAG, "submitForm: Started...");

        boolean results = false;
        double latitudeDEC = 0, longitudeDEC = 0, heightEllipsoid = 0, heightOrtho = 0;

        String latDegreeIn = etLatDegree.getText().toString();
        String latMinuteIn = etLatMinute.getText().toString();
        String latSecondIn = etLatSecond.getText().toString();

        String longDegreeIn = etLongDegree.getText().toString();
        String longMinuteIn = etLongMinute.getText().toString();
        String longSecondIn = etLongSecond.getText().toString();

        String heightEllipsoidIn = etHeightEllipse.getText().toString();
        String heightOrthoIn = etHeightOrtho.getText().toString();


        if(!StringUtilityHelper.isStringNull(latDegreeIn)) {
            latitudeDEC = SurveyMathHelper.convertPartsToDEC(latDegreeIn, latMinuteIn, latSecondIn);
            Log.d(TAG, "Latitude to DEC: " + latitudeDEC);

            longitudeDEC = SurveyMathHelper.convertPartsToDEC(longDegreeIn, longMinuteIn, longSecondIn);
            Log.d(TAG, "Longitude D-M-S: " + longDegreeIn + ":" + longMinuteIn + ":" + longSecondIn );
            Log.d(TAG, "Longitude to DEC: " + longitudeDEC);

        }

        if(!StringUtilityHelper.isStringNull(heightEllipsoidIn)){
            heightEllipsoid = Double.parseDouble(heightEllipsoidIn);

        }

        if(!StringUtilityHelper.isStringNull(heightOrthoIn)){
            //Ask if user wants to use heightOrtho as Point Elevation
            heightOrtho = Double.parseDouble(heightOrthoIn);
        }


        if (!isEdit) {
            PointGeodeticEntryListener listener = (PointGeodeticEntryListener) getActivity();
            listener.onWorldReturnValues(latitudeDEC, longitudeDEC, heightEllipsoid, heightOrtho);

        } else {
            listener.onUpdateGeodeticCoordinatesSubmit(latitudeDEC, longitudeDEC, heightEllipsoid, heightOrtho);

        }

        dismiss();

    }

    private void clearResults(){
        Log.d(TAG, "clearResults: Started");



    }

    private void showToast(String data, boolean shortTime) {
        Log.d(TAG, "showToast: Started...");
        if (shortTime) {
            Toast.makeText(getActivity(), data, Toast.LENGTH_SHORT).show();

        } else{
            Toast.makeText(getActivity(), data, Toast.LENGTH_LONG).show();

        }
    }

    public interface onUpdateGeodeticCoordinatesListener {
        void onUpdateGeodeticCoordinatesSubmit(double latOut, double longOut, double heightEllipsOut, double heightOrthoOut);
    }





}
