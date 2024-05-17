package com.jvisualscripting.editor;

import java.util.ArrayList;
import java.util.List;

import javax.swing.table.DefaultTableModel;

import com.jvisualscripting.Engine;
import com.jvisualscripting.Node;

public final class NodeTableModel extends DefaultTableModel {
    private List<Class<? extends Node>> nodes = new ArrayList<>();
    private final Engine engine;

    public NodeTableModel(List<Class<? extends Node>> nodes, Engine engine) {
        this.nodes = nodes;
        this.engine = engine;
    }

    @Override
    public Object getValueAt(int row, int column) {

        Class<? extends Node> n = this.nodes.get(row);
        if (column == 0) {
            return this.engine.getTypeName(n);
        }
        return this.engine.getName(n);
    }

    @Override
    public boolean isCellEditable(int row, int column) {
        return false;
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return String.class;
    }

    @Override
    public int getColumnCount() {
        return 2;
    }

    @Override
    public int getRowCount() {
        if (this.nodes == null) {
            return 0;
        }
        return this.nodes.size();
    }

    @Override
    public String getColumnName(int column) {
        if (column == 0) {
            return "Type";
        }
        return "Name";
    }

}