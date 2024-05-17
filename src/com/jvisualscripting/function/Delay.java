package com.jvisualscripting.function;

import com.jvisualscripting.DataPin;
import com.jvisualscripting.FlowNode;
import com.jvisualscripting.Node;
import com.jvisualscripting.Pin.PinMode;
import com.jvisualscripting.variable.IntegerPin;

public class Delay extends FlowNode {

    public Delay() {
        super("Delay");
        this.inputs.add(new IntegerPin(this, "Duration (ms)", PinMode.INPUT));
    }

    @Override
    public boolean execute() {
        Integer millis = this.getInputValue();
        if (millis == null) {
            return false;
        }
        try {
            Thread.sleep(millis);
        } catch (Exception e) {
            // Nothing
            return false;
        }
        return true;
    }

    @Override
    public boolean canBeExecuted() {
        return this.getInputValue() != null;
    }

    private Integer getInputValue() {
        DataPin dataPin = (DataPin) this.inputs.get(1);
        if (dataPin.isConnected()) {
            IntegerPin oPin = (IntegerPin) dataPin.getConnectedPin();
            Node previousNode = oPin.getNode();
            Integer value = (Integer) previousNode.getOuputValue(oPin);
            return value;
        }
        return null;

    }
}
