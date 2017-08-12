package com.survlogic.survlogic.adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.survlogic.survlogic.R;
import com.survlogic.survlogic.model.PointSurvey;
import com.survlogic.survlogic.utils.PreferenceLoaderHelper;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;

import de.codecrafters.tableview.TableView;
import de.codecrafters.tableview.toolkit.LongPressAwareTableDataAdapter;

/**
 * Created by chrisfillmore on 8/10/2017.
 */

public class PointSurveyTableDataAdapter extends LongPressAwareTableDataAdapter<PointSurvey> {
    private static final String TAG = "PointSurveyTableDataAda";

    private Context mContext;
    private SharedPreferences sharedPreferences;
    PreferenceLoaderHelper preferenceLoaderHelper;

    private static final int TEXT_SIZE = 14;

    private static DecimalFormat COORDINATE_FORMATTER;

    public PointSurveyTableDataAdapter(final Context context, final List<PointSurvey> data, final TableView<PointSurvey> tableView) {
        super(context, data, tableView);

        this.mContext = context;

        preferenceLoaderHelper = new PreferenceLoaderHelper(mContext);
        loadPreferences();

    }


    @Override
    public View getDefaultCellView(int rowIndex, int columnIndex, ViewGroup parentView) {
        final PointSurvey pointSurvey = getRowData(rowIndex);

        View renderedView = null;

        switch (columnIndex){
            case 0:  //Point No
                renderedView = renderPointNo(pointSurvey);
                break;

            case 1:  //Northing
                if (preferenceLoaderHelper.getValueFormatCoordinateEntry())
                    renderedView = renderNorthing(pointSurvey);
                else{
                    renderedView = renderEasting(pointSurvey);
                }
                break;

            case 2:  //Easting
                if (preferenceLoaderHelper.getValueFormatCoordinateEntry())
                    renderedView = renderEasting(pointSurvey);
                else{
                    renderedView = renderNorthing(pointSurvey);
                }

                break;

            case 3:  //Elevation
                renderedView = renderElevation(pointSurvey);
                break;

            case 4:  //Description
                renderedView = renderDescription(pointSurvey);
                break;

        }

        return renderedView;
    }

    @Override
    public View getLongPressCellView(int rowIndex, int columnIndex, ViewGroup parentView) {
        //possibly used for occupy and backsight commands
        return null;
    }

    private View renderPointNo(final PointSurvey pointSurvey){
        return renderString(String.valueOf(pointSurvey.getPoint_no()));
    }


    private View renderNorthing(final  PointSurvey pointSurvey){
        final String coordinateString = COORDINATE_FORMATTER.format(pointSurvey.getNorthing());

        final TextView textView = new TextView(getContext());

        textView.setText(coordinateString);
        textView.setPadding(20, 10, 20, 10);
        textView.setTextSize(TEXT_SIZE);
        return textView;

    }

    private View renderEasting(final  PointSurvey pointSurvey){
        final String coordinateString = COORDINATE_FORMATTER.format(pointSurvey.getEasting());

        final TextView textView = new TextView(getContext());

        textView.setText(coordinateString);
        textView.setPadding(20, 10, 20, 10);
        textView.setTextSize(TEXT_SIZE);
        return textView;

    }

    private View renderElevation(final  PointSurvey pointSurvey){
        final String coordinateString = COORDINATE_FORMATTER.format(pointSurvey.getElevation());

        Log.d(TAG, "renderElevation: " + coordinateString);
        final TextView textView = new TextView(getContext());

        textView.setText(coordinateString);
        textView.setPadding(20, 10, 20, 10);
        textView.setTextSize(TEXT_SIZE);
        return textView;

    }

    private View renderDescription(final PointSurvey pointSurvey){
        return renderString(pointSurvey.getDescription());
    }

    private View renderString(final String value) {
        final TextView textView = new TextView(getContext());
        textView.setText(value);
        textView.setPadding(20, 10, 20, 10);
        textView.setTextSize(TEXT_SIZE);
        return textView;
    }



    private void loadPreferences(){
        Log.d(TAG, "loadPreferences: Started...");



        COORDINATE_FORMATTER = new DecimalFormat(preferenceLoaderHelper.getValueSystemDistancePrecisionDisplay());


    }



}
