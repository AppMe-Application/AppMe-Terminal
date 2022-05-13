package com.appme.story;

import android.support.annotation.NonNull;
import android.content.Context;

import com.appme.story.application.ApplicationMain;
import com.appme.story.engine.app.analytics.CrashHandler;
import com.appme.story.engine.app.commons.downloader.PRDownloader;
import com.appme.story.engine.app.commons.downloader.PRDownloaderConfig;
import com.appme.story.settings.PreferenceManager;

public class AppController extends ApplicationMain {
    
    private static AppController isInctance;
    //private static Bus mBus = null;
    private static PreferenceManager mPreferenceManager = null;
    
	@Override
	public void onCreate() {	
		super.onCreate();
        isInctance = this;
        mContext = this;
             
	}

    @Override
    public void initConfig() {
        super.initConfig();
        PRDownloaderConfig config = PRDownloaderConfig.newBuilder()
            .setDatabaseEnabled(true)
            .build();
        PRDownloader.initialize(this, config);
    }


    @Override
    public void initCrashHandler() {
        super.initCrashHandler();
        CrashHandler.init(this);        
    }
    
    @NonNull
    public static AppController get(@NonNull Context context) {
        return (AppController) context.getApplicationContext();
    }

    public static AppController getAppController(){
        return isInctance;
    }
    
    public static AppController getInstance(){
        return isInctance;
    }
    
   /* public static Bus getBus(@NonNull Context context) {
        if (mBus == null)
            mBus = new Bus();
            
        return mBus;
    }*/
    
    public static PreferenceManager getPreferenceManager()
    {
        if (mPreferenceManager == null)
            mPreferenceManager = new PreferenceManager(getAppController().getApplicationContext());
        return mPreferenceManager;
    }
}
