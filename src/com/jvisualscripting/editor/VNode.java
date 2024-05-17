package com.jvisualscripting.editor;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.Set;

import com.jvisualscripting.ExecutionPin;
import com.jvisualscripting.FlowNode;
import com.jvisualscripting.Node;
import com.jvisualscripting.Pin;
import com.jvisualscripting.event.EventNode;
import com.jvisualscripting.variable.StringVariable;

public class VNode {

    private static final Color DEFAULT_COLOR = Color.GRAY;
    // https://coolors.co/palette/001219-005f73-0a9396-94d2bd-e9d8a6-ee9b00-ca6702-bb3e03-ae2012-9b2226
    public static final Color SELECTION_COLOR = new Color(255, 200, 20, 100);

    public static final Color NODE_COLOR_STRING = Color.decode("#CA6702");// Color.decode("#bb3e03");
    public static final Color NODE_COLOR_EVENT = Color.decode("#0a9396");
    public static final Color NODE_COLOR_FUNCTION_EXEC = Color.decode("#005f73");
    public static final Color NODE_COLOR_FUNCTION = Color.decode("#006f73");

    private static final int PIN_DISTANCE = 20;
    private static final int PIN_WIDTH = 15;
    private Node node;
    private Color color;

    public VNode(Node n) {
        this.node = n;

        color = getNodeColor(n);

    }

    public static Color getNodeColor(Node n) {
        Color color = NODE_COLOR_FUNCTION;
        if (n instanceof EventNode) {
            color = NODE_COLOR_EVENT;
        } else if (n instanceof StringVariable) {
            color = NODE_COLOR_STRING;
        } else if (n instanceof FlowNode) {
            color = NODE_COLOR_FUNCTION_EXEC;
        }
        return color;
    }

    public Color getColor() {
        return color;
    }

    public void paint(Graphics g, boolean selected, Set<Pin> selectedPins) {

        int x = node.getX();
        int y = node.getY();
        int width = this.node.getWidth() + 1;
        int height = this.node.getHeight();
        if (!g.getClipBounds().intersects(x - 20, y - 20, width + 40, height + 40)) {
            return;
        }

        Graphics2D g2 = (Graphics2D) g;
        g.setFont(g.getFont().deriveFont(Font.BOLD, 13));
        String name = this.node.getName();
        // Bordures
        if (node.getInputSize() + node.getOutputSize() > 0) {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
            g2.setStroke(new BasicStroke(2f));

            g.setColor(getColor());

        }
        // BG

        // Shadow
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        if (selected) {
            g.setColor(SELECTION_COLOR);
        } else {
            if (node.isBlocked()) {
                g.setColor(Color.RED);
            } else if (node.isActive()) {
                g.setColor(Color.GREEN);
            } else {
                g.setColor(new Color(0, 0, 0, 20));
            }
        }
        g2.fillRoundRect(x - 5, y + 1 + -5, width + 10, height - 1 + 10, 7, 7);

        // Background
        g.setColor(getDarkColor());
        g2.fillRoundRect(x, y + 1, width, height - 1, 7, 7);
        g.setColor(this.getColor());
        g2.fillRoundRect(x, y + 1, width, height - 1 - 10, 7, 7);
        g.setColor(this.getBrightColor());
        g2.fillRoundRect(x, y + 1, width, 20 + 5, 7, 7);

        // Name
        final int BORDER = 4;
        g.setColor(new Color(255, 255, 255, 255));
        g.drawString(name, x + BORDER + 5, y + 13 + 5);

        // Pins

        int y2 = getFirstPinY(y);
        g2.setStroke(new BasicStroke(3f));
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);

        g.setFont(g.getFont().deriveFont(Font.PLAIN, 13));

        if (node.getInputSize() > 0) {
            for (Pin p : node.getInputs()) {
                if (selectedPins.contains(p)) {

                    g.setColor(SELECTION_COLOR);

                    g2.setStroke(new BasicStroke(5f));
                    int d = 0;
                    if (p instanceof ExecutionPin) {
                        d = -2;
                    }
                    // g.drawOval(x - 15 - d, y2 + 3, 14, 14);
                    g.fillOval(x - 15 - d, y2 + 3, 14, 14);

                }
                g2.setStroke(new BasicStroke(3f));
                g.setColor(VLink.getPinColor(p));
                if (p instanceof ExecutionPin) {
                    //
                    int d = 16;
                    int[] xPoints = new int[] { x + 5 - d, x + 9 - d, x + 13 - d, x + 9 - d, x + 5 - d };
                    int[] yPoints = new int[] { y2 + 5, y2 + 5, y2 + 10, y2 + 15, y2 + 15 };
                    if (p.isConnected()) {
                        g.fillPolygon(xPoints, yPoints, 5);
                        g.fillRect(x - 16, y2 + 9, 6, 3);
                    }
                    g.drawPolygon(xPoints, yPoints, 5);

                } else {
                    if (p.isConnected()) {
                        g.fillOval(x - 13, y2 + 5, 10, 10);
                    }
                    g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
                    g.drawOval(x - 13, y2 + 5, 10, 10);
                    g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_DEFAULT);

                }
                g.setColor(Color.WHITE);
                g.drawString(p.getName(), x + 5, y2 + 15);
                y2 += PIN_DISTANCE;
            }
        }
        y2 = getFirstPinY(y);
        if (node.getOutputSize() > 0) {
            for (Pin p : node.getOutputs()) {
                int dx = x + width;
                if (selectedPins.contains(p)) {

                    g.setColor(SELECTION_COLOR);

                    g2.setStroke(new BasicStroke(5f));
                    int d = 0;
                    if (p instanceof ExecutionPin) {
                        d = 5;
                    }
                    g.drawOval(dx + 2 - d, y2 + 3, 14, 14);
                    g.fillOval(dx + 2 - d, y2 + 3, 14, 14);

                }
                g2.setStroke(new BasicStroke(3f));
                g.setColor(VLink.getPinColor(p));
                if (p instanceof ExecutionPin) {
                    int d = 5;
                    int[] xPoints = new int[] { dx + 7 - d, dx + 11 - d, dx + 15 - d, dx + 11 - d, dx + 7 - d };
                    int[] yPoints = new int[] { y2 + 5, y2 + 5, y2 + 10, y2 + 15, y2 + 15 };
                    if (p.isConnected()) {
                        g.fillPolygon(xPoints, yPoints, 5);
                    }
                    g.drawPolygon(xPoints, yPoints, 5);

                } else {

                    if (p.isConnected()) {
                        g.fillOval(dx + 5, y2 + 6, 9, 9);
                    }
                    g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
                    g.drawOval(dx + 5, y2 + 6, 9, 9);
                    g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_DEFAULT);

                }
                g.setColor(Color.WHITE);
                int strWidth = (int) g.getFontMetrics().getStringBounds(p.getName(), g).getWidth();
                g.drawString(p.getName(), dx - strWidth - 5, y2 + 15);
                y2 += 20;
            }
        }
        // g.setColor(new Color(168, 186, 234));
        // g2.setStroke(new BasicStroke(3f));
        // g.drawRect(x, y, width, height+20);

    }

    private int getFirstPinY(int y) {
        return y + 20 + 10;
    }

    private Color getBrightColor() {
        return brighter(getColor());
    }

    private Color getDarkColor() {
        Color c = getColor();
        float FACTOR = 0.95f;
        return new Color(Math.max((int) (c.getRed() * FACTOR), 0), Math.max((int) (c.getGreen() * FACTOR), 0), Math.max((int) (c.getBlue() * FACTOR), 0), c.getAlpha());

    }

    private Color brighter(Color color2) {
        int r = color2.getRed();
        int g = color2.getGreen();
        int b = color2.getBlue();
        int alpha = color2.getAlpha();
        float FACTOR = 0.95f;
        /*
         * From 2D group: 1. black.brighter() should return grey 2. applying brighter to blue will
         * always return blue, brighter 3. non pure color (non zero rgb) will eventually return
         * white
         */
        int i = (int) (1.0 / (1.0 - FACTOR));
        if (r == 0 && g == 0 && b == 0) {
            return new Color(i, i, i, alpha);
        }
        if (r > 0 && r < i)
            r = i;
        if (g > 0 && g < i)
            g = i;
        if (b > 0 && b < i)
            b = i;

        return new Color(Math.min((int) (r / FACTOR), 255), Math.min((int) (g / FACTOR), 255), Math.min((int) (b / FACTOR), 255), alpha);

    }

    public void setLocation(int x, int y) {
        this.node.setLocation((short) x, (short) y);

    }

    public boolean contains(int x, int y) {
        return x >= this.node.getX() && x <= this.node.getX() + this.node.getWidth() && y > this.node.getY() && y <= this.node.getY() + this.node.getHeight();
    }

    public Node getNode() {
        return node;
    }

    public Pin getPin(int x, int y) {
        if (y < this.node.getY() || y > this.node.getY() + this.node.getHeight()) {
            return null;
        }

        if (x < this.node.getX() && x > this.node.getX() - PIN_WIDTH) {
            // Input pin
            if (node.getInputSize() > 0) {
                int y2 = getFirstPinY(this.getNode().getY());
                for (Pin p : node.getInputs()) {
                    if (y > y2 && y < y2 + PIN_DISTANCE) {
                        return p;
                    }
                    y2 += PIN_DISTANCE;
                }
            }
        }
        if (x > this.node.getX() + this.node.getWidth() && x < this.node.getX() + this.node.getWidth() + PIN_WIDTH) {
            // Ouput pin
            if (node.getOutputSize() > 0) {
                int y2 = getFirstPinY(this.getNode().getY());
                for (Pin p : node.getOutputs()) {
                    if (y > y2 && y < y2 + PIN_DISTANCE) {
                        return p;
                    }
                    y2 += PIN_DISTANCE;
                }
            }
        }

        return null;
    }
}
