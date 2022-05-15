package com.appme.story;

import android.Manifest;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.PackageStats;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.AsyncTask;
import android.util.Pair;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.Calendar;

import com.appme.story.application.TerminalActivity;
import com.appme.story.application.DebianActivity;
import com.appme.story.engine.app.analytics.Analytics;
import com.appme.story.engine.app.terminal.command.ShellResultUtils;
import com.appme.story.settings.ShellResultPreference;
import com.appme.story.receiver.RemoteService;
import com.appme.story.service.TerminalService;
import com.appme.story.service.TerminalDebianService;

public class SplashActivity extends AppCompatActivity {


    public static final String TAG = SplashActivity.class.getSimpleName();
    public static final String ACTION_RESTART = "ACTION_RESTART";
    public final static String ACTION_TERMINAL_ACTIVITY = "TERMINAL_ACTIVITY";

    private ImageView mAppIcon;
    private TextView mAppName;
    private TextView mAppMessage;
    private TextView mCopyRight;

    private PackageManager packageManager;
    private PackageInfo packageInfo;
    private File appFile;

    public static void start(Context c) {
        Intent mIntent = new Intent(c, SplashActivity.class);
        mIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        c.startActivity(mIntent);
    }

    public static void restart(Context c, String action) {
        Intent mIntent = new Intent(c, SplashActivity.class);
        mIntent.setAction(action);
        mIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        c.startActivity(mIntent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {	
	    setTheme(R.style.AppTheme_Application);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        packageManager = getPackageManager();
        try {
            packageInfo = packageManager.getPackageInfo(getPackageName(), PackageManager.GET_PERMISSIONS);
            appFile = new File(packageInfo.applicationInfo.sourceDir);
            mAppIcon = (ImageView) findViewById(R.id.splash_app_icon);
            mAppIcon.setImageDrawable(packageInfo.applicationInfo.loadIcon(packageManager));

            mAppName = (TextView) findViewById(R.id.splash_app_title);
            mAppName.setText(packageInfo.applicationInfo.loadLabel(packageManager).toString());

            mAppMessage = (TextView) findViewById(R.id.splash_app_message);
            mAppMessage.setText(getString(R.string.app_welcome) + packageInfo.applicationInfo.loadLabel(packageManager));

            mCopyRight = (TextView) findViewById(R.id.app_copy_right);
            final String copyrights = String.format(getString(R.string.app_copy_right), Calendar.getInstance().get(Calendar.YEAR));
            mCopyRight.setText(copyrights);
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        } 
        Analytics.with(this).setDeviceAnalytics();
        String action = getIntent().getAction();        
        if (action != null && action.equals(ACTION_TERMINAL_ACTIVITY)) {
            setTerminal();
        } 

        setTerminal();
    }

    public void setTerminal(){
        Analytics.with(this).setAnalyticsTerminal(SplashActivity.this, new Analytics.OnTerminalListener(){
                @Override
                public void onAndroid() {              
                    startMainActivity(TerminalActivity.class);
                }

                @Override
                public void onDebian() {                
                    startMainActivity(DebianActivity.class);
                }
            });     
    }
    /**
     * If receive data from other app (it could be file, text from clipboard),
     * You will be handle data and send to {@link MainActivity}
     */
    private void startMainActivity(final Class<?> mClass) {
		//if (!ShellResultUtils.updateTelnet(this)) return;
        new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    Intent intent = new Intent(SplashActivity.this, mClass);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                    overridePendingTransition(0, 0);
                    startActivity(intent);
                    finish();
                }
            }, 400);
    }

}
