package com.jvisualscripting.editor;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JColorChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.jvisualscripting.Lane;

public class LaneEditor extends JPanel {

    public LaneEditor(Lane lane, final EventGraphEditorPanel panel) {
        this.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 0;
        c.insets = new Insets(2, 4, 2, 4);
        c.fill = GridBagConstraints.HORIZONTAL;
        this.add(new JLabel("Name", SwingConstants.RIGHT), c);
        c.gridx++;
        c.weightx = 1;
        final JTextField textName = new JTextField(lane.getName());
        textName.getDocument().addDocumentListener(new DocumentListener() {

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
                lane.setName(textName.getText());
                panel.fireGraphChange();
                panel.createCheckPoint();
                panel.repaint();

            }
        });
        this.add(textName, c);
        //
        c.gridy++;
        c.gridx = 0;
        c.weightx = 0;
        this.add(new JLabel("Y position", SwingConstants.RIGHT), c);
        c.gridx++;
        c.weightx = 1;
        final JSpinner spinnerY = new JSpinner(new SpinnerNumberModel(lane.getY(), 0, Short.MAX_VALUE, 20));
        this.add(spinnerY, c);

        final SwingThrottle t = new SwingThrottle(100, new Runnable() {

            @Override
            public void run() {
                lane.setY(((Number) spinnerY.getValue()).shortValue());
                panel.fireGraphChange();
                panel.createCheckPoint();
                panel.repaint();
            }
        });

        spinnerY.addChangeListener(new ChangeListener() {

            @Override
            public void stateChanged(ChangeEvent e) {
                t.execute();

            }
        });
        //
        c.gridy++;
        c.gridx = 0;
        c.weightx = 0;
        this.add(new JLabel("Height", SwingConstants.RIGHT), c);
        c.gridx++;
        c.weightx = 1;
        final JSpinner spinnerH = new JSpinner(new SpinnerNumberModel(lane.getHeight(), 20, Short.MAX_VALUE, 20));
        this.add(spinnerH, c);

        final SwingThrottle tH = new SwingThrottle(100, new Runnable() {

            @Override
            public void run() {
                lane.setHeight(((Number) spinnerH.getValue()).shortValue());
                panel.fireGraphChange();
                panel.createCheckPoint();
                panel.repaint();
            }
        });

        spinnerH.addChangeListener(new ChangeListener() {

            @Override
            public void stateChanged(ChangeEvent e) {
                tH.execute();

            }
        });

        // Color selector
        //
        c.gridy++;
        c.gridx = 0;
        c.weightx = 0;
        this.add(new JLabel("Lane color", SwingConstants.RIGHT), c);
        c.gridy++;
        c.gridwidth = 2;
        final JColorChooser colorChooser = new JColorChooser();

        final SwingThrottle tColor = new SwingThrottle(100, new Runnable() {

            @Override
            public void run() {
                Color color = colorChooser.getColor();
                lane.setColor(color);
                panel.fireGraphChange();
                panel.createCheckPoint();
                panel.repaint();
            }
        });
        colorChooser.getSelectionModel().addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                tColor.execute();

            }
        });
        colorChooser.setPreviewPanel(new JPanel());
        this.add(colorChooser, c);

        // Spacer

        c.weighty = 1;
        c.gridy++;
        this.add(new JPanel(), c);

    }

}
