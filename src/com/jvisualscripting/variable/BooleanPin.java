package com.jvisualscripting.variable;

import com.jvisualscripting.DataPin;
import com.jvisualscripting.Node;
import com.jvisualscripting.Pin;

public class BooleanPin extends DataPin {

    public BooleanPin() {
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

    @Override
    public Node createCompatibleVariableNode() {
        // We don't create a boolean node variable
        // The user will connect an output of a node
        return null;
    }
}
