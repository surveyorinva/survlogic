package com.survlogic.survlogic.view;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.survlogic.survlogic.R;
import com.survlogic.survlogic.background.BackgroundProjectImagesSetup;
import com.survlogic.survlogic.model.ProjectImages;

import java.io.ByteArrayOutputStream;

/**
 * Created by chrisfillmore on 7/12/2017.
 */

public class DialogProjectPhotoAdd extends DialogFragment {

    private static final String TAG = "DialogProjectPhotoAdd";

    private byte[] mImage;
    private Bitmap mImageLocal, mImageWatermark;
    private int project_id;

    private boolean mAddWatermark = false;

    private EditText etDescription;
    private Button btnAddWatermark, btnRemoveWatermark;

    public static DialogProjectPhotoAdd newInstance(Integer mProjectId, Bitmap mBitmap) {

        DialogProjectPhotoAdd frag = new DialogProjectPhotoAdd();
        Bundle args = new Bundle();

        args.putInt("project_id", mProjectId);
        args.putParcelable("image", mBitmap);

        frag.setArguments(args);
        return frag;

    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        project_id = getArguments().getInt("project_id");
        mImageLocal = getArguments().getParcelable("image");

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(),R.style.DialogPopupStyle);
        LayoutInflater inflater = getActivity().getLayoutInflater();

        View v = inflater.inflate(R.layout.dialog_project_picture,null);
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

        AlertDialog alertDialog = (AlertDialog) getDialog();

        etDescription = (EditText) getDialog().findViewById(R.id.photo_description);

        final ImageView ivPhoto = (ImageView) getDialog().findViewById(R.id.photo_in_dialog_project_picture);
        ivPhoto.setImageBitmap(mImageLocal);

        Button btnSave = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitForm(v);
            }
        });

        btnAddWatermark = (Button) getDialog().findViewById(R.id.btn_add_watermark);
        btnAddWatermark.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String description = etDescription.getText().toString();
                if(!isStringNull(description)){
                    mAddWatermark = true;
                    mImageWatermark = setWatermark(mImageLocal, description, true);
                    ivPhoto.setImageBitmap(mImageWatermark);
                    Log.d(TAG, "btnAddWatermark: Added Watermark ");
                }

            }
        });

        btnRemoveWatermark = (Button) getDialog().findViewById(R.id.btn_revert_watermark);
        btnRemoveWatermark.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                etDescription.setText("");
                mAddWatermark = false;
                ivPhoto.setImageBitmap(mImageLocal);
                Log.d(TAG, "btnAddWatermark: Cleared Watermark  ");
            }
        });


    }

    private void submitForm(View v) {

            if (mAddWatermark){
                mImage = convertImageToByte(mImageWatermark);
            }else{
                mImage = convertImageToByte(mImageLocal);
            }

            // Create Project model
            ProjectImages projectImages = new ProjectImages(project_id,0,mImage,0,0,0);

            // Setup Background Task
            BackgroundProjectImagesSetup backgroundProjectImagesSetup = new BackgroundProjectImagesSetup(getActivity());

            // Execute background task
            backgroundProjectImagesSetup.execute(projectImages);
            Log.d(TAG, "submitForm: Complete.  Photo with ProjectID: " + project_id + " Saved");
            getDialog().dismiss();


    }

    private static Bitmap setWatermark(Bitmap src, String watermark, Boolean createOverlay){
        Log.d(TAG, "setWatermark: Starting method");

        Paint rectBlackStroke,rectBlackFill;
        Rect rectWatermarkBounds = new Rect();
        RectF rectOverlay;

        int mTextSize = 40, mTextAlpha = 245;
        int bottomPadding = 20;
        float mLineWidth = 3;

        int w = src.getWidth();
        int h = src.getHeight();

        Bitmap result = Bitmap.createBitmap(w,h,src.getConfig());

        Canvas canvas = new Canvas(result);
        canvas.drawBitmap(src,0,0,null);


//        Text
        Paint paint = new Paint();
        paint.setColor(Color.WHITE);
        paint.setAlpha(mTextAlpha);
        paint.setTextSize(mTextSize);
        paint.setStyle(Paint.Style.FILL);
        paint.setAntiAlias(true);

        paint.getTextBounds(watermark,0, watermark.length(),rectWatermarkBounds);
        int headerHeight = rectWatermarkBounds.height();
        int headerWidth = rectWatermarkBounds.width();

        int intStartTextX = 1;
        int intStartTextY = h - headerHeight - bottomPadding;

        canvas.drawText(watermark, intStartTextX, intStartTextY, paint);

//        Overlay
        if (createOverlay) {
            rectBlackFill = new Paint();
            rectBlackFill.setStyle(Paint.Style.FILL);
            rectBlackFill.setColor(Color.BLACK);
            rectBlackFill.setAlpha(180);

            rectBlackStroke = new Paint();
            rectBlackStroke.setStyle(Paint.Style.STROKE);
            rectBlackStroke.setColor(Color.BLACK);
            rectBlackStroke.setStrokeWidth(mLineWidth);
            rectBlackStroke.setAntiAlias(true);

            int intStartBoxX = 0;
            int StartBoxY = intStartTextY - headerHeight - bottomPadding;

            rectOverlay = new RectF(intStartBoxX, StartBoxY, w, h);
            canvas.drawRect(rectOverlay, rectBlackFill);

        }

        canvas.save(Canvas.ALL_SAVE_FLAG);
        Log.d(TAG, "setWatermark: finish creating watermark");

        return result;
    }

    //Convert bitmap to bytes
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
    private static byte[] convertImageToByte(Bitmap b){

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        b.compress(Bitmap.CompressFormat.PNG, 0, bos);
        return bos.toByteArray();

    }

    private Bitmap convertToBitmap(byte[] b){

        return BitmapFactory.decodeByteArray(b, 0, b.length);

    }


    private boolean isStringNull(String string){
        Log.d(TAG, "isStringNull: checking string if null.");

        if(string.equals("")){
            return true;
        }
        else{
            return false;
        }
    }

}
