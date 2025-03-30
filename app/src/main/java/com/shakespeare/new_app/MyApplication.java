package com.shakespeare.new_app;

import android.app.Application;
import android.speech.tts.TextToSpeech;
import android.speech.tts.Voice;
import android.util.Log;

import java.util.Locale;
import java.util.Set;

public class MyApplication extends Application {
    private static final String TAG = "MyApplication";
    public static TextToSpeech textToSpeech;

    @Override
    public void onCreate() {
        super.onCreate();
        textToSpeech = new TextToSpeech(this
                ,
                new TextToSpeech.OnInitListener() {
                    @Override
                    public void onInit(int i) {
                        Set<Voice> voices = getVoices();
                        for (Voice voice : voices) {

                            Log.d(TAG, "setLanguage: "+voice.getName());
                            for (String feature : voice.getFeatures()) {
                                Log.d(TAG, "setLanguage:feature "+feature);
                            }

                        }
                    }
                },"com.google.android.tts");
    }

    public  static Set<Voice> getVoices(){
      return  textToSpeech.getVoices();
    }

    public static void setLanguage(Locale lang){
      if (textToSpeech!=null && textToSpeech.isLanguageAvailable(lang)!=TextToSpeech.LANG_NOT_SUPPORTED){
          textToSpeech.setLanguage(lang);
      }
    }

}
