package com.jvisualscripting.variable;

import java.util.ArrayList;

import com.jvisualscripting.DataPin;
import com.jvisualscripting.Node;
import com.jvisualscripting.Pin.PinMode;

public class StringParameter extends Node {

    public StringParameter() {
        super("param");
        this.outputs = new ArrayList<>(1);
        this.outputs.add(new StringPin(this, "", PinMode.OUTPUT));
    }

    public StringParameter(String string) {
        super(string);
        this.outputs = new ArrayList<>(1);
        this.outputs.add(new StringPin(this, string, PinMode.OUTPUT));
    }

    public String getValue() {
        return this.graph.getParameter(getName());
    }

    public StringPin getDataOuputPin() {
        return (StringPin) getOutputs().get(0);
    }

    @Override
    public Object getOuputValue(DataPin pin) {
        if (pin != getDataOuputPin()) {
            throw new IllegalArgumentException(pin + " is not an declared output pin of this node");
        }
        return getValue();
    }

    @Override
    public boolean execute() {
        return true;
    }

    @Override
    public boolean canBeExecuted() {
        return true;
    }

    @Override
    public String toString() {
        return super.toString() + " name:" + this.name + " value:" + getValue();
    }
}
