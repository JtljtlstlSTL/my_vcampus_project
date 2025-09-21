package com.vcampus.client.core.ui.login;

import com.vcampus.client.core.ui.admin.AdminFrame;
import com.vcampus.client.core.ui.admin.StaffFrame;
import com.vcampus.client.core.ui.student.StudentFrame;
import com.vcampus.client.core.ui.widget.FloatingCatWindow;
// 暂时注释掉MainFrame的导入，因为还没找到这个类
// import com.vcampus.client.core.ui.MainFrame;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.util.Map;
import com.vcampus.client.core.net.NettyClient;
import com.vcampus.common.message.Request;
import com.vcampus.common.message.Response;
import com.vcampus.client.core.ui.component.SvgButton;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.videoio.VideoCapture;

@Slf4j
public class LoginFrame extends JFrame {

    @Getter
    private JTextField txtUsername = new JTextField(15);
    @Getter
    private JPasswordField txtPassword = new JPasswordField(15);
    @Getter
    private JButton btnLogin = new JButton("登录");
    @Getter
    private JButton btnFaceLogin = new JButton("人脸识别登录"); // 新增人脸识别登录按钮
    @Getter
    private SvgButton btnTogglePwd = new SvgButton("/figures/eye_close.svg");
    @Getter
    private SvgButton btnClose = new SvgButton("/figures/close.svg");
    @Getter
    private SvgButton btnMinimize = new SvgButton("/figures/minimize.svg");
    @Getter
    private JLabel lblUsernameError = new JLabel("用户名不能为空！");
    @Getter
    private JLabel lblPasswordError = new JLabel("密码不能为空！");
    @Getter
    private boolean isPasswordVisible = false;
    // 暂时注释掉MainFrame相关代码，直到找到或创建该类
    // private MainFrame mainFrame = null;
    @Getter
    private NettyClient nettyClient;
    @Getter
    private String currentUserCardNum = "";
    @Getter
    private String currentUserType = "";
    @Getter
    private String currentUserName = "";

    public LoginFrame() {
        setOpacity(1.0f);
        initializeUI();
        setupEventHandlers();
        // 从配置文件加载服务器地址与端口，回退到默认值
        String host = "localhost";
        int port = 8080;
        try {
            java.util.Properties props = new java.util.Properties();
            // 尝试多个可能的配置路径，兼容不同工作目录
            String[] candidates = new String[]{
                    "config/client.properties",
                    "client/config/client.properties",
                    "../config/client.properties",
                    "./config/client.properties"
            };
            java.io.File found = null;
            for (String p : candidates) {
                java.io.File f = new java.io.File(p);
                if (f.exists()) { found = f; break; }
            }
            if (found != null) {
                try (java.io.FileInputStream fis = new java.io.FileInputStream(found)) {
                    props.load(fis);
                    host = props.getProperty("server.host", host);
                    port = Integer.parseInt(props.getProperty("server.port", String.valueOf(port)));
                }
            }
        } catch (Exception e) {
            log.warn("加载客户端配置失败，使用默认连接配置", e);
        }
        nettyClient = new NettyClient(host, port);
        log.info("登录界面初始化完成, server={}:{}", host, port);
    }

    private void initializeUI() {
        setTitle("VCampus - 统一登录门户");
        setSize(710, 400);
        setLocationRelativeTo(null);
        setUndecorated(true);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        try {
            setIconImage(new ImageIcon(getClass().getResource("/figures/logo.png")).getImage());
        } catch (Exception e) {
            log.warn("图标加载失败");
        }

        JLayeredPane layeredPane = new JLayeredPane();
        layeredPane.setPreferredSize(new Dimension(710, 400));

        JSplitPane splitPane = new JSplitPane();
        splitPane.setBounds(0, 0, 710, 400);
        splitPane.setDividerLocation(450);
        splitPane.setEnabled(false);
        splitPane.setDividerSize(1);
        splitPane.setBorder(null);
        splitPane.setLeftComponent(createLeftPanel.createLeftPanel(this));
        splitPane.setRightComponent(createRightPanel.createRightPanel(this));

        layeredPane.add(splitPane, JLayeredPane.DEFAULT_LAYER);
        createTopRightButtons.createTopRightButtons(this, layeredPane);
        createPasswordToggleButton(layeredPane);
        // 添加人脸识别登录按钮到界面右下角
        btnFaceLogin.setBounds(520, 350, 150, 38);
        btnFaceLogin.setFont(new Font("微软雅黑", Font.BOLD, 15));
        btnFaceLogin.setForeground(Color.WHITE);
        btnFaceLogin.setBackground(new Color(66, 133, 244));
        btnFaceLogin.setFocusPainted(false);
        layeredPane.add(btnFaceLogin, JLayeredPane.PALETTE_LAYER);
        setContentPane(layeredPane);
        setupWindowEffects();
    }


    private void createPasswordToggleButton(JLayeredPane layeredPane) {
        // 移除重复创建，直接使用已定义的 btnTogglePwd
        btnTogglePwd.setBorderPainted(false);
        btnTogglePwd.setFocusPainted(false);
        btnTogglePwd.setContentAreaFilled(false);
        btnTogglePwd.setBounds(656, 220, 20, 20);

        btnTogglePwd.addActionListener(e -> {
            if (isPasswordVisible) {
                txtPassword.setEchoChar('*');
                btnTogglePwd.setSvgIcon("/figures/eye_close.svg");
                isPasswordVisible = false;
            } else {
                txtPassword.setEchoChar((char) 0);
                btnTogglePwd.setSvgIcon("/figures/eye_open.svg");
                isPasswordVisible = true;
            }
        });

        layeredPane.add(btnTogglePwd, JLayeredPane.PALETTE_LAYER);
    }

    private void setupWindowEffects() {
        SwingUtilities.invokeLater(() -> {
            try {
                setWindowRounded();
                addSubtleShadow();
            } catch (Exception e) {
                log.warn("设置窗口效果失败: {}", e.getMessage());
            }
        });
    }

    private void setWindowRounded() {
        setBackground(new Color(0, 0, 0, 0));
        setShape(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 15, 15));
    }

    private void addSubtleShadow() {
        JLayeredPane layeredPane = (JLayeredPane) getContentPane();
        JPanel shadowPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                for (int i = 4; i > 0; i--) {
                    int alpha = Math.max(5, 20 - i * 3);
                    g2d.setColor(new Color(0, 0, 0, alpha));
                    g2d.fillRoundRect(i, i, getWidth() - i * 2, getHeight() - i * 2, 15, 15);
                }
                g2d.dispose();
            }
        };
        shadowPanel.setOpaque(false);
        shadowPanel.setBounds(0, 0, getWidth(), getHeight());
        layeredPane.add(shadowPanel, JLayeredPane.FRAME_CONTENT_LAYER);
        layeredPane.moveToBack(shadowPanel);
    }

    private void setupEventHandlers() {
        btnLogin.addActionListener(e -> handleLogin());
        txtPassword.addActionListener(e -> handleLogin());
        btnFaceLogin.addActionListener(e -> handleFaceLogin()); // 新增人脸识别登录事件
        addMouseListeners();

        addWindowStateListener(new WindowStateListener() {
            @Override
            public void windowStateChanged(WindowEvent e) {
                if (e.getNewState() == JFrame.NORMAL && e.getOldState() == JFrame.ICONIFIED) {
                    SwingUtilities.invokeLater(() -> {
                        setVisible(true);
                        toFront();
                        requestFocus();
                        repaint();
                    });
                }
            }
        });

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowDeiconified(WindowEvent e) {
                SwingUtilities.invokeLater(() -> {
                    setVisible(true);
                    toFront();
                    requestFocus();
                    setState(JFrame.NORMAL);
                });
            }
        });
    }

    // 预留人脸识别登录处理方法
    private void handleFaceLogin() {
        // 显示当前 classpath，便于调试 JAR 包加载问题
        String classpath = System.getProperty("java.class.path");
        StringBuilder cpMsg = new StringBuilder();
        boolean found = false;
        for (String path : classpath.split(";|:")) {
            if (path.contains("opencv-4110.jar")) {
                cpMsg.append(path).append("\n");
                found = true;
            }
        }
        if (found) {
            // 已在 classpath，调试弹窗移除
        } else {
            JOptionPane.showMessageDialog(this, "[人脸识别] opencv-4110.jar 未在 classpath！请检查 gradle/IDE依赖配置。", "错误", JOptionPane.ERROR_MESSAGE);
            return;
        }
        try {
            // 自动查找 DLL 路径
            String[] dllDirs = {"client/lib", "lib", "../lib", "./lib"};
            String dllName = "opencv_java4110.dll";
            String foundDllPath = null;
            // 当前工作目录调试弹窗移除
            StringBuilder dllPathsMsg = new StringBuilder("[人脸识别] 尝试查找 OpenCV DLL，以下是所有尝试路径:\n");
            for (String dir : dllDirs) {
                String candidate = new java.io.File(dir, dllName).getAbsolutePath();
                dllPathsMsg.append(candidate).append("\n");
                if (new java.io.File(candidate).exists()) {
                    foundDllPath = candidate;
                    // 找到 DLL，调试弹窗移除
                    break;
                }
            }
            // DLL 路径调试弹窗移除
            // 检查 opencv-4110.jar 是否在 classpath
            boolean jarLoaded = false;
            try {
                Class.forName("org.opencv.core.Mat");
                jarLoaded = true;
            } catch (ClassNotFoundException e) {
                jarLoaded = false;
            }
            if (!jarLoaded) {
                JOptionPane.showMessageDialog(this, "[人脸识别] opencv-4110.jar 未被正确加载到 classpath！请检查 gradle/IDE依赖配置。", "错误", JOptionPane.ERROR_MESSAGE);
                return;
            } else {
                // classloader 识别成功调试弹窗移除
            }
            if (foundDllPath == null) {
                JOptionPane.showMessageDialog(this, "[人脸识别] OpenCV DLL未找到，请确认 DLL 是否存在于 client/lib 或 lib 目录。", "错误", JOptionPane.ERROR_MESSAGE);
                return;
            }
            //JOptionPane.showMessageDialog(this, "[人脸识别] DLL加载成功: " + foundDllPath, "成功", JOptionPane.INFORMATION_MESSAGE);
            System.load(foundDllPath);

            // 检查人脸检测模型
            String cascadePath = new java.io.File("resources/haarcascade_frontalface_default.xml").getAbsolutePath();
            //JOptionPane.showMessageDialog(this, "[人脸识别] 检测模型路径: " + cascadePath, "调试信息", JOptionPane.INFORMATION_MESSAGE);
            if (!new java.io.File(cascadePath).exists()) {
                JOptionPane.showMessageDialog(this, "[人脸识别] 人脸检测模型未找到: " + cascadePath, "错误", JOptionPane.ERROR_MESSAGE);
                return;
            } else {
                //JOptionPane.showMessageDialog(this, "[人脸识别] 人脸检测模型已找到，大小: " + new java.io.File(cascadePath).length() + " 字节", "成功", JOptionPane.INFORMATION_MESSAGE);
            }

            // 打开摄像头，连续拍摄3~5张图片
            //JOptionPane.showMessageDialog(this, "[人脸识别] 正在尝试打开摄像头并采集多张图片...", "调试信息", JOptionPane.INFORMATION_MESSAGE);
            VideoCapture camera = new VideoCapture(0);
            if (!camera.isOpened()) {
                JOptionPane.showMessageDialog(this, "[人脸识别] 摄像头打开失败", "错误", JOptionPane.ERROR_MESSAGE);
                return;
            }
            JOptionPane.showMessageDialog(this, "[人脸识别] 摄像头打开成功！", "成功", JOptionPane.INFORMATION_MESSAGE);
            // 创建临时文件夹
            java.io.File faceRootDir = new java.io.File("faces");
            if (!faceRootDir.exists()) faceRootDir.mkdirs();
            java.io.File tempDir = new java.io.File(faceRootDir, "temp_faces");
            if (!tempDir.exists()) tempDir.mkdirs();
            int numPhotos = 5;
            for (int i = 0; i < numPhotos; i++) {
                Mat frame = new Mat();
                camera.read(frame);
                // 转换为BGR格式
                if (frame.channels() == 1) {
                    Mat bgrFrame = new Mat();
                    org.opencv.imgproc.Imgproc.cvtColor(frame, bgrFrame, org.opencv.imgproc.Imgproc.COLOR_GRAY2BGR);
                    frame = bgrFrame;
                } else if (frame.channels() == 4) {
                    Mat bgrFrame = new Mat();
                    org.opencv.imgproc.Imgproc.cvtColor(frame, bgrFrame, org.opencv.imgproc.Imgproc.COLOR_BGRA2BGR);
                    frame = bgrFrame;
                }
                String photoPath = tempDir.getAbsolutePath() + java.io.File.separator + i + ".jpg";
                org.opencv.imgcodecs.Imgcodecs.imwrite(photoPath, frame);
                //JOptionPane.showMessageDialog(this, "[人脸识别] 已采集第" + (i+1) + "张图片: " + photoPath, "调试信息", JOptionPane.INFORMATION_MESSAGE);
                try { Thread.sleep(400); } catch (Exception ignore) {} // 间隔采集
            }
            camera.release();
            //JOptionPane.showMessageDialog(this, "[人脸识别] 多张图片采集完成！", "成功", JOptionPane.INFORMATION_MESSAGE);

            // 检测人脸（多张图片）
            //JOptionPane.showMessageDialog(this, "[人脸识别] 开始多张图片人脸检测...", "调试信息", JOptionPane.INFORMATION_MESSAGE);
            CascadeClassifier faceDetector = new CascadeClassifier(cascadePath);
            java.io.File[] tempPhotos = tempDir.listFiles((dir, name) -> name.endsWith(".jpg") || name.endsWith(".png") || name.endsWith(".jpeg"));
            int validFaceCount = 0;
            for (java.io.File photo : tempPhotos) {
                Mat img = org.opencv.imgcodecs.Imgcodecs.imread(photo.getAbsolutePath());
                MatOfRect faceDetections = new MatOfRect();
                faceDetector.detectMultiScale(img, faceDetections);
                Rect[] facesArray = faceDetections.toArray();
                if (facesArray.length > 0) validFaceCount++;
            }
            if (validFaceCount == 0) {
                JOptionPane.showMessageDialog(this, "[人脸识别] 未检测到人脸，请重试", "错误", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // 遍历 faces 下所有 jpg/png/jpeg 图片
            java.io.File faceDir = faceRootDir;
            java.io.File[] faceFiles = faceDir.listFiles((dir, name) -> name.endsWith(".jpg") || name.endsWith(".png") || name.endsWith(".jpeg"));
            if (faceFiles == null || faceFiles.length == 0) {
                JOptionPane.showMessageDialog(this, "[人脸识别] 未找到人脸库图片", "错误", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // 多张图片与人脸库批量比对（均方误差MSE）
            String matchedCardNum = null;
            String matchedPassword = null;
            double minMSE = Double.MAX_VALUE;
            for (java.io.File faceFile : faceFiles) {
                String fileName = faceFile.getName().replace(".jpg","").replace(".png","").replace(".jpeg","");
                String[] parts = fileName.split("_");
                if (parts.length < 2) continue;
                String cardNum = parts[0];
                String password = parts[1];
                double totalMSE = 0;
                int compareCount = 0;
                for (java.io.File tempPhoto : tempPhotos) {
                    Mat userFace = org.opencv.imgcodecs.Imgcodecs.imread(faceFile.getAbsolutePath());
                    Mat tempFace = org.opencv.imgcodecs.Imgcodecs.imread(tempPhoto.getAbsolutePath());
                    if (userFace.empty() || tempFace.empty()) continue;
                    // 尺寸归一化（自动适配不同分辨率）
                    org.opencv.imgproc.Imgproc.resize(tempFace, tempFace, userFace.size());
                    // 计算均方误差
                    Mat diff = new Mat();
                    org.opencv.core.Core.absdiff(userFace, tempFace, diff);
                    diff.convertTo(diff, org.opencv.core.CvType.CV_32F);
                    double mse = org.opencv.core.Core.sumElems(diff).val[0] / (diff.rows() * diff.cols());
                    totalMSE += mse;
                    compareCount++;
                }
                double avgMSE = compareCount > 0 ? totalMSE / compareCount : Double.MAX_VALUE;
                if (avgMSE < minMSE) {
                    minMSE = avgMSE;
                    matchedCardNum = cardNum;
                    matchedPassword = password;
                }
            }

            // 判断距离阈值，MSE小于2000为匹配（可根据实际情况调整），否则弹窗不在人脸库内
            if (minMSE < 2000 && matchedCardNum != null && matchedPassword != null) {
                JOptionPane.showMessageDialog(this, "[人脸识别] 登录成功！匹配用户: " + matchedCardNum, "成功", JOptionPane.INFORMATION_MESSAGE);
                txtUsername.setText(matchedCardNum);
                txtPassword.setText(matchedPassword);
                handleLogin();
            } else {
                JOptionPane.showMessageDialog(this, "无匹配人脸，登录失败", "登录失败", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "[人脸识别] 登录异常: " + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleLogin() {
        // 获取文本并处理占位符
        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword());
        
        // 如果文本是占位符，则视为空
        if ("用户名".equals(username)) {
            username = "";
        }
        if ("密码".equals(password)) {
            password = "";
        }

        boolean usernameEmpty = username.isEmpty();
        boolean passwordEmpty = password.isEmpty();

        // 根据验证结果显示或隐藏错误标签，并设置颜色
        if (usernameEmpty) {
            lblUsernameError.setText("用户名不能为空！");
            lblUsernameError.setForeground(new Color(255, 0, 0)); // 使用纯红色
            lblUsernameError.setVisible(true); // 显示错误标签
        } else {
            lblUsernameError.setVisible(false); // 完全隐藏错误标签
        }

        if (passwordEmpty) {
            lblPasswordError.setText("密码不能为空！");
            lblPasswordError.setForeground(new Color(255, 0, 0)); // 使用纯红色
            lblPasswordError.setVisible(true); // 显示错误标签
        } else {
            lblPasswordError.setVisible(false); // 完全隐藏错误标签
        }

        if (usernameEmpty || passwordEmpty) {
            return;
        }

        String cardNum = username;

        log.info("用户尝试登录: cardNum={}", cardNum);

        try {
            Response response = performLoginWithNetty(cardNum, password);

            if (response != null && "SUCCESS".equals(response.getStatus())) {
                @SuppressWarnings("unchecked")
                Map<String, Object> userData = (Map<String, Object>) response.getData();

                // 添加调试日志，显示服务器返回的完整数据
                log.info("服务器返回的用户数据: {}", userData);

                // 统一解析用户类型与用户名，兼容多种后端字段命名
                String userType = resolveUserType(userData);
                String userName = resolveUserName(userData);

                log.info("解析的用户类型: '{}', 用户名: '{}'", userType, userName);

                storeUserInfo(cardNum, userType, userName);
                log.info("登录成功，用户: {} ({})", userName, userType);

                // 根据用户类型跳转到不同的界面
                openFrameBasedOnUserType(userData);
            } else {
                String errorMessage = response != null ? response.getMessage() : "无服务器响应";
                if (errorMessage.toLowerCase().contains("user not found") || errorMessage.contains("用户不存在")) {
                    lblUsernameError.setText("用户名不存在！");
                    lblUsernameError.setForeground(new Color(255, 0, 0)); // 使用纯红色
                    lblUsernameError.setVisible(true); // 显示用户名错误
                    lblPasswordError.setVisible(false); // 隐藏密码错误
                } else {
                    lblPasswordError.setText("用户名或密码错误");
                    lblPasswordError.setForeground(new Color(255, 0, 0)); // 使用纯红色
                    lblPasswordError.setVisible(true); // 显示密码错误
                    lblUsernameError.setVisible(false); // 隐藏用户名错误
                }
                log.warn("登录失败: {}", errorMessage);
            }
        } catch (Exception e) {
            log.error("登录过程中发生错误", e);
            JOptionPane.showMessageDialog(this,
                    "登录失败: " + e.getMessage(),
                    "系统错误",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    // 解析服务器返回的用户类型，兼容 primaryRole / userType / role / type 等字段
    private String resolveUserType(Map<String, Object> userData) {
        if (userData == null) return "";
        Object v = userData.get("primaryRole");
        if (v == null) v = userData.get("userType");
        if (v == null) v = userData.get("role");
        if (v == null) v = userData.get("type");
        if (v == null) return "";
        return String.valueOf(v).toLowerCase();
    }

    // 解析用户名，兼容 userName / name / displayName
    private String resolveUserName(Map<String, Object> userData) {
        if (userData == null) return "";
        Object v = userData.get("userName");
        if (v == null) v = userData.get("name");
        if (v == null) v = userData.get("displayName");
        return v == null ? "" : String.valueOf(v);
    }

    // 新增方法：根据用户类型打开对应的界面
    private void openFrameBasedOnUserType(Map<String, Object> userData) {
        try {
            // 使用兼容解析函数获取类型与用户名
            String userType = resolveUserType(userData);
            String userName = resolveUserName(userData);

            log.info("在 openFrameBasedOnUserType 中，用户类型: '{}', 用户名: '{}'", userType, userName);

            if ("student".equalsIgnoreCase(userType)) {
                // 打开学生界面
                StudentFrame studentFrame = new StudentFrame(nettyClient, userData);
                studentFrame.setVisible(true);
                // 创建并显示独立的悬浮猫窗口，绑定到学生界面右下角
                try {
                    FloatingCatWindow cat = new FloatingCatWindow(studentFrame);
                    SwingUtilities.invokeLater(() -> {
                        cat.setVisible(true);
                        cat.toFront();
                    });
                } catch (Exception ignore) {}
                log.info("学生界面打开成功，用户: {}", userName);
            } else if ("staff".equalsIgnoreCase(userType) || "teacher".equalsIgnoreCase(userType)) {
                // 打开教职工界面
                StaffFrame staffFrame = new StaffFrame(nettyClient, userData);
                staffFrame.setVisible(true);
                try {
                    FloatingCatWindow cat = new FloatingCatWindow(staffFrame);
                    SwingUtilities.invokeLater(() -> {
                        cat.setVisible(true);
                        cat.toFront();
                    });
                } catch (Exception ignore) {}
                log.info("教职工界面打开成功，用户: {}", userName);
            } else if ("admin".equalsIgnoreCase(userType) || "manager".equalsIgnoreCase(userType)) {
                // 打开管理员界面
                AdminFrame adminFrame = new AdminFrame(nettyClient, userData);
                adminFrame.setVisible(true);
                try {
                    FloatingCatWindow cat = new FloatingCatWindow(adminFrame);
                    SwingUtilities.invokeLater(() -> {
                        cat.setVisible(true);
                        cat.toFront();
                    });
                } catch (Exception ignore) {}
                log.info("管理员界面打开成功，用户: {}", userName);
            } else {
                // 默认打开原来的主界面
                log.warn("未知的用户类型: '{}'，打开默认主界面", userType);
                // mainFrame = new MainFrame(currentUserCardNum, currentUserType, currentUserName);
                // mainFrame.setVisible(true);
                log.info("主界面打开成功，用户: {} ({})", currentUserName, currentUserType);
            }

            // 隐藏登录窗口
            this.setVisible(false);
        } catch (Exception e) {
            log.error("💥 界面打开失败", e);
            JOptionPane.showMessageDialog(this,
                    "界面打开失败: " + e.getMessage(),
                    "错误",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private Response performLoginWithNetty(String cardNum, String password) {
        try {
            log.info("使用 Netty 客户端进行登录: cardNum={}", cardNum);

            if (!nettyClient.isConnected()) {
                log.info("正在连接到服务器...");
                nettyClient.connect().get(5, java.util.concurrent.TimeUnit.SECONDS);
            }

            Request loginRequest = new Request("auth/login")
                    .addParam("username", cardNum)
                    .addParam("password", password);

            Response response = nettyClient.sendRequest(loginRequest).get(10, java.util.concurrent.TimeUnit.SECONDS);

            return response;
        } catch (Exception e) {
            log.error("Netty 登录错误", e);
            return null;
        }
    }

    private void storeUserInfo(String cardNum, String userType, String userName) {
        this.currentUserCardNum = cardNum;
        this.currentUserType = userType;
        this.currentUserName = userName;
    }

    private void openMainFrame() {
        try {
            // mainFrame = new MainFrame(currentUserCardNum, currentUserType, currentUserName);
            // mainFrame.setVisible(true);
            this.setVisible(false);
            log.info("主界面打开成功，用户: {} ({})", currentUserName, currentUserType);
        } catch (Exception e) {
            log.error("💥 主界面打开失败", e);
            JOptionPane.showMessageDialog(this,
                    "主界面打开失败: " + e.getMessage(),
                    "错误",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
    private void addMouseListeners() {
    }
    public void addComponent(JPanel panel, JComponent comp, int x, int y, int w, GridBagConstraints gbc) {
        gbc.gridx = x;
        gbc.gridy = y;
        gbc.gridwidth = w;
        panel.add(comp, gbc);
    }
    
}