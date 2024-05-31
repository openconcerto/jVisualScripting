package com.jvisualscripting.editor.editors;

import com.jvisualscripting.Node;
import com.jvisualscripting.Pin;
import com.jvisualscripting.Pin.PinMode;
import com.jvisualscripting.variable.BooleanPin;

public class MultipleBooleanInputEditor extends MultipleInputNodeEditor {

    @Override
    public String getName() {
        return "Multiple Boolean";
    }

    @Override
    public Pin createNewPin(Node n) {
        return new BooleanPin(n, "Boolean", PinMode.INPUT);
    }

}
