package com.appme.story.engine.app.folders;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.util.Arrays;
import java.util.Collection;

import com.appme.story.R;
import com.appme.story.AppController;
import com.appme.story.engine.app.analytics.Analytics;

public class FileMe {
    
    private static String TAG = FileMe.class.getSimpleName();
    private static volatile FileMe sInstance = null;
    private Context context;
    
    private FileMe(Context context) {
        this.context = context; 
    }
    
    public static FileMe with(Context context) {
        return new FileMe(context);
    }

    public static FileMe get() {
        FileMe localInstance = sInstance;
        if (localInstance == null) {
            synchronized (FileMe.class) {
                localInstance = sInstance;
                if (localInstance == null) {
                    sInstance = localInstance = new FileMe(AppController.getContext());
                }
            }
        }
        return localInstance;
    }

    public static Context getContext() {
        return AppController.getContext();
    }
    
    public static String getDebianFile(){    
        return "main." + Integer.toString(Analytics.getObbVersion()) + ".com.gnuroot.debian.obb";
    }
    
    public static final char[] ILLEGAL_FILENAME_CHARS = {34, 60, 62, 124, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13,
        14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 58, 42, 63, 92, 47};

    public String readFile(File file) throws IOException {
        return readFile(file, "UTF-8");
    }

    public String readFile(File file, String encoding) throws IOException {
        FileInputStream is = new FileInputStream(file);
        String text = readFile(is, encoding);
        is.close();

        return text;
    }

    public String readFile(InputStream is, String encoding) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(is, encoding));
        char[] buf = new char[8192];
        int size;
        StringBuilder sb = new StringBuilder(buf.length * 4);
        while ((size = br.read(buf)) != -1) {
            sb.append(buf, 0, size);
        }
        return sb.toString();
    }

    public boolean writeFile(File file, String text) {
        try {
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file));
            bufferedWriter.write(text);
            bufferedWriter.flush();
            bufferedWriter.close();
            return true;
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
            file.delete();
            return false;
        }
    }

    public boolean isInvalidFilename(String fileName) {
        if (TextUtils.isEmpty(fileName))
            return false;

        int size = fileName.length();
        char c;
        for (int i = 0; i < size; i++) {
            c = fileName.charAt(i);
            if (Arrays.binarySearch(ILLEGAL_FILENAME_CHARS, c) >= 0)
                return true;
        }

        return false;
    }

    public String getFileName(String inFilePath) {
        if (TextUtils.isEmpty(inFilePath))
            return "";

        return inFilePath.substring(inFilePath.lastIndexOf('/') + 1);
    }

    public String getFilePath(String inFilePath) {
        if (TextUtils.isEmpty(inFilePath)){
            return "";
        }
        String filePath = inFilePath.substring(0, inFilePath.lastIndexOf("/"));
        return filePath;
    }
    
    public String toString(InputStream inputStream) throws IOException {
        return toString(inputStream, 16*1024, "UTF-8");
    }

    public String toString(InputStream inputStream, final int bufferSize, String encoding) throws IOException {
        final char[] buffer = new char[bufferSize];
        final StringBuilder out = new StringBuilder();

        Reader in = new InputStreamReader(inputStream, encoding);
        for (; ; ) {
            int rsz = in.read(buffer, 0, buffer.length);
            if (rsz < 0)
                break;
            out.append(buffer, 0, rsz);
        }

        return out.toString();
    }

    public boolean isBinaryFile(File f) {
        FileInputStream in = null;
        try {
            in = new FileInputStream(f);
        } catch (FileNotFoundException e) {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            return false;
        }
        byte[] data = null;
        try {
            int size = in.available();
            if(size > 1024) size = 1024;
            data = new byte[size];
            in.read(data);
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
            try {
                in.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            return false;
        }

        for (byte b : data) {
            if (b < 0x09) return true;
        }

        return false;
    }

    public static boolean copyFile(File oldLocation, File newLocation) {
        if (!oldLocation.exists()) {
            return false;
        }

        try {
            return copyFile(new FileInputStream(oldLocation), new FileOutputStream(newLocation, false));
        } catch (Exception ex) {
            Log.e(TAG, ex.getMessage());
            return false;
        }
    }

    public static boolean copyFile(InputStream inputStream, OutputStream outputStream) {

        BufferedInputStream reader = null;
        BufferedOutputStream writer = null;
        try {
            reader = new BufferedInputStream(inputStream);
            writer = new BufferedOutputStream(outputStream);

            byte[] buff = new byte[8192];
            int numChars;
            while ((numChars = reader.read(buff, 0, buff.length)) != -1) {
                writer.write(buff, 0, numChars);
            }

            return true;
        } catch (Exception ex) {
            return false;
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException ex) {
                Log.d(TAG, ex.getMessage());
            }
            try {
                if (writer != null) {
                    writer.close();
                }
            } catch (IOException ex) {
                Log.d(TAG, ex.getMessage());
            }
        }
    }
    
    public static File installBinary(Context context, String targetFile, String targetFolder) {
        File sbinDir = new File(FolderMe.getDataDir(context), targetFolder);
        if (!sbinDir.exists()) {
            
            try {
                sbinDir.mkdir();
                chmod("755", sbinDir.getAbsolutePath());
            } catch (Exception e) {
            }
        }

        File ls = new File(sbinDir, targetFile);
        if (!ls.exists()) {
            boolean replaceStrings = false;
            
            try {
                InputStream src = context.getAssets().open(targetFile);
                if (targetFile.contains(".replace.mp2")) {
                    replaceStrings = true;
                }
                targetFile = targetFile.replace(".replace.mp2", "").replace(".mp2", "").replace(".mp3", ".tar.gz");
                File outFile = new File(sbinDir, targetFile);    
                FileOutputStream dst = new FileOutputStream(outFile);
                copyStream(dst, src);
                if (replaceStrings) {
                   // replaceScriptStrings(outFile);
                }
                chmod("755", ls.getAbsolutePath());
            } catch (Exception e) {
            }
        }

        return sbinDir;
    }

    public static void copyStream(OutputStream dst, InputStream src) throws IOException {
        byte[] buffer = new byte[4096];
        int bytesRead = 0;
        while ((bytesRead = src.read(buffer)) >= 0) {
            dst.write(buffer, 0, bytesRead);
        }
        dst.close();
    }

    public static void validateCopyMoveDirectory(File file, File toFolder) throws IOException
    {
        if (toFolder.equals(file))
            throw new IOException("Folder cannot be copied to itself");
        else if (toFolder.equals(file.getParentFile()))
            throw new IOException("Source and target directory are the same");
        else if (toFolder.getAbsolutePath().startsWith(file.getAbsolutePath()))
            throw new IOException("Folder cannot be copied to its child folder");
    }

    
    public static int deleteFiles(Collection<File> files)
    {
        int n=0;
        for (File file : files)
        {
            if (file.isDirectory())
            {
                n += deleteFiles(Arrays.asList(file.listFiles()));
            }
            if (file.delete()) n++;
        }
        return n;
	}
    
    public static void chmod(String... args) throws IOException {
        String[] cmdline = new String[args.length + 1];
        cmdline[0] = "/system/bin/chmod";
        System.arraycopy(args, 0, cmdline, 1, args.length);
        new ProcessBuilder(cmdline).start();
    }
    
    public static int countFilesIn(Collection<File> roots)
    {
        int result=0;
        for (File file : roots)
            result += countFilesIn(file);
        return result;
    }

    public static int countFilesIn(File root)
    {
        if (root.isDirectory() == false) return 1;
        File[] files = root.listFiles();
        if (files == null) return 0;

        int n = 0;

        for (File file : files)
        {
            if (file.isDirectory())
                n += countFilesIn(file);
            else
                n ++;
        }
        return n;
    }
	
    
}
