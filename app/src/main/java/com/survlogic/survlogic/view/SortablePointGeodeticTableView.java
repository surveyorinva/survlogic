package com.survlogic.survlogic.view;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;

import com.survlogic.survlogic.R;
import com.survlogic.survlogic.model.PointGeodetic;
import com.survlogic.survlogic.model.PointSurvey;
import com.survlogic.survlogic.model_util.PointGeodeticComparators;
import com.survlogic.survlogic.model_util.PointSurveyComparators;
import com.survlogic.survlogic.utils.PreferenceLoaderHelper;

import de.codecrafters.tableview.SortableTableView;
import de.codecrafters.tableview.model.TableColumnWeightModel;
import de.codecrafters.tableview.toolkit.SimpleTableHeaderAdapter;
import de.codecrafters.tableview.toolkit.SortStateViewProviders;
import de.codecrafters.tableview.toolkit.TableDataRowBackgroundProviders;

/**
 * Created by chrisfillmore on 8/10/2017.
 */

public class SortablePointGeodeticTableView extends SortableTableView <PointGeodetic> {

    private static final String TAG = "SortablePointSurveyTabl";

    private Context mContext;
    private static final int TEXT_SIZE = 10;

    public SortablePointGeodeticTableView(final Context context) {
        this(context, null);
        this.mContext = context;
    }

    public SortablePointGeodeticTableView(final Context context, final AttributeSet attributes) {
        this(context, attributes, android.R.attr.listViewStyle);
        this.mContext = context;
    }

    public SortablePointGeodeticTableView(Context context, AttributeSet attributes, int styleAttributes) {
        super(context, attributes, styleAttributes);

        this.mContext = context;

        PreferenceLoaderHelper preferenceLoaderHelper = new PreferenceLoaderHelper(mContext);
        final SimpleTableHeaderAdapter simpleTableHeaderAdapter;

        if (preferenceLoaderHelper.getValueFormatCoordinateEntry()){
            simpleTableHeaderAdapter = new SimpleTableHeaderAdapter(context,
                    R.string.job_point_list_header_pntNo, R.string.job_point_list_header_latitude,
                    R.string.job_point_list_header_longitude, R.string.job_point_list_header_ellipsoid,
                    R.string.job_point_list_header_description);

        }else{
            simpleTableHeaderAdapter = new SimpleTableHeaderAdapter(context,
                    R.string.job_point_list_header_pntNo, R.string.job_point_list_header_longitude,
                    R.string.job_point_list_header_latitude, R.string.job_point_list_header_ellipsoid,
                    R.string.job_point_list_header_description);

        }

        simpleTableHeaderAdapter.setTextColor(ContextCompat.getColor(context, R.color.tableHeaderText));
        simpleTableHeaderAdapter.setTextSize(TEXT_SIZE);
        setHeaderAdapter(simpleTableHeaderAdapter);

        final int rowColorEven = ContextCompat.getColor(context, R.color.tableDataRowEven);
        final int rowColorOdd = ContextCompat.getColor(context, R.color.tableDataRowOdd);
        setDataRowBackgroundProvider(TableDataRowBackgroundProviders.alternatingRowColors(rowColorEven, rowColorOdd));
        setHeaderSortStateViewProvider(SortStateViewProviders.brightArrows());

        final TableColumnWeightModel tableColumnWeightModel = new TableColumnWeightModel(5);
        tableColumnWeightModel.setColumnWeight(0,2); //Point no
        tableColumnWeightModel.setColumnWeight(1,3); //Northing/Easting
        tableColumnWeightModel.setColumnWeight(2,3); //Easting/Northing
        tableColumnWeightModel.setColumnWeight(3,2); //Elevation
        tableColumnWeightModel.setColumnWeight(4,2); //Description
        setColumnModel(tableColumnWeightModel);

        setColumnComparator(0, PointGeodeticComparators.getPointNoComparator());

        if (preferenceLoaderHelper.getValueFormatCoordinateEntry()){
            setColumnComparator(1, PointGeodeticComparators.getLatitudeComparator());
            setColumnComparator(2, PointGeodeticComparators.getLongtitudeComparator());
        }else{
            setColumnComparator(1, PointGeodeticComparators.getLongtitudeComparator());
            setColumnComparator(2, PointGeodeticComparators.getLatitudeComparator());
        }

        setColumnComparator(3, PointGeodeticComparators.getElevationComparator());
        setColumnComparator(4, PointGeodeticComparators.getDescriptionComparator());


    }
}
