package com.appme.story.engine.app.terminal.compat;

import java.io.File;

/**
 * Compatibility class for java.io.File
 */
public class FileCompat {
    private static class Api9OrLater {
        public static boolean canExecute(File file) {
            return file.canExecute();
        }
    }

    private static class Api8OrEarlier {
        static {
            System.loadLibrary("appme-androidterm5");
        }

        public static boolean canExecute(File file) {
            return testExecute(file.getAbsolutePath());
        }

        private static native boolean testExecute(String pathname);
    }

    public static boolean canExecute(File file) {
        if (AndroidCompat.SDK < 9) {
            return Api8OrEarlier.canExecute(file);
        } else {
            return Api9OrLater.canExecute(file);
        }
    }
}
