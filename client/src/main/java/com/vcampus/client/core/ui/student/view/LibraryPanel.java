package com.vcampus.client.core.ui.student.view;

import com.vcampus.client.core.ui.student.model.StudentModel;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import java.awt.*;

/**
 * 图书馆面板
 *
 * @author VCampus Team
 * @version 1.0
 */
@Slf4j
public class LibraryPanel extends JPanel {
    
    private StudentModel model;
    private StudentView parentView;
    
    public LibraryPanel(StudentModel model, StudentView parentView) {
        this.model = model;
        this.parentView = parentView;
        
        initUI();
    }
    
    private void initUI() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        
        JLabel titleLabel = new JLabel("图书馆", SwingConstants.CENTER);
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 24));
        titleLabel.setForeground(new Color(25, 25, 112));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(50, 0, 0, 0));
        
        add(titleLabel, BorderLayout.CENTER);
    }
}
