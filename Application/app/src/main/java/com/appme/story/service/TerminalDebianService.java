package com.appme.story.service;

import android.app.Service;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.*;
import android.content.Intent;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.app.Notification;
import android.app.PendingIntent;

import com.appme.story.R;
import com.appme.story.AppConfig;
import com.appme.story.application.TerminalDebian;
import com.appme.story.application.DebianRemoteInterface;
import com.appme.story.engine.app.terminal.TermExec;
import com.appme.story.engine.app.terminal.TermDebug;
import com.appme.story.engine.app.terminal.BoundSession;
import com.appme.story.engine.app.terminal.GenericTermSession;
import com.appme.story.engine.app.terminal.emulatorview.TermSession;
import com.appme.story.engine.app.terminal.compat.ServiceForegroundCompat;
import com.appme.story.engine.app.terminal.libtermexec.v1.*;
import com.appme.story.engine.app.terminal.util.SessionList;
import com.appme.story.engine.app.terminal.util.TermSettings;

import java.util.UUID;

public class TerminalDebianService extends Service implements TermSession.FinishCallback
{
    /* Parallels the value of START_STICKY on API Level >= 5 */
    private static final int COMPAT_START_STICKY = 1;

    private static final int RUNNING_NOTIFICATION = 1;
    private ServiceForegroundCompat compat;

    private SessionList mTermSessions;

    public class TSBinder extends Binder {
        public TerminalDebianService getService() {
            Log.i("TermService", "Activity binding to service");
            return TerminalDebianService.this;
        }
    }
    
    private final IBinder mTSBinder = new TSBinder();

    @Override
    public void onStart(Intent intent, int flags) {
    }

    /* This should be @Override if building with API Level >=5 */
    public int onStartCommand(Intent intent, int flags, int startId) {
        return COMPAT_START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        if (TermExec.SERVICE_ACTION_V1.equals(intent.getAction())) {
            Log.i("TermService", "Outside process called onBind()");

            return new RBinder();
        } else {
            Log.i("TermService", "Activity called onBind()");

            return mTSBinder;
        }
    }

    @Override
    public void onCreate() {
        // should really belong to the Application class, but we don't use one...
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor = prefs.edit();
        String defValue = getDir("HOME", MODE_PRIVATE).getAbsolutePath();

        compat = new ServiceForegroundCompat(this);
        mTermSessions = new SessionList();

         /* Put the service in the foreground. */
        Intent notifyIntent = new Intent(this, TerminalDebian.class);
        notifyIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notifyIntent, 0);
        Notification.Builder builder = new Notification.Builder(this);
        Notification notification = builder.setContentIntent(pendingIntent)
            .setSmallIcon(R.drawable.ic_stat_service_notification_icon)
            .setTicker(getText(R.string.terminal_service_notify_text))
            .setWhen(System.currentTimeMillis())
            //.setAutoCancel(true)  TODO
            .setContentTitle(getText(R.string.application_terminal))
            .setContentText(getText(R.string.terminal_service_notify_text))
            .build();
        notification.flags |= Notification.FLAG_ONGOING_EVENT;
        compat.startForeground(RUNNING_NOTIFICATION, notification);

        Log.d(TermDebug.LOG_TAG, "TermService started");
        return;
    }

    @Override
    public void onDestroy() {
        compat.stopForeground(true);
        for (TermSession session : mTermSessions) {
            /* Don't automatically remove from list of sessions -- we clear the
             * list below anyway and we could trigger
             * ConcurrentModificationException if we do */
            session.setFinishCallback(null);
            session.finish();
        }
        mTermSessions.clear();
        return;
    }

    public SessionList getSessions() {
        return mTermSessions;
    }

    public void onSessionFinish(TermSession session) {
        mTermSessions.remove(session);
    }

    private final class RBinder extends ITerminal.Stub {
        @Override
        public IntentSender startSession(final ParcelFileDescriptor pseudoTerminalMultiplexerFd,
                                         final ResultReceiver callback) {
            final String sessionHandle = UUID.randomUUID().toString();

            // distinct Intent Uri and PendingIntent requestCode must be sufficient to avoid collisions
            final Intent switchIntent = new Intent(AppConfig.DEBIAN_OPEN_NEW_WINDOW)
                    .setData(Uri.parse(sessionHandle))
                    .addCategory(Intent.CATEGORY_DEFAULT)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    .putExtra(AppConfig.DEBIAN_EXTRA_TARGET_WINDOW, sessionHandle);

            final PendingIntent result = PendingIntent.getActivity(getApplicationContext(), sessionHandle.hashCode(),
                    switchIntent, 0);

            final PackageManager pm = getPackageManager();
            final String[] pkgs = pm.getPackagesForUid(getCallingUid());
            if (pkgs == null || pkgs.length == 0)
                return null;

            for (String packageName:pkgs) {
                try {
                    final PackageInfo pkgInfo = pm.getPackageInfo(packageName, 0);

                    final ApplicationInfo appInfo = pkgInfo.applicationInfo;
                    if (appInfo == null)
                        continue;

                    final CharSequence label = pm.getApplicationLabel(appInfo);

                    if (!TextUtils.isEmpty(label)) {
                        final String niceName = label.toString();

                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                GenericTermSession session = null;
                                try {
                                    final TermSettings settings = new TermSettings(getResources(),
                                            PreferenceManager.getDefaultSharedPreferences(getApplicationContext()));

                                    session = new BoundSession(pseudoTerminalMultiplexerFd, settings, niceName);

                                    mTermSessions.add(session);

                                    session.setHandle(sessionHandle);
                                    session.setFinishCallback(new RBinderCleanupCallback(result, callback));
                                    session.setTitle("");

                                    session.initializeEmulator(80, 24);
                                } catch (Exception whatWentWrong) {
                                    Log.e("TermService", "Failed to bootstrap AIDL session: "
                                            + whatWentWrong.getMessage());

                                    if (session != null)
                                        session.finish();
                                }
                            }
                        });

                        return result.getIntentSender();
                    }
                } catch (PackageManager.NameNotFoundException ignore) {}
            }

            return null;
        }
    }

    private final class RBinderCleanupCallback implements TermSession.FinishCallback {
        private final PendingIntent result;
        private final ResultReceiver callback;

        public RBinderCleanupCallback(PendingIntent result, ResultReceiver callback) {
            this.result = result;
            this.callback = callback;
        }

        @Override
        public void onSessionFinish(TermSession session) {
            result.cancel();

            callback.send(0, new Bundle());

            mTermSessions.remove(session);
        }
    }
}
