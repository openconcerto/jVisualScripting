package com.jvisualscripting.editor.editors;

import com.jvisualscripting.Node;
import com.jvisualscripting.Pin;
import com.jvisualscripting.Pin.PinMode;
import com.jvisualscripting.variable.StringPin;

public class StringConcatEditor extends MultipleInputNodeEditor {

    @Override
    public String getName() {
        return "String Concatenation";
    }

    @Override
    public Pin createNewPin(Node n) {
        return new StringPin(n, "String", PinMode.INPUT);
    }

}
