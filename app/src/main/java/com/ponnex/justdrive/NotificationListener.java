package com.ponnex.justdrive;

import android.annotation.TargetApi;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.service.notification.NotificationListenerService;
import android.text.TextUtils;
import android.util.Log;

/**
 * Created by EmmanuelFrancis on 5/5/2015.
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class NotificationListener extends NotificationListenerService {

    public static Intent getInterruptionFilterRequestIntent(final int filter) {
        Intent request = new Intent("com.ponnex.justdrive.ACTION_REQUEST_INTERRUPTION_FILTER");
        request.putExtra("filter", filter);
        return request;
    }

    public static void requestInterruptionFilter(Context context, final int filter) {
        Intent request = getInterruptionFilterRequestIntent(filter);
        context.sendBroadcast(request);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e("TYPE", "NL Created");
        // Handle being told to change the interruption filter (zen mode).
        if (!TextUtils.isEmpty(intent.getAction())) {
            if ("com.ponnex.justdrive.ACTION_REQUEST_INTERRUPTION_FILTER".equals(intent.getAction())) {
                if (intent.hasExtra("filter")) {
                    final int filter = intent.getIntExtra("filter", INTERRUPTION_FILTER_ALL);
                    if(filter!=INTERRUPTION_FILTER_NONE){
                        requestInterruptionFilter(INTERRUPTION_FILTER_NONE);
                    }
                }
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }
}
