package com.appme.story.application;

import com.appme.story.engine.app.terminal.TermDebug;
import com.appme.story.engine.app.terminal.util.ShortcutEncryption;
import com.appme.story.service.TerminalService;

import java.security.GeneralSecurityException;

import android.content.Intent;
import android.util.Log;

public final class RunShortcut extends RemoteInterface {
    public static final String ACTION_RUN_SHORTCUT = "com.appme.story.RUN_SHORTCUT";

    public static final String EXTRA_WINDOW_HANDLE = "com.appme.story.window_handle";
    public static final String EXTRA_SHORTCUT_COMMAND = "com.appme.story.iShortcutCommand";

    @Override
    protected void handleIntent() {
        TerminalService service = getTermService();
        if (service == null) {
            finish();
            return;
        }

        Intent myIntent = getIntent();
        String action = myIntent.getAction();
        if (action.equals(ACTION_RUN_SHORTCUT)) {
            String encCommand = myIntent.getStringExtra(EXTRA_SHORTCUT_COMMAND);
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

            String handle = myIntent.getStringExtra(EXTRA_WINDOW_HANDLE);
            if (handle != null) {
                // Target the request at an existing window if open
                handle = appendToWindow(handle, command);
            } else {
                // Open a new window
                handle = openNewWindow(command);
            }
            Intent result = new Intent();
            result.putExtra(EXTRA_WINDOW_HANDLE, handle);
            setResult(RESULT_OK, result);
        }

        finish();
    }
}
