package com.appme.story.engine.app.fragments;

import android.support.annotation.ColorInt;
import android.support.annotation.ColorRes;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;

import android.os.Environment;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.ListView;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ImageView;

import java.util.Arrays;
import java.util.ArrayList;

import com.appme.story.R;
import com.appme.story.application.TerminalDebian;
import com.appme.story.engine.app.adapters.WindowListAdapter;
import com.appme.story.engine.app.terminal.TermDebug;
import com.appme.story.engine.app.terminal.compat.ActionBarCompat;
import com.appme.story.engine.app.terminal.compat.ActivityCompat;
import com.appme.story.engine.app.terminal.compat.AndroidCompat;
import com.appme.story.engine.app.terminal.util.SessionList;
import com.appme.story.engine.app.listeners.ObservableScrollViewCallbacks;
import com.appme.story.engine.widget.ObservableListView;
import com.appme.story.engine.widget.helper.ScrollState;
import com.appme.story.service.TerminalDebianService;

public class TerminalDebianDrawerFragment extends Fragment implements AdapterView.OnItemClickListener, ObservableScrollViewCallbacks, View.OnClickListener {
    
    private static final String TAG = TerminalDebianDrawerFragment.class.getSimpleName();

    /**
     * The fragment argument representing the section number for this
     * fragment.
     */
    private static final String ARG_NAV_DRAWER_MESSAGE = "ARG_NAV_DRAWER_MESSAGE";
    //private String message;
    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static TerminalDebianDrawerFragment newInstance(int number) {
        TerminalDebianDrawerFragment fragment = new TerminalDebianDrawerFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_NAV_DRAWER_MESSAGE, number);
        Log.d(TAG, "newInstance: id = " + number);
        fragment.setArguments(args);
        return fragment;
    }

    public TerminalDebianDrawerFragment() {
    }

    private TerminalDebian mActivity;
    private Context mContext;
    private RelativeLayout mHeaderDrawer;
    private RelativeLayout mFooterDrawer;
    private ObservableListView mDrawerListMenu;
    //private OnRecyclerScrollListener mOnRecyclerScrollListener;
    private View mFooter;
    private SessionList sessions;
    private WindowListAdapter mWindowListAdapter;
    private TerminalDebianService mTermService;

    private ServiceConnection mTSConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            TerminalDebianService.TSBinder binder = (TerminalDebianService.TSBinder) service;
            mTermService = binder.getService();
            populateList();
        }

        public void onServiceDisconnected(ComponentName arg0) {
            mTermService = null;
        }
    };
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //Log.d(TAG, "onCreateView: onCreateView, id = " + message);
        View rootView = inflater.inflate(R.layout.fragment_terminal_drawer, container, false);
        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mActivity = (TerminalDebian)getActivity();
        mContext = getActivity();
        
        mHeaderDrawer = (RelativeLayout) view.findViewById(R.id.header_drawer); 
        mDrawerListMenu = (ObservableListView) view.findViewById(R.id.menu_list_drawer_recycler);       
        //View newWindow = getLayoutInflater().inflate(R.layout.window_list_new_window, mDrawerListMenu, false);
        //mDrawerListMenu.addHeaderView(newWindow, null, true);
        mDrawerListMenu.setOnItemClickListener(this);
        mDrawerListMenu.setScrollViewCallbacks(this);
        
        mActivity.setResult(Activity.RESULT_CANCELED);
        
        mFooterDrawer = (RelativeLayout) view.findViewById(R.id.footer_drawer);
        mFooter = view.findViewById(R.id.footer_layout);
        
        view.findViewById(R.id.menu_drawer_1).setOnClickListener(this);
        ImageView icon1 = (ImageView)view.findViewById(R.id.icon_menu_1);
        icon1.setImageResource(R.drawable.ic_menu_home);
        TextView text1 = (TextView)view.findViewById(R.id.text_menu_1);
        text1.setText("Home");
        ImageView icon_menu_single = (ImageView)view.findViewById(R.id.menu_drawer_single);
        icon_menu_single.setImageResource(R.drawable.ic_menu_add_new);
        icon_menu_single.setOnClickListener(this);       
        view.findViewById(R.id.menu_drawer_2).setOnClickListener(this);
        ImageView icon2 = (ImageView)view.findViewById(R.id.icon_menu_2);
        icon2.setImageResource(R.drawable.ic_menu_settings);
        TextView text2 = (TextView)view.findViewById(R.id.text_menu_2);
        text2.setText(R.string.action_drawer_settings);
        view.findViewById(R.id.menu_drawer_3).setOnClickListener(this);
        ImageView icon3 = (ImageView)view.findViewById(R.id.icon_menu_3);
        icon3.setImageResource(R.drawable.ic_menu_exit);
        TextView text3 = (TextView)view.findViewById(R.id.text_menu_3);
        text3.setText(R.string.action_drawer_exit);
        //changeColor();
    }
    

    @Override
    public void onScrollChanged(int scrollY, boolean firstScroll, boolean dragging) {
    }

    @Override
    public void onDownMotionEvent() {
    }

    @Override
    public void onUpOrCancelMotionEvent(ScrollState scrollState) {
        if (scrollState == ScrollState.UP) {
            RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) mFooter.getLayoutParams();
            int fabMargin = lp.bottomMargin;
            mFooter.animate().translationY(mFooter.getHeight() + fabMargin).setInterpolator(new AccelerateInterpolator(2.0f)).start();

        } else if (scrollState == ScrollState.DOWN) {
            mFooter.animate().translationY(0).setInterpolator(new DecelerateInterpolator(2)).start();                     
        }
    }
    
    @Override
    public void onResume() {
        super.onResume();

        Intent TSIntent = new Intent(mActivity, TerminalDebianService.class);
        if (!mActivity.bindService(TSIntent, mTSConnection, Activity.BIND_AUTO_CREATE)) {
            Log.w(TermDebug.LOG_TAG, "bind to service failed!");
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        WindowListAdapter adapter = mWindowListAdapter;
        if (sessions != null) {
            sessions.removeCallback(adapter);
            sessions.removeTitleChangedListener(adapter);
        }
        if (adapter != null) {
            adapter.setSessions(null);
        }
        mActivity.unbindService(mTSConnection);
    }

    private void populateList() {
        sessions = mTermService.getSessions();
        WindowListAdapter adapter = mWindowListAdapter;

        if (adapter == null) {
            adapter = new WindowListAdapter(sessions);
            mDrawerListMenu.setAdapter(adapter);
            mWindowListAdapter = adapter;
        } else {
            adapter.setSessions(sessions);
        }
        sessions.addCallback(adapter);
        sessions.addTitleChangedListener(adapter);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        
        mActivity.openWindow(position);
    } 
    
    @Override
    public void onClick(View v) {
        String title = "";
        switch (v.getId()) {
            case R.id.menu_drawer_1:
                title = "About";
                //mActivity.getAboutPage();
                break;
            case R.id.menu_drawer_2:
                title = "Settings";
                //mActivity.doPreferences();
                break;
            case R.id.menu_drawer_single:
                title = "New Window";
                mActivity.doCreateNewWindow();
                break; 
            case R.id.menu_drawer_3:
                title = "Exit";
                mActivity.finish();
                break;

        }
        //Toast.makeText(this, title, Toast.LENGTH_SHORT).show();
    }
    
    
    
}
