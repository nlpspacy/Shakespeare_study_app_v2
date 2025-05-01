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
    private static int currentGeneration = 0; // 🔁 Tracks latest playback

    private static final OkHttpClient client = new OkHttpClient();
    private static final String OPENAI_API_KEY = "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"; // 🔒 Keep secret in production

    // 🔒 Lock object to ensure only one speech at a time
    private static final Object playbackLock = new Object();
    private static boolean isPlaying = false;
    private static Call currentCall = null; // ✅ Track the HTTP request

    public static void synthesizeAndPlay(Context context, String text, String voice, int generation) {
//    public static void synthesizeAndPlay(Context context, String text, String voice) {
//        final int generation = ++currentGeneration;  // 🔁 Unique ID per request

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

                    RequestBody body = RequestBody.create(
                            MediaType.parse("application/json"),
                            json.toString()
                    );

                    Request request = new Request.Builder()
                            .url("https://api.openai.com/v1/audio/speech")
                            .addHeader("Authorization", "Bearer " + OPENAI_API_KEY)
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
                            Log.d("generation tracking", "currentGeneration: " + String.valueOf(currentGeneration) + ", generation: " + String.valueOf(generation));
                            if (generation != currentGeneration) {
                                Log.d("VoiceSynth", "Discarding old generation playback");
                                releasePlayback();
                                return;
                            }

                            if (response.isSuccessful() && response.body() != null) {
                                byte[] audioData = response.body().bytes();
                                if (generation == currentGeneration) {
                                    playAudio(context, audioData);
                                } else {
                                    Log.d("VoiceSynth", "Playback canceled before playing");
                                    releasePlayback();
                                }
                            } else {
                                Log.e("VoiceSynth", "Response error: " + response.code() + " " + response.message());
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

    public static int nextGeneration() {
        Log.d("generation tracking", "currentGeneration: " + String.valueOf(currentGeneration));
        return ++currentGeneration;
    }

    public static int getCurrentGeneration() {
        Log.d("generation tracking", "currentGeneration: " + String.valueOf(currentGeneration));
        return currentGeneration;
    }

    public static void stopPlayback() {
        synchronized (playbackLock) {
            isPlaying = false;

            // ✅ Cancel any in-flight HTTP request
            if (currentCall != null) {
                currentCall.cancel();
                currentCall = null;
            }

            if (currentPlayer != null) {
                try {
                    currentPlayer.stop();
                } catch (IllegalStateException e) {
                    Log.w("VoiceSynth", "Tried to stop player not in started state");
                }

                try {
                    currentPlayer.release();
                } catch (Exception e) {
                    Log.w("VoiceSynth", "Error releasing player", e);
                }

                currentPlayer = null;
            }

            playbackLock.notifyAll();
        }
    }


    private static void playAudio(Context context, byte[] audioData) {
        try {
            File tempFile = File.createTempFile("openai_audio", ".mp3", context.getCacheDir());
            Log.d("file location", "mp3 file location " + tempFile.toString());
            try (FileOutputStream fos = new FileOutputStream(tempFile)) {
                fos.write(audioData);
            }

            MediaPlayer mediaPlayer = new MediaPlayer();
            currentPlayer = mediaPlayer;  // Track this instance globally

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

    public static void releasePlayback() {
        synchronized (playbackLock) {
            isPlaying = false;
            playbackLock.notifyAll();
        }
    }


}
