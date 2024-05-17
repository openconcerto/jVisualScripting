package com.jvisualscripting.function;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import com.jvisualscripting.DataPin;
import com.jvisualscripting.FlowNode;
import com.jvisualscripting.Pin.PinMode;
import com.jvisualscripting.variable.FilePin;
import com.jvisualscripting.variable.IntegerPin;
import com.jvisualscripting.variable.StringPin;

public class ExternalCommand extends FlowNode {

    private String out;
    private String err;
    private int exitCode;

    public ExternalCommand() {
        this(4);
    }

    public ExternalCommand(int argsCount) {
        super("External Command");
        this.inputs.add(new StringPin(this, "Command", PinMode.INPUT));
        this.inputs.add(new FilePin(this, "Current dir", PinMode.INPUT));
        for (int i = 0; i < argsCount; i++) {
            this.inputs.add(new StringPin(this, "Arg " + (i + 1), PinMode.INPUT));
        }

        this.outputs.add(new StringPin(this, "Out", PinMode.OUTPUT));
        this.outputs.add(new StringPin(this, "Err", PinMode.OUTPUT));
        this.outputs.add(new IntegerPin(this, "Status", PinMode.OUTPUT));

    }

    @Override
    public Object getOuputValue(DataPin pin) {
        if (pin == getOutOuputPin()) {
            return this.out;
        }
        if (pin == getErrOuputPin()) {
            return this.err;
        }
        if (pin == getExitCodeOuputPin()) {
            return this.exitCode;
        }
        throw new IllegalArgumentException(pin + " is not an declared output pin of this node");
    }

    private DataPin getOutOuputPin() {
        return (DataPin) getOutputs().get(1);
    }

    private DataPin getErrOuputPin() {
        return (DataPin) getOutputs().get(2);
    }

    private DataPin getExitCodeOuputPin() {
        return (DataPin) getOutputs().get(3);
    }

    @Override
    public boolean run() {
        this.out = "";
        this.err = "";
        this.exitCode = 0;

        final String cmd = this.getCommandValue();
        final ProcessBuilder processBuilder = new ProcessBuilder();

        // Command and args
        final List<String> list = new ArrayList<>();
        list.add(cmd);
        final int argCount = this.inputs.size() - 3;
        for (int i = 0; i < argCount; i++) {
            Object value = getInputValue(3 + i);
            if (value != null) {
                list.add(value.toString());
            }
        }
        processBuilder.command(list);

        // Current directory
        if (this.getDirValue() != null) {
            processBuilder.directory(this.getDirValue());
        }

        try {

            final Process process = processBuilder.start();
            // StdOut
            final BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            final StringBuilder bStdout = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                bStdout.append(line);
                bStdout.append("\r\n");
            }
            // StdErr
            final StringBuilder bStderr = new StringBuilder();
            final BufferedReader readerErr = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            String lineErr;
            while ((lineErr = readerErr.readLine()) != null) {
                bStderr.append(lineErr);
                bStderr.append("\r\n");
            }

            this.out = bStdout.toString();
            this.err = bStderr.toString();
            this.exitCode = process.waitFor();
        } catch (Exception e) {
            this.err += e.getMessage();
            return false;
        }
        return true;
    }

    @Override
    public boolean canBeExecuted() {
        final DataPin dataPin = (DataPin) this.inputs.get(1);
        return dataPin.isConnected();
    }

    private String getCommandValue() {
        final Object value = getInputValue(1);
        if (value != null) {
            return value.toString();
        }
        return null;

    }

    private File getDirValue() {
        final Object value = getInputValue(2);
        if (value != null) {
            return (File) value;
        }
        return null;

    }

}
