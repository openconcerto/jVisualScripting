package com.jvisualscripting.filefunction;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import com.jvisualscripting.DataPin;
import com.jvisualscripting.ExecutionPin;
import com.jvisualscripting.Node;
import com.jvisualscripting.Pin;
import com.jvisualscripting.Pin.PinMode;
import com.jvisualscripting.variable.FilePin;
import com.jvisualscripting.variable.StringPin;

public class ReadLinesFromFile extends Node {

    private String currentLine = null;

    public ReadLinesFromFile() {
        super("ReadLinesFromFile");
        this.inputs = new ArrayList<>(2);
        this.inputs.add(new ExecutionPin(this, "", PinMode.INPUT));
        this.inputs.add(new FilePin(this, "File", PinMode.INPUT));
        this.outputs = new ArrayList<>(3);
        this.outputs.add(new ExecutionPin(this, "Line read", PinMode.OUTPUT));
        this.outputs.add(new StringPin(this, "Line", PinMode.OUTPUT));
        this.outputs.add(new ExecutionPin(this, "Completed", PinMode.OUTPUT));
    }

    @Override
    public boolean canBeExecuted() {
        return isInputConnected(1);
    }

    @Override
    public boolean execute() {
        this.currentLine = null;
        final List<Pin> connectedPins = getOutputs().get(0).getConnectedPins();
        if (connectedPins.isEmpty()) {
            return false;
        }

        File inFile = (File) getInputValue(1);
        try (BufferedReader bReader = new BufferedReader(new InputStreamReader(new FileInputStream(inFile), StandardCharsets.UTF_8))) {
            String line = null;
            do {
                line = bReader.readLine();
                this.currentLine = line;
                if (this.currentLine != null) {
                    for (Pin connectedPin : connectedPins) {

                        boolean r = connectedPin.getNode().execute();
                        if (!r) {
                            return false;
                        }
                    }
                }
            } while (line != null);

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        final List<Pin> endPins = getOutputs().get(2).getConnectedPins();
        if (endPins.isEmpty()) {
            return false;
        }
        for (Pin endPin : endPins) {
            boolean r = endPin.getNode().execute();
            if (!r) {
                return false;
            }
        }
        return true;
    }

    @Override
    public Object getOuputValue(DataPin pin) {
        if (pin == getOutputs().get(1)) {
            return this.currentLine;
        }

        throw new IllegalArgumentException(pin + " is not an declared output pin of this node");
    }

}
