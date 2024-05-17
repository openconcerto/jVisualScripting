package com.jvisualscripting.variable;

import java.util.ArrayList;

import com.jvisualscripting.DataPin;
import com.jvisualscripting.Node;
import com.jvisualscripting.Pin.PinMode;

public class StringSplitter extends Node {

    public StringSplitter() {
        this(2);
    }

    public StringSplitter(int n) {
        super("String Splitter");
        this.inputs = new ArrayList<>(1);
        this.inputs.add(new StringPin(this, "String", PinMode.INPUT));

        this.outputs = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            this.outputs.add(new StringPin(this, "String", PinMode.OUTPUT));
        }

    }

    @Override
    public Object getOuputValue(DataPin pin) {
        final Object inputValue = getInputValue(0);
        if (inputValue == null) {
            return "";
        }
        return inputValue.toString();
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
