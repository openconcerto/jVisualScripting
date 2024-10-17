package com.jvisualscripting.variable;

import java.util.ArrayList;

import com.jvisualscripting.DataPin;
import com.jvisualscripting.Node;
import com.jvisualscripting.Pin.PinMode;

public class StringToInteger extends Node{

    public StringToInteger() {
        super("Convert String to Integer");
        this.inputs = new ArrayList<>(1);
        this.inputs.add(new StringPin(this, "String", PinMode.INPUT));
        this.outputs = new ArrayList<>(1);
        this.outputs.add(new IntegerPin(this, "Int", PinMode.OUTPUT));
    }

    @Override
    public Object getOutputValue(DataPin pin){
        String string = (String) getInputValue(0);
        if (string == null) {
            return null;
        }
        return Integer.parseInt(string);
    }

    @Override
    public boolean canBeExecuted(){return this.inputs.getFirst().isConnected();}

    @Override
    public boolean execute() {
        return true;
    }
}
