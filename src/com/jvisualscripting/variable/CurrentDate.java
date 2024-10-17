package com.jvisualscripting.variable;

import java.util.ArrayList;
import java.util.Calendar;

import com.jvisualscripting.DataPin;
import com.jvisualscripting.Node;
import com.jvisualscripting.Pin.PinMode;

public class CurrentDate extends Node {

    public CurrentDate() {
        super("Current date");

        this.outputs = new ArrayList<>(3);
        this.outputs.add(new IntegerPin(this, "Year", PinMode.OUTPUT));
        this.outputs.add(new IntegerPin(this, "Month", PinMode.OUTPUT));
        this.outputs.add(new IntegerPin(this, "Day", PinMode.OUTPUT));

    }

    @Override
    public Object getOutputValue(DataPin pin) {
        int index = getOutputs().indexOf(pin);
        Calendar c = Calendar.getInstance();
        if (index == 0) {
            return c.get(Calendar.YEAR);
        } else if (index == 1) {
            return c.get(Calendar.MONTH) + 1;
        } else if (index == 2) {
            return c.get(Calendar.DAY_OF_MONTH);
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
