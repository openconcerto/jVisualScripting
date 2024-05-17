package com.jvisualscripting.variable;

import com.jvisualscripting.DataPin;
import com.jvisualscripting.Node;
import com.jvisualscripting.Pin;

public class IntegerPin extends DataPin {
    protected IntegerPin() {
        // Serialisation
    }

    public IntegerPin(Node node, String name, PinMode mode) {
        super(node, name, mode);
    }

    @Override
    public boolean canConnectPin(Pin pin) {
        boolean b = super.canConnectPin(pin);
        if (!b) {
            return false;
        }
        return (pin instanceof IntegerPin);
    }

    @Override
    public Object getValue() {
        return Integer.parseInt(super.getValue().toString());
    }

}
