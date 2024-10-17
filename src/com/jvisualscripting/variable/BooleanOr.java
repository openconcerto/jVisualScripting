package com.jvisualscripting.variable;

import java.util.ArrayList;

import com.jvisualscripting.DataPin;
import com.jvisualscripting.Node;
import com.jvisualscripting.Pin.PinMode;

public class BooleanOr extends Node {

    public BooleanOr() {
        this(2);
    }

    public BooleanOr(int n) {
        super("Or");
        this.inputs = new ArrayList<>(1);
        for (int i = 0; i < n; i++) {
            this.inputs.add(new BooleanPin(this, "Boolean", PinMode.INPUT));
        }
        this.outputs = new ArrayList<>(n);
        this.outputs.add(new BooleanPin(this, "Or", PinMode.OUTPUT));
    }

    @Override
    public Object getOutputValue(DataPin pin) {
        boolean result = false;
        final int size = this.inputs.size();
        for (int i = 0; i < size; i++) {
            Object o = getInputValue(i);
            if (o != null) {
                result |= (Boolean) o;
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
