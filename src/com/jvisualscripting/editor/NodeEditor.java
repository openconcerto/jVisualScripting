package com.jvisualscripting.editor;

import javax.swing.JPanel;

import com.jvisualscripting.Node;

public interface NodeEditor {

    public JPanel createEditor(EventGraphEditorPanel panel, Node n);

    public String getName();

}
