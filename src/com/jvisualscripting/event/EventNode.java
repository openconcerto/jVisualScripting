package com.jvisualscripting.event;

import java.util.ArrayList;

import com.jvisualscripting.ExecutionPin;
import com.jvisualscripting.Node;
import com.jvisualscripting.Pin;
import com.jvisualscripting.Pin.PinMode;

public abstract class EventNode extends Node {

    protected EventNode(String name) {
        super(name);
        this.outputs = new ArrayList<>(1);
        this.outputs.add(new ExecutionPin(this, "", PinMode.OUTPUT));
    }

    public void setNext(Pin p) {
        this.graph.addLink(this.outputs.get(0), p);
    }

}
