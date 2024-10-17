package com.jvisualscripting.variable;

import java.util.ArrayList;

import com.jvisualscripting.DataPin;
import com.jvisualscripting.Node;
import com.jvisualscripting.Pin;
import com.jvisualscripting.Pin.PinMode;

public class StringToFloat extends Node {

    public StringToFloat() {
        super("String to Float");
        this.inputs = new ArrayList<>(1);
        this.inputs.add(new StringPin(this, "String", PinMode.INPUT));

        this.outputs = new ArrayList<>(2);
        this.outputs.add(new FloatPin(this, "Float", PinMode.OUTPUT));

    }

    public StringPin getStringInputPin() {
        return (StringPin) getInputs().get(0);
    }

    public FloatPin getDataOuputPin() {
        return (FloatPin) getOutputs().get(0);
    }

    @Override
    public Object getOuputValue(DataPin pin) {
        if (pin == getDataOuputPin()) {
            Pin p = getStringInputPin().getConnectedOutputPin();
            if (p == null) {
                return "";
            }
            StringPin connectedPin = (StringPin) p;
            return Float.parseFloat(((String) connectedPin.getNode().getOuputValue(connectedPin)));
        }

        throw new IllegalArgumentException(pin + " is not an declared output pin of this node");
    }

    @Override
    public boolean execute() {
        return true;
    }

    @Override
    public boolean canBeExecuted() {
        return true;
    }
}
