package com.jvisualscripting.variable;

import java.util.ArrayList;

import com.jvisualscripting.DataPin;
import com.jvisualscripting.Node;
import com.jvisualscripting.Pin;
import com.jvisualscripting.Pin.PinMode;

public class StringEquals extends Node {

    public StringEquals() {
        super("String equals");

        this.inputs = new ArrayList<>(1);
        this.inputs.add(new StringPin(this, "String 1", PinMode.INPUT));
        this.inputs.add(new StringPin(this, "String 2", PinMode.INPUT));
        this.outputs = new ArrayList<>(2);
        this.outputs.add(new BooleanPin(this, "Equals", PinMode.OUTPUT));
        this.outputs.add(new BooleanPin(this, "Not equals", PinMode.OUTPUT));
    }

    public StringPin getStringInputPin1() {
        final StringPin str1 = (StringPin) getInputs().get(0);
        return (StringPin) str1.getConnectedOutputPin();
    }

    public StringPin getStringInputPin2() {
        final StringPin str2 = (StringPin) getInputs().get(1);
        return (StringPin) str2.getConnectedOutputPin();
    }

    public Pin getLengthOuputPinEquals() {
        return getOutputs().get(0);
    }

    public Pin getLengthOuputPinNotEquals() {
        return getOutputs().get(1);
    }

    @Override
    public Object getOuputValue(DataPin pin) {

        if (pin == getLengthOuputPinEquals()) {
            if (!canBeExecuted()) {
                return Boolean.FALSE;
            }
            StringPin input1 = getStringInputPin1();
            final String string1 = input1.getNode().getOuputValue(input1).toString();

            StringPin input2 = getStringInputPin2();
            final String string2 = input2.getNode().getOuputValue(input2).toString();
            return string2.equals(string1);
        } else if (pin == getLengthOuputPinNotEquals()) {
            if (!canBeExecuted()) {
                return Boolean.FALSE;
            }
            StringPin input1 = getStringInputPin1();
            final String string1 = input1.getNode().getOuputValue(input1).toString();

            StringPin input2 = getStringInputPin2();
            final String string2 = input2.getNode().getOuputValue(input2).toString();
            return !string2.equals(string1);
        }

        throw new IllegalArgumentException(pin + " is not an declared output pin of this node");
    }

    @Override
    public boolean execute() {
        return true;
    }

    @Override
    public boolean canBeExecuted() {
        return getInputs().get(0).isConnected() && getInputs().get(1).isConnected();
    }
}
