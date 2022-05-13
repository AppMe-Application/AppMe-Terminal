package com.appme.story.application;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import android.support.v7.app.AppCompatActivity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

import com.appme.story.R;
import com.appme.story.AppConfig;
import com.appme.story.engine.app.terminal.TermDebug;
import com.appme.story.engine.app.terminal.GenericTermSession;
import com.appme.story.engine.app.terminal.emulatorview.TermSession;
import com.appme.story.engine.app.terminal.util.SessionList;
import com.appme.story.engine.app.terminal.util.TermSettings;
import com.appme.story.service.TerminalDebianService;

public class DebianRemoteInterface extends AppCompatActivity {
  
    private TermSettings mSettings;

    private TerminalDebianService mTermService;
    private Intent mTSIntent;
    private ServiceConnection mTSConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            TerminalDebianService.TSBinder binder = (TerminalDebianService.TSBinder) service;
            mTermService = binder.getService();
            handleIntent();
        }

        public void onServiceDisconnected(ComponentName className) {
            mTermService = null;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        mSettings = new TermSettings(getResources(), prefs);

        Intent TSIntent = new Intent(this, TerminalDebianService.class);
        mTSIntent = TSIntent;
        startService(TSIntent);
        if (!bindService(TSIntent, mTSConnection, BIND_AUTO_CREATE)) {
            Log.e(TermDebug.LOG_TAG, "bind to service failed!");
            finish();
        }
    }

    @Override
    public void finish() {
        ServiceConnection conn = mTSConnection;
        if (conn != null) {
            unbindService(conn);

            // Stop the service if no terminal sessions are running
            TerminalDebianService service = mTermService;
            if (service != null) {
                SessionList sessions = service.getSessions();
                if (sessions == null || sessions.size() == 0) {
                    stopService(mTSIntent);
                }
            }

            mTSConnection = null;
            mTermService = null;
        }
        super.finish();
    }

    protected TerminalDebianService getTermService() {
        return mTermService;
    }

    protected void handleIntent() {
        TerminalDebianService service = getTermService();
        if (service == null) {
            finish();
            return;
        }

        Intent myIntent = getIntent();
        String action = myIntent.getAction();
        if (action.equals(Intent.ACTION_SEND)
                && myIntent.hasExtra(Intent.EXTRA_STREAM)) {
          /* "permission.RUN_SCRIPT" not required as this is merely opening a new window. */
            Object extraStream = myIntent.getExtras().get(Intent.EXTRA_STREAM);
            if (extraStream instanceof Uri) {
                String path = ((Uri) extraStream).getPath();
                File file = new File(path);
                String dirPath = file.isDirectory() ? path : file.getParent();
                openNewWindow("cd " + quoteForBash(dirPath));
            }
        } else {
            // Intent sender may not have permissions, ignore any extras
            openNewWindow(null);
        }

        finish();
    }

    /**
     *  Quote a string so it can be used as a parameter in bash and similar shells.
     */
    public static String quoteForBash(String s) {
        StringBuilder builder = new StringBuilder();
        String specialChars = "\"\\$`!";
        builder.append('"');
        int length = s.length();
        for (int i = 0; i < length; i++) {
            char c = s.charAt(i);
            if (specialChars.indexOf(c) >= 0) {
                builder.append('\\');
            }
            builder.append(c);
        }
        builder.append('"');
        return builder.toString();
    }

    protected String openNewWindow(String iInitialCommand) {
        TerminalDebianService service = getTermService();

        String initialCommand = mSettings.getInitialCommand();
        if (iInitialCommand != null) {
            //CCX added second part of condition (.equals("")...)
            if ((initialCommand != null) && (initialCommand.equals("") == false)) {
                initialCommand += "\r" + iInitialCommand;
            } else {
                initialCommand = iInitialCommand;
            }
        }

        try {
            TermSession session = TerminalDebian.createTermSession(this, mSettings, initialCommand);

            session.setFinishCallback(service);
            service.getSessions().add(session);

            String handle = UUID.randomUUID().toString();
            ((GenericTermSession) session).setHandle(handle);

            Intent intent = new Intent(AppConfig.DEBIAN_OPEN_NEW_WINDOW);
            intent.addCategory(Intent.CATEGORY_DEFAULT);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);

            return handle;
        } catch (IOException e) {
            return null;
        }
    }

    protected String appendToWindow(String handle, String iInitialCommand) {
        TerminalDebianService service = getTermService();

        // Find the target window
        SessionList sessions = service.getSessions();
        GenericTermSession target = null;
        int index;
        for (index = 0; index < sessions.size(); ++index) {
            GenericTermSession session = (GenericTermSession) sessions.get(index);
            String h = session.getHandle();
            if (h != null && h.equals(handle)) {
                target = session;
                break;
            }
        }

        if (target == null) {
            // Target window not found, open a new one
            return openNewWindow(iInitialCommand);
        }

        if (iInitialCommand != null) {
            target.write(iInitialCommand);
            target.write('\r');
        }

        Intent intent = new Intent(AppConfig.DEBIAN_SWITCH_WINDOW);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(AppConfig.DEBIAN_EXTRA_TARGET_WINDOW, index);
        startActivity(intent);

        return handle;
    }
}
