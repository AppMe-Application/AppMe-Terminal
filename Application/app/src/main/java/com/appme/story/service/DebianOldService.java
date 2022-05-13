package com.appme.story.service;

import android.app.IntentService;
import android.content.Intent;

public class DebianOldService extends IntentService {
    
    boolean shown = false;
    public DebianOldService() {
        super("GNURootOldService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        if(intent.getAction() == "com.appme.story.CHECK_STATUS") {
            Intent resultIntent = new Intent("com.appme.story.GNURootService.status");
            resultIntent.putExtra("packageName", intent.getStringExtra("packageName"));
            resultIntent.putExtra("requestCode", 5);    //Indicate that a CHECK_STATUS has PASSed
            resultIntent.putExtra("resultCode", 1);
            sendBroadcast(resultIntent);
        }

        if(!shown) {
            shown = true;
            Intent errorIntent = new Intent("com.appme.story.UPDATE_ERROR");
            errorIntent.addCategory(Intent.CATEGORY_DEFAULT);
            errorIntent.putExtra("packageName", intent.getStringExtra("packageName"));
            errorIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(errorIntent);
        }
    }
}

