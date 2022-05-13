package com.appme.story.service;

import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.IBinder;
import android.util.Log;

import java.io.File;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.appme.story.AppConfig;
import com.appme.story.application.DebianRunScript;

public class DebianService extends Service {

    boolean shown = false;

    @Override
    public IBinder onBind(Intent intent) {
        // We don't provide binding, so return null
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startID) {
        /*String intentType = intent.getStringExtra(PubkeyDatabase.FIELD_PUBKEY_TYPE);
        if ("VNC".equals(intentType)) {
            startVNCServer(intent.getBooleanExtra("newXterm", false), intent.getStringExtra("command"));
        }
        if (!"VNCReconnect".equals(intentType)) {
            return Service.START_STICKY;
        }
        reconnectVNC();*/

        return Service.START_STICKY;
    }

    private void startVNCServer(boolean createNewXTerm, String command) {
        /*Intent termIntent = new Intent(this, DebianRunScript.class);
        termIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);    
        termIntent.addCategory(Intent.CATEGORY_DEFAULT);
        termIntent.setAction(AppConfig.DEBIAN_ACTION_RUN_SCRIPT);
        if (command == null) {
            command = "/bin/bash";
        }
        if (createNewXTerm) {
            termIntent.putExtra(AppConfig.DEBIAN_EXTRA_INITIAL_COMMAND, getInstallDir().getAbsolutePath() + "/support/launchXterm " + command);
        } else {
            termIntent.putExtra(AppConfig.DEBIAN_EXTRA_INITIAL_COMMAND, getInstallDir().getAbsolutePath() + "/support/launchXterm  button_pressed " + command);
        }
        startActivity(termIntent);
        final Intent notifServiceIntent = new Intent(this, DebianNotificationService.class);
        notifServiceIntent.putExtra(PubkeyDatabase.FIELD_PUBKEY_TYPE, "VNC");
        final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(new Runnable() {
                public void run() {
                    File checkStarted = new File(DebianService.this.getInstallDir().getAbsolutePath() + "/support/.gnuroot_x_started");
                    File checkRunning = new File(DebianService.this.getInstallDir().getAbsolutePath() + "/support/.gnuroot_x_running");
                    if (checkStarted.exists() || checkRunning.exists()) {
                        Intent bvncIntent = new Intent(DebianService.this.getBaseContext(), RemoteCanvasActivity.class);
                        bvncIntent.setData(Uri.parse("vnc://127.0.0.1:5951/?VncPassword=gnuroot"));
                        bvncIntent.addFlags(335544320);
                        DebianService.this.startActivity(bvncIntent);
                        DebianService.this.startService(notifServiceIntent);
                        scheduler.shutdown();
                    }
                }
            }, 3, 2, TimeUnit.SECONDS);
        stopSelf();*/
    }

    private void reconnectVNC() {
        /*Intent bvncIntent = new Intent(getBaseContext(), RemoteCanvasActivity.class);
        bvncIntent.setData(Uri.parse("vnc://127.0.0.1:5951/?VncPassword=gnuroot"));
        bvncIntent.addFlags(335544320);
        startActivity(bvncIntent);
        stopSelf();*/
    }

    public File getInstallDir() {
        try {
            return new File(getPackageManager().getApplicationInfo(AppConfig.APPLICATION_ID, 0).dataDir);
        } catch (PackageManager.NameNotFoundException e) {
            return null;
        }
    }
}
