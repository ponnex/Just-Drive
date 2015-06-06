package com.ponnex.justdrive;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

/**
 * Created by ramos on 6/6/2015.
 */

public class MessageNotification extends IntentService {
    private int drawable;

    protected static String TAG = "com.ponnex.justdrive.MessageNotification";

    public MessageNotification() {
        super(TAG);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG + "_MN", "Created");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String msg_from = intent.getStringExtra("From");
        String message = intent.getStringExtra("Message");
        Boolean status = intent.getBooleanExtra("Status", true);
        if(status) {
            drawable = R.drawable.ic_message_sent;
        } else {
            drawable = R.drawable.ic_message_failed;
        }
        shownotification(msg_from, message, drawable);
    }

    private void shownotification(String msg_from, String message, int drawable) {
        Intent intent1 = new Intent(this, MainActivity.class); //change to notification activity next time
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent1, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext());
        builder.setContentTitle(getString(R.string.app_name))
                .setContentText(message + msg_from)
                .setSmallIcon(drawable)
                .setContentIntent(pendingIntent);

        // Get an instance of the Notification Manager
        NotificationManager notifyManager = (NotificationManager)
                getSystemService(Context.NOTIFICATION_SERVICE);

        // Build the notification and post it
        notifyManager.notify(3, builder.build());
    }
}
