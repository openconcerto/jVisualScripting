package com.jvisualscripting;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Externalizable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

import org.json.JSONArray;
import org.json.JSONObject;

import com.jvisualscripting.event.StartEventNode;
import com.jvisualscripting.function.EndNode;

public class EventGraph implements Externalizable {

    private static final byte[] FILE_HEADER = "jVisualScripting".getBytes();
    private List<Node> nodes = new ArrayList<>();
    private List<Link> links = new ArrayList<>();
    private List<Lane> lanes = new ArrayList<>();
    private List<EventGraphListener> listeners;

    private String uuid;

    public EventGraph() {
        this.uuid = UUID.randomUUID().toString();
    }

    public void addListener(EventGraphListener l) {
        if (this.listeners == null) {
            this.listeners = new ArrayList<>(1);
        }
        this.listeners.add(l);
    }

    public void removeListener(EventGraphListener l) {
        if (this.listeners == null) {
            return;
        }
        this.listeners.remove(l);
        if (this.listeners.isEmpty()) {
            this.listeners = null;
        }
    }

    public void add(Node n) {
        this.nodes.add(n);
        n.setGrap(this);
    }

    public void add(Link link) {
        this.links.add(link);
    }

    public Link addLink(Pin from, Pin to) {
        Link link = new Link(from, to);
        this.links.add(link);
        return link;
    }

    public void add(Lane lane) {
        this.lanes.add(lane);
    }

    public void fireNodeActivated(Node node) {
        if (this.listeners != null) {
            for (EventGraphListener l : this.listeners) {
                l.nodeActivated(node);
            }
        }
    }

    public void fireNodeDesactivated(Node node) {
        if (this.listeners != null) {
            for (EventGraphListener l : this.listeners) {
                l.nodeDesactivated(node);
            }
        }
    }

    public List<Node> getNodes() {
        return this.nodes;
    }

    public List<Link> getLinks() {
        return this.links;
    }

    public List<Lane> getLanes() {
        return this.lanes;
    }

    public void remove(Node node) {
        boolean b = this.nodes.remove(node);
        if (!b) {
            throw new IllegalArgumentException(node + " is not a node of this graph");
        }
    }

    public void remove(Link link) {
        link.getFrom().setConnectedPin(null);
        link.getTo().setConnectedPin(null);
        boolean b = this.links.remove(link);
        if (!b) {
            throw new IllegalArgumentException(link + " is a link of this graph");
        }
    }

    public JSONObject exportGraphAndState(Engine e) {
        JSONObject obj = new JSONObject();
        obj.put("engine-id", e.getId());
        obj.put("uuid", this.uuid);
        JSONArray nodesList = new JSONArray();
        obj.put("nodes", nodesList);
        for (Node n : this.nodes) {
            nodesList.add(n.exportGraphAndState(e));
        }

        final JSONArray linksList = new JSONArray();
        obj.put("links", linksList);
        for (Link l : this.links) {
            final JSONObject link = new JSONObject();
            link.put("from", l.getFrom().getId());
            link.put("to", l.getTo().getId());
            linksList.add(link);
        }
        return obj;
    }

    public void initFromJSON(JSONObject obj, Engine e) throws IOException {
        String engineId = obj.optString("engine-id", "default");
        if (!engineId.equals(e.getId())) {
            throw new IllegalArgumentException("current engine (" + e.getId() + ") does not match graph engine (" + engineId + ")");
        }

        this.uuid = obj.getString("uuid");
        JSONArray nodesList = obj.getJSONArray("nodes");
        int nodeCount = nodesList.size();
        this.nodes = new ArrayList<>(nodeCount);

        Map<Integer, Pin> pinMap = new HashMap<>();
        for (int i = 0; i < nodeCount; i++) {
            JSONObject objNode = nodesList.getJSONObject(i);
            int type = objNode.getInt("type");
            Class<? extends Node> c = e.getClassForNodeType(type);
            try {
                Constructor<?> ctor = c.getDeclaredConstructor();
                ctor.setAccessible(true);
                Node n = (Node) ctor.newInstance();
                //
                n.initFromJSON(obj, e);

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
                add(n);

            } catch (Exception ex) {
                throw new IOException("cannot create node : " + i + ":" + c, ex);
            }

        }

        JSONArray linksList = obj.getJSONArray("links");
        int linkCount = linksList.size();

        this.links = new ArrayList<>(linkCount);

        for (int i = 0; i < linkCount; i++) {
            JSONObject objLink = (JSONObject) linksList.get(i);

            int pinId = objLink.getInt("from");
            Pin from = pinMap.get(pinId);
            pinId = objLink.getInt("to");
            Pin to = pinMap.get(pinId);
            if (from == null) {
                System.err.println("cannot find pin (from)" + pinId);
                continue;
            }

            if (to == null) {
                System.err.println("cannot find pin (to) " + pinId);
                continue;
            }
            addLink(from, to);

        }

    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        Engine e = Engine.getDefault();
        out.writeByte(this.nodes.size());
        for (Node n : this.nodes) {
            out.writeInt(e.getTypeForNode(n.getClass()));
            n.writeExternal(out);
        }
        out.writeByte(this.links.size());
        for (Link l : this.links) {
            out.writeInt(l.getFrom().getId());
            out.writeInt(l.getTo().getId());
        }
        out.writeByte(this.lanes.size());
        for (Lane l : this.lanes) {
            l.writeExternal(out);
        }
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        int nodeCount = in.readByte();
        this.nodes = new ArrayList<>(nodeCount);
        Engine e = Engine.getDefault();
        Map<Integer, Pin> pinMap = new HashMap<>();
        for (int i = 0; i < nodeCount; i++) {
            int type = in.readInt();
            Class<? extends Node> c = e.getClassForNodeType(type);
            try {
                Constructor<?> ctor = c.getDeclaredConstructor();
                ctor.setAccessible(true);
                Node n = (Node) ctor.newInstance();
                n.readExternal(in);
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
                add(n);

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
            Pin to = pinMap.get(pinId);
            if (from == null) {
                System.err.println("cannot find pin (from)" + pinId);
                continue;
            }

            if (to == null) {
                System.err.println("cannot find pin (to) " + pinId);
                continue;
            }
            addLink(from, to);

        }
        int laneCount = in.readByte();
        this.lanes = new ArrayList<>(laneCount);
        for (int i = 0; i < laneCount; i++) {
            Lane lane = new Lane();
            lane.readExternal(in);
        }

    }

    public void dump(PrintStream out) {
        out.println("uuid:" + this.uuid);
        out.println(this.nodes.size() + " nodes :");
        for (Node n : this.nodes) {
            out.println(n);
        }
        out.println(this.links.size() + " links :");
        for (Link l : this.links) {
            out.println(l);
        }
    }

    public Node getNodeFromId(int id) {
        for (Node n : this.nodes) {
            if (n.getId() == id) {
                return n;
            }
        }
        return null;
    }

    public Pin getPinFromId(int id) {
        for (Node n : this.nodes) {
            if (n.getInputSize() > 0) {
                for (Pin p : n.getInputs()) {
                    if (p.getId() == id) {
                        return p;
                    }
                }
            }
            if (n.getOutputSize() > 0) {
                for (Pin p : n.getOutputs()) {
                    if (p.getId() == id) {
                        return p;
                    }
                }
            }
        }
        return null;
    }

    public StartEventNode getFirstStartEvent() {
        for (Node v : this.nodes) {
            if (v instanceof StartEventNode) {
                return (StartEventNode) v;
            }
        }
        return null;
    }

    public void start() {
        StartEventNode n = getFirstStartEvent();
        if (n != null) {
            if (!n.canBeExecuted()) {
                throw new IllegalStateException("start node cannot be executed");
            }
            n.execute();
        } else {
            throw new IllegalStateException("no start node");
        }
    }

    public boolean isEnded() {
        for (Node n : this.nodes) {
            if (n instanceof EndNode) {
                final EndNode endNode = (EndNode) n;
                return endNode.isActive();
            }
        }
        return false;
    }

    public void save(File f) throws IOException {
        try (FileOutputStream fileOutputStream = new FileOutputStream(f)) {
            fileOutputStream.write(FILE_HEADER);
            DeflaterOutputStream dos = new DeflaterOutputStream(fileOutputStream);
            ObjectOutputStream o = new ObjectOutputStream(new BufferedOutputStream(dos));
            this.writeExternal(o);
            o.close();
            dos.close();
        }
    }

    public void load(File f) throws IOException, ClassNotFoundException {

        try (FileInputStream fileInputStream = new FileInputStream(f)) {
            byte[] h = new byte[FILE_HEADER.length];
            int read = fileInputStream.read(h);
            if (read != h.length || !Arrays.equals(h, FILE_HEADER)) {
                throw new IOException("unknown file type");
            }
            // Decompress file
            final InflaterInputStream iis = new InflaterInputStream(fileInputStream);
            final ByteArrayOutputStream bOut = new ByteArrayOutputStream();
            int data;
            while ((data = iis.read()) != -1) {
                bOut.write(data);
            }
            final byte[] bytes = bOut.toByteArray();
            // Deserialize
            final ObjectInputStream oIn = new ObjectInputStream(new ByteArrayInputStream(bytes));
            this.readExternal(oIn);
            oIn.close();
        }
    }

    public void clearState() {
        for (Node v : this.nodes) {
            v.clearState();
        }
    }

    public String getUUID() {
        return this.uuid;
    }

    public boolean isBlocked() {
        for (Node n : this.nodes) {
            if (n.isBlocked()) {
                return true;
            }
        }
        return false;
    }

    public void resume() {
        for (Node n : this.nodes) {
            if (n.isBlocked()) {
                n.execute();
                if (isEnded()) {
                    return;
                }
            }
        }

    }

}
