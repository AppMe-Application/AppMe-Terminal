package com.appme.story.engine.app.terminal;

import android.os.ParcelFileDescriptor;
import android.text.TextUtils;

import com.appme.story.engine.app.terminal.util.TermSettings;

public class BoundSession extends GenericTermSession {
    private final String issuerTitle;

    private boolean fullyInitialized;

    public BoundSession(ParcelFileDescriptor ptmxFd, TermSettings settings, String issuerTitle) {
        super(ptmxFd, settings, true);

        this.issuerTitle = issuerTitle;

        setTermIn(new ParcelFileDescriptor.AutoCloseInputStream(ptmxFd));
        setTermOut(new ParcelFileDescriptor.AutoCloseOutputStream(ptmxFd));
    }

    @Override
    public String getTitle() {
        final String extraTitle = super.getTitle();

        return TextUtils.isEmpty(extraTitle)
               ? issuerTitle
               : issuerTitle + " — " + extraTitle;
    }

    @Override
    public void initializeEmulator(int columns, int rows) {
        super.initializeEmulator(columns, rows);

        fullyInitialized = true;
    }

    @Override
    public boolean isFailFast() {
        return !fullyInitialized;
    }
}
