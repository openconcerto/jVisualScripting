package com.jvisualscripting.variable;

import java.util.ArrayList;

import com.jvisualscripting.DataPin;
import com.jvisualscripting.Node;
import com.jvisualscripting.Pin.PinMode;

public class IntegerMultiplier extends Node {

    public IntegerMultiplier() {
        this(2);
    }

    public IntegerMultiplier(int n) {
        super("Integer Multiplier");
        this.inputs = new ArrayList<>(1);
        for (int i = 0; i < n; i++) {
            this.inputs.add(new IntegerPin(this, "Integer", PinMode.INPUT));
        }
        this.outputs = new ArrayList<>(n);
        this.outputs.add(new IntegerPin(this, "Integer", PinMode.OUTPUT));
    }

    @Override
    public Object getOutputValue(DataPin pin) {
        int result = 1;
        final int size = this.inputs.size();
        for (int i = 0; i < size; i++) {
            Object o = getInputValue(i);
            if (o != null) {
                result *= (Integer) o;
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
