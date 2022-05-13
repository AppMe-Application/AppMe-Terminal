package com.appme.story.application;

import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import com.appme.story.AppConfig;
import com.appme.story.service.TerminalDebianService;

/*
 * New procedure for launching a command in ATE.
 * Build the path and arguments into a Uri and set that into Intent.data.
 * intent.data(new Uri.Builder().setScheme("file").setPath(path).setFragment(arguments))
 *
 * The old procedure of using Intent.Extra is still available but is discouraged.
 */
public final class DebianRunScript extends DebianRemoteInterface {
    
    @Override
    protected void handleIntent() {
        TerminalDebianService service = getTermService();
        if (service == null) {
            finish();
            return;
        }

        Intent myIntent = getIntent();
        String action = myIntent.getAction();
        if (action.equals(AppConfig.DEBIAN_ACTION_RUN_SCRIPT)) {
            /* Someone with the appropriate permissions has asked us to
               run a script */
            String handle = myIntent.getStringExtra(AppConfig.DEBIAN_EXTRA_WINDOW_HANDLE);
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
            if(command==null) command=myIntent.getStringExtra(AppConfig.DEBIAN_EXTRA_INITIAL_COMMAND);
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

            finish();
        } else {
            super.handleIntent();
        }
    }
}
