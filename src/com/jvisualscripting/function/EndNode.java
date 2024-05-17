package com.jvisualscripting.function;

import java.util.ArrayList;

import com.jvisualscripting.ExecutionPin;
import com.jvisualscripting.Node;
import com.jvisualscripting.Pin.PinMode;

public class EndNode extends Node {

    public EndNode() {
        super("End");
        this.inputs = new ArrayList<>(2);
        this.inputs.add(new ExecutionPin(this, "", PinMode.INPUT));
    }

    @Override
    public boolean execute() {
        this.setActive(true);
        return true;
    }

    @Override
    public boolean canBeExecuted() {
        return true;
    }

}
