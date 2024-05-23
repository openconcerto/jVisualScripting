package com.jvisualscripting.editor;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

public class MiniViewPanel extends JPanel implements GraphChangeListener {

    private static final Color SHADOW_COLOR = new Color(100, 100, 100, 10);
    private EventGraphEditorPanel graphPanel;
    private Image imageOverlay;
    private Rectangle2D rect;
    private ExecutorService executor;
    private final SwingThrottle t;

    MiniViewPanel(EventGraphEditorPanel graph) {
        this.graphPanel = graph;
        graph.addGraphChangeListener(this);
        this.executor = Executors.newSingleThreadExecutor();
        this.t = new SwingThrottle(15, new Runnable() {

            @Override
            public void run() {
                computeBitmap();
            }
        });

        this.addComponentListener(new ComponentAdapter() {

            @Override
            public void componentResized(ComponentEvent e) {
                MiniViewPanel.this.t.execute();
            }

        });
        this.addMouseListener(new MouseAdapter() {

            @Override
            public void mousePressed(MouseEvent e) {
                relocateRect(e.getX(), e.getY());

            }

        });
        this.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                relocateRect(e.getX(), e.getY());
            }
        });
    }

    protected void relocateRect(int x, int y) {
        int width = this.getWidth();
        float r = ((float) width) / this.graphPanel.getWidth();
        final int w1 = (int) (this.rect.getWidth() / r);
        final int h1 = (int) (this.rect.getHeight() / r);
        final int x1 = (int) (x / r) - w1 / 2;
        final int y1 = (int) (y / r) - h1 / 2;

        this.graphPanel.scrollRect(new Rectangle(x1, y1, w1, h1));
    }

    @Override
    public void graphChanged() {
        this.t.execute();
    }

    float ra = 1f;

    private void computeBitmap() {
        int width = this.getWidth();
        int height = (this.getWidth() * this.graphPanel.getMinimumSize().height) / this.graphPanel.getMinimumSize().width;

        this.executor.submit(new Runnable() {

            @Override
            public void run() {

                long t1 = System.currentTimeMillis();

                final int imgWidth = (int) (width * MiniViewPanel.this.ra);
                final int imgHeight = (int) (height * MiniViewPanel.this.ra);
                Image i = createImage(imgWidth, imgHeight);
                Graphics2D g2 = (Graphics2D) i.getGraphics();

                g2.setClip(0, 0, imgWidth, imgHeight);
                float r = ((float) width) / MiniViewPanel.this.graphPanel.getMinimumSize().width;

                AffineTransform scalingTransform = AffineTransform.getScaleInstance(r * MiniViewPanel.this.ra, r * MiniViewPanel.this.ra);

                // Apply the transform to the graphics
                g2.transform(scalingTransform);

                MiniViewPanel.this.graphPanel.paintComponent(g2, false);
                g2.dispose();

                final Image i2 = ImageUtils.createQualityResizedImage(MiniViewPanel.this, i, width, height, false, true, Color.BLACK, false);
                long t3 = System.currentTimeMillis();
                final long delay = t3 - t1;
                if (delay >= MiniViewPanel.this.t.getDelay()) {
                    MiniViewPanel.this.t.setDelay(MiniViewPanel.this.t.getDelay() + 5);
                } else if (delay < MiniViewPanel.this.t.getDelay() - 5) {
                    MiniViewPanel.this.t.setDelay(MiniViewPanel.this.t.getDelay() - 1);
                }

                SwingUtilities.invokeLater(new Runnable() {

                    @Override
                    public void run() {
                        MiniViewPanel.this.imageOverlay = i2;
                        repaint();
                    }
                });

            }
        });

    }

    @Override
    public void paint(Graphics g) {
        g.setColor(Color.WHITE);

        g.fillRect(0, 0, this.getWidth(), this.getHeight());
        if (this.imageOverlay != null) {
            g.drawImage(this.imageOverlay, 0, 0, null);
        }
        if (this.rect != null) {
            g.setColor(SHADOW_COLOR);
            g.fillRect(0, 0, this.getWidth(), (int) this.rect.getMinY());
            final int rectHeight = (int) this.rect.getMaxY() - (int) this.rect.getMinY();
            g.fillRect(0, (int) this.rect.getMinY(), (int) this.rect.getMinX(), rectHeight);
            g.fillRect(0, (int) this.rect.getMaxY(), this.getWidth(), this.getHeight() - (int) this.rect.getMaxY());
            g.fillRect((int) this.rect.getMaxX(), (int) this.rect.getMinY(), (this.getWidth() - (int) this.rect.getMaxX()), rectHeight);
        }
    }

    public void setRect(int x, int y, int width, int height) {
        float r = ((float) this.getWidth()) / this.graphPanel.getWidth();
        float x2 = x * r;
        float y2 = y * r;
        float w2 = width * r;
        float h2 = height * r;

        if (x2 < 0)
            x2 = 0;
        if (y2 < 0)
            y2 = 0;

        this.rect = new Rectangle2D.Float(x2, y2, w2, h2);
        repaint();
    }

}
