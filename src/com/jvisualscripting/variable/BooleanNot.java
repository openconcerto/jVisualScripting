package com.jvisualscripting.variable;

import java.util.ArrayList;

import com.jvisualscripting.DataPin;
import com.jvisualscripting.Node;
import com.jvisualscripting.Pin.PinMode;

public class BooleanNot extends Node {

    public BooleanNot() {

        super("Not");
        this.inputs = new ArrayList<>(1);

        this.inputs.add(new BooleanPin(this, "In Boolean", PinMode.INPUT));

        this.outputs = new ArrayList<>(2);
        this.outputs.add(new BooleanPin(this, "Not", PinMode.OUTPUT));
        this.outputs.add(new BooleanPin(this, "In", PinMode.OUTPUT));
    }

    @Override
    public Object getOutputValue(DataPin pin) {
        if (pin == getOutputs().get(0)) {
            return !(Boolean) getInputValue(0);
        }
        return getInputValue(0);
    }

    @Override
    public boolean canBeExecuted() {
        return getInputs().get(0).isConnected();
    }

    @Override
    public boolean execute() {
        return true;
    }

}
