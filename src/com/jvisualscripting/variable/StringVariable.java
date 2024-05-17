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

    public void setValue(String value) {
        this.value = value;
        getDataOuputPin().setName(value);
    }

    public StringPin getDataOuputPin() {
        return (StringPin) getOutputs().get(0);
    }

    @Override
    public Object getOuputValue(DataPin pin) {
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
    public void writeExternal(ObjectOutput out) throws IOException {
        super.writeExternal(out);
        out.writeUTF(this.value);
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        super.readExternal(in);
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
