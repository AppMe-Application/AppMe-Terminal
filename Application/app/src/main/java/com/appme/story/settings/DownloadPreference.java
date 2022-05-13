package com.appme.story.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.preference.PreferenceManager;
import static android.content.Context.MODE_PRIVATE;

import com.appme.story.AppController;

public class DownloadPreference {
    
    private static volatile DownloadPreference Instance = null;
    private Context context;
    public static final String SD_CARD_ROOT = Environment.getExternalStorageDirectory().getAbsolutePath();
    public static final String RECORD = "com.appme.story.record";
    public static final String SHARED_PREF_DATA_SET_CHANGED = "com.appme.story.datasetchanged";
    public static final String CHANGE_OCCURED = "com.appme.story.changeoccured";
    
    public static DownloadPreference get() {
        DownloadPreference localInstance = Instance;
        if (localInstance == null) {
            synchronized (DownloadPreference.class) {
                localInstance = Instance;
                if (localInstance == null) {
                    Instance = localInstance = new DownloadPreference(AppController.getAppController().getApplicationContext());
                }
            }
        }
        return localInstance;
    }

    private DownloadPreference(Context context) {
        this.context = context;
    }

    public static DownloadPreference with(Context context) {
        return new DownloadPreference(context);
    }
    // Getter Methods

    private SharedPreferences getPrefs() {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }

    private SharedPreferences.Editor getEditor() {
        return getPrefs().edit();
    }
    
    public boolean IsDownloadRunning() {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREF_DATA_SET_CHANGED, MODE_PRIVATE);
        return sharedPreferences.getBoolean(CHANGE_OCCURED, false);
    }
    
    public String getDownloadFolder() {
        return getPrefs().getString("download_folder", context.getExternalFilesDir("record").getAbsolutePath());
    }
    
    public String getWorkingFolder() {
        return getPrefs().getString("working_folder", SD_CARD_ROOT);
    }
    
    public void setIsDownloadRunning(boolean value) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREF_DATA_SET_CHANGED, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(CHANGE_OCCURED, value);
        editor.apply();
    }
    
    public void setDownloadFolder(String value) {
        getEditor().putString("download_folder", value).commit();
    }
    
    public void setWorkingFolder(Context context, String value) {
        getEditor().putString("working_folder", value).commit();
    }
    
}
