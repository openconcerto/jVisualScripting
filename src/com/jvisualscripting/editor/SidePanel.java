package com.jvisualscripting.editor;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

public class SidePanel extends JPanel {

    private MiniViewPanel miniViewPanel;

    private JTabbedPane tabbedPane = new JTabbedPane();

    public SidePanel(EventGraphEditorPanel graph, NodeListPanel nodeListPanel) {
        nodeListPanel.setOpaque(false);
        this.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 1;
        c.weighty = 0;
        c.fill = GridBagConstraints.BOTH;
        // Mini view
        this.miniViewPanel = new MiniViewPanel(graph) {
            @Override
            public Dimension getPreferredSize() {
                final int w = SidePanel.this.getSize().width;
                final int h = (int) (w / 1.618);
                return new Dimension(w, h);
            }

            @Override
            public Dimension getMinimumSize() {
                return getPreferredSize();
            }

            @Override
            public Dimension getMaximumSize() {
                final int w = 300;
                final int h = (int) (w / 1.618);
                return new Dimension(w, h);
            }

        };
        this.add(this.miniViewPanel, c);

        // Main panel
        c.gridy++;
        c.weighty = 1;

        this.tabbedPane.addTab("Available Nodes", nodeListPanel);
        this.add(this.tabbedPane, c);
    }

    public void setRect(int x, int y, int width, int height) {
        this.miniViewPanel.setRect(x, y, width, height);
    }

    @Override
    public Dimension getMinimumSize() {
        return new Dimension(250, 250);
    }

    @Override
    public Dimension getMaximumSize() {
        return new Dimension(500, 500);
    }

    public void showEditor(String name, JComponent c) {
        if (this.tabbedPane.getTabCount() > 1) {
            this.tabbedPane.remove(1);
        }
        c.setOpaque(false);
        this.tabbedPane.addTab(name, c);
        this.tabbedPane.setSelectedIndex(this.tabbedPane.getTabCount() - 1);
    }

    public void showOnlyNodeList() {
        if (this.tabbedPane.getTabCount() > 1) {
            this.tabbedPane.remove(1);
        }
    }

}
