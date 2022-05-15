package com.appme.story.application;

import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AlertDialog;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.CountDownTimer;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ProgressBar;
import android.widget.ImageButton;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Calendar;
import java.util.HashSet;

import com.appme.story.R;
import com.appme.story.AppConfig;
import com.appme.story.engine.app.analytics.Analytics;
import com.appme.story.engine.app.commons.downloader.models.Error;
import com.appme.story.engine.app.commons.downloader.listeners.OnCancelListener;
import com.appme.story.engine.app.commons.downloader.listeners.OnDownloadListener;
import com.appme.story.engine.app.commons.downloader.listeners.OnPauseListener;
import com.appme.story.engine.app.commons.downloader.listeners.OnProgressListener;
import com.appme.story.engine.app.commons.downloader.listeners.OnStartOrResumeListener;
import com.appme.story.engine.app.commons.downloader.PRDownloader;
import com.appme.story.engine.app.commons.downloader.models.Progress;
import com.appme.story.engine.app.commons.downloader.Status;
import com.appme.story.engine.app.folders.FileMe;
import com.appme.story.engine.app.folders.FolderMe;
import com.appme.story.engine.app.folders.clipboard.Clipboard;
import com.appme.story.engine.app.folders.clipboard.Clipboard.FileAction;
import com.appme.story.engine.app.utils.UpdateUtils;
import com.appme.story.receiver.ApplicationLogger;
import com.appme.story.receiver.RemoteService;
import com.appme.story.service.TerminalService;
import com.appme.story.service.TerminalDebianService;
import com.appme.story.engine.app.folders.clipboard.FileOperationListener;

public class DebianActivity extends AppCompatActivity  {

    public static final String GNUROOT_TERM = "launchTerm";
    public static final String GNUROOT_XTERM = "launchXTerm";
    Integer downloadResultCode;
    Boolean errOcc;
    boolean noInstallAgain = false;

    Intent savedIntent = null;
    private TextView mTitle;
    private FloatingActionButton btnControll;
    private ImageView avatarView;
    private ProgressBar progressBarOne;  
    private TextView textViewProgressOne;
    private boolean taskRunning = true;
    private int downloadIdOne;

    private File obbFolder;
    private String obbFile;
    private String obbDownloadUrl;
    private File obbDownloadFolder;
    private final HashSet<File> selectedFiles = new HashSet<File>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme_Application);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_application_updater);

        mTitle = (TextView) findViewById(R.id.title_download);
        mTitle.setText("Debian File System");

        avatarView = (ImageView) findViewById(R.id.avatar);
        avatarView.setImageResource(R.drawable.ic_app_debian);

        progressBarOne = (ProgressBar)findViewById(R.id.progressBarOne);
        textViewProgressOne = (TextView)findViewById(R.id.textViewProgressOne);
        textViewProgressOne.setText(UpdateUtils.getProgressDisplayLine(0, 0));
        textViewProgressOne.setSingleLine(true);
        textViewProgressOne.setEllipsize(TextUtils.TruncateAt.MARQUEE);
        textViewProgressOne.setMarqueeRepeatLimit(-1);
        textViewProgressOne.setSelected(true);
        textViewProgressOne.setTextSize(16);
        textViewProgressOne.setPadding(0, 0, 0, 0);

        obbFolder = new File(getObbDir().getAbsolutePath());
        if (!obbFolder.exists()) {
            obbFolder.mkdir();
        }

        obbFile = FileMe.getDebianFile();  
        obbDownloadUrl = Analytics.getObbDownloadUrl();
        obbDownloadFolder = FolderMe.get().getFolderTemporer();  
        if (!obbFolder.exists()) {
            obbFolder.mkdir();
        }

        btnControll = (FloatingActionButton) findViewById(R.id.btn_controll);
        btnControll.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    startDownload(obbDownloadUrl);
                    taskRunning = !taskRunning;
                }
            });


        ApplicationLogger.sendBroadcast(this, "Debian Installation");


        handleIntent(getIntent());
    }

    private void handleIntent(Intent intent) {
        String intentAction = intent.getAction();
        File installStatus = new File(getInstallDir().getAbsolutePath() + "/support/.gnuroot_rootfs_passed");
        if (!isSymlink(installStatus) && !installStatus.exists() && !this.noInstallAgain) {
            runOnUiThread(new Runnable() {
                    public void run() {
                        textViewProgressOne.setText(getString(R.string.toast_not_installed));                     
                    }
                });
            this.savedIntent = intent;
            setupSupportFiles(true);
            updateVersion();
            setupFirstHalf();
            Analytics.with(this).setTerminal(true);
        } else if (AppConfig.LAUNCH_DEBIAN_REINSTALL.equals(intentAction)) {
            setupSupportFiles(true);
            updateVersion();
            setupFirstHalf();
            Analytics.with(this).setTerminal(true);
        } else if (AppConfig.LAUNCH_DEBIAN_TERMINAL.equals(intentAction)) {
            this.noInstallAgain = false;
            handleLaunchIntent(intent);
        } else if (AppConfig.LAUNCH_DEBIAN_UPDATE_ERROR.equals(intentAction)) {
            showUpdateErrorButton(intent.getStringExtra("packageName"));
        } else {
            launchTerm((String) null);
        }
    }

    public void handleLaunchIntent(Intent intent) {
        boolean z = true;
        Uri sharedFile = intent.getData();
        String launchType = intent.getStringExtra("launchType");
        String command = intent.getStringExtra("command");
        if (command != null) {
            File file = new File(getInstallDir().getAbsolutePath() + "/support/newCommand");
            try {
                OutputStreamWriter outputStreamWriter = new OutputStreamWriter(new FileOutputStream(file));
                outputStreamWriter.write(command);
                outputStreamWriter.close();
            } catch (IOException e) {
                Log.e("Exception", "File write failed: " + e.toString());
            }
            exec("chmod 0777 " + file.getAbsolutePath(), true);
            command = "/support/newCommand";
        }
        if (sharedFile != null) {
            copySharedFile(sharedFile);
        }

        switch (launchType) {
            case GNUROOT_TERM:
                launchTerm(command);
                return;
            case GNUROOT_XTERM:
                if (intent.getBooleanExtra("terminal_button", false)) {
                    z = false;
                }
                launchXTerm(Boolean.valueOf(z), command);
                return;
            default:
                Toast.makeText(this, R.string.toast_bad_launch_type, 1).show();
                finish();
                return;
        }
    }

    public void launchTerm(String command) {
        Intent termIntent = new Intent(this, DebianRunScript.class);
        termIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        termIntent.addCategory(Intent.CATEGORY_DEFAULT);
        termIntent.setAction(AppConfig.DEBIAN_ACTION_RUN_SCRIPT);
        if (command == null) {
            termIntent.putExtra(AppConfig.DEBIAN_EXTRA_INITIAL_COMMAND, getInstallDir().getAbsolutePath() + "/support/launchProot /bin/bash");
        } else {
            termIntent.putExtra(AppConfig.DEBIAN_EXTRA_INITIAL_COMMAND, getInstallDir().getAbsolutePath() + "/support/launchProot " + command);
        }
        checkPatches();
        startActivity(termIntent);
        finish();
    }

    public void launchXTerm(Boolean createNewXTerm, String command) {
        File deleteStarted = new File(getInstallDir().getAbsolutePath() + "/support/.gnuroot_x_started");
        if (deleteStarted.exists()) {
            deleteStarted.delete();
        }
        /*Intent serviceIntent = new Intent(this, DebianService.class);
         serviceIntent.addCategory(Intent.CATEGORY_DEFAULT);
         serviceIntent.putExtra(PubkeyDatabase.FIELD_PUBKEY_TYPE, "VNC");
         serviceIntent.putExtra("command", command);
         serviceIntent.putExtra("newXterm", createNewXTerm);
         checkPatches();
         startService(serviceIntent);
         finish();*/
    }

    public void startDownload(String url) {

        if (Status.RUNNING == PRDownloader.getStatus(downloadIdOne)) {
            PRDownloader.pause(downloadIdOne);
            return;
        }

        progressBarOne.setIndeterminate(true);
        progressBarOne.getIndeterminateDrawable().setColorFilter(Color.BLUE, android.graphics.PorterDuff.Mode.SRC_IN);

        if (Status.PAUSED == PRDownloader.getStatus(downloadIdOne)) {
            PRDownloader.resume(downloadIdOne);
            return;
        }

        downloadIdOne = PRDownloader.download(url, obbDownloadFolder.getAbsolutePath(), obbFile)
            .build()
            .setOnStartOrResumeListener(new OnStartOrResumeListener() {
                @Override
                public void onStartOrResume() {
                    progressBarOne.setIndeterminate(false);
                    btnControll.setImageResource(R.drawable.ic_download);                           
                }
            })
            .setOnPauseListener(new OnPauseListener() {
                @Override
                public void onPause() {
                    btnControll.setImageResource(R.drawable.ic_download_off);                       
                }
            })
            .setOnCancelListener(new OnCancelListener() {
                @Override
                public void onCancel() {
                    btnControll.setImageResource(R.drawable.ic_download_off);                           
                    progressBarOne.setProgress(0);
                    textViewProgressOne.setText("");
                    downloadIdOne = 0;
                    progressBarOne.setIndeterminate(false);
                }
            })
            .setOnProgressListener(new OnProgressListener() {
                @Override
                public void onProgress(Progress progress) {
                    long progressPercent = progress.currentBytes * 100 / progress.totalBytes;
                    progressBarOne.setProgress((int) progressPercent);
                    textViewProgressOne.setText(UpdateUtils.getProgressDisplayLine(progress.currentBytes, progress.totalBytes));
                    progressBarOne.setIndeterminate(false);
                }
            })
            .start(new OnDownloadListener() {
                @Override
                public void onDownloadComplete() {
                    btnControll.setImageResource(R.drawable.ic_done); 
                    //setupSecondHalf();
                    File file = new File(obbDownloadFolder.getAbsolutePath(), obbFile);
                    selectedFiles.add(file); 
                    Clipboard.getInstance().addFiles(selectedFiles, FileAction.Copy);
                    new CountDownTimer(1000, 1000){
                        @Override
                        public void onTick(long l) {

                        }

                        @Override
                        public void onFinish() {
                            pasteFiles();
                        }
                    }.start();    
                }

                @Override
                public void onError(Error error) {
                    btnControll.setImageResource(R.drawable.ic_download_off);   
                    if (error.isConnectionError()) {
                        textViewProgressOne.setText(error.getConnectionException().getMessage());
                    } else if (error.isServerError()) {
                        textViewProgressOne.setText(error.getServerErrorMessage());
                    }

                    progressBarOne.setProgress(0);
                    progressBarOne.setIndeterminate(false);
                    downloadIdOne = 0;                                                                                      
                }
            });            
    }

    public void pasteFiles() {
        new android.os.AsyncTask<Clipboard, Float, Exception>()
        {

            @Override
            protected void onPreExecute() {
                super.onPreExecute();

            }

            @Override
            protected void onProgressUpdate(Float... values) {
                float progress = values[0];
                progressBarOne.setMax(100);
                progressBarOne.setProgress((int) (progress * 100));
            }

            @Override
            protected Exception doInBackground(Clipboard... params) {
                try {
                    final int total = FileMe.countFilesIn(params[0].getFiles());
                    final int[] progress = {0};
                    params[0].paste(obbFolder, new FileOperationListener()
                        {                               
                            @Override
                            public void onFileProcessed(String filename) {
                                progress[0]++;
                                publishProgress((float)progress[0] / (float)total);
                            }

                            @Override
                            public boolean isOperationCancelled() {
                                return isCancelled();
                            }
                        });
                    return null;
                } catch (IOException e) {
                    e.printStackTrace();
                    return e;
                }
            }

            @Override
            protected void onCancelled() {

            }

            @Override
            protected void onPostExecute(Exception result) {
                
                if (result == null) {
                    Clipboard.getInstance().clear();
                    setupSecondHalf();
                } else {
                    new AlertDialog.Builder(DebianActivity.this)
                        .setMessage(result.getMessage())
                        .setPositiveButton(android.R.string.ok, null)
                        .show();
                }
            }

        }.executeOnExecutor(android.os.AsyncTask.THREAD_POOL_EXECUTOR, Clipboard.getInstance());
    }


    public void onCancel() {
        PRDownloader.cancel(downloadIdOne);
    }

    private void copySharedFile(Uri sharedFile) {
        InputStream srcStream;
        boolean status = true;
        File srcFile = new File(sharedFile.getPath());
        File destFile = new File(getInstallDir().getAbsolutePath() + "/support/" + srcFile.getName());
        try {
            srcStream = getContentResolver().openInputStream(sharedFile);

            try {
                copyFile(srcStream, new FileOutputStream(destFile));
            } catch (IOException e) {
                status = false;
            }
        } catch (FileNotFoundException e2) {
            status = false;
        }
        if (!status) {
            Toast.makeText(this, R.string.toast_file_not_found, 1).show();
        }
    }

    private void showUpdateErrorButton(final String packageName) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.update_error_message);
        builder.setPositiveButton(R.string.button_affirmative, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    if (packageName != null) {
                        Intent intent = new Intent("android.intent.action.VIEW");
                        intent.setData(Uri.parse("market://details?id=" + packageName));
                        DebianActivity.this.startActivity(intent);                 
                    }
                    Analytics.with(DebianActivity.this).setTerminal(true);
                    DebianActivity.this.finish();
                }
            });
        builder.create().show();
    }

    public void setupSupportFiles(boolean deleteFirst) {
        File installDir = getInstallDir();
        if (deleteFirst) {
            deleteRecursive(new File(installDir.getAbsolutePath() + "/support"));
        }
        File tempFile = new File(installDir.getAbsolutePath() + "/support");
        if (!tempFile.exists()) {
            tempFile.mkdir();
        }
        copyAssets(AppConfig.APPLICATION_ID);
    }

    private void setupFirstHalf() {
        this.errOcc = false;
        RemoteService.setStopService(DebianActivity.this, TerminalService.class);
        RemoteService.setStopService(DebianActivity.this, TerminalDebianService.class);
        new Thread() {
            public void run() {
                
                try {
                    final File debianFile = new File(obbFolder, obbFile);
                    final File obbTemp = new File(obbDownloadFolder, obbFile);
                    if (debianFile.exists()) {
                        runOnUiThread(new Runnable() {
                                public void run() {
                                    new CountDownTimer(1000, 1000){
                                        @Override
                                        public void onTick(long l) {

                                        }

                                        @Override
                                        public void onFinish() {
                                            setupSecondHalf();
                                        }
                                    }.start();                                
                                    ApplicationLogger.sendBroadcast(DebianActivity.this, "Debian File System : " + debianFile);
                                    textViewProgressOne.setText("Debian ObbFile : " + obbFile + " Exists");
                                }
                            });                    
                    } else {
                        runOnUiThread(new Runnable() {
                                public void run() {
                                    new CountDownTimer(1000, 1000){
                                        @Override
                                        public void onTick(long l) {

                                        }

                                        @Override
                                        public void onFinish() {
                                            if (obbTemp.exists()) {
                                                runOnUiThread(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            File file = new File(obbDownloadFolder.getAbsolutePath(), obbFile);
                                                            selectedFiles.add(file); 
                                                            Clipboard.getInstance().addFiles(selectedFiles, FileAction.Copy);
                                                            new CountDownTimer(1000, 1000){
                                                                @Override
                                                                public void onTick(long l) {

                                                                }

                                                                @Override
                                                                public void onFinish() {
                                                                    pasteFiles();
                                                                }
                                                            }.start();    
                                                        }
                                                    });
                                            } else {
                                                textViewProgressOne.setText("Download Debian File System");
                                                startDownload(obbDownloadUrl);
                                            }

                                        }
                                    }.start();                               
                                }
                            });
                    }
                } catch (Exception e) {

                    DebianActivity.this.runOnUiThread(new Runnable() {
                            public void run() {
                                textViewProgressOne.setText(getString(R.string.toast_bad_install));
                            }
                        });
                }
            }
        }.start();

    }

    /* access modifiers changed from: private */
    public void setupSecondHalf() {
        File installDir = getInstallDir();
        File sdcardInstallDir = getSdcardInstallDir();
        File tempFile = new File(installDir.getAbsolutePath() + "/debian");
        if (tempFile.exists()) {
            deleteRecursive(tempFile);
        }
        tempFile.mkdir();
        File tempFile2 = new File(sdcardInstallDir.getAbsolutePath());
        if (!tempFile2.exists()) {
            tempFile2.mkdir();
        }
        File tempFile3 = new File(sdcardInstallDir.getAbsolutePath() + "/debian");
        if (tempFile3.exists()) {
            deleteRecursive(tempFile3);
        }
        tempFile3.mkdir();
        File tempFile4 = new File(installDir.getAbsolutePath() + "/debian/host-rootfs");
        if (!tempFile4.exists()) {
            tempFile4.mkdir();
        }
        File tempFile5 = new File(installDir.getAbsolutePath() + "/debian/.proot.noexec");
        if (!tempFile5.exists()) {
            tempFile5.mkdir();
        }
        File tempFile6 = new File(installDir.getAbsolutePath() + "/debian/sdcard");
        if (!tempFile6.exists()) {
            tempFile6.mkdir();
        }
        File tempFile7 = new File(installDir.getAbsolutePath() + "/debian/dev");
        if (!tempFile7.exists()) {
            tempFile7.mkdir();
        }
        File tempFile8 = new File(installDir.getAbsolutePath() + "/debian/proc");
        if (!tempFile8.exists()) {
            tempFile8.mkdir();
        }
        File tempFile9 = new File(installDir.getAbsolutePath() + "/debian/mnt");
        if (!tempFile9.exists()) {
            tempFile9.mkdir();
        }
        File tempFile10 = new File(installDir.getAbsolutePath() + "/debian/data");
        if (!tempFile10.exists()) {
            tempFile10.mkdir();
        }
        File tempFile11 = new File(installDir.getAbsolutePath() + "/debian/sys");
        if (!tempFile11.exists()) {
            tempFile11.mkdir();
        }
        File tempFile12 = new File(installDir.getAbsolutePath() + "/debian/home");
        if (!tempFile12.exists()) {
            tempFile12.mkdir();
        }
        File tempFile13 = new File(sdcardInstallDir.getAbsolutePath() + "/home");
        if (!tempFile13.exists()) {
            tempFile13.mkdir();
        }
        File tempFile14 = new File(installDir.getAbsolutePath() + "/debian/intents");
        if (!tempFile14.exists()) {
            tempFile14.mkdir();
        }
        File tempFile15 = new File(sdcardInstallDir.getAbsolutePath() + "/intents");
        if (!tempFile15.exists()) {
            tempFile15.mkdir();
        }
        if (this.savedIntent == null || this.savedIntent.getAction() != "com.appme.story.LAUNCH") {
            launchTerm((String) null);
            return;
        }
        this.noInstallAgain = true;
        handleIntent(this.savedIntent);
    }

    public File getInstallDir() {
        try {
            return new File(getPackageManager().getApplicationInfo(AppConfig.APPLICATION_ID, 0).dataDir);
        } catch (PackageManager.NameNotFoundException e) {
            return null;
        }
    }

    public File getSdcardInstallDir() {
        return new File(Environment.getExternalStorageDirectory() + "/Debian");
    }

    /* access modifiers changed from: package-private */
    public void deleteRecursive(File fileOrDirectory) {
        exec(getInstallDir().getAbsolutePath() + "/support/busybox rm -rf " + fileOrDirectory.getAbsolutePath(), true);
    }

    private void copyFile(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        while (true) {
            int read = in.read(buffer);
            if (read != -1) {
                out.write(buffer, 0, read);
            } else {
                return;
            }
        }
    }

    public void copyDirectory(File sourceLocation, File targetLocation) throws IOException {
        if (sourceLocation.isDirectory()) {
            if (!targetLocation.exists()) {
                targetLocation.mkdir();
            }
            String[] children = sourceLocation.list();
            for (int i = 0; i < children.length; i++) {
                copyDirectory(new File(sourceLocation, children[i]), new File(targetLocation, children[i]));
            }
            return;
        }
        InputStream in = new FileInputStream(sourceLocation);
        OutputStream out = new FileOutputStream(targetLocation);
        byte[] buf = new byte[1024];
        while (true) {
            int len = in.read(buf);
            if (len > 0) {
                out.write(buf, 0, len);
            } else {
                in.close();
                out.close();
                return;
            }
        }
    }

    private void copyAssets(String packageName) {
        try {
            AssetManager assetManager = createPackageContext(packageName, 2).getAssets();
            File tempFile = new File(getInstallDir().getAbsolutePath() + "/support");
            if (!tempFile.exists()) {
                tempFile.mkdir();
            }
            String[] files = null;
            try {
                files = assetManager.list("");
            } catch (IOException e) {
                Log.e("tag", "Failed to get asset file list.", e);
            }
            for (String filename : files) {
                boolean replaceStrings = false;
                try {
                    InputStream in = assetManager.open(filename);
                    if (filename.contains(".replace.mp2")) {
                        replaceStrings = true;
                    }
                    filename = filename.replace(".replace.mp2", "").replace(".mp2", "").replace(".mp3", ".tar.gz");
                    File outFile = new File(tempFile, filename);
                    FileOutputStream fileOutputStream = new FileOutputStream(outFile);
                    try {
                        copyFile(in, fileOutputStream);
                        in.close();
                        fileOutputStream.flush();
                        fileOutputStream.close();
                        if (replaceStrings) {
                            replaceScriptStrings(outFile);
                        }
                        exec("chmod 0777 " + outFile.getAbsolutePath(), true);
                    } catch (IOException e2) {
                        e2.printStackTrace();

                        Log.e("tag", "Failed to copy asset file: " + filename);
                    }
                } catch (IOException e3) {
                    e3.printStackTrace();
                    Log.e("tag", "Failed to copy asset file: " + filename);
                }
            }
        } catch (PackageManager.NameNotFoundException e4) {
            e4.printStackTrace();
        }
    }

    /**
     * Replaces key phrases in scripts copied with the asset manager.
     * @param myFile
     */
    private void replaceScriptStrings(File myFile) {
        File installDir = getInstallDir();
        File sdcardInstallDir = getSdcardInstallDir();
        String sdcardPath = Environment.getExternalStorageDirectory().getAbsolutePath();
        int OBBVERSION = 10;
        String mainFilePath = Environment.getExternalStorageDirectory() + "/Android/obb/com.appme.story/main." + OBBVERSION + ".com.gnuroot.debian.obb";

        String oldFileName = myFile.getAbsolutePath();
        String tmpFileName = oldFileName + ".tmp";

        BufferedReader br = null;
        BufferedWriter bw = null;
        try {
            br = new BufferedReader(new FileReader(oldFileName));
            bw = new BufferedWriter(new FileWriter(tmpFileName));
            String line;
            while ((line = br.readLine()) != null) {
                line = line.replace("ROOT_PATH", installDir.getAbsolutePath());
                line = line.replace("SDCARD_GNU_PATH", sdcardInstallDir.getAbsolutePath());
                line = line.replace("SDCARD_PATH", sdcardPath);
                line = line.replace("EXTRA_BINDINGS", "");
                line = line.replace("MAIN_FILE_PATH", mainFilePath);
                bw.write(line + "\n");
            }
        } catch (Exception e) {
            Toast.makeText(this, R.string.toast_bad_install, Toast.LENGTH_LONG).show();
            return;
        } finally {
            try {
                if (br != null)
                    br.close();
            } catch (IOException e) {
                //
            }
            try {
                if (bw != null)
                    bw.close();
            } catch (IOException e) {
                //
            }
        }
        // Once everything is complete, delete old file..
        File oldFile = new File(oldFileName);
        oldFile.delete();

        // And rename tmp file's name to old file name
        File newFile = new File(tmpFileName);
        newFile.renameTo(oldFile);

    }

    private void exec(String command, boolean setError) {
        try {
            Process process = Runtime.getRuntime().exec(command);
            try {
                process.waitFor();
                BufferedReader stdError = new BufferedReader(new InputStreamReader(process.getErrorStream()));
                while (true) {
                    String str = stdError.readLine();
                    if (str != null) {
                        Log.e("Exec", str);
                        if (setError) {
                            this.errOcc = true;
                        }
                    } else {
                        process.getInputStream().close();
                        process.getOutputStream().close();
                        process.getErrorStream().close();
                        return;
                    }
                }
            } catch (InterruptedException e) {
                if (setError) {
                    this.errOcc = true;
                }
            }
        } catch (IOException e2) {
            this.errOcc = true;
        }
    }

    private void checkPatches() {
        SharedPreferences prefs = getSharedPreferences("MAIN", 0);
        SharedPreferences.Editor editor = prefs.edit();
        String patchVersion = "notARealPatchVersion";
        String sharedVersion = prefs.getString("patchVersion", (String) null);
        try {
            patchVersion = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            Toast.makeText(getApplicationContext(), R.string.toast_bad_package, 1).show();
        }
        if (sharedVersion != null && !sharedVersion.equals(patchVersion)) {
            deleteRecursive(new File(getInstallDir().getAbsolutePath() + "/support/.gnuroot_patch_passed"));
            Toast.makeText(this, R.string.toast_bad_patch, 1).show();
        }
        if (sharedVersion == null || !sharedVersion.equals(patchVersion)) {
            setupSupportFiles(false);
            try {
                copyDirectory(new File(getInstallDir().getAbsolutePath() + "/debian/home"), new File(getSdcardInstallDir().getAbsolutePath() + "/home"));
            } catch (IOException e2) {
            }
        }
        if (!patchVersion.equals("notARealPatchVersion")) {
            editor.putString("patchVersion", patchVersion);
            editor.commit();
        }
    }

    private void updateVersion() {
        SharedPreferences.Editor editor = getSharedPreferences("MAIN", 0).edit();
        String patchVersion = "notARealPatchVersion";
        try {
            patchVersion = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            Toast.makeText(getApplicationContext(), R.string.toast_bad_package, 1).show();
        }
        if (!patchVersion.equals("notARealPatchVersion")) {
            editor.putString("patchVersion", patchVersion);
            editor.commit();
        }
    }

    public static boolean isSymlink(File file) {
        try {
            if (file == null)
                throw new NullPointerException("File must not be null");
            File canon;
            if (file.getParent() == null) {
                canon = file;
            } else {
                File canonDir;

                canonDir = file.getParentFile().getCanonicalFile();

                canon = new File(canonDir, file.getName());
            }
            return !canon.getCanonicalFile().equals(canon.getAbsoluteFile());
        } catch (IOException e) {
            return false;
        }
    }

}
