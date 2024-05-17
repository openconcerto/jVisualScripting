package com.jvisualscripting;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

public class DataPin extends Pin {
    protected Object value;
    protected Object defaultValue = "?";

    public DataPin() {

    }

    public DataPin(Node node, String name, PinMode mode) {
        super(node, name, mode);
        this.value = this.defaultValue;
    }

    public void setDefaultValue(Object defaultValue) {
        this.defaultValue = defaultValue;
        if (this.value == null) {
            this.value = defaultValue;
        }
    }

    public Object getValue() {
        return this.value;
    }

    @Override
    public boolean canConnectPin(Pin pin) {
        boolean b = super.canConnectPin(pin);
        if (!b) {
            return false;
        }
        return (pin instanceof DataPin);
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        super.writeExternal(out);
        out.writeObject(this.value);
        out.writeObject(this.defaultValue);
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        super.readExternal(in);
        this.value = in.readObject();
        this.defaultValue = in.readObject();

    }
}
