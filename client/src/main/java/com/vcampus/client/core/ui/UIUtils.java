// 新增文件，提供选择 emoji 支持字体的辅助方法
package com.vcampus.client.core.ui;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.PNGTranscoder;

public class UIUtils {
    // 常见 emoji 字体优先列表（跨平台）
    private static final String[] EMOJI_FONTS = new String[]{
            "Segoe UI Emoji", // Windows
            "Noto Color Emoji", // Linux (Google)
            "Apple Color Emoji", // macOS
            "EmojiOne Color",
            "Twitter Color Emoji",
            "Segoe UI Symbol",
            "Microsoft YaHei"
    };

    public static Font getEmojiCapableFont(int style, int size) {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        String[] available = ge.getAvailableFontFamilyNames();
        java.util.Set<String> set = new java.util.HashSet<>();
        for (String s : available) set.add(s);
        for (String f : EMOJI_FONTS) {
            if (set.contains(f)) return new Font(f, style, size);
        }
        // 兜底：使用系统默认逻辑字体（让 JVM 自行做回退）
        return new Font(Font.DIALOG, style, size);
    }

    // 如果系统字体支持 emoji，则返回 emoji 字符，否则返回回退文本
    public static String getEmojiOrFallback(String emoji, String fallback) {
        if (emoji == null || emoji.isEmpty()) return fallback;
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        String[] available = ge.getAvailableFontFamilyNames();
        java.util.Set<String> set = new java.util.HashSet<>();
        for (String s : available) set.add(s);
        // 尝试优先字体并检查它们是否能显示 emoji
        for (String f : EMOJI_FONTS) {
            if (!set.contains(f)) continue;
            Font font = new Font(f, Font.PLAIN, 24);
            try {
                if (font.canDisplayUpTo(emoji) == -1) return emoji;
            } catch (Exception ignored) {}
        }
        // 如果没有已知字体支持，尝试系统默认字体
        Font def = new Font(Font.DIALOG, Font.PLAIN, 24);
        try { if (def.canDisplayUpTo(emoji) == -1) return emoji; } catch (Exception ignored) {}
        return fallback;
    }

    // 尝试从 classpath 加载图标资源并缩放到指定大小，找不到返回 null
    public static javax.swing.ImageIcon loadIcon(String resourcePath, int width, int height) {
        try {
            if (resourcePath == null) return null;
            // try SVG first
            if (resourcePath.toLowerCase().endsWith(".svg")) {
                InputStream is = UIUtils.class.getResourceAsStream(resourcePath);
                if (is == null) return null;
                PNGTranscoder t = new PNGTranscoder();
                if (width > 0 && height > 0) {
                    t.addTranscodingHint(PNGTranscoder.KEY_WIDTH, (float) width);
                    t.addTranscodingHint(PNGTranscoder.KEY_HEIGHT, (float) height);
                }
                TranscoderInput input = new TranscoderInput(is);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                TranscoderOutput output = new TranscoderOutput(baos);
                t.transcode(input, output);
                baos.flush();
                byte[] pngData = baos.toByteArray();
                baos.close();
                javax.swing.ImageIcon icon = new javax.swing.ImageIcon(pngData);
                return icon;
            }

            java.net.URL url = UIUtils.class.getResource(resourcePath);
            // 若直接用 class resource 找不到，尝试线程上下文类加载器（不带前导 '/'）
            if (url == null) {
                try {
                    String p = resourcePath.startsWith("/") ? resourcePath.substring(1) : resourcePath;
                    java.net.URL u2 = Thread.currentThread().getContextClassLoader().getResource(p);
                    if (u2 != null) url = u2;
                } catch (Exception ignored) {}
            }
            // 回退：在开发环境从文件系统加载（相对项目根），方便本地运行时资源放在 client/src/main/resources 下
            if (url == null) {
                try {
                    java.io.File f1 = new java.io.File("client/src/main/resources" + (resourcePath.startsWith("/") ? resourcePath : "/" + resourcePath));
                    if (f1.exists()) url = f1.toURI().toURL();
                    else {
                        java.io.File f2 = new java.io.File(resourcePath);
                        if (f2.exists()) url = f2.toURI().toURL();
                    }
                } catch (Exception ignored) {}
            }
             if (url == null) return null;
            javax.swing.ImageIcon icon = new javax.swing.ImageIcon(url);
            if (width > 0 && height > 0) {
                java.awt.Image img = icon.getImage().getScaledInstance(width, height, java.awt.Image.SCALE_SMOOTH);
                return new javax.swing.ImageIcon(img);
            }
            return icon;
        } catch (Exception e) {
            return null;
        }
    }

    // 将 emoji 文本渲染为 ImageIcon，避免不同平台彩色 emoji 字体渲染不全的问题
    public static javax.swing.ImageIcon renderEmojiAsIcon(String emoji, int size) {
        if (emoji == null || emoji.isEmpty()) return null;
        try {
            int imgSize = Math.max(16, size);
            java.awt.image.BufferedImage bi = new java.awt.image.BufferedImage(imgSize, imgSize, java.awt.image.BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = bi.createGraphics();
            try {
                g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g.setComposite(AlphaComposite.Src);
                g.setColor(new Color(0,0,0,0));
                g.fillRect(0,0,imgSize,imgSize);

                // 更宽裕的字体比例，避免彩色 emoji 被裁切
                int fontSize = Math.max(12, (int)(imgSize * 0.9));
                Font font = getEmojiCapableFont(Font.PLAIN, fontSize);
                g.setFont(font);

                java.awt.font.FontRenderContext frc = g.getFontRenderContext();
                java.awt.font.TextLayout tl = new java.awt.font.TextLayout(emoji, font, frc);
                Rectangle bounds = tl.getPixelBounds(null, 0, 0);
                int textWidth = bounds.width;
                int textHeight = bounds.height;
                int x = (imgSize - textWidth) / 2 - bounds.x; // bounds.x may be negative
                int y = (imgSize - textHeight) / 2 - bounds.y;

                g.setComposite(AlphaComposite.SrcOver);
                // 使用默认前景色，彩色 emoji 字体会输出彩色
                g.setColor(Color.BLACK);
                tl.draw(g, x, y);
            } finally {
                g.dispose();
            }
            return new javax.swing.ImageIcon(bi);
        } catch (Exception e) {
            return null;
        }
    }

    // 根据商品分类返回首选图标资源（如果存在），否则返回 null。资源位于 /figures/icons/ 下，文件名以小写分类为准
    public static javax.swing.ImageIcon getIconForCategory(String category, int size) {
        if (category == null) return null;
        String cat = category.toLowerCase();
        String[] candidates;
        if (cat.contains("book") || cat.contains("书")) candidates = new String[]{"/figures/icons/book.png","/figures/icons/book.svg"};
        else if (cat.contains("food") || cat.contains("食")) candidates = new String[]{"/figures/icons/food.png","/figures/icons/food.svg"};
        else if (cat.contains("elect") || cat.contains("数")) candidates = new String[]{"/figures/icons/elect.png","/figures/icons/elect.svg"};
        else if (cat.contains("cloth") || cat.contains("衣") || cat.contains("服")) candidates = new String[]{"/figures/icons/cloth.png","/figures/icons/cloth.svg"};
        else if (cat.contains("game") || cat.contains("游")) candidates = new String[]{"/figures/icons/game.png","/figures/icons/game.svg"};
        else candidates = new String[]{"/figures/icons/item.png","/figures/icons/product.png"};

        for (String c : candidates) {
            javax.swing.ImageIcon ic = loadIcon(c, size, size);
            if (ic != null) return ic;
        }
        return null;
    }

    // 返回缺省商品图片（用于找不到实际图片时展示）
    public static java.awt.Image getDefaultProductImage() {
        try {
            javax.swing.ImageIcon ic = loadIcon("/shop/default.png", 520, 520);
            if (ic != null) return ic.getImage();
        } catch (Exception ignored) {}
        // 动态生成一个简易占位图
        int w = 520, h = 520;
        BufferedImage bi = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = bi.createGraphics();
        try {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(new Color(245, 247, 250));
            g.fillRect(0,0,w,h);
            g.setColor(new Color(200, 208, 216));
            int pad = 40;
            g.fillRoundRect(pad, pad, w - pad*2, h - pad*2, 24, 24);
            g.setColor(new Color(120,125,140));
            Font f = new Font("微软雅黑", Font.PLAIN, 18);
            g.setFont(f);
            String t = "无图片";
            FontMetrics fm = g.getFontMetrics(f);
            int tx = (w - fm.stringWidth(t)) / 2;
            int ty = (h + fm.getAscent() - fm.getDescent()) / 2;
            g.drawString(t, tx, ty);
        } finally { g.dispose(); }
        return bi;
    }

}
