package com.jvisualscripting;

import java.util.ArrayList;

import com.jvisualscripting.Pin.PinMode;

public class FlowNode extends Node {

    public FlowNode() {

    }

    // only a single input execution pin and a single output execution pin as
    // functions only have one entry point and one exit point
    public FlowNode(String name) {
        super(name);
        this.inputs = new ArrayList<>(2);
        this.inputs.add(new ExecutionPin(this, "", PinMode.INPUT));
        this.outputs = new ArrayList<>(1);
        this.outputs.add(new ExecutionPin(this, "", PinMode.OUTPUT));
    }

    @Override
    public boolean execute() {
        this.setActive(true);
        boolean succeed = this.run();
        this.setActive(false);
        // Next
        ExecutionPin pin = ((ExecutionPin) this.outputs.get(0));

        if (pin.isConnected()) {
            pin.getConnectedPin().getNode().execute();
        }

        return succeed;
    }

    public boolean run() {
        return true;
    }

    @Override
    public boolean canBeExecuted() {
        return true;
    }
}
