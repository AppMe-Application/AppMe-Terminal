package com.appme.story.receiver;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.appme.story.receiver.base.AntBroadcastStaticReceiver;
import com.appme.story.engine.app.utils.NetworkUtil;

public class ApplicationNetworkReceiver extends AntBroadcastStaticReceiver {

    private Context mContext;
    private OnConnectionStatus mOnConnectionStatus;
    private static boolean flag = false;
    public ApplicationNetworkReceiver(Context context){
        this.mContext = context;
    }

    @Override
    public void onReceiveHandler(Context ctx, Intent intent) {
   
        int status = NetworkUtil.getConnectivityStatusString(ctx);
        if (status == NetworkUtil.NETWORK_STATUS_NOT_CONNECTED) {
            flag = true;
            if(mOnConnectionStatus != null){
                mOnConnectionStatus.onDiskConnect();
            }
        }else{
            flag = false;
            if(mOnConnectionStatus != null){
                mOnConnectionStatus.onConnect();
            }
        }
    }

    public void setOnConnectionStatus(OnConnectionStatus mOnConnectionStatus){
        this.mOnConnectionStatus = mOnConnectionStatus;
    }

    public interface OnConnectionStatus
    {
        void onConnect();
        void onDiskConnect();
    }
}

