package com.appme.story.engine.app.terminal;

import android.os.Build;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import java.io.File;
import java.io.FileDescriptor;
import java.io.IOException;
import java.lang.IllegalStateException;
import java.lang.Process;
import java.lang.reflect.Field;

/**
 * Utility methods for managing a pty file descriptor.
 */
public class Exec
{
    // Warning: bump the library revision, when an incompatible change happens
    static {
        System.loadLibrary("appme-androidterm5");
    }

    static native void setPtyWindowSizeInternal(int fd, int row, int col, int xpixel, int ypixel) throws IOException;

    static native void setPtyUTF8ModeInternal(int fd, boolean utf8Mode) throws IOException;
}

