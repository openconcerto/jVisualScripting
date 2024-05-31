package com.jvisualscripting.variable;

import java.util.ArrayList;

import com.jvisualscripting.DataPin;
import com.jvisualscripting.Node;
import com.jvisualscripting.Pin.PinMode;

public class StringConcat extends Node {

    public StringConcat() {
        this(2);
    }

    public StringConcat(int n) {
        super("String Concatenation");
        this.inputs = new ArrayList<>(1);
        this.inputs.add(new StringPin(this, "Separator", PinMode.INPUT));
        for (int i = 0; i < n; i++) {
            this.inputs.add(new StringPin(this, "String", PinMode.INPUT));
        }
        this.outputs = new ArrayList<>(n);
        this.outputs.add(new StringPin(this, "String", PinMode.OUTPUT));
    }

    @Override
    public Object getOuputValue(DataPin pin) {
        final StringBuilder result = new StringBuilder();
        final int size = this.inputs.size();
        final Object separator = getInputValue(0);
        if (separator != null && !((String) separator).isEmpty()) {
            for (int i = 1; i < size; i++) {
                Object o = getInputValue(i);
                if (o != null) {
                    result.append((String) o);
                    if (i < (size - 1)) {
                        result.append(separator);
                    }
                }
            }
        } else {
            for (int i = 1; i < size; i++) {
                Object o = getInputValue(i);
                if (o != null) {
                    result.append((String) o);
                }
            }
        }

        return result;
    }

    @Override
    public boolean canBeExecuted() {
        return true;
    }

    @Override
    public boolean execute() {
        return true;
    }

}
