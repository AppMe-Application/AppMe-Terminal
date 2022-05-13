package com.appme.story.application;

import com.appme.story.R;
import com.appme.story.application.base.AppCompatPreferenceActivity;
import com.appme.story.engine.app.terminal.compat.ActionBarCompat;
import com.appme.story.engine.app.terminal.compat.ActivityCompat;
import com.appme.story.engine.app.terminal.compat.AndroidCompat;

import android.support.v7.app.ActionBar;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.view.MenuItem;

public class TerminalPreferences extends AppCompatPreferenceActivity {
    private static final String ACTIONBAR_KEY = "actionbar";
    private static final String CATEGORY_SCREEN_KEY = "screen";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.terminal_preferences);

        // Remove the action bar pref on older platforms without an action bar
        if (AndroidCompat.SDK < 11) {
            Preference actionBarPref = findPreference(ACTIONBAR_KEY);
            PreferenceCategory screenCategory =(PreferenceCategory) findPreference(CATEGORY_SCREEN_KEY);
            if ((actionBarPref != null) && (screenCategory != null)) {
                screenCategory.removePreference(actionBarPref);
            }
        }

        // Display up indicator on action bar home button
        ActionBar bar = getSupportActionBar();
        if (bar != null) {
            bar.setDisplayOptions(ActionBarCompat.DISPLAY_HOME_AS_UP, ActionBarCompat.DISPLAY_HOME_AS_UP);
        }
        
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case ActionBarCompat.ID_HOME:
                // Action bar home button selected
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
