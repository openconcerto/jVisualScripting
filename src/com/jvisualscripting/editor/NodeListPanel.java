package com.jvisualscripting.editor;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.TransferHandler;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

import com.jvisualscripting.Engine;
import com.jvisualscripting.Node;

public class NodeListPanel extends JPanel {
    JPanel pSearch = new JPanel();

    public NodeListPanel(Engine engine) {

        List<Class<? extends Node>> allNodes = new ArrayList<>(engine.getRegisteredNodes());
        List<Class<? extends Node>> nodes = new ArrayList<>();
        Collections.sort(allNodes, new Comparator<Class<? extends Node>>() {

            @Override
            public int compare(Class<? extends Node> o1, Class<? extends Node> o2) {

                return engine.getTypeName(o1).compareTo(engine.getTypeName(o2));

            }
        });
        nodes.addAll(allNodes);
        this.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new Insets(4, 2, 4, 2);
        final NodeTableModel dm = new NodeTableModel(nodes, engine);
        c.weightx = 1;
        this.pSearch.setOpaque(false);
        this.pSearch.setLayout(new BorderLayout(0, 4));

        final JLabel comp = new JLabel("Drag a node into the editor to add it");
        comp.setFont(comp.getFont().deriveFont(Font.BOLD));
        this.pSearch.add(comp, BorderLayout.NORTH);

        JTextField search = new JTextField();
        this.pSearch.add(new JLabel("Search "), BorderLayout.WEST);
        this.pSearch.add(search, BorderLayout.CENTER);
        c.gridy++;

        this.add(this.pSearch, c);

        final SwingThrottle t = new SwingThrottle(100, new Runnable() {

            @Override
            public void run() {
                String text = search.getText().trim();
                nodes.clear();
                if (text.isEmpty()) {

                    nodes.addAll(allNodes);

                }
                String[] parts = text.split(" ");
                List<String> partsToSearch = new ArrayList<>();
                for (String p : parts) {
                    String tr = p.trim().toLowerCase();
                    if (!tr.isEmpty()) {
                        partsToSearch.add(p);
                    }
                }

                for (Class<? extends Node> n : allNodes) {
                    boolean match = false;
                    String name = (engine.getTypeName(n) + engine.getName(n)).toLowerCase();

                    for (String s : partsToSearch) {
                        if (!name.contains(s)) {
                            match = false;
                            break;
                        }
                        match = true;
                    }
                    if (match) {
                        nodes.add(n);
                    }

                }
                dm.fireTableDataChanged();
            }
        });

        search.getDocument().addDocumentListener(new DocumentListener() {

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
        });
        final Color bgColor = new Color(248, 248, 248);
        final Color fgColor = new Color(0, 0, 0);
				JTable table = new JTable(dm) {

            @Override
            public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
                // with alternate background
                Component comp = super.prepareRenderer(renderer, row, column);
                if (!comp.getBackground().equals(getSelectionBackground())) {
                    comp.setBackground(row % 2 == 0 ? bgColor : Color.WHITE);
                }
                boolean selection = false;
                for (int index : getSelectedRows()) {
                    if (row == index) {
                        selection = true;
                    }
                }
                comp.setForeground(selection ? Color.WHITE : Color.BLACK);
                return comp;
            }

        };

        c.insets = new Insets(0, 0, 0, 0);
        table.setRowHeight((int) (table.getRowHeight() * 1.3));
        table.setAutoCreateRowSorter(true);
        table.setDragEnabled(true);
        table.setGridColor(bgColor);
        table.setShowHorizontalLines(false);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
        table.setDefaultRenderer(String.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                // Hack to add spacing because setIntercellSpacing is not compatible with alternate
                // cell background
                String v = " " + value.toString();
                return super.getTableCellRendererComponent(table, v, isSelected, hasFocus, row, column);
            }
        });
        table.setTransferHandler(new TransferHandler() {
            @Override
            protected Transferable createTransferable(JComponent c) {
                System.err.println("NodeListPanel createTransferable()");

                return new Transferable() {

                    @Override
                    public boolean isDataFlavorSupported(DataFlavor flavor) {
                        System.err.println("NodeListPanel isDataFlavorSupported()" + flavor);
                        return true;
                    }

                    @Override
                    public DataFlavor[] getTransferDataFlavors() {
                        DataFlavor[] dataFlavors = new DataFlavor[1];

                        dataFlavors[0] = DataFlavor.stringFlavor;

                        return dataFlavors;
                    }

                    @Override
                    public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
                        System.err.println("NodeListPanel getTransferData()" + flavor);
                        Class<? extends Node> c = nodes.get(table.getSelectedRow());
                        return c.getCanonicalName();
                    }
                };
            }

            @Override
            public int getSourceActions(JComponent c) {
                return COPY;
            }

            @Override
            protected void exportDone(JComponent source, Transferable data, int action) {
                System.err.println("NodeListPanel exportDone()" + data);
                super.exportDone(source, data, action);
            }

        });
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getColumnModel().getColumn(0).setMinWidth(100);
        table.getColumnModel().getColumn(0).setMaxWidth(150);
        c.weighty = 1;
        c.gridy++;
        this.add(new JScrollPane(table), c);

    }

}
