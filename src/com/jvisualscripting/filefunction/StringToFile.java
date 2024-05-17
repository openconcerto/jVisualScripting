package com.jvisualscripting.filefunction;

import java.io.File;
import java.util.ArrayList;

import com.jvisualscripting.DataPin;
import com.jvisualscripting.Node;
import com.jvisualscripting.Pin;
import com.jvisualscripting.Pin.PinMode;
import com.jvisualscripting.variable.BooleanPin;
import com.jvisualscripting.variable.FilePin;
import com.jvisualscripting.variable.StringPin;

public class StringToFile extends Node {

    public StringToFile() {
        super("File");
        this.inputs = new ArrayList<>(1);
        this.inputs.add(new StringPin(this, "Path", PinMode.INPUT));

        this.outputs = new ArrayList<>(2);
        this.outputs.add(new FilePin(this, "File", PinMode.OUTPUT));
        this.outputs.add(new BooleanPin(this, "Exists", PinMode.OUTPUT));
        this.outputs.add(new BooleanPin(this, "Readable", PinMode.OUTPUT));
        this.outputs.add(new BooleanPin(this, "Writable", PinMode.OUTPUT));
    }

    public FilePin getFilePin() {
        return (FilePin) getOutputs().get(0);
    }

    @Override
    public Object getOuputValue(DataPin pin) {
        if (pin == getFilePin()) {
            return getFile();
        } else if (pin == getOutputs().get(1)) {
            File file = getFile();
            if (file == null)
                return false;
            return file.exists();
        } else if (pin == getOutputs().get(2)) {
            File file = getFile();
            if (file == null)
                return false;
            return file.canRead();
        } else if (pin == getOutputs().get(3)) {
            File file = getFile();
            if (file == null)
                return false;
            return file.canWrite();
        }

        throw new IllegalArgumentException(pin + " is not an declared output pin of this node");
    }

    private File getFile() {
        Pin p = getPathInputPin().getConnectedPin();
        if (p == null) {
            return null;
        }
        StringPin connectedPin = (StringPin) p;
        return new File((String) connectedPin.getNode().getOuputValue(connectedPin));
    }

    public StringPin getPathInputPin() {
        return (StringPin) getInputs().get(0);
    }

    @Override
    public boolean execute() {
        return true;
    }

    @Override
    public boolean canBeExecuted() {
        return true;
    }

   
}
