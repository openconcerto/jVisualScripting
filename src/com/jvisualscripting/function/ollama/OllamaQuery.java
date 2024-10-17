package com.jvisualscripting.function.ollama;

import java.io.IOException;

import com.jvisualscripting.DataPin;
import com.jvisualscripting.FlowNode;
import com.jvisualscripting.Pin.PinMode;
import com.jvisualscripting.variable.BooleanPin;
import com.jvisualscripting.variable.IntegerPin;
import com.jvisualscripting.variable.StringPin;

public class OllamaQuery extends FlowNode {

    private String response;
    private boolean error;

    public OllamaQuery() {
        super("Ollama Query");
        this.inputs.add(new StringPin(this, "Host", PinMode.INPUT));
        this.inputs.add(new IntegerPin(this, "Port", PinMode.INPUT));
        this.inputs.add(new BooleanPin(this, "Use HTTPs", PinMode.INPUT));
        this.inputs.add(new StringPin(this, "Model", PinMode.INPUT));
        this.inputs.add(new StringPin(this, "Query", PinMode.INPUT));

        this.outputs.add(new StringPin(this, "Response", PinMode.OUTPUT));
        this.outputs.add(new BooleanPin(this, "Error", PinMode.OUTPUT));

    }

    private DataPin getResponseOuputPin() {
        return (DataPin) getOutputs().get(1);
    }

    private DataPin getErrorOuputPin() {
        return (DataPin) getOutputs().get(2);
    }

    @Override
    public Object getOutputValue(DataPin pin) {
        if (pin == getResponseOuputPin()) {
            return this.response;
        }
        if (pin == getErrorOuputPin()) {
            return this.error;
        }

        throw new IllegalArgumentException(pin + " is not an declared output pin of this node");
    }

    @Override
    public boolean run() {
        this.response = null;
        this.error = false;
        String host = (String) getInputValue(1);
        int port = (Integer) getInputValue(2);
        boolean useHttps = false;
        if (getInputValue(3) != null && getInputValue(3) == Boolean.TRUE) {
            useHttps = true;
        }

        OllamaAPI api = new OllamaAPI(host, port, useHttps);
        String modelName = (String) getInputValue(4);
        String prompt = (String) getInputValue(5);
        try {
            this.response = api.query(modelName, prompt);
            return true;
        } catch (IOException e) {
            this.error = true;
            this.response = e.getMessage();
        }

        return false;
    }

    @Override
    public boolean canBeExecuted() {
        return isInputConnected(1) && isInputConnected(2) && isInputConnected(4) && isInputConnected(5);
    }

}
