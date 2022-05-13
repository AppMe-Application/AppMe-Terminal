package com.appme.story.engine.app.tasks;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.TextView;

import com.appme.story.engine.app.analytics.RootChecker;
import com.appme.story.engine.app.analytics.helpers.Utils;

import java.util.ArrayList;


/**
 * class to pretend we are doing some really clever stuff that takes time
 * <p/>
 * Old skool Async - this could been nicer but just threw together at the mo
 */
public class CheckRootTask extends AsyncTask<Boolean, String, Boolean> {

    private static final int SLEEP_TIME = 150;
    private static final String TAG = "CheckRootTask";
    private final Context mContext;
    //private ArrayList<String> mCheckRootList;
    private TextView mTextView;
    private OnCheckRootFinishedListener mListener;
    //private Drawable redCross;
    //private Drawable greenTick;
    private boolean mIsCheck;

    public interface OnCheckRootFinishedListener {
        void onCheckRootFinished(boolean isRooted);
    }

    public CheckRootTask(Context ctx, TextView checkRoot, OnCheckRootFinishedListener listener) {
        mListener = listener;     
        mContext = ctx;
        mTextView = checkRoot;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        mTextView.setText("Check Rooted");
    }

    @Override
    protected void onProgressUpdate(String... values) {
        super.onProgressUpdate(values);
        String value = values[0];
        
        mTextView.setText(value);
        mIsCheck = false;
    }

    @Override
    protected Boolean doInBackground(Boolean... params) {
        RootChecker check = new RootChecker(mContext);
        check.setLogging(true);
        String message = "";
        for (int i = 0; i < 90; i++) {
            try {
                Thread.sleep(SLEEP_TIME);
            } catch (InterruptedException e) {

            }
            switch (i) {
                case 8:
                    mIsCheck = check.detectRootManagementApps();
                    message = "Root Management Apps " + (mIsCheck ? "detected" : "not detected");
                    Log.d(TAG, "Root Management Apps " + (mIsCheck ? "detected" : "not detected"));
                    break;
                case 16:
                    mIsCheck = check.detectPotentiallyDangerousApps();
                    message = "PotentiallyDangerousApps " + (mIsCheck ? "detected" : "not detected");
                    Log.d(TAG, "PotentiallyDangerousApps " + (mIsCheck ? "detected" : "not detected"));
                    break;
                case 24:
                    mIsCheck = check.detectTestKeys();
                    message = "TestKeys " + (mIsCheck ? "detected" : "not detected");
                    
                    Log.d(TAG, "TestKeys " + (mIsCheck ? "detected" : "not detected"));
                    break;
                case 32:
                    mIsCheck = check.checkForBusyBoxBinary();
                    message = "BusyBoxBinary " + (mIsCheck ? "detected" : "not detected");
                    
                    Log.d(TAG, "BusyBoxBinary " + (mIsCheck ? "detected" : "not detected"));
                    break;
                case 40:
                    mIsCheck = check.checkForSuBinary();
                    message = "SU Binary " + (mIsCheck ? "detected" : "not detected");
                    
                    Log.d(TAG, "SU Binary " + (mIsCheck ? "detected" : "not detected"));
                    break;
                case 48:
                    mIsCheck = check.checkSuExists();
                    message = "2nd SU Binary check " + (mIsCheck ? "detected" : "not detected");
                    
                    Log.d(TAG, "2nd SU Binary check " + (mIsCheck ? "detected" : "not detected"));
                    break;
                case 56:
                    mIsCheck = check.checkForRWPaths();
                    message = "ForRWPaths " + (mIsCheck ? "detected" : "not detected");
                    
                    Log.d(TAG, "ForRWPaths " + (mIsCheck ? "detected" : "not detected"));
                    break;
                case 64:
                    mIsCheck = check.checkForDangerousProps();
                    message = "DangerousProps " + (mIsCheck ? "detected" : "not detected");
                    
                    Log.d(TAG, "DangerousProps " + (mIsCheck ? "detected" : "not detected"));
                    break;
                case 72:
                    mIsCheck = check.checkForRootNative();
                    message = "Root via native check " + (mIsCheck ? "detected" : "not detected");
                    
                    Log.d(TAG, "Root via native check " + (mIsCheck ? "detected" : "not detected"));
                    break;
                case 80:
                    mIsCheck = check.detectRootCloakingApps();
                    message = "ForRWPaths " + (mIsCheck ? "detected" : "not detected");
                    
                    Log.d(TAG, "ForRWPaths " + (mIsCheck ? "detected" : "not detected"));
                    break;
                case 88:
                    mIsCheck = Utils.isSelinuxFlagInEnabled();
                    message = "Selinux Flag Is Enabled " + (mIsCheck ? "detected" : "not detected");
                    
                    Log.d(TAG, "Selinux Flag Is Enabled " + (mIsCheck ? "true" : "false"));
                    break;
                case 89:
                    mIsCheck = check.checkForMagiskBinary();
                    message = "Magisk " + (mIsCheck ? "detected" : "not detected");
                    
                    Log.d(TAG, "Magisk " + (mIsCheck ? "deteced" : "not deteced"));
                    break;
            }
            publishProgress(message);
        }
        return check.isRooted();
    }

    @Override
    protected void onPostExecute(Boolean isRooted) {
        super.onPostExecute(isRooted);
        mListener.onCheckRootFinished(isRooted);
    }

}
