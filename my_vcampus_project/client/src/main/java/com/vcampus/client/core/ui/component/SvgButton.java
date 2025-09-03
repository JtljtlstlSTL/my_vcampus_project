// Java
package com.vcampus.client.core.ui.component;

import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.ImageTranscoder;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.InputStream;

// Java
public class SvgButton extends JButton {
    private String svgPath;

    public SvgButton(String svgPath) {
        this.svgPath = svgPath;
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        try (InputStream is = getClass().getResourceAsStream(svgPath)) {
            if (is != null) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                Image svgImage = renderSvgToImage(is, getWidth(), getHeight());
                g2d.drawImage(svgImage, 0, 0, getWidth(), getHeight(), null);
                g2d.dispose();
            } else {
                setText("?");
            }
        } catch (Exception e) {
            setText("?");
        }
    }

    private Image renderSvgToImage(InputStream svgStream, int width, int height) throws Exception {
        final Image[] image = new Image[1];
        ImageTranscoder t = new ImageTranscoder() {
            @Override
            public BufferedImage createImage(int w, int h) {
                BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
                Graphics2D g2d = img.createGraphics();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                g2d.dispose();
                return img;
            }
            @Override
            public void writeImage(BufferedImage img, TranscoderOutput out) {
                image[0] = img;
            }
        };
        t.addTranscodingHint(ImageTranscoder.KEY_WIDTH, (float) width);
        t.addTranscodingHint(ImageTranscoder.KEY_HEIGHT, (float) height);
        t.transcode(new TranscoderInput(svgStream), null);
        return image[0];
    }
    // SvgButton.java
    public void setSvgIcon(String svgPath) {
        this.svgPath = svgPath;
        repaint();
    }
}