package com.survlogic.survlogic.view;

import android.content.Context;
import android.graphics.Point;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;

import com.survlogic.survlogic.R;
import com.survlogic.survlogic.model.PointSurvey;
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

public class SortablePointSurveyTableView extends SortableTableView <PointSurvey> {

    private static final String TAG = "SortablePointSurveyTabl";

    private Context mContext;
    private static final int TEXT_SIZE = 14;

    public SortablePointSurveyTableView(final Context context) {
        this(context, null);
        this.mContext = context;
    }

    public SortablePointSurveyTableView(final Context context, final AttributeSet attributes) {
        this(context, attributes, android.R.attr.listViewStyle);
        this.mContext = context;
    }

    public SortablePointSurveyTableView(Context context, AttributeSet attributes, int styleAttributes) {
        super(context, attributes, styleAttributes);

        this.mContext = context;

        PreferenceLoaderHelper preferenceLoaderHelper = new PreferenceLoaderHelper(mContext);
        final SimpleTableHeaderAdapter simpleTableHeaderAdapter;

        if (preferenceLoaderHelper.getValueFormatCoordinateEntry()){
            simpleTableHeaderAdapter = new SimpleTableHeaderAdapter(context,
                    R.string.job_point_list_header_pntNo, R.string.job_point_list_header_northing,
                    R.string.job_point_list_header_easting, R.string.job_point_list_header_elevation,
                    R.string.job_point_list_header_description);

        }else{
            simpleTableHeaderAdapter = new SimpleTableHeaderAdapter(context,
                    R.string.job_point_list_header_pntNo, R.string.job_point_list_header_easting,
                    R.string.job_point_list_header_northing, R.string.job_point_list_header_elevation,
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
        tableColumnWeightModel.setColumnWeight(0,1); //Point no
        tableColumnWeightModel.setColumnWeight(1,3); //Northing/Easting
        tableColumnWeightModel.setColumnWeight(2,3); //Easting/Northing
        tableColumnWeightModel.setColumnWeight(3,2); //Elevation
        tableColumnWeightModel.setColumnWeight(4,2); //Description

        setColumnComparator(0, PointSurveyComparators.getPointNoComparator());

        if (preferenceLoaderHelper.getValueFormatCoordinateEntry()){
            setColumnComparator(1, PointSurveyComparators.getNorthingComparator());
            setColumnComparator(2, PointSurveyComparators.getEastingComparator());
        }else{
            setColumnComparator(1, PointSurveyComparators.getEastingComparator());
            setColumnComparator(2, PointSurveyComparators.getNorthingComparator());
        }

        setColumnComparator(3, PointSurveyComparators.getElevationComparator());
        setColumnComparator(4, PointSurveyComparators.getDescriptionComparator());


    }
}
