package com.jvisualscripting;

public class ExecutionPin extends Pin {

    public ExecutionPin() {

    }

    public ExecutionPin(Node node, String name, PinMode mode) {
        super(node, name, mode);
    }

    @Override
    public boolean canConnectPin(Pin pin) {
        boolean b = super.canConnectPin(pin);
        if (!b) {
            return false;
        }
        return (pin instanceof ExecutionPin);
    }
}
