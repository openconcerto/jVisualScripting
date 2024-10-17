package com.jvisualscripting.editor;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.prefs.Preferences;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
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
import com.jvisualscripting.event.StartEventNode;

public class VisualScriptingEditorMainPanel extends JPanel {

    private final JButton clearButton = new JButton("Clear console");

    protected final JButton saveButton = new JButton("Save");

    private final VisualScriptingEditorPanel editor;
    private File loadedFile;

    final Engine engine;

    public VisualScriptingEditorMainPanel(final Engine engine) {

        this.engine = engine;
        this.setLayout(new BorderLayout());
        JPanel tools = new JPanel();
        tools.setLayout(new FlowLayout(FlowLayout.LEFT));
        JButton playButton = new JButton("Start");

        tools.add(playButton);

        tools.add(this.clearButton);

        this.saveButton.setEnabled(false);
        tools.add(this.saveButton);

        this.add(tools, BorderLayout.NORTH);
        this.editor = new VisualScriptingEditorPanel(engine);

        JTextArea area = new JTextArea(1, 20);

        System.setOut(new PrintStream(new JTextAreaOutputStream(area)));

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        split.setLeftComponent(this.editor);
        split.setRightComponent(new JScrollPane(area));
        split.setDividerLocation(730);
        this.add(split, BorderLayout.CENTER);

        //
        playButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                StartEventNode firstStartEvent = VisualScriptingEditorMainPanel.this.editor.getGraph().getFirstStartEvent();
                if (firstStartEvent == null) {
                    System.out.print("No start event found");
                    JOptionPane.showMessageDialog(VisualScriptingEditorMainPanel.this, "Error : no start event found");
                    return;
                }

                playButton.setEnabled(false);

                VisualScriptingEditorMainPanel.this.editor.getGraph().clearState();

                SwingWorker<Boolean, Boolean> o = new SwingWorker<Boolean, Boolean>() {

                    @Override
                    protected Boolean doInBackground() throws Exception {
                        StartEventNode firstStartEvent = VisualScriptingEditorMainPanel.this.editor.getGraph().getFirstStartEvent();
                        return firstStartEvent.execute();

                    }

                    @Override
                    protected void done() {
                        try {
                            Boolean r = get();
                            if (!r.booleanValue()) {
                                JOptionPane.showMessageDialog(VisualScriptingEditorMainPanel.this, "Error during execution");
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        playButton.setEnabled(true);

                        if (VisualScriptingEditorMainPanel.this.editor.getGraph().isEnded()) {
                            JOptionPane.showMessageDialog(VisualScriptingEditorMainPanel.this, "End reached");
                        } else {
                            JOptionPane.showMessageDialog(VisualScriptingEditorMainPanel.this, "Error : end not reached");
                        }
                    }
                };
                o.execute();
            }
        });

        this.saveButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    save();
                } catch (Exception e1) {
                    e1.printStackTrace();
                    JOptionPane.showMessageDialog(VisualScriptingEditorMainPanel.this, "Erreur lors de l'enregistrement.\n" + e1.getMessage());
                }
            }
        });

        this.clearButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                area.setText("");

            }
        });

        this.editor.getEditor().addGraphChangeListener(new GraphChangeListener() {

            @Override
            public void graphChanged() {
                if (!VisualScriptingEditorMainPanel.this.saveButton.isEnabled()) {
                    VisualScriptingEditorMainPanel.this.saveButton.setEnabled(true);
                }
            }

        });

    }

    public VisualScriptingEditorPanel getEditor() {
        return this.editor;
    }

    protected void load(File file) throws IOException {

        EventGraph g = new EventGraph();
        if (file != null) {
            g.load(file, this.engine);
        }

        this.loadedFile = file;

        // Update title
        final JFrame topFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
        if (this.loadedFile == null) {
            topFrame.setTitle("jVisualScripting Editor");
        } else {
            topFrame.setTitle(this.loadedFile.getName() + " - jVisualScripting Editor");
        }

        setGraph(g);
        System.gc();
    }

    public void setGraph(EventGraph g) {
        this.editor.setGraph(g);
        this.saveButton.setEnabled(false);

    }

    public void saveAs() throws IOException {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Specify a file to save");
        if (VisualScriptingEditorMainPanel.this.loadedFile != null) {
            fileChooser.setCurrentDirectory(VisualScriptingEditorMainPanel.this.loadedFile.getParentFile());
        } else {
            String str = Preferences.userRoot().get("lastPath", System.getProperty("user.home"));
            if (str != null && new File(str).exists()) {
                fileChooser.setCurrentDirectory(new File(str));
            } else {
                fileChooser.setCurrentDirectory(new File(System.getProperty("user.home")));
            }
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
                int answer = JOptionPane.showConfirmDialog(fileChooser, fileToSave.getName() + " already exists.\nDo you want to replace it?", "Confirm save", JOptionPane.YES_NO_OPTION,
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
        this.editor.getGraph().save(f, this.engine);
        this.saveButton.setEnabled(false);
    }

    public void load() {
        try {
            JFileChooser fileChooser = new JFileChooser();
            if (VisualScriptingEditorMainPanel.this.loadedFile != null) {
                fileChooser.setCurrentDirectory(VisualScriptingEditorMainPanel.this.loadedFile.getParentFile());
            } else {
                String str = Preferences.userRoot().get("lastPath", System.getProperty("user.home"));
                if (str != null && new File(str).exists()) {
                    fileChooser.setCurrentDirectory(new File(str));
                }
            }
            fileChooser.setAcceptAllFileFilterUsed(false);
            fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("jVisualScript (.jvsz or .jvs)", "jvsz", "jvs"));

            int result = fileChooser.showOpenDialog(VisualScriptingEditorMainPanel.this);
            if (result == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile();
                System.out.println("Selected file: " + selectedFile.getAbsolutePath());
                load(selectedFile);
                Preferences.userRoot().put("lastPath", selectedFile.getAbsolutePath());

            }

        } catch (Exception e1) {
            e1.printStackTrace();
            JOptionPane.showMessageDialog(VisualScriptingEditorMainPanel.this, "Erreur lors du chargement.\n" + e1.getMessage());
        }
    }

}
