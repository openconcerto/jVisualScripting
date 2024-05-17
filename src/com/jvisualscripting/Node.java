package com.jvisualscripting;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import com.jvisualscripting.Pin.PinMode;
import com.jvisualscripting.editor.EventGraphEditorPanel;

public abstract class Node implements Externalizable {
    protected List<Pin> inputs;
    protected List<Pin> outputs;
    protected EventGraph graph;
    private short x;
    private short y;
    protected short width;
    private short height;

    protected String name;
    private int id;
    private static int lastUsedId = 0;
    private boolean isBlocked = false;
    private boolean isActive = false;

    public Node() {
        this.id = getNextId();
    }

    public Node(String name) {
        this.id = getNextId();
        setName(name);
    }

    public int getId() {
        return this.id;
    }

    private static int getNextId() {
        lastUsedId++;
        return lastUsedId;
    }

    public void assignNewId() {
        this.id = getNextId();
        if (this.inputs != null) {
            for (Pin pin : this.inputs) {
                pin.assignNewId();
            }
        }
        if (this.outputs != null) {
            for (Pin pin : this.outputs) {
                pin.assignNewId();
            }
        }
    }

    protected void setActive(boolean b) {
        this.isActive = b;
        if (b) {
            this.graph.fireNodeActivated(this);
        } else {
            this.graph.fireNodeDesactivated(this);
        }
    }

    public boolean isActive() {
        return this.isActive;
    }

    public void setBlocked(boolean isBlocked) {
        this.isBlocked = isBlocked;
    }

    public boolean isBlocked() {
        return this.isBlocked;
    }

    public void clearState() {
        this.isBlocked = false;
        this.isActive = false;
    }

    public void setGrap(EventGraph graph) {
        this.graph = graph;
        computeSize();
    }

    public Pin getFirstInputExecutionPin() {
        for (Pin pin : this.inputs) {
            if (pin instanceof ExecutionPin) {
                return pin;
            }
        }
        return null;
    }

    public Pin getFirstOutputExecutionPin() {
        for (Pin pin : this.outputs) {
            if (pin instanceof ExecutionPin) {
                return pin;
            }
        }
        return null;
    }

    public Pin getLastOutputPin() {
        if (this.outputs == null)
            return null;
        return this.outputs.get(this.outputs.size() - 1);
    }

    public Pin getLastInputPin() {
        if (this.inputs == null)
            return null;
        return this.inputs.get(this.inputs.size() - 1);
    }

    public void removeInputPin(Pin pin) {
        this.inputs.remove(pin);
    }

    public void removeOutputPin(Pin pin) {
        this.outputs.remove(pin);
        for (Link link : this.graph.getLinks()) {
            if (link.hasPin(pin)) {
                this.graph.remove(link);
                break;
            }
        }
    }

    public int getX() {
        return this.x;
    }

    public int getY() {
        return this.y;
    }

    public int getWidth() {
        return this.width;
    }

    public int getHeight() {
        return this.height;
    }

    public String getName() {
        return this.name;
    }

    public void setLocation(int x, int y) {
        this.x = (short) x;
        this.y = (short) y;
    }

    public void setSize(short w, short h) {
        this.width = w;
        this.height = h;
    }

    public List<Pin> getInputs() {
        return this.inputs;
    }

    public List<Pin> getOutputs() {
        return this.outputs;
    }

    public int getInputSize() {
        if (this.inputs == null)
            return 0;
        return this.inputs.size();
    }

    public int getOutputSize() {
        if (this.outputs == null)
            return 0;
        return this.outputs.size();
    }

    public Object getOuputValue(DataPin pin) {
        return null;
    }

    public int getX(Pin p) {
        if (p.getMode() == PinMode.INPUT) {
            return this.x - 9;
        } else {
            return this.x + this.getWidth() + 10;
        }

    }

    public int getY(Pin p) {
        if (this.inputs != null) {
            int y = this.inputs.indexOf(p);
            if (y >= 0) {
                return this.y + 40 + y * 20;
            }
        }
        if (this.outputs != null) {
            int y = this.outputs.indexOf(p);
            if (y >= 0) {
                return this.y + 40 + y * 20;
            }
        }
        return 0;
    }

    public JSONObject exportGraphAndState(Engine e) {
        JSONObject obj = new JSONObject();
        obj.put("type", e.getTypeForNode(getClass()));
        obj.put("id", this.id);
        obj.put("x", this.x);
        obj.put("y", this.y);
        if (!this.name.isEmpty()) {
            obj.put("name", this.name);
        }
        if (this.inputs != null) {
            JSONArray array = new JSONArray();
            obj.put("inputs", array);
            for (Pin p : this.inputs) {
                array.add(p.exportGraphAndState(e));
            }
        }
        if (this.outputs != null) {
            JSONArray array = new JSONArray();
            obj.put("outputs", array);
            for (Pin p : this.outputs) {
                array.add(p.exportGraphAndState(e));
            }
        }
        return obj;
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeInt(this.id);
        out.writeShort(this.x);
        out.writeShort(this.y);
        out.writeUTF(this.name);
        Engine e = Engine.getDefault();
        // Inputs
        if (this.inputs == null) {
            out.writeByte(0);
        } else {
            out.writeByte(this.inputs.size());
            for (Pin p : this.inputs) {
                out.writeInt(e.getTypeForPin(p.getClass()));
                p.writeExternal(out);
            }
        }
        // Outputs
        if (this.outputs == null) {
            out.writeByte(0);
        } else {
            out.writeByte(this.outputs.size());
            for (Pin p : this.outputs) {
                out.writeInt(e.getTypeForPin(p.getClass()));
                p.writeExternal(out);
            }
        }

    }

    public void initFromJSON(JSONObject obj, Engine e) throws IOException {
        this.id = obj.getInt("id");
        if (this.id > lastUsedId) {
            lastUsedId = this.id;
        }
        this.x = (short) obj.getInt("x");
        this.y = (short) obj.getInt("y");
        this.name = obj.optString("name", "");

        if (obj.has("inputs")) {

            // Inputs
            JSONArray array = obj.getJSONArray("inputs");
            int inputCount = array.size();

            this.inputs = new ArrayList<>(inputCount);
            for (int i = 0; i < inputCount; i++) {
                JSONObject pinObj = array.getJSONObject(i);
                int type = pinObj.getInt("type");
                Class<? extends Pin> c = e.getClassForPinType(type);
                try {
                    Constructor<?> ctor = c.getDeclaredConstructor();
                    ctor.setAccessible(true);
                    Pin newInstance = (Pin) ctor.newInstance();
                    newInstance.initFromJSON(pinObj, e);
                    newInstance.setNode(this);
                    this.inputs.add(newInstance);
                } catch (Exception ex) {
                    throw new IOException("cannot create input pin : " + c, ex);
                }
            }

        }
        if (obj.has("outputs")) {
            // Outputs
            JSONArray array = obj.getJSONArray("outputs");
            int ouputCount = array.size();
            this.outputs = new ArrayList<>(ouputCount);
            for (int i = 0; i < ouputCount; i++) {
                JSONObject pinObj = array.getJSONObject(i);
                int type = pinObj.getInt("type");
                Class<? extends Pin> c = e.getClassForPinType(type);
                try {
                    Constructor<?> ctor = c.getDeclaredConstructor();
                    ctor.setAccessible(true);
                    Pin newInstance = (Pin) ctor.newInstance();
                    newInstance.initFromJSON(pinObj, e);
                    newInstance.setNode(this);
                    this.outputs.add(newInstance);
                } catch (Exception ex) {
                    throw new IOException("cannot create output pin : " + c, ex);
                }
            }

        }

    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        this.id = in.readInt();
        if (this.id > lastUsedId) {
            lastUsedId = this.id;
        }
        this.x = in.readShort();
        this.y = in.readShort();
        this.setName(in.readUTF());
        Engine e = Engine.getDefault();
        // Inputs
        int inputCount = in.readByte();
        if (inputCount == 0) {
            this.inputs = null;
        } else {
            this.inputs = new ArrayList<>(inputCount);
            for (int i = 0; i < inputCount; i++) {
                int type = in.readInt();
                Class<? extends Pin> c = e.getClassForPinType(type);
                try {
                    Constructor<?> ctor = c.getDeclaredConstructor();
                    ctor.setAccessible(true);
                    Pin newInstance = (Pin) ctor.newInstance();
                    newInstance.readExternal(in);
                    newInstance.setNode(this);
                    this.inputs.add(newInstance);
                } catch (Exception ex) {
                    throw new IOException("cannot create input pin : " + c, ex);
                }
            }
        }
        // Outputs
        int ouputCount = in.readByte();
        if (ouputCount == 0) {
            this.outputs = null;
        } else {
            this.outputs = new ArrayList<>(ouputCount);
            for (int i = 0; i < ouputCount; i++) {
                int type = in.readInt();
                Class<? extends Pin> c = e.getClassForPinType(type);
                try {
                    Constructor<?> ctor = c.getDeclaredConstructor();
                    ctor.setAccessible(true);
                    Pin newInstance = (Pin) ctor.newInstance();
                    newInstance.readExternal(in);
                    newInstance.setNode(this);
                    this.outputs.add(newInstance);
                } catch (Exception ex) {
                    throw new IOException("cannot create output pin : " + c, ex);
                }
            }
        }
    }

    public void setName(String name) {
        this.name = name;
        computeSize();
    }

    public void computeSize() {
        short w = EventGraphEditorPanel.GRID_SIZE * 6;
        if (this.name.length() > 13) {
            w += EventGraphEditorPanel.GRID_SIZE;
        }
        if (this.name.length() > 18) {
            w += EventGraphEditorPanel.GRID_SIZE;
        }
        this.width = w;
        this.height = (short) (20 * Math.max(getInputSize(), getOutputSize()) + 20 * 2 + 10);
    }

    public void disconnectAllPins() {
        if (this.outputs != null) {
            for (Pin pin : this.outputs) {
                pin.setConnectedPin(null);
            }
        }
        if (this.inputs != null) {
            for (Pin pin : this.inputs) {
                pin.setConnectedPin(null);
            }
        }
    }

    public abstract boolean execute();

    public abstract boolean canBeExecuted();

    @Override
    public String toString() {
        return this.getClass().getName() + " id:" + this.id + " : " + this.name;
    }

    public Object getInputValue(int pinIndex) {

        DataPin dataPin = (DataPin) this.inputs.get(pinIndex);
        if (!dataPin.isConnected()) {
            return null;
        }
        DataPin oPin = (DataPin) dataPin.getConnectedPin();
        Node previousNode = oPin.getNode();
        return previousNode.getOuputValue(oPin);

    }

}
