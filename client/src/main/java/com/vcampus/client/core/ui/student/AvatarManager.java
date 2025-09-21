package com.vcampus.client.core.ui.student;

import com.vcampus.client.core.net.NettyClient;
import com.vcampus.common.message.Request;
import com.vcampus.common.message.Response;
import lombok.extern.slf4j.Slf4j;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * 头像管理器
 */
@Slf4j
public class AvatarManager {

    private final Map<String, Object> userData;
    private final NettyClient nettyClient;
    private String currentAvatarPath;

    public AvatarManager(Map<String, Object> userData) {
        this.userData = userData;
        this.nettyClient = null; // 兼容旧构造函数
    }

    public AvatarManager(Map<String, Object> userData, NettyClient nettyClient) {
        this.userData = userData;
        this.nettyClient = nettyClient;
    }

    /**
     * 加载用户头像
     */
    public void loadUserAvatar(JLabel avatarLabel, int width, int height) {
        try {
            // 尝试从用户数据中获取头像路径
            String avatarPath = getUserAvatarPath();

            if (avatarPath != null && !avatarPath.isEmpty()) {
                // 加载自定义头像
                loadCustomAvatar(avatarLabel, avatarPath, width, height);
            } else {
                // 加载默认头像
                loadDefaultAvatar(avatarLabel, width, height);
            }
        } catch (Exception e) {
            log.warn("加载头像失败，使用默认头像: " + e.getMessage());
            loadDefaultAvatar(avatarLabel, width, height);
        }
    }

    /**
     * 显示头像选择对话框
     */
    public void showAvatarSelectionDialog(JLabel sidebarAvatar, JLabel profileAvatar) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(sidebarAvatar), "更换头像", true);
        dialog.setSize(400, 300);
        dialog.setLocationRelativeTo(sidebarAvatar);
        dialog.setLayout(new BorderLayout());

        JPanel contentPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        // 当前头像显示
        JLabel currentAvatarLabel = new JLabel();
        currentAvatarLabel.setPreferredSize(new Dimension(100, 100));
        currentAvatarLabel.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        loadUserAvatar(currentAvatarLabel, 100, 100);

        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        contentPanel.add(new JLabel("当前头像:", SwingConstants.CENTER), gbc);
        gbc.gridy = 1;
        contentPanel.add(currentAvatarLabel, gbc);

        // 按钮
        gbc.gridy = 2; gbc.gridwidth = 1;
        JButton btnChooseFile = new JButton("选择文件");
        JButton btnUseDefault = new JButton("使用默认");

        gbc.gridx = 0;
        contentPanel.add(btnChooseFile, gbc);
        gbc.gridx = 1;
        contentPanel.add(btnUseDefault, gbc);

        dialog.add(contentPanel, BorderLayout.CENTER);

        // 确认取消按钮
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton btnOK = new JButton("确定");
        JButton btnCancel = new JButton("取消");
        buttonPanel.add(btnOK);
        buttonPanel.add(btnCancel);
        dialog.add(buttonPanel, BorderLayout.SOUTH);

        // 事件处理
        btnChooseFile.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileFilter(new FileNameExtensionFilter("图片文件", "jpg", "jpeg", "png", "gif"));

            if (fileChooser.showOpenDialog(dialog) == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile();
                currentAvatarPath = selectedFile.getAbsolutePath();
                loadCustomAvatar(currentAvatarLabel, currentAvatarPath, 100, 100);
            }
        });

        btnUseDefault.addActionListener(e -> {
            currentAvatarPath = null;
            loadDefaultAvatar(currentAvatarLabel, 100, 100);
        });

        btnOK.addActionListener(e -> {
            // 保存头像到服务器和本地
            if (currentAvatarPath != null) {
                saveAvatarToServer(currentAvatarPath, dialog, sidebarAvatar, profileAvatar);
            } else {
                resetToDefaultAvatar(dialog, sidebarAvatar, profileAvatar);
            }
            // 移除这里的同步更新代码
            dialog.dispose();
        });

        btnCancel.addActionListener(e -> dialog.dispose());

        dialog.setVisible(true);
    }

    /**
     * 加载自定义头像
     */
    private void loadCustomAvatar(JLabel avatarLabel, String avatarPath, int width, int height) {
        try {
            BufferedImage originalImage = ImageIO.read(new File(avatarPath));
            Image scaledImage = originalImage.getScaledInstance(width, height, Image.SCALE_SMOOTH);

            // 创建方形头像（与StaffFrame和AdminFrame保持一致）
            BufferedImage squareImage = createSquareImage(scaledImage, width, height);
            avatarLabel.setIcon(new ImageIcon(squareImage));
        } catch (Exception e) {
            log.warn("加载自定义头像失败: " + e.getMessage());
            loadDefaultAvatar(avatarLabel, width, height);
        }
    }

    /**
     * 加载默认头像
     */
    private void loadDefaultAvatar(JLabel avatarLabel, int width, int height) {
        String gender = getUserGender();
        ImageIcon defaultAvatar = createDefaultAvatar(width, height, gender);
        avatarLabel.setIcon(defaultAvatar);
    }

    /**
     * 创建方形图像（与StaffFrame和AdminFrame保持一致）
     */
    private BufferedImage createSquareImage(Image image, int width, int height) {
        BufferedImage squareImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = squareImage.createGraphics();

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // 直接绘制图像，不进行圆形剪切
        g2d.drawImage(image, 0, 0, width, height, null);

        g2d.dispose();
        return squareImage;
    }

    /**
     * 创建默认头像
     */
    private ImageIcon createDefaultAvatar(int width, int height, String gender) {
        try {
            // 尝试从资源加载默认头像
            String avatarResource = getDefaultAvatarResource(gender);
            InputStream imageStream = getClass().getResourceAsStream(avatarResource);
            if (imageStream != null) {
                BufferedImage originalImage = ImageIO.read(imageStream);
                Image scaledImage = originalImage.getScaledInstance(width, height, Image.SCALE_SMOOTH);
                return new ImageIcon(scaledImage);
            }
        } catch (Exception e) {
            log.warn("加载默认头像资源失败，使用生成的头像: " + e.getMessage());
        }

        // 生成默认头像
        return generateDefaultAvatar(width, height, gender);
    }

    /**
     * 生成默认头像
     */
    private ImageIcon generateDefaultAvatar(int width, int height, String gender) {
        Color backgroundColor, outlineColor;
        if ("女".equals(gender)) {
            backgroundColor = new Color(0xFF, 0xC1, 0xF9); // 浅粉色
            outlineColor = new Color(0xFF, 0, 0xFF);
        } else {
            backgroundColor = new Color(0xA5, 0xFF, 0xE2); // 浅青色背景
            outlineColor = new Color(0x1F, 0x6F, 0x85);
        }

        BufferedImage avatar = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = avatar.createGraphics();

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // 绘制圆形背景
        g2d.setColor(backgroundColor);
        g2d.fillOval(2, 2, width - 4, height - 4);

        // 绘制人像轮廓
        g2d.setColor(outlineColor);
        g2d.setStroke(new BasicStroke(2));

        // 绘制头部
        int headSize = width / 3;
        int headX = (width - headSize) / 2;
        int headY = height / 4;
        g2d.drawOval(headX, headY, headSize, headSize);

        // 绘制身体
        int bodyWidth = width / 2;
        int bodyHeight = height / 3;
        int bodyX = (width - bodyWidth) / 2;
        int bodyY = headY + headSize - 5;
        g2d.drawOval(bodyX, bodyY, bodyWidth, bodyHeight);

        g2d.dispose();
        return new ImageIcon(avatar);
    }

    /**
     * 获取用户头像路径
     */
    private String getUserAvatarPath() {
        // 优先从avatar字段获取（与StaffFrame和AdminFrame保持一致）
        if (userData != null && userData.containsKey("avatar")) {
            String avatarPath = userData.get("avatar").toString();
            if (avatarPath != null && !avatarPath.isEmpty()) {
                return avatarPath;
            }
        }
        // 兼容旧的avatarPath字段
        if (userData != null && userData.containsKey("avatarPath")) {
            return userData.get("avatarPath").toString();
        }
        return currentAvatarPath;
    }

    /**
     * 获取用户性别
     */
    private String getUserGender() {
        if (userData != null && userData.containsKey("gender")) {
            return userData.get("gender").toString();
        }
        return "男"; // 默认
    }

    /**
     * 获取默认头像资源路径
     */
    private String getDefaultAvatarResource(String gender) {
        String genderSuffix = "女".equals(gender) ? "_female" : "_male";
        return String.format("/figures/student%s.png", genderSuffix);
    }

    /**
     * 保存头像到服务器和本地文件系统
     */
    private void saveAvatarToServer(String avatarPath, JDialog dialog, JLabel sidebarAvatar, JLabel profileAvatar) {
        if (nettyClient == null) {
            log.warn("NettyClient未初始化，无法保存头像到服务器");
            return;
        }

        try {
            // 创建用户头像目录
            String userDir = System.getProperty("user.home");
            Path avatarDir = Paths.get(userDir, ".vcampus", "avatars");
            Files.createDirectories(avatarDir);

            // 生成新的文件名
            String cardNum = userData.get("cardNum").toString();
            String extension = avatarPath.substring(avatarPath.lastIndexOf('.'));
            String newFileName = "avatar_" + cardNum + "_" + System.currentTimeMillis() + extension;
            Path newAvatarPath = avatarDir.resolve(newFileName);

            // 复制文件到新位置
            Files.copy(Paths.get(avatarPath), newAvatarPath, StandardCopyOption.REPLACE_EXISTING);

            log.info("头像文件已复制到: {}", newAvatarPath.toString());

            // 更新头像到服务器
            updateAvatarOnServer(newAvatarPath.toString(), dialog, sidebarAvatar, profileAvatar);

        } catch (Exception e) {
            log.error("保存头像文件时发生错误", e);
            SwingUtilities.invokeLater(() -> 
                JOptionPane.showMessageDialog(dialog, "保存头像文件时发生错误：" + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE)
            );
        }
    }

    /**
     * 更新头像到服务器
     */
    private void updateAvatarOnServer(String avatarPath, JDialog dialog, JLabel sidebarAvatar, JLabel profileAvatar) {
        try {
            String cardNum = userData.get("cardNum").toString();
            
            log.info("发送头像更新请求 - 卡号: {}, 头像路径: {}", cardNum, avatarPath);
            
            Request request = new Request("auth/updateAvatar")
                    .addParam("cardNum", cardNum)
                    .addParam("avatar", avatarPath);

            CompletableFuture<Response> future = nettyClient.sendRequest(request);
            future.thenAccept(response -> {
                if (response != null && "SUCCESS".equals(response.getStatus())) {
                    SwingUtilities.invokeLater(() -> {
                        // 更新用户数据
                        userData.put("avatar", avatarPath);
                        JOptionPane.showMessageDialog(dialog, "头像更新成功！", "成功", JOptionPane.INFORMATION_MESSAGE);
                        // 在这里更新UI
                        if (sidebarAvatar != null) {
                            loadUserAvatar(sidebarAvatar, 60, 60);
                        }
                        if (profileAvatar != null) {
                            loadUserAvatar(profileAvatar, 120, 120);
                        }
                    });
                    log.info("用户 {} 头像更新成功", userData.get("userName"));
                } else {
                    String errorMsg = response != null ? response.getMessage() : "服务器无响应";
                    SwingUtilities.invokeLater(() -> 
                        JOptionPane.showMessageDialog(dialog, "头像更新失败：" + errorMsg, "错误", JOptionPane.ERROR_MESSAGE)
                    );
                    log.warn("用户 {} 头像更新失败: {}", userData.get("userName"), errorMsg);
                }
            }).exceptionally(throwable -> {
                log.error("更新头像时发生错误", throwable);
                SwingUtilities.invokeLater(() -> 
                    JOptionPane.showMessageDialog(dialog, "更新头像时发生错误：" + throwable.getMessage(), "错误", JOptionPane.ERROR_MESSAGE)
                );
                return null;
            });

        } catch (Exception e) {
            log.error("发送头像更新请求时发生错误", e);
            SwingUtilities.invokeLater(() -> 
                JOptionPane.showMessageDialog(dialog, "发送头像更新请求时发生错误：" + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE)
            );
        }
    }

    /**
     * 恢复默认头像
     */
    private void resetToDefaultAvatar(JDialog dialog, JLabel sidebarAvatar, JLabel profileAvatar) {
        if (nettyClient == null) {
            log.warn("NettyClient未初始化，无法重置头像到服务器");
            return;
        }

        try {
            String cardNum = userData.get("cardNum").toString();
            
            Request request = new Request("auth/updateAvatar")
                    .addParam("cardNum", cardNum)
                    .addParam("avatar", ""); // 空字符串表示使用默认头像

            CompletableFuture<Response> future = nettyClient.sendRequest(request);
            future.thenAccept(response -> {
                if (response != null && "SUCCESS".equals(response.getStatus())) {
                    SwingUtilities.invokeLater(() -> {
                        // 清空用户头像路径
                        userData.remove("avatar");
                        JOptionPane.showMessageDialog(dialog, "已恢复默认头像", "成功", JOptionPane.INFORMATION_MESSAGE);
                        // 在这里更新UI
                        if (sidebarAvatar != null) {
                            loadUserAvatar(sidebarAvatar, 60, 60);
                        }
                        if (profileAvatar != null) {
                            loadUserAvatar(profileAvatar, 120, 120);
                        }
                    });
                    log.info("用户 {} 已恢复默认头像", userData.get("userName"));
                } else {
                    String errorMsg = response != null ? response.getMessage() : "服务器无响应";
                    SwingUtilities.invokeLater(() -> 
                        JOptionPane.showMessageDialog(dialog, "恢复默认头像失败：" + errorMsg, "错误", JOptionPane.ERROR_MESSAGE)
                    );
                    log.warn("用户 {} 恢复默认头像失败: {}", userData.get("userName"), errorMsg);
                }
            }).exceptionally(throwable -> {
                log.error("恢复默认头像时发生错误", throwable);
                SwingUtilities.invokeLater(() -> 
                    JOptionPane.showMessageDialog(dialog, "恢复默认头像时发生错误：" + throwable.getMessage(), "错误", JOptionPane.ERROR_MESSAGE)
                );
                return null;
            });

        } catch (Exception e) {
            log.error("发送恢复默认头像请求时发生错误", e);
            SwingUtilities.invokeLater(() -> 
                JOptionPane.showMessageDialog(dialog, "发送恢复默认头像请求时发生错误：" + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE)
            );
        }
    }
}
