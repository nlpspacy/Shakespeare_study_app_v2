package com.shakespeare.new_app;

import android.util.Log;

import com.shakespeare.new_app.GlobalClass;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class ChatGPTApiHelper {

    public interface ChatGPTResponseCallback {
        void onSuccess(String response, int responseCode);
        void onError(Exception e, int responseCode);
    }

    public static void callChatGPTApi(String apiKey, String userPrompt, String userPromptPlay, ChatGPTResponseCallback callback) {
        new Thread(() -> {
            HttpURLConnection conn = null;
            int responseCode = -1; // Default response code if the connection fails

            try {
                // Create the connection
                URL url = new URL("https://api.openai.com/v1/chat/completions");
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Authorization", "Bearer " + apiKey);
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoInput(true);
                conn.setDoOutput(true);

                // JSON request payload
                String jsonInputString = "{"
                        + "\"model\": \"gpt-3.5-turbo\","
                        + "\"messages\": ["
//                        + "    {\"role\": \"system\", \"content\": \"You are a helpful assistant.\"},"
//                        + "    {\"role\": \"user\", \"content\": \"Hello! How are you?\"}"
//                        + "    {\"role\": \"system\", \"content\": \"We are using your response in an Android app written in Java.\"},"
//                        + "    {\"role\": \"user\", \"content\": \"" + userPrompt + "\"}"
//                        + "    {\"role\": \"system\", \"content\": \"Assume this query is from a 14 year old boy in Sydney Australia who is studying the Shakespeare play A Midsummer Nights Dream, and wants to learn more about this play from Chat GPT for his high school studies.\"},"
//                        + "    {\"role\": \"system\", \"content\": \"Please reply in Russian, for a 70 year old woman in Russia who is studying the Shakespeare play A Midsummer Nights Dream, and wants to learn more about this play from Chat GPT for an amateur theatre performance of this play which she is preparing for, in which she takes on the role of a character in the play.\"},"
                        + "    {\"role\": \"system\", \"content\": \""+ GlobalClass.system_prompt +"\"},"
                        + "    {\"role\": \"user\", \"content\": \"" + userPrompt + userPromptPlay + "\"}"
                        + "],"
//                        + "\"max_tokens\": 50,"
                        + "\"temperature\": 0.7"
                        + "}";

                // Write the request payload
                try (OutputStream os = conn.getOutputStream()) {
                    byte[] input = jsonInputString.getBytes("utf-8");
                    os.write(input, 0, input.length);
                }

                // Get the response code
                responseCode = conn.getResponseCode();

                // Read the response
                BufferedReader br = new BufferedReader(new InputStreamReader(
                        responseCode >= 200 && responseCode < 300
                                ? conn.getInputStream()
                                : conn.getErrorStream(), "utf-8"));

                StringBuilder response = new StringBuilder();
                String responseLine;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }

                Log.d("update", "** calling API **");
                // Pass the response to the callback
                callback.onSuccess(response.toString(), responseCode);

            } catch (Exception e) {
                // Pass the exception and response code to the callback
                callback.onError(e, responseCode);
            } finally {
                if (conn != null) {
                    conn.disconnect();
                }
            }
        }).start();
    }
}
