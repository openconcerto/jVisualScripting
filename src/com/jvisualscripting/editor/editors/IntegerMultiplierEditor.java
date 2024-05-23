package com.jvisualscripting.editor.editors;

import com.jvisualscripting.Node;
import com.jvisualscripting.Pin;
import com.jvisualscripting.Pin.PinMode;
import com.jvisualscripting.variable.IntegerPin;

public class IntegerMultiplierEditor extends MultipleInputNodeEditor {

    @Override
    public String getName() {
        return "Integer Multiplier";
    }

    @Override
    public Pin createNewPin(Node n) {
        return new IntegerPin(n, "Integer", PinMode.INPUT);
    }

}
