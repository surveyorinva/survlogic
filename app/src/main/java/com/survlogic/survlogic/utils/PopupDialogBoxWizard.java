package com.survlogic.survlogic.utils;

import android.app.Activity;
import android.content.Context;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

import com.survlogic.survlogic.R;

public class PopupDialogBoxWizard {
    private static final String TAG = "PopupDialogBoxWizard";
    private Context mContext;
    private Activity mActivity;

    public PopupDialogBoxWizard(Context mContext) {
        this.mContext = mContext;
        this.mActivity = (Activity) mContext;

    }

    public AlertDialog genericPointSaveStatus(){
        Log.d(TAG, "dialogSaveStatus: Started");

        LayoutInflater inflater = mActivity.getLayoutInflater();
        View v = inflater.inflate(R.layout.dialog_simple_save_confirmation,null);

        ImageView checkView = v.findViewById(R.id.check);


        android.support.v7.app.AlertDialog myDialogBox = new android.support.v7.app.AlertDialog.Builder(mContext,R.style.DialogSlideDown)
                .setView(v)
                .create();

        Window dialogWindow = myDialogBox.getWindow();
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        dialogWindow.setGravity(Gravity.START | Gravity.TOP);
        dialogWindow.setAttributes(lp);


        return myDialogBox;

    }



}
