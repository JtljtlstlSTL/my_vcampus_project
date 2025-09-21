package com.vcampus.client.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import uk.co.caprica.vlcj.player.component.EmbeddedMediaPlayerComponent;

public class VideoPlayerPanel extends JPanel {
    // 媒体组件延迟创建，避免在没有本地 libvlc 时应用启动即崩溃
    private EmbeddedMediaPlayerComponent mediaPlayerComponent;
    private final JPanel mediaContainer = new JPanel(new BorderLayout());

    // 仅保留音量滑块用于调节音量，保留进度条用于跳转
    private final JSlider progressSlider = new JSlider();
    private final JSlider volumeSlider = new JSlider(0, 100, 80);

    private Timer progressTimer;

    // 内置资源路径
    private static final String BUNDLED_VIDEO_RESOURCE = "/video/1.mp4"; // 修改为 resources 下的 video 目录

    // 当前播放文件（临时或用户选择）
    private File currentMediaFile;

    // 取消内置视频选择UI，始终使用默认内置资源或外部传入资源
    private String selectedBundledResource = BUNDLED_VIDEO_RESOURCE;

    // 移除界面上的音量控件，音量使用默认值（80）
    private static final int DEFAULT_VOLUME = 80;

    public VideoPlayerPanel() {
        setLayout(new BorderLayout());

        // 在构造时强制指定工程内的 VLC 路径，避免用户手动设置
        setDefaultVlcPath();
        // 在界面打开时尽量静默初始化 VLC（若可用），避免弹出错误提示打断用户
        ensureMediaPlayerCreated(true);

        // 媒体容器（延迟填充 EmbeddedMediaPlayerComponent）
        mediaContainer.setBackground(Color.BLACK);

        // 控制条：保留音量控件（不再显示内置视频选择）
        JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT));
        controls.add(new JLabel(" 音量 "));
        controls.add(volumeSlider);

        add(mediaContainer, BorderLayout.CENTER);
        add(controls, BorderLayout.NORTH);
        add(progressSlider, BorderLayout.SOUTH);

        // 初始状态：进度条不可用
        progressSlider.setEnabled(false);

        // 音量监听
        volumeSlider.addChangeListener(e -> {
            int v = volumeSlider.getValue();
            if (mediaPlayerComponent != null) {
                try { mediaPlayerComponent.mediaPlayer().audio().setVolume(v); } catch (Throwable ignored) {}
            }
        });

        // 媒体容器点击交互：单击在未加载媒体时启动播放；双击切换播放/暂停（双击暂停，再次双击播放）
        mediaContainer.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                try {
                    if (e.getClickCount() == 2) {
                        // 双击：切换播放/暂停（若尚未创建播放器，先初始化）
                        if (!ensureMediaPlayerCreated()) return;
                        try {
                            boolean playing = false;
                            try { playing = mediaPlayerComponent.mediaPlayer().status().isPlaying(); } catch (Throwable ignored) {}
                            if (playing) mediaPlayerComponent.mediaPlayer().controls().pause();
                            else mediaPlayerComponent.mediaPlayer().controls().play();
                        } catch (Throwable ignored) {}
                        return;
                    }

                    if (e.getClickCount() == 1) {
                        // 单击：仅在尚未加载媒体时触发加载并播放，避免与双击冲突
                        if (!ensureMediaPlayerCreated()) return;
                        if (currentMediaFile == null) {
                            loadBundledAndPlay(selectedBundledResource);
                        }
                    }
                } catch (Throwable ignored) {}
            }
        });

        // 使面板能接收键盘事件，并注册全局按键（WHEN_IN_FOCUSED_WINDOW）
        setFocusable(true);
        // 注册按键：Esc -> 关闭并释放资源；Space -> 切换播放/暂停
        InputMap im = getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap am = getActionMap();
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "exit");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0), "togglePause");
        am.put("exit", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) {
                try {
                    // 停止播放并释放资源
                    try { stop(); } catch (Throwable ignored) {}
                    try { dispose(); } catch (Throwable ignored) {}
                    // 关闭所属窗口（若为模态对话框则退出模态）
                    Window w = SwingUtilities.getWindowAncestor(VideoPlayerPanel.this);
                    if (w != null) w.dispose();
                } catch (Throwable ignored) {}
            }
        });
        am.put("togglePause", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) {
                try { togglePlayPause(); } catch (Throwable ignored) {}
            }
        });

        progressSlider.setMinimum(0);
        progressSlider.setMaximum(1000);
        progressSlider.setValue(0);
        progressSlider.setEnabled(false);

        progressSlider.addMouseListener(new MouseAdapter() {
            private boolean dragging = false;
            @Override public void mousePressed(MouseEvent e) { dragging = true; }
            @Override public void mouseReleased(MouseEvent e) {
                if (dragging && mediaPlayerComponent != null) {
                    try {
                        if (mediaPlayerComponent.mediaPlayer().status().length() > 0) {
                            int value = progressSlider.getValue();
                            long length = mediaPlayerComponent.mediaPlayer().status().length();
                            long pos = (long) (value / 1000.0 * length);
                            mediaPlayerComponent.mediaPlayer().controls().setTime(pos);
                        }
                    } catch (Throwable ignored) {}
                }
                dragging = false;
            }
        });

        // 进度定时器
        progressTimer = new Timer(500, e -> updateProgress());
    }


    // 将原有默认加载方法改为调用带参数的实现，保持兼容
    private void loadBundledAndPlay() {
        loadBundledAndPlay(BUNDLED_VIDEO_RESOURCE);
    }

    private void loadBundledAndPlay(String resourcePath) {
        try {
            URL res = getClass().getResource(resourcePath);
            if (res == null) {
                SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this, "未找到内置视频资源: " + resourcePath, "错误", JOptionPane.ERROR_MESSAGE));
                return;
            }

            File mediaFile;
            String protocol = res.getProtocol();
            if ("file".equalsIgnoreCase(protocol)) {
                mediaFile = new File(res.toURI());
            } else {
                try (InputStream in = getClass().getResourceAsStream(resourcePath)) {
                    if (in == null) {
                        SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this, "无法读取内置视频资源流", "错误", JOptionPane.ERROR_MESSAGE));
                        return;
                    }
                    mediaFile = File.createTempFile("vcampus_bundled_video_", ".mp4");
                    mediaFile.deleteOnExit();
                    try (OutputStream out = Files.newOutputStream(mediaFile.toPath())) {
                        byte[] buf = new byte[8192];
                        int r;
                        while ((r = in.read(buf)) != -1) out.write(buf, 0, r);
                    }
                }
            }

            currentMediaFile = mediaFile;
            if (ensureMediaPlayerCreated()) startPlay(currentMediaFile);
        } catch (Exception ex) {
            SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this, "加载内置视频失败: " + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE));
        }
    }

    private void startPlay(File file) {
        if (file == null || !file.exists()) return;
        // 停止之前的播放
        try { mediaPlayerComponent.mediaPlayer().controls().stop(); } catch (Exception ignored) {}

        mediaPlayerComponent.mediaPlayer().media().play(file.getAbsolutePath());
        try { mediaPlayerComponent.mediaPlayer().audio().setVolume(volumeSlider.getValue()); } catch (Throwable ignored) {}

        SwingUtilities.invokeLater(() -> {
            progressSlider.setEnabled(true);
        });
        progressTimer.start();
    }

    private void pause() {
        mediaPlayerComponent.mediaPlayer().controls().pause();
        // 暂停后不显示任何按钮文本（所有按钮已移除）；保留进度条显示
    }

    private void stop() {
        mediaPlayerComponent.mediaPlayer().controls().stop();
        progressTimer.stop();
        SwingUtilities.invokeLater(() -> {
            progressSlider.setValue(0);
        });
    }

    private void updateProgress() {
        if (mediaPlayerComponent.mediaPlayer().status().length() > 0) {
            long time = mediaPlayerComponent.mediaPlayer().status().time();
            long len = mediaPlayerComponent.mediaPlayer().status().length();
            int value = (int) Math.round(time * 1000.0 / len);
            SwingUtilities.invokeLater(() -> progressSlider.setValue(value));
        }
    }

    // 释放资源
    public void dispose() {
        try {
            progressTimer.stop();
            if (mediaPlayerComponent != null) mediaPlayerComponent.release();
        } catch (Exception ignored) {}
    }

    /**
     * 确保 mediaPlayerComponent 已创建。若无法创建（缺少本地 libvlc），显示帮助信息并返回 false。
     */
    private boolean ensureMediaPlayerCreated() {
        if (mediaPlayerComponent != null) return true;
        try {
            // 先尝试自动检测常见的 VLC 安装路径（仅 Windows 常见位置）
            tryAutoDetectVlc();

            // 尝试创建，可能抛出 UnsatisfiedLinkError
            mediaPlayerComponent = new EmbeddedMediaPlayerComponent();
            SwingUtilities.invokeLater(() -> {
                mediaContainer.removeAll();
                mediaContainer.add(mediaPlayerComponent, BorderLayout.CENTER);
                mediaContainer.revalidate();
                mediaContainer.repaint();
            });
            return true;
        } catch (Throwable t) {
            // 常见原因是本地 libvlc 未安装或未在 PATH/JNA 路径中
            // 输出完整异常和运行时关键环境，便于排查
            try {
                t.printStackTrace();
                String info = buildVlcDebugInfo(t);
                System.err.println(info);

                // 更明确的提示：包括硬编码路径说明和位数匹配建议
                String hard1 = "C:\\Users\\sin_0\\Desktop\\my_vcampus_project\\libs\\vlc";
                String hard2 = "C:\\Users\\sin_0\\Desktop\\my_vcampus_project\\client\\libs\\vlc";
                String msg = "无法初始化本地 VLC 库(libvlc)。\n" +
                        "请确保已将与 JVM 位数匹配的 VLC 放置到以下任一目录，或在窗口中手动指定 VLC 安装目录：\n\n" +
                        "1) " + hard1 + "\n" +
                        "2) " + hard2 + "\n\n" +
                        "如果你使用 64 位 VLC，请确认 JVM 也是 64 位 (os.arch=amd64)。\n" +
                        "详细诊断已打印到控制台（包含 jna.library.path、VLC_PLUGIN_PATH、PATH 前/后片段以及 libvlc.dll 是否存在）。\n\n" +
                        "错误类型: " + t.getClass().getSimpleName() + "\n消息: " + t.getMessage();

                SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this, msg, "本地库缺失", JOptionPane.ERROR_MESSAGE));
            } catch (Throwable ignored) {}
            return false;
        }
    }

    // 新增：支持静默或非静默初始化的重载方法
    private boolean ensureMediaPlayerCreated(boolean silent) {
        if (mediaPlayerComponent != null) return true;
        try {
            // 先尝试自动检测常见的 VLC 安装路径（仅 Windows 常见位置）
            tryAutoDetectVlc();

            // 尝试创建，可能抛出 UnsatisfiedLinkError
            mediaPlayerComponent = new EmbeddedMediaPlayerComponent();
            SwingUtilities.invokeLater(() -> {
                mediaContainer.removeAll();
                mediaContainer.add(mediaPlayerComponent, BorderLayout.CENTER);
                mediaContainer.revalidate();
                mediaContainer.repaint();
            });
            return true;
        } catch (Throwable t) {
            if (!silent) {
                try {
                    t.printStackTrace();
                    String info = buildVlcDebugInfo(t);
                    System.err.println(info);
                    String msg = "无法初始化本地 VLC 库(libvlc)。\n请确保已安装与 JVM 位数匹配的 VLC，并将其安装目录加入系统 PATH，或在此窗口中点击 '设置 VLC 路径' 指定 VLC 安装目录。\n\n详细信息(已打印至控制台)：\n" + t.getClass().getSimpleName() + ": " + t.getMessage();
                    SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this, msg, "本地库缺失", JOptionPane.ERROR_MESSAGE));
                } catch (Throwable ignored) {}
            }
            return false;
        }
    }

    // 尝试自动检测 VLC 安装目录并设置 JNA 路径与插件路径（Windows 常用位置）
    private void tryAutoDetectVlc() {
        String os = System.getProperty("os.name").toLowerCase();
        if (!os.contains("win")) return; // 目前仅为 Windows 添加自动检测

        String[] candidates = new String[] {
                System.getenv("VLC_HOME"),
                // 优先检查项目内的本地 libvlc（便于无需全局安装）
                System.getProperty("user.dir") + File.separator + "client" + File.separator + "libs" + File.separator + "vlc",
                System.getenv("ProgramFiles") + "\\VideoLAN\\VLC",
                System.getenv("ProgramFiles(x86)") + "\\VideoLAN\\VLC",
                "C:\\Program Files\\VideoLAN\\VLC",
                "C:\\Program Files (x86)\\VideoLAN\\VLC"
        };

        for (String path : candidates) {
            try {
                if (path == null) continue;
                File dir = new File(path);
                if (dir.exists() && dir.isDirectory()) {
                    // 可能存在 libvlc.dll
                    File lib = new File(dir, "libvlc.dll");
                    if (lib.exists()) {
                        System.setProperty("jna.library.path", dir.getAbsolutePath());
                        File plugins = new File(dir, "plugins");
                        if (plugins.exists()) {
                            System.setProperty("VLC_PLUGIN_PATH", plugins.getAbsolutePath());
                        }
                        return;
                    }
                }
            } catch (Exception ignored) {}
        }
    }

    // 将默认 VLC 路径设为项目内的 libs/vlc 或 client/libs/vlc（优先级最高）
    private void setDefaultVlcPath() {
        try {
            // 强制使用绝对硬编码路径（项目工作区已知位置）
            // 请根据实际开发机路径修改下列硬编码路径
            String hardPath1 = "C:\\Users\\sin_0\\Desktop\\my_vcampus_project\\libs\\vlc"; // 优先使用此处
            String hardPath2 = "C:\\Users\\sin_0\\Desktop\\my_vcampus_project\\client\\libs\\vlc"; // 备用

            File f1 = new File(hardPath1);
            File f2 = new File(hardPath2);
            File chosen;
            if (f1.exists() && f1.isDirectory()) chosen = f1;
            else if (f2.exists() && f2.isDirectory()) chosen = f2;
            else chosen = f1; // 即使不存在也设置为硬编码路径，方便用户复制后生效

            // 如果在硬编码路径中未找到 libvlc.dll 或 plugins，尝试回退到系统安装位置
            File lib = new File(chosen, "libvlc.dll");
            File plugins = new File(chosen, "plugins");
            if (!lib.exists() || !plugins.exists()) {
                String[] fallbacks = new String[] {
                        System.getenv("ProgramFiles") + "\\VideoLAN\\VLC",
                        System.getenv("ProgramFiles(x86)") + "\\VideoLAN\\VLC",
                        "C:\\Program Files\\VideoLAN\\VLC",
                        "C:\\Program Files (x86)\\VideoLAN\\VLC"
                };
                for (String p : fallbacks) {
                    try {
                        if (p == null) continue;
                        File d = new File(p);
                        File dl = new File(d, "libvlc.dll");
                        File dp = new File(d, "plugins");
                        if (d.exists() && d.isDirectory() && dl.exists() && dp.exists()) {
                            chosen = d;
                            lib = dl;
                            plugins = dp;
                            break;
                        }
                    } catch (Exception ignored) {}
                }
            }

            System.setProperty("jna.library.path", chosen.getAbsolutePath());
            if (plugins.exists()) {
                System.setProperty("VLC_PLUGIN_PATH", plugins.getAbsolutePath());
            }

            // 记录当前尝试的路径，便于调试
            try {
                String jna = System.getProperty("jna.library.path");
                String vpp = System.getProperty("VLC_PLUGIN_PATH");
                String arch = System.getProperty("os.arch");
                System.err.println("setDefaultVlcPath (hardcoded+fallback): chosen=" + chosen.getAbsolutePath() + ", libvlc.exists=" + (new File(chosen, "libvlc.dll").exists()) + ", plugins.exists=" + (new File(chosen, "plugins").exists()) + ", jna.library.path=" + jna + ", VLC_PLUGIN_PATH=" + vpp + ", os.arch=" + arch);
            } catch (Throwable ignored) {}

            // 尝试将该路径临时加入当前进程的 PATH 环境，帮助本地加载 DLL
            try {
                String pathEnv = System.getenv("PATH");
                if (pathEnv == null) pathEnv = "";
                String newPath = chosen.getAbsolutePath() + File.pathSeparator + pathEnv;
                java.lang.reflect.Field field = java.lang.ProcessBuilder.class.getDeclaredField("environment");
                field.setAccessible(true);
                @SuppressWarnings("unchecked")
                java.util.Map<String, String> env = (java.util.Map<String, String>) field.get(new java.lang.ProcessBuilder());
                env.put("PATH", newPath);
            } catch (Throwable ignored) {}
        } catch (Exception ignored) {}
    }

    // 新增：构建调试信息字符串，包含关键环境变量及异常信息
    private String buildVlcDebugInfo(Throwable t) {
        StringBuilder sb = new StringBuilder();
        try {
            sb.append("VLC 初始化异常: ").append(t == null ? "null" : t.toString()).append("\n");
            sb.append("jna.library.path=").append(System.getProperty("jna.library.path")).append("\n");
            sb.append("VLC_PLUGIN_PATH=").append(System.getProperty("VLC_PLUGIN_PATH")).append("\n");
            sb.append("PATH(env) (仅前/后200字符)=");
            String path = System.getenv("PATH");
            if (path != null && path.length() > 400) {
                sb.append(path.substring(0,200)).append(" ... ");
                sb.append(path.substring(path.length()-200));
            } else sb.append(path);
            sb.append("\n");
            sb.append("user.dir=").append(System.getProperty("user.dir")).append("\n");
            sb.append("os.name=").append(System.getProperty("os.name")).append("\n");
            sb.append("os.arch=").append(System.getProperty("os.arch")).append("\n");
            sb.append("java.version=").append(System.getProperty("java.version")).append("\n");
            // 检查是否存在 libvlc.dll 在已设置的 jna 路径
            try {
                String jna = System.getProperty("jna.library.path");
                if (jna != null) {
                    File jdir = new File(jna);
                    File lib = new File(jdir, "libvlc.dll");
                    sb.append("libvlc.dll exists in jna path=").append(lib.exists()).append(" (checked=").append(lib.getAbsolutePath()).append(")\n");
                    File plugins = new File(jdir, "plugins");
                    sb.append("plugins exists in jna path=").append(plugins.exists()).append(" (checked=").append(plugins.getAbsolutePath()).append(")\n");
                }
            } catch (Throwable ignored) {}
        } catch (Throwable ignored) {}
        return sb.toString();
    }

    // 列出 resources/video 目录下的所有文件名，支持 file:// 和 jar:// 两种场景
    private List<String> listBundledVideoNames() {
        List<String> names = new ArrayList<>();
        try {
            URL dirURL = getClass().getResource("/video/");
            if (dirURL != null) {
                String protocol = dirURL.getProtocol();
                if ("file".equalsIgnoreCase(protocol)) {
                    File folder = new File(dirURL.toURI());
                    File[] files = folder.listFiles();
                    if (files != null) {
                        for (File f : files) if (f.isFile()) names.add(f.getName());
                    }
                    return names;
                }

                if ("jar".equalsIgnoreCase(protocol)) {
                    // dirURL like: jar:file:/path/to/jarfile.jar!/video/
                    String path = dirURL.getPath();
                    int bang = path.indexOf("!");
                    String jarPath = path.substring(path.indexOf(":") + 1, bang);
                    jarPath = URLDecoder.decode(jarPath, "UTF-8");
                    try (JarFile jar = new JarFile(jarPath)) {
                        Enumeration<JarEntry> entries = jar.entries();
                        while (entries.hasMoreElements()) {
                            JarEntry entry = entries.nextElement();
                            String name = entry.getName();
                            if (name.startsWith("video/") && !name.endsWith("/")) {
                                names.add(name.substring("video/".length()));
                            }
                        }
                    }
                    return names;
                }
            }

            // 作为兜底，尝试通过类加载器扫描类路径中的 video/ 资源
            Enumeration<URL> resources = getClass().getClassLoader().getResources("video");
            while (resources.hasMoreElements()) {
                URL url = resources.nextElement();
                if (url.getProtocol().equals("file")) {
                    File folder = new File(url.toURI());
                    File[] files = folder.listFiles();
                    if (files != null) for (File f : files) if (f.isFile()) names.add(f.getName());
                }
                // jar 情况通常已在上面处理
            }
        } catch (Exception ignored) {}
        return names;
    }

    // 对外公开：直接播放指定的内置资源（resourcePath 形如 "/video/foo.mp4"）
    public void playBundledResource(String resourcePath) {
        if (resourcePath == null || resourcePath.isEmpty()) return;
        this.selectedBundledResource = resourcePath;
        // 确保媒体组件创建并播放
        if (ensureMediaPlayerCreated()) {
            loadBundledAndPlay(resourcePath);
        }
    }

    // 对外公开：播放外部文件
    public void playFile(File file) {
        if (file == null) return;
        if (!file.exists() || !file.isFile()) {
            SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this, "指定的媒体文件不存在: " + (file == null ? "null" : file.getAbsolutePath()), "错误", JOptionPane.ERROR_MESSAGE));
            return;
        }
        this.currentMediaFile = file;
        if (ensureMediaPlayerCreated()) {
            startPlay(file);
        }
    }

    // 对外：切换播放/暂停，返回当前是否处于播放中
    public boolean togglePlayPause() {
        try {
            if (mediaPlayerComponent == null) return false;
            boolean playing = false;
            try {
                playing = mediaPlayerComponent.mediaPlayer().status().isPlaying();
            } catch (Throwable ignored) {}
            if (playing) {
                try { mediaPlayerComponent.mediaPlayer().controls().pause(); } catch (Throwable ignored) {}
                return false;
            } else {
                try { mediaPlayerComponent.mediaPlayer().controls().play(); } catch (Throwable ignored) {}
                return true;
            }
        } catch (Throwable t) {
            return false;
        }
    }
}
