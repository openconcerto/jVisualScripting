package com.jvisualscripting.variable;

import java.util.ArrayList;
import java.util.Calendar;

import com.jvisualscripting.DataPin;
import com.jvisualscripting.Node;
import com.jvisualscripting.Pin.PinMode;

public class CurrentTime extends Node {

    public CurrentTime() {
        super("Current time");
        this.outputs = new ArrayList<>(4);
        this.outputs.add(new IntegerPin(this, "Hour", PinMode.OUTPUT));
        this.outputs.add(new IntegerPin(this, "Minute", PinMode.OUTPUT));
        this.outputs.add(new IntegerPin(this, "Second", PinMode.OUTPUT));
        this.outputs.add(new IntegerPin(this, "Millisecond", PinMode.OUTPUT));
    }

    @Override
    public Object getOutputValue(DataPin pin) {
        int index = getOutputs().indexOf(pin);
        Calendar c = Calendar.getInstance();
        if (index == 0) {
            return c.get(Calendar.HOUR_OF_DAY);
        } else if (index == 1) {
            return c.get(Calendar.MINUTE);
        } else if (index == 2) {
            return c.get(Calendar.SECOND);
        } else if (index == 3) {
            return c.get(Calendar.MILLISECOND);
        }

        throw new IllegalArgumentException(pin + " is not an declared output pin of this node");
    }

    @Override
    public boolean execute() {
        return true;
    }

    @Override
    public boolean canBeExecuted() {
        return true;
    }
}
