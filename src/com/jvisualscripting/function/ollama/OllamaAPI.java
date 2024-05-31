package com.jvisualscripting.function.ollama;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import org.json.JSON;
import org.json.JSONObject;

public class OllamaAPI {

    private final String host;
    private final int port;
    private boolean useHttps;

    public OllamaAPI() {
        this("localhost", 11434, false);
    }

    public OllamaAPI(String host, int port, boolean useHttps) {
        this.host = host;
        this.port = port;
        this.useHttps = useHttps;
    }

    public String query(String modelName, String prompt) throws IOException {

        // Request URL
        String apiUrl;
        if (this.useHttps) {
            apiUrl = "https://" + this.host + ":" + this.port + "/api/generate";
        } else {
            apiUrl = "http://" + this.host + ":" + this.port + "/api/generate";
        }

        URL url = new URL(apiUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setDoOutput(true);

        // JSON request
        final JSONObject request = new JSONObject();
        request.put("model", modelName);
        request.put("prompt", prompt);
        request.put("stream", false);
        String requestBody = request.toString();

        // Send request
        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = requestBody.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        // Handle response
        int responseCode = connection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                StringBuilder response = new StringBuilder();
                String responseLine;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine);
                }
                JSONObject r = (JSONObject) JSON.parse(response.toString());
                return r.getString("response");

            }
        } else {
            throw new IOException("HTTP error code : " + responseCode);
        }

    }

}
