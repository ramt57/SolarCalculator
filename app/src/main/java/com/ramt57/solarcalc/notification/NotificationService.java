package com.ramt57.solarcalc.notification;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.JobIntentService;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

import com.ramt57.solarcalc.MapsActivity;
import com.ramt57.solarcalc.R;

public class NotificationService extends JobIntentService { /*for oreo we use JobIntentService*/
    private static final int NOTIFICATION_ID = 3;
    private final String CHANNEL_ID = "YOUR_CHANNEL_ID";

    @Override
    protected void onHandleWork(@NonNull Intent intent) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setContentTitle("Golden Hour");
        builder.setContentText("Your goldenhour stars now");
        builder.setSmallIcon(R.mipmap.ic_launcher_round);
        Intent notifyIntent = new Intent(this, MapsActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 2, notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        //to be able to launch your activity from the notification
        builder.setContentIntent(pendingIntent);

        // Set the Channel ID for Android O.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder.setChannelId(CHANNEL_ID); // Channel ID
        }
        Notification notificationCompat = builder.build();
        NotificationManagerCompat managerCompat = NotificationManagerCompat.from(this);
        managerCompat.notify(NOTIFICATION_ID, notificationCompat);
    }
}
