package com.appme.story.engine.app.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.appme.story.R;
import com.appme.story.engine.app.terminal.GenericTermSession;
import com.appme.story.engine.app.terminal.emulatorview.TermSession;
import com.appme.story.engine.app.terminal.emulatorview.UpdateCallback;
import com.appme.story.engine.app.terminal.util.SessionList;
import com.appme.story.engine.widget.CloseButton;

public class WindowListAdapter extends BaseAdapter implements UpdateCallback {
    private SessionList mSessions;

    public WindowListAdapter(SessionList sessions) {
        setSessions(sessions);
    }

    public void setSessions(SessionList sessions) {
        mSessions = sessions;

        if (sessions != null) {
            sessions.addCallback(this);
            sessions.addTitleChangedListener(this);
        } else {
            onUpdate();
        }
    }

    public int getCount() {
        if (mSessions != null) {
            return mSessions.size();
        } else {
            return 0;
        }
    }

    public Object getItem(int position) {
        return mSessions.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    protected String getSessionTitle(int position, String defaultTitle) {
        TermSession session = mSessions.get(position);
        if (session != null && session instanceof GenericTermSession) {
            return ((GenericTermSession) session).getTitle(defaultTitle);
        } else {
            return defaultTitle;
        }
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        Activity act = findActivityFromContext(parent.getContext());
        View child = act.getLayoutInflater().inflate(R.layout.window_list_item, parent, false);
        View close = child.findViewById(R.id.window_list_close);

        TextView label = (TextView) child.findViewById(R.id.window_list_label);
        String defaultTitle = act.getString(R.string.terminal_window_title, position+1);
        label.setText(getSessionTitle(position, defaultTitle));

        final SessionList sessions = mSessions;
        final int closePosition = position;
        close.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                TermSession session = sessions.remove(closePosition);
                if (session != null) {
                    session.finish();
                    notifyDataSetChanged();
                }
            }
        });

        return child;
    }

    public void onUpdate() {
        notifyDataSetChanged();
    }

    private static Activity findActivityFromContext(Context context) {
        if (context == null) {
            return null;
        } else if (context instanceof Activity) {
            return (Activity) context;
        } else if (context instanceof ContextWrapper) {
            ContextWrapper cw = (ContextWrapper) context;
            return findActivityFromContext(cw.getBaseContext());
        }
        return null;
    }
}
