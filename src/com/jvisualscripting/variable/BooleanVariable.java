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

public class BooleanVariable extends Node {
    private Boolean value;

    public BooleanVariable() {
        // Serialiation
    }

    public BooleanVariable(boolean b) {
        super("Boolean");
        this.value = b;
        this.outputs = new ArrayList<>(1);
        if (b)
            this.outputs.add(new BooleanPin(this, "True", PinMode.OUTPUT));
        else
            this.outputs.add(new BooleanPin(this, "False", PinMode.OUTPUT));
    }

    @Override
    public int getWidth() {
        return 80;
    }

    public Boolean getValue() {
        return this.value;
    }

    public void setValue(Boolean value) {
        this.value = value;
    }

    public BooleanPin getDataOuputPin() {
        return (BooleanPin) getOutputs().get(0);
    }

    @Override
    public Object getOutputValue(DataPin pin) {
        if (pin != getDataOuputPin()) {
            throw new IllegalArgumentException(pin + " is not an declared output pin of this node");
        }
        return getValue();
    }

    @Override
    public void writeExternal(Engine e, ObjectOutput out) throws IOException {
        super.writeExternal(e, out);
        out.writeBoolean(this.value);
    }

    @Override
    public void readExternal(Engine e, ObjectInput in) throws IOException, ClassNotFoundException {
        super.readExternal(e, in);
        setValue(in.readBoolean());
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
        setValue(obj.getBoolean("value"));
    }

    @Override
    public String toString() {
        return super.toString() + " name:" + this.name + " value:" + this.value;
    }

    @Override
    public boolean execute() {
        return true;
    }

    @Override
    public boolean canBeExecuted() {
        return true;
    }
}
