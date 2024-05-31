package com.jvisualscripting.function;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import org.json.JSONObject;

import com.jvisualscripting.DataPin;
import com.jvisualscripting.Engine;
import com.jvisualscripting.FlowNode;
import com.jvisualscripting.Node;
import com.jvisualscripting.Pin.PinMode;
import com.jvisualscripting.variable.StringPin;

public class Print extends FlowNode {
    private boolean newLine = true;

    public Print() {
        super("Print");
        this.inputs.add(new StringPin(this, "In String", PinMode.INPUT));
    }

    @Override
    public boolean run() {
        final String inputValue = this.getInputValue();
        if (inputValue != null) {
            System.out.print(inputValue);
        }
        if (this.newLine) {
            System.out.println();
        }
        return true;
    }

    @Override
    public boolean canBeExecuted() {
        final DataPin dataPin = (DataPin) this.inputs.get(1);
        return dataPin.isConnected();
    }

    private String getInputValue() {
        DataPin dataPin = (DataPin) this.inputs.get(1);
        if (dataPin.isConnected()) {
            DataPin oPin = (DataPin) dataPin.getConnectedPin();
            Node previousNode = oPin.getNode();
            Object value = previousNode.getOuputValue(oPin);
            if (value == null) {
                return null;
            }
            return value.toString();
        }
        return null;

    }

    public void setNewLine(boolean newLine) {
        this.newLine = newLine;
    }

    public boolean hasNewLine() {
        return this.newLine;
    }

    @Override
    public String getName() {
        if (!hasNewLine())
            return super.getName();
        else
            return super.getName() + " (new line)";
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        super.readExternal(in);
        this.setNewLine(in.readBoolean());
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        super.writeExternal(out);
        out.writeBoolean(this.newLine);
    }

    @Override
    public JSONObject exportGraphAndState(Engine e) {
        JSONObject obj = super.exportGraphAndState(e);
        obj.put("newLine", this.newLine);
        return obj;
    }

    @Override
    public void initFromJSON(JSONObject obj, Engine e) throws IOException {
        super.initFromJSON(obj, e);
        setNewLine(obj.getBoolean("newLine"));
    }
}
