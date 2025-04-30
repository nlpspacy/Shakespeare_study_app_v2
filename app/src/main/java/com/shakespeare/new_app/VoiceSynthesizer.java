package com.shakespeare.new_app;

import android.content.Context;
import android.media.MediaPlayer;
import android.util.Log;

import androidx.annotation.NonNull;

import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class VoiceSynthesizer {

    private static final OkHttpClient client = new OkHttpClient();
    private static final String OPENAI_API_KEY = "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"; // 🔒 Keep secret in production

    // 🔒 Lock object to ensure only one speech at a time
    private static final Object playbackLock = new Object();
    private static boolean isPlaying = false;

    public static void synthesizeAndPlay(Context context, String text, String voice) {
        new Thread(() -> {
            synchronized (playbackLock) {
                try {
                    while (isPlaying) {
                        playbackLock.wait();  // ⏳ Wait until the previous audio finishes
                    }
                    isPlaying = true;
                } catch (InterruptedException e) {
                    Log.e("VoiceSynth", "Interrupted", e);
                    return;
                }

                try {
                    JSONObject json = new JSONObject();
                    json.put("model", "gpt-4o-mini-tts");
                    json.put("input", text);
                    json.put("voice", voice);

                    RequestBody body = RequestBody.create(
                            MediaType.parse("application/json"),
                            json.toString()
                    );

                    Request request = new Request.Builder()
                            .url("https://api.openai.com/v1/audio/speech")
                            .addHeader("Authorization", "Bearer " + OPENAI_API_KEY)
                            .post(body)
                            .build();

                    client.newCall(request).enqueue(new Callback() {
                        @Override
                        public void onFailure(@NonNull Call call, @NonNull IOException e) {
                            Log.e("VoiceSynth", "Request failed", e);
                            releasePlayback();
                        }

                        @Override
                        public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                            if (response.isSuccessful() && response.body() != null) {
                                byte[] audioData = response.body().bytes();
                                playAudio(context, audioData);
                            } else {
                                Log.e("VoiceSynth", "Response error: " + response.code() + " " + response.message());
                                releasePlayback();
                            }
                        }
                    });
                } catch (Exception e) {
                    Log.e("VoiceSynth", "Exception building request", e);
                    releasePlayback();
                }
            }
        }).start();
    }

    private static void playAudio(Context context, byte[] audioData) {
        try {
            File tempFile = File.createTempFile("openai_audio", ".mp3", context.getCacheDir());
            try (FileOutputStream fos = new FileOutputStream(tempFile)) {
                fos.write(audioData);
            }

            MediaPlayer mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(tempFile.getAbsolutePath());
            mediaPlayer.setOnPreparedListener(MediaPlayer::start);
            mediaPlayer.setOnCompletionListener(mp -> {
                mp.release();
                tempFile.delete();
                releasePlayback();
            });
            mediaPlayer.prepareAsync();
        } catch (IOException e) {
            Log.e("VoiceSynth", "Error playing audio", e);
            releasePlayback();
        }
    }

    private static void releasePlayback() {
        synchronized (playbackLock) {
            isPlaying = false;
            playbackLock.notifyAll();
        }
    }
}
