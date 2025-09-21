package com.vcampus.client.core.ui.teacher;

import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 视频上传工具类
 * 用于将本地视频文件上传到项目根目录的videos目录
 * 
 * @author VCampus Team
 * @version 1.0
 */
@Slf4j
public class VideoUpload extends JDialog {
    
    private static final String VIDEO_DIR = "videos";
    private static final String[] SUPPORTED_EXTENSIONS = {".mp4", ".avi", ".mov", ".wmv", ".flv", ".mkv", ".webm"};
    
    private JProgressBar progressBar;
    private JLabel statusLabel;
    private JButton uploadButton;
    private JButton cancelButton;
    private File selectedFile;
    private boolean uploadCancelled = false;
    private Path targetDir;
    
    public VideoUpload(JFrame parent) {
        super(parent, "视频上传", true);
        initUI();
        setLocationRelativeTo(parent);
    }
    
    private void initUI() {
        setLayout(new BorderLayout());
        setSize(500, 300);
        setResizable(false);
        
        // 创建主面板
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(new Color(248, 250, 252));
        
        // 标题面板
        JPanel titlePanel = createTitlePanel();
        mainPanel.add(titlePanel, BorderLayout.NORTH);
        
        // 内容面板
        JPanel contentPanel = createContentPanel();
        mainPanel.add(contentPanel, BorderLayout.CENTER);
        
        // 按钮面板
        JPanel buttonPanel = createButtonPanel();
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        add(mainPanel);
    }
    
    private JPanel createTitlePanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        panel.setOpaque(false);
        
        JLabel titleLabel = new JLabel("视频文件上传");
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 18));
        titleLabel.setForeground(new Color(51, 102, 153));
        
        panel.add(titleLabel);
        return panel;
    }
    
    private JPanel createContentPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 215, 235), 1),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        
        // 文件选择区域
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        JLabel fileLabel = new JLabel("选择视频文件：");
        fileLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        panel.add(fileLabel, gbc);
        
        gbc.gridx = 1; gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        JTextField filePathField = new JTextField();
        filePathField.setEditable(false);
        filePathField.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        filePathField.setBackground(Color.WHITE);
        panel.add(filePathField, gbc);
        
        gbc.gridx = 2; gbc.gridy = 0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        JButton selectButton = new JButton("浏览...");
        selectButton.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        selectButton.setPreferredSize(new Dimension(80, 30));
        selectButton.addActionListener(e -> selectVideoFile(filePathField));
        panel.add(selectButton, gbc);
        
        // 进度条区域
        gbc.gridx = 0; gbc.gridy = 1;
        gbc.gridwidth = 3;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(20, 10, 10, 10);
        JLabel progressLabel = new JLabel("上传进度：");
        progressLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        panel.add(progressLabel, gbc);
        
        gbc.gridx = 0; gbc.gridy = 2;
        gbc.insets = new Insets(5, 10, 10, 10);
        progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);
        progressBar.setString("等待上传...");
        progressBar.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        panel.add(progressBar, gbc);
        
        // 状态标签
        gbc.gridx = 0; gbc.gridy = 3;
        gbc.insets = new Insets(10, 10, 5, 10);
        statusLabel = new JLabel("请选择要上传的视频文件");
        statusLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        statusLabel.setForeground(new Color(100, 116, 139));
        panel.add(statusLabel, gbc);
        
        return panel;
    }
    
    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        panel.setOpaque(false);
        
        uploadButton = new JButton("开始上传");
        uploadButton.setFont(new Font("微软雅黑", Font.BOLD, 12));
        uploadButton.setPreferredSize(new Dimension(100, 35));
        uploadButton.setEnabled(false);
        uploadButton.addActionListener(e -> startUpload());
        
        cancelButton = new JButton("取消");
        cancelButton.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        cancelButton.setPreferredSize(new Dimension(80, 35));
        cancelButton.addActionListener(e -> {
            uploadCancelled = true;
            dispose();
        });
        
        // 美化按钮
        styleButton(uploadButton, new Color(40, 167, 69), Color.WHITE);
        styleButton(cancelButton, new Color(108, 117, 125), Color.WHITE);
        
        panel.add(uploadButton);
        panel.add(cancelButton);
        
        return panel;
    }
    
    private void styleButton(JButton button, Color backgroundColor, Color textColor) {
        button.setBackground(backgroundColor);
        button.setForeground(textColor);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(true);
        
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                button.setBackground(backgroundColor.brighter());
            }
            
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                button.setBackground(backgroundColor);
            }
        });
    }
    
    private void selectVideoFile(JTextField filePathField) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("选择视频文件");
        
        // 设置文件过滤器
        FileNameExtensionFilter filter = new FileNameExtensionFilter(
            "视频文件 (*.mp4, *.avi, *.mov, *.wmv, *.flv, *.mkv, *.webm)", 
            "mp4", "avi", "mov", "wmv", "flv", "mkv", "webm"
        );
        fileChooser.setFileFilter(filter);
        
        // 设置当前目录为用户桌面
        fileChooser.setCurrentDirectory(new File(System.getProperty("user.home") + "/Desktop"));
        
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            selectedFile = fileChooser.getSelectedFile();
            filePathField.setText(selectedFile.getAbsolutePath());
            
            // 验证文件扩展名
            String fileName = selectedFile.getName().toLowerCase();
            boolean isValidExtension = false;
            for (String ext : SUPPORTED_EXTENSIONS) {
                if (fileName.endsWith(ext)) {
                    isValidExtension = true;
                    break;
                }
            }
            
            if (isValidExtension) {
                statusLabel.setText("文件已选择，可以开始上传");
                statusLabel.setForeground(new Color(40, 167, 69));
                uploadButton.setEnabled(true);
            } else {
                statusLabel.setText("不支持的文件格式，请选择视频文件");
                statusLabel.setForeground(new Color(239, 68, 68));
                uploadButton.setEnabled(false);
            }
        }
    }
    
    private void startUpload() {
        if (selectedFile == null) {
            JOptionPane.showMessageDialog(this, "请先选择要上传的文件", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        uploadButton.setEnabled(false);
        uploadCancelled = false;
        
        // 在新线程中执行上传
        SwingWorker<Void, Integer> uploadWorker = new SwingWorker<Void, Integer>() {
            @Override
            protected Void doInBackground() throws Exception {
                try {
                    // 确保目标目录存在 - 智能检测项目根目录的videos文件夹
                    Path currentDir = Paths.get("").toAbsolutePath();
                    log.info("当前工作目录: {}", currentDir);
                    
                    // 智能检测项目根目录
                    Path projectRoot = currentDir;
                    
                    // 如果当前在client目录下，需要回到项目根目录
                    if (currentDir.getFileName().toString().equals("client")) {
                        projectRoot = currentDir.getParent();
                    }
                    // 如果当前在client/src/main/java等子目录下，需要回到项目根目录
                    else if (currentDir.toString().contains("client")) {
                        // 找到包含client的路径，然后回到项目根目录
                        Path temp = currentDir;
                        while (temp != null && !temp.getFileName().toString().equals("client")) {
                            temp = temp.getParent();
                        }
                        if (temp != null) {
                            projectRoot = temp.getParent();
                        }
                    }
                    
                    // 使用项目根目录的videos文件夹
                    targetDir = projectRoot.resolve(VIDEO_DIR);
                    
                    // 将targetDir赋值给实例变量
                    VideoUpload.this.targetDir = targetDir;
                    
                    log.info("目标视频目录: {}", targetDir.toAbsolutePath());
                    
                    if (!Files.exists(targetDir)) {
                        Files.createDirectories(targetDir);
                        log.info("创建视频目录: {}", targetDir.toAbsolutePath());
                    }
                    
                    // 生成目标文件名（避免重名）
                    String fileName = selectedFile.getName();
                    String baseName = fileName.substring(0, fileName.lastIndexOf('.'));
                    String extension = fileName.substring(fileName.lastIndexOf('.'));
                    
                    Path targetFile = targetDir.resolve(fileName);
                    int counter = 1;
                    while (Files.exists(targetFile)) {
                        String newName = baseName + "_" + counter + extension;
                        targetFile = targetDir.resolve(newName);
                        counter++;
                    }
                    
                    // 复制文件
                    long fileSize = selectedFile.length();
                    long copiedBytes = 0;
                    
                    try (InputStream in = new FileInputStream(selectedFile);
                         OutputStream out = new FileOutputStream(targetFile.toFile())) {
                        
                        byte[] buffer = new byte[8192];
                        int bytesRead;
                        
                        while ((bytesRead = in.read(buffer)) != -1 && !uploadCancelled) {
                            out.write(buffer, 0, bytesRead);
                            copiedBytes += bytesRead;
                            
                            // 更新进度
                            int progress = (int) ((copiedBytes * 100) / fileSize);
                            publish(progress);
                            
                            // 模拟上传延迟（可选）
                            Thread.sleep(10);
                        }
                    }
                    
                    if (!uploadCancelled) {
                        log.info("视频上传成功: {} -> {}", selectedFile.getName(), targetFile.toAbsolutePath());
                    }
                    
                } catch (Exception e) {
                    log.error("视频上传失败", e);
                    throw e;
                }
                
                return null;
            }
            
            @Override
            protected void process(java.util.List<Integer> chunks) {
                if (!chunks.isEmpty()) {
                    int progress = chunks.get(chunks.size() - 1);
                    progressBar.setValue(progress);
                    progressBar.setString("上传中... " + progress + "%");
                    statusLabel.setText("正在上传文件...");
                    statusLabel.setForeground(new Color(59, 130, 246));
                }
            }
            
            @Override
            protected void done() {
                try {
                    if (!uploadCancelled) {
                        get(); // 检查是否有异常
                        progressBar.setValue(100);
                        progressBar.setString("上传完成");
                        statusLabel.setText("视频上传成功！");
                        statusLabel.setForeground(new Color(40, 167, 69));
                        
                        JOptionPane.showMessageDialog(VideoUpload.this, 
                            "视频上传成功！\n文件已保存到: " + targetDir.toAbsolutePath(), 
                            "上传成功", 
                            JOptionPane.INFORMATION_MESSAGE);
                        
                        dispose();
                    }
                } catch (Exception e) {
                    progressBar.setValue(0);
                    progressBar.setString("上传失败");
                    statusLabel.setText("上传失败: " + e.getMessage());
                    statusLabel.setForeground(new Color(239, 68, 68));
                    
                    JOptionPane.showMessageDialog(VideoUpload.this, 
                        "视频上传失败: " + e.getMessage(), 
                        "上传失败", 
                        JOptionPane.ERROR_MESSAGE);
                } finally {
                    uploadButton.setEnabled(true);
                }
            }
        };
        
        uploadWorker.execute();
    }
    
    /**
     * 显示视频上传对话框
     */
    public static void showUploadDialog(JFrame parent) {
        VideoUpload dialog = new VideoUpload(parent);
        dialog.setVisible(true);
    }
}
