package com.appme.story;

public class AppConfig {
    static {
        System.loadLibrary("appConfig");
    }

    public static final native String cpuArchFromJNI();

    public static boolean isARM_v7_CPU(String cpuInfoString) {
        return cpuInfoString.contains("v7");
    }

    public static boolean isNeonSupported(String cpuInfoString) {
        // check cpu arch for loading correct ffmpeg lib
        return cpuInfoString.contains("-neon");
    }
    public static final String nh_app_name = "com.appme.story";
    public static final String nh_app_script_path = "/data/data/" + nh_app_name + "/files/scripts";
    public static final String nh_app_script_bin_path = "/data/data/" + nh_app_name + "/files/scripts/bin";
    
    public static final String NH_APP_SCRIPT_PATH = "\"${" + nh_app_script_path + "}\"";
    public static final String NH_APP_SCRIPT_BIN_PATH = "\"${" + nh_app_script_bin_path+ "}\"";
    public static final String APPLICATION_ID = "com.appme.story";
    public static final String OBB_DEBIAN_ARMEL = "https://raw.githubusercontent.com/AppMe-Application/AppMe-Binary/main/Debian/armel/obb/main.9.com.gnuroot.debian.obb";
    public static final String OBB_DEBIAN_ARMHF = "https://raw.githubusercontent.com/AppMe-Application/AppMe-Binary/main/Debian/armhf/obb/main.10.com.gnuroot.debian.obb";
    public static final String OBB_DEBIAN_i386 = "https://raw.githubusercontent.com/AppMe-Application/AppMe-Binary/main/Debian/i386/obb/main.11.com.gnuroot.debian.obb";
    
    public static final String DEBIAN_ACTION_PATH_BROADCAST = "com.appme.story.broadcast.DEBIAN_APPEND_TO_PATH";
    public static final String DEBIAN_ACTION_PATH_PREPEND_BROADCAST = "com.appme.story.broadcast.DEBIAN_PREPEND_TO_PATH";
    public static final String DEBIAN_PERMISSION_PATH_BROADCAST = "com.appme.story.permission.DEBIAN_APPEND_TO_PATH";
    public static final String DEBIAN_PERMISSION_PATH_PREPEND_BROADCAST = "com.appme.story.permission.DEBIAN_PREPEND_TO_PATH";
    public static final String DEBIAN_EXTRA_WINDOW_ID = "com.appme.story.debian_window_id";
    
    //CCX change these to include gnurootdebian
    public static final String DEBIAN_OPEN_NEW_WINDOW = "com.appme.story.private.gnurootdebian.OPEN_NEW_WINDOW";
    public static final String DEBIAN_SWITCH_WINDOW = "com.appme.story.private.gnurootdebian.SWITCH_WINDOW";
    public static final String DEBIAN_EXTRA_TARGET_WINDOW = "com.appme.story.private.debian_target_window";
    public static final String DEBIAN_ACTIVITY_ALIAS = "com.appme.story.application.TerminalDebianInternal";
    
    public static final String DEBIAN_ACTION_RUN_SCRIPT = "com.appme.story.DEBIAN_RUN_SCRIPT";
    public static final String DEBIAN_EXTRA_WINDOW_HANDLE = "com.appme.story.debian_window_handle";
    public static final String DEBIAN_EXTRA_INITIAL_COMMAND = "com.appme.story.iInitialDebianCommand";
    public static final String DEBIAN_ACTION_RUN_SHORTCUT = "com.appme.story.DEBIAN_RUN_SHORTCUT";
    public static final String DEBIAN_EXTRA_SHORTCUT_COMMAND = "com.appme.story.iShortcutCommand";
    
    
    public static final String LAUNCH_DEBIAN_TERMINAL = "com.appme.story.LAUNCH_DEBIAN_TERMINAL";
    public static final String LAUNCH_DEBIAN_REINSTALL = "com.appme.story.LAUNCH_DEBIAN_REINSTALL";
    public static final String LAUNCH_DEBIAN_UPDATE_ERROR = "com.appme.story.LAUNCH_DEBIAN_UPDATE_ERROR";
    public static final String LAUNCH_DEBIAN_TOAST_ALARM = "com.appme.story.LAUNCH_DEBIAN_TOAST_ALARM";
    public static final String INSTALL_TAR = "com.appme.story.INSTALL_TAR";
    public static final String RUN_SCRIPT_STR = "com.appme.story.RUN_SCRIPT_STR";
    public static final String RUN_XSCRIPT_STR = "com.appme.story.RUN_XSCRIPT_STR";
    public static final String RUN_BLOCKING_SCRIPT_STR = "com.appme.story.RUN_BLOCKING_SCRIPT_STR";
    public static final String INSTALL_PACKAGES = "com.appme.story.INSTALL_PACKAGES";
    public static final String CHECK_STATUS = "com.appme.story.CHECK_STATUS";
    public static final String CHECK_PREREQ = "com.appme.story.CHECK_PREREQ";
    public static final String CONNECT_VNC_VIEWER = "com.appme.story.CONNECT_VNC_VIEWER";
    public static final String bVNC_DISCONNECT = "com.appme.story.bVNC_DISCONNECT";
    
}
