package com.shakespeare.new_app;

import android.util.Log;

import com.shakespeare.new_app.GlobalClass;

import org.json.JSONObject;
import org.json.JSONArray;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ChatGPTApiHelper {

    public interface ChatGPTResponseCallback {
        void onSuccess(String response, int responseCode);
        void onError(Exception e, int responseCode);
    }

    // Added to use Railway for the OPENAI_API_KEY.
    // 4 Oct 2025
    private static final String BASE_URL = "https://android-sqlitecloud-api-production.up.railway.app";

    // Replaced to use Railway for the OPENAI_API_KEY.
    // 4 Oct 2025
//    public static void callChatGPTApi(String apiKey, String userPrompt, String userPromptPlay, ChatGPTResponseCallback callback) {
    public static void callChatGPTApi(String userPrompt, String userPromptPlay, ChatGPTResponseCallback callback) {

        // Added to use Railway for the OPENAI_API_KEY.
        // 4 Oct 2025

        OkHttpClient client = new OkHttpClient();

        // Build the JSON body your FastAPI /chat endpoint expects
        // If you only need a single prompt field, just send prompt.
        JSONObject json = new JSONObject();

        try {
//            json.put("prompt", userPrompt);
//            json.put("play_context", userPromptPlay); // optional: remove if your backend doesn’t use it
            json.put("system", GlobalClass.system_prompt);
            json.put("prompt", userPrompt + userPromptPlay);
        } catch (Exception e) {
            callback.onError(e, 0);
            return;
        }

        RequestBody body = RequestBody.create(
                json.toString(),
                MediaType.parse("application/json; charset=utf-8")
        );

        Request req = new Request.Builder()
                .url(BASE_URL + "/chat")
                .post(body)
                .build();

        client.newCall(req).enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException e) {
                callback.onError(e, 0);
            }
            @Override public void onResponse(Call call, Response response) throws IOException {
                String resp = response.body() != null ? response.body().string() : "";
                if (!response.isSuccessful()) {
                    callback.onError(new IOException(resp), response.code());
                    return;
                }
                callback.onSuccess(resp, response.code());
            }
        });
    }

// =====================================================================
        // Replacing this block to use Railway for the OPENAI_API_KEY.
        // 4 Oct 2025
// =====================================================================
//        new Thread(() -> {
//            HttpURLConnection conn = null;
//            int responseCode = -1; // Default response code if the connection fails
//
//            try {
//                // Create the connection
//                URL url = new URL("https://api.openai.com/v1/chat/completions");
//                conn = (HttpURLConnection) url.openConnection();
//                conn.setRequestMethod("POST");
//                conn.setRequestProperty("Authorization", "Bearer " + apiKey);
//                conn.setRequestProperty("Content-Type", "application/json");
//                conn.setDoInput(true);
//                conn.setDoOutput(true);
//
//                // JSON request payload
//                String jsonInputString = "{"
//                        + "\"model\": \"gpt-3.5-turbo\","
//                        + "\"messages\": ["
//                        + "    {\"role\": \"system\", \"content\": \""+ GlobalClass.system_prompt +"\"},"
//                        + "    {\"role\": \"user\", \"content\": \"" + userPrompt + userPromptPlay + "\"}"
//                        + "],"
//                        + "\"temperature\": 0.7"
//                        + "}";
//
//                // Write the request payload
//                try (OutputStream os = conn.getOutputStream()) {
//                    byte[] input = jsonInputString.getBytes("utf-8");
//                    os.write(input, 0, input.length);
//                }
//
//                // Get the response code
//                responseCode = conn.getResponseCode();
//
//                // Read the response
//                BufferedReader br = new BufferedReader(new InputStreamReader(
//                        responseCode >= 200 && responseCode < 300
//                                ? conn.getInputStream()
//                                : conn.getErrorStream(), "utf-8"));
//
//                StringBuilder response = new StringBuilder();
//                String responseLine;
//                while ((responseLine = br.readLine()) != null) {
//                    response.append(responseLine.trim());
//                }
//
//                Log.d("update", "** calling API **");
//                // Pass the response to the callback
//                callback.onSuccess(response.toString(), responseCode);
//
//            } catch (Exception e) {
//                // Pass the exception and response code to the callback
//                callback.onError(e, responseCode);
//            } finally {
//                if (conn != null) {
//                    conn.disconnect();
//                }
//            }
//        }).start();
// =====================================================================

}
