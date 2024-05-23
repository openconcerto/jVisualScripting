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
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
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
import javax.swing.UIManager;
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
import com.jvisualscripting.editor.editors.PrintEditor;
import com.jvisualscripting.editor.editors.SequenceEditor;
import com.jvisualscripting.editor.editors.StringSplitterEditor;
import com.jvisualscripting.editor.editors.StringVariableEditor;
import com.jvisualscripting.event.StartEventNode;
import com.jvisualscripting.filefunction.FileSaveString;
import com.jvisualscripting.flowcontrol.Sequence;
import com.jvisualscripting.function.ExternalCommand;
import com.jvisualscripting.function.Print;
import com.jvisualscripting.variable.FloatSplitter;
import com.jvisualscripting.variable.FloatVariable;
import com.jvisualscripting.variable.IntegerAdder;
import com.jvisualscripting.variable.IntegerFormatter;
import com.jvisualscripting.variable.IntegerMultiplier;
import com.jvisualscripting.variable.IntegerSplitter;
import com.jvisualscripting.variable.IntegerVariable;
import com.jvisualscripting.variable.StringLength;
import com.jvisualscripting.variable.StringSplitter;
import com.jvisualscripting.variable.StringVariable;

// TODO : node : concat String

// TODO : node : endline String
// TODO : node : load file line/line
// TODO : JSOUP
// TODO : JMenu
// TODO : Step
// TODO : Speed of move

public class VisualScritingEditor extends JPanel {

    private final JButton clearButton = new JButton("Clear console");
    private final JButton loadButton = new JButton("Open File…");
    private final JButton saveButton = new JButton("Save");
    private final JButton saveAsButton = new JButton("Save As…");
    private final EventGraphEditorPanel editor = new EventGraphEditorPanel(new EventGraph());
    private File loadedFile;
    private Engine engine;
    private SidePanel sidePanel;

    public VisualScritingEditor(final Engine engine) {
        this.engine = engine;
        this.setLayout(new BorderLayout());
        JPanel tools = new JPanel();
        tools.setLayout(new FlowLayout(FlowLayout.LEFT));
        JButton playButton = new JButton("Start");

        tools.add(playButton);
        JButton stopButton = new JButton("Stop");
        stopButton.setEnabled(false);
        tools.add(stopButton);
        tools.add(this.clearButton);

        tools.add(this.loadButton);

        this.saveButton.setEnabled(false);
        tools.add(this.saveButton);
        this.saveAsButton.setEnabled(false);
        tools.add(this.saveAsButton);

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
                VisualScritingEditor.this.sidePanel.setRect(scroll.getHorizontalScrollBar().getValue(), scroll.getVerticalScrollBar().getValue(), scroll.getVisibleRect().width,
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
                        VisualScritingEditor.this.sidePanel.setRect(scroll.getHorizontalScrollBar().getValue(), scroll.getVerticalScrollBar().getValue(), scroll.getVisibleRect().width,
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
                StartEventNode firstStartEvent = VisualScritingEditor.this.editor.getGraph().getFirstStartEvent();
                if (firstStartEvent == null) {
                    System.out.print("No start event found");
                    VisualScritingEditor.this.editor.getGraph().dump(System.out);
                    JOptionPane.showMessageDialog(VisualScritingEditor.this, "Error : no start event found");
                    return;
                }

                playButton.setEnabled(false);
                stopButton.setEnabled(true);
                VisualScritingEditor.this.editor.getGraph().clearState();

                SwingWorker<Boolean, Boolean> o = new SwingWorker<Boolean, Boolean>() {

                    @Override
                    protected Boolean doInBackground() throws Exception {
                        StartEventNode firstStartEvent = VisualScritingEditor.this.editor.getGraph().getFirstStartEvent();

                        return firstStartEvent.execute();

                    }

                    @Override
                    protected void done() {
                        try {
                            Boolean r = get();
                            if (!r) {
                                JOptionPane.showMessageDialog(VisualScritingEditor.this, "Error during execution");
                            }
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        } catch (ExecutionException e) {
                            e.printStackTrace();
                        }
                        playButton.setEnabled(true);
                        stopButton.setEnabled(false);
                        if (VisualScritingEditor.this.editor.getGraph().isEnded()) {
                            JOptionPane.showMessageDialog(VisualScritingEditor.this, "End reached");
                        } else {
                            JOptionPane.showMessageDialog(VisualScritingEditor.this, "Error : end not reached");
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
        this.loadButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    JFileChooser fileChooser = new JFileChooser();
                    if (VisualScritingEditor.this.loadedFile != null) {
                        fileChooser.setCurrentDirectory(VisualScritingEditor.this.loadedFile.getParentFile());
                    } else {
                        String str = Preferences.userRoot().get("lastPath", System.getProperty("user.home"));
                        if (str != null && new File(str).exists()) {
                            fileChooser.setCurrentDirectory(new File(str));
                        }
                    }
                    fileChooser.setAcceptAllFileFilterUsed(false);
                    fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("jVisualScript (.jvsz or .jvs)", "jvsz", "jvs"));
                    int result = fileChooser.showOpenDialog(VisualScritingEditor.this);
                    if (result == JFileChooser.APPROVE_OPTION) {
                        File selectedFile = fileChooser.getSelectedFile();
                        System.out.println("Selected file: " + selectedFile.getAbsolutePath());
                        load(selectedFile);
                        Preferences.userRoot().put("lastPath", selectedFile.getAbsolutePath());

                    }

                } catch (Exception e1) {
                    e1.printStackTrace();
                    JOptionPane.showMessageDialog(VisualScritingEditor.this, "Erreur lors du chargement.\n" + e1.getMessage());
                }
            }
        });
        this.saveButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    save();
                } catch (Exception e1) {
                    e1.printStackTrace();
                    JOptionPane.showMessageDialog(VisualScritingEditor.this, "Erreur lors de l'enregistrement.\n" + e1.getMessage());
                }
            }
        });
        this.saveAsButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    saveAs();
                } catch (Exception e1) {
                    e1.printStackTrace();
                    JOptionPane.showMessageDialog(VisualScritingEditor.this, "Erreur lors de l'enregistrement.\n" + e1.getMessage());
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
                    VisualScritingEditor.this.sidePanel.showOnlyNodeList();
                } else {
                    Class<? extends NodeEditor> c = VisualScritingEditor.this.editor.getEditor(n.getClass());
                    if (c != null) {
                        NodeEditor e;
                        try {
                            e = c.getConstructor().newInstance();
                            JPanel p = e.createEditor(VisualScritingEditor.this.editor, n);
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
                            VisualScritingEditor.this.sidePanel.showEditor("Selected Node", rPanel);

                        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e1) {
                            // TODO Auto-generated catch block
                            e1.printStackTrace();
                        }

                    } else {
                        VisualScritingEditor.this.sidePanel.showOnlyNodeList();
                    }

                }

            }

            private void setRightComponent(JComponent c) {
                VisualScritingEditor.this.sidePanel.showEditor("Selected Lane", c);

            }

            @Override
            public void laneSelected(Lane lane) {
                setRightComponent(new LaneEditor(lane, VisualScritingEditor.this.editor));
            }
        });
        this.editor.addGraphChangeListener(new GraphChangeListener() {

            @Override
            public void graphChanged() {
                if (!VisualScritingEditor.this.saveButton.isEnabled())
                    VisualScritingEditor.this.saveButton.setEnabled(true);
                if (!VisualScritingEditor.this.saveAsButton.isEnabled())
                    VisualScritingEditor.this.saveAsButton.setEnabled(true);
            }

        });

    }

    public static void main(String[] args) throws IOException, ClassNotFoundException {
        if (false) {
            final StringVariable str1 = new StringVariable("str1");
            str1.setValue("Hello");
            str1.setLocation(100, 300);
            final StringVariable str2 = new StringVariable("str2");
            str2.setValue("World");
            str2.setLocation(100, 400);
            final StringLength length = new StringLength();
            length.setLocation(280, 400);

            IntegerFormatter intFormatter = new IntegerFormatter();
            intFormatter.setLocation(480, 400);
            final StringVariable str3 = new StringVariable("str3");
            str3.setValue("!");
            str3.setLocation(100, 500);

            final EventGraph g = new EventGraph();
            final Print print1 = new Print();
            print1.setLocation(300, 200);
            g.addLink(str1.getDataOuputPin(), print1.getInputs().get(1));

            final StartEventNode startEventNode = new StartEventNode();
            startEventNode.setLocation(100, 100);

            g.add(startEventNode);

            g.add(str1);
            g.add(str2);
            g.add(str3);
            g.add(print1);
            g.add(length);
            g.add(intFormatter);
            g.addLink(str2.getDataOuputPin(), length.getInputs().get(0));

            Sequence seq = new Sequence(2);
            g.addLink(print1.getFirstOutputExecutionPin(), seq.getFirstInputExecutionPin());
            seq.setLocation(500, 200);
            g.add(seq);

            final Print print2 = new Print();
            print2.setLocation(700, 200);
            g.addLink(seq.getOutputs().get(0), print2.getInputs().get(0));
            g.add(print2);

            final Print print3 = new Print();
            print3.setLocation(700, 400);
            g.addLink(seq.getOutputs().get(1), print3.getInputs().get(0));
            g.add(print3);

            g.addLink(length.getLengthOuputPin(), intFormatter.getIntegerInputPin());
            g.addLink(intFormatter.getDataOuputPin(), print2.getInputs().get(1));
            g.addLink(str3.getDataOuputPin(), print3.getInputs().get(1));

            startEventNode.setNext(print1);

            startEventNode.execute();

            //
            System.out.println("TestPrint.main()===============");
            g.dump(System.out);
            g.save(new File("test.graphz"));
            ByteArrayOutputStream bOut = new ByteArrayOutputStream();
            ObjectOutputStream objOut = new ObjectOutputStream(bOut);
            g.writeExternal(objOut);
            objOut.flush();
            objOut.close();
            bOut.flush();
            bOut.close();
            byte[] bytes = bOut.toByteArray();
            System.out.println("TestPrint.main()=>" + bytes.length + " bytes");
            ByteArrayInputStream bIn = new ByteArrayInputStream(bytes);
            EventGraph g1b = new EventGraph();
            g1b.readExternal(new ObjectInputStream(bIn));
            g1b.dump(System.out);
        }
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                try {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                final JFrame f = new JFrame();
                f.setTitle("jVisualScripting Editor");
                f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

                final Engine engine = Engine.getDefault();

                final VisualScritingEditor visualEditor = new VisualScritingEditor(engine);
                try {
                    if (args.length > 0) {
                        String script = args[0];
                        visualEditor.load(new File(script));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                f.setContentPane(visualEditor);
                f.setSize(1200, 900);
                f.setLocationRelativeTo(null);
                f.setVisible(true);

            }
        });
    }

    protected void load(File file) throws Exception {

        EventGraph g = new EventGraph();
        g.load(file);
        this.loadedFile = file;
        this.editor.setGraph(g);
        this.saveButton.setEnabled(true);
        this.saveAsButton.setEnabled(true);
        this.editor.getGraph().addListener(new EventGraphListener() {

            @Override
            public void nodeActivated(Node node) {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        VisualScritingEditor.this.editor.revalidate();
                        VisualScritingEditor.this.editor.repaint();
                    }
                });

            }

            @Override
            public void nodeDesactivated(Node node) {

                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        VisualScritingEditor.this.editor.revalidate();
                        VisualScritingEditor.this.editor.repaint();
                    }
                });

            }
        });
    }

    public void saveAs() throws IOException {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Specify a file to save");
        if (VisualScritingEditor.this.loadedFile != null) {
            fileChooser.setCurrentDirectory(VisualScritingEditor.this.loadedFile.getParentFile());
        } else {
            fileChooser.setCurrentDirectory(new File(System.getProperty("user.home")));
        }
        fileChooser.setAcceptAllFileFilterUsed(false);

        int userSelection = fileChooser.showSaveDialog(this);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            System.out.println("Save as file: " + fileToSave.getAbsolutePath());
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

}
