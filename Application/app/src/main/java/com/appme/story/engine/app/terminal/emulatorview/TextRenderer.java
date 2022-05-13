package com.appme.story.engine.app.terminal.emulatorview;

import android.graphics.Canvas;
import android.text.TextPaint;

/**
 * Text renderer interface
 */

interface TextRenderer {
    int MODE_OFF = 0;
    int MODE_ON = 1;
    int MODE_LOCKED = 2;
    int MODE_MASK = 3;

    int MODE_SHIFT_SHIFT = 0;
    int MODE_ALT_SHIFT = 2;
    int MODE_CTRL_SHIFT = 4;
    int MODE_FN_SHIFT = 6;

    void setReverseVideo(boolean reverseVideo);
    float getCharacterWidth();
    int getCharacterHeight();
    /** @return pixels above top row of text to avoid looking cramped. */
    int getTopMargin();
    /**
     * Draw a run of text
     * @param canvas The canvas to draw into.
     * @param x Canvas coordinate of the left edge of the whole line.
     * @param y Canvas coordinate of the bottom edge of the whole line.
     * @param lineOffset The screen character offset of this text run (0..length of line)
     * @param runWidth
     * @param text
     * @param index
     * @param count
     * @param selectionStyle True to draw the text using the "selected" style (for clipboard copy)
     * @param textStyle
     * @param cursorOffset The screen character offset of the cursor (or -1 if not on this line.)
     * @param cursorIndex The index of the cursor in text chars.
     * @param cursorIncr The width of the cursor in text chars. (1 or 2)
     * @param cursorWidth The width of the cursor in screen columns (1 or 2)
     * @param cursorMode The cursor mode (used to show state of shift/control/alt/fn locks.
     */
    void drawTextRun(Canvas canvas, float x, float y,
                     int lineOffset, int runWidth, char[] text,
                     int index, int count, boolean selectionStyle, int textStyle,
                     int cursorOffset, int cursorIndex, int cursorIncr, int cursorWidth, int cursorMode);
    float getMeasureText(String str);
    void setImePaint(TextPaint paint);
}
