package com.jvisualscripting.flowcontrol;

import com.jvisualscripting.DataPin;
import com.jvisualscripting.ExecutionPin;
import com.jvisualscripting.FlowNode;
import com.jvisualscripting.Pin.PinMode;
import com.jvisualscripting.variable.IntegerPin;

public class ForLoop extends FlowNode {

    private int index = 0;

    public ForLoop() {
        super("For Loop");
        this.inputs.add(new IntegerPin(this, "First Index", PinMode.INPUT));
        this.inputs.add(new IntegerPin(this, "Last Index", PinMode.INPUT));
        this.outputs.clear();
        this.outputs.add(new ExecutionPin(this, "Loop Body", PinMode.OUTPUT));
        this.outputs.add(new IntegerPin(this, "Index", PinMode.OUTPUT));
        this.outputs.add(new ExecutionPin(this, "Completed", PinMode.OUTPUT));
    }

    @Override
    public boolean execute() {
        this.setActive(true);
        int firstIndex = 0;
        int lastIndex = 0;
        boolean succeed = false;
        if (this.getInputs().get(1).isConnected()) {
            IntegerPin in = (IntegerPin) ((DataPin) this.getInputs().get(1)).getConnectedOutputPin();
            firstIndex = (Integer) in.getNode().getOuputValue(in);
        }
        if (this.getInputs().get(2).isConnected()) {
            IntegerPin in = (IntegerPin) ((DataPin) this.getInputs().get(2)).getConnectedOutputPin();
            lastIndex = (Integer) in.getNode().getOuputValue(in);
        }
        for (int i = firstIndex; i <= lastIndex; i++) {
            this.index = i;
            ExecutionPin p = (ExecutionPin) this.getOutputs().get(0);

            if (p.isConnected()) {
                succeed = p.getConnectedPin().getNode().execute();
                if (!succeed) {
                    this.setActive(false);
                    return false;
                }
            }

        }
        ExecutionPin p = (ExecutionPin) this.getOutputs().get(2);
        if (p.isConnected()) {
            succeed = p.getConnectedPin().getNode().execute();
        } else {
            setBlocked(true);
        }
        this.setActive(false);
        return succeed;
    }

    @Override
    public Object getOuputValue(DataPin pin) {
        if (pin == this.getOutputs().get(1)) {
            return this.index;
        }
        throw new IllegalArgumentException(pin + " is not the output pin of this node");
    }

    @Override
    public int getWidth() {
        return 160;
    }
}
