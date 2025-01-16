package com.shakespeare.new_app;

import static android.view.KeyEvent.KEYCODE_ENTER;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Toast;

public class EditTextNoNewLine extends androidx.appcompat.widget.AppCompatEditText {

    public EditTextNoNewLine(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
        public boolean onKeyDown(int keyCode, KeyEvent event)
        {
//            Log.d("onKeyDown keypressinfo", "onKeyDown keyCode "+String.valueOf(keyCode));
//            Log.d("onKeyDown keypressinfo", "onKeyDown event "+String.valueOf(event));
            if (keyCode == KEYCODE_ENTER)
            {
                // Just ignore the [Enter] key
                Log.d("hit enter", "onKeyDown hit enter");
//                KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_BACK)
                Toast.makeText(this.getContext(), "Thinking...", Toast.LENGTH_SHORT).show();
                return super.onKeyDown(KEYCODE_ENTER, new KeyEvent(KeyEvent.KEYCODE_ENTER, KeyEvent.KEYCODE_ENTER));

            }
            // Handle all other keys in the default way
            Log.d("hit enter", "onKeyDown did not hit enter");
            return super.onKeyDown(keyCode, event);
        }
}
