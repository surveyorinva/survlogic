package com.survlogic.survlogic.dialog;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.survlogic.survlogic.R;
import com.survlogic.survlogic.model.CurveSurvey;
import com.survlogic.survlogic.utils.SurveyMathHelper;
import com.survlogic.survlogic.utils.PreferenceLoaderHelper;

import java.text.DecimalFormat;

/**
 * Created by chrisfillmore on 7/22/2017.
 */

public class DialogToolsCurveSolve extends DialogFragment {
    private static final String TAG = "DialogProjectDescr";
    private Context mContext;

    private AlertDialog alertDialog;
    private CurveSurvey curveSurvey;
    private PreferenceLoaderHelper preferenceLoaderHelper;

    private static DecimalFormat COORDINATE_FORMATTER, DISTANCE_PRECISION_FORMATTER;

    private TextView tvDelta, tvRadius, tvLength, tvTangent, tvChordDistance;

    public static DialogToolsCurveSolve newInstance(CurveSurvey curveSurvey) {

        DialogToolsCurveSolve frag = new DialogToolsCurveSolve();
        Bundle args = new Bundle();

        args.putParcelable("list", curveSurvey);

        frag.setArguments(args);
        return frag;

    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        curveSurvey = getArguments().getParcelable("list");

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(),R.style.DialogPopupStyle);
        LayoutInflater inflater = getActivity().getLayoutInflater();

        View v = inflater.inflate(R.layout.dialog_tools_curve_solution,null);
        builder.setView(v);

        builder.setNegativeButton(R.string.general_close, new DialogInterface.OnClickListener() {
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

        mContext = getActivity();
        AlertDialog alertDialog = (AlertDialog) getDialog();

        preferenceLoaderHelper = new PreferenceLoaderHelper(mContext);
        loadPreferences();

        tvDelta = (TextView) getDialog().findViewById(R.id.curve_delta);
        tvRadius = (TextView) getDialog().findViewById(R.id.curve_radius);
        tvLength = (TextView) getDialog().findViewById(R.id.curve_length);
        tvTangent = (TextView) getDialog().findViewById(R.id.curve_tangent);
        tvChordDistance = (TextView) getDialog().findViewById(R.id.curve_chord_distance);

        tvDelta.setText(SurveyMathHelper.convertDECtoDMS(curveSurvey.getDeltaDEC(),0));
        tvRadius.setText(DISTANCE_PRECISION_FORMATTER.format(curveSurvey.getRadius()));
        tvLength.setText(DISTANCE_PRECISION_FORMATTER.format(curveSurvey.getLength()));
        tvTangent.setText(DISTANCE_PRECISION_FORMATTER.format(curveSurvey.getTangent()));
        tvChordDistance.setText(DISTANCE_PRECISION_FORMATTER.format(curveSurvey.getChord()));

    }

    private void loadPreferences(){
        Log.d(TAG, "loadPreferences: Started...");

        COORDINATE_FORMATTER = new DecimalFormat(preferenceLoaderHelper.getValueSystemCoordinatesPrecisionDisplay());
        DISTANCE_PRECISION_FORMATTER = new DecimalFormat(preferenceLoaderHelper.getValueSystemDistancePrecisionDisplay());

    }

}
