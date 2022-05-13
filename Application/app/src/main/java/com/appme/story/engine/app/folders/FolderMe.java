package com.appme.story.engine.app.folders;

import android.support.annotation.Nullable;
import android.support.annotation.NonNull;
import android.support.v4.provider.DocumentFile;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.CountDownTimer;
import android.os.storage.StorageManager;
import android.text.TextUtils;
import android.content.pm.PackageManager;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.appme.story.R;
import com.appme.story.AppController;
import com.appme.story.engine.app.analytics.Analytics;

public class FolderMe {


    private static volatile FolderMe sInstance = null;
    private Context context;
    
    /** Note that this is a symlink on the Android M preview. */
    @SuppressLint("SdCardPath")
    public static String EXTERNAL_DIR = getExternalStorageDirectory();
    public static String FOLDER = getFolderMe();
    //Folder For PackageArchive
    public static String FOLDER_APK = FOLDER + "/1.Apk";
    public static String FOLDER_APK_BACKUP = FOLDER_APK + "/Backup";
    public static String FOLDER_APK_DOWNLOAD = FOLDER_APK + "/Download";

    //Folder For Image
    public static String FOLDER_IMAGE = FOLDER + "/2.Image";
    public static String FOLDER_IMAGE_CONVERT = FOLDER_IMAGE + "/Convert";
    public static String FOLDER_IMAGE_DOWNLOAD = FOLDER_IMAGE + "/Download";
    public static String FOLDER_IMAGE_PROJECTS = FOLDER_IMAGE + "/Projects";
    
    //Folder For Audio
    public static String FOLDER_AUDIO = FOLDER + "/3.Audio";
    public static String FOLDER_AUDIO_RECORDER = FOLDER_AUDIO + "/Recorder";
    public static String FOLDER_AUDIO_DOWNLOAD = FOLDER_AUDIO + "/Download";
    public static String FOLDER_AUDIO_CONVERT = FOLDER_AUDIO + "/Convert";

    //Folder For Video
    public static String FOLDER_VIDEO = FOLDER + "/4.Video";
    public static String FOLDER_VIDEO_RECORDER = FOLDER_VIDEO + "/Recorder";
    public static String FOLDER_VIDEO_DOWNLOAD = FOLDER_VIDEO + "/Download";
    public static String FOLDER_VIDEO_PROJECTS = FOLDER_VIDEO + "/Projects";

    //Folder For YouTube
    public static String FOLDER_YOUTUBE = FOLDER_VIDEO + "/Youtube";
    public static String FOLDER_YOUTUBE_ANALYTICS = FOLDER_YOUTUBE + "/Analytics";
    public static String FOLDER_YOUTUBE_DOWNLOAD = FOLDER_YOUTUBE + "/Download";

    //Folder For Ebook
    public static String FOLDER_EBOOK = FOLDER + "/5.Ebook";
    public static String FOLDER_EBOOK_PROJECTS = FOLDER_EBOOK + "/Projects";
    public static String FOLDER_EBOOK_DOWNLOAD = FOLDER_EBOOK + "/Download";
    
    //Folder For Script
    public static String FOLDER_SCRIPTME = FOLDER + "/6.ScriptMe";
    public static String FOLDER_SCRIPTME_PROJECTS = FOLDER_EBOOK + "/Projects";
    public static String FOLDER_SCRIPTME_DOWNLOAD = FOLDER_SCRIPTME + "/Download";

    //Folder For Archive
    public static String FOLDER_ARCHIVE = FOLDER + "/7.Archive";
    public static String FOLDER_ARCHIVE_DOWNLOAD = FOLDER_VIDEO + "/Download";
    public static String FOLDER_ARCHIVE_EXTRACTION = FOLDER_ARCHIVE + "/Extracted";
    public static String FOLDER_ARCHIVE_ARCHIVES = FOLDER_ARCHIVE + "/Archives";

    public static class DirectoryNotEmptyException extends IOException
    {
        private static final long serialVersionUID = 1L;

        public DirectoryNotEmptyException(File file)
        {
            super("Directory "+file.getName()+" is not empty");
        }
    }

    public static class FileAlreadyExistsException extends IOException
    {
        private static final long serialVersionUID = 1L;

        public FileAlreadyExistsException(File file)
        {
            super("File "+file.getName()+" already exists in destination");
        }
	}
    
    private FolderMe(Context context) {
        this.context = context; 
        if (externalAvailable()) {
            File mDefaultDir = new File(getExternalStorageDirectory() + File.separator + context.getString(R.string.app_name), ".nomedia");
            mDefaultDir.getParentFile().mkdirs();
            if (!mDefaultDir.exists()) {
                try {
                    mDefaultDir.createNewFile();
                } catch (IOException io) {
                    io.getMessage();
                }
            }

            File mExternalDir = context.getExternalFilesDir(null);
            mExternalDir.getParentFile().mkdirs();
            if (!mExternalDir.exists()) {
                mExternalDir.mkdirs();
            }

            File mScanning = context.getExternalFilesDir("scanning");
            mScanning.getParentFile().mkdirs();
            if (!mScanning.exists()) {
                mScanning.mkdirs();
            }
            
            File mLog = context.getExternalFilesDir("log");
            mLog.getParentFile().mkdirs();
            if (!mLog.exists()) {
                mLog.mkdirs();
            }
            
            File mTemp = context.getExternalFilesDir("temp");
            mTemp.getParentFile().mkdirs();
            if (!mTemp.exists()) {
                mTemp.mkdirs();
            }
            
            File mObbFolder = context.getObbDir();
            mObbFolder.getParentFile().mkdirs();
            if (!mObbFolder.exists()) {
                mObbFolder.mkdirs();
            }
            
            File mInternalDir = context.getFilesDir();
            mInternalDir.getParentFile().mkdirs();
            if (!mInternalDir.exists()) {
                mInternalDir.mkdirs();
                mInternalDir.setExecutable(true, false);
            }

            File mDataDir = new File(getDataDir(context));
            mDataDir.getParentFile().mkdirs();
            if (!mDataDir.exists()) {
                mDataDir.mkdirs();
                mDataDir.setExecutable(true, false);
            } 
            
            File mSupportDir = new File(mDataDir, "support");
            mSupportDir.getParentFile().mkdirs();
            if (!mSupportDir.exists()) {
                mSupportDir.mkdirs();
                mSupportDir.setExecutable(true, false);
            } 
            

            File mExternalCacheDir = context.getCacheDir();
            mExternalCacheDir.getParentFile().mkdirs();
            if (!mExternalCacheDir.exists()) {
                mExternalCacheDir.mkdirs();
            } 

            File mInternalCacheDir = context.getExternalCacheDir();
            mInternalCacheDir.getParentFile().mkdirs();
            if (!mInternalCacheDir.exists()) {
                mInternalCacheDir.mkdirs();
            }          
        }    
    }

    public static FolderMe with(Context context) {
        return new FolderMe(context);
    }
    
    public static FolderMe get() {
        FolderMe localInstance = sInstance;
        if (localInstance == null) {
            synchronized (FolderMe.class) {
                localInstance = sInstance;
                if (localInstance == null) {
                    sInstance = localInstance = new FolderMe(AppController.getContext());
                }
            }
        }
        return localInstance;
    }

    public static void deleteEmptyFolders(Collection<File> directories) throws DirectoryNotEmptyException
    {
        for (File file : directories) if (file.isDirectory())
            {           
                FileMe.deleteFiles(Arrays.asList(file.listFiles()));
                file.delete();
            }
            else throw new DirectoryNotEmptyException(file);
    }
    
    public static Context getContext() {
        return AppController.getContext();
    }
    
    public static boolean externalAvailable() {
        return Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());
    }  

    public void initFolderMe(){
        if (externalAvailable()) {
            //Folder Apk
            File mFolderApk = new File(FOLDER_APK);
            mFolderApk.getParentFile().mkdirs();
            if (!mFolderApk.exists()) {
                mFolderApk.mkdirs();
            }          
            
            File mFolderApkBackup = new File(FOLDER_APK_BACKUP);
            mFolderApkBackup.getParentFile().mkdirs();
            if (!mFolderApkBackup.exists()) {
                mFolderApkBackup.mkdirs();
            }
            
            File mFolderApkDownload = new File(FOLDER_APK_DOWNLOAD);
            mFolderApkDownload.getParentFile().mkdirs();
            if (!mFolderApkDownload.exists()) {
                mFolderApkDownload.mkdirs();
            }      
            
            //Folder Image
            File mFolderImage = new File(FOLDER_IMAGE);
            mFolderImage.getParentFile().mkdirs();
            if (!mFolderImage.exists()) {
                mFolderImage.mkdirs();
            }

            File mFolderImageConvert = new File(FOLDER_IMAGE_CONVERT);
            mFolderImageConvert.getParentFile().mkdirs();
            if (!mFolderImageConvert.exists()) {
                mFolderImageConvert.mkdirs();
            }
            
            File mFolderImageDownload = new File(FOLDER_IMAGE_DOWNLOAD);
            mFolderImageDownload.getParentFile().mkdirs();
            if (!mFolderImageDownload.exists()) {
                mFolderImageDownload.mkdirs();
            } 
            
            File mFolderImageProjects = new File(FOLDER_IMAGE_PROJECTS);
            mFolderImageProjects.getParentFile().mkdirs();
            if (!mFolderImageProjects.exists()) {
                mFolderImageProjects.mkdirs();
            }
            
            //Folder Audio
            File mFolderAudio = new File(FOLDER_AUDIO);
            mFolderAudio.getParentFile().mkdirs();
            if (!mFolderAudio.exists()) {
                mFolderAudio.mkdirs();
            }
            
            File mFolderAudioDownload = new File(FOLDER_AUDIO_DOWNLOAD);
            mFolderAudioDownload.getParentFile().mkdirs();
            if (!mFolderAudioDownload.exists()) {
                mFolderAudioDownload.mkdirs();
            }  
            
            File mFolderAudioConvert = new File(FOLDER_AUDIO_CONVERT);
            mFolderAudioConvert.getParentFile().mkdirs();
            if (!mFolderAudioConvert.exists()) {
                mFolderAudioConvert.mkdirs();
            }  
            
            File mFolderAudioRecorder = new File(FOLDER_AUDIO_RECORDER);
            mFolderAudioRecorder.getParentFile().mkdirs();
            if (!mFolderAudioRecorder.exists()) {
                mFolderAudioRecorder.mkdirs();
            }  
           
            //Folder Video
            File mFolderVideo = new File(FOLDER_VIDEO);
            mFolderVideo.getParentFile().mkdirs();
            if (!mFolderVideo.exists()) {
                mFolderVideo.mkdirs();
            }
            
            File mFolderVideoRecorder = new File(FOLDER_VIDEO_RECORDER);
            mFolderVideoRecorder.getParentFile().mkdirs();
            if (!mFolderVideoRecorder.exists()) {
                mFolderVideoRecorder.mkdirs();
            }
            
            File mFolderVideoDownload = new File(FOLDER_VIDEO_DOWNLOAD);
            mFolderVideoDownload.getParentFile().mkdirs();
            if (!mFolderVideoDownload.exists()) {
                mFolderVideoDownload.mkdirs();
            }
            
            File mFolderVideoProjects = new File(FOLDER_VIDEO_PROJECTS);
            mFolderVideoProjects.getParentFile().mkdirs();
            if (!mFolderVideoProjects.exists()) {
                mFolderVideoProjects.mkdirs();
            }
           
            //Folder YouTube
            File mFolderYouTube = new File(FOLDER_YOUTUBE);
            mFolderYouTube.getParentFile().mkdirs();
            if (!mFolderYouTube.exists()) {
                mFolderYouTube.mkdirs();
            }
          
            File mFolderYouTubeAnalytics = new File(FOLDER_YOUTUBE_ANALYTICS);
            mFolderYouTubeAnalytics.getParentFile().mkdirs();
            if (!mFolderYouTubeAnalytics.exists()) {
                mFolderYouTubeAnalytics.mkdirs();
            }
            
            File mFolderYouTubeDownload = new File(FOLDER_YOUTUBE_DOWNLOAD);
            mFolderYouTubeDownload.getParentFile().mkdirs();
            if (!mFolderYouTubeDownload.exists()) {
                mFolderYouTubeDownload.mkdirs();
            }
          
            //Folder Ebook
            File mFolderEbook = new File(FOLDER_EBOOK);
            mFolderEbook.getParentFile().mkdirs();
            if (!mFolderEbook.exists()) {
                mFolderEbook.mkdirs();
            }
            
            File mFolderEbookProjects = new File(FOLDER_EBOOK_PROJECTS);
            mFolderEbookProjects.getParentFile().mkdirs();
            if (!mFolderEbookProjects.exists()) {
                mFolderEbookProjects.mkdirs();
            }
            
            File mFolderEbookDownload = new File(FOLDER_EBOOK_DOWNLOAD);
            mFolderEbookDownload.getParentFile().mkdirs();
            if (!mFolderEbookDownload.exists()) {
                mFolderEbookDownload.mkdirs();
            }
            
            File mFolderScriptMe = new File(FOLDER_SCRIPTME);
            mFolderScriptMe.getParentFile().mkdirs();
            if (!mFolderScriptMe.exists()) {
                mFolderScriptMe.mkdirs();
            }
            
            File mFolderScriptMeProjects = new File(FOLDER_SCRIPTME_PROJECTS);
            mFolderScriptMeProjects.getParentFile().mkdirs();
            if (!mFolderScriptMeProjects.exists()) {
                mFolderScriptMeProjects.mkdirs();
            }
            
            File mFolderScriptMeDownload = new File(FOLDER_SCRIPTME_DOWNLOAD);
            mFolderScriptMeDownload.getParentFile().mkdirs();
            if (!mFolderScriptMeDownload.exists()) {
                mFolderScriptMeDownload.mkdirs();
            }
            
            File mFolderArchive = new File(FOLDER_ARCHIVE);
            mFolderArchive.getParentFile().mkdirs();
            if (!mFolderArchive.exists()) {
                mFolderArchive.mkdirs();
            }

            File mFolderArchiveDownload = new File(FOLDER_ARCHIVE_DOWNLOAD);
            mFolderArchiveDownload.getParentFile().mkdirs();
            if (!mFolderArchiveDownload.exists()) {
                mFolderArchiveDownload.mkdirs();
            }  

            File mFolderArchiveCompressor = new File(FOLDER_ARCHIVE_ARCHIVES);
            mFolderArchiveCompressor.getParentFile().mkdirs();
            if (!mFolderArchiveCompressor.exists()) {
                mFolderArchiveCompressor.mkdirs();
            }  
            
            File mFolderArchiveExtractor = new File(FOLDER_ARCHIVE_EXTRACTION);
            mFolderArchiveExtractor.getParentFile().mkdirs();
            if (!mFolderArchiveExtractor.exists()) {
                mFolderArchiveExtractor.mkdirs();
            }

        }
    }
    
    public FolderMe setFolder(String folder) {
        if (externalAvailable()) {

            File mFolderMe = new File(folder);
            if (!mFolderMe.exists()) {
                mFolderMe.mkdirs();
            }
        }
        return this;
    }
    
    public static String getFolder(File folderName) {
        String type = folderName.getName();
        if (externalAvailable()) {
            File root = Environment.getExternalStorageDirectory();
            File appRoot = new File(root, folderName.getName());
            appRoot.mkdirs();
            String folder = appRoot.getAbsolutePath();
            if (TextUtils.isEmpty(type)) {
                return folder;
            }

            File dir = new File(appRoot, type);
            if(!dir.exists()){
                dir.mkdirs();
            }
            String folderMe = dir.getAbsolutePath();
            return folderMe;
        }
        throw new RuntimeException("External storage device is not available.");
        
    }
    
    public static File getFolder(String folder) {
        String type = folder;
        if (externalAvailable()) {
            File root = Environment.getExternalStorageDirectory();
            File appRoot = new File(root, folder);
            appRoot.mkdirs();
            if (TextUtils.isEmpty(type)) {
                return appRoot;
            }

            File dir = new File(appRoot, type);
            if(!dir.exists()){
                dir.mkdirs();
            }
            
            return dir;
        }
        throw new RuntimeException("External storage device is not available.");
    }
    
    
    /*====================*/
    /*== Default Folder ==*/
    /*====================*/
    //AppMe Folder In External Storage
    public static String getFolderMe() {
        File folder = new File(getExternalStorageDirectory() + "/" + getContext().getString(R.string.app_name));
        if (!folder.exists()) {
            folder.mkdirs();
        }   
        return folder.getAbsolutePath();
    }

    public File getDefaultDir(){
        return new File(getFolderMe());
    }
    
    public File getExternalDir(String dir){
        return getContext().getExternalFilesDir(dir);
    }
    
    public File getInternalDir(){
        return getContext().getFilesDir();
    }
    
    public File getDataDir(){
        return new File(getDataDir(getContext()));
    }
    
    public File getObbDir(){
        return getContext().getObbDir();
    }
    
    public File getExternalCacheDir(){
        return getContext().getExternalCacheDir();
    }
    
    public File getInternalCacheDir(){
        return getContext().getCacheDir();
    }
    
    public File getFolderScanning(){
        return getContext().getExternalFilesDir("scanning");
    }
    
    public File getFolderLog(){
        return getContext().getExternalFilesDir("log");
    }
    
    public File getFolderTemporer(){
        return getContext().getExternalFilesDir("temp");
    }
    
    public File getFolderApk(){
        return new File(FOLDER_APK);
    }
    
    public File getFolderApkDownload(){
        return new File(FOLDER_APK_DOWNLOAD);
    }
    
    public File getFolderApkBackup(){
        return new File(FOLDER_APK_BACKUP);
    }
    
    public File getFolderImage(){
        return new File(FOLDER_IMAGE);
    }

    public File getFolderImageDownload(){
        return new File(FOLDER_IMAGE_DOWNLOAD);
    }

    public File getFolderImageProjects(){
        return new File(FOLDER_IMAGE_PROJECTS);
    }
    
    public File getFolderImageConvert(){
        return new File(FOLDER_IMAGE_CONVERT);
    }
    
    public File getFolderAudio(){
        return new File(FOLDER_AUDIO);
    }

    public File getFolderAudioDownload(){
        return new File(FOLDER_AUDIO_DOWNLOAD);
    }

    public File getFolderAudioConvert(){
        return new File(FOLDER_AUDIO_CONVERT);
    }

    public File getFolderAudioRecord(){
        return new File(FOLDER_AUDIO_RECORDER);
    }
    
    public File getFolderVideo(){
        return new File(FOLDER_VIDEO);
    }

    public File getFolderVideoDownload(){
        return new File(FOLDER_VIDEO_DOWNLOAD);
    }

    public File getFolderVideoProjects(){
        return new File(FOLDER_VIDEO_PROJECTS);
    }

    public File getFolderVideoRecord(){
        return new File(FOLDER_VIDEO_RECORDER);
    }
    
    public File getFolderYouTube(){
        return new File(FOLDER_YOUTUBE);
    }

    public File getFolderYouTubeDownload(){
        return new File(FOLDER_YOUTUBE_DOWNLOAD);
    }

    public File getFolderYouTubeAnalytics(){
        return new File(FOLDER_YOUTUBE_ANALYTICS);
    }

    public File getFolderEbook(){
        return new File(FOLDER_EBOOK);
    }

    public File getFolderEbookDownload(){
        return new File(FOLDER_EBOOK_DOWNLOAD);
    }

    public File getFolderEbookProjects(){
        return new File(FOLDER_EBOOK_PROJECTS);
    }

    public File getFolderScriptMe(){
        return new File(FOLDER_SCRIPTME);
    }

    public File getFolderScriptMeDownload(){
        return new File(FOLDER_SCRIPTME_DOWNLOAD);
    }

    public File getFolderScriptMeProjects(){
       return new File(FOLDER_SCRIPTME_PROJECTS);
    }
    
    //WebServer Folder
    public static final String HOME_WEB_PATH = FOLDER_SCRIPTME + "/web";
    public static final String FOLDER_WEB_CLIENT = HOME_WEB_PATH + "/client";
    public static final String FOLDER_WEB_EDITOR = HOME_WEB_PATH + "/editor";
    public static final String FOLDER_WEB_TERMINAL = HOME_WEB_PATH + "/terminal";
    public static final String FOLDER_FILE_TRANSFER = HOME_WEB_PATH + "/folders";

    public static File getWebFolder(){
        File mWeb_Path = new File(HOME_WEB_PATH);
        if (!mWeb_Path.exists()) {
            mWeb_Path.mkdirs();
        }   
        return mWeb_Path;
    }

    public static File getWebTerminal(){
        File mWeb_Path = new File(FOLDER_WEB_TERMINAL);
        if (!mWeb_Path.exists()) {
            mWeb_Path.mkdirs();
        }   
        return mWeb_Path;
    }
    
    public static File getWebEditor(){
        File mWeb_Path = new File(FOLDER_WEB_EDITOR);
        if (!mWeb_Path.exists()) {
            mWeb_Path.mkdirs();
        }   
        return mWeb_Path;
    }
    
    public static File getWebClient(){
        File mWeb_Path = new File(FOLDER_WEB_CLIENT);
        if (!mWeb_Path.exists()) {
            mWeb_Path.mkdirs();
        }   
        return mWeb_Path;
    }
    
    public static File getFileTransfer(){
        File filePath = new File(FOLDER_FILE_TRANSFER);
        if (!filePath.exists()) {
            filePath.mkdirs();
        }   
        return filePath;
    }

    public static String getServerIP(){
        return HOME_WEB_PATH + "/ip-address.json";
    }
    
    public File getInstallDir() {
        try {
            return new File(getContext().getPackageManager().getApplicationInfo(getContext().getPackageName(), 0).dataDir);
        } catch (PackageManager.NameNotFoundException e) {
            return null;
        }
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

    public static File getSupportDir(Context c) {
        return new File(getDataDir(c) + "/support/");
    }

    public static File getSdcardInstallDir() {
        return new File(FolderMe.EXTERNAL_DIR + "/Debian");
    }
    
    /**
     *get the internal or outside sd card path
     * @param is_removale true is is outside sd card
     * */
    public static String getExternalStorageDirectory() {
        return Environment.getExternalStorageDirectory().getAbsolutePath();   
    }

    public static String getInternalStorageDirectory() {
        return getInternalStorageDirectory(AppController.getContext(), true);
    }

    public static String getInternalStorageDirectory(Context mContext, boolean is_removale) {

        StorageManager mStorageManager = (StorageManager) mContext.getSystemService(Context.STORAGE_SERVICE);
        Class<?> storageVolumeClazz = null;
        try {
            storageVolumeClazz = Class.forName("android.os.storage.StorageVolume");
            Method getVolumeList = mStorageManager.getClass().getMethod("getVolumeList");
            Method getPath = storageVolumeClazz.getMethod("getPath");
            Method isRemovable = storageVolumeClazz.getMethod("isRemovable");
            Object result = getVolumeList.invoke(mStorageManager);
            final int length = Array.getLength(result);
            for (int i = 0; i < length; i++) {
                Object storageVolumeElement = Array.get(result, i);
                String path = (String) getPath.invoke(storageVolumeElement);
                boolean removable = (Boolean) isRemovable.invoke(storageVolumeElement);
                if (is_removale == removable) {
                    return path;
                }
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }
    
}
