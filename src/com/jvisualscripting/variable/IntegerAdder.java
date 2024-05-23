package com.jvisualscripting.variable;

import java.util.ArrayList;

import com.jvisualscripting.DataPin;
import com.jvisualscripting.Node;
import com.jvisualscripting.Pin.PinMode;

public class IntegerAdder extends Node {

    public IntegerAdder() {
        this(2);
    }

    public IntegerAdder(int n) {
        super("Integer Adder");
        this.inputs = new ArrayList<>(1);
        for (int i = 0; i < n; i++) {
            this.inputs.add(new IntegerPin(this, "Integer", PinMode.INPUT));
        }
        this.outputs = new ArrayList<>(n);
        this.outputs.add(new IntegerPin(this, "Integer", PinMode.OUTPUT));
    }

    @Override
    public Object getOuputValue(DataPin pin) {
        int total = 0;
        final int size = this.inputs.size();
        for (int i = 0; i < size; i++) {
            Object o = getInputValue(i);
            if (o != null) {
                total += (Integer) o;
            }
        }

        return total;
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
