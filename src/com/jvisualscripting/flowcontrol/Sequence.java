package com.jvisualscripting.flowcontrol;

import com.jvisualscripting.ExecutionPin;
import com.jvisualscripting.FlowNode;
import com.jvisualscripting.Pin.PinMode;

public class Sequence extends FlowNode {

    protected Sequence() {
        this(2);
    }

    public Sequence(int outputPinCount) {
        super("Sequence");
        this.outputs.clear();
        for (int i = 0; i < outputPinCount; i++) {
            this.outputs.add(new ExecutionPin(this, "Then " + i, PinMode.OUTPUT));
        }
    }

    @Override
    public boolean execute() {
        this.setActive(true);
        int outputPinCount = getOutputSize();
        for (int i = 0; i < outputPinCount; i++) {
            ExecutionPin p = (ExecutionPin) this.getOutputs().get(i);
            if (p.isConnected()) {
                boolean succeed = p.getConnectedPin().getNode().execute();
                if (!succeed) {
                    setActive(false);
                    return false;
                }

            }

        }
        setActive(false);
        return true;
    }

}
