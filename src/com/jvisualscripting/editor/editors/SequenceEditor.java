package com.jvisualscripting.editor.editors;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.jvisualscripting.ExecutionPin;
import com.jvisualscripting.Node;
import com.jvisualscripting.Pin;
import com.jvisualscripting.Pin.PinMode;
import com.jvisualscripting.editor.EventGraphEditorPanel;
import com.jvisualscripting.editor.NodeEditor;
import com.jvisualscripting.editor.SwingThrottle;
import com.jvisualscripting.editor.VLink;
import com.jvisualscripting.flowcontrol.Sequence;

public class SequenceEditor implements NodeEditor {

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
        p.add(new JLabel("Ouput pins", SwingConstants.RIGHT), c);
        c.gridx++;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        final Sequence v = (Sequence) n;
        final JSpinner spinner = new JSpinner(new SpinnerNumberModel(v.getOutputSize(), 1, 16, 1));
        p.add(spinner, c);

        final SwingThrottle t = new SwingThrottle(100, new Runnable() {

            @Override
            public void run() {
                int newSize = (Integer) spinner.getValue();
                if (v.getOutputSize() > newSize) {
                    int toRemove = v.getOutputSize() - newSize;
                    for (int i = 0; i < toRemove; i++) {
                        Pin pinToRemove = v.getLastOutputPin();
                        VLink link = panel.getVLink(pinToRemove);
                        if (link != null) {
                            panel.remove(link);
                        }
                        v.getOutputs().remove(pinToRemove);
                    }

                } else if (v.getOutputSize() < newSize) {
                    int toAdd = newSize - v.getOutputSize();
                    for (int i = 0; i < toAdd; i++) {
                        v.getOutputs().add(new ExecutionPin(v, "Then " + v.getOutputSize(), PinMode.OUTPUT));
                    }
                }
                v.computeSize();
                panel.fireGraphChange();
                panel.createCheckPoint();
                panel.repaint();
            }
        });

        spinner.addChangeListener(new ChangeListener() {

            @Override
            public void stateChanged(ChangeEvent e) {
                t.execute();

            }
        });

        return p;
    }

    @Override
    public String getName() {
        return "Sequence";
    }

}
