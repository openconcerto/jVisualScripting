package com.jvisualscripting.editor;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.awt.image.VolatileImage;

import javax.swing.ImageIcon;

import com.jvisualscripting.editor.FillMode.ZoomIn;
import com.jvisualscripting.editor.FillMode.ZoomOut;

/**
 * Tools for Image manipulation
 */
public class ImageUtils {

    ImageUtils() {
        // Nothing
    }

    /**
     * Create a resized Image with high quality rendering, specifying a ratio rather than pixels.
     * 
     * @param c the component providing the Image
     * @param orginalImage the original Image
     * @param finalRatio the ratio that the returned image will have.
     * @param applySoftFilter soft filter
     * @param bgColor the background color
     * @param fast algorithm to use for resampling.
     * @return the resized image of the given ratio.
     */
    public static Image createQualityResizedImage(Component c, Image orginalImage, float finalRatio, boolean applySoftFilter, Color bgColor, boolean fast) {
        int w = orginalImage.getWidth(null);
        int h = orginalImage.getHeight(null);
        float imageRatio = w / (float) h;
        final int newWidth;
        final int newHeight;
        if (finalRatio > imageRatio) {
            newHeight = h;
            newWidth = (int) (newHeight * finalRatio);
        } else {
            newWidth = w;
            newHeight = (int) (newWidth / finalRatio);
        }
        return createQualityResizedImage(c, orginalImage, newWidth, newHeight, false, true, bgColor, fast);
    }

    /**
     * Create a resized Image with high quality rendering
     * 
     * @param c the component providing the Image
     * @param orginalImage the original Image
     * @param width the desired width
     * @param height the desired heights
     * @param applySoftFilter soft filter
     * @param keepRatio <code>true</code> to keep the ratio
     * @param bgColor the background color
     * @param fast algorithm to use for resampling.
     * @return the resized image of the given size
     */
    public static Image createQualityResizedImage(Component c, Image orginalImage, int width, int height, boolean applySoftFilter, boolean keepRatio, Color bgColor, boolean fast) {
        return createQualityResizedImage(c, orginalImage, width, height, applySoftFilter, keepRatio ? new ZoomOut(Color.BLACK) : FillMode.STRETCH, fast);
    }

    public static Image createQualityResizedImage(Component c, Image orginalImage, int width, int height, boolean applySoftFilter, FillMode fillMode, boolean fast) {
        if (orginalImage == null) {
            throw new IllegalArgumentException("null argument");
        }

        final VolatileImage bufferedImage = c.createVolatileImage(width, height);
        Graphics2D g2 = bufferedImage.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        int quality = Image.SCALE_SMOOTH;
        if (fast) {
            quality = Image.SCALE_FAST;
        }

        if (!fillMode.isRatioKept()) {
            Image resizedImage = orginalImage.getScaledInstance(width, height, quality);
            // This code ensures that all the pixels in the image are loaded:
            // NE PAS VIRER, deja trop de temps perdu a debugger ca!
            Image temp = new ImageIcon(resizedImage).getImage();
            g2.drawImage(temp, 0, 0, null);
        } else {
            float finalW = width;
            float finalH = height;
            final int w = orginalImage.getWidth(null);
            final int h = orginalImage.getHeight(null);
            float imageRatio = w / (float) h;
            float finalRatio = finalW / finalH;

            if (fillMode instanceof ZoomOut) {
                // Clear background and paint the image.
                g2.setColor(((ZoomOut) fillMode).getBackgroundColor());
                g2.fillRect(0, 0, width, height);

                int newH;
                int newW;
                if (finalRatio > imageRatio) {
                    newW = Math.round(finalH * imageRatio);
                    newH = Math.round(finalH);
                    Image resizedImage = orginalImage.getScaledInstance(newW, newH, quality);
                    // This code ensures that all the pixels in the image are loaded:
                    // NE PAS VIRER, deja trop de temps perdu a debugger ca!
                    Image temp = new ImageIcon(resizedImage).getImage();
                    g2.drawImage(temp, (int) ((finalW - newW) / 2), 0, null);
                } else {
                    newW = Math.round(finalW);
                    newH = Math.round(finalW / imageRatio);
                    Image resizedImage = orginalImage.getScaledInstance(newW, newH, quality);
                    // This code ensures that all the pixels in the image are loaded:
                    // NE PAS VIRER, deja trop de temps perdu a debugger ca!
                    Image temp = new ImageIcon(resizedImage).getImage();
                    g2.drawImage(temp, 0, (int) ((finalH - newH) / 2), null);
                }
            } else if (fillMode instanceof ZoomIn) {
                final ZoomIn zoomIn = (ZoomIn) fillMode;
                if (finalRatio > imageRatio) {
                    final int clippedH = Math.round(w / finalRatio);
                    final int sy1 = zoomIn.getPosition(h, clippedH);
                    final int sy2 = sy1 + clippedH;
                    g2.drawImage(orginalImage, 0, 0, width, height, 0, sy1, w, sy2, null);
                } else {
                    final int clippedW = Math.round(h * finalRatio);
                    final int sx1 = zoomIn.getPosition(w, clippedW);
                    final int sx2 = sx1 + clippedW;
                    g2.drawImage(orginalImage, 0, 0, width, height, sx1, 0, sx2, h, null);
                }
            }

        }

        g2.dispose();

        if (applySoftFilter) {
            return getSoftFilteredImage(bufferedImage.getSnapshot());
        }

        return bufferedImage;
    }

    /**
     * Create an soft filtered Image
     * 
     * @param bufferedImage the orignial BufferedImage
     * @return the soft filtered Image
     */
    public static BufferedImage getSoftFilteredImage(BufferedImage bufferedImage) {
        // soften

        float softenFactor = 0.01f;
        float[] softenArray = { 0, softenFactor, 0, softenFactor, 1 - (softenFactor * 3), softenFactor, 0, softenFactor, 0 };
        Kernel kernel = new Kernel(3, 3, softenArray);
        ConvolveOp cOp = new ConvolveOp(kernel, ConvolveOp.EDGE_NO_OP, null);
        bufferedImage = cOp.filter(bufferedImage, null);

        return bufferedImage;
    }

}
