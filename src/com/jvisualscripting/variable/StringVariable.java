package com.jvisualscripting.variable;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;

import org.json.JSONObject;

import com.jvisualscripting.DataPin;
import com.jvisualscripting.Engine;
import com.jvisualscripting.Node;
import com.jvisualscripting.Pin.PinMode;
import com.jvisualscripting.editor.EventGraphEditorPanel;

public class StringVariable extends Node {
    private String value;

    public StringVariable() {
        super("str");
        this.value = "";
        this.outputs = new ArrayList<>(1);
        this.outputs.add(new StringPin(this, "", PinMode.OUTPUT));
    }

    public StringVariable(String string) {
        super(string);
        this.value = string;
        this.outputs = new ArrayList<>(1);
        this.outputs.add(new StringPin(this, string, PinMode.OUTPUT));
    }

    public String getValue() {
        return this.value;
    }

    @Override
    public void computeSize() {
        super.computeSize();
        this.width += EventGraphEditorPanel.GRID_SIZE;
    }

    public void setValue(String value) {
        this.value = value;

        String name = value;
        if (name.length() > 12) {
            name = name.substring(0, 12) + "…";
        }
        name = name.replace('\r', ' ');
        name = name.replace('\n', ' ');
        getDataOuputPin().setName(name.trim());
    }

    public StringPin getDataOuputPin() {
        return (StringPin) getOutputs().get(0);
    }

    @Override
    public Object getOutputValue(DataPin pin) {
        if (pin != getDataOuputPin()) {
            throw new IllegalArgumentException(pin + " is not an declared output pin of this node");
        }
        return getValue();
    }

    @Override
    public boolean execute() {
        return true;
    }

    @Override
    public boolean canBeExecuted() {
        return true;
    }

    @Override
    public void writeExternal(Engine e, ObjectOutput out) throws IOException {
        super.writeExternal(e, out);
        out.writeUTF(this.value);
    }

    @Override
    public void readExternal(Engine e, ObjectInput in) throws IOException, ClassNotFoundException {
        super.readExternal(e, in);
        setValue(in.readUTF());
    }

    @Override
    public JSONObject exportGraphAndState(Engine e) {
        JSONObject obj = super.exportGraphAndState(e);
        obj.put("value", this.value);
        return obj;
    }

    @Override
    public void initFromJSON(JSONObject obj, Engine e) throws IOException {
        super.initFromJSON(obj, e);
        setValue(obj.getString("value"));
    }

    @Override
    public String toString() {
        return super.toString() + " name:" + this.name + " value:" + this.value;
    }
}
