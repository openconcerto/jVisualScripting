package com.jvisualscripting.editor;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashSet;
import java.util.Set;

import com.jvisualscripting.Engine;
import com.jvisualscripting.EventGraph;
import com.jvisualscripting.Node;
import com.jvisualscripting.Pin;

public class CheckPoint {
    private EventGraph g;
    private Set<Pin> selectedPins;
    private Set<Node> selectedNodes;
    private byte[] bytes;

    public CheckPoint(EventGraph g, Set<Pin> selectedPins, Set<Node> selectedNodes) {
        this.g = g;
        this.selectedPins = selectedPins;
        this.selectedNodes = selectedNodes;
    }

    public CheckPoint(byte[] bytes2) {
        this.bytes = bytes2;
    }

    void park(Engine engine) throws IOException {
        if (this.bytes != null) {
            throw new IOException("already parked");
        }
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        final ObjectOutputStream objOut = new ObjectOutputStream(out);
        this.g.writeExternal(engine, objOut);
        objOut.writeInt(this.selectedPins.size());
        for (Pin p : this.selectedPins) {
            objOut.writeInt(p.getId());
        }
        objOut.writeInt(this.selectedNodes.size());
        for (Node p : this.selectedNodes) {
            objOut.writeInt(p.getId());
        }
        objOut.close();

        this.bytes = out.toByteArray();
        // Release ressources
        this.g = null;
        this.selectedPins = null;
        this.selectedNodes = null;

    }

    public void unpark(Engine engine) throws IOException {
        if (this.bytes == null) {
            throw new IllegalStateException("not parked");
        }
        try {
            final ByteArrayInputStream in = new ByteArrayInputStream(this.bytes);
            final ObjectInputStream objIn = new ObjectInputStream(in);
            this.g = new EventGraph();
            this.g.readExternal(engine, objIn);
            int pinCount = objIn.readInt();
            this.selectedPins = new HashSet<>();
            for (int i = 0; i < pinCount; i++) {
                int id = objIn.readInt();
                Pin pin = this.g.getPinFromId(id);
                if (pin == null) {
                    throw new IOException("cannot find pin from id : " + id);
                }
                this.selectedPins.add(pin);
            }
            int nodeCount = objIn.readInt();
            this.selectedNodes = new HashSet<>();
            for (int i = 0; i < nodeCount; i++) {
                int id = objIn.readInt();
                Node node = this.g.getNodeFromId(id);
                if (node == null) {
                    throw new IOException("cannot find node from id : " + id);
                }
                this.selectedNodes.add(node);
            }
            objIn.close();
        } catch (Exception e) {
            throw new IOException(e);
        }
        //
        this.bytes = null;

    }

    public byte[] getBytes() {
        return this.bytes;
    }

    public EventGraph getGraph() {
        return this.g;
    }

    public Set<Node> getSelectedNodes() {
        return this.selectedNodes;
    }

    public Set<Pin> getSelectedPins() {
        return this.selectedPins;
    }

    @Override
    public String toString() {
        if (this.bytes != null) {
            return super.toString() + " parked :" + hash(this.bytes) + " : " + this.bytes.length + " bytes";
        }
        return super.toString();
    }

    private int hash(byte[] bytes2) {
        int h = 55;
        for (byte b : bytes2) {
            h = h * 3 + b;
        }
        return h;
    }
}
