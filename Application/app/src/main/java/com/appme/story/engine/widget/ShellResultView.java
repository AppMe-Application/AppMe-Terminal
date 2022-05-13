package com.appme.story.engine.widget;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.appme.story.R;
import com.appme.story.engine.app.terminal.command.ExecScript;
import com.appme.story.engine.app.terminal.command.ShellResult;
import com.appme.story.settings.ShellResultPreference;

public class ShellResultView extends RelativeLayout {
    
    private Context mContext;
    private View mFrame;
    public static ScrollView scrollResult;
    public static TextView shellResult;
    
    public ShellResultView(Context context) {
        super(context);
        init(context, null);
    }

    public ShellResultView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public ShellResultView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        this.mContext = context;
    }
    
    
    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        setKeepScreenOn(true);

    
        // Instantiate and add TextureView for rendering
        final LayoutInflater li = LayoutInflater.from(getContext());
        mFrame = li.inflate(R.layout.layout_shell_result, this, false);
        addView(mFrame);
        
        scrollResult =(ScrollView) mFrame.findViewById(R.id.scrollView);     
        shellResult = (TextView) mFrame.findViewById(R.id.shell_result);      
        shellResult.setMovementMethod(LinkMovementMethod.getInstance());
        
     }
     
    /**
     * Show message in TextView, used from Logger
     *
     * @param log message
     */
    public static void showLog(final String log) {
        // show log in TextView
        shellResult.post(new Runnable() {
                @Override
                public void run() {
                    shellResult.setText(log);
                    // scroll TextView to bottom
                    scrollResult.post(new Runnable() {
                            @Override
                            public void run() {
                                scrollResult.fullScroll(View.FOCUS_DOWN);
                                scrollResult.clearFocus();
                            }
                        });
                }
            });
    }
    
    public void start() {
        TextView outputView = (TextView)mFrame.findViewById(R.id.shell_result);
        // restore font size
        outputView.setTextSize(TypedValue.COMPLEX_UNIT_SP, ShellResultPreference.getFontSize(getContext()));
        // restore logs
        String log = ShellResult.get();
        if (log.length() == 0) {
            // show info if empty
            new ExecScript(getContext(), "info").start();
        } else {
            showLog(log);
        }
    }
}
