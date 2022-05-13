package com.appme.story.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import com.appme.story.R;

public class DebianNotificationService extends Service {
   
    @Override
    public void onCreate() {
        Log.i("NotifService", "Started with onCreate");
    }
      
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startID) {
        /*String type = intent.getStringExtra(PubkeyDatabase.FIELD_PUBKEY_TYPE);
        if ("VNC".equals(type)) {
            startVNCServerNotification();
        }
        if (!"cancelVNCNotif".equals(type)) {
            return 1;
        }
        cancelVNCServerNotification();*/
        return 1;
    }

    private void startVNCServerNotification() {
       /* int id = Integer.parseInt(new SimpleDateFormat("ddHHmmss", Locale.US).format(new Date()));
        Intent VNCIntent = new Intent(this, DebianService.class);
        VNCIntent.putExtra(PubkeyDatabase.FIELD_PUBKEY_TYPE, "VNCReconnect");
        PendingIntent pendingIntent = PendingIntent.getService(this, 0, VNCIntent, 0);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext());
        builder.setContentIntent(pendingIntent);
        builder.setAutoCancel(false);
        builder.setContentTitle("VNC Client Running");
        builder.setOngoing(true);
        builder.setSmallIcon(R.drawable.xterm_transparent);
        builder.setPriority(1);
        Notification notification = builder.build();
        notification.flags |= 34;
        startForeground(id, notification);*/
    }

    private void cancelVNCServerNotification() {
        stopForeground(true);
        stopSelf();
    }
}

