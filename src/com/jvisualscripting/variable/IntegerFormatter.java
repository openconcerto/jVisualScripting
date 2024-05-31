package com.jvisualscripting.variable;

import java.util.ArrayList;

import com.jvisualscripting.DataPin;
import com.jvisualscripting.Node;
import com.jvisualscripting.Pin;
import com.jvisualscripting.Pin.PinMode;

public class IntegerFormatter extends Node {

    public IntegerFormatter() {
        super("Integer formatter");
        this.inputs = new ArrayList<>(1);
        this.inputs.add(new IntegerPin(this, "Integer", PinMode.INPUT));

        this.outputs = new ArrayList<>(2);
        this.outputs.add(new StringPin(this, "String", PinMode.OUTPUT));

    }

    public StringPin getDataOuputPin() {
        return (StringPin) getOutputs().get(0);
    }

    @Override
    public Object getOuputValue(DataPin pin) {
        if (pin == getDataOuputPin()) {
            Pin p = getIntegerInputPin().getConnectedPin();
            if (p == null) {
                return "";
            }
            IntegerPin connectedPin = (IntegerPin) p;
            return String.valueOf((connectedPin.getNode().getOuputValue(connectedPin)));
        }

        throw new IllegalArgumentException(pin + " is not an declared output pin of this node");
    }

    public IntegerPin getIntegerInputPin() {
        return (IntegerPin) getInputs().get(0);
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
