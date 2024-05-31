package com.jvisualscripting.variable;

import java.util.ArrayList;

import com.jvisualscripting.DataPin;
import com.jvisualscripting.Node;
import com.jvisualscripting.Pin.PinMode;

public class IntegerDivider extends Node {

    public IntegerDivider() {
        this(2);
    }

    public IntegerDivider(int n) {
        super("Integer Divider");
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
                if (i2 == 0) {
                    return 0;
                }
                return i1 / i2;
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
