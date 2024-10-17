package com.jvisualscripting;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.jvisualscripting.event.StartEventNode;
import com.jvisualscripting.filefunction.FileSaveString;
import com.jvisualscripting.filefunction.ReadLinesFromFile;
import com.jvisualscripting.filefunction.StringToFile;
import com.jvisualscripting.flowcontrol.Branch;
import com.jvisualscripting.flowcontrol.ForLoop;
import com.jvisualscripting.flowcontrol.Sequence;
import com.jvisualscripting.function.Delay;
import com.jvisualscripting.function.EndNode;
import com.jvisualscripting.function.ExternalCommand;
import com.jvisualscripting.function.Print;
import com.jvisualscripting.function.desktop.EditFile;
import com.jvisualscripting.function.desktop.Mail;
import com.jvisualscripting.function.desktop.OpenFile;
import com.jvisualscripting.function.desktop.PrintFile;
import com.jvisualscripting.function.desktop.ShowMessage;
import com.jvisualscripting.function.network.HttpGet;
import com.jvisualscripting.function.ollama.OllamaQuery;
import com.jvisualscripting.variable.BooleanAnd;
import com.jvisualscripting.variable.BooleanFalseVariable;
import com.jvisualscripting.variable.BooleanNot;
import com.jvisualscripting.variable.BooleanOr;
import com.jvisualscripting.variable.BooleanPin;
import com.jvisualscripting.variable.BooleanTrueVariable;
import com.jvisualscripting.variable.CurrentDate;
import com.jvisualscripting.variable.CurrentTime;
import com.jvisualscripting.variable.FilePin;
import com.jvisualscripting.variable.FloatComparator;
import com.jvisualscripting.variable.FloatFormatter;
import com.jvisualscripting.variable.FloatPin;
import com.jvisualscripting.variable.FloatSplitter;
import com.jvisualscripting.variable.FloatVariable;
import com.jvisualscripting.variable.IndexOf;
import com.jvisualscripting.variable.IntegerAdder;
import com.jvisualscripting.variable.IntegerComparator;
import com.jvisualscripting.variable.IntegerDivider;
import com.jvisualscripting.variable.IntegerFormatter;
import com.jvisualscripting.variable.IntegerModulus;
import com.jvisualscripting.variable.IntegerMultiplier;
import com.jvisualscripting.variable.IntegerPin;
import com.jvisualscripting.variable.IntegerSplitter;
import com.jvisualscripting.variable.IntegerSubstract;
import com.jvisualscripting.variable.IntegerVariable;
import com.jvisualscripting.variable.StringConcat;
import com.jvisualscripting.variable.StringEquals;
import com.jvisualscripting.variable.StringLength;
import com.jvisualscripting.variable.StringParameter;
import com.jvisualscripting.variable.StringPin;
import com.jvisualscripting.variable.StringSplitter;
import com.jvisualscripting.variable.StringToFloat;
import com.jvisualscripting.variable.StringVariable;

// TODO : BigDecimal

// TODO : JSOUP

public class Engine {
    public static final String DEFAULT_VERSION = "1.4";
    private static Engine defaultEngine;
    private Map<Integer, Class<? extends Pin>> mapTypePin = new HashMap<>();
    private Map<Class<? extends Pin>, Integer> mapClassPin = new HashMap<>();
    private Map<Integer, Class<? extends Node>> mapTypeNode = new HashMap<>();
    private Map<Class<? extends Node>, Integer> mapClassNode = new HashMap<>();
    private Map<Class<? extends Node>, String> mapClassNodeType = new HashMap<>();
    private Map<Class<? extends Node>, String> mapClassNodeName = new HashMap<>();
    private String id;
    private String version;

    public Engine(String id) {
        this.id = id;
        this.version = DEFAULT_VERSION;
    }

    @SuppressWarnings("unchecked")
    public void initFromJSON(JSONObject obj) throws JSONException, ClassNotFoundException {
        this.mapTypePin.clear();
        this.mapClassPin.clear();
        this.mapTypeNode.clear();
        this.mapClassNode.clear();
        this.mapClassNodeType.clear();
        this.mapClassNodeName.clear();
        this.version = obj.getString("version");
        this.id = obj.getString("id");
        JSONArray pTypes = obj.getJSONArray("pin-types");
        final int pTypesSize = pTypes.size();
        for (int i = 0; i < pTypesSize; i++) {
            JSONObject o = pTypes.getJSONObject(i);
            registerPinType(o.getInt("id"), (Class<? extends Pin>) Class.forName(o.getString("class")));
        }
        JSONArray nTypes = obj.getJSONArray("node-types");
        final int nTypesSize = pTypes.size();
        for (int i = 0; i < nTypesSize; i++) {
            JSONObject o = nTypes.getJSONObject(i);
            registerNodeType(o.getInt("id"), o.getString("type"), o.getString("name"), (Class<? extends Node>) Class.forName(o.getString("class")));
        }
    }

    public JSONObject toJSON() {
        JSONObject result = new JSONObject();
        result.put("version", this.version);
        result.put("id", this.id);
        JSONArray pTypes = new JSONArray();
        for (Entry<Integer, Class<? extends Pin>> p : this.mapTypePin.entrySet()) {
            JSONObject o = new JSONObject();
            o.put("id", p.getKey());
            o.put("class", p.getValue().getName());
            pTypes.add(o);
        }
        result.put("pin-types", pTypes);
        JSONArray nTypes = new JSONArray();
        for (Entry<Integer, Class<? extends Node>> p : this.mapTypeNode.entrySet()) {
            JSONObject o = new JSONObject();
            o.put("id", p.getKey());
            final Class<? extends Node> c = p.getValue();
            o.put("class", c.getName());
            o.put("type", this.mapClassNodeType.get(c));
            o.put("name", this.mapClassNodeName.get(c));
            pTypes.add(o);
        }
        result.put("node-types", nTypes);

        return result;
    }

    public static Engine getDefault() {
        if (defaultEngine == null) {
            defaultEngine = new Engine("default");
            // Pins
            defaultEngine.registerPinType(0, DataPin.class);
            defaultEngine.registerPinType(1, BooleanPin.class);
            defaultEngine.registerPinType(2, StringPin.class);
            defaultEngine.registerPinType(3, IntegerPin.class);
            defaultEngine.registerPinType(4, FilePin.class);
            defaultEngine.registerPinType(5, FloatPin.class);
            defaultEngine.registerPinType(100, ExecutionPin.class);
            // Node
            defaultEngine.registerNodeType(0, "Execution", "Start", StartEventNode.class);

            defaultEngine.registerNodeType(1, "Action", "Print to console", Print.class);
            defaultEngine.registerNodeType(2, "Execution", "Sequence", Sequence.class);
            defaultEngine.registerNodeType(3, "Variable", "String", StringVariable.class);
            defaultEngine.registerNodeType(4, "Execution", "Branch", Branch.class);
            defaultEngine.registerNodeType(5, "String operation", "Length", StringLength.class);
            defaultEngine.registerNodeType(6, "Integer operation", "Integer formatter", IntegerFormatter.class);
            defaultEngine.registerNodeType(7, "String operation", "String to Float", StringToFloat.class);
            defaultEngine.registerNodeType(8, "Float operation", "Formatter", FloatFormatter.class);
            defaultEngine.registerNodeType(9, "Float operation", "Comparator", FloatComparator.class);
            defaultEngine.registerNodeType(31, "Variable", "Boolean (True)", BooleanTrueVariable.class);
            defaultEngine.registerNodeType(32, "Variable", "Boolean (False)", BooleanFalseVariable.class);
            defaultEngine.registerNodeType(33, "Variable", "Integer", IntegerVariable.class);
            defaultEngine.registerNodeType(34, "Variable", "Float", FloatVariable.class);
            defaultEngine.registerNodeType(35, "String operation", "IndexOf", IndexOf.class);
            defaultEngine.registerNodeType(36, "String operation", "Splitter", StringSplitter.class);
            defaultEngine.registerNodeType(37, "Float operation", "Splitter", FloatSplitter.class);
            defaultEngine.registerNodeType(38, "Integer operation", "Splitter", IntegerSplitter.class);
            defaultEngine.registerNodeType(38, "String operation", "Concatenate", StringConcat.class);

            defaultEngine.registerNodeType(40, "Execution", "Delay", Delay.class);
            defaultEngine.registerNodeType(41, "Execution", "For Loop", ForLoop.class);
            defaultEngine.registerNodeType(42, "Time", "Current time", CurrentTime.class);
            defaultEngine.registerNodeType(43, "Time", "Current date", CurrentDate.class);
            defaultEngine.registerNodeType(44, "String operation", "Equals", StringEquals.class);

            defaultEngine.registerNodeType(45, "Integer operation", "Add", IntegerAdder.class);
            defaultEngine.registerNodeType(46, "Integer operation", "Multiply", IntegerMultiplier.class);

            defaultEngine.registerNodeType(47, "Variable", "Parameter", StringParameter.class);
            defaultEngine.registerNodeType(48, "Bool operation", "And", BooleanAnd.class);
            defaultEngine.registerNodeType(49, "Bool operation", "Or", BooleanOr.class);
            defaultEngine.registerNodeType(50, "Bool operation", "Not", BooleanNot.class);
            defaultEngine.registerNodeType(51, "Integer operation", "Substract", IntegerSubstract.class);
            defaultEngine.registerNodeType(52, "Integer operation", "Divide", IntegerDivider.class);
            defaultEngine.registerNodeType(53, "Integer operation", "Modulus", IntegerModulus.class);
            defaultEngine.registerNodeType(54, "Integer operation", "Comparator", IntegerComparator.class);
            defaultEngine.registerNodeType(100, "Execution", "External Command", ExternalCommand.class);
            defaultEngine.registerNodeType(200, "Variable", "File", StringToFile.class);
            defaultEngine.registerNodeType(201, "IO", "Save String to File", FileSaveString.class);
            defaultEngine.registerNodeType(202, "IO", "Read Lines from File", ReadLinesFromFile.class);
            defaultEngine.registerNodeType(500, "AI", "Ollama Query", OllamaQuery.class);
            defaultEngine.registerNodeType(600, "Network", "HTTP Get", HttpGet.class);
            defaultEngine.registerNodeType(700, "Desktop", "Show Message", ShowMessage.class);
            defaultEngine.registerNodeType(701, "Desktop", "Print File", PrintFile.class);
            defaultEngine.registerNodeType(702, "Desktop", "Open File", OpenFile.class);
            defaultEngine.registerNodeType(703, "Desktop", "Edit File", EditFile.class);
            defaultEngine.registerNodeType(704, "Desktop", "Mail", Mail.class);

            defaultEngine.registerNodeType(10000, "Execution", "End", EndNode.class);
        }

        return defaultEngine;
    }

    public void registerPinType(int type, Class<? extends Pin> class1) {
        this.mapTypePin.put(type, class1);
        this.mapClassPin.put(class1, type);
    }

    public Integer getTypeForPin(Class<? extends Pin> class1) {
        return this.mapClassPin.get(class1);
    }

    public Class<? extends Pin> getClassForPinType(Integer type) {
        return this.mapTypePin.get(type);
    }

    public void registerNodeType(int type, String nodeType, String name, Class<? extends Node> class1) {
        this.mapTypeNode.put(type, class1);
        this.mapClassNode.put(class1, type);
        this.mapClassNodeType.put(class1, nodeType);
        this.mapClassNodeName.put(class1, name);
    }

    public Integer getTypeForNode(Class<? extends Node> class1) {
        return this.mapClassNode.get(class1);
    }

    public Class<? extends Node> getClassForNodeType(Integer type) {
        return this.mapTypeNode.get(type);
    }

    public List<Class<? extends Node>> getRegisteredNodes() {
        return new ArrayList<>(this.mapClassNode.keySet());
    }

    public String getName(Class<? extends Node> c) {
        return this.mapClassNodeName.get(c);
    }

    public String getTypeName(Class<? extends Node> c) {
        return this.mapClassNodeType.get(c);
    }

    public String getId() {
        return this.id;
    }

    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("jVisualScripting engine (" + DEFAULT_VERSION + ")");
            System.out.println();
            System.out.println("Usage: ");
            System.out.println("  java -jar jVisualScripting-" + DEFAULT_VERSION + ".jar yourscript.jvsz");

        } else {
            String script = args[0];
            EventGraph g = new EventGraph();
            try {
                g.load(new File(script));
                for (int i = 1; i < args.length; i++) {
                    // key=value
                    String arg = args[i];
                    int index = arg.indexOf('=');
                    if (index > 1 && index < arg.length() - 1) {
                        String key = arg.substring(0, index);
                        String value = arg.substring(index + 1);
                        g.putParameter(key, value);
                    }
                }

                g.start();
            } catch (IOException e) {
                System.err.println("Error : " + e.getMessage());
            }

        }
    }
}
