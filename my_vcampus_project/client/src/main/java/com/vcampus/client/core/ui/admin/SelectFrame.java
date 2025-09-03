package com.vcampus.client.core.ui.admin;

import com.vcampus.client.core.net.NettyClient;
import com.vcampus.client.core.ui.academic.StatusPanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;

/**
 * 功能选择界面
 *
 * @author VCampus Team
 * @version 1.0
 */
public class SelectFrame extends JFrame {
    private static final Logger logger = LoggerFactory.getLogger(SelectFrame.class);

    private JRadioButton rbEducation;
    private JRadioButton rbLibrary;
    private JRadioButton rbShop;
    private ButtonGroup functionGroup;
    private JButton btnConfirm;
    private JButton btnCancel;

    private NettyClient nettyClient;
    private Map<String, Object> userData;
    private AdminFrame parentFrame;

    public SelectFrame(NettyClient nettyClient, Map<String, Object> userData, AdminFrame parentFrame) {
        this.nettyClient = nettyClient;
        this.userData = userData;
        this.parentFrame = parentFrame;

        initUI();
        setupEventHandlers();

        logger.info("功能选择界面初始化完成，管理员: {}", userData.get("userName"));
    }

    private void initUI() {
        setTitle("VCampus - 功能选择");
        setSize(500, 350); // 减少窗口高度
        setLocationRelativeTo(parentFrame);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setResizable(false);

        try {
            ImageIcon logoIcon = new ImageIcon(getClass().getResource("/figures/logo.png"));
            setIconImage(logoIcon.getImage());
        } catch (Exception e) {
            logger.warn("图标加载失败");
        }

        // 创建主面板
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        mainPanel.setBackground(new Color(248, 249, 250));

        // 创建标题面板
        JPanel titlePanel = createTitlePanel();
        mainPanel.add(titlePanel, BorderLayout.NORTH);

        // 创建内容面板
        JPanel contentPanel = createContentPanel();
        mainPanel.add(contentPanel, BorderLayout.CENTER);

        // 创建按钮面板
        JPanel buttonPanel = createButtonPanel();
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        setContentPane(mainPanel);
    }

    private JPanel createTitlePanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));

        JLabel titleLabel = new JLabel("功能模块选择");
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 20));
        titleLabel.setForeground(new Color(25, 133, 57));

        JLabel subtitleLabel = new JLabel("请选择要访问的功能模块");
        subtitleLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        subtitleLabel.setForeground(Color.GRAY);

        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        panel.add(titleLabel);
        panel.add(Box.createVerticalStrut(5));
        panel.add(subtitleLabel);

        return panel;
    }

    private JPanel createContentPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // 功能选择区域标题
        JLabel lblFunction = new JLabel("选择功能模块:");
        lblFunction.setFont(new Font("微软雅黑", Font.BOLD, 14));
        lblFunction.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(lblFunction);
        panel.add(Box.createVerticalStrut(15));

        // 功能选择单选按钮组面板
        JPanel functionPanel = new JPanel();
        functionPanel.setLayout(new BoxLayout(functionPanel, BoxLayout.Y_AXIS));
        functionPanel.setOpaque(false);
        functionPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        functionGroup = new ButtonGroup();

        rbEducation = new JRadioButton("教务管理系统");
        rbEducation.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        rbEducation.setOpaque(false);
        rbEducation.setSelected(true); // 默认选中
        rbEducation.setAlignmentX(Component.CENTER_ALIGNMENT);

        rbLibrary = new JRadioButton("图书馆管理系统");
        rbLibrary.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        rbLibrary.setOpaque(false);
        rbLibrary.setAlignmentX(Component.CENTER_ALIGNMENT);

        rbShop = new JRadioButton("商店管理系统");
        rbShop.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        rbShop.setOpaque(false);
        rbShop.setAlignmentX(Component.CENTER_ALIGNMENT);

        functionGroup.add(rbEducation);
        functionGroup.add(rbLibrary);
        functionGroup.add(rbShop);

        functionPanel.add(rbEducation);
        functionPanel.add(Box.createVerticalStrut(10));
        functionPanel.add(rbLibrary);
        functionPanel.add(Box.createVerticalStrut(10));
        functionPanel.add(rbShop);

        panel.add(functionPanel);
        panel.add(Box.createVerticalStrut(20));

        return panel;
    }

    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        btnConfirm = new JButton("确认");
        btnCancel = new JButton("取消");

        // 设置按钮样式
        Dimension buttonSize = new Dimension(100, 35);
        btnConfirm.setPreferredSize(buttonSize);
        btnCancel.setPreferredSize(buttonSize);

        btnConfirm.setBackground(new Color(25, 133, 57));
        btnConfirm.setForeground(Color.WHITE);
        btnConfirm.setFocusPainted(false);
        btnConfirm.setBorderPainted(false);
        btnConfirm.setFont(new Font("微软雅黑", Font.BOLD, 12));

        btnCancel.setBackground(new Color(108, 117, 125));
        btnCancel.setForeground(Color.WHITE);
        btnCancel.setFocusPainted(false);
        btnCancel.setBorderPainted(false);
        btnCancel.setFont(new Font("微软雅黑", Font.BOLD, 12));

        panel.add(btnConfirm);
        panel.add(btnCancel);

        return panel;
    }

    private void setupEventHandlers() {
        // 确认按钮事件
        btnConfirm.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleConfirm();
            }
        });

        // 取消按钮事件
        btnCancel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleCancel();
            }
        });
    }

    private void handleConfirm() {
        String selectedFunction = getSelectedFunction();
        openFunctionModule(selectedFunction);
    }

    private void handleCancel() {
        logger.info("用户取消功能选择，返回管理员主界面");
        this.dispose();
    }

    private String getSelectedFunction() {
        if (rbEducation.isSelected()) {
            return "education";
        } else if (rbLibrary.isSelected()) {
            return "library";
        } else if (rbShop.isSelected()) {
            return "shop";
        }
        return "education"; // 默认返回教务
    }

    private void openFunctionModule(String function) {
        try {
            String functionName = getFunctionName(function);
            logger.info("管理员 {} 正在打开 {} 模块", userData.get("userName"), functionName);

            if ("education".equals(function)) {
                // 打开教务管理界面
                SwingUtilities.invokeLater(() -> {
                    try {
                        StatusPanel statusPanel = new StatusPanel(nettyClient, userData);
                        statusPanel.setVisible(true);
                        logger.info("教务管理系统界面已打开");
                    } catch (Exception e) {
                        logger.error("打开教务管理系统时发生错误", e);
                        JOptionPane.showMessageDialog(this,
                                "打开教务管理系统时发生错误：" + e.getMessage(),
                                "错误",
                                JOptionPane.ERROR_MESSAGE);
                    }
                });
            } else {
                // 其他模块暂时显示消息
                JOptionPane.showMessageDialog(this,
                        functionName + " 模块功能正在开发中...",
                        "提示",
                        JOptionPane.INFORMATION_MESSAGE);
            }

            // 关闭当前窗口
            this.dispose();

            logger.info("功能选择完成，已选择: {}", functionName);

        } catch (Exception e) {
            logger.error("处理功能选择时发生错误", e);
            JOptionPane.showMessageDialog(this,
                    "处理功能选择时发生错误：" + e.getMessage(),
                    "错误",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private String getFunctionName(String function) {
        switch (function) {
            case "education":
                return "教务管理系统";
            case "library":
                return "图书馆管理系统";
            case "shop":
                return "商店管理系统";
            default:
                return "未知模块";
        }
    }
}
