package com.jvisualscripting.variable;

import com.jvisualscripting.DataPin;
import com.jvisualscripting.Node;
import com.jvisualscripting.Pin;
import com.jvisualscripting.filefunction.StringToFile;

public class FilePin extends DataPin {

    public FilePin() {
        // Serialisation
    }

    public FilePin(Node node, String name, PinMode mode) {
        super(node, name, mode);
    }

    @Override
    public boolean canConnectPin(Pin pin) {
        boolean b = super.canConnectPin(pin);
        if (!b) {
            return false;
        }
        return (pin instanceof FilePin);
    }

    @Override
    public Node createCompatibleVariableNode() {
        return new StringToFile();
    }

}
