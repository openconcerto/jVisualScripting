package com.jvisualscripting.editor.editors;

import com.jvisualscripting.Node;
import com.jvisualscripting.Pin;
import com.jvisualscripting.Pin.PinMode;
import com.jvisualscripting.variable.FloatPin;

public class FloatSplitterEditor extends MultipleOutputNodeEditor {

    @Override
    public String getName() {
        return "Float Splitter";
    }

    @Override
    public Pin createNewPin(Node n) {
        return new FloatPin(n, "Float", PinMode.OUTPUT);
    }

}
