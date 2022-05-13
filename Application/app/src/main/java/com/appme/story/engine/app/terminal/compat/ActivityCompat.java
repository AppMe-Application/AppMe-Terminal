package com.appme.story.engine.app.terminal.compat;

import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.ActionBar;

/**
 * Compatibility class for android.app.Activity
 */
public class ActivityCompat {
    private static class Api11OrLater {
        public static void invalidateOptionsMenu(AppCompatActivity activity) {
            activity.supportInvalidateOptionsMenu();
        }

        public static ActionBar getActionBar(AppCompatActivity activity) {
            return activity.getSupportActionBar();
        }
    }

    public static void invalidateOptionsMenu(AppCompatActivity activity) {
        if (AndroidCompat.SDK >= 11) {
            Api11OrLater.invalidateOptionsMenu(activity);
        }
    }

    public static ActionBarCompat getActionBar(AppCompatActivity activity) {
        if (AndroidCompat.SDK < 11) {
            return null;
        }
        return ActionBarCompat.wrap(Api11OrLater.getActionBar(activity));
    }
}

