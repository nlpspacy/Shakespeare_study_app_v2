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

    private static MediaPlayer currentPlayer = null;
    private static final Object playbackLock = new Object();
    private static boolean isPlaying = false;
    private static Call currentCall = null;
    private static String currentPlayKey = "";

    private static final OkHttpClient client = new OkHttpClient();
    private static final String openaiApiKey = BuildConfig.OPENAI_API_KEY;
//    private static final String OPENAI_API_KEY = "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"; // 🔒 Replace with your key

    // ✅ Set the active scene key explicitly before playback begins
    public static void prepareScenePlayback(String key) {
        synchronized (playbackLock) {
            currentPlayKey = key;
        }
    }

    public static void synthesizeAndPlay(Context context, String text, String voice, String sceneKey) {
        synchronized (playbackLock) {
            // Cancel if scene changed mid-play
            if (isPlaying && !sceneKey.equals(currentPlayKey)) {
                Log.d("VoiceSynth", "Stopping due to scene change");
                stopPlayback();
            }
        }

        new Thread(() -> {
            synchronized (playbackLock) {
                try {
                    while (isPlaying) {
                        playbackLock.wait();
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

                    RequestBody body = RequestBody.create(MediaType.parse("application/json"), json.toString());

                    Request request = new Request.Builder()
                            .url("https://api.openai.com/v1/audio/speech")
                            .addHeader("Authorization", "Bearer " + openaiApiKey)
                            .post(body)
                            .build();

                    Call call = client.newCall(request);
                    currentCall = call;

                    call.enqueue(new Callback() {
                        @Override
                        public void onFailure(@NonNull Call call, @NonNull IOException e) {
                            Log.e("VoiceSynth", "Request failed", e);
                            releasePlayback();
                        }

                        @Override
                        public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                            synchronized (playbackLock) {
                                if (!sceneKey.equals(currentPlayKey)) {
//                                    Log.d("VoiceSynth", "Discarding old scene response");
                                    releasePlayback();
                                    return;
                                }
                            }

                            if (response.isSuccessful() && response.body() != null) {
                                byte[] audioData = response.body().bytes();
                                playAudio(context, audioData, sceneKey);
                            } else {
//                                Log.e("VoiceSynth", "Response error: " + response.code());
                                releasePlayback();
                            }
                        }
                    });

                } catch (Exception e) {
                    Log.e("VoiceSynth", "Exception", e);
                    releasePlayback();
                }
            }
        }).start();
    }

    private static void playAudio(Context context, byte[] audioData, String sceneKey) {
        try {
            File tempFile = File.createTempFile("openai_audio", ".mp3", context.getCacheDir());
            try (FileOutputStream fos = new FileOutputStream(tempFile)) {
                fos.write(audioData);
            }

            MediaPlayer mediaPlayer = new MediaPlayer();
            currentPlayer = mediaPlayer;
            mediaPlayer.setDataSource(tempFile.getAbsolutePath());

            mediaPlayer.setOnPreparedListener(mp -> {
                synchronized (playbackLock) {
                    String currentScene = GlobalClass.selectedPlayCode + "_" +
                            GlobalClass.selectedActNumber + "_" +
                            GlobalClass.selectedSceneNumber;

                    if (!sceneKey.equals(currentScene)) {
                        Log.d("VoiceSynth", "Scene mismatch at playAudio");
                        mp.release();
                        isPlaying = false;
                        return;
                    }

                    mp.start();
                }
            });

            mediaPlayer.setOnCompletionListener(mp -> {
                mp.release();
                tempFile.delete();
                releasePlayback();
            });

            mediaPlayer.prepareAsync();
        } catch (IOException e) {
            Log.e("VoiceSynth", "Audio playback failed", e);
            releasePlayback();
        }
    }

    public static void stopPlayback() {
        synchronized (playbackLock) {
            isPlaying = false;
            currentPlayKey = "";

            if (currentCall != null) {
                currentCall.cancel();
                currentCall = null;
            }

            if (currentPlayer != null) {
                try {
                    currentPlayer.stop();
                } catch (IllegalStateException e) {
                    Log.w("VoiceSynth", "Stop failed");
                }

                try {
                    currentPlayer.release();
                } catch (Exception e) {
                    Log.w("VoiceSynth", "Release failed");
                }

                currentPlayer = null;
            }

            playbackLock.notifyAll();
        }
    }

    private static void releasePlayback() {
        synchronized (playbackLock) {
            isPlaying = false;
            playbackLock.notifyAll();
        }
    }
}
