package com.appme.story.application;

import java.security.GeneralSecurityException;

import android.content.Intent;
import android.util.Log;

import com.appme.story.AppConfig;
import com.appme.story.engine.app.terminal.TermDebug;
import com.appme.story.engine.app.terminal.util.ShortcutEncryption;
import com.appme.story.service.TerminalDebianService;

public final class DebianRunShortcut extends DebianRemoteInterface {
    
    @Override
    protected void handleIntent() {
        TerminalDebianService service = getTermService();
        if (service == null) {
            finish();
            return;
        }

        Intent myIntent = getIntent();
        String action = myIntent.getAction();
        if (action.equals(AppConfig.DEBIAN_ACTION_RUN_SHORTCUT)) {
            String encCommand = myIntent.getStringExtra(AppConfig.DEBIAN_EXTRA_SHORTCUT_COMMAND);
            if (encCommand == null) {
                Log.e(TermDebug.LOG_TAG, "No command provided in shortcut!");
                finish();
                return;
            }

            // Decrypt and verify the command
            ShortcutEncryption.Keys keys = ShortcutEncryption.getKeys(this);
            if (keys == null) {
                // No keys -- no valid shortcuts can exist
                Log.e(TermDebug.LOG_TAG, "No shortcut encryption keys found!");
                finish();
                return;
            }
            String command;
            try {
                command = ShortcutEncryption.decrypt(encCommand, keys);
            } catch (GeneralSecurityException e) {
                Log.e(TermDebug.LOG_TAG, "Invalid shortcut: " + e.toString());
                finish();
                return;
            }

            String handle = myIntent.getStringExtra(AppConfig.DEBIAN_EXTRA_WINDOW_HANDLE);
            if (handle != null) {
                // Target the request at an existing window if open
                handle = appendToWindow(handle, command);
            } else {
                // Open a new window
                handle = openNewWindow(command);
            }
            Intent result = new Intent();
            result.putExtra(AppConfig.DEBIAN_EXTRA_WINDOW_HANDLE, handle);
            setResult(RESULT_OK, result);
        }

        finish();
    }
}
