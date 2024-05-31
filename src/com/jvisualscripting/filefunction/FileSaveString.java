package com.jvisualscripting.filefunction;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import org.json.JSONObject;

import com.jvisualscripting.DataPin;
import com.jvisualscripting.Engine;
import com.jvisualscripting.FlowNode;
import com.jvisualscripting.Node;
import com.jvisualscripting.Pin.PinMode;
import com.jvisualscripting.variable.FilePin;
import com.jvisualscripting.variable.StringPin;

public class FileSaveString extends FlowNode {
    private boolean appendMode = true;
    private boolean ioError = false;

    public FileSaveString() {
        super("FileSaveString");
        this.inputs.add(new FilePin(this, "File", PinMode.INPUT));
        this.inputs.add(new StringPin(this, "In String", PinMode.INPUT));
    }

    @Override
    public boolean run() {

        final String inputValue = getString();
        if (inputValue == null)
            return false;

        this.ioError = false;
        try (OutputStream os = new BufferedOutputStream(new FileOutputStream(getFile(), isAppendMode()))) {
            os.write(inputValue.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            this.ioError = true;
            e.printStackTrace();
        }
        return !this.ioError;
    }

    @Override
    public boolean canBeExecuted() {
        final DataPin p1 = (DataPin) this.inputs.get(1);
        final DataPin p2 = (DataPin) this.inputs.get(2);
        return p1.isConnected() && p2.isConnected();
    }

    private String getString() {
        StringPin dataPin = (StringPin) this.inputs.get(2);
        if (dataPin.isConnected()) {
            DataPin oPin = (DataPin) dataPin.getConnectedPin();
            Node previousNode = oPin.getNode();
            Object value = previousNode.getOuputValue(oPin);
            return value.toString();
        }
        return null;
    }

    private File getFile() {
        FilePin dataPin = (FilePin) this.inputs.get(1);
        if (dataPin.isConnected()) {
            DataPin oPin = (DataPin) dataPin.getConnectedPin();
            Node previousNode = oPin.getNode();
            return (File) previousNode.getOuputValue(oPin);
        }
        return null;
    }

    public void setAppendMode(boolean append) {
        this.appendMode = append;
    }

    public boolean isAppendMode() {
        return this.appendMode;
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        super.readExternal(in);
        this.setAppendMode(in.readBoolean());
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        super.writeExternal(out);
        out.writeBoolean(this.appendMode);
    }

    @Override
    public JSONObject exportGraphAndState(Engine e) {
        JSONObject obj = super.exportGraphAndState(e);
        obj.put("appendMode", this.appendMode);
        return obj;
    }

    @Override
    public void initFromJSON(JSONObject obj, Engine e) throws IOException {
        super.initFromJSON(obj, e);
        setAppendMode(obj.getBoolean("appendMode"));
    }

    @Override
    public String getName() {
        if (!isAppendMode())
            return super.getName();
        else
            return super.getName() + " (append)";
    }

    @Override
    public int getWidth() {
        int w = super.getWidth();
        if (isAppendMode())
            return w + 40;

        return w;
    }
}
