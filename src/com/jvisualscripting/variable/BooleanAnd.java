package com.jvisualscripting.variable;

import java.util.ArrayList;

import com.jvisualscripting.DataPin;
import com.jvisualscripting.Node;
import com.jvisualscripting.Pin.PinMode;

public class BooleanAnd extends Node {

    public BooleanAnd() {
        this(2);
    }

    public BooleanAnd(int n) {
        super("And");
        this.inputs = new ArrayList<>(1);
        for (int i = 0; i < n; i++) {
            this.inputs.add(new BooleanPin(this, "Boolean", PinMode.INPUT));
        }
        this.outputs = new ArrayList<>(n);
        this.outputs.add(new BooleanPin(this, "And", PinMode.OUTPUT));
    }

    @Override
    public Object getOuputValue(DataPin pin) {
        boolean result = true;
        final int size = this.inputs.size();
        for (int i = 0; i < size; i++) {
            Object o = getInputValue(i);
            if (o != null) {
                result &= (Boolean) o;
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
