package com.jvisualscripting.editor.editors;

import com.jvisualscripting.Node;
import com.jvisualscripting.Pin;
import com.jvisualscripting.Pin.PinMode;
import com.jvisualscripting.variable.StringPin;

public class StringSplitterEditor extends MultipleOutputNodeEditor {

    @Override
    public String getName() {
        return "StringSplitter";
    }

    @Override
    public Pin createNewPin(Node n) {
        return new StringPin(n, "String", PinMode.OUTPUT);
    }

}
