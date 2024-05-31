package com.jvisualscripting.editor;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.ExecutionException;
import java.util.prefs.Preferences;

import javax.accessibility.AccessibleContext;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.filechooser.FileNameExtensionFilter;

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

// TODO : Step

public class VisualScritingEditorPanel extends JPanel {

    private final JButton clearButton = new JButton("Clear console");

    private final JButton saveButton = new JButton("Save");

    private final EventGraphEditorPanel editor = new EventGraphEditorPanel(new EventGraph());
    private File loadedFile;

    private SidePanel sidePanel;

    public VisualScritingEditorPanel(final Engine engine) {
        this.setLayout(new BorderLayout());
        JPanel tools = new JPanel();
        tools.setLayout(new FlowLayout(FlowLayout.LEFT));
        JButton playButton = new JButton("Start");

        tools.add(playButton);
        JButton stopButton = new JButton("Stop");
        stopButton.setEnabled(false);
        tools.add(stopButton);
        tools.add(this.clearButton);

        this.saveButton.setEnabled(false);
        tools.add(this.saveButton);

        this.add(tools, BorderLayout.NORTH);

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
        JTextArea area = new JTextArea(1, 20);

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT);

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
        this.sidePanel = new SidePanel(this.editor, new NodeListPanel(Engine.getDefault()));
        split2.setRightComponent(this.sidePanel);
        split2.setDividerLocation(900);
        split.setLeftComponent(split2);
        split.setRightComponent(new JScrollPane(area));
        split.setDividerLocation(730);
        this.add(split, BorderLayout.CENTER);

        final AdjustmentListener l = new AdjustmentListener() {

            @Override
            public void adjustmentValueChanged(AdjustmentEvent e) {
                VisualScritingEditorPanel.this.sidePanel.setRect(scroll.getHorizontalScrollBar().getValue(), scroll.getVerticalScrollBar().getValue(), scroll.getVisibleRect().width,
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
                        VisualScritingEditorPanel.this.sidePanel.setRect(scroll.getHorizontalScrollBar().getValue(), scroll.getVerticalScrollBar().getValue(), scroll.getVisibleRect().width,
                                scroll.getVisibleRect().height);

                    }
                });

            }
        });
        System.setOut(new PrintStream(new JTextAreaOutputStream(area)));

        //
        playButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                StartEventNode firstStartEvent = VisualScritingEditorPanel.this.editor.getGraph().getFirstStartEvent();
                if (firstStartEvent == null) {
                    System.out.print("No start event found");
                    VisualScritingEditorPanel.this.editor.getGraph().dump(System.out);
                    JOptionPane.showMessageDialog(VisualScritingEditorPanel.this, "Error : no start event found");
                    return;
                }

                playButton.setEnabled(false);
                stopButton.setEnabled(true);
                VisualScritingEditorPanel.this.editor.getGraph().clearState();

                SwingWorker<Boolean, Boolean> o = new SwingWorker<Boolean, Boolean>() {

                    @Override
                    protected Boolean doInBackground() throws Exception {
                        StartEventNode firstStartEvent = VisualScritingEditorPanel.this.editor.getGraph().getFirstStartEvent();

                        return firstStartEvent.execute();

                    }

                    @Override
                    protected void done() {
                        try {
                            Boolean r = get();
                            if (!r) {
                                JOptionPane.showMessageDialog(VisualScritingEditorPanel.this, "Error during execution");
                            }
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        } catch (ExecutionException e) {
                            e.printStackTrace();
                        }
                        playButton.setEnabled(true);
                        stopButton.setEnabled(false);
                        if (VisualScritingEditorPanel.this.editor.getGraph().isEnded()) {
                            JOptionPane.showMessageDialog(VisualScritingEditorPanel.this, "End reached");
                        } else {
                            JOptionPane.showMessageDialog(VisualScritingEditorPanel.this, "Error : end not reached");
                        }
                    }
                };
                o.execute();
            }
        });
        stopButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
            }

        });

        this.saveButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    save();
                } catch (Exception e1) {
                    e1.printStackTrace();
                    JOptionPane.showMessageDialog(VisualScritingEditorPanel.this, "Erreur lors de l'enregistrement.\n" + e1.getMessage());
                }
            }
        });

        this.clearButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                area.setText("");

            }
        });

        this.editor.addNodeSelectionListener(new NodeSelectionListener() {

            @Override
            public void nodeSelected(Node n) {
                if (n == null) {
                    VisualScritingEditorPanel.this.sidePanel.showOnlyNodeList();
                } else {
                    Class<? extends NodeEditor> c = VisualScritingEditorPanel.this.editor.getEditor(n.getClass());
                    if (c != null) {
                        NodeEditor e;
                        try {
                            e = c.getConstructor().newInstance();
                            JPanel p = e.createEditor(VisualScritingEditorPanel.this.editor, n);
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
                            VisualScritingEditorPanel.this.sidePanel.showEditor("Selected Node", rPanel);

                        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e1) {
                            e1.printStackTrace();
                        }

                    } else {
                        VisualScritingEditorPanel.this.sidePanel.showOnlyNodeList();
                    }

                }

            }

            private void setRightComponent(JComponent c) {
                VisualScritingEditorPanel.this.sidePanel.showEditor("Selected Lane", c);

            }

            @Override
            public void laneSelected(Lane lane) {
                setRightComponent(new LaneEditor(lane, VisualScritingEditorPanel.this.editor));
            }
        });
        this.editor.addGraphChangeListener(new GraphChangeListener() {

            @Override
            public void graphChanged() {
                if (!VisualScritingEditorPanel.this.saveButton.isEnabled()) {
                    VisualScritingEditorPanel.this.saveButton.setEnabled(true);
                }
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

    protected void load(File file) throws Exception {

        EventGraph g = new EventGraph();
        if (file != null) {
            g.load(file);
        }

        this.loadedFile = file;

        // Update title
        final JFrame topFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
        if (this.loadedFile == null) {
            topFrame.setTitle("jVisualScripting Editor");
        } else {
            topFrame.setTitle(this.loadedFile.getName() + " - jVisualScripting Editor");
        }

        this.editor.setGraph(g);
        this.saveButton.setEnabled(false);

        this.editor.getGraph().addListener(new EventGraphListener() {

            @Override
            public void nodeActivated(Node node) {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        VisualScritingEditorPanel.this.editor.revalidate();
                        VisualScritingEditorPanel.this.editor.repaint();
                    }
                });

            }

            @Override
            public void nodeDesactivated(Node node) {

                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        VisualScritingEditorPanel.this.editor.revalidate();
                        VisualScritingEditorPanel.this.editor.repaint();
                    }
                });

            }
        });
        System.gc();
    }

    public void saveAs() throws IOException {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Specify a file to save");
        if (VisualScritingEditorPanel.this.loadedFile != null) {
            fileChooser.setCurrentDirectory(VisualScritingEditorPanel.this.loadedFile.getParentFile());
        } else {
            fileChooser.setCurrentDirectory(new File(System.getProperty("user.home")));
        }
        if (this.loadedFile != null) {
            fileChooser.setSelectedFile(new File(this.loadedFile.getName()));
        } else {
            fileChooser.setSelectedFile(new File("New Workflow.jvsz"));
        }
        fileChooser.setAcceptAllFileFilterUsed(false);
        fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("jVisualScript (.jvsz)", "jvsz"));
        fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("jVisualScript JSON (.jvs)", "jvsz"));
        int userSelection = fileChooser.showSaveDialog(this);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            if (fileToSave.exists()) {
                int answer = JOptionPane.showConfirmDialog(fileChooser, fileToSave.getName() + " already exists.\n Do you want to replace it?", "Confirm save", JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE);
                if (answer != JOptionPane.YES_OPTION) {
                    return;
                }
            }
            save(fileToSave);
            Preferences.userRoot().put("lastPath", fileToSave.getAbsolutePath());
            this.loadedFile = fileToSave;
        }

    }

    public void save() throws IOException {
        if (this.loadedFile == null) {
            saveAs();
        } else {
            save(this.loadedFile);
        }
    }

    public void save(File f) throws IOException {
        this.editor.getGraph().save(f);
        this.saveButton.setEnabled(false);
    }

    public void load() {
        try {
            JFileChooser fileChooser = new JFileChooser();
            if (VisualScritingEditorPanel.this.loadedFile != null) {
                fileChooser.setCurrentDirectory(VisualScritingEditorPanel.this.loadedFile.getParentFile());
            } else {
                String str = Preferences.userRoot().get("lastPath", System.getProperty("user.home"));
                if (str != null && new File(str).exists()) {
                    fileChooser.setCurrentDirectory(new File(str));
                }
            }
            fileChooser.setAcceptAllFileFilterUsed(false);
            fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("jVisualScript (.jvsz or .jvs)", "jvsz", "jvs"));

            int result = fileChooser.showOpenDialog(VisualScritingEditorPanel.this);
            if (result == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile();
                System.out.println("Selected file: " + selectedFile.getAbsolutePath());
                load(selectedFile);
                Preferences.userRoot().put("lastPath", selectedFile.getAbsolutePath());

            }

        } catch (Exception e1) {
            e1.printStackTrace();
            JOptionPane.showMessageDialog(VisualScritingEditorPanel.this, "Erreur lors du chargement.\n" + e1.getMessage());
        }
    }

}
