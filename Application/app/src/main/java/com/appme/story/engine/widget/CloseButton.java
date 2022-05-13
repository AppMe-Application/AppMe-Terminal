package com.appme.story.engine.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;

public class CloseButton extends ImageView {
        public CloseButton(Context context) {
            super(context);
        }

        public CloseButton(Context context, AttributeSet attrs) {
            super(context, attrs);
        }

        public CloseButton(Context context, AttributeSet attrs, int style) {
            super(context, attrs, style);
        }

        @Override
        public void setPressed(boolean pressed) {
            if (pressed && ((View) getParent()).isPressed()) {
                return;
            }
            super.setPressed(pressed);
        }
    }
