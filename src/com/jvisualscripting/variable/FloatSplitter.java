package com.jvisualscripting.variable;

import java.util.ArrayList;

import com.jvisualscripting.DataPin;
import com.jvisualscripting.Node;
import com.jvisualscripting.Pin.PinMode;

public class FloatSplitter extends Node {

    public FloatSplitter() {
        this(2);
    }

    public FloatSplitter(int n) {
        super("Float Splitter");
        this.inputs = new ArrayList<>(1);
        this.inputs.add(new FloatPin(this, "Float", PinMode.INPUT));

        this.outputs = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            this.outputs.add(new FloatPin(this, "Float", PinMode.OUTPUT));
        }

    }

    @Override
    public Object getOutputValue(DataPin pin) {
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
