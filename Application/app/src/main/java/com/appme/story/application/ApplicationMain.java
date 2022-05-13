package com.appme.story.application;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.database.ContentObserver;
import android.os.Handler;
import android.util.Log;

public class ApplicationMain extends Application {

    //public ApplicationChangeReceiver mModel;
    public static Context mContext;

    @Override
    public void onCreate() {
        Log.d(ApplicationMain.class.getSimpleName(), "Enter");
        super.onCreate();

        mContext = this;

        initConfig();
        initCrashHandler();
    }



    public void initConfig(){}
    
    public void initCrashHandler(){}
    public static Context getContext() {
        return mContext;
    }

}
