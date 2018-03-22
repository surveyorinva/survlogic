package com.survlogic.survlogic.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.survlogic.survlogic.R;
import com.survlogic.survlogic.interf.DialogJobPointViewInterface;
import com.survlogic.survlogic.interf.PointGeodeticEntryListener;
import com.survlogic.survlogic.model.Point;
import com.survlogic.survlogic.utils.PreferenceLoaderHelper;
import com.survlogic.survlogic.utils.StringUtilityHelper;

import java.text.DecimalFormat;

/**
 * Created by chrisfillmore on 8/11/2017.
 */

public class DialogJobPointPlanarEntryAdd extends DialogFragment {
    private static final String TAG = "DialogJobPointEntryAdd";

    private static DecimalFormat COORDINATE_FORMATTER;

    private Context mContext;
    private SharedPreferences sharedPreferences;
    private PreferenceLoaderHelper preferenceLoaderHelper;

    private TextView tvAppBarTitle;
    private ImageButton ibAppBarBack;
    private EditText etPlanarNorth, etPlanarEast, etPlanarElevation;

    private double planarNorthIn = 0, planarEastIn = 0, planarElevationIn = 0 ;
    private double planarNorthOut = 0, planarEastOut = 0, planarElevationOut = 0 ;
    private boolean isEdit = false;

    private onUpdatePlanarCoordinatesListener listener;

    public static DialogJobPointPlanarEntryAdd newInstance(double planarNorth, double planarEast, double planarElevation ) {
        Log.d(TAG, "newInstance: Starting...");
        DialogJobPointPlanarEntryAdd frag = new DialogJobPointPlanarEntryAdd();
        Bundle args = new Bundle();
        args.putDouble("planar_north", planarNorth);
        args.putDouble("planar_east", planarEast);
        args.putDouble("planar_elevation", planarElevation);

        frag.setArguments(args);
        return frag;

    }

    public static DialogJobPointPlanarEntryAdd newInstance(double planarNorth, double planarEast, double planarElevation, boolean isEdit ) {
        Log.d(TAG, "newInstance: Starting...");
        DialogJobPointPlanarEntryAdd frag = new DialogJobPointPlanarEntryAdd();
        Bundle args = new Bundle();
        args.putDouble("planar_north", planarNorth);
        args.putDouble("planar_east", planarEast);
        args.putDouble("planar_elevation", planarElevation);
        args.putBoolean("is_edit", isEdit);

        frag.setArguments(args);
        return frag;

    }



    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Log.d(TAG, "onCreateDialog: Starting...>");

        planarNorthIn = getArguments().getDouble("planar_north");
        planarEastIn = getArguments().getDouble("planar_east");
        planarElevationIn = getArguments().getDouble("planar_elevation");
        isEdit = getArguments().getBoolean("is_edit");

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.DialogPopupStyleExpolodingOut);
        LayoutInflater inflater = getActivity().getLayoutInflater();

        View v = inflater.inflate(R.layout.dialog_job_point_planar_entry,null);
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


    public void setDialogListener(onUpdatePlanarCoordinatesListener listener){
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
                boolean isError = checkForm();
                if(!isError){
                    submitForm(v);
                }

            }
        });

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

        tvAppBarTitle.setText(getResources().getString(R.string.dialog_job_point_planar_app_title));
        ibAppBarBack.setVisibility(View.GONE);

        etPlanarNorth = (EditText) getDialog().findViewById(R.id.planar_north);
        etPlanarNorth.setSelectAllOnFocus(true);

        etPlanarEast = (EditText) getDialog().findViewById(R.id.planar_east);
        etPlanarNorth.setSelectAllOnFocus(true);

        etPlanarElevation = (EditText) getDialog().findViewById(R.id.planar_elevation);
        etPlanarElevation.setSelectAllOnFocus(true);

    }

    private void setOnClickListeners(){
        Log.d(TAG, "setOnClickListeners: Starting...");



    }

    private void setOnFocusChangeListeners(){
        Log.d(TAG, "setOnFocusChangeListeners: Started");

        etPlanarNorth.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(!hasFocus){
                    try{
                        String value = null;
                        value = etPlanarNorth.getText().toString();

                        if(!StringUtilityHelper.isStringNull(value)){
                            double mValue = Double.parseDouble(value);

                            etPlanarNorth.setText(COORDINATE_FORMATTER.format(mValue));

                        }

                    }catch(NumberFormatException ex){
                        showToast("Error.  Check Number Format", true);

                    }
                }
            }
        });

        etPlanarEast.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(!hasFocus){
                    try{
                        String value = null;
                        value = etPlanarEast.getText().toString();

                        if(!StringUtilityHelper.isStringNull(value)){
                            double mValue = Double.parseDouble(value);

                            etPlanarEast.setText(COORDINATE_FORMATTER.format(mValue));

                        }

                    }catch(NumberFormatException ex){
                        showToast("Error.  Check Number Format", true);

                    }
                }
            }
        });

        etPlanarElevation.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(!hasFocus){
                    try{
                        String value = null;
                        value = etPlanarElevation.getText().toString();

                        if(!StringUtilityHelper.isStringNull(value)){
                            double mValue = Double.parseDouble(value);

                            etPlanarElevation.setText(COORDINATE_FORMATTER.format(mValue));

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


        if(planarNorthIn != 0){
            etPlanarNorth.setText(COORDINATE_FORMATTER.format(planarNorthIn));
        }else{
            etPlanarNorth.setText((COORDINATE_FORMATTER.format(0)));
        }

        if(planarEastIn !=0){
            etPlanarEast.setText(COORDINATE_FORMATTER.format(planarEastIn));
        }else{
            etPlanarEast.setText((COORDINATE_FORMATTER.format(0)));
        }

        if(planarElevationIn !=0){
            etPlanarElevation.setText(COORDINATE_FORMATTER.format(planarElevationIn));
        }else{
            etPlanarElevation.setText((COORDINATE_FORMATTER.format(0)));
        }

    }


    private boolean checkForm(){
        boolean isInError = false;

        String gNorth = etPlanarNorth.getText().toString();
        String gEast = etPlanarEast.getText().toString();
        String gElevation = etPlanarElevation.getText().toString();

        if(!StringUtilityHelper.isStringNull(gNorth) && !StringUtilityHelper.isStringNull(gEast) && !StringUtilityHelper.isStringNull(gElevation)){
            //Ready
            isInError = false;
            planarNorthOut = Double.parseDouble(gNorth);
            planarEastOut = Double.parseDouble(gEast);
            planarElevationOut = Double.parseDouble(gElevation);

            Log.d(TAG, "checkForm: Elevation: " + planarElevationOut);

        }else if(StringUtilityHelper.isStringNull(gNorth)){
            etPlanarNorth.setError(getResources().getString(R.string.dialog_job_point_planar_error_north_not_entered));
            isInError = true;
        }else if(StringUtilityHelper.isStringNull(gEast)){
            etPlanarEast.setError(getResources().getString(R.string.dialog_job_point_planar_error_east_not_entered));
            isInError = true;
        }else if(StringUtilityHelper.isStringNull(gElevation)) {
            etPlanarElevation.setError(getResources().getString(R.string.dialog_job_point_planar_error_elevation_not_entered));
            isInError = true;
        }

        return isInError;

    }


    private void submitForm(View v){
        Log.d(TAG, "submitForm: Started...");

        Point pointOut = new Point();
        pointOut.setNorthing(planarNorthOut);
        pointOut.setEasting(planarEastOut);
        pointOut.setElevation(planarElevationOut);

        listener.onUpdatePlanarCoordinatesSubmit(pointOut);
        dismiss();

    }

    private void showToast(String data, boolean shortTime) {
        Log.d(TAG, "showToast: Started...");
        if (shortTime) {
            Toast.makeText(getActivity(), data, Toast.LENGTH_SHORT).show();

        } else{
            Toast.makeText(getActivity(), data, Toast.LENGTH_LONG).show();

        }
    }



    public interface onUpdatePlanarCoordinatesListener {
        void onUpdatePlanarCoordinatesSubmit(Point pointOut);
    }

}
