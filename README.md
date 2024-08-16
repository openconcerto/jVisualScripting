# jVisualScripting
 Visual Scripting Engine for Java.

# Overview
This Java project provides a powerful workflow engine and a user-friendly visual editor. The workflow engine allows users to define, execute, and manage complex business processes, while the visual editor offers an intuitive interface for designing workflows without requiring deep technical knowledge.

Additionally, the engine can be embedded in your Java application or run workflows from the command line.

![Screenshot](editor.png?raw=true "Title")

### Workflow Engine
- **Dynamic Workflow Creation**: Easily define and modify workflows using Java.
- **Execution Management**: Control and monitor workflow execution with real-time updates.
- **Task Management**: Automate task assignments and manage task states.
- **Error Handling**: Robust error handling mechanisms for ensuring workflow reliability.
- **Persistence**: Store workflow definitions and states in compressed binary file or in JSON file.
- **Embeddable**: Integrate the engine into your Java application.
- **Command Line Interface**: Run workflows directly from the command line.

### Visual Editor
- **Drag-and-Drop Interface**: Design workflows with a simple drag-and-drop interface.
- **Node Configuration**: Configure each workflow node with specific actions and conditions.
- **Real-time Validation**: Immediate feedback on workflow design errors and inconsistencies.
- **Export/Import**: Export workflows as compressed binary file or JSON for easy sharing and version control.
- **Integration**: Seamlessly integrates with the workflow engine for direct deployment and testing.



![Screenshot](hello.png?raw=true "Title")

# Execution of a workflow in command line 
```
java -jar jVisualScripting-1.2.jar helloworld.jvsz
```
You can pass parameters using key=value format (use Parameter nodes to access them), for example
```
java -jar jVisualScripting-1.2.jar helloworld.jvsz name=you
```

# Embbeding jVisualScript

Just add jVisualScripting-1.2.jar in your classpath.

You can build a workflow using the EventGraph class.

EventGraph can be created programmaticaly or loaded from a file.

# Workflow creation

Workflows can be created using the editor or in pure Java (using Node and Link objects on EventGraph).

2 file formats are provided : a binary format (.jvsz) and a JSON format (.jvs)



