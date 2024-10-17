package com.jvisualscripting;

public class ExecutionPin extends Pin {

    public ExecutionPin() {

    }

    public ExecutionPin(Node node, String name, PinMode mode) {
        super(node, name, mode);
    }

    @Override
    public boolean canConnectPin(Pin pin) {
        if (isConnected()) {
            return false;
        }

        boolean b = super.canConnectPin(pin);
        if (!b) {
            return false;
        }
        return (pin instanceof ExecutionPin);
    }

    @Override
    public Node createCompatibleVariableNode() {
        return null;
    }

    public Pin getConnectedPin() {
        return getFirstConnectedPin();
    }
}
