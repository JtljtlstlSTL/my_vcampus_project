package com.vcampus.client.core.ui.AiChatAssistant;

import com.vcampus.client.core.service.AIAssistantService;

import javax.swing.*;
import javax.swing.text.StyledDocument;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * AI助手聊天窗口
 */
public class AiChatAssistantPanel extends JPanel {
    private JTextPane chatPane;
    private JScrollPane scrollPane;
    private JTextField inputField;
    private JButton sendButton;
    private AIAssistantService aiService;

    public AiChatAssistantPanel() {
        this.aiService = new AIAssistantService();
        setLayout(new BorderLayout(10, 10));

        chatPane = new JTextPane();
        chatPane.setEditable(false);
        chatPane.setFont(new Font("微软雅黑", Font.PLAIN, 15));
        chatPane.setBackground(new Color(245, 248, 250));
        scrollPane = new JScrollPane(chatPane);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(scrollPane, BorderLayout.CENTER);

        JPanel inputPanel = new JPanel(new BorderLayout(8, 8));
        inputPanel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        inputField = new JTextField();
        inputField.setFont(new Font("微软雅黑", Font.PLAIN, 15));
        inputField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            BorderFactory.createEmptyBorder(6, 10, 6, 10)));
        sendButton = new JButton("发送");
        sendButton.setFont(new Font("微软雅黑", Font.BOLD, 15));
        sendButton.setBackground(new Color(60, 179, 113));
        sendButton.setForeground(Color.WHITE);
        sendButton.setFocusPainted(false);
        sendButton.setBorder(BorderFactory.createEmptyBorder(6, 18, 6, 18));

        inputPanel.add(inputField, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);
        add(inputPanel, BorderLayout.SOUTH);

        sendButton.addActionListener(e -> sendMessage());
        inputField.addActionListener(e -> sendMessage());
    }

    private void sendMessage() {
        String userMsg = inputField.getText();
        if (userMsg.trim().isEmpty()) return;
        
        // 用户消息显示在右侧
        appendMessageBubble("👤 你: " + userMsg, true);
        inputField.setText("");

        // AI消息显示在左侧 - 先显示等待动画
        int loadingStart = appendMessageBubble("🤖 AI助手: ·", false);

        Timer loadingTimer = new Timer(400, null);
        loadingTimer.setRepeats(true);
        loadingTimer.addActionListener(new ActionListener() {
            int dotCount = 1;
            @Override
            public void actionPerformed(ActionEvent e) {
                dotCount = (dotCount % 3) + 1;
                String dots = new String(new char[dotCount]).replace("\0", "·");
                updateBubbleText(loadingStart, "🤖 AI助手: " + dots);
            }
        });
        loadingTimer.start();

        new Thread(() -> {
            String aiReply = aiService.sendMessageToAI(userMsg);
            SwingUtilities.invokeLater(() -> {
                loadingTimer.stop();
                updateBubbleText(loadingStart, "🤖 AI助手: " + aiReply);
            });
        }).start();
    }

    // 添加消息气泡，返回起始位置索引
    private int appendMessageBubble(String message, boolean isUser) {
        try {
            StyledDocument doc = chatPane.getStyledDocument();
            int start = doc.getLength();
            SimpleAttributeSet attr = new SimpleAttributeSet();
            
            StyleConstants.setFontFamily(attr, "微软雅黑");
            StyleConstants.setFontSize(attr, 15);
            StyleConstants.setSpaceAbove(attr, 8);
            StyleConstants.setSpaceBelow(attr, 8);
            StyleConstants.setLineSpacing(attr, 0.2f);
            
            if (isUser) {
                // 用户消息 - 左侧，蓝色背景
                StyleConstants.setBackground(attr, new Color(33, 150, 243));
                StyleConstants.setForeground(attr, Color.WHITE);
                StyleConstants.setAlignment(attr, StyleConstants.ALIGN_LEFT);
                StyleConstants.setLeftIndent(attr, 10);
                StyleConstants.setRightIndent(attr, 40);
            } else {
                // AI消息 - 左侧，灰色背景
                StyleConstants.setBackground(attr, new Color(240, 240, 240));
                StyleConstants.setForeground(attr, new Color(80, 80, 80));
                StyleConstants.setAlignment(attr, StyleConstants.ALIGN_LEFT);
                StyleConstants.setLeftIndent(attr, 10);
                StyleConstants.setRightIndent(attr, 40);
            }
            
            doc.insertString(doc.getLength(), message + "\n", attr);
            doc.setParagraphAttributes(doc.getLength(), 1, attr, false);
            chatPane.setCaretPosition(doc.getLength());
            return start;
        } catch (Exception ex) {
            ex.printStackTrace();
            return -1;
        }
    }

    // 更新指定位置的气泡内容
    private void updateBubbleText(int start, String newText) {
        try {
            StyledDocument doc = chatPane.getStyledDocument();
            int end = doc.getLength();
            doc.remove(start, end - start);
            
            SimpleAttributeSet attr = new SimpleAttributeSet();
            StyleConstants.setFontFamily(attr, "微软雅黑");
            StyleConstants.setFontSize(attr, 15);
            StyleConstants.setBackground(attr, new Color(240, 240, 240));
            StyleConstants.setForeground(attr, new Color(80, 80, 80));
            StyleConstants.setAlignment(attr, StyleConstants.ALIGN_LEFT);
            StyleConstants.setLeftIndent(attr, 10);
            StyleConstants.setRightIndent(attr, 40);
            StyleConstants.setSpaceAbove(attr, 8);
            StyleConstants.setSpaceBelow(attr, 8);
            StyleConstants.setLineSpacing(attr, 0.2f);
            
            doc.insertString(start, newText + "\n", attr);
            doc.setParagraphAttributes(doc.getLength(), 1, attr, false);
            chatPane.setCaretPosition(doc.getLength());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}