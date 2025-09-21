package com.vcampus.client.core.util;

import lombok.extern.slf4j.Slf4j;

import java.awt.*;
import java.io.InputStream;

/**
 * 字体管理工具类
 * 负责加载和管理自定义字体
 */
@Slf4j
public class FontManager {

    private static Font customFont;
    private static final String CUSTOM_FONT_PATH = "/fonts/MyFont.ttf";

    static {
        loadCustomFont();
    }

    /**
     * 加载自定义字体
     */
    private static void loadCustomFont() {
        try {
            InputStream fontStream = FontManager.class.getResourceAsStream(CUSTOM_FONT_PATH);
            if (fontStream != null) {
                customFont = Font.createFont(Font.TRUETYPE_FONT, fontStream);
                GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
                ge.registerFont(customFont);
                log.info("自定义字体加载成功: {}", customFont.getFontName());
            } else {
                log.warn("找不到自定义字体文件: {}", CUSTOM_FONT_PATH);
                customFont = new Font("微软雅黑", Font.PLAIN, 12); // 回退到默认字体
            }
        } catch (Exception e) {
            log.error("加载自定义字体失败", e);
            customFont = new Font("微软雅黑", Font.PLAIN, 12); // 回退到默认字体
        }
    }

    /**
     * 获取自定义字体
     * @param style 字体样式 (Font.PLAIN, Font.BOLD, Font.ITALIC)
     * @param size 字体大小
     * @return Font对象
     */
    public static Font getCustomFont(int style, float size) {
        if (customFont != null) {
            return customFont.deriveFont(style, size);
        }
        return new Font("微软雅黑", style, (int)size);
    }

    /**
     * 获取标题字体 (大号、粗体)
     * @param size 字体大小
     * @return Font对象
     */
    public static Font getTitleFont(float size) {
        return getCustomFont(Font.BOLD, size);
    }

    /**
     * 获取中等粗细的标题字体 (适合门户标题)
     * @param size 字体大小
     * @return Font对象
     */
    public static Font getMediumTitleFont(float size) {
        return getCustomFont(Font.PLAIN, size);
    }

    /**
     * 获取普通文本字体
     * @param size 字体大小
     * @return Font对象
     */
    public static Font getRegularFont(float size) {
        return getCustomFont(Font.PLAIN, size);
    }

    /**
     * 获取按钮字体 (中等粗细)
     * @param size 字体大小
     * @return Font对象
     */
    public static Font getButtonFont(float size) {
        return getCustomFont(Font.BOLD, size);
    }

    /**
     * 获取标签字体
     * @param size 字体大小
     * @return Font对象
     */
    public static Font getLabelFont(float size) {
        return getCustomFont(Font.PLAIN, size);
    }

    /**
     * 检查自定义字体是否成功加载
     * @return true如果自定义字体已加载，false如果使用回退字体
     */
    public static boolean isCustomFontLoaded() {
        return customFont != null && !customFont.getFontName().equals("微软雅黑");
    }

    /**
     * 获取当前使用的字体名称
     * @return 字体名称
     */
    public static String getCurrentFontName() {
        return customFont != null ? customFont.getFontName() : "微软雅黑";
    }
}
