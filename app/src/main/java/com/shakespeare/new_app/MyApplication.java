package com.shakespeare.new_app;

import android.app.Application;
import android.speech.tts.TextToSpeech;
import android.speech.tts.Voice;
import android.util.Log;

import java.util.Locale;
import java.util.Set;

public class MyApplication extends Application {

    private static final String TAG = "MyApplication";

    // 🔹 Global singleton instance
    private static MyApplication instance;

    // 🔹 Existing TTS field
    public static TextToSpeech textToSpeech;

    @Override
    public void onCreate() {
        super.onCreate();

        // 🔹 Initialise singleton
        instance = this;

        // 🔹 Existing TTS initialisation
        textToSpeech = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int i) {
                if (i == TextToSpeech.SUCCESS) {
                    Log.d(TAG, "onInit: init success");
                } else {
                    Log.d(TAG, "onInit: init failed!");
                }
            }
        });
    }

    // 🔹 Global accessor for Application context
    public static MyApplication getInstance() {
        return instance;
    }

    // 🔹 Existing helper methods (unchanged)
    public static Set<Voice> getVoices() {
        return textToSpeech.getVoices();
    }

    public static void setLanguage(Locale lang) {
        if (textToSpeech != null &&
                textToSpeech.isLanguageAvailable(lang) != TextToSpeech.LANG_NOT_SUPPORTED) {
            textToSpeech.setLanguage(lang);
        }
    }
}
