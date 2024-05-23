package com.jvisualscripting.editor;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.CubicCurve2D;

import com.jvisualscripting.ExecutionPin;
import com.jvisualscripting.Link;
import com.jvisualscripting.Node;
import com.jvisualscripting.Pin;
import com.jvisualscripting.variable.BooleanPin;
import com.jvisualscripting.variable.IntegerPin;
import com.jvisualscripting.variable.StringPin;

public class VLink {

    private Link link;
    private boolean selected;
    private boolean enabled = true;
    private static final Color NICE_BLACK = Color.decode("#001219");
    public static final Color COLOR_INTEGER = Color.decode("#0A9396");
    public static final Color COLOR_BOOLEAN = Color.decode("#9b2226");
    public static final Color COLOR_STRING = Color.decode("#CA6702");

    public VLink(Link link) {
        this.link = link;
    }

    public static Color getPinColor(Pin n) {
        Color color = Color.CYAN;
        if (n instanceof ExecutionPin) {
            color = NICE_BLACK;
        } else if (n instanceof StringPin) {
            color = COLOR_STRING;
        } else if (n instanceof IntegerPin) {
            color = COLOR_INTEGER;
        } else if (n instanceof BooleanPin) {
            color = COLOR_BOOLEAN;
        }
        return color;
    }

    public void paint(Graphics g, boolean full) {
        final Graphics2D g2 = (Graphics2D) g;
        final Pin p1 = link.getFrom();
        final Pin p2 = link.getTo();
        final Node node1 = p1.getNode();
        final int x1 = node1.getX(p1);
        final int y1 = node1.getY(p1);
        final Node node2 = p2.getNode();
        final int x2 = node2.getX(p2);
        final int y2 = node2.getY(p2);

        int x = Math.min(x1, x2) - 50;
        int y = Math.min(y1, y2) - 50;
        int width = Math.abs(x2 - x1) + 100;
        int height = Math.abs(y2 - y1) + 100;
        if (!g.getClipBounds().intersects(x, y, width, height)) {
            return;
        }

        // Highlight
        if (selected) {
            g2.setColor(VNode.SELECTION_COLOR);
            if (p1 instanceof ExecutionPin) {
                g2.setStroke(new BasicStroke(5f));
            } else {
                g2.setStroke(new BasicStroke(9f));
            }
            draw(g2, x1, y1, x2, y2);
        }
        // Link
        Color linkColor = getPinColor(p1);
        if (p1 instanceof TempPin)
            linkColor = getPinColor(p2);
        if (enabled) {
            g.setColor(linkColor);
        } else {
            g.setColor(Color.LIGHT_GRAY);
        }

        if (p1 instanceof ExecutionPin || p2 instanceof ExecutionPin) {
            g2.setStroke(new BasicStroke(3f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[] { 9 }, 0));
        } else {
            g2.setStroke(new BasicStroke(3f));
        }
        draw(g2, x1 + 7, y1, x2 - 7, y2);
        g2.setStroke(new BasicStroke(3f));
        if (p1 instanceof TempPin) {
            g.fillOval(x1 - 4, y1 - 4, 9, 9);
        }
        if (p2 instanceof TempPin) {
            g.fillOval(x2 - 4, y2 - 4, 9, 9);
        }
    }

    private void draw(Graphics2D g2, int x1, int y1, int x2, int y2) {
        if (x1 == x2 || y1 == y2) {
            g2.drawLine(x1, y1, x2, y2);
        } else {
            final CubicCurve2D c = new CubicCurve2D.Double();
            double e = ((x2 - x1) / 1.5f);
            if (e < 0) {
                e = e * (-4f);
                if (e > 100)
                    e = 100;
            }
            final double ctrlx1 = x1 + e;
            final double ctrly1 = y1;
            final double ctrlx2 = x2 - e;
            final double ctrly2 = y2;
            c.setCurve(x1, y1, ctrlx1, ctrly1, ctrlx2, ctrly2, x2, y2);
            g2.draw(c);
        }
    }

    public Link getLink() {
        return link;
    }

    public void setEnabled(boolean b) {
        this.enabled = b;
        // link.getFrom().setEnabled(b);
        // link.getTo().setEnabled(b);
    }

    @Override
    public String toString() {
        return super.toString() + " : " + link + ", enabled:" + this.enabled + ", selected:" + selected;
    }

}
