package com.jvisualscripting;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
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
import java.util.Map.Entry;
import java.util.UUID;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

import org.json.JSONArray;
import org.json.JSONObject;

import com.jvisualscripting.event.StartEventNode;
import com.jvisualscripting.function.EndNode;

/**
 * Represents an event graph that consists of nodes, links, and lanes. It supports adding, removing,
 * and exporting the state of nodes and links. The graph can be serialized and deserialized for
 * persistent storage in JSON or on binary format.
 */
public class EventGraph {

    private static final byte[] FILE_HEADER = "jVisualScripting".getBytes();
    private List<Node> nodes = new ArrayList<>();
    private List<Link> links = new ArrayList<>();
    private List<Lane> lanes = new ArrayList<>();
    private List<EventGraphListener> listeners;
    private Map<String, String> parameters = new HashMap<>();

    private String uuid;

    /**
     * Creates a new EventGraph with a unique UUID.
     */
    public EventGraph() {
        this.uuid = UUID.randomUUID().toString();
    }

    public EventGraph(String uuid) {
        this.uuid = uuid;
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

    public void putParameter(String key, String value) {
        this.parameters.put(key, value);
    }

    public String getParameter(String key) {
        return this.parameters.get(key);
    }

    public void add(Node n) {
        this.nodes.add(n);
        n.setGrap(this);
    }

    public void add(Link link) {
        this.links.add(link);
    }

    /**
     * Creates and adds a link between two pins.
     * 
     * The nodes of the pins are automatically added to the graph
     *
     * @param from the source pin
     * @param to the destination pin
     * @return the created link
     */
    public Link addLink(Pin from, Pin to) {
        if (!this.nodes.contains(from.getNode())) {
            this.add(from.getNode());
        }
        if (!this.nodes.contains(to.getNode())) {
            this.add(to.getNode());
        }

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

        final List<Link> linksToremove = new ArrayList<>(1);
        for (Link link : this.links) {
            if (link.getFrom().getNode().equals(node) || link.getTo().getNode().equals(node)) {
                linksToremove.add(link);
            }
        }
        for (Link link : linksToremove) {
            remove(link);
        }

    }

    public void remove(Link link) {
        final Pin fromPin = link.getFrom();
        final Pin toPin = link.getTo();
        fromPin.removeConnectedPin(toPin);
        toPin.removeConnectedPins();
        boolean b = this.links.remove(link);
        if (!b) {
            throw new IllegalArgumentException(link + " is not a link of this graph");
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

        if (!this.lanes.isEmpty()) {
            final JSONArray lanesList = new JSONArray();
            obj.put("lanes", lanesList);
            for (Lane lane : this.lanes) {
                nodesList.add(lane.exportGraphAndState(e));
            }
        }

        if (!this.parameters.isEmpty()) {
            final JSONObject parametersObj = new JSONObject();
            obj.put("parameters", parametersObj);
            for (Entry<String, String> entry : this.parameters.entrySet()) {
                parametersObj.put(entry.getKey(), entry.getValue());
            }
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

                Node n = (Node) ctor.newInstance();
                //
                n.initFromJSON(objNode, e);

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

        if (obj.has("lanes")) {
            final JSONArray lanesList = obj.getJSONArray("lanes");
            this.lanes = new ArrayList<>(lanesList.size());
            for (Object o : lanesList) {
                final Lane lane = new Lane();
                lane.initFromJSON((JSONObject) o);
                this.lanes.add(lane);
            }
        }

        if (obj.has("parameters")) {
            final JSONObject oParameters = obj.getJSONObject("parameters");
            int parameterCount = oParameters.length();
            this.parameters = new HashMap<>(parameterCount);
            for (String key : oParameters.keySet()) {
                this.parameters.put(key, oParameters.getString(key));
            }
        }
    }

    public void writeExternal(Engine e, ObjectOutput out) throws IOException {

        out.writeInt(this.nodes.size());
        for (Node n : this.nodes) {
            out.writeInt(e.getTypeForNode(n.getClass()));
            n.writeExternal(e, out);
        }
        out.writeInt(this.links.size());
        for (Link l : this.links) {
            out.writeInt(l.getFrom().getId());
            out.writeInt(l.getTo().getId());
        }
        out.writeInt(this.lanes.size());
        for (Lane l : this.lanes) {
            l.writeExternal(out);
        }
        out.writeInt(this.parameters.size());
        for (Entry<String, String> entry : this.parameters.entrySet()) {
            out.writeUTF(entry.getKey());
            out.writeUTF(entry.getValue());
        }

    }

    public void readExternal(Engine e, ObjectInput in) throws IOException, ClassNotFoundException {
        int nodeCount = in.readInt();
        this.nodes = new ArrayList<>(nodeCount);

        Map<Integer, Pin> pinMap = new HashMap<>();
        for (int i = 0; i < nodeCount; i++) {
            int type = in.readInt();
            Class<? extends Node> c = e.getClassForNodeType(type);
            try {
                Constructor<?> ctor = c.getDeclaredConstructor();

                Node n = (Node) ctor.newInstance();
                n.readExternal(e, in);
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
        int linkCount = in.readInt();
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
        int laneCount = in.readInt();
        this.lanes = new ArrayList<>(laneCount);
        for (int i = 0; i < laneCount; i++) {
            Lane lane = new Lane();
            lane.readExternal(in);
            this.lanes.add(lane);
        }
        int parameterCount = in.readInt();
        this.parameters = new HashMap<>(parameterCount);
        for (int i = 0; i < parameterCount; i++) {
            this.parameters.put(in.readUTF(), in.readUTF());
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
                if (endNode.isActive()) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Save in compressed binary format the graph
     * 
     * @param f the file to save into
     * @throws IOException on output error
     */
    public void save(File f) throws IOException {
        save(f, Engine.getDefault());
    }

    public void save(File f, Engine e) throws IOException {
        try (FileOutputStream fileOutputStream = new FileOutputStream(f)) {
            fileOutputStream.write(FILE_HEADER);
            DeflaterOutputStream dos = new DeflaterOutputStream(fileOutputStream);
            ObjectOutputStream o = new ObjectOutputStream(new BufferedOutputStream(dos));
            this.writeExternal(e, o);
            o.close();
            dos.close();
        }
    }

    public void load(File f) throws IOException {
        load(f, Engine.getDefault());
    }

    public void load(File f, Engine e) throws IOException {

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
            this.readExternal(e, oIn);
            oIn.close();
        } catch (ClassNotFoundException e1) {
            throw new IOException(e1);
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
        return getFirstBlockedNode() != null;
    }

    public Node getFirstBlockedNode() {
        for (Node n : this.nodes) {
            if (n.isBlocked()) {
                return n;
            }
        }
        return null;
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
