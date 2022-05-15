package com.appme.story.engine.app.analytics;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;
import android.widget.TextView;

import org.apache.commons.io.FileUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;

import com.appme.story.R;
import com.appme.story.AppConfig;
import com.appme.story.AppController;
import com.appme.story.engine.app.analytics.helpers.CpuArchHelper;
import com.appme.story.engine.app.folders.FolderMe;
import com.appme.story.engine.app.tasks.CheckRootTask;
import com.appme.story.settings.ShellResultPreference;

public class Analytics {

    private static final String TAG = Analytics.class.getSimpleName();
    private Context context;
    private SharedPreferences mSharedPreference;
    /** An intent for launching the system settings. */
    //private static final Intent sSettingsIntent = new Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS);
    public static final String SHARED_PREF_DATA_SET_CHANGED = "com.appme.story.setTerminal";
    public static final String CHANGE_TERMINAL = "com.appme.story.changeTerminal";
    
    private static final String deviceFile = "device_info.json";
    public static String getAnalyticFolder() {
        return AppController.getContext().getExternalFilesDir("analytics").getAbsolutePath();
    }

    private OnNetworkState mOnNetworkState;

    private Analytics(Context context) {
        this.context = context;
        mSharedPreference = PreferenceManager.getDefaultSharedPreferences(context);

        FolderMe.with(context).initFolderMe();
    }

    public static Analytics with(Context context) {
        return new Analytics(context);
    }

    public static void setDeviceAnalytics() {
        try {
            File dlCacheFile = new File(getAnalyticFolder(), deviceFile);
            JSONObject json = new JSONObject();
            
            String cpuArchNameFromAssets = null;
            String obbUrl = null;
            int obbVersion = 0;
            switch (CpuArchHelper.getCpuArch()) {
                case x86:
                    Log.i(CpuArchHelper.TAG, "Execute Binary for x86 CPU");
                    cpuArchNameFromAssets = "x86";
                    obbVersion = 11;
                    obbUrl = AppConfig.OBB_DEBIAN_i386;
                    break;
                case ARMv7:
                    Log.i(CpuArchHelper.TAG, "Execute Binary for armv7 CPU");
                    cpuArchNameFromAssets = "armeabi-v7a";
                    obbUrl = AppConfig.OBB_DEBIAN_ARMHF;
                    obbVersion = 10;
                    break;
                case NONE:
                    throw new RuntimeException("Device Not Supported");
            }
            if (!TextUtils.isEmpty(cpuArchNameFromAssets)) {
                json.put("device_manufacturer", Build.MANUFACTURER);
                json.put("device_model", Build.MODEL);
                json.put("device_arch", cpuArchNameFromAssets);
                json.put("android_version ", Build.VERSION.RELEASE);
                json.put("android_sdk", Build.VERSION.SDK_INT);
                json.put("obb_version", obbVersion);
                json.put("obb_download_url", obbUrl);
            } else {
                throw new RuntimeException("Device Not Supported");
            }

            try {
                dlCacheFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            FileUtils.writeStringToFile(dlCacheFile, json.toString());
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
    }
    
    public Analytics setAnalyticsActivity(OnFirstTimeListener mOnFirstTimeListener) {
        /**** START APP ****/
        boolean isFirstStart = mSharedPreference.getBoolean("firstStart", true);
        if (isFirstStart) {
            SharedPreferences.Editor e = mSharedPreference.edit();
            e.putBoolean("firstStart", false);
            e.apply();
            if (mOnFirstTimeListener != null) {
                mOnFirstTimeListener.onFirsTime();
            }
        } else {
            if (mOnFirstTimeListener != null) {
                mOnFirstTimeListener.onSecondTime();
            }
        }
        return this;
    }

    public Analytics setAnalyticsTerminal(Context c, OnTerminalListener mOnTerminalListener) {
        if (IsTerminal(c)) {
            if (mOnTerminalListener != null) {
                mOnTerminalListener.onAndroid();
            }
        } else {
            if (mOnTerminalListener != null) {
                mOnTerminalListener.onDebian();
            }
        }
        return this;
    }
    
    public void setTerminal(boolean isTerminal){
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREF_DATA_SET_CHANGED, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(CHANGE_TERMINAL, isTerminal);
        editor.apply();
    }
    
    public static boolean IsTerminal(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREF_DATA_SET_CHANGED, Context.MODE_PRIVATE);
        return sharedPreferences.getBoolean(CHANGE_TERMINAL, true);
    }
    
    /*public void checkNetwork(final TextView tv) {
     if (!NetworkStateUtil.getNetworkStatus(context)) { 
     if(mOnNetworkState != null){
     mOnNetworkState.onNetworkConnected();
     }
     //ToastUtils.show(context, "Network Is Connected");
     return;
     }else{
     if(mOnNetworkState != null){
     mOnNetworkState.onNetworkDisConnected();
     }
     //ToastUtils.show(context, "Network.Is DisConnected");            
     }
     }

     public void setServerIP()
     {
     try
     {
     JSONObject json = new JSONObject();
     json.put("title", "IP Address Analytics");
     json.put("ip_address", AppController.getServerIP());
     String serverJson = FolderMe.getServerIP();
     File serverIP = new File(serverJson);
     serverIP.getParentFile().mkdirs();
     FileUtils.writeStringToFile(serverIP, json.toString());
     }
     catch (IOException | JSONException e)
     {
     e.printStackTrace();
     }
     }*/

    public String getServerIP() {
        try {
            String serverJson = FolderMe.getServerIP();           
            File serverIP = new File(serverJson);
            JSONObject json = new JSONObject(FileUtils.readFileToString(serverIP));
            return json.getString("ip_address");
        } catch (IOException | JSONException e) {
            return null;
        }
    } 

    public static String getArch() {
        try {
            File infoFile = new File(getAnalyticFolder(), deviceFile);
            JSONObject json = new JSONObject(FileUtils.readFileToString(infoFile));
            return json.getString("device_arch");
        } catch (IOException | JSONException e) {
            return null;
        }    
	}

    public static String getManufacturer() {
        try {
            File infoFile = new File(getAnalyticFolder(), deviceFile);
            JSONObject json = new JSONObject(FileUtils.readFileToString(infoFile));
            return json.getString("device_manufacturer");
        } catch (IOException | JSONException e) {
            return null;
        }    
	}

    public static String getDeviceModel() {
        try {
            File infoFile = new File(getAnalyticFolder(), deviceFile);
            JSONObject json = new JSONObject(FileUtils.readFileToString(infoFile));
            return json.getString("device_model");
        } catch (IOException | JSONException e) {
            return null;
        }    
	}

    public static String getAndroidVersion() {
        try {
            File infoFile = new File(getAnalyticFolder(), deviceFile);
            JSONObject json = new JSONObject(FileUtils.readFileToString(infoFile));
            return json.getString("android_version");
        } catch (IOException | JSONException e) {
            return null;
        }    
	}

    public static String getAndroidSdk() {
        try {
            File infoFile = new File(getAnalyticFolder(), deviceFile);
            JSONObject json = new JSONObject(FileUtils.readFileToString(infoFile));
            return json.getString("android_sdk");
        } catch (IOException | JSONException e) {
            return null;
        }    
	}
    
    public static int getObbVersion() {
        try {
            File infoFile = new File(getAnalyticFolder(), deviceFile);
            JSONObject json = new JSONObject(FileUtils.readFileToString(infoFile));
            return json.getInt("obb_version");
        } catch (IOException | JSONException e) {
            return 0;
        }    
	}
    
    public static String getObbDownloadUrl() {
        try {
            File infoFile = new File(getAnalyticFolder(), deviceFile);
            JSONObject json = new JSONObject(FileUtils.readFileToString(infoFile));
            return json.getString("obb_download_url");
        } catch (IOException | JSONException e) {
            return null;
        }    
	}

    public void setOnNetworkStatusListener(OnNetworkState mOnNetworkState) {
        this.mOnNetworkState = mOnNetworkState;
    }

    public interface onRootCheckerListener {
        void onStart(String message);
        void onProgress(String value);
        void onFinish(String result);
    }

    public interface OnFirstTimeListener {
        void onFirsTime();
        void onSecondTime();
    }
    
    public interface OnTerminalListener {
        void onAndroid();
        void onDebian();
    }

    public interface OnNetworkState {
        void onNetworkConnected();
        void onNetworkDisConnected();
    }
}
