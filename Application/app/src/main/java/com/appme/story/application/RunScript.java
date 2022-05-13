package com.appme.story.application;

import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import com.appme.story.service.TerminalService;
/*
 * New procedure for launching a command in ATE.
 * Build the path and arguments into a Uri and set that into Intent.data.
 * intent.data(new Uri.Builder().setScheme("file").setPath(path).setFragment(arguments))
 *
 * The old procedure of using Intent.Extra is still available but is discouraged.
 */
public final class RunScript extends RemoteInterface {
    private static final String ACTION_RUN_SCRIPT = "com.appme.story.RUN_SCRIPT";

    private static final String EXTRA_WINDOW_HANDLE = "com.appme.story.window_handle";
    private static final String EXTRA_INITIAL_COMMAND = "com.appme.story.iInitialCommand";

    @Override
    protected void handleIntent() {
        TerminalService service = getTermService();
        if (service == null) {
            finish();
            return;
        }

        Intent myIntent = getIntent();
        String action = myIntent.getAction();
        if (action.equals(ACTION_RUN_SCRIPT)) {
            /* Someone with the appropriate permissions has asked us to
               run a script */
            String handle = myIntent.getStringExtra(EXTRA_WINDOW_HANDLE);
            String command=null;
            /*
             * First look in Intent.data for the path; if not there, revert to
             * the EXTRA_INITIAL_COMMAND location.
             */
            Uri uri=myIntent.getData();
            if(uri!=null) // scheme[path][arguments]
            {
              String s=uri.getScheme();
              if(s!=null && s.toLowerCase().equals("file"))
              {
                command=uri.getPath();
                // Allow for the command to be contained within the arguments string.
                if(command==null) command="";
                if(!command.equals("")) command=quoteForBash(command);
                // Append any arguments.
                if(null!=(s=uri.getFragment())) command+=" "+s;
              }
            }
            // If Intent.data not used then fall back to old method.
            if(command==null) command=myIntent.getStringExtra(EXTRA_INITIAL_COMMAND);
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

            finish();
        } else {
            super.handleIntent();
        }
    }
}
