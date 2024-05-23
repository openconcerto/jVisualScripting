package com.jvisualscripting.editor.editors;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;
import javax.swing.JPanel;

import com.jvisualscripting.Node;
import com.jvisualscripting.editor.EventGraphEditorPanel;
import com.jvisualscripting.editor.NodeEditor;
import com.jvisualscripting.editor.SwingThrottle;
import com.jvisualscripting.filefunction.FileSaveString;

public class FileSaveStrinEditor implements NodeEditor {

    @Override
    public JPanel createEditor(EventGraphEditorPanel panel, Node n) {
        final JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new GridBagLayout());
        final GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(2, 4, 2, 4);
        c.gridx = 0;
        c.gridy = 0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        final FileSaveString v = (FileSaveString) n;
        final JCheckBox check = new JCheckBox("append");
        check.setSelected(v.isAppendMode());
        p.add(check, c);

        final SwingThrottle t = new SwingThrottle(100, new Runnable() {

            @Override
            public void run() {
                v.setAppendMode(check.isSelected());
                panel.fireGraphChange();
                panel.createCheckPoint();
                panel.repaint();
            }
        });
        check.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                t.execute();
            }
        });

        return p;
    }

    @Override
    public String getName() {
        return "Save String to File";
    }

}
