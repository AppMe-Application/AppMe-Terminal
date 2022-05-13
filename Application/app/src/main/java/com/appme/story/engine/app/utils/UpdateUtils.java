package com.appme.story.engine.app.utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;

import android.content.Intent;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.util.Log;


public class UpdateUtils {

    public static final String TAG = UpdateUtils.class.getSimpleName();

    public static String getProgressDisplayLine(long currentBytes, long totalBytes) {
        return getBytesToMBString(currentBytes) + "/" + getBytesToMBString(totalBytes);
    }

    private static String getBytesToMBString(long bytes){
        return String.format(Locale.ENGLISH, "%.2fMb", bytes / (1024.00 * 1024.00));
    }
    
    public static void deleteContentsOfDir(File dir) {
        if (dir == null || !dir.exists())
            return;
        File[] files = dir.listFiles();
        if (files != null && files.length > 0) {
            for (int i = 0; i < files.length; i++) {
                deleteDirIncludeSelf(files[i]);
            }
        }
    }

    public static void deleteDirIncludeSelf(File dir) {
        if (dir == null || !dir.exists()) return;
        if (dir.isDirectory()) {
            File[] files = dir.listFiles();
            for (File file : files)
                if (file.isDirectory()) deleteDirIncludeSelf(file);
                else file.delete();
            dir.delete();
        } else dir.delete();
    }


    public static void closeInputStream(InputStream is) {
        if (is == null) {
            return;
        }
        try {
            is.close();
            is = null;
        } catch (IOException e) {
            Log.i(TAG, e.getMessage());
        }
    }

    public static void closeOutputStream(OutputStream os) {
        if (os == null) {
            return;
        }
        try {
            os.close();
            os = null;
        } catch (IOException e) {
            Log.i(TAG, e.getMessage());
        }
    }

    public static String md5s(String plainText) {
        String str = "";
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(plainText.getBytes());
            byte b[] = md.digest();

            int i;

            StringBuffer buf = new StringBuffer("");
            for (int offset = 0; offset < b.length; offset++) {
                i = b[offset];
                if (i < 0) {
                    i += 256;
                }
                if (i < 16) {
                    buf.append("0");
                }
                buf.append(Integer.toHexString(i));
            }
            str = buf.toString().substring(8, 24);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();

        }
        return str;
    }
}
