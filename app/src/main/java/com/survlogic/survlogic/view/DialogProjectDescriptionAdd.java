package com.survlogic.survlogic.view;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.survlogic.survlogic.R;

/**
 * Created by chrisfillmore on 7/22/2017.
 */

public class DialogProjectDescriptionAdd extends DialogFragment {
    private static final String TAG = "DialogProjectDescriptionAdd";
    private Context mContext;

    private int project_id;
    private String mDescription;

    EditText etDescription;

    public static DialogProjectDescriptionAdd newInstance(Integer mProjectId, String description) {

        DialogProjectDescriptionAdd frag = new DialogProjectDescriptionAdd();
        Bundle args = new Bundle();

        args.putInt("project_id", mProjectId);
        args.putString("description", description);

        frag.setArguments(args);
        return frag;

    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        project_id = getArguments().getInt("project_id");
        mDescription = getArguments().getString("description");

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();

        View v = inflater.inflate(R.layout.dialog_project_note,null);
        builder.setView(v);

        builder.setPositiveButton(R.string.general_post,null);

        builder.create();
        return builder.show();


    }

    @Override
    public void onResume() {
        super.onResume();

        mContext = getActivity();
        AlertDialog alertDialog = (AlertDialog) getDialog();

        etDescription = (EditText) getDialog().findViewById(R.id.description);

        Button btnSave = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitForm(v);
            }
        });

    }

    private void submitForm(View v) {

    }





}
