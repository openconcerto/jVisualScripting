package com.jvisualscripting.function;

import com.jvisualscripting.FlowNode;
import com.jvisualscripting.Pin.PinMode;
import com.jvisualscripting.variable.IntegerPin;

public class Delay extends FlowNode {

    public Delay() {
        super("Delay");
        this.inputs.add(new IntegerPin(this, "Duration (ms)", PinMode.INPUT));
    }

    @Override
    public boolean run() {
        Object millis = this.getInputValue(1);
        if (millis == null) {
            return false;
        }
        try {
            Thread.sleep((Integer) millis);
        } catch (Exception e) {
            // Nothing
            return false;
        }
        return true;
    }

    @Override
    public boolean canBeExecuted() {
        return this.getInputValue(1) != null;
    }

}
