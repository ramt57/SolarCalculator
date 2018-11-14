package com.ramt57.solarcalc.notification;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/*Broadcast reciver which triggers at a certain time*/
public class BroadcastReciever extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent intent1 = new Intent(context, NotificationService.class);
        context.startService(intent1);
    }
}
