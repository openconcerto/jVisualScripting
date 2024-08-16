package com.jvisualscripting.editor;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.io.File;
import java.io.IOException;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import com.jvisualscripting.Engine;

public class VisualScritingEditor extends JFrame {

    VisualScritingEditorMainPanel visualEditorPanel;

    public VisualScritingEditor(Engine engine) {
        setTitle("jVisualScripting Editor");
        this.visualEditorPanel = new VisualScritingEditorMainPanel(engine);
        setContentPane(this.visualEditorPanel);
        JMenuBar menubar = createJMenuBar();
        setJMenuBar(menubar);
    }

    public void addStartAndEndNodes() {
        this.visualEditorPanel.getEditor().addStartAndEndNodes();

    }

    public void load(File file) throws Exception {
        this.visualEditorPanel.load(file);

    }

    public VisualScritingEditorMainPanel getVisualEditorPanel() {
        return this.visualEditorPanel;
    }

    public JMenuBar createJMenuBar() {
        // Menu
        JMenuBar menubar = new JMenuBar();

        final JMenu mFile = new JMenu("File");

        final JMenuItem newItem = new JMenuItem("New");
        newItem.setAccelerator(KeyStroke.getKeyStroke('N', InputEvent.CTRL_DOWN_MASK));
        mFile.add(newItem);
        newItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    // TODO ask if not saved
                    VisualScritingEditor.this.visualEditorPanel.load(null);
                    VisualScritingEditor.this.visualEditorPanel.getEditor().addStartAndEndNodes();

                } catch (Exception e1) {
                    e1.printStackTrace();
                }

            }
        });

        final JMenuItem openItem = new JMenuItem("Open File...");
        openItem.setAccelerator(KeyStroke.getKeyStroke('O', InputEvent.CTRL_DOWN_MASK));
        mFile.add(openItem);
        openItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                // TODO ask if not saved
                VisualScritingEditor.this.visualEditorPanel.load();

            }
        });

        mFile.addSeparator();
        final JMenuItem saveItem = new JMenuItem("Save");
        saveItem.setAccelerator(KeyStroke.getKeyStroke('S', InputEvent.CTRL_DOWN_MASK));
        mFile.add(saveItem);
        saveItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    VisualScritingEditor.this.visualEditorPanel.save();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }

            }
        });

        final JMenuItem saveAsItem = new JMenuItem("Save As...");
        mFile.add(saveAsItem);
        saveAsItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    VisualScritingEditor.this.visualEditorPanel.saveAs();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }

            }
        });

        mFile.addSeparator();
        final JMenuItem exitItem = new JMenuItem("Exit");
        mFile.add(exitItem);
        exitItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                // TODO ask if not saved
                System.exit(0);

            }
        });

        menubar.add(mFile);

        final JMenu mEdit = new JMenu("Edit");
        final JMenuItem undoItem = new JMenuItem("Undo");
        undoItem.setAccelerator(KeyStroke.getKeyStroke('Z', InputEvent.CTRL_DOWN_MASK));
        mEdit.add(undoItem);
        undoItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    VisualScritingEditor.this.visualEditorPanel.getEditor().getEditor().undo();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        });

        final JMenuItem redoItem = new JMenuItem("Redo");
        redoItem.setAccelerator(KeyStroke.getKeyStroke('Y', InputEvent.CTRL_DOWN_MASK));
        mEdit.add(redoItem);
        undoItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    VisualScritingEditor.this.visualEditorPanel.getEditor().getEditor().redo();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        });

        mEdit.addSeparator();
        final JMenuItem cutItem = new JMenuItem("Cut");
        cutItem.setAccelerator(KeyStroke.getKeyStroke('X', InputEvent.CTRL_DOWN_MASK));
        mEdit.add(cutItem);
        final JMenuItem copyItem = new JMenuItem("Copy");
        copyItem.setAccelerator(KeyStroke.getKeyStroke('C', InputEvent.CTRL_DOWN_MASK));
        mEdit.add(copyItem);
        final JMenuItem pasteItem = new JMenuItem("Paste");
        pasteItem.setAccelerator(KeyStroke.getKeyStroke('V', InputEvent.CTRL_DOWN_MASK));
        mEdit.add(pasteItem);

        menubar.add(mEdit);
        final JMenu mAbout = new JMenu("About");
        final JMenuItem aboutItem = new JMenuItem("About jVisualScripting");
        mAbout.add(aboutItem);
        aboutItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(VisualScritingEditor.this, "jVisualScripting editor"
                        + " is a simple workflow editor for jVisualScripting.\n\njVisualScripting is an OpenSource software released under LGPL licence.\nIt's part of the OpenConcerto ERP.\n \nSource code, binaries and issues tracking are hosted on GitHub:\nhttps://github.com/openconcerto/jVisualScripting\n\n",
                        "About jVisualScripting editor", JOptionPane.PLAIN_MESSAGE);

            }
        });

        menubar.add(mAbout);
        return menubar;
    }

    public static void main(String[] args) throws IOException, ClassNotFoundException {

        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                try {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                final VisualScritingEditor f = new VisualScritingEditor(Engine.getDefault());
                try {
                    if (args.length > 0) {
                        String script = args[0];
                        f.load(new File(script));
                        for (int i = 1; i < args.length; i++) {
                            // key=value
                            String arg = args[i];
                            int index = arg.indexOf('=');
                            if (index > 1 && index < arg.length() - 1) {
                                String key = arg.substring(0, index);
                                String value = arg.substring(index + 1);
                                f.getVisualEditorPanel().getEditor().getGraph().putParameter(key, value);
                            }
                        }

                    } else {
                        f.addStartAndEndNodes();
                    }
                } catch (Exception e) {
                    f.addStartAndEndNodes();
                    e.printStackTrace();
                }
                f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                f.setSize(1200, 900);
                f.setLocationRelativeTo(null);
                f.setVisible(true);

            }
        });
    }
}
