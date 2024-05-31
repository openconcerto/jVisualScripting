package com.jvisualscripting;

import java.awt.Color;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import org.json.JSONObject;

public class Lane {
    private String name;
    private Color color;
    private short y;
    private short height;

    Lane() {
        // Serialization
    }

    public Lane(String name, Color color, short y, short height) {
        super();
        this.name = name;
        this.color = color;
        this.y = y;
        this.height = height;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Color getColor() {
        return this.color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public short getY() {
        return this.y;
    }

    public void setY(short y) {
        this.y = y;
    }

    public short getHeight() {
        return this.height;
    }

    public void setHeight(short height) {
        this.height = height;
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeUTF(this.name);
        out.writeInt(this.color.getRGB());
        out.writeShort(this.y);
        out.writeShort(this.height);
    }

    public void readExternal(ObjectInput in) throws IOException {
        this.name = in.readUTF();
        this.color = new Color(in.readInt());
        this.y = in.readShort();
        this.height = in.readShort();
    }

    public JSONObject exportGraphAndState(Engine e) {
        final JSONObject o = new JSONObject();
        o.put("name", this.name);
        o.put("color", this.color);
        o.put("y", this.y);
        o.put("height", this.height);
        return o;
    }

    public void initFromJSON(JSONObject o) {
        this.name = o.getString("name");
        this.color = o.getColor("color");
        this.y = (short) o.getInt("y");
        this.height = (short) o.getInt("height");
    }

}
