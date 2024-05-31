package com.jvisualscripting.editor;

import com.jvisualscripting.Node;
import com.jvisualscripting.Pin;

public class TempPin extends Pin {
    private int x;
    private int y;

    public TempPin() {
        // Serialization
    }

    public TempPin(int x, int y, PinMode mode) {
        super(null, "temp", mode);
        this.x = x;
        this.y = y;
        this.node = new Node("temp") {
            @Override
            public int getX(Pin p) {
                return TempPin.this.x;
            }

            @Override
            public int getY(Pin p) {
                return TempPin.this.y;
            }

            @Override
            public boolean execute() {
                return false;
            }

            @Override
            public boolean canBeExecuted() {
                return false;
            }
        };
    }

    void setLocation(int x, int y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public boolean canConnectPin(Pin pin) {
        return true;
    }

    @Override
    public void setNode(Node node) {
        // Nothing
    }

    @Override
    public Node getNode() {
        return this.node;

    }

    @Override
    public Node createCompatibleVariableNode() {
        return null;
    }

}
