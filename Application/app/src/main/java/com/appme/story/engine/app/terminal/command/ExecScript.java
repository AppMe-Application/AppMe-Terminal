package com.appme.story.engine.app.terminal.command;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

import com.appme.story.settings.ShellResultPreference;

public class ExecScript extends Thread {

    private Context context;
    private String command;

    public ExecScript(Context c, String command) {
        this.context = c;
        this.command = command;
    }

    private void info() {
        String envDir = ShellResultPreference.getDefaultDir(context) + "/etc";
        List<String> params = new ArrayList<>();
        params.add("ENV_DIR=" + envDir);
        params.add("INSTALL_DIR=" + ShellResultPreference.getInstallDir(context));
        params.add(". " + envDir + "/scripts/info.sh");
        ShellResultUtils.exec(context, "sh", params);
    }

    private void install() {
        // check root
        if (!ShellResultUtils.isRooted(context)) return;
        String envDir = ShellResultPreference.getDefaultDir(context) + "/etc";
        List<String> params = new ArrayList<>();
        params.add("ENV_DIR=" + envDir);
        params.add("INSTALL_DIR=" + ShellResultPreference.getInstallDir(context));
        params.add("INSTALL_APPLETS=" + ShellResultPreference.isInstallApplets(context));
        params.add("REPLACE_APPLETS=" + ShellResultPreference.isReplaceApplets(context));
        params.add("MOUNT_RAMDISK=" + ShellResultPreference.isRamDisk(context));
        params.add(". " + envDir + "/scripts/install.sh");
        ShellResultUtils.exec(context, "su", params);
    }

    private void remove() {
        // check root
        if (!ShellResultUtils.isRooted(context)) return;
        String envDir = ShellResultPreference.getDefaultDir(context) + "/etc";
        List<String> params = new ArrayList<>();
        params.add("INSTALL_DIR=" + ShellResultPreference.getInstallDir(context));
        params.add(". " + envDir + "/scripts/remove.sh");
        ShellResultUtils.exec(context, "su", params);
    }

    @Override
    public void run() {
        ShellResult.clear();
        // update env
        //if (!ShellResultUtils.update(context)) return;
        // exec command
        switch (command) {
            case "info":
                info();
                break;
            case "install":
                ShellResult.log(context, "### BEGIN INSTALL\n");
                install();
                ShellResult.log(context, "### END\n");
                break;
            case "remove":
                ShellResult.log(context, "### BEGIN REMOVE\n");
                remove();
                ShellResult.log(context, "### END\n");
                break;
        }
    }

}

