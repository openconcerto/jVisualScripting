package com.jvisualscripting.variable;

import java.util.ArrayList;

import com.jvisualscripting.DataPin;
import com.jvisualscripting.Node;
import com.jvisualscripting.Pin.PinMode;

public class IndexOf extends Node {

    public IndexOf() {
        super("IndexOf");
        this.inputs = new ArrayList<>(2);
        this.inputs.add(new StringPin(this, "Text", PinMode.INPUT));
        this.inputs.add(new StringPin(this, "Part", PinMode.INPUT));

        this.outputs = new ArrayList<>(2);
        this.outputs.add(new IntegerPin(this, "First", PinMode.OUTPUT));
        this.outputs.add(new IntegerPin(this, "Last", PinMode.OUTPUT));

    }

    @Override
    public Object getOutputValue(DataPin pin) {
        if (pin == getOutputs().get(0)) {
            // First
            String in1 = getInputValue(0).toString();
            String in2 = getInputValue(1).toString();
            return in1.indexOf(in2);
        } else if (pin == getOutputs().get(1)) {
            // Last
            String in1 = getInputValue(0).toString();
            String in2 = getInputValue(1).toString();
            return in1.lastIndexOf(in2);
        }
        throw new IllegalArgumentException(pin + " is not an declared output pin of this node");
    }

    @Override
    public boolean canBeExecuted() {
        return getInputValue(0) != null && getInputValue(1) != null;
    }

    @Override
    public boolean execute() {
        return true;
    }

}
