package com.appme.story.engine.app.utils;

import android.content.Context;
import android.content.pm.PackageManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.ProcessBuilder;
import com.appme.story.settings.ShellResultPreference;

public class IOUtils {
	
	public static File setupBinDir(Context context) {
         File binDir = new File(ShellResultPreference.getDefaultDir(context), "bin");
        if (!binDir.exists()) {
            try {
                binDir.mkdir();
                chmod("755", binDir.getAbsolutePath());
            } catch (Exception e) {
            }
        }

        File hello = new File(binDir, "vim");
        if (!hello.exists()) {
            try {
                InputStream src = context.getAssets().open("vim");
                FileOutputStream dst = new FileOutputStream(hello);
                copyStream(dst, src);
                chmod("755", hello.getAbsolutePath());
            } catch (Exception e) {
            }
        }

        return binDir;
    }
	
	private static void copyStream(OutputStream dst, InputStream src) throws IOException {
        byte[] buffer = new byte[4096];
        int bytesRead = 0;
        while ((bytesRead = src.read(buffer)) >= 0) {
            dst.write(buffer, 0, bytesRead);
        }
        dst.close();
    }
	
    public static void chmod(String... args) throws IOException {
        String[] cmdline = new String[args.length + 1];
        cmdline[0] = "/system/bin/chmod";
        System.arraycopy(args, 0, cmdline, 1, args.length);
        new ProcessBuilder(cmdline).start();
    }
    
	public static String getDataDir(Context context) {
        /* On API 4 and later, you can just do this */
        // return context.getApplicationInfo().dataDir;

        String packageName = context.getPackageName();
        PackageManager pm = context.getPackageManager();
        String dataDir = null;
        try {
            dataDir = pm.getApplicationInfo(packageName, 0).dataDir;
        } catch (Exception e) {
            // Won't happen -- we know we're installed
        }
        return dataDir;
    }
}
