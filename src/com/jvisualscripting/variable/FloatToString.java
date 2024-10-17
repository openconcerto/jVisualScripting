package com.jvisualscripting.variable;

import java.util.ArrayList;

import com.jvisualscripting.DataPin;
import com.jvisualscripting.Node;
import com.jvisualscripting.Pin.PinMode;

public class FloatToString extends Node{

    public FloatToString() {
        super("Convert Float To String");
        this.inputs = new ArrayList<>(1);
        this.inputs.add(new FloatPin(this, "Float", PinMode.INPUT));
        this.outputs = new ArrayList<>(1);
        this.outputs.add(new StringPin(this, "String", PinMode.OUTPUT));
    }

    @Override
    public Object getOutputValue(DataPin pin){
        return String.valueOf(getInputValue(0));
    }

    @Override
    public boolean canBeExecuted(){return this.inputs.getFirst().isConnected();}

    @Override
    public boolean execute() {
        return true;
    }
}
