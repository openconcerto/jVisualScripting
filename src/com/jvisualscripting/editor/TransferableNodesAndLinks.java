package com.jvisualscripting.editor;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.jvisualscripting.Engine;
import com.jvisualscripting.Link;
import com.jvisualscripting.Node;
import com.jvisualscripting.Pin;

public class TransferableNodesAndLinks implements Transferable, Serializable, Externalizable {

    private static final long serialVersionUID = -3257245011954729496L;

    public static final DataFlavor FLAVOR = new DataFlavor(TransferableNodesAndLinks.class, "nodes");

    private List<Node> nodes;
    private List<Link> links;
    private final Engine engine;

    public TransferableNodesAndLinks(Engine engine, List<Node> nodes, List<Link> links) {
        this.engine = engine;
        this.nodes = nodes;
        this.links = links;

    }

    public List<Node> getNodes() {
        return this.nodes;
    }

    public List<Link> getLinks() {
        return this.links;
    }

    @Override
    public DataFlavor[] getTransferDataFlavors() {
        final DataFlavor[] r = new DataFlavor[1];
        r[0] = FLAVOR;
        return r;
    }

    @Override
    public boolean isDataFlavorSupported(DataFlavor flavor) {
        return FLAVOR.equals(flavor);
    }

    @Override
    public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
        System.out.println("TransferableNodes.getTransferData()" + flavor);
        if (FLAVOR.equals(flavor)) {
            return this;
        }
        return null;
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeInt(this.nodes.size());

        for (Node n : this.nodes) {
            out.writeInt(engine.getTypeForNode(n.getClass()));
            n.writeExternal(engine, out);
        }
        out.writeByte(this.links.size());
        for (Link l : this.links) {
            out.writeInt(l.getFrom().getId());
            out.writeInt(l.getTo().getId());
        }

    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        final int size = in.readInt();

        this.nodes = new ArrayList<>(size);

        Map<Integer, Pin> pinMap = new HashMap<>();
        for (int i = 0; i < size; i++) {
            final int type = in.readInt();
            Class<? extends Node> c = engine.getClassForNodeType(type);
            try {
                Constructor<?> ctor = c.getDeclaredConstructor();
                final Node n = (Node) ctor.newInstance();
                n.readExternal(engine, in);
                if (n.getInputSize() > 0) {
                    for (Pin p : n.getInputs()) {
                        pinMap.put(p.getId(), p);
                        p.setNode(n);
                    }
                }
                if (n.getOutputSize() > 0) {
                    for (Pin p : n.getOutputs()) {
                        pinMap.put(p.getId(), p);
                        p.setNode(n);
                    }
                }

                this.nodes.add(n);
            } catch (Exception ex) {
                throw new IOException("cannot create node : " + i + ":" + c, ex);
            }
        }
        int linkCount = in.readByte();
        this.links = new ArrayList<>(linkCount);

        for (int i = 0; i < linkCount; i++) {
            int pinId = in.readInt();
            Pin from = pinMap.get(pinId);
            pinId = in.readInt();
            if (from == null) {
                System.err.println("cannot find pin (from)" + pinId);
                continue;
            }
            Pin to = pinMap.get(pinId);
            if (to == null) {
                System.err.println("cannot find pin (to) " + pinId);
                continue;
            }
            Link link = new Link(from, to);
            this.links.add(link);

        }

    }

    @Override
    public String toString() {
        return super.toString() + " " + this.nodes + " nodes, " + this.links + " links";
    }
}
