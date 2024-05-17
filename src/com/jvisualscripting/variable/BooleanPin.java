package com.jvisualscripting.variable;

import com.jvisualscripting.DataPin;
import com.jvisualscripting.Node;
import com.jvisualscripting.Pin;

public class BooleanPin extends DataPin {

    protected BooleanPin() {
        // Serialisation
    }

    public BooleanPin(Node node, String name, PinMode mode) {
        super(node, name, mode);
    }

    @Override
    public boolean canConnectPin(Pin pin) {
        boolean b = super.canConnectPin(pin);
        if (!b)
            return false;
        return pin instanceof BooleanPin;
    }
}
