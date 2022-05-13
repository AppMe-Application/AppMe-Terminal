package com.appme.story.application;

import android.support.annotation.ColorInt;
import android.support.annotation.ColorRes;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.FileObserver;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.text.TextUtils;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Button;
import android.widget.Toast;

//CCX
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.text.Collator;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import com.appme.story.R;
import com.appme.story.AppConfig;
import com.appme.story.SplashActivity;
import com.appme.story.engine.app.analytics.Analytics;
import com.appme.story.engine.app.fragments.TerminalDebianDrawerFragment;
import com.appme.story.engine.app.adapters.WindowListAdapter;
import com.appme.story.engine.app.terminal.TermView;
import com.appme.story.engine.app.terminal.TermDebug;
import com.appme.story.engine.app.terminal.TerminalDebianView;
import com.appme.story.engine.app.terminal.GenericTermSession;
import com.appme.story.engine.app.terminal.ShellDebianSession;
import com.appme.story.engine.app.terminal.compat.ActionBarCompat;
import com.appme.story.engine.app.terminal.compat.ActivityCompat;
import com.appme.story.engine.app.terminal.compat.AndroidCompat;
import com.appme.story.engine.app.terminal.compat.MenuItemCompat;
import com.appme.story.engine.app.terminal.emulatorview.EmulatorView;
import com.appme.story.engine.app.terminal.emulatorview.TermSession;
import com.appme.story.engine.app.terminal.emulatorview.UpdateCallback;
import com.appme.story.engine.app.terminal.emulatorview.compat.ClipboardManagerCompat;
import com.appme.story.engine.app.terminal.emulatorview.compat.ClipboardManagerCompatFactory;
import com.appme.story.engine.app.terminal.emulatorview.compat.KeycodeConstants;
import com.appme.story.engine.app.terminal.util.SessionList;
import com.appme.story.engine.app.terminal.util.TermSettings;
import com.appme.story.engine.app.folders.FolderMe;
import com.appme.story.engine.app.folders.FileMe;
import com.appme.story.engine.app.utils.KeyboardUtils;
import com.appme.story.engine.widget.ShellResultView;
import com.appme.story.service.TerminalDebianService;


/**
 * A terminal emulator activity.
 */

public class TerminalDebian extends AppCompatActivity implements UpdateCallback, SharedPreferences.OnSharedPreferenceChangeListener, View.OnClickListener {
    /**
     * Per the design guidelines, you should show the drawer on launch until the user manually
     * expands it. This shared preference tracks this.
     */
    private static final String PREF_USER_LEARNED_DRAWER = "navigation_drawer_learned";
    /**
     * Helper component that ties the action bar to the navigation drawer.
     */
    private ActionBarDrawerToggle mDrawerToggle;
    private Toolbar mToolbar;
    private DrawerLayout mDrawerLayout;
    private View mFragmentContainerView;
    private View mFragmentNavigationView;
    private TerminalDebianDrawerFragment mNavigationDrawerFragment;
    private LinearLayout mRootView;
    private RelativeLayout mContentDrawer;
    private RelativeLayout mContentLayout;
    private boolean mUserLearnedDrawer;
    private SharedPreferences sp;
    
    /**
     * The ViewFlipper which holds the collection of EmulatorView widgets.
     */
    private TerminalDebianView mViewFlipper;
    private ShellResultView mShellResult;
    /**
     * The name of the ViewFlipper in the resources.
     */
    private static final int VIEW_FLIPPER = R.id.view_flipper;

    private SessionList mTermSessions;

    private TermSettings mSettings;

    private final static int SELECT_TEXT_ID = 0;
    private final static int COPY_ALL_ID = 1;
    private final static int PASTE_ID = 2;
    private final static int SEND_CONTROL_KEY_ID = 3;
    private final static int SEND_FN_KEY_ID = 4;

    private boolean mAlreadyStarted = false;
    private boolean mStopServiceOnFinish = false;

    private Intent TSIntent;

    public static final int REQUEST_CHOOSE_WINDOW = 1;
    private int onResumeSelectWindow = -1;
    private ComponentName mPrivateAlias;

    private PowerManager.WakeLock mWakeLock;
    private WifiManager.WifiLock mWifiLock;
    // Available on API 12 and later
    private static final int WIFI_MODE_FULL_HIGH_PERF = 3;

    private boolean mBackKeyPressed;

    private FileObserver mObserver;

    AlertDialog.Builder alertDialogBuilder;
    AlertDialog alertDialog = null;
    
    private int mPendingPathBroadcasts = 0;
    private BroadcastReceiver mPathReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String path = makePathFromBundle(getResultExtras(false));
            if (intent.getAction().equals(AppConfig.DEBIAN_ACTION_PATH_PREPEND_BROADCAST)) {
                mSettings.setPrependPath(path);
            } else {
                mSettings.setAppendPath(path);
            }
            mPendingPathBroadcasts--;

            if (mPendingPathBroadcasts <= 0 && mTermService != null) {
                populateViewFlipper();
            }
        }
    };
    // Available on API 12 and later
    private static final int FLAG_INCLUDE_STOPPED_PACKAGES = 0x20;

    private TerminalDebianService mTermService;
    private ServiceConnection mTSConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            Log.i(TermDebug.LOG_TAG, "Bound to TermService");
            TerminalDebianService.TSBinder binder = (TerminalDebianService.TSBinder) service;
            mTermService = binder.getService();
            if (mPendingPathBroadcasts <= 0) {
                populateViewFlipper();
            }
        }

        public void onServiceDisconnected(ComponentName arg0) {
            mTermService = null;
        }
    };

    private ActionBarCompat mActionBar;
    private int mActionBarMode = TermSettings.ACTION_BAR_MODE_NONE;

    private WindowListAdapter mWinListAdapter;

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        mSettings.readPrefs(sharedPreferences);
    }

    private boolean mHaveFullHwKeyboard = false;

    private class EmulatorViewGestureListener extends SimpleOnGestureListener {
        private EmulatorView view;

        public EmulatorViewGestureListener(EmulatorView view) {
            this.view = view;
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            // Let the EmulatorView handle taps if mouse tracking is active
            if (view.isMouseTrackingActive()) return false;

            //Check for link at tap location
            String link = view.getURLat(e.getX(), e.getY());
            if(link != null)
                execURL(link);
            else
                doUIToggle((int) e.getX(), (int) e.getY(), view.getVisibleWidth(), view.getVisibleHeight());
            return true;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            float absVelocityX = Math.abs(velocityX);
            float absVelocityY = Math.abs(velocityY);
            if (absVelocityX > Math.max(1000.0f, 2.0 * absVelocityY)) {
                // Assume user wanted side to side movement
                if (velocityX > 0) {
                    // Left to right swipe -- previous window
                    mViewFlipper.showPrevious();
                } else {
                    // Right to left swipe -- next window
                    mViewFlipper.showNext();
                }
                return true;
            } else {
                return false;
            }
        }
    }

    /**
     * Should we use keyboard shortcuts?
     */
    private boolean mUseKeyboardShortcuts;

    /**
     * Intercepts keys before the view/terminal gets it.
     */
    private View.OnKeyListener mKeyListener = new View.OnKeyListener() {
        public boolean onKey(View v, int keyCode, KeyEvent event) {
            return backkeyInterceptor(keyCode, event) || keyboardShortcuts(keyCode, event);
        }

        /**
         * Keyboard shortcuts (tab management, paste)
         */
        private boolean keyboardShortcuts(int keyCode, KeyEvent event) {
            if (event.getAction() != KeyEvent.ACTION_DOWN) {
                return false;
            }
            if (!mUseKeyboardShortcuts) {
                return false;
            }
            boolean isCtrlPressed = (event.getMetaState() & KeycodeConstants.META_CTRL_ON) != 0;
            boolean isShiftPressed = (event.getMetaState() & KeycodeConstants.META_SHIFT_ON) != 0;

            if (keyCode == KeycodeConstants.KEYCODE_TAB && isCtrlPressed) {
                if (isShiftPressed) {
                    mViewFlipper.showPrevious();
                } else {
                    mViewFlipper.showNext();
                }

                return true;
            } else if (keyCode == KeycodeConstants.KEYCODE_N && isCtrlPressed && isShiftPressed) {
                doCreateNewWindow();

                return true;
            } else if (keyCode == KeycodeConstants.KEYCODE_V && isCtrlPressed && isShiftPressed) {
                doPaste();

                return true;
            } else {
                return false;
            }
        }

        /**
         * Make sure the back button always leaves the application.
         */
        private boolean backkeyInterceptor(int keyCode, KeyEvent event) {
            if (keyCode == KeyEvent.KEYCODE_BACK && mActionBarMode == TermSettings.ACTION_BAR_MODE_HIDES && mActionBar != null && mActionBar.isShowing()) {
                /* We need to intercept the key event before the view sees it,
                   otherwise the view will handle it before we get it */
                onKeyUp(keyCode, event);
                return true;
            } else {
                return false;
            }
        }
    };

    private Handler mHandler = new Handler();

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        Log.v(TermDebug.LOG_TAG, "onCreate");

        mPrivateAlias = new ComponentName(this, AppConfig.DEBIAN_ACTIVITY_ALIAS);

        if (icicle == null)
            onNewIntent(getIntent());

        final SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        mSettings = new TermSettings(getResources(), mPrefs);
        mPrefs.registerOnSharedPreferenceChangeListener(this);

        Intent broadcast = new Intent(AppConfig.DEBIAN_ACTION_PATH_BROADCAST);
        if (AndroidCompat.SDK >= 12) {
            broadcast.addFlags(FLAG_INCLUDE_STOPPED_PACKAGES);
        }
        mPendingPathBroadcasts++;
        sendOrderedBroadcast(broadcast, AppConfig.DEBIAN_PERMISSION_PATH_BROADCAST, mPathReceiver, null, RESULT_OK, null, null);

        broadcast = new Intent(broadcast);
        broadcast.setAction(AppConfig.DEBIAN_ACTION_PATH_PREPEND_BROADCAST);
        mPendingPathBroadcasts++;
        sendOrderedBroadcast(broadcast, AppConfig.DEBIAN_PERMISSION_PATH_PREPEND_BROADCAST, mPathReceiver, null, RESULT_OK, null, null);

        TSIntent = new Intent(this, TerminalDebianService.class);
        startService(TSIntent);

        if (AndroidCompat.SDK >= 11) {
            int actionBarMode = mSettings.actionBarMode();
            mActionBarMode = actionBarMode;
            //CCX need to remove this to for the emulator to populate properly on newer devices
            //if (AndroidCompat.V11ToV20) {
                switch (actionBarMode) {
                case TermSettings.ACTION_BAR_MODE_ALWAYS_VISIBLE:
                    setTheme(R.style.AppTheme_Application);
                    break;
                case TermSettings.ACTION_BAR_MODE_HIDES:
                        setTheme(R.style.AppTheme_Application);
                    break;
                }
            //}
        } else {
            mActionBarMode = TermSettings.ACTION_BAR_MODE_ALWAYS_VISIBLE;
        }

        setContentView(R.layout.activity_terminal_debian);
        mRootView = (LinearLayout)findViewById(R.id.root_view);
        mRootView.setBackgroundColor(color(R.color.windowBackground));

        mToolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        mContentDrawer = (RelativeLayout)findViewById(R.id.layout_drawer);
        mContentDrawer.setBackgroundColor(color(R.color.windowBackground));
        //ScreenUtils.setScaleAnimation(mContentView, 2000);
        mContentLayout = (RelativeLayout)findViewById(R.id.layout_content);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_terminal_layout);
        mFragmentContainerView = findViewById(R.id.view_flipper);
        mFragmentNavigationView = findViewById(R.id.navigation_drawer);
        mViewFlipper = (TerminalDebianView) findViewById(VIEW_FLIPPER); 
        mViewFlipper.setVisibility(View.VISIBLE);
        setFunctionKeyListener();  
        
        mShellResult = (ShellResultView)findViewById(R.id.shell_result_view);
        mShellResult.setVisibility(View.GONE);
        setFunctionKeyListener();
        sp = PreferenceManager.getDefaultSharedPreferences(TerminalDebian.this);
        // ActionBarDrawerToggle ties together the the proper interactions
        // between the navigation drawer and the action bar app icon.
        mDrawerToggle = new ActionBarDrawerToggle(
            this,                    /* host Activity */
            mDrawerLayout,                    /* DrawerLayout object */
            mToolbar,             /* nav drawer image to replace 'Up' caret */
            R.string.action_drawer_open,  /* "open drawer" description for accessibility */
            R.string.action_drawer_close  /* "close drawer" description for accessibility */
        ) {
            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                if (!mHaveFullHwKeyboard) {         
                    KeyboardUtils.showSoftInput(TerminalDebian.this);
                }
                invalidateOptionsMenu(); // calls onPrepareOptionsMenu()
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);

                if (!mUserLearnedDrawer) {
                    // The user manually opened the drawer; store this flag to prevent auto-showing
                    // the navigation drawer automatically in the future.
                    /* Explicitly close the input method
                     Otherwise, the soft keyboard could cover up whatever activity takes
                     our place */       
                    mUserLearnedDrawer = true;
                    sp.edit().putBoolean(PREF_USER_LEARNED_DRAWER, true).apply();
                }
                KeyboardUtils.closeSoftInput(TerminalDebian.this);

                invalidateOptionsMenu(); // calls onPrepareOptionsMenu()
            }
        };

        // If the user hasn't 'learned' about the drawer, open it to introduce them to the drawer,
        // per the navigation drawer design guidelines.
        if (!mUserLearnedDrawer) {
            //KeyboardUtils.closeSoftInput(Terminal.this);
            //mDrawerLayout.openDrawer(mFragmentNavigationView);
        }

        // Defer code dependent on restoration of previous instance state.
        mDrawerLayout.post(new Runnable() {
                @Override
                public void run() {
                    mDrawerToggle.syncState();
                }
            });

        mDrawerLayout.setDrawerListener(mDrawerToggle);
        mUserLearnedDrawer = sp.getBoolean(PREF_USER_LEARNED_DRAWER, false);
        mNavigationDrawerFragment = new TerminalDebianDrawerFragment();
        getSupportFragmentManager()
            .beginTransaction()
            .replace(R.id.navigation_drawer, mNavigationDrawerFragment)
            .commit();
        
        
        PowerManager pm = (PowerManager)getSystemService(Context.POWER_SERVICE);
        mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TermDebug.LOG_TAG);
        WifiManager wm = (WifiManager)getSystemService(Context.WIFI_SERVICE);
        int wifiLockMode = WifiManager.WIFI_MODE_FULL;
        if (AndroidCompat.SDK >= 12) {
            wifiLockMode = WIFI_MODE_FULL_HIGH_PERF;
        }
        mWifiLock = wm.createWifiLock(wifiLockMode, TermDebug.LOG_TAG);
        //CCX added acquire statements
        mWakeLock.acquire();
        mWifiLock.acquire();

        ActionBarCompat actionBar = ActivityCompat.getActionBar(this);
        if (actionBar != null) {
            mActionBar = actionBar;
            actionBar.setDisplayOptions(ActionBarCompat.DISPLAY_SHOW_TITLE, ActionBarCompat.DISPLAY_SHOW_TITLE);
            actionBar.setSubtitle("Debian Mode");
            if (mActionBarMode == TermSettings.ACTION_BAR_MODE_HIDES) {
                actionBar.hide();
            }
        }

        mHaveFullHwKeyboard = checkHaveFullHwKeyboard(getResources().getConfiguration());
        if (mFunctionBar == -1) mFunctionBar = mSettings.showFunctionBar() ? 1 : 0;
        if (mFunctionBar == 0) setFunctionBar(mFunctionBar);
        
        updatePrefs();
        mAlreadyStarted = true;

        //CCX declared and defined this observer and the runnable needed for toast
        mObserver = new FileObserver(Environment.getExternalStorageDirectory() + "/GNURoot/intents") { // set up a file observer to watch this directory on sd card

            @Override
            public void onEvent(int event, String file) {
                if (event == FileObserver.CLOSE_WRITE){ // check if its a "create" and not equal to .probe because thats created every time camera is launched
                    if (file.endsWith("intent")) {
                        try {
                            file = readFileAsString(Environment.getExternalStorageDirectory() + "/GNURoot/intents/" + file);
                        } catch (IOException e) {
                            return;
                        }
                        if (file.startsWith("/home")) {
                            file = Environment.getExternalStorageDirectory() + "/GNURoot" + file;
                        } else {
                            //CCX need to fix this
                            //file = Environment.getDataDirectory() + "/data/com.appme.story/debian/" + file;
                            return;
                        }
                        Intent intent = new Intent(Intent.ACTION_EDIT);
                        Uri uri = Uri.parse("file://"+file);
                        intent.setDataAndType(uri, "text/plain");
                        startActivity(intent);
                    } else if (file.endsWith("png")) {
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        Uri uri = Uri.parse("file://"+Environment.getExternalStorageDirectory() + "/GNURoot/intents/" + file);
                        intent.setDataAndType(uri, "image/png");
                        startActivity(intent);
                    } /*TODO use file observer to make toasts based on intents directory
                    else if (file.endsWith("alarm")) {
                        Intent toastIntent = new Intent("com.gnuroot.debian.TOAST_ALARM");
                        toastIntent.addCategory(Intent.CATEGORY_DEFAULT);
                        String sub = "An unknown command is being applied to GNUroot";
                        int separator = file.indexOf(".");
                        if(separator != -1)
                            sub = file.substring(0, separator);
                        Toast.makeText(getBaseContext(), sub + "... Please wait for completetion.", Toast.LENGTH_LONG).show();
                        toastIntent.putExtra("alarmName", sub);

                        startActivity(toastIntent);
                    } */
                }
            }
        };
        mObserver.startWatching(); //START OBSERVING

    }

    @ColorInt
    private int color(@ColorRes int res) {
        return ContextCompat.getColor(this, res);
    }

    public void closeDrawer() {
        mDrawerLayout.closeDrawer(mFragmentNavigationView);
    }

    public void openDrawer() {
        mDrawerLayout.openDrawer(mFragmentNavigationView);
    }  

    public boolean isDrawerOpen() {
        return mDrawerLayout != null && mDrawerLayout.isDrawerOpen(mFragmentNavigationView);
    }
    
    private static String readFileAsString(String filePath) throws java.io.IOException
    {
        BufferedReader reader = new BufferedReader(new FileReader(filePath));
        String line, results = "";
        while( ( line = reader.readLine() ) != null)
        {
            results += line;
        }
        reader.close();
        return results;
    }
    //CCX added the above function

    private String makePathFromBundle(Bundle extras) {
        if (extras == null || extras.size() == 0) {
            return "";
        }

        String[] keys = new String[extras.size()];
        keys = extras.keySet().toArray(keys);
        Collator collator = Collator.getInstance(Locale.US);
        Arrays.sort(keys, collator);

        StringBuilder path = new StringBuilder();
        for (String key : keys) {
            String dir = extras.getString(key);
            if (dir != null && !dir.equals("")) {
                path.append(dir);
                path.append(":");
            }
        }

        return path.substring(0, path.length()-1);
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (!bindService(TSIntent, mTSConnection, BIND_AUTO_CREATE)) {
            throw new IllegalStateException("Failed to bind to TermService!");
        }
    }

    private void populateViewFlipper() {
        if (mTermService != null) {
            mTermSessions = mTermService.getSessions();

            if (mTermSessions.size() == 0) {
                try {
                    mTermSessions.add(createTermSession());
                } catch (IOException e) {
                    Toast.makeText(this, "Failed to start terminal session", Toast.LENGTH_LONG).show();
                    finish();
                    return;
                }
            }

            mTermSessions.addCallback(this);

            for (TermSession session : mTermSessions) {
                EmulatorView view = createEmulatorView(session);
                mViewFlipper.addView(view);
            }

            updatePrefs();

            if (onResumeSelectWindow >= 0) {
                //CCX changed this back to the old value from onResumeSelectWindow, which is
                //Integer.MAX in cases of new windows
                mViewFlipper.setDisplayedChild(mTermSessions.size()-1);
                onResumeSelectWindow = -1;
            }
            mViewFlipper.onResume();
        }
    }

    public void openWindow(int position) {
        mViewFlipper.setDisplayedChild(position);
        onResumeSelectWindow = position;
        closeDrawer();
        mContentLayout.post(new Runnable() {
                @Override
                public void run() {
                    mViewFlipper.setVisibility(View.VISIBLE);
                    mShellResult.setVisibility(View.GONE);
                }
            });   
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();

        PreferenceManager.getDefaultSharedPreferences(this)
                .unregisterOnSharedPreferenceChangeListener(this);

        if (mStopServiceOnFinish) {
            stopService(TSIntent);
            mFunctionBar = -1;
        }
        mTermService = null;
        mTSConnection = null;
        if (mWakeLock.isHeld()) {
            mWakeLock.release();
        }
        if (mWifiLock.isHeld()) {
            mWifiLock.release();
        }
    }

    private void restart() {
        startActivity(getIntent());
        finish();
    }

    protected static TermSession createTermSession(Context context, TermSettings settings, String initialCommand) throws IOException {
        GenericTermSession session = new ShellDebianSession(settings, initialCommand);
        // XXX We should really be able to fetch this from within TermSession
        session.setProcessExitMessage(context.getString(R.string.terminal_process_exit_message));

        return session;
    }

    private TermSession createTermSession() throws IOException {
        TermSettings settings = mSettings;
        TermSession session = createTermSession(this, settings, settings.getInitialCommand());
        session.setFinishCallback(mTermService);
        return session;
    }

    private TermView createEmulatorView(TermSession session) {
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        TermView emulatorView = new TermView(this, session, metrics);

        emulatorView.setExtGestureListener(new EmulatorViewGestureListener(emulatorView));
        emulatorView.setOnKeyListener(mKeyListener);
        registerForContextMenu(emulatorView);

        return emulatorView;
    }

    private TermSession getCurrentTermSession() {
        SessionList sessions = mTermSessions;
        if (sessions == null) {
            return null;
        } else {
            return sessions.get(mViewFlipper.getDisplayedChild());
        }
    }

    private EmulatorView getCurrentEmulatorView() {
        return (EmulatorView) mViewFlipper.getCurrentView();
    }

    private void updatePrefs() {
        mUseKeyboardShortcuts = mSettings.getUseKeyboardShortcutsFlag();

        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);

        mViewFlipper.updatePrefs(mSettings);
        setFunctionKeyVisibility();
        
        for (View v : mViewFlipper) {
            ((EmulatorView) v).setDensity(metrics);
            ((TermView) v).updatePrefs(mSettings);
        }

        if (mTermSessions != null) {
            for (TermSession session : mTermSessions) {
                ((GenericTermSession) session).updatePrefs(mSettings);
            }
        }

        {
            Window win = getWindow();
            WindowManager.LayoutParams params = win.getAttributes();
            final int FULLSCREEN = WindowManager.LayoutParams.FLAG_FULLSCREEN;
            int desiredFlag = mSettings.showStatusBar() ? 0 : FULLSCREEN;
            if (desiredFlag != (params.flags & FULLSCREEN) || (AndroidCompat.SDK >= 11 && mActionBarMode != mSettings.actionBarMode())) {
                if (mAlreadyStarted) {
                    // Can't switch to/from fullscreen after
                    // starting the activity.
                    restart();
                } else {
                    win.setFlags(desiredFlag, FULLSCREEN);
                    if (mActionBarMode == TermSettings.ACTION_BAR_MODE_HIDES) {
                        if (mActionBar != null) {
                            mActionBar.hide();
                        }
                    }
                }
            }
        }

        int orientation = mSettings.getScreenOrientation();
        int o = 0;
        if (orientation == 0) {
            o = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED;
        } else if (orientation == 1) {
            o = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
        } else if (orientation == 2) {
            o = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
        } else {
            /* Shouldn't be happened. */
        }
        setRequestedOrientation(o);
    }

    @Override
    public void onPause() {
        super.onPause();

        if (AndroidCompat.SDK < 5) {
            /* If we lose focus between a back key down and a back key up,
               we shouldn't respond to the next back key up event unless
               we get another key down first */
            mBackKeyPressed = false;
        }

        /* Explicitly close the input method
           Otherwise, the soft keyboard could cover up whatever activity takes
           our place */
        new Thread() {
            @Override
            public void run() {
                KeyboardUtils.closeSoftInput(TerminalDebian.this);
            }
        }.start();
    }

    @Override
    protected void onStop() {
        mViewFlipper.onPause();
        if (mTermSessions != null) {
            mTermSessions.removeCallback(this);

            if (mWinListAdapter != null) {
                mTermSessions.removeCallback(mWinListAdapter);
                mTermSessions.removeTitleChangedListener(mWinListAdapter);
                mViewFlipper.removeCallback(mWinListAdapter);
            }
        }

        mViewFlipper.removeAllViews();

        unbindService(mTSConnection);

        super.onStop();
    }

    private boolean checkHaveFullHwKeyboard(Configuration c) {
        return (c.keyboard == Configuration.KEYBOARDHIDDEN_YES) && (c.hardKeyboardHidden == Configuration.KEYBOARDHIDDEN_NO);
    }
    
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Forward the new configuration the drawer toggle component.
        mDrawerToggle.onConfigurationChanged(newConfig);

        mHaveFullHwKeyboard = checkHaveFullHwKeyboard(newConfig);

        EmulatorView v = (EmulatorView) mViewFlipper.getCurrentView();
        if (v != null) {
            v.updateSize(false);
        }

        if (mWinListAdapter != null) {
            // Force Android to redraw the label in the navigation dropdown
            mWinListAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_terminal_debian, menu);
        MenuItemCompat.setShowAsAction(menu.findItem(R.id.menu_toggle_soft_keyboard), MenuItemCompat.SHOW_AS_ACTION_ALWAYS);
        MenuItemCompat.setShowAsAction(menu.findItem(R.id.menu_open_android), MenuItemCompat.SHOW_AS_ACTION_ALWAYS);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
         if (mDrawerToggle.onOptionsItemSelected(item)) {
            return(true);
        } else if (id == R.id.menu_toggle_soft_keyboard) {
            KeyboardUtils.doToggleSoftKeyboard(TerminalDebian.this);
        } else if (id == R.id.menu_open_android) {    
            SplashActivity.restart(this, SplashActivity.ACTION_TERMINAL_ACTIVITY);
            finish();
            Analytics.with(this).setTerminal(true);
        } if (id == R.id.menu_gnuroot_reinstall) {
            doGNURootReinstall(); //CCX
        } else if (id == R.id.menu_reset) {
            doResetTerminal();
            Toast toast = Toast.makeText(this,R.string.terminal_reset_toast_notification,Toast.LENGTH_LONG);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
        } else if (id == R.id.menu_send_email) {
            doEmailTranscript();
        } else if (id == R.id.menu_special_keys) {
            doDocumentKeys();
        } else if (id == R.id.menu_toggle_wakelock) {
            doToggleWakeLock();
        } else if (id == R.id.menu_toggle_wifilock) {
            doToggleWifiLock();
        } else if  (id == R.id.action_help) {
                Intent openHelp = new Intent(Intent.ACTION_VIEW,
                Uri.parse(getString(R.string.terminal_help_url)));
                startActivity(openHelp);
        }
        // Hide the action bar if appropriate
        if (mActionBarMode == TermSettings.ACTION_BAR_MODE_HIDES) {
            mActionBar.hide();
        }
        return super.onOptionsItemSelected(item);
    }
    
    private static boolean mVimApp = false;
    private boolean doSendActionBarKey(EmulatorView view, int key) {
        if (key == 999) {
            // do nothing
        } else if (key == 1002) {

            doToggleSoftKeyboard();
        } else if (key == 1249) {
            doPaste();
        } else if (key == 1250) {
            doCreateNewWindow();
        } else if (key == 1251) {
            if (mVimApp && mSettings.getInitialCommand().matches("(.|\n)*(^|\n)-vim\\.app(.|\n)*") && mTermSessions.size() == 1) {
                sendKeyStrings(":confirm qa\r", true);
            } else {
                confirmCloseWindow();
            }
        } else if (key == 1252) {
            InputMethodManager imm = (InputMethodManager)
                getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showInputMethodPicker();
        } else if (key == 1253) {
            sendKeyStrings(":confirm qa\r", true);
        } else if (key == 1254) {
            view.sendFnKeyCode();
        } else if (key == KeycodeConstants.KEYCODE_ALT_LEFT) {
            view.sendAltKeyCode();
        } else if (key == KeycodeConstants.KEYCODE_CTRL_LEFT) {
            view.sendControlKeyCode();
        } else if (key == 1247) {
            sendKeyStrings(":", false);
        } else if (key == 1255) {
            setFunctionBar(2);
        } else if (key > 0) {
            KeyEvent event = new KeyEvent(KeyEvent.ACTION_DOWN, key);
            dispatchKeyEvent(event);
            event = new KeyEvent(KeyEvent.ACTION_UP, key);
            dispatchKeyEvent(event);
        }
        return true;
    }

    private void sendKeyStrings(String str, boolean esc) {
        TermSession session = getCurrentTermSession();
        if (session != null) {
            if (esc) {
                KeyEvent event = new KeyEvent(KeyEvent.ACTION_DOWN, KeycodeConstants.KEYCODE_ESCAPE);
                dispatchKeyEvent(event);
            }
            session.write(str);
        }
    }
    
    
    private void doCreateXterm() {
        Intent newXtermIntent = new Intent(AppConfig.LAUNCH_DEBIAN_TERMINAL);
        newXtermIntent.addCategory(Intent.CATEGORY_DEFAULT);
        newXtermIntent.putExtra("launchType", "launchXTerm");
        newXtermIntent.putExtra("terminal_button", true);
        startActivity(newXtermIntent);
    }

    private void doGNURootReinstall() {
        final AlertDialog.Builder b = new AlertDialog.Builder(this);
        b.setIcon(android.R.drawable.ic_dialog_alert);
        b.setMessage("Are you sure you would like to reinstall GNURoot? This will" +
                " delete all files not stored on the sdcard or in the home directory.");
        final Runnable closeWindow = new Runnable() {
            public void run() {
                doCloseWindow();
                Intent gnuRootReinstall = new Intent(AppConfig.LAUNCH_DEBIAN_REINSTALL);
                gnuRootReinstall.addCategory(Intent.CATEGORY_DEFAULT);
                startActivity(gnuRootReinstall);
            }
        };
        b.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
                mHandler.post(closeWindow);
            }
        });
        b.setNegativeButton(android.R.string.no, null);
        b.show();

    }

    public void doCreateNewWindow() {
        if (mTermSessions == null) {
            Log.w(TermDebug.LOG_TAG, "Couldn't create new window because mTermSessions == null");
            return;
        }

        //CCX create an intent to use launchTerm() in GNURoot
        /*
        try {
            TermSession session = createTermSession();

            mTermSessions.add(session);

            TermView view = createEmulatorView(session);
            view.updatePrefs(mSettings);

            mViewFlipper.addView(view);
            mViewFlipper.setDisplayedChild(mViewFlipper.getChildCount()-1);
        } catch (IOException e) {
            Toast.makeText(this, "Failed to create a session", Toast.LENGTH_SHORT).show();
        }
        */

        Intent newWindowIntent = new Intent(AppConfig.LAUNCH_DEBIAN_TERMINAL);
        newWindowIntent.addCategory(Intent.CATEGORY_DEFAULT);
        newWindowIntent.putExtra("launchType", "launchTerm");
        startActivity(newWindowIntent);

    }

    private void confirmCloseWindow() {
        final AlertDialog.Builder b = new AlertDialog.Builder(this);
        b.setIcon(android.R.drawable.ic_dialog_alert);
        b.setMessage(R.string.confirm_window_close_message);
        final Runnable closeWindow = new Runnable() {
            public void run() {
                doCloseWindow();
            }
        };
        b.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
           public void onClick(DialogInterface dialog, int id) {
               dialog.dismiss();
               mHandler.post(closeWindow);
           }
        });
        b.setNegativeButton(android.R.string.no, null);
        b.show();
    }

    private void doCloseWindow() {
        if (mTermSessions == null) {
            return;
        }

        EmulatorView view = getCurrentEmulatorView();
        if (view == null) {
            return;
        }
        TermSession session = mTermSessions.remove(mViewFlipper.getDisplayedChild());
        view.onPause();
        session.finish();
        mViewFlipper.removeView(view);
        if (mTermSessions.size() != 0) {
            mViewFlipper.showNext();
        }
    }

    @Override
    protected void onActivityResult(int request, int result, Intent data) {
        switch (request) {
        case REQUEST_CHOOSE_WINDOW:
            if (result == RESULT_OK && data != null) {
                int position = data.getIntExtra(AppConfig.DEBIAN_EXTRA_WINDOW_ID, -2);
                if (position >= 0) {
                    // Switch windows after session list is in sync, not here
                    onResumeSelectWindow = position;
                } else if (position == -1) {
                    doCreateNewWindow();
                    onResumeSelectWindow = mTermSessions.size() - 1;
                }
            } else {
                // Close the activity if user closed all sessions
                // TODO the left path will be invoked when nothing happened, but this Activity was destroyed!
                if (mTermSessions == null || mTermSessions.size() == 0) {
                    mStopServiceOnFinish = true;
                    finish();
                }
            }
            break;
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        if ((intent.getFlags() & Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY) != 0) {
            // Don't repeat action if intent comes from history
            return;
        }

        String action = intent.getAction();
        if (TextUtils.isEmpty(action) || !mPrivateAlias.equals(intent.getComponent())) {
            return;
        }

        // huge number simply opens new window
        // TODO: add a way to restrict max number of windows per caller (possibly via reusing BoundSession)
        switch (action) {
            case AppConfig.DEBIAN_OPEN_NEW_WINDOW:
                onResumeSelectWindow = Integer.MAX_VALUE;
                break;
            case AppConfig.DEBIAN_SWITCH_WINDOW:
                int target = intent.getIntExtra(AppConfig.DEBIAN_EXTRA_TARGET_WINDOW, -1);
                if (target >= 0) {
                    onResumeSelectWindow = target;
                }
                break;
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem wakeLockItem = menu.findItem(R.id.menu_toggle_wakelock);
        MenuItem wifiLockItem = menu.findItem(R.id.menu_toggle_wifilock);
        if (mWakeLock.isHeld()) {
            wakeLockItem.setTitle(R.string.terminal_disable_wakelock);
        } else {
            wakeLockItem.setTitle(R.string.terminal_enable_wakelock);
        }
        if (mWifiLock.isHeld()) {
            wifiLockItem.setTitle(R.string.terminal_disable_wifilock);
        } else {
            wifiLockItem.setTitle(R.string.terminal_enable_wifilock);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
            ContextMenuInfo menuInfo) {
      super.onCreateContextMenu(menu, v, menuInfo);
      menu.setHeaderTitle(R.string.terminal_edit_text);
      menu.add(0, SELECT_TEXT_ID, 0, R.string.terminal_select_text);
      menu.add(0, COPY_ALL_ID, 0, R.string.terminal_copy_all);
      menu.add(0, PASTE_ID, 0, R.string.terminal_paste);
      menu.add(0, SEND_CONTROL_KEY_ID, 0, R.string.terminal_send_control_key);
      menu.add(0, SEND_FN_KEY_ID, 0, R.string.terminal_send_fn_key);
      if (!canPaste()) {
          menu.getItem(PASTE_ID).setEnabled(false);
      }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
          switch (item.getItemId()) {
          case SELECT_TEXT_ID:
            getCurrentEmulatorView().toggleSelectingText();
            return true;
          case COPY_ALL_ID:
            doCopyAll();
            return true;
          case PASTE_ID:
            doPaste();
            return true;
          case SEND_CONTROL_KEY_ID:
            doSendControlKey();
            return true;
          case SEND_FN_KEY_ID:
            doSendFnKey();
            return true;
          default:
            return super.onContextItemSelected(item);
          }
        }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        /* The pre-Eclair default implementation of onKeyDown() would prevent
           our handling of the Back key in onKeyUp() from taking effect, so
           ignore it here */
        if (AndroidCompat.SDK < 5 && keyCode == KeyEvent.KEYCODE_BACK) {
            /* Android pre-Eclair has no key event tracking, and a back key
               down event delivered to an activity above us in the back stack
               could be succeeded by a back key up event to us, so we need to
               keep track of our own back key presses */
            mBackKeyPressed = true;
            return true;
        } else {
            return super.onKeyDown(keyCode, event);
        }
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:

                if (AndroidCompat.SDK < 5) {
                    if (!mBackKeyPressed) {
                        /* This key up event might correspond to a key down
                         delivered to another activity -- ignore */
                        return false;
                    }
                    mBackKeyPressed = false;
                }
                if (mActionBarMode == TermSettings.ACTION_BAR_MODE_HIDES && mActionBar != null && mActionBar.isShowing()) {
                    mActionBar.hide();
                    return true;
                }


                switch (mSettings.getBackKeyAction()) {
                    case TermSettings.BACK_KEY_STOPS_SERVICE:
                        if (isDrawerOpen()) {
                            closeDrawer();
                        } else {
                            mStopServiceOnFinish = true;
                        }
                        return true;
                    case TermSettings.BACK_KEY_CLOSES_ACTIVITY:
                        if (isDrawerOpen()) {
                            closeDrawer();
                        } else {
                            finish();
                        }
                        return true;
                    case TermSettings.BACK_KEY_CLOSES_WINDOW:
                        doCloseWindow();
                        return true;
                    default:
                        return false;
                }
            case KeyEvent.KEYCODE_MENU:
                if (mActionBar != null && !mActionBar.isShowing()) {
                    if (isDrawerOpen()) {
                        closeDrawer();
                    } else {
                        mActionBar.show();
                    }
                    return true;
                } else {
                    return super.onKeyUp(keyCode, event);
                }
            default:
                return super.onKeyUp(keyCode, event);
        }
    }
    // Called when the list of sessions changes
    public void onUpdate() {
        SessionList sessions = mTermSessions;
        if (sessions == null) {
            return;
        }

        if (sessions.size() == 0) {
            mStopServiceOnFinish = true;
            finish();
        } else if (sessions.size() < mViewFlipper.getChildCount()) {
            for (int i = 0; i < mViewFlipper.getChildCount(); ++i) {
                EmulatorView v = (EmulatorView) mViewFlipper.getChildAt(i);
                if (!sessions.contains(v.getTermSession())) {
                    v.onPause();
                    mViewFlipper.removeView(v);
                    --i;
                }
            }
        }
    }

    private boolean canPaste() {
        ClipboardManagerCompat clip = ClipboardManagerCompatFactory
                .getManager(getApplicationContext());
        if (clip.hasText()) {
            return true;
        }
        return false;
    }

    private void doPreferences() {
        startActivity(new Intent(this, TerminalPreferences.class));
    }

    private void doResetTerminal() {
        TermSession session = getCurrentTermSession();
        if (session != null) {
            session.reset();
        }
    }

    private void doEmailTranscript() {
        TermSession session = getCurrentTermSession();
        if (session != null) {
            // Don't really want to supply an address, but
            // currently it's required, otherwise nobody
            // wants to handle the intent.
            String addr = "user@example.com";
            Intent intent =
                    new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:"
                            + addr));

            String subject = getString(R.string.email_transcript_subject);
            String title = session.getTitle();
            if (title != null) {
                subject = subject + " - " + title;
            }
            intent.putExtra(Intent.EXTRA_SUBJECT, subject);
            intent.putExtra(Intent.EXTRA_TEXT,
                    session.getTranscriptText().trim());
            try {
                startActivity(Intent.createChooser(intent,
                        getString(R.string.email_transcript_chooser_title)));
            } catch (ActivityNotFoundException e) {
                Toast.makeText(this,
                        R.string.email_transcript_no_email_activity_found,
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    private void doCopyAll() {
        ClipboardManagerCompat clip = ClipboardManagerCompatFactory
                .getManager(getApplicationContext());
        clip.setText(getCurrentTermSession().getTranscriptText().trim());
    }

    private void doPaste() {
        if (!canPaste()) {
            return;
        }
        ClipboardManagerCompat clip = ClipboardManagerCompatFactory
                .getManager(getApplicationContext());
        CharSequence paste = clip.getText();
        getCurrentTermSession().write(paste.toString());
    }

    private void doSendControlKey() {
        getCurrentEmulatorView().sendControlKey();
    }

    private void doSendFnKey() {
        getCurrentEmulatorView().sendFnKey();
    }

    private void doDocumentKeys() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        Resources r = getResources();
        dialog.setTitle(r.getString(R.string.control_key_dialog_title));
        dialog.setMessage(
            formatMessage(mSettings.getControlKeyId(), TermSettings.CONTROL_KEY_ID_NONE,
                r, R.array.control_keys_short_names,
                R.string.control_key_dialog_control_text,
                R.string.control_key_dialog_control_disabled_text, "CTRLKEY")
            + "\n\n" +
            formatMessage(mSettings.getFnKeyId(), TermSettings.FN_KEY_ID_NONE,
                r, R.array.fn_keys_short_names,
                R.string.control_key_dialog_fn_text,
                R.string.control_key_dialog_fn_disabled_text, "FNKEY"));
         dialog.show();
     }

     private String formatMessage(int keyId, int disabledKeyId,
         Resources r, int arrayId,
         int enabledId,
         int disabledId, String regex) {
         if (keyId == disabledKeyId) {
             return r.getString(disabledId);
         }
         String[] keyNames = r.getStringArray(arrayId);
         String keyName = keyNames[keyId];
         String template = r.getString(enabledId);
         String result = template.replaceAll(regex, keyName);
         return result;
    }

    private void doToggleSoftKeyboard() {
        InputMethodManager imm = (InputMethodManager)
            getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED,0);

    }

    private void doToggleWakeLock() {
        if (mWakeLock.isHeld()) {
            mWakeLock.release();
        } else {
            mWakeLock.acquire();
        }
        ActivityCompat.invalidateOptionsMenu(this);
    }

    private void doToggleWifiLock() {
        if (mWifiLock.isHeld()) {
            mWifiLock.release();
        } else {
            mWifiLock.acquire();
        }
        ActivityCompat.invalidateOptionsMenu(this);
    }

    private void doToggleActionBar() {
        ActionBarCompat bar = mActionBar;
        if (bar == null) {
            return;
        }
        if (bar.isShowing()) {
            bar.hide();
        } else {
            bar.show();
        }
    }

    private void doUIToggle(int x, int y, int width, int height) {
        switch (mActionBarMode) {
            case TermSettings.ACTION_BAR_MODE_NONE:
                if (AndroidCompat.SDK >= 11 && (mHaveFullHwKeyboard || y < height / 2)) {
                    openOptionsMenu();
                    return;
                } else {
                    openDrawer();
                    //doToggleSoftKeyboard();
                }
                break;
            case TermSettings.ACTION_BAR_MODE_ALWAYS_VISIBLE:
                if (!mHaveFullHwKeyboard) {
                    openDrawer();
                    //doToggleSoftKeyboard();
                }
                break;
            case TermSettings.ACTION_BAR_MODE_HIDES:
                if (mHaveFullHwKeyboard || y < height / 2) {
                    doToggleActionBar();
                    return;
                } else {
                    openDrawer();
                    //doToggleSoftKeyboard();
                }
                break;
        }
        getCurrentEmulatorView().requestFocus();
    }

    /**
     *
     * Send a URL up to Android to be handled by a browser.
     * @param link The URL to be opened.
     */
    private void execURL(String link)
    {
        Uri webLink = Uri.parse(link);
        Intent openLink = new Intent(Intent.ACTION_VIEW, webLink);
        PackageManager pm = getPackageManager();
        List<ResolveInfo> handlers = pm.queryIntentActivities(openLink, 0);
        if(handlers.size() > 0)
            startActivity(openLink);
    }
    
    private static int mFunctionBar = -1;

    private void setFunctionBar(int mode) {
        if (mode == 2) mFunctionBar = mFunctionBar == 0 ? 1 : 0;
        else mFunctionBar = mode;
        if (mAlreadyStarted) updatePrefs();
    }

    private void setFunctionBarSize() {
        int size = findViewById(R.id.view_function_bar).getHeight();
        if (mViewFlipper != null) mViewFlipper.setFunctionBarSize(size);
    }

    private void setFunctionKeyListener() {
        findViewById(R.id.button_esc  ).setOnClickListener(this);
        findViewById(R.id.button_ctrl ).setOnClickListener(this);
        findViewById(R.id.button_alt ).setOnClickListener(this);
        findViewById(R.id.button_tab  ).setOnClickListener(this);
        findViewById(R.id.button_up   ).setOnClickListener(this);
        findViewById(R.id.button_down ).setOnClickListener(this);
        findViewById(R.id.button_left ).setOnClickListener(this);
        findViewById(R.id.button_right).setOnClickListener(this);
        findViewById(R.id.button_backspace).setOnClickListener(this);
        findViewById(R.id.button_enter).setOnClickListener(this);
        findViewById(R.id.button_i).setOnClickListener(this);
        findViewById(R.id.button_colon).setOnClickListener(this);
        findViewById(R.id.button_slash).setOnClickListener(this);
        findViewById(R.id.button_equal).setOnClickListener(this);
        findViewById(R.id.button_asterisk).setOnClickListener(this);
        findViewById(R.id.button_pipe).setOnClickListener(this);
        findViewById(R.id.button_minus).setOnClickListener(this);
        findViewById(R.id.button_vim_paste).setOnClickListener(this);
        findViewById(R.id.button_vim_yank).setOnClickListener(this);
        findViewById(R.id.button_softkeyboard).setOnClickListener(this);
        findViewById(R.id.button_menu).setOnClickListener(this);
        findViewById(R.id.button_menu_hide).setOnClickListener(this);
        findViewById(R.id.button_menu_plus ).setOnClickListener(this);
        findViewById(R.id.button_menu_minus).setOnClickListener(this);
        findViewById(R.id.button_menu_x    ).setOnClickListener(this);
        findViewById(R.id.button_menu_user ).setOnClickListener(this);
        findViewById(R.id.button_menu_quit ).setOnClickListener(this);
    }

    private void setFunctionKeyVisibility() {
        int visibility;
        final SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(this);

        visibility = mPrefs.getBoolean("functionbar_esc", true) ? View.VISIBLE : View.GONE;
        setFunctionBarButton(R.id.button_esc, visibility);
        visibility = mPrefs.getBoolean("functionbar_ctrl", true) ? View.VISIBLE : View.GONE;
        setFunctionBarButton(R.id.button_ctrl, visibility);
        visibility = mPrefs.getBoolean("functionbar_alt", true) ? View.VISIBLE : View.GONE;
        setFunctionBarButton(R.id.button_alt, visibility);
        visibility = mPrefs.getBoolean("functionbar_tab", true) ? View.VISIBLE : View.GONE;
        setFunctionBarButton(R.id.button_tab, visibility);

        visibility = mPrefs.getBoolean("functionbar_up", true) ? View.VISIBLE : View.GONE;
        setFunctionBarButton(R.id.button_up, visibility);
        visibility = mPrefs.getBoolean("functionbar_down", true) ? View.VISIBLE : View.GONE;
        setFunctionBarButton(R.id.button_down, visibility);
        visibility = mPrefs.getBoolean("functionbar_left", true) ? View.VISIBLE : View.GONE;
        setFunctionBarButton(R.id.button_left, visibility);
        visibility = mPrefs.getBoolean("functionbar_right", true) ? View.VISIBLE : View.GONE;
        setFunctionBarButton(R.id.button_right, visibility);

        visibility = mPrefs.getBoolean("functionbar_backspace", false) ? View.VISIBLE : View.GONE;
        setFunctionBarButton(R.id.button_backspace, visibility);
        visibility = mPrefs.getBoolean("functionbar_enter", true) ? View.VISIBLE : View.GONE;
        setFunctionBarButton(R.id.button_enter, visibility);

        visibility = mPrefs.getBoolean("functionbar_i", false) ? View.VISIBLE : View.GONE;
        setFunctionBarButton(R.id.button_i, visibility);
        visibility = mPrefs.getBoolean("functionbar_colon", false) ? View.VISIBLE : View.GONE;
        setFunctionBarButton(R.id.button_colon, visibility);
        visibility = mPrefs.getBoolean("functionbar_slash", true) ? View.VISIBLE : View.GONE;
        setFunctionBarButton(R.id.button_slash, visibility);
        visibility = mPrefs.getBoolean("functionbar_equal", false) ? View.VISIBLE : View.GONE;
        setFunctionBarButton(R.id.button_equal, visibility);
        visibility = mPrefs.getBoolean("functionbar_asterisk", false) ? View.VISIBLE : View.GONE;
        setFunctionBarButton(R.id.button_asterisk, visibility);
        visibility = mPrefs.getBoolean("functionbar_pipe", true) ? View.VISIBLE : View.GONE;
        setFunctionBarButton(R.id.button_pipe, visibility);
        visibility = mPrefs.getBoolean("functionbar_minus", true) ? View.VISIBLE : View.GONE;
        setFunctionBarButton(R.id.button_minus, visibility);
        visibility = mPrefs.getBoolean("functionbar_vim_paste", true) ? View.VISIBLE : View.GONE;
        setFunctionBarButton(R.id.button_vim_paste, visibility);
        visibility = mPrefs.getBoolean("functionbar_vim_yank", false) ? View.VISIBLE : View.GONE;
        setFunctionBarButton(R.id.button_vim_yank, visibility);

        visibility = mPrefs.getBoolean("functionbar_menu", true) ? View.VISIBLE : View.GONE;
        setFunctionBarButton(R.id.button_menu, visibility);
        visibility = mPrefs.getBoolean("functionbar_softkeyboard", true) ? View.VISIBLE : View.GONE;
        setFunctionBarButton(R.id.button_softkeyboard, visibility);
        visibility = mPrefs.getBoolean("functionbar_hide", true) ? View.VISIBLE : View.GONE;
        setFunctionBarButton(R.id.button_menu_hide, visibility);

        visibility = View.GONE;
        // visibility = mPrefs.getBoolean("functionbar_menu_plus", false)  ? View.VISIBLE : View.GONE;
        setFunctionBarButton(R.id.button_menu_plus, visibility);
        // visibility = mPrefs.getBoolean("functionbar_menu_minus", false) ? View.VISIBLE : View.GONE;
        setFunctionBarButton(R.id.button_menu_minus, visibility);
        // visibility = mPrefs.getBoolean("functionbar_menu_x", false) ? View.VISIBLE : View.GONE;
        setFunctionBarButton(R.id.button_menu_x, visibility);
        // visibility = mPrefs.getBoolean("functionbar_menu_user", false)  ? View.VISIBLE : View.GONE;
        setFunctionBarButton(R.id.button_menu_user, visibility);
        visibility = mPrefs.getBoolean("functionbar_menu_quit", true)  ? View.VISIBLE : View.GONE;
        setFunctionBarButton(R.id.button_menu_quit, visibility);

        setFunctionBarSize();
        visibility = mFunctionBar == 1 ? View.VISIBLE : View.GONE;
        findViewById(R.id.view_function_bar).setVisibility(visibility);
        mViewFlipper.setFunctionBar(mFunctionBar == 1);
    }

    @SuppressLint("NewApi")
    private void setFunctionBarButton(int id, int visibility) {
        Button button = (Button)findViewById(id);
        button.setVisibility(visibility);
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int height = mSettings.getFontSize() * (int) (metrics.density * metrics.scaledDensity);
        button.setMinHeight(height);
        if (AndroidCompat.SDK >= 14) {
            button.setAllCaps(false);
        }
    }

    @Override
    public void onClick(View v) {
        EmulatorView view = getCurrentEmulatorView();
        switch (v.getId()) {
            case R.id.button_esc:
                doSendActionBarKey(view, KeycodeConstants.KEYCODE_ESCAPE);
                break;
            case R.id.button_ctrl:
                doSendActionBarKey(view, KeycodeConstants.KEYCODE_CTRL_LEFT);
                break;
            case R.id.button_alt:
                doSendActionBarKey(view, KeycodeConstants.KEYCODE_ALT_LEFT);
                break;
            case R.id.button_tab:
                doSendActionBarKey(view, KeycodeConstants.KEYCODE_TAB);
                break;
            case R.id.button_up:
                doSendActionBarKey(view, KeycodeConstants.KEYCODE_DPAD_UP);
                break;
            case R.id.button_down:
                doSendActionBarKey(view, KeycodeConstants.KEYCODE_DPAD_DOWN);
                break;
            case R.id.button_left:
                doSendActionBarKey(view, KeycodeConstants.KEYCODE_DPAD_LEFT);
                break;
            case R.id.button_right:
                doSendActionBarKey(view, KeycodeConstants.KEYCODE_DPAD_RIGHT);
                break;
            case R.id.button_backspace:
                doSendActionBarKey(view, KeycodeConstants.KEYCODE_DEL);
                break;
            case R.id.button_enter:
                doSendActionBarKey(view, KeycodeConstants.KEYCODE_ENTER);
                break;
            case R.id.button_i:
                sendKeyStrings("i", false);
                break;
            case R.id.button_colon:
                sendKeyStrings(":", false);
                break;
            case R.id.button_slash:
                sendKeyStrings("/", false);
                break;
            case R.id.button_equal:
                sendKeyStrings("=", false);
                break;
            case R.id.button_asterisk:
                sendKeyStrings("*", false);
                break;
            case R.id.button_pipe:
                sendKeyStrings("|", false);
                break;
            case R.id.button_minus:
                sendKeyStrings("-", false);
                break;
            case R.id.button_vim_paste:
                doPaste();
                //sendKeyStrings("\"*p", false);
                break;
            case R.id.button_vim_yank:
                sendKeyStrings("\"*yy", false);
                break;
            case R.id.button_menu_plus:
                doSendActionBarKey(view, mSettings.getActionBarPlusKeyAction());
                break;
            case R.id.button_menu_minus:
                doSendActionBarKey(view, mSettings.getActionBarMinusKeyAction());
                break;
            case R.id.button_menu_x:
                doSendActionBarKey(view, mSettings.getActionBarXKeyAction());
                break;
            case R.id.button_menu_user:
                doSendActionBarKey(view, mSettings.getActionBarUserKeyAction());
                break;
            case R.id.button_menu_quit:
                doSendActionBarKey(view, mSettings.getActionBarQuitKeyAction());
                break;
            case R.id.button_softkeyboard:
                doSendActionBarKey(view, mSettings.getActionBarIconKeyAction());
                break;
            case R.id.button_menu:
                openOptionsMenu();
                break;
            case R.id.button_menu_hide:
                setFunctionBar(2);
                break;
        }
    }
}
