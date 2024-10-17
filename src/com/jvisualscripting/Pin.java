package com.jvisualscripting;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;

import com.jvisualscripting.editor.TempPin;

public abstract class Pin implements Externalizable {
    protected Node node;
    private String name;
    private List<Pin> connectedPins = new ArrayList<>(1);
    private PinMode mode;
    private int id;
    private static int lastUsedId = 0;

    public enum PinMode {
        INPUT, OUTPUT;
    }

    public Pin() {
        // Serialization
    }

    public Pin(Node node, String name, PinMode mode) {
        this.node = node;
        this.name = name;
        this.mode = mode;
        this.id = getNextId();
    }

    private static int getNextId() {
        lastUsedId++;
        return lastUsedId;
    }

    public Node getNode() {
        return this.node;
    }

    // Assign a pin to its node
    public void setNode(Node node) {
        this.node = node;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public PinMode getMode() {
        return this.mode;
    }

    public Pin getFirstConnectedPin() {
        if (this.connectedPins.isEmpty()) {
            return null;
        }
        return this.connectedPins.get(0);
    }

    public List<Pin> getConnectedPins() {
        return this.connectedPins;
    }

    public boolean canConnectPin(Pin pin) {
        if (pin.getNode().equals(getNode())) {
            return false;
        }
        if (this.connectedPins.contains(pin)) {
            return false;
        }
        if (pin instanceof TempPin) {
            return true;
        }
        if (pin.getMode() == PinMode.INPUT && this.getMode() == PinMode.OUTPUT) {
            return true;
        }
        return pin.getMode() == PinMode.OUTPUT && this.getMode() == PinMode.INPUT;
    }

    public void addConnectedPin(Pin pin) {
        if (pin == null) {
            throw new IllegalArgumentException("null pin");
        }
        if (this.connectedPins.contains(pin)) {
            throw new IllegalArgumentException(pin + " is already connected");
        }
        this.connectedPins.add(pin);
    }

    public void removeConnectedPins() {
        this.connectedPins.clear();
    }

    public void removeConnectedPin(Pin toPin) {
        boolean removed = this.connectedPins.remove(toPin);
        if (!removed) {
            System.err.println("Pin.removeConnectedPin() " + toPin + "+connected pins (" + (this.connectedPins.size()) + ")");
            for (Pin p : this.connectedPins) {
                System.err.println(p);
            }
            throw new IllegalArgumentException(toPin + " was not connected to " + this);
        }
    }

    public boolean isConnected() {
        if (this.connectedPins.isEmpty()) {
            return false;
        }
        for (Pin p : this.connectedPins) {
            if (!(p instanceof TempPin)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return super.toString() + " " + this.name + " [" + getMode().name() + ", node:" + getNode() + "]";

    }

    public int getId() {
        return this.id;
    }

    public void assignNewId() {
        this.id = getNextId();
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeInt(this.id);
        if (this.mode.equals(PinMode.INPUT)) {
            out.writeByte(0);
        } else {
            out.writeByte(1);
        }
        out.writeUTF(this.name);

    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        this.id = in.readInt();
        if (this.id > lastUsedId) {
            lastUsedId = this.id;
        }
        byte b = in.readByte();
        if (b == 0) {
            this.mode = PinMode.INPUT;
        } else {
            this.mode = PinMode.OUTPUT;
        }
        this.name = in.readUTF();
    }

    public JSONObject exportGraphAndState(Engine e) {
        final JSONObject obj = new JSONObject();
        obj.put("id", this.id);
        obj.put("type", e.getTypeForPin(getClass()));
        obj.put("name", this.name);
        if (this.mode.equals(PinMode.INPUT)) {
            obj.put("mode", "in");
        } else {
            obj.put("mode", "out");
        }
        return obj;
    }

    public void initFromJSON(JSONObject obj, Engine e) {
        this.id = obj.getInt("id");
        this.name = obj.optString("name", "");
        if (obj.get("mode").equals("in")) {
            this.mode = PinMode.INPUT;
        } else {
            this.mode = PinMode.OUTPUT;
        }
    }

    public abstract Node createCompatibleVariableNode();

}
