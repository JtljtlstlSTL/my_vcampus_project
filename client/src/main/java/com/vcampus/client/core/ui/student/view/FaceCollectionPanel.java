package com.vcampus.client.core.ui.student.view;



import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.awt.geom.Ellipse2D;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Map;

public class FaceCollectionPanel extends JPanel {
    // 动画渐变相关
    private Color startColor, endColor; // 当前渐变色
    private Color targetStartColor, targetEndColor; // 目标渐变色
    private Timer gradientTimer;
    private boolean collectedStatus = false; // 当前采集状态
    // 标记OpenCV是否已加载
    private static boolean opencvLoaded = false;
    private JLabel avatarLabel;
    private JLabel statusLabel;
    private JLabel nameLabel, genderLabel, cardNumLabel, deptLabel;
    private File selectedFile;
    private final String studentCardNum;
    private final String studentName, studentGender, studentDept;

    public FaceCollectionPanel(Map<String, Object> userData) {
        this.studentCardNum = userData.get("cardNum") != null ? userData.get("cardNum").toString() : "";
        this.studentName = userData.get("userName") != null ? userData.get("userName").toString() : "";
        this.studentGender = userData.get("gender") != null ? userData.get("gender").toString() : "";
        this.studentDept = userData.get("department") != null ? userData.get("department").toString() : "";

    setLayout(new GridBagLayout());
    setOpaque(false); // 背景由paintComponent绘制
    // 初始渐变色（浅蓝到浅绿，假定初始为已采集）
    startColor = new Color(210, 225, 245);
    endColor = new Color(180, 240, 200);
    targetStartColor = startColor;
    targetEndColor = endColor;
    // 启动动画Timer
    gradientTimer = new Timer(30, e -> animateGradient());
    gradientTimer.start();

        // 卡片Panel
        JPanel cardPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // 卡片背景透明，保留圆角和阴影
                g2.setColor(new Color(255,255,255,230));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 32, 32);
                g2.setColor(new Color(180, 200, 230, 80));
                g2.setStroke(new BasicStroke(2f));
                g2.drawRoundRect(2, 2, getWidth()-4, getHeight()-4, 32, 32);
            }
        };
    cardPanel.setLayout(new BoxLayout(cardPanel, BoxLayout.Y_AXIS));
    cardPanel.setPreferredSize(new Dimension(320, 480));
    cardPanel.setMaximumSize(new Dimension(340, 520));
    cardPanel.setOpaque(false);
    cardPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // 头像（圆形）
    avatarLabel = new JLabel();
    avatarLabel.setHorizontalAlignment(SwingConstants.CENTER);
    avatarLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
    avatarLabel.setPreferredSize(new Dimension(120, 120));
    avatarLabel.setMaximumSize(new Dimension(120, 120));
    avatarLabel.setBorder(BorderFactory.createEmptyBorder(0,0,0,0));
    loadFaceAvatar();
    // 圆形头像
    avatarLabel.setIcon(makeCircleAvatar(avatarLabel.getIcon()));
    cardPanel.add(Box.createVerticalStrut(10));
    JLabel titleLabel = new JLabel("人脸采集", SwingConstants.CENTER);
    titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 22));
    titleLabel.setForeground(new Color(52, 73, 94));
    titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
    cardPanel.add(titleLabel);
    cardPanel.add(Box.createVerticalStrut(10));
    cardPanel.add(avatarLabel);
    cardPanel.add(Box.createVerticalStrut(18));

        // 采集状态
    statusLabel = new JLabel();
    statusLabel.setFont(new Font("微软雅黑", Font.BOLD, 16));
    statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
    updateFaceStatus(); // 会自动设置渐变色目标
    cardPanel.add(statusLabel);
    cardPanel.add(Box.createVerticalStrut(10));
    JSeparator sep1 = new JSeparator();
    sep1.setMaximumSize(new Dimension(360, 2));
    cardPanel.add(sep1);
    cardPanel.add(Box.createVerticalStrut(10));

        // 基本信息区
    JPanel infoPanel = new JPanel() {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(245, 250, 255, 220));
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 18, 18);
        }
    };
    infoPanel.setOpaque(false);
    infoPanel.setLayout(new GridBagLayout());
    GridBagConstraints infoGbc = new GridBagConstraints();
    infoGbc.gridx = 0;
    infoGbc.gridy = GridBagConstraints.RELATIVE;
    infoGbc.anchor = GridBagConstraints.WEST;
    infoGbc.insets = new Insets(8, 12, 8, 12);

    nameLabel = new JLabel("姓名：" + studentName);
    nameLabel.setFont(new Font("微软雅黑", Font.BOLD, 16));
    nameLabel.setForeground(new Color(52, 73, 94));
    genderLabel = new JLabel("性别：" + studentGender);
    genderLabel.setFont(new Font("微软雅黑", Font.PLAIN, 15));
    genderLabel.setForeground(new Color(52, 73, 94));
    cardNumLabel = new JLabel("卡号：" + studentCardNum);
    cardNumLabel.setFont(new Font("微软雅黑", Font.PLAIN, 15));
    cardNumLabel.setForeground(new Color(52, 73, 94));
    deptLabel = new JLabel("学院：" + studentDept);
    deptLabel.setFont(new Font("微软雅黑", Font.PLAIN, 15));
    deptLabel.setForeground(new Color(52, 73, 94));

    infoPanel.add(nameLabel, infoGbc);
    infoPanel.add(genderLabel, infoGbc);
    infoPanel.add(cardNumLabel, infoGbc);
    infoPanel.add(deptLabel, infoGbc);

    cardPanel.add(infoPanel);
    cardPanel.add(Box.createVerticalStrut(10));
    JSeparator sep2 = new JSeparator();
    sep2.setMaximumSize(new Dimension(360, 2));
    cardPanel.add(sep2);
    cardPanel.add(Box.createVerticalStrut(18));

        // “上传人像”按钮，缩小并上移
        cardPanel.add(Box.createVerticalStrut(2)); // 缩短分隔后间距
        JButton btnCollect = new JButton("上传人像");
    styleUploadButtonFullRound(btnCollect);
        btnCollect.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnCollect.addActionListener(e -> showCollectDialog());
        btnCollect.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btnCollect.setBackground(new Color(41, 128, 185));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btnCollect.setBackground(new Color(52, 152, 219));
            }
            public void mousePressed(java.awt.event.MouseEvent evt) {
                btnCollect.setBackground(new Color(30, 90, 140));
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                btnCollect.setBackground(new Color(41, 128, 185));
            }
        });
        cardPanel.add(btnCollect);
        // ...existing code...

        // 居中显示卡片
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.CENTER;
        add(cardPanel, gbc);
    // 构造函数结束，下面是类级别方法
    }

    // 新的美化方法：更大圆角、更小高度
    // 更小更圆按钮样式
    private void styleUploadButtonSmall(JButton button) {
        button.setFont(new Font("微软雅黑", Font.BOLD, 14));
        button.setBackground(new Color(52, 152, 219));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setPreferredSize(new Dimension(120, 28));
        button.setMaximumSize(new Dimension(140, 28));
        button.setBorder(new RoundedBorder(18));
    }

    // 始终圆角化按钮
    private void styleUploadButtonFullRound(JButton button) {
        button.setFont(new Font("微软雅黑", Font.BOLD, 14));
        button.setBackground(new Color(52, 152, 219));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setPreferredSize(new Dimension(120, 28));
        button.setMaximumSize(new Dimension(140, 28));
        button.setBorder(new RoundedBorder(28)); // 更大圆角，近似椭圆
    }
    private void styleUploadButton(JButton button) {
        button.setFont(new Font("微软雅黑", Font.BOLD, 15));
        button.setBackground(new Color(52, 152, 219));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setPreferredSize(new Dimension(160, 36));
        button.setMaximumSize(new Dimension(180, 36));
        button.setBorder(new RoundedBorder(24)); // 更大圆角
    }

    // 动画渐变绘制
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        GradientPaint gp = new GradientPaint(0, 0, startColor, 0, getHeight(), endColor);
        g2.setPaint(gp);
        g2.fillRect(0, 0, getWidth(), getHeight());
    }

    // 动画渐变色过渡
    private void animateGradient() {
        startColor = animateColorStep(startColor, targetStartColor);
        endColor = animateColorStep(endColor, targetEndColor);
        repaint();
    }

    // 单步颜色过渡
    private Color animateColorStep(Color c, Color target) {
        int r = step(c.getRed(), target.getRed());
        int g = step(c.getGreen(), target.getGreen());
        int b = step(c.getBlue(), target.getBlue());
        return new Color(r, g, b);
    }

    private int step(int cur, int tgt) {
        if (cur == tgt) return cur;
        int diff = tgt - cur;
        int delta = Math.abs(diff) < 8 ? diff : diff / 8; // 步长
        return cur + delta;
    }

    // 圆形头像处理
    private ImageIcon makeCircleAvatar(Icon srcIcon) {
    if (!(srcIcon instanceof ImageIcon)) return null;
    Image img = ((ImageIcon)srcIcon).getImage();
    int size = 120;
    BufferedImage circleImg = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
    Graphics2D g2 = circleImg.createGraphics();
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    g2.setClip(new Ellipse2D.Float(0,0,size,size));
    g2.drawImage(img, 0, 0, size, size, null);
    g2.dispose();
    return new ImageIcon(circleImg);
// 删除多余右括号
    }

    // 按钮美化方法
    private void styleButton(JButton button) {
        button.setFont(new Font("微软雅黑", Font.BOLD, 16));
        button.setBackground(new Color(52, 152, 219));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(new RoundedBorder(18)); // 圆角边框
    }

    // 圆角边框类
    private static class RoundedBorder extends javax.swing.border.AbstractBorder {
        private int radius;
        public RoundedBorder(int radius) {
            this.radius = radius;
        }
        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(52, 152, 219));
            g2.setStroke(new BasicStroke(2));
            g2.drawRoundRect(x+1, y+1, width-3, height-3, radius, radius);
            g2.dispose();
        }
        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(radius, radius, radius, radius);
        }
        @Override
        public Insets getBorderInsets(Component c, Insets insets) {
            insets.left = insets.right = insets.top = insets.bottom = radius;
            return insets;
        }
    }

    // 加载头像（如已采集则显示人脸，否则显示默认）
    private void loadFaceAvatar() {
        String[] exts = {".png", ".jpg", ".jpeg"};
        File faceDir = new File("faces");
        File avatarFile = null;
        // 账号_密码 作为文件名
        String account = studentCardNum;
        String password = "";
        // 尝试从 userData 获取密码（如有）
        try {
            java.lang.reflect.Field pwdField = this.getClass().getDeclaredField("studentPassword");
            pwdField.setAccessible(true);
            password = (String) pwdField.get(this);
        } catch (Exception ignore) {}
        String filePrefix = account + "_" + password;
        for (String ext : exts) {
            File f = new File(faceDir, filePrefix + ext);
            if (f.exists()) { avatarFile = f; break; }
        }
        if (avatarFile != null) {
            ImageIcon icon = new ImageIcon(avatarFile.getAbsolutePath());
            Image img = icon.getImage().getScaledInstance(120, 120, Image.SCALE_SMOOTH);
            avatarLabel.setIcon(new ImageIcon(img));
            avatarLabel.setText("");
        } else {
            // 使用指定图片作为默认头像，并圆形裁剪
            ImageIcon defaultIcon = new ImageIcon("src/main/resources/figures/ai.png");
            avatarLabel.setIcon(makeCircleAvatar(defaultIcon));
            avatarLabel.setText("未采集人脸");
        }
    }

    // 默认头像生成（圆形+渐变色）
    private ImageIcon makeDefaultAvatar() {
        int size = 120;
        BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        GradientPaint gp = new GradientPaint(0, 0, new Color(52,152,219), size, size, new Color(41,128,185));
        g2.setPaint(gp);
        g2.fillOval(0, 0, size, size);
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("微软雅黑", Font.BOLD, 48));
        g2.drawString("?", size/2-16, size/2+18);
        g2.dispose();
        return new ImageIcon(img);
    }

    // 更新采集状态，并切换渐变色目标
    private void updateFaceStatus() {
        String[] exts = {".png", ".jpg", ".jpeg"};
        File faceDir = new File("faces");
        boolean collected = false;
        String account = studentCardNum;
        String password = "";
        try {
            java.lang.reflect.Field pwdField = this.getClass().getDeclaredField("studentPassword");
            pwdField.setAccessible(true);
            password = (String) pwdField.get(this);
        } catch (Exception ignore) {}
        String filePrefix = account + "_" + password;
        for (String ext : exts) {
            File f = new File(faceDir, filePrefix + ext);
            if (f.exists()) { collected = true; break; }
        }
        collectedStatus = collected;
        statusLabel.setText(collected ? "已采集人脸" : "未采集人脸");
        statusLabel.setForeground(collected ? new Color(46, 204, 113) : new Color(231, 76, 60));
        // 动画渐变色目标切换
        if (collected) {
            targetStartColor = new Color(210, 225, 245); // 浅蓝
            targetEndColor = new Color(180, 240, 200); // 浅绿
        } else {
            targetStartColor = new Color(210, 225, 245); // 浅蓝
            targetEndColor = new Color(245, 200, 200); // 浅红
        }
    }

    // 采集弹窗
    private void showCollectDialog() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("选择人脸图片");
        fileChooser.setFileFilter(new FileNameExtensionFilter("图片文件 (jpg, png)", "jpg", "png", "jpeg"));
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            selectedFile = fileChooser.getSelectedFile();
            try {
                // 只加载一次OpenCV dll
                if (!opencvLoaded) {
                    String dllPath = new File("lib/opencv_java4110.dll").getAbsolutePath();
                    if (!new File(dllPath).exists()) {
                        dllPath = new File("lib/opencv_java4110.dll").getAbsolutePath();
                    }
                    System.out.println("OpenCV DLL Path: " + dllPath);
                    if (!new File(dllPath).exists()) {
                        JOptionPane.showMessageDialog(this, "OpenCV DLL未找到: " + dllPath, "错误", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    System.load(dllPath);
                    opencvLoaded = true;
                }
                String cascadePath = new File("resources/haarcascade_frontalface_default.xml").getAbsolutePath();
                System.out.println("Cascade Model Path: " + cascadePath);
                if (!new File(cascadePath).exists()) {
                    JOptionPane.showMessageDialog(this, "人脸检测模型未找到: " + cascadePath, "错误", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                FaceDetector detector = new FaceDetector("resources/haarcascade_frontalface_default.xml");
                boolean hasFace = detector.hasFace(selectedFile.getAbsolutePath());
                if (!hasFace) {
                    JOptionPane.showMessageDialog(this, "未检测到人脸，请重新上传！", "提示", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                // 保存到 faces 目录
                File faceDir = new File("faces");
                if (!faceDir.exists()) faceDir.mkdirs();
                String ext = getFileExtension(selectedFile.getName());
                String account = studentCardNum;
                String password = "";
                try {
                    java.lang.reflect.Field pwdField = this.getClass().getDeclaredField("studentPassword");
                    pwdField.setAccessible(true);
                    password = (String) pwdField.get(this);
                } catch (Exception ignore) {}
                String filePrefix = account + "_" + password;
                // 删除同名但不同扩展名的旧图片
                String[] allExts = {".jpg", ".png", ".jpeg"};
                for (String e : allExts) {
                    File oldFile = new File(faceDir, filePrefix + e);
                    if (oldFile.exists()) oldFile.delete();
                }
                File destFile = new File(faceDir, filePrefix + "." + ext);
                Files.copy(selectedFile.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                JOptionPane.showMessageDialog(this, "图片采集成功！", "成功", JOptionPane.INFORMATION_MESSAGE);
                loadFaceAvatar();
                updateFaceStatus();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "图片保存失败：" + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private String getFileExtension(String filename) {
        int dot = filename.lastIndexOf('.');
        return (dot == -1) ? "" : filename.substring(dot + 1);
    }

}
