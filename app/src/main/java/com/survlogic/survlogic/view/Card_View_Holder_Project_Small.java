package com.survlogic.survlogic.view;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.maps.model.Circle;
import com.survlogic.survlogic.R;

import org.w3c.dom.Text;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by chrisfillmore on 6/30/2017.
 */

public class Card_View_Holder_Project_Small extends RecyclerView.ViewHolder  {

    public TextView txtProjectName, txtLocation, txtLastModify, txtProjectDesc, txtProjectId;
    public CircleImageView imgProjectImage;
    public ProgressBar progressBar;
    public View mCardView;

    public Card_View_Holder_Project_Small(View itemView) {
        super(itemView);

        mCardView = itemView;

        txtProjectId = (TextView) itemView.findViewById(R.id.card_project_view_card_id);
        txtProjectName = (TextView) itemView.findViewById(R.id.card_project_view_project_name);
        txtLocation  = (TextView) itemView.findViewById(R.id.card_project_view_location_value);
        txtLastModify  = (TextView) itemView.findViewById(R.id.card_project_view_last_opened);
        txtProjectDesc  = (TextView) itemView.findViewById(R.id.card_project_view_project_details);

        imgProjectImage = (CircleImageView) itemView.findViewById(R.id.card_project_view_project_image);
        progressBar = (ProgressBar) itemView.findViewById(R.id.card_project_view_project_progress);
    }

}
