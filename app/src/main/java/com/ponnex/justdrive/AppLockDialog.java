package com.ponnex.justdrive;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.view.Gravity;
import android.widget.Button;
import android.widget.TextView;

/**
 * Created by EmmanuelFrancis on 5/21/2015.
 */
public class AppLockDialog {
    public Context mContext;
    AlertDialog alertDialog;

    public AppLockDialog(Context context) {
        mContext = context;
    }

    public void Dialog() {
        alertDialog = new AlertDialog.Builder(mContext).create();
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.setTitle("Just Drive");
        alertDialog.setMessage("No text, tweet, Facebook update, or email is worth your life so put down your phone and Just Drive.");
        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        AppLockActivity.dismiss.finish();
                        // Go to the Home screen
                        Intent homeIntent = new Intent();
                        homeIntent.setAction(Intent.ACTION_MAIN);
                        homeIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                                | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        homeIntent.addCategory(Intent.CATEGORY_HOME);
                        mContext.startActivity(homeIntent);
                    }
                });
        alertDialog.show();
        TextView messageText = (TextView) alertDialog.findViewById(android.R.id.message);
        messageText.setGravity(Gravity.CENTER);

        final int accentcolor = mContext.getResources().getColor(R.color.accent);

        Button button = alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE);
        button.setTextColor(accentcolor);
    }
}