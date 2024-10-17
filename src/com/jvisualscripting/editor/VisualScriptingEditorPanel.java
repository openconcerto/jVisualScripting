package com.jvisualscripting.editor;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.InvocationTargetException;

import javax.accessibility.AccessibleContext;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.SwingUtilities;

import com.jvisualscripting.Engine;
import com.jvisualscripting.EventGraph;
import com.jvisualscripting.EventGraphListener;
import com.jvisualscripting.Lane;
import com.jvisualscripting.Node;
import com.jvisualscripting.editor.editors.ExternalCommandEditor;
import com.jvisualscripting.editor.editors.FileSaveStrinEditor;
import com.jvisualscripting.editor.editors.FloatSplitterEditor;
import com.jvisualscripting.editor.editors.FloatVariableEditor;
import com.jvisualscripting.editor.editors.IntegerAdderEditor;
import com.jvisualscripting.editor.editors.IntegerMultiplierEditor;
import com.jvisualscripting.editor.editors.IntegerSplitterEditor;
import com.jvisualscripting.editor.editors.IntegerVariableEditor;
import com.jvisualscripting.editor.editors.MultipleBooleanInputEditor;
import com.jvisualscripting.editor.editors.NameEditor;
import com.jvisualscripting.editor.editors.PrintEditor;
import com.jvisualscripting.editor.editors.SequenceEditor;
import com.jvisualscripting.editor.editors.StringConcatEditor;
import com.jvisualscripting.editor.editors.StringSplitterEditor;
import com.jvisualscripting.editor.editors.StringVariableEditor;
import com.jvisualscripting.event.StartEventNode;
import com.jvisualscripting.filefunction.FileSaveString;
import com.jvisualscripting.flowcontrol.Sequence;
import com.jvisualscripting.function.EndNode;
import com.jvisualscripting.function.ExternalCommand;
import com.jvisualscripting.function.Print;
import com.jvisualscripting.variable.BooleanAnd;
import com.jvisualscripting.variable.BooleanOr;
import com.jvisualscripting.variable.FloatSplitter;
import com.jvisualscripting.variable.FloatVariable;
import com.jvisualscripting.variable.IntegerAdder;
import com.jvisualscripting.variable.IntegerMultiplier;
import com.jvisualscripting.variable.IntegerSplitter;
import com.jvisualscripting.variable.IntegerVariable;
import com.jvisualscripting.variable.StringConcat;
import com.jvisualscripting.variable.StringParameter;
import com.jvisualscripting.variable.StringSplitter;
import com.jvisualscripting.variable.StringVariable;

// TODO : node : endline String

// TODO : JSOUP

public class VisualScriptingEditorPanel extends JPanel {

    private final EventGraphEditorPanel editor;

    private SidePanel sidePanel;
    final Engine engine;

    public VisualScriptingEditorPanel(final Engine engine) {

        this.engine = engine;
        this.setLayout(new BorderLayout());

        this.editor = new EventGraphEditorPanel(engine, new EventGraph());
        this.editor.registerNodeEditor(StringVariable.class, StringVariableEditor.class);
        this.editor.registerNodeEditor(IntegerVariable.class, IntegerVariableEditor.class);
        this.editor.registerNodeEditor(FloatVariable.class, FloatVariableEditor.class);
        this.editor.registerNodeEditor(Sequence.class, SequenceEditor.class);
        this.editor.registerNodeEditor(Print.class, PrintEditor.class);
        this.editor.registerNodeEditor(FileSaveString.class, FileSaveStrinEditor.class);
        this.editor.registerNodeEditor(ExternalCommand.class, ExternalCommandEditor.class);
        this.editor.registerNodeEditor(StringSplitter.class, StringSplitterEditor.class);
        this.editor.registerNodeEditor(IntegerSplitter.class, IntegerSplitterEditor.class);
        this.editor.registerNodeEditor(FloatSplitter.class, FloatSplitterEditor.class);
        this.editor.registerNodeEditor(IntegerAdder.class, IntegerAdderEditor.class);
        this.editor.registerNodeEditor(IntegerMultiplier.class, IntegerMultiplierEditor.class);
        this.editor.registerNodeEditor(StringConcat.class, StringConcatEditor.class);
        this.editor.registerNodeEditor(StringParameter.class, NameEditor.class);

        this.editor.registerNodeEditor(BooleanAnd.class, MultipleBooleanInputEditor.class);
        this.editor.registerNodeEditor(BooleanOr.class, MultipleBooleanInputEditor.class);

        JSplitPane split2 = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT) {
            @Override
            public AccessibleContext getAccessibleContext() {
                return new AccessibleJSplitPane() {
                    @Override
                    public Number getMinimumAccessibleValue() {
                        return 50;
                    }

                };
            }

        };
        final JScrollPane scroll = new JScrollPane(this.editor);
        split2.setLeftComponent(scroll);
        this.sidePanel = new SidePanel(this.editor, new NodeListPanel(engine));
        split2.setRightComponent(this.sidePanel);
        split2.setDividerLocation(900);

        this.add(split2, BorderLayout.CENTER);

        final AdjustmentListener l = new AdjustmentListener() {

            @Override
            public void adjustmentValueChanged(AdjustmentEvent e) {
                VisualScriptingEditorPanel.this.sidePanel.setRect(scroll.getHorizontalScrollBar().getValue(), scroll.getVerticalScrollBar().getValue(), scroll.getVisibleRect().width,
                        scroll.getVisibleRect().height);
            }
        };
        scroll.getHorizontalScrollBar().addAdjustmentListener(l);
        scroll.getVerticalScrollBar().addAdjustmentListener(l);
        this.sidePanel.setRect(scroll.getHorizontalScrollBar().getValue(), scroll.getVerticalScrollBar().getValue(), scroll.getVisibleRect().width, scroll.getVisibleRect().height);
        split2.addPropertyChangeListener(new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                SwingUtilities.invokeLater(new Runnable() {

                    @Override
                    public void run() {
                        // invokeLater is required here
                        VisualScriptingEditorPanel.this.sidePanel.setRect(scroll.getHorizontalScrollBar().getValue(), scroll.getVerticalScrollBar().getValue(), scroll.getVisibleRect().width,
                                scroll.getVisibleRect().height);

                    }
                });

            }
        });

        this.editor.addNodeSelectionListener(new NodeSelectionListener() {

            @Override
            public void nodeSelected(Node n) {
                if (n == null) {
                    VisualScriptingEditorPanel.this.sidePanel.showOnlyNodeList();
                } else {
                    Class<? extends NodeEditor> c = VisualScriptingEditorPanel.this.editor.getEditor(n.getClass());
                    if (c != null) {
                        NodeEditor e;
                        try {
                            e = c.getConstructor().newInstance();
                            JPanel p = e.createEditor(VisualScriptingEditorPanel.this.editor, n);
                            JPanel rPanel = new JPanel();
                            rPanel.setLayout(new GridBagLayout());

                            GridBagConstraints constraints = new GridBagConstraints();
                            constraints.insets = new Insets(4, 4, 4, 4);
                            constraints.gridx = 0;
                            constraints.gridy = 0;
                            constraints.weightx = 1;
                            constraints.fill = GridBagConstraints.HORIZONTAL;

                            final JLabel l = new JLabel(e.getName());
                            l.setFont(l.getFont().deriveFont(Font.BOLD));
                            rPanel.add(l, constraints);

                            constraints.insets = new Insets(2, 0, 2, 0);
                            constraints.gridy++;
                            constraints.anchor = GridBagConstraints.NORTHWEST;

                            rPanel.add(p, constraints);
                            JPanel spacer = new JPanel();
                            spacer.setOpaque(false);
                            constraints.gridy++;
                            constraints.weighty++;
                            rPanel.add(spacer, constraints);
                            VisualScriptingEditorPanel.this.sidePanel.showEditor("Selected Node", rPanel);

                        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e1) {
                            e1.printStackTrace();
                        }

                    } else {
                        VisualScriptingEditorPanel.this.sidePanel.showOnlyNodeList();
                    }

                }

            }

            private void setRightComponent(JComponent c) {
                VisualScriptingEditorPanel.this.sidePanel.showEditor("Selected Lane", c);

            }

            @Override
            public void laneSelected(Lane lane) {
                setRightComponent(new LaneEditor(lane, VisualScriptingEditorPanel.this.editor));
            }
        });

    }

    public void addStartAndEndNodes() {
        final EventGraph graph = this.getEditor().getGraph();
        final StartEventNode start = new StartEventNode();
        graph.add(start);
        final EndNode end = new EndNode();
        graph.add(end);

        this.getEditor().addVNode(start, 20, 20);
        this.getEditor().addVNode(end, 700, 20);
        this.getEditor().fireGraphChange();
    }

    public EventGraphEditorPanel getEditor() {
        return this.editor;
    }

    public void setGraph(EventGraph g) {
        this.editor.setGraph(g);

        this.editor.getGraph().addListener(new EventGraphListener() {

            @Override
            public void nodeActivated(Node node) {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        VisualScriptingEditorPanel.this.editor.revalidate();
                        VisualScriptingEditorPanel.this.editor.repaint();
                    }
                });

            }

            @Override
            public void nodeDesactivated(Node node) {

                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        VisualScriptingEditorPanel.this.editor.revalidate();
                        VisualScriptingEditorPanel.this.editor.repaint();
                    }
                });

            }
        });
    }

    public EventGraph getGraph() {
        return this.editor.getGraph();
    }

}
