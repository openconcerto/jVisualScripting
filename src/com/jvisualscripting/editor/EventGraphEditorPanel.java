package com.jvisualscripting.editor;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JViewport;
import javax.swing.KeyStroke;
import javax.swing.Scrollable;
import javax.swing.SwingUtilities;
import javax.swing.TransferHandler;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;

import com.jvisualscripting.Engine;
import com.jvisualscripting.EventGraph;
import com.jvisualscripting.FlowNode;
import com.jvisualscripting.Lane;
import com.jvisualscripting.Link;
import com.jvisualscripting.Node;
import com.jvisualscripting.Pin;
import com.jvisualscripting.Pin.PinMode;
import com.jvisualscripting.event.EventNode;

public class EventGraphEditorPanel extends JPanel implements Scrollable {

    public static final int GRID_SIZE = 20;
    private static final long serialVersionUID = 8745082478333263177L;
    private EventGraph graph;
    private List<VNode> vNodes = new ArrayList<>();
    private List<VLink> vLinks = new ArrayList<>();

    private Font fontBase;
    private Font fontNode;
    private Font fontLane;
    protected int dClickX;
    protected int dClickY;
    protected boolean moving;
    protected VNode movingNode;
    protected int previousX;
    protected int previousY;
    protected long previousTime;
    protected boolean glued;
    protected int highlightGlueX = Integer.MIN_VALUE;
    protected int highlightGlueY = Integer.MIN_VALUE;
    protected Set<VNode> selectedNodes = new HashSet<>();
    private Set<Pin> selectedPins = new HashSet<>();
    protected HalfConnectedVLink temporyLink;
    private Point selectedPinLocation;
    protected VLink disabledLink;
    private History history = new History(500);

    private Point origin;
    protected int originButton;
    private Map<Class<? extends Node>, Class<? extends NodeEditor>> editor = new HashMap<>();
    private List<NodeSelectionListener> nListeners = new ArrayList<>();
    private List<GraphChangeListener> graphChangeListeners = new ArrayList<>();

    private long lastTimeScrollRect = 0;

    public EventGraphEditorPanel(EventGraph g) {

        setGraph(g);
        try {

            this.fontBase = Font.createFont(Font.TRUETYPE_FONT, this.getClass().getResourceAsStream("Roboto-Regular.ttf"));

        } catch (FontFormatException | IOException e) {
            throw new IllegalStateException(e);
        }

        this.fontNode = this.fontBase.deriveFont(14f);
        this.fontLane = this.fontBase.deriveFont(18f);

        this.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseReleased(MouseEvent e) {
                boolean needCheckPoint = false;
                if (EventGraphEditorPanel.this.moving) {
                    needCheckPoint = true;
                }
                EventGraphEditorPanel.this.moving = false;
                EventGraphEditorPanel.this.movingNode = null;
                EventGraphEditorPanel.this.highlightGlueX = Integer.MIN_VALUE;
                EventGraphEditorPanel.this.highlightGlueY = Integer.MIN_VALUE;
                if (EventGraphEditorPanel.this.temporyLink != null) {
                    needCheckPoint = true;
                    final Pin p = getPinUnder(e.getX(), e.getY());
                    if (p != null && !p.isConnected()) {
                        Pin to = EventGraphEditorPanel.this.temporyLink.getConnectedPin();
                        if (to.canConnectPin(p) && p.canConnectPin(to)) {
                            if (EventGraphEditorPanel.this.disabledLink != null) {
                                EventGraphEditorPanel.this.disabledLink.getLink().getFrom().setConnectedPin(null);
                                EventGraphEditorPanel.this.disabledLink.getLink().getTo().setConnectedPin(null);
                                EventGraphEditorPanel.this.graph.remove(EventGraphEditorPanel.this.disabledLink.getLink());
                                EventGraphEditorPanel.this.vLinks.remove(EventGraphEditorPanel.this.disabledLink);
                            }
                            if (to.getMode() == PinMode.INPUT) {
                                Link newLink = EventGraphEditorPanel.this.graph.addLink(p, to);
                                EventGraphEditorPanel.this.vLinks.add(new VLink(newLink));
                            } else {
                                Link newLink = EventGraphEditorPanel.this.graph.addLink(to, p);
                                EventGraphEditorPanel.this.vLinks.add(new VLink(newLink));
                            }
                            EventGraphEditorPanel.this.selectedPins.clear();

                        } else {
                            EventGraphEditorPanel.this.temporyLink.getConnectedPin().setConnectedPin(null);
                            if (EventGraphEditorPanel.this.disabledLink != null)
                                EventGraphEditorPanel.this.disabledLink.setEnabled(true);
                        }
                    } else if (EventGraphEditorPanel.this.disabledLink != null) {
                        EventGraphEditorPanel.this.disabledLink.getLink().getFrom().setConnectedPin(null);
                        EventGraphEditorPanel.this.disabledLink.getLink().getTo().setConnectedPin(null);
                        EventGraphEditorPanel.this.graph.remove(EventGraphEditorPanel.this.disabledLink.getLink());
                        EventGraphEditorPanel.this.vLinks.remove(EventGraphEditorPanel.this.disabledLink);
                    } else {
                        EventGraphEditorPanel.this.temporyLink.getConnectedPin().setConnectedPin(null);
                    }

                }
                EventGraphEditorPanel.this.temporyLink = null;
                EventGraphEditorPanel.this.disabledLink = null;
                EventGraphEditorPanel.this.selectedPinLocation = null;
                repaint();
                if (e.isPopupTrigger()) {
                    showMenu(e.getX(), e.getY());
                }
                if (needCheckPoint) {
                    createCheckPoint();

                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
                grabFocus();
                EventGraphEditorPanel.this.origin = new Point(e.getPoint());
                EventGraphEditorPanel.this.originButton = e.getButton();
                EventGraphEditorPanel.this.previousX = e.getX();
                EventGraphEditorPanel.this.previousY = e.getY();
                EventGraphEditorPanel.this.selectedPins.clear();
                EventGraphEditorPanel.this.disabledLink = null;
                Lane lane = getLaneUnder(e.getX(), e.getY());
                if (lane != null) {
                    fireLaneSelected(lane);
                    return;
                }

                VNode node = getVNodeUnder(e.getX(), e.getY());
                if (node != null) {
                    if (!e.isControlDown() && (!EventGraphEditorPanel.this.selectedNodes.contains(node) || EventGraphEditorPanel.this.selectedNodes.isEmpty())) {
                        EventGraphEditorPanel.this.selectedNodes.clear();
                    }

                    EventGraphEditorPanel.this.selectedNodes.add(node);
                    EventGraphEditorPanel.this.moving = true;
                    EventGraphEditorPanel.this.movingNode = node;
                    EventGraphEditorPanel.this.dClickX = e.getX() - node.getNode().getX();
                    EventGraphEditorPanel.this.dClickY = e.getY() - node.getNode().getY();
                    // Move to front
                    EventGraphEditorPanel.this.vNodes.remove(node);
                    EventGraphEditorPanel.this.vNodes.add(node);
                    repaint();
                    if (e.isPopupTrigger()) {
                        showMenu(e.getX(), e.getY());
                    } else {
                        fireNodeSelected(node.getNode());

                    }
                } else {
                    EventGraphEditorPanel.this.selectedNodes.clear();

                    Pin pin = getPinUnder(e.getX(), e.getY());
                    if (pin != null) {
                        EventGraphEditorPanel.this.selectedPins.add(pin);
                        EventGraphEditorPanel.this.selectedPinLocation = e.getPoint();
                        if (pin.getConnectedPin() == null) {
                            Pin from;
                            Pin to;
                            if (pin.getMode().equals(PinMode.OUTPUT)) {
                                from = pin;
                                to = new TempPin(e.getX(), e.getY(), PinMode.INPUT);
                            } else {
                                from = new TempPin(e.getX(), e.getY(), PinMode.OUTPUT);
                                to = pin;
                            }
                            EventGraphEditorPanel.this.temporyLink = new HalfConnectedVLink(from, to);
                        }
                    }
                    repaint();
                    for (NodeSelectionListener l : EventGraphEditorPanel.this.nListeners) {
                        l.nodeSelected(null);
                    }
                }
                if (e.isPopupTrigger()) {
                    showMenu(e.getX(), e.getY());
                }
            }

        });
        this.addMouseMotionListener(new MouseMotionListener() {

            @Override
            public void mouseMoved(MouseEvent e) {
                if (EventGraphEditorPanel.this.moving) {
                    updateLocation(e);
                }
                updateTemporyLink(e);
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (EventGraphEditorPanel.this.moving) {
                    updateLocation(e);
                }
                updateTemporyLink(e);

                if (EventGraphEditorPanel.this.origin != null && !EventGraphEditorPanel.this.moving && EventGraphEditorPanel.this.selectedPinLocation == null
                        && EventGraphEditorPanel.this.temporyLink == null && EventGraphEditorPanel.this.originButton == MouseEvent.BUTTON1) {
                    // Move wiew with drag & left mouse pressed
                    JViewport viewPort = (JViewport) SwingUtilities.getAncestorOfClass(JViewport.class, EventGraphEditorPanel.this);
                    if (viewPort != null) {
                        int deltaX = EventGraphEditorPanel.this.origin.x - e.getX();
                        int deltaY = EventGraphEditorPanel.this.origin.y - e.getY();

                        Rectangle view = viewPort.getViewRect();
                        view.x += deltaX;
                        view.y += deltaY;
                        EventGraphEditorPanel.super.scrollRectToVisible(view);
                    }
                } else {
                    // User is moving a node or a link
                    JViewport viewPort = (JViewport) SwingUtilities.getAncestorOfClass(JViewport.class, EventGraphEditorPanel.this);
                    if (viewPort != null && !e.isPopupTrigger() && EventGraphEditorPanel.this.originButton == MouseEvent.BUTTON1) {
                        Rectangle viewRect = viewPort.getViewRect();
                        viewRect.x = e.getX() - 100;
                        viewRect.y = e.getY() - 100;
                        viewRect.width = 200;
                        viewRect.height = 200;
                        // FIXME : scrollRectToVisible is not working great...
                        // TODO : detect if the mouse is at the left/right/top/bottom border and use
                        // a timer to update the viewRect location
                        // viewRect.x += (dx > 0) ? SCROLL_SPEED : -SCROLL_SPEED;
                        // viewRect.y += (dy > 0) ? SCROLL_SPEED : -SCROLL_SPEED;
                        // scrollRectToVisible(view);
                    }
                }
            }

            private void updateLocation(MouseEvent e) {

                int dx = EventGraphEditorPanel.this.previousX - e.getX();
                int dy = EventGraphEditorPanel.this.previousY - e.getY();

                float v = dx * dx + dy * dy;
                long l = System.currentTimeMillis() - EventGraphEditorPanel.this.previousTime;
                if (l == 0) {
                    l = 1;
                }

                v = v / l;

                int x = e.getX() - EventGraphEditorPanel.this.dClickX;
                int y = e.getY() - EventGraphEditorPanel.this.dClickY;
                if (v < 0.01 || EventGraphEditorPanel.this.glued) {
                    EventGraphEditorPanel.this.glued = false;
                    if (x % GRID_SIZE < 6 || x % GRID_SIZE > 14) {
                        x = GRID_SIZE * Math.round(x / (GRID_SIZE * 1.0f));
                        EventGraphEditorPanel.this.glued = true;
                        EventGraphEditorPanel.this.highlightGlueX = x;
                        if (x < GRID_SIZE) {
                            x = GRID_SIZE;
                        }
                    } else {
                        EventGraphEditorPanel.this.highlightGlueX = Integer.MIN_VALUE;
                    }
                    if (y % GRID_SIZE < 6 || y % GRID_SIZE > 14) {
                        y = GRID_SIZE * Math.round(y / (GRID_SIZE * 1.0f));
                        if (y < GRID_SIZE) {
                            y = GRID_SIZE;
                        }
                        EventGraphEditorPanel.this.glued = true;
                        EventGraphEditorPanel.this.highlightGlueY = y;
                    } else {
                        EventGraphEditorPanel.this.highlightGlueY = Integer.MIN_VALUE;
                    }
                }
                // Update selected node location
                final Node node = EventGraphEditorPanel.this.movingNode.getNode();
                final int deltaX = x - node.getX();
                final int deltaY = y - node.getY();
                for (VNode n : EventGraphEditorPanel.this.selectedNodes) {
                    setNodeLocation(n, n.getNode().getX() + deltaX, n.getNode().getY() + deltaY);
                }

                repaint();

                EventGraphEditorPanel.this.previousX = e.getX();
                EventGraphEditorPanel.this.previousY = e.getY();
                EventGraphEditorPanel.this.previousTime = System.currentTimeMillis();
                fireGraphChange();
            }

            private void updateTemporyLink(MouseEvent e) {
                int x = e.getX();
                int y = e.getY();
                if (x < 0) {
                    x = GRID_SIZE;
                }
                if (x > getSize().width - GRID_SIZE) {
                    x = getSize().width - GRID_SIZE;
                }
                if (y < GRID_SIZE) {
                    y = GRID_SIZE;
                }
                if (y > getSize().height - GRID_SIZE) {
                    y = getSize().height - GRID_SIZE;
                }
                if (EventGraphEditorPanel.this.selectedPinLocation != null) {
                    int dx = EventGraphEditorPanel.this.selectedPinLocation.x - x;
                    int dy = EventGraphEditorPanel.this.selectedPinLocation.y - y;

                    float v = dx * dx + dy * dy;
                    if (v > 200 && EventGraphEditorPanel.this.temporyLink == null) {
                        VLink l = getVLink(EventGraphEditorPanel.this.selectedPinLocation.x, EventGraphEditorPanel.this.selectedPinLocation.y);
                        if (l != null) {
                            // User is detaching a link
                            EventGraphEditorPanel.this.disabledLink = l;
                            l.setEnabled(false);
                            Pin pin = getPinUnder(EventGraphEditorPanel.this.selectedPinLocation.x, EventGraphEditorPanel.this.selectedPinLocation.y);
                            EventGraphEditorPanel.this.selectedPinLocation = null;
                            Pin from;
                            Pin to;
                            Pin p2 = l.getLink().getTo();
                            if (p2.equals(pin)) {
                                p2 = l.getLink().getFrom();
                            }

                            if (pin.getMode().equals(PinMode.INPUT)) {
                                from = p2;
                                to = new TempPin(x, y, PinMode.OUTPUT);
                                l.getLink().getTo().setConnectedPin(null);
                            } else {
                                from = new TempPin(x, y, PinMode.INPUT);
                                to = p2;
                                l.getLink().getFrom().setConnectedPin(null);
                            }
                            EventGraphEditorPanel.this.temporyLink = new HalfConnectedVLink(from, to);

                        }
                        repaint();
                    }

                }

                if (EventGraphEditorPanel.this.temporyLink != null) {
                    EventGraphEditorPanel.this.selectedPins.clear();
                    EventGraphEditorPanel.this.temporyLink.getTempPin().setLocation(x, y);
                    EventGraphEditorPanel.this.selectedPins.add(EventGraphEditorPanel.this.temporyLink.getConnectedPin());
                    final Pin p = getPinUnder(x, y);
                    if (p != null && !p.isConnected()) {
                        Pin to = EventGraphEditorPanel.this.temporyLink.getConnectedPin();
                        if (to.canConnectPin(p) && p.canConnectPin(to)) {
                            EventGraphEditorPanel.this.selectedPins.add(p);
                        }
                    }
                    repaint();
                    fireGraphChange();
                }
            }

        });
        this.addAncestorListener(new AncestorListener() {

            @Override
            public void ancestorRemoved(AncestorEvent event) {
                // Nothing
            }

            @Override
            public void ancestorMoved(AncestorEvent event) {
                // Nothing
            }

            @Override
            public void ancestorAdded(AncestorEvent event) {
                final String UNDO = "Undo action key";
                final String REDO = "Redo action key";
                final String DELETE = "delete";
                final String UP = "up";
                final String DOWN = "down";
                final String LEFT = "left";
                final String RIGHT = "right";
                final String COPY = "copy";
                final String PASTE = "paste";

                Action copyAction = new AbstractAction() {

                    public void actionPerformed(ActionEvent e) {
                        copy();
                        fireGraphChange();
                    }
                };
                Action pasteAction = new AbstractAction() {

                    public void actionPerformed(ActionEvent e) {
                        try {
                            paste();
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }
                        fireGraphChange();
                    }
                };
                Action undoAction = new AbstractAction() {
                    private static final long serialVersionUID = 1L;

                    public void actionPerformed(ActionEvent e) {
                        try {
                            undo();
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }
                        fireGraphChange();
                    }
                };
                Action redoAction = new AbstractAction() {
                    private static final long serialVersionUID = 2L;

                    public void actionPerformed(ActionEvent e) {
                        try {
                            redo();
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }
                        fireGraphChange();
                    }
                };
                Action deleteAction = new AbstractAction() {
                    private static final long serialVersionUID = 2L;

                    public void actionPerformed(ActionEvent e) {
                        try {
                            deleteSelectedNodes();
                        } catch (Exception e1) {
                            e1.printStackTrace();
                        }
                    }

                    private void deleteSelectedNodes() {
                        for (VNode node : EventGraphEditorPanel.this.selectedNodes) {
                            remove(node);
                        }
                        createCheckPoint();
                        EventGraphEditorPanel.this.repaint();
                        fireGraphChange();
                    }
                };

                Action leftAction = new AbstractAction() {
                    private static final long serialVersionUID = 2L;

                    public void actionPerformed(ActionEvent e) {
                        for (VNode node : EventGraphEditorPanel.this.selectedNodes) {
                            int x = node.getNode().getX();
                            int y = node.getNode().getY();
                            x -= GRID_SIZE;
                            x = Math.round((1.0f * x) / GRID_SIZE) * GRID_SIZE;
                            if (x < GRID_SIZE) {
                                x = GRID_SIZE;
                            }
                            node.setLocation(x, y);
                        }
                        createCheckPoint();
                        EventGraphEditorPanel.this.repaint();
                        fireGraphChange();
                    }
                };
                Action upAction = new AbstractAction() {
                    private static final long serialVersionUID = 2L;

                    public void actionPerformed(ActionEvent e) {
                        for (VNode node : EventGraphEditorPanel.this.selectedNodes) {
                            int x = node.getNode().getX();
                            int y = node.getNode().getY();
                            y -= GRID_SIZE;
                            y = Math.round((1.0f * y) / GRID_SIZE) * GRID_SIZE;
                            if (y < GRID_SIZE) {
                                y = GRID_SIZE;
                            }
                            node.setLocation(x, y);
                        }
                        createCheckPoint();
                        EventGraphEditorPanel.this.repaint();
                        fireGraphChange();
                    }
                };
                Action rightAction = new AbstractAction() {
                    private static final long serialVersionUID = 2L;

                    public void actionPerformed(ActionEvent e) {
                        for (VNode node : EventGraphEditorPanel.this.selectedNodes) {
                            int x = node.getNode().getX();
                            int y = node.getNode().getY();
                            if (x < EventGraphEditorPanel.this.getWidth() - 2 * GRID_SIZE) {
                                x += GRID_SIZE;
                                x = Math.round((1.0f * x) / GRID_SIZE) * GRID_SIZE;
                            }
                            node.setLocation(x, y);
                        }
                        createCheckPoint();
                        EventGraphEditorPanel.this.repaint();
                        fireGraphChange();
                    }
                };
                Action downAction = new AbstractAction() {
                    private static final long serialVersionUID = 2L;

                    public void actionPerformed(ActionEvent e) {
                        for (VNode node : EventGraphEditorPanel.this.selectedNodes) {
                            int x = node.getNode().getX();
                            int y = node.getNode().getY();
                            if (y < EventGraphEditorPanel.this.getHeight() - 2 * GRID_SIZE) {
                                y += GRID_SIZE;
                                y = Math.round((1.0f * y) / GRID_SIZE) * GRID_SIZE;
                            }
                            node.setLocation(x, y);
                        }
                        createCheckPoint();
                        EventGraphEditorPanel.this.repaint();
                        fireGraphChange();
                    }
                };

                getActionMap().put(UNDO, undoAction);
                getActionMap().put(REDO, redoAction);
                getActionMap().put(DELETE, deleteAction);
                getActionMap().put(LEFT, leftAction);
                getActionMap().put(RIGHT, rightAction);
                getActionMap().put(UP, upAction);
                getActionMap().put(DOWN, downAction);
                getActionMap().put(COPY, copyAction);
                getActionMap().put(PASTE, pasteAction);

                InputMap[] inputMaps = new InputMap[] { getInputMap(JComponent.WHEN_FOCUSED), getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT),
                        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW), };
                for (InputMap i : inputMaps) {
                    i.put(KeyStroke.getKeyStroke(KeyEvent.VK_Z, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()), UNDO);
                    i.put(KeyStroke.getKeyStroke(KeyEvent.VK_Y, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()), REDO);
                    i.put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), DELETE);
                    i.put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0), LEFT);
                    i.put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0), RIGHT);
                    i.put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), UP);
                    i.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), DOWN);
                    i.put(KeyStroke.getKeyStroke(KeyEvent.VK_C, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()), COPY);
                    i.put(KeyStroke.getKeyStroke(KeyEvent.VK_V, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()), PASTE);
                }

            }
        });

        this.setTransferHandler(new TransferHandler() {
            @Override
            public boolean importData(TransferSupport support) {
                final Point drop = support.getDropLocation().getDropPoint();

                try {
                    String str = support.getTransferable().getTransferData(DataFlavor.stringFlavor).toString();
                    List<Class<? extends Node>> nodes = Engine.getDefault().getRegisteredNodes();
                    for (Class<? extends Node> class1 : nodes) {
                        if (class1.getCanonicalName().equals(str)) {
                            Constructor<?> ctor = class1.getDeclaredConstructor();
                            ctor.setAccessible(true);
                            Node n = (Node) ctor.newInstance();
                            EventGraphEditorPanel.this.graph.add(n);
                            VNode vn = new VNode(n);
                            vn.setLocation((int) drop.getX(), (int) drop.getY());
                            EventGraphEditorPanel.this.vNodes.add(vn);
                            repaint();
                            createCheckPoint();
                            fireGraphChange();
                            return true;
                        }
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
                return super.importData(support);
            }

            @Override
            public boolean canImport(JComponent comp, DataFlavor[] transferFlavors) {
                for (DataFlavor e : transferFlavors) {
                    if (e.equals(DataFlavor.stringFlavor)) {
                        return true;
                    }
                }
                return super.canImport(comp, transferFlavors);
            }

        });

    }

    @Override
    public void scrollRectToVisible(Rectangle aRect) {
        if (System.currentTimeMillis() - this.lastTimeScrollRect > 500) {
            this.lastTimeScrollRect = System.currentTimeMillis();
            super.scrollRectToVisible(aRect);
        }
    }

    public void scrollRect(Rectangle aRect) {
        super.scrollRectToVisible(aRect);
    }

    protected Lane getLaneUnder(int x, int y) {
        Graphics g = getGraphics();
        g.setFont(this.fontLane);
        for (Lane lane : this.graph.getLanes()) {
            if (y < lane.getY() || y > lane.getY() + lane.getHeight()) {
                continue;
            }

            String str = lane.getName();
            Rectangle2D rect = g.getFontMetrics().getStringBounds(str, g);

            int stringWidth = (int) rect.getWidth();
            int stringHeight = (int) rect.getHeight();

            Rectangle2D fullRect = new Rectangle(4, lane.getY() + 7, 20 + stringWidth, stringHeight + 4);
            if (fullRect.contains(x, y)) {
                return lane;
            }
        }
        return null;
    }

    public void setGraph(EventGraph graph) {
        this.graph = graph;
        List<Node> nodes = graph.getNodes();
        this.vNodes.clear();
        for (Node node : nodes) {
            this.vNodes.add(new VNode(node));
        }
        this.vLinks.clear();
        for (Link link : graph.getLinks()) {

            VLink l = new VLink(link);
            this.vLinks.add(l);
        }
        this.history.clear();
        createCheckPoint();
        repaint();
    }

    protected void setNodeLocation(VNode n, int x, int y) {
        if (x < GRID_SIZE) {
            x = GRID_SIZE;
        }
        if (x > getSize().width - GRID_SIZE) {
            x = getSize().width - GRID_SIZE;
        }
        if (y < GRID_SIZE) {
            y = GRID_SIZE;
        }
        if (y > getSize().height - GRID_SIZE) {
            y = getSize().height - GRID_SIZE;
        }
        n.setLocation(x, y);

    }

    protected void copy() {
        if (this.selectedNodes.isEmpty()) {
            return;
        }

        List<Node> nodes = new ArrayList<>(this.selectedNodes.size());
        for (VNode vnodes : this.selectedNodes) {
            nodes.add(vnodes.getNode());
        }
        List<Link> links = new ArrayList<>();
        for (Link l : getGraph().getLinks()) {
            if (nodes.contains(l.getFrom().getNode()) && nodes.contains(l.getTo().getNode())) {
                links.add(l);
            }
        }

        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        TransferableNodesAndLinks t = new TransferableNodesAndLinks(nodes, links);
        clipboard.setContents(t, null);
    }

    protected void paste() throws IOException {

        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        try {
            Object o = clipboard.getData(TransferableNodesAndLinks.FLAVOR);

            if (o instanceof TransferableNodesAndLinks) {
                TransferableNodesAndLinks n = (TransferableNodesAndLinks) o;

                // Create new nodes
                final List<VNode> newNodes = new ArrayList<>(n.getNodes().size());
                for (Node node : n.getNodes()) {
                    node.assignNewId();
                    node.setLocation(node.getX() + 60, node.getY() + 60);
                    newNodes.add(new VNode(node));
                    getGraph().add(node);
                }
                // Add nodes
                this.vNodes.addAll(newNodes);

                // New nodes are the new selection
                this.selectedNodes.clear();
                this.selectedNodes.addAll(newNodes);

                // Add links
                for (Link l : n.getLinks()) {
                    this.vLinks.add(new VLink(l));
                    getGraph().add(l);
                }

                fireGraphChange();
                createCheckPoint();
                repaint();
            }

        } catch (UnsupportedFlavorException e) {
            e.printStackTrace();
        }

    }

    protected void redo() throws IOException {
        CheckPoint c = this.history.redo();
        if (c != null) {
            restoreFromCheckPoint(c);
        }
        repaint();
    }

    protected void undo() throws IOException {
        CheckPoint c = this.history.undo();
        if (c != null) {
            restoreFromCheckPoint(c);

        }
        repaint();
    }

    private void restoreFromCheckPoint(CheckPoint c) {
        this.graph = c.getGraph();
        this.selectedNodes.clear();
        Set<Node> selectedNodes2 = c.getSelectedNodes();
        for (Node n : selectedNodes2) {
            this.selectedNodes.add(new VNode(n));
        }

        this.selectedPins.clear();
        this.selectedPins.addAll(c.getSelectedPins());
        this.vNodes.clear();
        for (Node node : c.getGraph().getNodes()) {
            VNode vn = new VNode(node);
            this.vNodes.add(vn);

        }
        this.vLinks.clear();
        for (Link link : c.getGraph().getLinks()) {
            VLink l = new VLink(link);
            this.vLinks.add(l);
        }
        fireNodeSelected(null);
    }

    protected VLink getVLink(int x, int y) {
        Pin p = getPinUnder(x, y);
        for (VLink link : this.vLinks) {
            if (link.getLink().hasPin(p)) {
                return link;
            }
        }
        return null;
    }

    protected void showMenu(int x, int y) {
        final VNode node = getVNodeUnder(x, y);
        if (node == null) {
            final JPopupMenu popup = new JPopupMenu();
            // Add node
            JMenu m = new JMenu("Add node");

            Engine e = Engine.getDefault();
            Map<String, List<Class<? extends Node>>> map = new HashMap<>();

            for (Class<? extends Node> c : e.getRegisteredNodes()) {
                final String typeName = e.getTypeName(c);
                List<Class<? extends Node>> list = map.get(typeName);
                if (list == null) {
                    list = new ArrayList<>();
                    map.put(typeName, list);
                }
                list.add(c);
            }
            List<String> types = new ArrayList<>();
            types.addAll(map.keySet());
            Collections.sort(types);

            for (String type : types) {
                JMenu menu = new JMenu(type);
                m.add(menu);

                List<Class<? extends Node>> nodes = map.get(type);
                Collections.sort(nodes, new Comparator<Class<? extends Node>>() {

                    @Override
                    public int compare(Class<? extends Node> o1, Class<? extends Node> o2) {
                        return e.getName(o1).compareToIgnoreCase(e.getName(o2));
                    }
                });
                for (Class<? extends Node> c : nodes) {

                    JMenuItem item = new JMenuItem(e.getName(c));
                    item.addActionListener(new ActionListener() {

                        @Override
                        public void actionPerformed(ActionEvent e) {

                            try {
                                Constructor<?> ctor = c.getDeclaredConstructor();
                                ctor.setAccessible(true);
                                Node n = (Node) ctor.newInstance();
                                EventGraphEditorPanel.this.graph.add(n);
                                VNode vn = new VNode(n);
                                vn.setLocation(x, y);
                                EventGraphEditorPanel.this.vNodes.add(vn);
                                EventGraphEditorPanel.this.repaint();
                                createCheckPoint();
                                fireGraphChange();
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }

                        }
                    });
                    menu.add(item);
                }
            }

            popup.add(m);
            JMenuItem addLane = new JMenuItem("Add Lane");
            addLane.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    EventGraphEditorPanel.this.graph.add(new Lane("New lane", Color.ORANGE, (short) (20 * (y / 20)), (short) 200));
                    fireGraphChange();
                    repaint();
                }
            });
            popup.add(addLane);
            //
            popup.addSeparator();
            JMenuItem undo = new JMenuItem("Undo");
            undo.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    try {
                        undo();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }

                }
            });
            undo.setEnabled(this.history.canUndo());
            popup.add(undo);

            JMenuItem redo = new JMenuItem("Redo");
            redo.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    try {
                        redo();

                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }

                }
            });
            redo.setEnabled(this.history.canRedo());
            popup.add(redo);

            popup.show(this, x, y);

            return;
        }
        final JPopupMenu popup = new JPopupMenu();

        if (node.getNode() instanceof EventNode) {

            final JMenuItem menuItem = new JMenuItem("Trigger Event");
            menuItem.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    final Node n = node.getNode();
                    if (n.canBeExecuted()) {
                        n.execute();
                    } else {
                        JOptionPane.showMessageDialog(EventGraphEditorPanel.this, "node cannot be executed");
                    }

                }
            });
            popup.add(menuItem);
        }

        if (node.getNode() instanceof FlowNode && node.getNode().getInputSize() == 0) {
            // Nodes with inputs cannot started because could introduce an infinite loop
            final JMenuItem menuItem = new JMenuItem("Execute");
            menuItem.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    final Node n = node.getNode();
                    if (n.canBeExecuted()) {
                        n.execute();
                    } else {
                        JOptionPane.showMessageDialog(EventGraphEditorPanel.this, "node cannot be executed");
                    }

                }
            });
            popup.add(menuItem);
        }
        if (!this.selectedNodes.isEmpty()) {
            String remodeActionName = "Remove";
            if (this.selectedNodes.size() > 1) {
                remodeActionName = "Remove all";
            }

            final JMenuItem mRemove = new JMenuItem(remodeActionName);
            mRemove.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    for (VNode n : EventGraphEditorPanel.this.selectedNodes) {
                        remove(n);
                    }
                    createCheckPoint();
                    EventGraphEditorPanel.this.repaint();

                }

            });
            popup.add(mRemove);

        }
        popup.show(this, x, y);

    }

    protected void remove(VNode node) {
        this.vNodes.remove(node);
        Node n = node.getNode();
        List<VLink> linksToRemove = new ArrayList<>();
        for (VLink v : this.vLinks) {
            if (v.getLink().getFrom().getNode() == n) {
                linksToRemove.add(v);
                v.getLink().getFrom().getConnectedPin().setConnectedPin(null);
                v.getLink().getFrom().setConnectedPin(null);
            }
            if (v.getLink().getTo().getNode() == n) {
                linksToRemove.add(v);
                v.getLink().getTo().getConnectedPin().setConnectedPin(null);
                v.getLink().getTo().setConnectedPin(null);
            }
        }
        for (VLink v : linksToRemove) {
            this.vLinks.remove(v);
            this.graph.remove(v.getLink());
        }
        this.graph.remove(node.getNode());
        fireGraphChange();
    }

    public void remove(VLink link) {
        VLink toRemove = null;
        for (VLink v : this.vLinks) {
            if (v == link) {
                toRemove = v;
                break;
            }
        }
        if (toRemove != null) {
            this.vLinks.remove(toRemove);
            this.graph.remove(toRemove.getLink());
        }
    }

    public VLink getVLink(Pin p) {
        for (VLink v : this.vLinks) {
            if (v.getLink().getFrom() == p || v.getLink().getTo() == p) {
                return v;
            }

        }
        return null;
    }

    protected VNode getVNodeUnder(int x, int y) {
        for (VNode vNode : this.vNodes) {
            if (vNode.contains(x, y)) {
                return vNode;
            }
        }
        return null;
    }

    protected Pin getPinUnder(int x, int y) {
        for (VNode vNode : this.vNodes) {
            Pin pin = vNode.getPin(x, y);
            if (pin != null) {
                return pin;
            }
        }
        return null;
    }

    @Override
    public void paintComponent(Graphics g) {
        paintComponent(g, true);
    }

    public void paintComponent(Graphics g, boolean full) {
        final Rectangle clipBounds = g.getClipBounds();
        final int clipBoundsMaxX = clipBounds.x + clipBounds.width;
        final int clipBoundsMaxY = clipBounds.y + clipBounds.height;

        // Background
        g.setColor(new Color(252, 252, 252));
        g.fillRect(clipBounds.x, clipBounds.y, clipBounds.width, clipBounds.height);
        g.setColor(new Color(230, 240, 252));
        if (full) {
            // Horizontal lines
            for (int y = 0; y < clipBoundsMaxY; y += 20) {
                if (y < clipBounds.y) {
                    continue;
                }
                g.drawLine(clipBounds.x, y, clipBoundsMaxX, y);
            }

            // Vertical lines
            for (int x = 0; x < clipBoundsMaxX; x += 20) {
                if (x < clipBounds.x) {
                    continue;
                }
                g.drawLine(x, clipBounds.y, x, clipBoundsMaxY);
            }

            // Cross
            g.setColor(new Color(220, 230, 240));
            for (int y = 0; y < clipBoundsMaxY; y += 20) {
                if (y < clipBounds.y) {
                    continue;
                }

                for (int x = 0; x < clipBoundsMaxX; x += 20) {
                    g.drawLine(x - 2, y, x + 2, y);
                    g.drawLine(x, y - 2, x, y + 2);
                }
            }
        }
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        // Lanes
        for (Lane lane : this.graph.getLanes()) {

            g2.setStroke(new BasicStroke(4f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[] { 30, 10 }, 0));
            g.setColor(lane.getColor());
            g.drawLine(0, lane.getY() + 2, clipBoundsMaxX, lane.getY() + 2);
            g.drawLine(0, lane.getY() + lane.getHeight() - 2, clipBoundsMaxX, lane.getY() + lane.getHeight() - 2);

            String str = lane.getName();
            g.setFont(this.fontLane);
            Rectangle2D rect = g.getFontMetrics().getStringBounds(str, g);
            int stringWidth = (int) rect.getWidth();
            int stringHeight = (int) rect.getHeight();

            g.fillRect(4, lane.getY() + 7, 20 + stringWidth, stringHeight + 4);
            g.setColor(Color.WHITE);
            g.drawString(str, 10 + 4, lane.getY() + stringHeight + 4);

        }
        // Glue
        if (full) {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
            g2.setStroke(new BasicStroke(2f));
            if (this.highlightGlueX > Integer.MIN_VALUE) {
                g.setColor(new Color(120, 185, 240));
                g.drawLine(this.highlightGlueX, 0, this.highlightGlueX, this.getHeight());
            }
            if (this.highlightGlueY > Integer.MIN_VALUE) {
                g.setColor(new Color(120, 185, 240));
                g.drawLine(0, this.highlightGlueY, this.getWidth(), this.highlightGlueY);
            }
        }
        // Nodes & Links
        g.setFont(this.fontNode);

        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        for (VNode node : this.vNodes) {
            node.paint(g, this.selectedNodes.contains(node), this.selectedPins, full);
        }
        for (VLink link : this.vLinks) {
            link.paint(g, full);
        }
        if (this.temporyLink != null) {
            this.temporyLink.paint(g, full);
        }

    }

    public void registerNodeEditor(Class<? extends Node> node, Class<? extends NodeEditor> e) {
        this.editor.put(node, e);
    }

    public Class<? extends NodeEditor> getEditor(Class<? extends Node> node) {
        return this.editor.get(node);
    }

    public void addNodeSelectionListener(NodeSelectionListener nodeSelectionListener) {
        this.nListeners.add(nodeSelectionListener);
    }

    @Override
    public boolean isFocusable() {
        return true;
    }

    public void createCheckPoint() {
        final Set<Node> nodes = new HashSet<>(this.selectedNodes.size() * 2);
        for (VNode vn : this.selectedNodes) {
            nodes.add(vn.getNode());
        }
        try {
            this.history.addCheckPoint(new CheckPoint(this.graph, this.selectedPins, nodes));
        } catch (IOException e1) {
            e1.printStackTrace();
        }

    }

    public EventGraph getGraph() {
        return this.graph;

    }

    @Override
    public Dimension getPreferredScrollableViewportSize() {
        return this.getPreferredSize();
    }

    @Override
    public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
        return 48;
    }

    @Override
    public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
        return 48;
    }

    @Override
    public boolean getScrollableTracksViewportWidth() {
        return false;
    }

    @Override
    public boolean getScrollableTracksViewportHeight() {
        return false;
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(4000, 2472);
    }

    @Override
    public Dimension getMinimumSize() {
        return new Dimension(4000, 2472);
    }

    public void addGraphChangeListener(GraphChangeListener graphChangeListener) {
        this.graphChangeListeners.add(graphChangeListener);
    }

    public void fireGraphChange() {
        for (GraphChangeListener l : this.graphChangeListeners) {
            l.graphChanged();
        }

    }

    private void fireNodeSelected(Node node) {
        for (NodeSelectionListener l : this.nListeners) {
            l.nodeSelected(node);
        }
    }

    private void fireLaneSelected(Lane lane) {
        for (NodeSelectionListener l : this.nListeners) {
            l.laneSelected(lane);
        }
    }

    public void dump(PrintStream out) {

        out.println(this.vNodes.size() + " vNodes :");
        for (VNode n : this.vNodes) {
            out.println(n);
        }
        out.println(this.vLinks.size() + " vLinks :");
        for (VLink l : this.vLinks) {
            out.println(l);
        }

    }
}
