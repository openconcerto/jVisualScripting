package com.jvisualscripting.event;

import com.jvisualscripting.Node;
import com.jvisualscripting.Pin;

public class StartEventNode extends EventNode {

    public StartEventNode() {
        super("Start");
    }

    public void setNext(Node n) {
        this.setNext(n.getFirstInputExecutionPin());
    }

    @Override
    public boolean execute() {
        setActive(true);
        final Pin pin = this.outputs.get(0);
        boolean succeed = false;
        if (pin.isConnected()) {
            succeed = pin.getFirstConnectedPin().getNode().execute();
        }
        setActive(false);
        return succeed;
    }

    @Override
    public boolean canBeExecuted() {
        return this.outputs.get(0).isConnected();
    }
}
