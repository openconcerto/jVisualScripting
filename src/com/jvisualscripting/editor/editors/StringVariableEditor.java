package com.jvisualscripting.editor.editors;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.jvisualscripting.Node;
import com.jvisualscripting.editor.EventGraphEditorPanel;
import com.jvisualscripting.editor.NodeEditor;
import com.jvisualscripting.editor.SwingThrottle;
import com.jvisualscripting.variable.StringVariable;

public class StringVariableEditor implements NodeEditor {

    @Override
    public JPanel createEditor(EventGraphEditorPanel panel, Node n) {
        final JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new GridBagLayout());
        final GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(2, 4, 2, 4);
        c.gridx = 0;
        c.gridy = 0;
        c.fill = GridBagConstraints.NONE;
        c.weightx = 0;
        // Name
        p.add(new JLabel("Name", SwingConstants.RIGHT), c);
        c.gridx++;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        final StringVariable v = (StringVariable) n;
        final JTextField textName = new JTextField(v.getName());
        p.add(textName, c);
        // Value
        c.gridy++;
        c.gridx = 0;
        c.weightx = 0;
        p.add(new JLabel("Value", SwingConstants.RIGHT), c);
        c.gridy++;
        c.gridwidth = 2;
        c.weightx = 1;
        c.weighty = 1;
        c.fill = GridBagConstraints.BOTH;
        final JTextArea text = new JTextArea(v.getValue());
        text.setFont(new JTextField().getFont());
        p.add(new JScrollPane(text), c);
        p.setMinimumSize(new Dimension(100, 200));
        p.setPreferredSize(new Dimension(100, 200));

        final SwingThrottle t = new SwingThrottle(100, new Runnable() {

            @Override
            public void run() {
                v.setValue(text.getText());
                v.setName(textName.getText());
                panel.fireGraphChange();
                panel.createCheckPoint();
                panel.repaint();
            }
        });

        DocumentListener listener = new DocumentListener() {

            @Override
            public void removeUpdate(DocumentEvent e) {
                changedUpdate(e);

            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                changedUpdate(e);

            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                t.execute();

            }
        };
        text.getDocument().addDocumentListener(listener);
        textName.getDocument().addDocumentListener(listener);
        return p;
    }

    @Override
    public String getName() {
        return "String";
    }

}
