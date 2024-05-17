package com.jvisualscripting.variable;

import java.util.ArrayList;

import com.jvisualscripting.DataPin;
import com.jvisualscripting.Node;
import com.jvisualscripting.Pin;
import com.jvisualscripting.Pin.PinMode;

public class FloatComparator extends Node {

    public FloatComparator() {
        super("Float comparator");
        this.inputs = new ArrayList<>(1);
        this.inputs.add(new FloatPin(this, "Float A", PinMode.INPUT));
        this.inputs.add(new FloatPin(this, "Float B", PinMode.INPUT));

        this.outputs = new ArrayList<>(2);
        this.outputs.add(new BooleanPin(this, "A > B", PinMode.OUTPUT));
    }

    public BooleanPin getDataOuputPin() {
        return (BooleanPin) getOutputs().get(0);
    }

    @Override
    public Object getOuputValue(DataPin pin) {
        if (pin == getDataOuputPin()) {
            Pin pA = getInputs().get(0).getConnectedPin();
            if (pA == null) {
                return null;
            }
            Pin pB = getInputs().get(1).getConnectedPin();
            if (pB == null) {
                return null;
            }
            float vA = (Float) pA.getNode().getOuputValue((DataPin) pA);
            float vB = (Float) pB.getNode().getOuputValue((DataPin) pB);
            return vA > vB;
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
