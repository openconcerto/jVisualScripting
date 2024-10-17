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
    public Object getOutputValue(DataPin pin) {
        if (pin == getDataOuputPin()) {
            Pin pA = ((DataPin) getInputs().get(0)).getConnectedOutputPin();
            if (pA == null) {
                return null;
            }
            Pin pB = ((DataPin) getInputs().get(1)).getConnectedOutputPin();
            if (pB == null) {
                return null;
            }
            float vA = (Float) pA.getNode().getOutputValue((DataPin) pA);
            float vB = (Float) pB.getNode().getOutputValue((DataPin) pB);
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
