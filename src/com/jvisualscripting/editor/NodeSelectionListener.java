package com.jvisualscripting.editor;

import com.jvisualscripting.Lane;
import com.jvisualscripting.Node;

public interface NodeSelectionListener {

    public void nodeSelected(Node n);

    public void laneSelected(Lane lane);
}
