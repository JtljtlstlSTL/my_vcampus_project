// Java
package com.vcampus.client.core.ui.component;

import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.ImageTranscoder;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import lombok.extern.slf4j.Slf4j;

// Java
@Slf4j
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
                if (svgImage == null) {
                    // 透明占位，避免抛出异常导致整个UI卡死
                    svgImage = new BufferedImage(Math.max(1, getWidth()), Math.max(1, getHeight()), BufferedImage.TYPE_INT_ARGB);
                }
                 g2d.drawImage(svgImage, 0, 0, getWidth(), getHeight(), null);
                 g2d.dispose();
             } else {
                 setText("?");
             }
         } catch (Exception e) {
            // 记录错误并使用占位图标，不中断UI渲染
            log.warn("SVG 渲染失败: {}", svgPath, e);
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
        // 读取 SVG 内容，先做简单清理以避免 Batik 在解析 CSS 时抛出 DOM 异常（例如 fill="" 或 fill:0）
         try {
            byte[] raw = svgStream.readAllBytes();
            String svg = new String(raw, StandardCharsets.UTF_8);
            // 替换空的 fill 属性和不合法的 fill 值
            svg = svg.replaceAll("fill\\s*=\\s*\"\\s*\"", "fill=\"none\"");
            svg = svg.replaceAll("fill\\s*:\\s*0", "fill:none");
            ByteArrayInputStream cleaned = new ByteArrayInputStream(svg.getBytes(StandardCharsets.UTF_8));
            t.transcode(new TranscoderInput(cleaned), null);
         } catch (org.w3c.dom.DOMException domEx) {
            // 记录并尝试直接传入原始流作为最后手段
            log.warn("Batik 解析 SVG 时遇到 DOMException，尝试回退: {}", svgPath, domEx);
            try {
                svgStream.reset();
            } catch (Exception ignored) {
            }
            try {
                t.transcode(new TranscoderInput(svgStream), null);
            } catch (Exception ex) {
                log.warn("回退转码仍失败: {}", svgPath, ex);
                // 返回 null 由调用方处理
                return null;
            }
         } catch (Exception e) {
            // 记录其他异常并返回 null
            log.warn("转码 SVG 出错: {}", svgPath, e);
            return null;
         }
         return image[0];
     }
     // SvgButton.java
     public void setSvgIcon(String svgPath) {
         this.svgPath = svgPath;
         repaint();
     }
 }
