package com.survlogic.survlogic.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.survlogic.survlogic.R;
import com.survlogic.survlogic.interf.PointGeodeticEntryListener;
import com.survlogic.survlogic.utils.PreferenceLoaderHelper;
import com.survlogic.survlogic.utils.StringUtilityHelper;

import java.text.DecimalFormat;

/**
 * Created by chrisfillmore on 8/11/2017.
 */

public class DialogJobPointDescriptionEntryEdit extends DialogFragment {
    private static final String TAG = "DialogJobPointEntryAdd";

    private Context mContext;
    private SharedPreferences sharedPreferences;
    private PreferenceLoaderHelper preferenceLoaderHelper;

    private TextView tvAppBarTitle;
    private ImageButton ibAppBarBack;
    private EditText etPointDescription;

    private String mDescription;
    private boolean isEdit = false;

    private onUpdateDescriptionListener listener;

    public static DialogJobPointDescriptionEntryEdit newInstance(String pointDescription) {
        Log.d(TAG, "newInstance: Starting...");
        DialogJobPointDescriptionEntryEdit frag = new DialogJobPointDescriptionEntryEdit();
        Bundle args = new Bundle();
        args.putString("point_description", pointDescription);


        frag.setArguments(args);
        return frag;

    }

    public static DialogJobPointDescriptionEntryEdit newInstance(String pointDescription, boolean isEdit) {
        Log.d(TAG, "newInstance: Starting...");
        DialogJobPointDescriptionEntryEdit frag = new DialogJobPointDescriptionEntryEdit();
        Bundle args = new Bundle();
        args.putString("point_description", pointDescription);
        args.putBoolean("is_Edit", isEdit);

        frag.setArguments(args);
        return frag;

    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Log.d(TAG, "onCreateDialog: Starting...>");

        mDescription = getArguments().getString("point_description");
        isEdit = getArguments().getBoolean("is_Edit");

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.DialogPopupStyleExpolodingOut);
        LayoutInflater inflater = getActivity().getLayoutInflater();

        View v = inflater.inflate(R.layout.dialog_job_point_description_entry,null);
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

    public void setDialogListener(onUpdateDescriptionListener listener){
        this.listener = listener;
    }

    private void loadPreferences(){
        Log.d(TAG, "loadPreferences: Started...");

    }

    private void initViewWidgets(){
        Log.d(TAG, "initViewWidgets: Started...");

        tvAppBarTitle = (TextView) getDialog().findViewById(R.id.app_bar_title);
        ibAppBarBack = (ImageButton) getDialog().findViewById(R.id.button_back);

        tvAppBarTitle.setText(getResources().getString(R.string.dialog_job_point_description_title));
        ibAppBarBack.setVisibility(View.GONE);

        etPointDescription = (EditText) getDialog().findViewById(R.id.point_description);
        etPointDescription.setSelectAllOnFocus(true);

    }

    private void setOnClickListeners(){
        Log.d(TAG, "setOnClickListeners: Starting...");



    }

    private void setOnFocusChangeListeners(){
        Log.d(TAG, "setOnFocusChangeListeners: Started");



    }

    private void populateItems(){
        Log.d(TAG, "populateItems: Starting...");


        etPointDescription.setText(mDescription);

    }


    private boolean checkForm(){
        boolean isInError = false;

        String gDescription = etPointDescription.getText().toString();

        if(!StringUtilityHelper.isStringNull(gDescription) && !StringUtilityHelper.isStringNull(gDescription)){
            //Ready
            isInError = false;
            mDescription = gDescription;

        }else if(StringUtilityHelper.isStringNull(gDescription)){
            etPointDescription.setError(getResources().getString(R.string.dialog_job_point_description_error_description_not_entered));
            isInError = true;
        }

        return isInError;

    }


    private void submitForm(View v){
        Log.d(TAG, "submitForm: Started...");

        if(isEdit){
            listener.onUpdateDescriptionSubmit(mDescription);
        }

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

    public interface onUpdateDescriptionListener {
        void onUpdateDescriptionSubmit(String description);
    }




}
