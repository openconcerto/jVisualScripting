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

public class IntegerVariable extends Node {
    private int value;

    public IntegerVariable() {
        super("Integer");
        this.outputs = new ArrayList<>(1);
        this.outputs.add(new IntegerPin(this, "0", PinMode.OUTPUT));
    }

    @Override
    public void computeSize() {
        super.computeSize();
        if (this.name.length() <= 7) {
            this.width = 80;
        } else {
            this.width = (short) Math.round((this.name.length() * 30f) / 3);
        }
    }

    public int getValue() {
        return this.value;
    }

    public void setValue(int value) {
        this.value = value;
        getDataOuputPin().setName(String.valueOf(value));
    }

    public IntegerPin getDataOuputPin() {
        return (IntegerPin) getOutputs().get(0);
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
        out.writeInt(this.value);
    }

    @Override
    public void readExternal(Engine e, ObjectInput in) throws IOException, ClassNotFoundException {
        super.readExternal(e, in);
        setValue(in.readInt());
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
        setValue(obj.getInt("value"));
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
