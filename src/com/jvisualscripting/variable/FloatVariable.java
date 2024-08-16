package com.jvisualscripting.variable;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.text.DecimalFormat;
import java.util.ArrayList;

import org.json.JSONObject;

import com.jvisualscripting.DataPin;
import com.jvisualscripting.Engine;
import com.jvisualscripting.Node;
import com.jvisualscripting.Pin.PinMode;

public class FloatVariable extends Node {
    private float value;

    public FloatVariable() {
        super("Float");
        this.outputs = new ArrayList<>(1);
        this.outputs.add(new FloatPin(this, "0", PinMode.OUTPUT));
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

    public float getValue() {
        return this.value;
    }

    public void setValue(float value) {

        this.value = value;

        DecimalFormat df = new DecimalFormat("0");
        df.setMaximumFractionDigits(6);

        final String format = df.format(value);
        getDataOuputPin().setName(format);
    }

    public FloatPin getDataOuputPin() {
        return (FloatPin) getOutputs().get(0);
    }

    @Override
    public Object getOuputValue(DataPin pin) {
        if (pin != getDataOuputPin()) {
            throw new IllegalArgumentException(pin + " is not an declared output pin of this node");
        }
        return getValue();
    }

    @Override
    public void writeExternal(Engine e, ObjectOutput out) throws IOException {
        super.writeExternal(e, out);
        out.writeFloat(this.value);
    }

    @Override
    public void readExternal(Engine e, ObjectInput in) throws IOException, ClassNotFoundException {
        super.readExternal(e, in);
        setValue(in.readFloat());
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
        setValue(obj.getFloat("value"));
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
