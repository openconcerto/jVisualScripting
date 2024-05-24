package com.jvisualscripting;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import com.jvisualscripting.function.network.HttpGet;
import com.jvisualscripting.function.ollama.OllamaQuery;
import com.jvisualscripting.variable.BooleanFalseVariable;
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
import com.jvisualscripting.variable.IntegerFormatter;
import com.jvisualscripting.variable.IntegerMultiplier;
import com.jvisualscripting.variable.IntegerPin;
import com.jvisualscripting.variable.IntegerSplitter;
import com.jvisualscripting.variable.IntegerVariable;
import com.jvisualscripting.variable.StringEquals;
import com.jvisualscripting.variable.StringLength;
import com.jvisualscripting.variable.StringPin;
import com.jvisualscripting.variable.StringSplitter;
import com.jvisualscripting.variable.StringToFloat;
import com.jvisualscripting.variable.StringVariable;

// TODO : AND / OR / NOT / + / * (multiple in)
// TODO : % , - et division
// TODO : BigDecimal

// TODO : http GET / JSOUP

public class Engine {
    public static final String VERSION = "1.1";
    private static Engine defaultEngine;
    private Map<Integer, Class<? extends Pin>> mapTypePin = new HashMap<>();
    private Map<Class<? extends Pin>, Integer> mapClassPin = new HashMap<>();
    private Map<Integer, Class<? extends Node>> mapTypeNode = new HashMap<>();
    private Map<Class<? extends Node>, Integer> mapClassNode = new HashMap<>();
    private Map<Class<? extends Node>, String> mapClassNodeType = new HashMap<>();
    private Map<Class<? extends Node>, String> mapClassNodeName = new HashMap<>();
    private String id;

    public Engine(String id) {
        this.id = id;
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
            defaultEngine.registerNodeType(5, "String operation", "String Length", StringLength.class);
            defaultEngine.registerNodeType(6, "Data processor", "Integer formatter", IntegerFormatter.class);
            defaultEngine.registerNodeType(7, "String operation", "String to Float", StringToFloat.class);
            defaultEngine.registerNodeType(8, "Data processor", "Float formatter", FloatFormatter.class);
            defaultEngine.registerNodeType(9, "Compare", "Float comparator", FloatComparator.class);
            defaultEngine.registerNodeType(31, "Variable", "Boolean (True)", BooleanTrueVariable.class);
            defaultEngine.registerNodeType(32, "Variable", "Boolean (False)", BooleanFalseVariable.class);
            defaultEngine.registerNodeType(33, "Variable", "Integer", IntegerVariable.class);
            defaultEngine.registerNodeType(34, "Variable", "Float", FloatVariable.class);
            defaultEngine.registerNodeType(35, "String operation", "IndexOf", IndexOf.class);
            defaultEngine.registerNodeType(36, "String operation", "String Splitter", StringSplitter.class);
            defaultEngine.registerNodeType(37, "Float operation", "Float Splitter", FloatSplitter.class);
            defaultEngine.registerNodeType(38, "Integer operation", "Integer Splitter", IntegerSplitter.class);

            defaultEngine.registerNodeType(40, "Execution", "Delay", Delay.class);
            defaultEngine.registerNodeType(41, "Execution", "For Loop", ForLoop.class);
            defaultEngine.registerNodeType(42, "Time", "Current time", CurrentTime.class);
            defaultEngine.registerNodeType(43, "Time", "Current date", CurrentDate.class);
            defaultEngine.registerNodeType(44, "Data processor", "String equals", StringEquals.class);

            defaultEngine.registerNodeType(45, "Data processor", "Integer Adder", IntegerAdder.class);
            defaultEngine.registerNodeType(46, "Data processor", "Integer Multiplier", IntegerMultiplier.class);

            defaultEngine.registerNodeType(100, "Execution", "External Command", ExternalCommand.class);
            defaultEngine.registerNodeType(200, "Variable", "File", StringToFile.class);
            defaultEngine.registerNodeType(201, "IO", "Save String to File", FileSaveString.class);
            defaultEngine.registerNodeType(202, "IO", "Read Lines from File", ReadLinesFromFile.class);
            defaultEngine.registerNodeType(500, "AI", "Ollama Query", OllamaQuery.class);
            defaultEngine.registerNodeType(600, "Network", "HTTP Get", HttpGet.class);
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
            System.out.println("jVisualScripting engine (" + VERSION + ")");
            System.out.println();
            System.out.println("Usage: ");
            System.out.println("  java -jar jVisualScripting-" + VERSION + ".jar yourscript.jvsz");

        } else {
            String script = args[0];
            EventGraph g = new EventGraph();
            try {
                g.load(new File(script));
                g.start();
            } catch (ClassNotFoundException | IOException e) {
                System.err.println("Error : " + e.getMessage());
            }

        }
    }
}
