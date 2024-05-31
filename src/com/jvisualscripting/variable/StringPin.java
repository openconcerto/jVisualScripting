package com.jvisualscripting.variable;

import com.jvisualscripting.DataPin;
import com.jvisualscripting.Node;
import com.jvisualscripting.Pin;

public class StringPin extends DataPin {

    public StringPin() {
        // Serialisation
    }

    public StringPin(Node node, String name, PinMode mode) {
        super(node, name, mode);
    }

    @Override
    public boolean canConnectPin(Pin pin) {
        boolean b = super.canConnectPin(pin);
        if (!b) {
            return false;
        }
        return (pin instanceof StringPin);
    }

    @Override
    public Node createCompatibleVariableNode() {
        final StringVariable stringVariable = new StringVariable(getName());
        stringVariable.setValue("");
        return stringVariable;
    }

}
