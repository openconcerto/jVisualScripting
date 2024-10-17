package com.jvisualscripting.function.network;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import com.jvisualscripting.DataPin;
import com.jvisualscripting.FlowNode;
import com.jvisualscripting.Pin.PinMode;
import com.jvisualscripting.variable.IntegerPin;
import com.jvisualscripting.variable.StringPin;

public class HttpGet extends FlowNode {
    private int responseCode;
    private String contentStr;

    public HttpGet() {
        super("HttpGet");
        this.inputs.add(new StringPin(this, "URL", PinMode.INPUT));

        this.outputs.add(new StringPin(this, "Content", PinMode.OUTPUT));
        this.outputs.add(new IntegerPin(this, "Status Code", PinMode.OUTPUT));

    }

    @Override
    public boolean run() {
        this.responseCode = 0;
        this.contentStr = "";
        String urlString = (String) getInputValue(1);
        System.out.println("HttpGet.run()" + urlString);
        try {
            // Create a URL object
            URL url = new URL(urlString);

            // Open a connection to the URL
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/125.0.0.0 Safari/537.36");

            // Set the request method to GET
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(30 * 1000);
            connection.setReadTimeout(60 * 1000);
            HttpURLConnection.setFollowRedirects(true);

            this.responseCode = connection.getResponseCode();
            if (this.responseCode == HttpURLConnection.HTTP_OK) {

                // Get the input stream from the connection
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));

                // Read the response line by line
                String inputLine;
                final StringBuilder content = new StringBuilder();
                while ((inputLine = in.readLine()) != null) {
                    content.append(inputLine);
                    content.append(System.lineSeparator());
                }
                this.contentStr = content.toString();
                // Close the input stream
                in.close();
            }
            // Disconnect the connection
            connection.disconnect();
            return true;
        } catch (Exception e) {
            this.contentStr = e.getMessage();
        }
        return false;
    }

    @Override
    public Object getOutputValue(DataPin pin) {
        if (pin == getContentOuputPin()) {
            return this.contentStr;
        }
        if (pin == getResponceCodeOuputPin()) {
            return this.responseCode;
        }

        throw new IllegalArgumentException(pin + " is not an declared output pin of this node");
    }

    public DataPin getContentOuputPin() {
        return (DataPin) getOutputs().get(1);
    }

    public DataPin getResponceCodeOuputPin() {
        return (DataPin) getOutputs().get(2);
    }

}
