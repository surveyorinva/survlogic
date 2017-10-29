package com.survlogic.survlogic.adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.survlogic.survlogic.model.PointGeodetic;
import com.survlogic.survlogic.model.PointSurvey;
import com.survlogic.survlogic.utils.PreferenceLoaderHelper;

import java.text.DecimalFormat;
import java.util.List;

import de.codecrafters.tableview.TableView;
import de.codecrafters.tableview.toolkit.LongPressAwareTableDataAdapter;

import static android.view.View.TEXT_ALIGNMENT_CENTER;

/**
 * Created by chrisfillmore on 8/10/2017.
 */

public class PointGeodeticTableDataAdapter extends LongPressAwareTableDataAdapter<PointGeodetic> {
    private static final String TAG = "PointGeodeticTableDataA";

    private Context mContext;
    private SharedPreferences sharedPreferences;
    PreferenceLoaderHelper preferenceLoaderHelper;

    private int TEXT_SIZE = 10;

    private static DecimalFormat COORDINATE_FORMATTER;

    public PointGeodeticTableDataAdapter(final Context context, final List<PointGeodetic> data, final TableView<PointGeodetic> tableView, int textSize) {
        super(context, data, tableView);

        this.mContext = context;
        this.TEXT_SIZE = textSize;

        preferenceLoaderHelper = new PreferenceLoaderHelper(mContext);
        loadPreferences();

    }


    @Override
    public View getDefaultCellView(int rowIndex, int columnIndex, ViewGroup parentView) {
        final PointGeodetic pointGeodetic = getRowData(rowIndex);

        View renderedView = null;

        switch (columnIndex){
            case 0:  //Point No
                renderedView = renderPointNo(pointGeodetic);
                break;

            case 1:  //Latitude
                if (preferenceLoaderHelper.getValueFormatCoordinateEntry())
                    renderedView = renderLatitude(pointGeodetic);
                else{
                    renderedView = renderLongitude(pointGeodetic);
                }
                break;

            case 2:  //Longitude
                if (preferenceLoaderHelper.getValueFormatCoordinateEntry())
                    renderedView = renderLongitude(pointGeodetic);
                else{
                    renderedView = renderLatitude(pointGeodetic);
                }

                break;

            case 3:  //Elevation
                renderedView = renderElevation(pointGeodetic);
                break;

            case 4:  //Description
                renderedView = renderDescription(pointGeodetic);
                break;

        }

        return renderedView;
    }

    @Override
    public View getLongPressCellView(int rowIndex, int columnIndex, ViewGroup parentView) {
        //possibly used for occupy and backsight commands
        return null;
    }

    private View renderPointNo(final PointGeodetic pointGeodetic){
        return renderString(String.valueOf(pointGeodetic.getPoint_no()));
    }


    private View renderLatitude(final  PointGeodetic pointGeodetic){
        final String coordinateString = COORDINATE_FORMATTER.format(pointGeodetic.getLatitude());

        final TextView textView = new TextView(getContext());

        textView.setText(coordinateString);
        textView.setPadding(20, 10, 20, 10);
        textView.setTextSize(TEXT_SIZE);
        textView.setTextAlignment(TEXT_ALIGNMENT_CENTER);

        return textView;

    }

    private View renderLongitude(final  PointGeodetic pointGeodetic){
        final String coordinateString = COORDINATE_FORMATTER.format(pointGeodetic.getLongitude());

        final TextView textView = new TextView(getContext());

        textView.setText(coordinateString);
        textView.setPadding(20, 10, 20, 10);
        textView.setTextSize(TEXT_SIZE);
        textView.setTextAlignment(TEXT_ALIGNMENT_CENTER);
        return textView;

    }

    private View renderElevation(final  PointGeodetic pointGeodetic){
        final String coordinateString = COORDINATE_FORMATTER.format(pointGeodetic.getEllipsoid());

        Log.d(TAG, "renderElevation: " + coordinateString);
        final TextView textView = new TextView(getContext());

        textView.setText(coordinateString);
        textView.setPadding(20, 10, 20, 10);
        textView.setTextSize(TEXT_SIZE);
        textView.setTextAlignment(TEXT_ALIGNMENT_CENTER);
        return textView;

    }

    private View renderDescription(final PointGeodetic pointGeodetic){
        return renderString(pointGeodetic.getDescription());
    }

    private View renderString(final String value) {
        final TextView textView = new TextView(getContext());
        textView.setText(value);
        textView.setPadding(20, 10, 20, 10);
        textView.setTextAlignment(TEXT_ALIGNMENT_CENTER);
        textView.setTextSize(TEXT_SIZE);
        return textView;
    }



    private void loadPreferences(){
        Log.d(TAG, "loadPreferences: Started...");


        COORDINATE_FORMATTER = new DecimalFormat(preferenceLoaderHelper.getValueSystemCoordinatesPrecisionDisplay());


    }



}
