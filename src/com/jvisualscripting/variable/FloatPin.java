package com.jvisualscripting.variable;

import com.jvisualscripting.DataPin;
import com.jvisualscripting.Node;
import com.jvisualscripting.Pin;

public class FloatPin extends DataPin {

    public FloatPin() {
        // Serialisation
    }

    public FloatPin(Node node, String name, PinMode mode) {
        super(node, name, mode);
    }

    @Override
    public boolean canConnectPin(Pin pin) {
        boolean b = super.canConnectPin(pin);
        if (!b) {
            return false;
        }
        return (pin instanceof FloatPin);
    }

    @Override
    public Object getValue() {
        return Float.parseFloat(super.getValue().toString());
    }

    @Override
    public Node createCompatibleVariableNode() {
        return new FloatVariable();
    }
}
