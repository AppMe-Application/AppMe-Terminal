package com.appme.story.engine.app.terminal.command;

import android.content.Context;
import android.content.res.AssetManager;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import com.appme.story.settings.ShellResultPreference;

public class ShellResultUtils {

    /**
     * Closeable helper
     *
     * @param c closable object
     */
    private static void close(Closeable c) {
        if (c != null) {
            try {
                c.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Extract file to env directory
     *
     * @param c         context
     * @param rootAsset root asset name
     * @param path      path to asset file
     * @return false if error
     */
    public static boolean extractFile(Context c, String rootAsset, String path) {
        AssetManager assetManager = c.getAssets();
        InputStream in = null;
        OutputStream out = null;
        try {
            in = assetManager.open(rootAsset + path);
            String fullPath = ShellResultPreference.getDefaultDir(c) + path;
            out = new FileOutputStream(fullPath);
            byte[] buffer = new byte[1024];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } finally {
            close(in);
            close(out);
        }
        return true;
    }

    /**
     * Extract path to env directory
     *
     * @param c         context
     * @param rootAsset root asset name
     * @param path      path to asset directory
     * @return false if error
     */
    public static boolean extractDir(Context c, String rootAsset, String path) {
        AssetManager assetManager = c.getAssets();
        try {
            String[] assets = assetManager.list(rootAsset + path);
            if (assets.length == 0) {
                if (!extractFile(c, rootAsset, path)) return false;
            } else {
                String fullPath = ShellResultPreference.getDefaultDir(c) + path;
                File dir = new File(fullPath);
                if (!dir.exists()) dir.mkdir();
                for (String asset : assets) {
                    if (!extractDir(c, rootAsset, path + "/" + asset)) return false;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * Recursive remove all from directory
     *
     * @param path path to directory
     */
    public static void cleanDirectory(File path) {
        if (path == null) return;
        if (path.exists()) {
            File[] list = path.listFiles();
            if (list == null) return;
            for (File f : list) {
                if (f.isDirectory()) cleanDirectory(f);
                f.delete();
            }
        }
    }

    /**
     * Recursive set permissions to directory
     *
     * @param path path to directory
     */
    public static void setPermissions(File path) {
        if (path == null) return;
        if (path.exists()) {
            path.setReadable(true, false);
            path.setExecutable(true, false);
            File[] list = path.listFiles();
            if (list == null) return;
            for (File f : list) {
                if (f.isDirectory()) setPermissions(f);
                f.setReadable(true, false);
                f.setExecutable(true, false);
            }
        }
    }

    /**
     * Update version file
     *
     * @param c context
     * @return false if error
     */
    public static Boolean setVersion(Context c) {
        Boolean result = false;
        String f = ShellResultPreference.getDefaultDir(c) + "/version";
        BufferedWriter bw = null;
        try {
            bw = new BufferedWriter(new FileWriter(f));
            bw.write(ShellResultPreference.getVersion(c));
            result = true;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            close(bw);
        }
        return result;
    }

    /**
     * Check latest env version
     *
     * @param c context
     * @return false if error
     */
    public static Boolean isLatestVersion(Context c) {
        boolean result = false;
        String f = ShellResultPreference.getDefaultDir(c) + "/version";
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(f));
            String line = br.readLine();
            if (ShellResultPreference.getVersion(c).equals(line)) result = true;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            close(br);
        }
        return result;
    }

    /**
     * Update env directory
     *
     * @param c context
     * @return false if error
     */
    public static boolean update(Context c) {
        if (isLatestVersion(c)) return true;

        // prepare env directory
        String envDir = ShellResultPreference.getDefaultDir(c);
        File fEnvDir = new File(envDir);
        fEnvDir.mkdirs();
        if (!fEnvDir.exists()) {
            return false;
        }
        cleanDirectory(fEnvDir);

        // extract assets
        if (!extractDir(c, "system", "")) {
            return false;
        }
        String mArch = ShellResultPreference.getArch();
        switch (mArch) {
            case "arm":
                if (!extractDir(c, "arm", "")) {
                    return false;
                }
                break;
            case "arm64":
                if (!extractDir(c, "arm", "")) {
                    return false;
                }
                if (!extractDir(c, "arm64", "")) {
                    return false;
                }
                break;
            case "x86":
                if (!extractDir(c, "x86", "")) {
                    return false;
                }
                break;
            case "x86_64":
                if (!extractDir(c, "x86", "")) {
                    return false;
                }
                if (!extractDir(c,  "x86_64", "")) {
                    return false;
                }
                break;
        }

        // set executable app directory
        File appDir = new File(ShellResultPreference.getDefaultDir(c) + "/..");
        appDir.setExecutable(true, false);

		File etcDir = new File(ShellResultPreference.getDefaultDir(c) + "/etc/..");
        etcDir.setExecutable(true, false);
		
		File scriptDir = new File(ShellResultPreference.getDefaultDir(c) + "/etc/scripts/..");
        scriptDir.setExecutable(true, false);
		
		File webDir = new File(ShellResultPreference.getDefaultDir(c) + "/etc/web/..");
        webDir.setExecutable(true, false);
		
		// set permissions
        setPermissions(fEnvDir);

        // install applets
        List<String> params = new ArrayList<>();
        params.add("busybox --install -s " + envDir + "/bin");
        exec(c, "sh", params);

        // update version
        return setVersion(c);
    }
	
	public static boolean updateTelnet(Context c) {
        if (isLatestVersion(c)) return true;

        // prepare env directory
        String envDir = ShellResultPreference.getDefaultDir(c);
        File fEnvDir = new File(envDir);
        fEnvDir.mkdirs();
        if (!fEnvDir.exists()) {
            return false;
        }
        cleanDirectory(fEnvDir);

        String mArch = ShellResultPreference.getArch();
        switch (mArch) {
            case "arm":
                if (!extractDir(c, "arm", "")) {
                    return false;
                }
                break;
            case "arm64":
                if (!extractDir(c, "arm", "")) {
                    return false;
                }
                if (!extractDir(c, "arm64", "")) {
                    return false;
                }
                break;
            case "x86":
                if (!extractDir(c, "x86", "")) {
                    return false;
                }
                break;
            case "x86_64":
                if (!extractDir(c, "x86", "")) {
                    return false;
                }
                if (!extractDir(c,  "x86_64", "")) {
                    return false;
                }
                break;
        }

        // set executable app directory
        File appDir = new File(ShellResultPreference.getDefaultDir(c) + "/..");
        appDir.setExecutable(true, false);

		
		// set permissions
        setPermissions(fEnvDir);

        // update version
        return setVersion(c);
    }
    
    /**
     * Remove env directory
     *
     * @param c context
     * @return false if error
     */
    public static boolean remove(Context c) {
        File fEnvDir = new File(ShellResultPreference.getDefaultDir(c));
        if (!fEnvDir.exists()) {
            return false;
        }
        cleanDirectory(fEnvDir);
        return true;
    }

    /**
     * Check root permissions
     *
     * @param c context
     * @return false if error
     */
    public static boolean isRooted(Context c) {
        boolean result = false;
        OutputStream stdin = null;
        InputStream stdout = null;
        try {
            Process process = Runtime.getRuntime().exec("su");
            stdin = process.getOutputStream();
            stdout = process.getInputStream();

            DataOutputStream os = null;
            try {
                os = new DataOutputStream(stdin);
                os.writeBytes("ls /data\n");
                os.writeBytes("exit\n");
                os.flush();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                close(os);
            }

            int n = 0;
            BufferedReader reader = null;
            try {
                reader = new BufferedReader(new InputStreamReader(stdout));
                while (reader.readLine() != null) {
                    n++;
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                close(reader);
            }

            if (n > 0) {
                result = true;
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            close(stdout);
            close(stdin);
        }
        if (!result) {
            ShellResult.log(c, "Require superuser privileges (root).\n");
        }
        return result;
    }

    /**
     * Execute commands from system shell
     *
     * @param c      context
     * @param params list of commands
     * @return false if error
     */
    public static boolean exec(final Context c, final String shell, final List<String> params) {
        if (params == null || params.size() == 0) {
            ShellResult.log(c, "No scripts for processing.\n");
            return false;
        }
        boolean result = false;
        OutputStream stdin = null;
        InputStream stdout;
        try {
            ProcessBuilder pb = new ProcessBuilder(shell);
            pb.directory(new File(ShellResultPreference.getDefaultDir(c)));
            // Map<String, String> env = pb.environment();
            // env.put("PATH", PrefStore.getEnvDir(c) + "/bin:" + env.get("PATH"));
            if (ShellResultPreference.isDebugMode(c)) pb.redirectErrorStream(true);
            Process process = pb.start();

            stdin = process.getOutputStream();
            stdout = process.getInputStream();

            params.add(0, "PATH=" + ShellResultPreference.getDefaultDir(c) + "/bin:$PATH");
            if (ShellResultPreference.isTraceMode(c)) params.add(0, "set -x");
            params.add("exit $?");

            DataOutputStream os = null;
            try {
                os = new DataOutputStream(stdin);
                for (String cmd : params) {
                    os.writeBytes(cmd + "\n");
                }
                os.flush();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                close(os);
            }

            // show stdout log
            final InputStream out = stdout;
            (new Thread() {
                @Override
                public void run() {
                    ShellResult.log(c, out);
                }
            }).start();

            if (process.waitFor() == 0) result = true;
        } catch (Exception e) {
            result = false;
            e.printStackTrace();
        } finally {
            close(stdin);
        }
        return result;
    }

    /**
     * Add file to zip archive
     *
     * @param srcFile file
     * @param zip     zip stream
     */
    private static void addFileToZip(File srcFile, ZipOutputStream zip) {
        byte[] buf = new byte[1024];
        int len;
        FileInputStream in = null;
        try {
            in = new FileInputStream(srcFile);
            while ((len = in.read(buf)) > 0) {
                zip.write(buf, 0, len);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            close(in);
        }
    }

    /**
     * Make zip archive
     *
     * @param c           context
     * @param archiveName archive file name
     */
    public static boolean makeZipArchive(Context c, String archiveName) {
        boolean result = false;
        FileOutputStream f = null;
        ZipOutputStream zip = null;
        try {
            f = new FileOutputStream(archiveName);
            zip = new ZipOutputStream(new BufferedOutputStream(f));
            File busybox = new File(ShellResultPreference.getDefaultDir(c) + "/bin/busybox");
            zip.putNextEntry(new ZipEntry("busybox"));
            addFileToZip(busybox, zip);
            File sslHelper = new File(ShellResultPreference.getDefaultDir(c) + "/bin/ssl_helper");
            zip.putNextEntry(new ZipEntry("ssl_helper"));
            addFileToZip(sslHelper, zip);
            File updateBinary = new File(ShellResultPreference.getDefaultDir(c) + "/scripts/recovery.sh");
            zip.putNextEntry(new ZipEntry("META-INF/com/google/android/update-binary"));
            addFileToZip(updateBinary, zip);
            File addondBinary = new File(ShellResultPreference.getDefaultDir(c) + "/scripts/addon.d.sh");
            zip.putNextEntry(new ZipEntry("addon.d.sh"));
            addFileToZip(addondBinary, zip);

            result = true;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            close(zip);
            close(f);
        }
        return result;
    }

}

