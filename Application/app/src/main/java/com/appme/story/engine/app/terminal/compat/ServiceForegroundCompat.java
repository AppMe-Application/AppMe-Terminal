package com.appme.story.engine.app.terminal.compat;

import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import android.app.Service;
import android.util.Log;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;

/* Provide startForeground() and stopForeground() compatibility, using the
   current interfaces where available and the deprecated setForeground()
   interface where necessary
   The idea for the implementation comes from an example in the documentation of
   android.app.Service */
public class ServiceForegroundCompat {
    private static Class<?>[] mSetForegroundSig = new Class[] {
        boolean.class };
    private static Class<?>[] mStartForegroundSig = new Class[] {
        int.class, Notification.class };
    private static Class<?>[] mStopForegroundSig = new Class[] {
        boolean.class };

    private Service service;
    private NotificationManager mNM;
    private Method mSetForeground;
    private Method mStartForeground;
    private Method mStopForeground;
    private int notifyId;

    private void invokeMethod(Object receiver, Method method, Object... args) {
        try {
            method.invoke(receiver, args);
        } catch (IllegalAccessException e) {
            // Shouldn't happen, but we have to catch this
            Log.w("ServiceCompat", "Unable to invoke method", e);
        } catch (InvocationTargetException e) {
            /* The methods we call don't throw exceptions -- in general,
               we should throw e.getCause() */
            Log.w("ServiceCompat", "Method threw exception", e.getCause());
        }
    }

    public void startForeground(int id, Notification notification) {
        if (mStartForeground != null) {
            invokeMethod(service, mStartForeground, id, notification);
            return;
        }

        invokeMethod(service, mSetForeground, Boolean.TRUE);
        mNM.notify(id, notification);
        notifyId = id;
    }

    public void stopForeground(boolean removeNotify) {
        if (mStopForeground != null) {
            invokeMethod(service, mStopForeground, removeNotify);
            return;
        }

        if (removeNotify) {
            mNM.cancel(notifyId);
        }
        invokeMethod(service, mSetForeground, Boolean.FALSE);
    }

    public ServiceForegroundCompat(Service service) {
        this.service = service;
        mNM = (NotificationManager)service.getSystemService(Context.NOTIFICATION_SERVICE);

        Class<?> clazz = service.getClass();

        try {
            mStartForeground = clazz.getMethod("startForeground", mStartForegroundSig);
            mStopForeground = clazz.getMethod("stopForeground", mStopForegroundSig);
        } catch (NoSuchMethodException e) {
            mStartForeground = mStopForeground = null;
        }

        try {
            mSetForeground = clazz.getMethod("setForeground", mSetForegroundSig);
        } catch (NoSuchMethodException e) {
            mSetForeground = null;
        }

        if (mStartForeground == null && mSetForeground == null) {
            throw new IllegalStateException("Neither startForeground() or setForeground() present!");
        }
    }
}
