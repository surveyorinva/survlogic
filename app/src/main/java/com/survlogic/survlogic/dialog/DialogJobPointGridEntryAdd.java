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
import com.survlogic.survlogic.utils.PreferenceLoaderHelper;
import com.survlogic.survlogic.utils.StringUtilityHelper;
import com.survlogic.survlogic.utils.SurveyMathHelper;

import java.text.DecimalFormat;

/**
 * Created by chrisfillmore on 8/11/2017.
 */

public class DialogJobPointGridEntryAdd extends DialogFragment {
    private static final String TAG = "DialogJobPointEntryAdd";

    private static DecimalFormat COORDINATE_FORMATTER;

    private Context mContext;
    private SharedPreferences sharedPreferences;
    private PreferenceLoaderHelper preferenceLoaderHelper;

    private TextView tvAppBarTitle;
    private ImageButton ibAppBarBack;
    private EditText etGridNorth, etGridEast;

    private double gridNorth = 0, gridEast = 0;

    public static DialogJobPointGridEntryAdd newInstance(double gridNorth, double gridEast) {
        Log.d(TAG, "newInstance: Starting...");
        DialogJobPointGridEntryAdd frag = new DialogJobPointGridEntryAdd();
        Bundle args = new Bundle();
        args.putDouble("grid_north", gridNorth);
        args.putDouble("grid_east", gridEast);

        frag.setArguments(args);
        return frag;

    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Log.d(TAG, "onCreateDialog: Starting...>");

        gridNorth = getArguments().getDouble("grid_north");
        gridEast = getArguments().getDouble("grid_east");

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.DialogPopupStyleExpolodingOut);
        LayoutInflater inflater = getActivity().getLayoutInflater();

        View v = inflater.inflate(R.layout.dialog_job_point_grid_entry,null);
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

        tvAppBarTitle.setText(getResources().getString(R.string.dialog_job_point_geodetic_app_title));
        ibAppBarBack.setVisibility(View.GONE);

        etGridNorth = (EditText) getDialog().findViewById(R.id.grid_north);
        etGridNorth.setSelectAllOnFocus(true);

        etGridEast = (EditText) getDialog().findViewById(R.id.grid_east);
        etGridEast.setSelectAllOnFocus(true);


    }

    private void setOnClickListeners(){
        Log.d(TAG, "setOnClickListeners: Starting...");



    }

    private void setOnFocusChangeListeners(){
        Log.d(TAG, "setOnFocusChangeListeners: Started");

        etGridNorth.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(!hasFocus){
                    try{
                        String value = null;
                        value = etGridNorth.getText().toString();

                        if(!StringUtilityHelper.isStringNull(value)){
                            double mValue = Double.parseDouble(value);

                            etGridNorth.setText(COORDINATE_FORMATTER.format(mValue));

                        }

                    }catch(NumberFormatException ex){
                        showToast("Error.  Check Number Format", true);

                    }
                }
            }
        });

        etGridEast.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(!hasFocus){
                    try{
                        String value = null;
                        value = etGridEast.getText().toString();

                        if(!StringUtilityHelper.isStringNull(value)){
                            double mValue = Double.parseDouble(value);

                            etGridEast.setText(COORDINATE_FORMATTER.format(mValue));

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


        if(gridNorth !=0){
            etGridNorth.setText(COORDINATE_FORMATTER.format(gridNorth));
        }

        if(gridEast !=0){
            etGridEast.setText(COORDINATE_FORMATTER.format(gridEast));
        }

    }


    private boolean checkForm(){
        boolean isInError = false;

        String gNorth = etGridNorth.getText().toString();
        String gEast = etGridEast.getText().toString();

        if(!StringUtilityHelper.isStringNull(gNorth) && !StringUtilityHelper.isStringNull(gEast)){
            //Ready
            isInError = false;
            gridNorth = Double.parseDouble(gNorth);
            gridEast = Double.parseDouble(gEast);

        }else if(StringUtilityHelper.isStringNull(gNorth)){
            etGridNorth.setError(getResources().getString(R.string.dialog_job_point_grid_error_north_not_entered));
            isInError = true;
        }else if(StringUtilityHelper.isStringNull(gEast)){
            etGridEast.setError(getResources().getString(R.string.dialog_job_point_grid_error_east_not_entered));
            isInError = true;
        }

        return isInError;

    }


    private void submitForm(View v){
        Log.d(TAG, "submitForm: Started...");

        PointGeodeticEntryListener listener = (PointGeodeticEntryListener) getActivity();
        listener.onGridReturnValues(gridNorth, gridEast);

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






}
