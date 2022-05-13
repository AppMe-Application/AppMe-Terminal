package com.appme.story.engine.app.terminal.emulatorview.compat;

public interface ClipboardManagerCompat {
	CharSequence getText();

	boolean hasText();

    void setText(CharSequence text);
}
