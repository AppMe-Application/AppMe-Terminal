package com.appme.story.engine.app.analytics.helpers;

import android.os.Build;
import android.util.Log;
import com.appme.story.AppConfig;

public class CpuArchHelper {
    
    public static final String TAG = CpuArchHelper.class.getSimpleName();
    public static CpuArch getCpuArch() {
        Log.d(TAG, "Build.CPU_ABI : " + Build.CPU_ABI);
        // check if device is x86 or x86_64
        if (Build.CPU_ABI.equals(getx86CpuAbi()) || Build.CPU_ABI.equals(getx86_64CpuAbi())) {
            return CpuArch.x86;
        } else {
            // check if device is armeabi
            if (Build.CPU_ABI.equals(getArmeabiv7CpuAbi())) {
                AppConfig cpuNativeArchHelper = new AppConfig();
                String archInfo = cpuNativeArchHelper.cpuArchFromJNI();
                // check if device is arm v7
                if (cpuNativeArchHelper.isARM_v7_CPU(archInfo)) {
                    // check if device is neon
                    return CpuArch.ARMv7;
                }
                // check if device is arm64 which is supported by ARMV7
            } else if (Build.CPU_ABI.equals(getArm64CpuAbi())) {
                return CpuArch.ARMv7;
            }
        }
        return CpuArch.NONE;
    }
    
    public static String getx86CpuAbi() {
        return "x86";
    }
    
    public static String getx86_64CpuAbi() {
        return "x86_64";
    }
    
    public static String getArm64CpuAbi() {
        return "arm64-v8a";
    }

    public static String getArmeabiv7CpuAbi() {
        return "armeabi-v7a";
    }
}
