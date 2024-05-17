package com.jvisualscripting.variable;

import java.util.ArrayList;

import com.jvisualscripting.DataPin;
import com.jvisualscripting.Node;
import com.jvisualscripting.Pin.PinMode;

public class IntegerSplitter extends Node {

    public IntegerSplitter() {
        this(2);
    }

    public IntegerSplitter(int n) {
        super("Integer Splitter");
        this.inputs = new ArrayList<>(1);
        this.inputs.add(new IntegerPin(this, "Integer", PinMode.INPUT));

        this.outputs = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            this.outputs.add(new IntegerPin(this, "Integer", PinMode.OUTPUT));
        }

    }

    @Override
    public Object getOuputValue(DataPin pin) {
        final Object inputValue = getInputValue(0);
        if (inputValue == null) {
            return "";
        }
        return inputValue;
    }

    @Override
    public boolean canBeExecuted() {
        return getInputValue(0) != null;
    }

    @Override
    public boolean execute() {
        return true;
    }

}
