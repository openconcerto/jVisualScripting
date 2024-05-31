package com.jvisualscripting.variable;

import java.util.ArrayList;

import com.jvisualscripting.DataPin;
import com.jvisualscripting.Node;
import com.jvisualscripting.Pin.PinMode;

public class IntegerSubstract extends Node {

    public IntegerSubstract() {
        this(2);
    }

    public IntegerSubstract(int n) {
        super("Integer Substracter");
        this.inputs = new ArrayList<>(1);
        this.inputs.add(new IntegerPin(this, "Integer", PinMode.INPUT));
        this.inputs.add(new IntegerPin(this, "Integer", PinMode.INPUT));
        this.outputs = new ArrayList<>(n);
        this.outputs.add(new IntegerPin(this, "Integer", PinMode.OUTPUT));
    }

    @Override
    public Object getOuputValue(DataPin pin) {
        int total = 0;

        Object o1 = getInputValue(0);

        if (o1 != null) {
            int i1 = (Integer) o1;
            Object o2 = getInputValue(1);
            if (o2 != null) {
                int i2 = (Integer) o2;
                return i1 - i2;
            }
        }

        return total;
    }

    @Override
    public boolean canBeExecuted() {
        return this.inputs.get(0).isConnected() && this.inputs.get(1).isConnected();
    }

    @Override
    public boolean execute() {
        return true;
    }

}
