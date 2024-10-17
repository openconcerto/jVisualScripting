package com.jvisualscripting.flowcontrol;

import java.util.ArrayList;

import com.jvisualscripting.ExecutionPin;
import com.jvisualscripting.FlowNode;
import com.jvisualscripting.Node;
import com.jvisualscripting.Pin.PinMode;
import com.jvisualscripting.variable.BooleanPin;

public class Branch extends FlowNode {

    public Branch() {
        super("Branch");
        this.inputs = new ArrayList<>(2);
        this.inputs.add(new ExecutionPin(this, "", PinMode.INPUT));
        this.inputs.add(new BooleanPin(this, "Condition", PinMode.INPUT));
        this.outputs = new ArrayList<>(1);
        this.outputs.add(new ExecutionPin(this, "True", PinMode.OUTPUT));
        this.outputs.add(new ExecutionPin(this, "False", PinMode.OUTPUT));
    }

    @Override
    public boolean execute() {
        this.setActive(true);
        BooleanPin dataPin = (BooleanPin) this.getInputs().get(1);
        boolean succeed = false;
        if (dataPin.isConnected()) {
            BooleanPin oPin = (BooleanPin) dataPin.getConnectedOutputPin();
            Node previousNode = oPin.getNode();
            Boolean b = (Boolean) (previousNode.getOutputValue(oPin));
            if (Boolean.TRUE.equals(b)) {
                ExecutionPin e = ((ExecutionPin) this.outputs.get(0));
                if (e.isConnected()) {

                    if (e.isConnected()) {
                        succeed = e.getConnectedPin().getNode().execute();
                    }

                } else {
                    ExecutionPin eFalse = ((ExecutionPin) this.outputs.get(1));
                    if (!eFalse.isConnected()) {
                        setBlocked(true);
                    }
                }
            } else {
                ExecutionPin e = ((ExecutionPin) this.outputs.get(1));
                if (e.isConnected()) {

                    if (e.isConnected()) {
                        succeed = e.getConnectedPin().getNode().execute();
                    }
                } else {
                    ExecutionPin eTrue = ((ExecutionPin) this.outputs.get(0));
                    if (!eTrue.isConnected()) {
                        setBlocked(true);
                    }
                }
            }

        } else {
            setBlocked(true);
        }
        this.setActive(false);
        return succeed;
    }

}
