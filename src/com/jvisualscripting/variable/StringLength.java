package com.jvisualscripting.variable;

import java.util.ArrayList;

import com.jvisualscripting.DataPin;
import com.jvisualscripting.Node;
import com.jvisualscripting.Pin;
import com.jvisualscripting.Pin.PinMode;

public class StringLength extends Node {

    public StringLength() {
        super("String length");

        this.inputs = new ArrayList<>(1);
        this.inputs.add(new StringPin(this, "In String", PinMode.INPUT));

        this.outputs = new ArrayList<>(2);
        this.outputs.add(new IntegerPin(this, "Length", PinMode.OUTPUT));
        this.outputs.add(new BooleanPin(this, "Empty", PinMode.OUTPUT));
    }

    public Pin getLengthOuputPin() {
        return getOutputs().get(0);
    }

    public Pin getEmptyOuputPin() {
        return getOutputs().get(1);
    }

    public StringPin getStringInputPin() {
        final StringPin str = (StringPin) getInputs().get(0);
        return (StringPin) str.getConnectedOutputPin();
    }

    @Override
    public Object getOutputValue(DataPin pin) {
        StringPin input = getStringInputPin();
        if (pin == getLengthOuputPin()) {
            if (input == null) {
                return 0;
            }
            final String string = input.getNode().getOutputValue(input).toString();
            return string.length();
        }
        if (pin == getEmptyOuputPin()) {
            if (input == null) {
                return Boolean.TRUE;
            }
            final String string = input.getNode().getOutputValue(input).toString();
            return string.isEmpty();
        }
        throw new IllegalArgumentException(pin + " is not an declared output pin of this node");
    }

    @Override
    public boolean execute() {
        return true;
    }

    @Override
    public boolean canBeExecuted() {
        return getInputs().get(0).isConnected();
    }
}
