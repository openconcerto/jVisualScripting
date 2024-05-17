package com.jvisualscripting.variable;

import java.text.DecimalFormat;
import java.util.ArrayList;

import com.jvisualscripting.DataPin;
import com.jvisualscripting.Node;
import com.jvisualscripting.Pin;
import com.jvisualscripting.Pin.PinMode;

public class FloatFormatter extends Node {

    public FloatFormatter() {
        super("Float formatter");
        this.inputs = new ArrayList<>(1);
        this.inputs.add(new FloatPin(this, "Float", PinMode.INPUT));

        this.outputs = new ArrayList<>(2);
        this.outputs.add(new StringPin(this, "String", PinMode.OUTPUT));

    }

    public StringPin getDataOuputPin() {
        return (StringPin) getOutputs().get(0);
    }

    @Override
    public Object getOuputValue(DataPin pin) {
        if (pin == getDataOuputPin()) {
            Pin p = getIntegerInputPin().getConnectedPin();
            if (p == null) {
                return "";
            }
            FloatPin connectedPin = (FloatPin) p;
            Float value = ((Float) connectedPin.getNode().getOuputValue(connectedPin));

            DecimalFormat df = new DecimalFormat("0");
            df.setMaximumFractionDigits(6);
            return df.format(value);
        }

        throw new IllegalArgumentException(pin + " is not an declared output pin of this node");
    }

    public FloatPin getIntegerInputPin() {
        return (FloatPin) getInputs().get(0);
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
