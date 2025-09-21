package com.vcampus.client.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.nio.file.*;
import static java.nio.file.StandardWatchEventKinds.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;

/**
 * 画廊面板：以卡片形式列出 resources/video 下的视频文件。
 * 点击卡片会打开一个模态对话框，内含独立的 VideoPlayerPanel（进入后显示视频内容，类似网站跳转行为）。
 */
 public class VideoGalleryPanel extends JPanel {
     // 画廊中可用的图片资源路径（形如 "course1"->"/figures/course1.png")
     private final java.util.Map<String, String> bundledImageResources = new java.util.HashMap<>();
     // 中央滚动区域引用，便于在点击卡片时替换为播放器
     private JScrollPane centerScrollPane;
     // 当前嵌入的播放器面板与其容器（用于停止并返回画廊）
     private VideoPlayerPanel currentPlayerPanel;
     private JPanel currentPlayerContainer;
     // WatchService 及线程，用于自动监听外部视频目录变化并刷新
     private WatchService watchService;
     private Thread watchThread;
     private volatile boolean watcherRunning = false;
     private java.util.List<Path> watchedDirs = new java.util.ArrayList<>();
     // 轮询器：作为 WatchService 的补充，定期主动扫描候选目录以捕获 IDE/构建系统未触发的情况
     private ScheduledExecutorService poller;
     private ScheduledFuture<?> pollerFuture;
     // 优先级：配置文件中 videos.path -> 硬编码仓库根路径下的 videos -> 向上查找祖先 videos -> user.dir/videos -> user.home/videos
     private static final String HARDCODED_PROJECT_ROOT = "C:\\Users\\sin_0\\Desktop\\my_vcampus_project";
     private final String configuredVideosPath;

     public VideoGalleryPanel() {
         this.configuredVideosPath = loadConfiguredVideosPath();
         setLayout(new BorderLayout());

         // 标题：改名为“云课堂”，并美化样式（加粗、增大、深色、底部分隔线）
         JLabel title = new JLabel("云课堂");
         title.setFont(new Font("微软雅黑", Font.BOLD, 20));
         title.setForeground(new Color(0x222222));
         title.setHorizontalAlignment(SwingConstants.LEFT);
         // 底部添加细线分隔，外层留白
         title.setBorder(BorderFactory.createCompoundBorder(
                 BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(0xE6E6E6)),
                 BorderFactory.createEmptyBorder(12, 12, 12, 12)
         ));
         // 放在容器左侧以保证与卡片对齐
         JPanel header = new JPanel(new BorderLayout());
         header.setBackground(new Color(0xF7F7F7));
         header.add(title, BorderLayout.WEST);

         // 不再显示手动刷新按钮，改为自动监听目录变化并刷新

         add(header, BorderLayout.NORTH);

         // 初次构建画廊（会自动优先扫描外部目录）
         refreshGallery();
         // 启动目录监听器用于自动刷新（若支持）
         //startDirectoryWatcher();
         // 启动轮询，作为備份機制（每2秒扫描一次候选目录）
         //startPollingWatcher();
     }

     // 每次面板加入到容器（显示）时，重新扫描一次，确保在切换页面/重新打开时能看到新增文件
     @Override
     public void addNotify() {
         super.addNotify();
         SwingUtilities.invokeLater(() -> refreshGallery());
         // 面板实际显示时（每次加入 UI 层级），启动 watcher 与 poller
         try {
             startDirectoryWatcher();
             startPollingWatcher();
         } catch (Throwable ignored) {}
     }

     @Override
     public void removeNotify() {
         // 面板被移除或窗口关闭时停止 watcher
         stopDirectoryWatcher();
         super.removeNotify();
     }

     // 启动 WatchService 来监听外部视频目录的变化并触发刷新
     private void startDirectoryWatcher() {
         // 如果已经存在 watcher，则不重复启动
         if (watchService != null || watcherRunning) return;
         try {
             // 候选目录（保持与 listAvailableVideoNames 一致），但我们将注册这些路径的现有祖先目录以便捕获后来创建的子目录
             String userDir = System.getProperty("user.dir");
             String userHome = System.getProperty("user.home");
             // 配置优先：如果配置提供了 videos.path，则优先使用
             String cfg = this.configuredVideosPath;
             // 尝试向上查找祖先的顶层 videos 目录并优先注册（如果没有配置）
             Path ancestorVideos = cfg == null ? findAncestorContainingDirectory(userDir, "videos") : null;
             String ancestorVideosPath = ancestorVideos == null ? null : ancestorVideos.toString();
             String hardcodedVideos = HARDCODED_PROJECT_ROOT + File.separator + "videos";
             String[] candidates = new String[]{
                     cfg,
                     hardcodedVideos,
                     ancestorVideosPath,
                     userDir + File.separator + "videos",
                     userHome + File.separator + "videos",
                     userDir + File.separator + "client" + File.separator + "build" + File.separator + "resources" + File.separator + "main" + File.separator + "video",
                     userDir + File.separator + "client" + File.separator + "build" + File.separator + "classes" + File.separator + "java" + File.separator + "main" + File.separator + "video",
                     userDir + File.separator + "client" + File.separator + "src" + File.separator + "main" + File.separator + "resources" + File.separator + "video"
             };

             // 记录要注册的目录（去重）——我们会注册候选路径本身如果存在，否则注册最近存在的祖先目录
             java.util.Set<Path> toRegister = new java.util.LinkedHashSet<>();
             for (String p : candidates) {
                 if (p == null || p.isEmpty()) continue;
                 try {
                     Path path = Paths.get(p);
                     if (Files.exists(path)) {
                         toRegister.add(path);
                     } else {
                         // 向上查找第一个存在的祖先目录并注册它，这样当 video 目录在运行时被创建时也能收到事件
                         Path parent = path;
                         while (parent != null && !Files.exists(parent)) parent = parent.getParent();
                         if (parent != null) toRegister.add(parent);
                     }
                 } catch (Throwable ignored) {}
             }
             // 总是至少注册项目根和用户主目录的存在祖先，保证不会空
             try { toRegister.add(Paths.get(userDir)); } catch (Throwable ignored) {}
             try { toRegister.add(Paths.get(userHome)); } catch (Throwable ignored) {}

             if (toRegister.isEmpty()) return;

             watchService = FileSystems.getDefault().newWatchService();
             for (Path base : toRegister) {
                 try {
                     if (!watchedDirs.contains(base)) {
                         base.register(watchService, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
                         watchedDirs.add(base);
                         System.err.println("VideoGalleryPanel: registered watch on: " + base);
                     }
                 } catch (Throwable ex) {
                     // 注册失败时继续尝试其它目录
                     System.err.println("VideoGalleryPanel: failed to register watch on " + base + ": " + ex.getMessage());
                 }
             }

             watcherRunning = true;
             watchThread = new Thread(() -> {
                 long lastRefresh = 0L;
                 while (watcherRunning) {
                     try {
                         WatchKey key = watchService.poll(500, TimeUnit.MILLISECONDS);
                         if (key == null) continue;
                         Path watchedDir = (Path) key.watchable();
                         boolean trigger = false;
                         for (WatchEvent<?> ev : key.pollEvents()) {
                             WatchEvent.Kind<?> kind = ev.kind();
                             if (kind == OVERFLOW) { trigger = true; continue; }

                             // 事件相对路径
                             Path rel = (Path) ev.context();
                             Path resolved = watchedDir.resolve(rel);

                             // 如果是目录被创建，且名字包含 "video"（按 path 元素判断），尝试对该目录注册 watcher（递增注册）
                             try {
                                 if (kind == ENTRY_CREATE) {
                                     if (Files.isDirectory(resolved)) {
                                         // 若新创建目录名或路径包含 video，则注册该目录以便监控其下文件
                                         if (pathContainsVideo(resolved)) {
                                             try {
                                                 if (!watchedDirs.contains(resolved)) {
                                                     resolved.register(watchService, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
                                                     watchedDirs.add(resolved);
                                                     System.err.println("VideoGalleryPanel: dynamically registered new video dir: " + resolved);
                                                 }
                                             } catch (Throwable ignored) {}
                                         }
                                     } else {
                                         // 新文件创建，若路径包含 /video/ 则触发刷新
                                         if (pathContainsVideo(resolved)) trigger = true;
                                     }
                                 } else if (kind == ENTRY_DELETE || kind == ENTRY_MODIFY) {
                                     if (pathContainsVideo(resolved)) trigger = true;
                                 }
                             } catch (Throwable ignored) {
                                 trigger = true; // 保守策略：任意异常都触发刷新
                             }
                         }
                         key.reset();
                         if (trigger) {
                           long now = System.currentTimeMillis();
                           // 简单去抖：1 秒内只刷新一次
                           if (now - lastRefresh > 1000) {
                               lastRefresh = now;
                               SwingUtilities.invokeLater(() -> refreshGallery());
                           }
                         }
                     } catch (InterruptedException ie) {
                         Thread.currentThread().interrupt();
                         break;
                     } catch (Throwable t) {
                         System.err.println("VideoGalleryPanel: watcher loop error: " + t.getMessage());
                         break;
                     }
                 }
             }, "VideoGallery-Watcher");
             watchThread.setDaemon(true);
             watchThread.start();
         } catch (Throwable ignored) {}
     }

     // 判断给定路径或其任一父路径名称是否为 video（大小写不敏感）
     private boolean pathContainsVideo(Path p) {
         try {
             for (Path cur = p; cur != null; cur = cur.getParent()) {
                 Path name = cur.getFileName();
                 if (name != null && "video".equalsIgnoreCase(name.toString())) return true;
             }
         } catch (Throwable ignored) {}
         return false;
     }

     private void stopDirectoryWatcher() {
         try {
             watcherRunning = false;
             if (watchService != null) {
                 try { watchService.close(); } catch (Throwable ignored) {}
                 watchService = null;
             }
             if (watchThread != null) {
                 try { watchThread.interrupt(); } catch (Throwable ignored) {}
                 watchThread = null;
             }
             watchedDirs.clear();
             stopPollingWatcher();
         } catch (Throwable ignored) {}
     }

    // 启动轮询器，周期性触发 refreshGallery()，用于处理 IDE/构建系统未产生文件事件的场景
    private void startPollingWatcher() {
        try {
            if (poller != null) return;
            poller = Executors.newSingleThreadScheduledExecutor(r -> {
                Thread t = new Thread(r, "VideoGallery-Poller"); t.setDaemon(true); return t;
            });
            // 初始延迟 2s，每 2s 执行一次
            pollerFuture = poller.scheduleAtFixedRate(() -> {
                try {
                    SwingUtilities.invokeLater(() -> refreshGallery());
                } catch (Throwable ignored) {}
            }, 2000, 2000, TimeUnit.MILLISECONDS);
            System.err.println("VideoGalleryPanel: polling watcher started");
        } catch (Throwable ignored) {}
    }

    private void stopPollingWatcher() {
        try {
            if (pollerFuture != null) {
                try { pollerFuture.cancel(true); } catch (Throwable ignored) {}
                pollerFuture = null;
            }
            if (poller != null) {
                try { poller.shutdownNow(); } catch (Throwable ignored) {}
                poller = null;
            }
            System.err.println("VideoGalleryPanel: polling watcher stopped");
        } catch (Throwable ignored) {}
    }

     // 刷新画廊：优先从外部目录读取视频文件，否则回退到打包资源
     private void refreshGallery() {
         try {
             java.util.List<String> names = listAvailableVideoNames();
             // 如果没有视频，显示提示
             if (names == null || names.isEmpty()) {
                 // 移除旧的中心组件或提示
                 try {
                     if (centerScrollPane != null && centerScrollPane.getParent() != null) remove(centerScrollPane);
                     // 也移除可能存在的占位标签
                     for (Component c : getComponents()) {
                         if (c instanceof JLabel && ((JLabel)c).getText().startsWith("未在")) remove(c);
                     }
                 } catch (Throwable ignored) {}
                 add(new JLabel("未在 videos 目录或 resources/video 中找到视频文件"), BorderLayout.CENTER);
                 revalidate();
                 repaint();
                 return;
             }
             rebuildGallery(names);
         } catch (Throwable t) {
             System.err.println("VideoGalleryPanel: refreshGallery error: " + t);
         }
     }

     // 刷新/构建中心画廊区域，复用现有 makeCard() 等方法
     private void rebuildGallery(java.util.List<String> names) {
         // 移除旧的中心组件
         try {
             if (centerScrollPane != null && centerScrollPane.getParent() != null) remove(centerScrollPane);
         } catch (Throwable ignored) {}

         // 只扫描 /figures 目录下的图片（只使用 course1..course4）用于卡片缩略图
         bundledImageResources.clear();
         bundledImageResources.putAll(listBundledImagePaths());

         // 使用 GridBagLayout，每行最多 5 个卡片，且不拉伸卡片（卡片按首选大小显示）
         JPanel grid = new JPanel(new GridBagLayout());
         grid.setBackground(new Color(0xF7F7F7));
         GridBagConstraints gbc = new GridBagConstraints();
         gbc.anchor = GridBagConstraints.WEST;
         gbc.fill = GridBagConstraints.NONE;
         gbc.insets = new Insets(0, 0, 16, 16);
         int cols = 5;
         for (int i = 0; i < names.size(); i++) {
             String name = names.get(i);
             JPanel card = makeCard(name, i);
             gbc.gridx = i % cols;
             gbc.gridy = i / cols;
             gbc.weightx = 0;
             gbc.gridwidth = 1;
             grid.add(card, gbc);

             // 在每行末尾或最后一个元素时，加入一个透明填充器占据剩余水平空间，避免卡片被拉伸并把它们靠左
             boolean endOfRow = (i % cols == cols - 1);
             boolean lastElement = (i == names.size() - 1);
             if (endOfRow || lastElement) {
                 JPanel filler = new JPanel();
                 filler.setOpaque(false);
                 GridBagConstraints fgbc = new GridBagConstraints();
                 fgbc.gridx = cols; // 放在第 cols 列，作为剩余空间
                 fgbc.gridy = i / cols;
                 fgbc.weightx = 1.0;
                 fgbc.fill = GridBagConstraints.HORIZONTAL;
                 fgbc.gridwidth = GridBagConstraints.REMAINDER;
                 fgbc.insets = new Insets(0, 0, 16, 0);
                 grid.add(filler, fgbc);
             }
         }

         // 把 grid 放到一个 wrapper 的 NORTH，这样当总宽度小于视口时不会垂直居中，而是从顶部开始排列
         JPanel wrapper = new JPanel(new BorderLayout());
         wrapper.setBackground(new Color(0xF7F7F7));
         wrapper.add(grid, BorderLayout.NORTH);

         centerScrollPane = new JScrollPane(wrapper);
         centerScrollPane.setBorder(BorderFactory.createEmptyBorder(8,8,8,8));
         centerScrollPane.getViewport().setBackground(new Color(0xF7F7F7));
         add(centerScrollPane, BorderLayout.CENTER);
         revalidate();
         repaint();
     }

     // 优先从外部目录读取（项目根/videos, 用户目录/videos, client/src/.../resources/video），否则回退到 classpath 列表
     private java.util.List<String> listAvailableVideoNames() {
         java.util.List<String> names = new java.util.ArrayList<>();
         try {
             String userDir = System.getProperty("user.dir");
             String userHome = System.getProperty("user.home");
             // 配置优先：如果配置提供了 videos.path，则优先使用；其次使用硬编码的仓库根路径中的 videos；再向上查找祖先 videos；最后使用常见候选目录
             String cfg = this.configuredVideosPath;
             String hardcodedVideos = HARDCODED_PROJECT_ROOT + File.separator + "videos";
             Path ancestorVideos = cfg == null ? findAncestorContainingDirectory(userDir, "videos") : null;
             String ancestorVideosPath = ancestorVideos == null ? null : ancestorVideos.toString();
             String[] candidates = new String[]{
                     cfg,
                     hardcodedVideos,
                     ancestorVideosPath,
                     userDir + File.separator + "videos",
                     userHome + File.separator + "videos",
                     // 常见的开发输出路径：Gradle 的 resources/main/video
                     userDir + File.separator + "client" + File.separator + "build" + File.separator + "resources" + File.separator + "main" + File.separator + "video",
                     // Gradle classes 输出（某些运行配置会把资源拷贝到 classes 目录）
                     userDir + File.separator + "client" + File.separator + "build" + File.separator + "classes" + File.separator + "java" + File.separator + "main" + File.separator + "video",
                     // 项目源资源目录（开发时直接放在这里）
                     userDir + File.separator + "client" + File.separator + "src" + File.separator + "main" + File.separator + "resources" + File.separator + "video"
             };

             // 收集所有候选目录中的文件（按候选顺序），但不要在第一个非空目录时就返回，
             // 这样可以合并 client/src/... 与 build 输出目录下的文件，避免被空的用户 videos 覆盖导致看不到开发资源。
             for (String p : candidates) {
                 if (p == null || p.isEmpty()) continue;
                 try {
                     File folder = new File(p);
                     if (folder.exists() && folder.isDirectory()) {
                         File[] files = folder.listFiles();
                         int count = 0;
                         if (files != null) {
                             for (File f : files) {
                                 if (!f.isFile()) continue;
                                 // 仅包含常见视频文件扩展，忽略系统文件（如 desktop.ini）和图片等
                                 String fn = f.getName().toLowerCase();
                                 if (fn.endsWith(".mp4") || fn.endsWith(".mkv") || fn.endsWith(".avi") || fn.endsWith(".mov") || fn.endsWith(".webm") || fn.endsWith(".flv") || fn.endsWith(".wmv")) {
                                     String entry = "file:" + f.getAbsolutePath();
                                     if (!names.contains(entry)) {
                                         names.add(entry);
                                         count++;
                                     }
                                 }
                             }
                         }
                         if (count > 0) {
                             System.err.println("VideoGalleryPanel: scanned external dir: " + p + ", videoCount=" + count);
                         } else {
                             System.err.println("VideoGalleryPanel: scanned external dir: " + p + ", videoCount=0");
                         }
                     }
                 } catch (Throwable ignored) {}
             }

             // 即使找到了外部文件，也将打包的内置资源追加到列表末尾（避免内置资源被完全隐藏）
             java.util.List<String> bundled = new java.util.ArrayList<>();
             for (String bn : listBundledVideoNames()) {
                 String lower = bn.toLowerCase();
                 if (lower.endsWith(".mp4") || lower.endsWith(".mkv") || lower.endsWith(".avi") || lower.endsWith(".mov") || lower.endsWith(".webm") || lower.endsWith(".flv") || lower.endsWith(".wmv")) {
                     String entry = "/video/" + bn;
                     if (!bundled.contains(entry)) bundled.add(entry);
                 }
             }
             // 如果没有外部文件，则返回仅内置资源；否则合并外部在前、内置在后
             if (names.isEmpty()) {
                 System.err.println("VideoGalleryPanel: no external videos, using bundled resources, total=" + bundled.size());
                 return bundled;
             } else {
                 // 合并，避免重复
                 for (String b : bundled) if (!names.contains(b)) names.add(b);
                 System.err.println("VideoGalleryPanel: using external videos + bundled fallback, totalEntries=" + names.size() + ", bundledFallback=" + bundled.size());
                 return names;
             }
         } catch (Throwable ignored) {}
         return names;
     }

      // 调整 makeCard 以支持传入的 name 可能是 file:... 或 /video/...
      private JPanel makeCard(String name, int index) {
          JPanel p = new JPanel(new BorderLayout());
          p.setBackground(Color.WHITE);
         // 卡片横向减小10%（原320 -> 288），高度保持256
         Dimension cardSize = new Dimension(288, 256);
         p.setPreferredSize(cardSize);
         p.setMinimumSize(cardSize);
         p.setMaximumSize(cardSize);

         // 显示无后缀的视频名（设为 final，便于在 lambda/匿名类中安全捕获）
         final String displayName;
         final String resourcePathForPlayer;
         if (name != null && name.startsWith("file:")) {
             String fp = name.substring("file:".length());
             File f = new File(fp);
             String fn = f.getName();
             int dot = fn.lastIndexOf('.');
             if (dot > 0) displayName = fn.substring(0, dot);
             else displayName = fn;
             resourcePathForPlayer = name; // 保留 file: 前缀
         } else if (name != null && name.startsWith("/video/")) {
             String fn = name.substring(name.lastIndexOf('/') + 1);
             int dot = fn.lastIndexOf('.');
             if (dot > 0) displayName = fn.substring(0, dot);
             else displayName = fn;
             resourcePathForPlayer = name;
         } else {
             int dot = name.lastIndexOf('.');
             if (dot > 0) displayName = name.substring(0, dot);
             else displayName = name;
             resourcePathForPlayer = "/video/" + name;
         }
         // 图片在上方，名称在下方；图片宽度相应缩小
         int imgW = cardSize.width - 24;
         int imgH = cardSize.height - 64;
         JLabel imgLabel = new JLabel();
         imgLabel.setHorizontalAlignment(SwingConstants.CENTER);
         imgLabel.setVerticalAlignment(SwingConstants.CENTER);
         imgLabel.setPreferredSize(new Dimension(imgW, imgH));

         // 废弃在图片下方重复显示课程名（名称已叠加在图片上）
         // 保留卡片提示以便仍能看到完整名称
         p.setToolTipText(displayName);

         // 若存在 Figures 图片（course1-4），根据卡片序号确定映射（course1->第1个卡片）
         if (!bundledImageResources.isEmpty()) {
             try {
                 // 采用确定映射：index 0 -> course1, index 1 -> course2, ... 超出则循环
                 String key = "course" + ((index % 4) + 1);
                 String imgPath = bundledImageResources.get(key);
                 // 若未找到对应命名的图片，回退为随机选择
                 if (imgPath == null) {
                     java.util.List<String> vals = new java.util.ArrayList<>(bundledImageResources.values());
                     if (!vals.isEmpty()) {
                         int rnd = java.util.concurrent.ThreadLocalRandom.current().nextInt(vals.size());
                         imgPath = vals.get(rnd);
                     }
                 }
                 // 生成填充型圆角图像并设置到 imgLabel（cover 模式，尽量填满边框）
                 int imgArc = 10;
                 int imgThickness = 1; // 边框厚度
                 ImageIcon filled = null;
                 if (imgPath != null) {
                     // padding=0 让图片尽量填满边框区域
                     filled = createRoundedFilledImageIcon(imgPath, imgW, imgH, imgArc, 0, displayName);
                 }
                 if (filled != null) {
                     // 在实际图片上叠加课程名（白色）
                     imgLabel.setIcon(filled);
                 } else {
                     // 回退占位图（占位图也会在底部显示课程名）
                     imgLabel.setIcon(createPlaceholderIcon(displayName, imgW, imgH));
                 }
             } catch (Throwable ignored) {}
         } else {
             imgLabel.setIcon(createPlaceholderIcon(displayName, imgW, imgH));
         }

         // 美化：给图片加白底与细边框（圆角）
         imgLabel.setOpaque(true);
         imgLabel.setBackground(Color.WHITE);
         // 使用圆角边框并尽量贴合图片（小内边距）
         int imgArc = 10;
         int imgThickness = 1;
         imgLabel.setBorder(new RoundedBorder(new Color(0xDDDDDD), imgThickness, imgArc));

         // 整个卡片可点击：点击在当前界面嵌入播放器（避免弹窗）
         Runnable openAction = () -> {
             try {
                 final String resPath = resourcePathForPlayer;
                 final String title = displayName;
                 // 在 EDT 中执行嵌入播放器动作；resPath 和 title 为 final，满足 lambda 捕获要求
                 SwingUtilities.invokeLater(() -> showPlayer(resPath, title));
             } catch (Throwable ex) {
                 JOptionPane.showMessageDialog(this, "无法打开视频: " + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
             }
         };

         p.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
         // 默认与悬停边框（圆角），内边距较小以便边框靠近图片
         int cardArc = 12;
         javax.swing.border.Border defaultBorder = BorderFactory.createCompoundBorder(new RoundedBorder(new Color(0xEEEEEE), 1, cardArc), BorderFactory.createEmptyBorder(4,4,4,4));
         javax.swing.border.Border hoverBorder = BorderFactory.createCompoundBorder(new RoundedBorder(new Color(0xCCCCCC), 2, cardArc), BorderFactory.createEmptyBorder(4,4,4,4));
         p.setBorder(defaultBorder);
         p.addMouseListener(new java.awt.event.MouseAdapter() {
             @Override public void mouseClicked(java.awt.event.MouseEvent e) { openAction.run(); }
             @Override public void mouseEntered(java.awt.event.MouseEvent e) { p.setBorder(hoverBorder); p.setBackground(new Color(0xFFFFFF)); }
             @Override public void mouseExited(java.awt.event.MouseEvent e) { p.setBorder(defaultBorder); p.setBackground(Color.WHITE); }
         });

         // 纵向布局：仅显示图片（课程名已叠加在图片上）
         JPanel center = new JPanel(new BorderLayout());
         center.setOpaque(false);
         center.add(imgLabel, BorderLayout.CENTER);
         p.add(center, BorderLayout.CENTER);
         return p;
     }

     // 在面板中央替换为播放器并开始播放内置资源
     private void showPlayer(String resourcePath, String title) {
         try {
             // 如果已有播放器，先清理
             hidePlayer();

             currentPlayerPanel = new VideoPlayerPanel();
             currentPlayerContainer = new JPanel(new BorderLayout());

             // 不显示顶部标题与返回按钮，直接加入播放器面板
             currentPlayerContainer.add(currentPlayerPanel, BorderLayout.CENTER);

             // 键盘绑定：ESC 关闭播放器，SPACE 切换播放/暂停
             currentPlayerContainer.setFocusable(true);
             InputMap im = currentPlayerContainer.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
             ActionMap am = currentPlayerContainer.getActionMap();
             im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "closePlayer");
             am.put("closePlayer", new AbstractAction() { public void actionPerformed(ActionEvent e) { hidePlayer(); } });
             im.put(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0), "togglePlay");
             am.put("togglePlay", new AbstractAction() { public void actionPerformed(ActionEvent e) { try { if (currentPlayerPanel != null) currentPlayerPanel.togglePlayPause(); } catch (Throwable ignored) {} } });
             // 尝试获取焦点以便接收键盘事件
             SwingUtilities.invokeLater(() -> currentPlayerContainer.requestFocusInWindow());

              // 替换中心组件
              remove(centerScrollPane);
              add(currentPlayerContainer, BorderLayout.CENTER);
              revalidate();
              repaint();

             // 延迟播放以保证 UI 已嵌入
             SwingUtilities.invokeLater(() -> {
                 try {
                     if (resourcePath != null && resourcePath.startsWith("file:")) {
                         String fp = resourcePath.substring("file:".length());
                         File f = new File(fp);
                         if (f.exists() && f.isFile()) {
                             currentPlayerPanel.playFile(f);
                         } else {
                             JOptionPane.showMessageDialog(this, "外部视频文件不存在: " + fp, "错误", JOptionPane.ERROR_MESSAGE);
                         }
                     } else {
                         currentPlayerPanel.playBundledResource(resourcePath);
                     }
                 } catch (Throwable ignored) {}
             });
           } catch (Throwable t) {
               JOptionPane.showMessageDialog(this, "无法在内嵌播放器中打开视频: " + t.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
           }
       }

     // 关闭并移除内嵌播放器，恢复画廊视图
     private void hidePlayer() {
         try {
             if (currentPlayerPanel != null) {
                 try { currentPlayerPanel.dispose(); } catch (Throwable ignored) {}
             }
             if (currentPlayerContainer != null) {
                 remove(currentPlayerContainer);
                 currentPlayerContainer = null;
             }
             // 如果滚动面板尚未被添加，确保恢复它
             if (centerScrollPane.getParent() == null) add(centerScrollPane, BorderLayout.CENTER);
             currentPlayerPanel = null;
             revalidate();
             repaint();
         } catch (Throwable ignored) {}
     }

     // 创建有父窗口的模态对话框，若找不到父窗口则返回无父对话框
     private JDialog createOwnerDialog(String title) {
         Window win = SwingUtilities.getWindowAncestor(this);
         if (win instanceof Frame) {
             return new JDialog((Frame) win, title, true);
         } else if (win instanceof Dialog) {
             return new JDialog((Dialog) win, title, true);
         } else {
             return new JDialog((Frame) null, title, true);
         }
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

             Enumeration<URL> resources = getClass().getClassLoader().getResources("video");
             while (resources.hasMoreElements()) {
                 URL url = resources.nextElement();
                 if (url.getProtocol().equals("file")) {
                     File folder = new File(url.toURI());
                     File[] files = folder.listFiles();
                     if (files != null) for (File f : files) if (f.isFile()) names.add(f.getName());
                 }
             }
         } catch (Exception ignored) {}
         return names;
     }

     // 只扫描 /figures/ 目录，并以固定 key (course1..course4) 映射到资源路径
     private java.util.Map<String, String> listBundledImagePaths() {
         java.util.Map<String, String> imgs = new java.util.HashMap<>();
         try {
             // 支持 /figures/ 和 /figure/ 两个目录（部分开发者可能使用单数名称）
             String[] candidates = new String[]{"figures", "figure"};
             for (String dirName : candidates) {
                 URL figDir = getClass().getResource("/" + dirName + "/");
                 if (figDir == null) continue;
                 String protocol = figDir.getProtocol();
                 if ("file".equalsIgnoreCase(protocol)) {
                     File folder = new File(figDir.toURI());
                     File[] files = folder.listFiles();
                     if (files != null) for (File f : files) if (f.isFile()) {
                         String fname = f.getName().toLowerCase();
                         // 去除扩展名并归一化（移除非字母数字），这样 course-1/course_1/course1 都能匹配为 course1
                         String base = fname;
                         int dot = base.lastIndexOf('.');
                         if (dot > 0) base = base.substring(0, dot);
                         String normalized = base.replaceAll("[^a-z0-9]", "");

                         for (int i = 1; i <= 4; i++) {
                             String key = "course" + i;
                             if (normalized.equals(key) || normalized.startsWith(key)) {
                                 imgs.put(key, "/" + dirName + "/" + f.getName());
                                 break;
                             }
                         }
                     }
                 } else if ("jar".equalsIgnoreCase(protocol)) {
                     String path = figDir.getPath();
                     int bang = path.indexOf("!");
                     String jarPath = path.substring(path.indexOf(":") + 1, bang);
                     jarPath = URLDecoder.decode(jarPath, "UTF-8");
                     try (JarFile jar = new JarFile(jarPath)) {
                         java.util.Enumeration<java.util.jar.JarEntry> entries = jar.entries();
                         while (entries.hasMoreElements()) {
                             java.util.jar.JarEntry entry = entries.nextElement();
                             String name = entry.getName();
                             if (name.startsWith(dirName + "/") && !name.endsWith("/")) {
                                 // 取文件名部分并归一化
                                 String fn = name.substring(name.lastIndexOf('/') + 1).toLowerCase();
                                 String base = fn;
                                 int dot = base.lastIndexOf('.');
                                 if (dot > 0) base = base.substring(0, dot);
                                 String normalized = base.replaceAll("[^a-z0-9]", "");

                                 for (int i = 1; i <= 4; i++) {
                                     String key = "course" + i;
                                     if (normalized.equals(key) || normalized.startsWith(key)) {
                                         imgs.put(key, "/" + name);
                                         break;
                                     }
                                 }
                             }
                         }
                     }
                 }
             }

             // 如果扫描结果不完整，主动尝试按常见路径和后缀查找 course1..course4，同时尝试带连字符和下划线的变体
             String[] pathsToTry = new String[]{"/figures/", "/figure/", "/images/", "/"};
             String[] exts = new String[]{".png", ".jpg", ".jpeg", ".gif"};
             for (int i = 1; i <= 4; i++) {
                 String key = "course" + i;
                 if (imgs.containsKey(key)) continue;
                 boolean found = false;
                 String[] variants = new String[]{key, "course-" + i, "course_" + i};
                 for (String base : pathsToTry) {
                     for (String var : variants) {
                         for (String ext : exts) {
                             String candidate = base + var + ext;
                             URL r = getClass().getResource(candidate);
                             if (r != null) {
                                 imgs.put(key, candidate);
                                 found = true;
                                 break;
                             }
                             // 本地开发路径回退
                             try {
                                 String userDir = System.getProperty("user.dir");
                                 String candidateFs = userDir + File.separator + "client" + File.separator + "src" + File.separator + "main" + File.separator + "resources" + candidate.replaceFirst("^/", "");
                                 File f = new File(candidateFs);
                                 if (f.exists() && f.isFile()) {
                                     imgs.put(key, candidate);
                                     found = true;
                                     break;
                                 }
                             } catch (Throwable ignored) {}
                         }
                         if (found) break;
                     }
                     if (found) break;
                 }
             }

             // 最终仍未找到的 key，生成内存占位图并保存为 special key 路径 (null 表示使用占位)
             for (int i = 1; i <= 4; i++) {
                 String key = "course" + i;
                 if (!imgs.containsKey(key)) {
                     System.err.println("VideoGalleryPanel: image not found for " + key + "; will use generated placeholder.");
                     imgs.put(key, null);
                 }
             }

             if (imgs.values().stream().allMatch(v -> v == null)) System.err.println("VideoGalleryPanel: no figures images available; placeholders will be used.");
         } catch (Exception e) {
             System.err.println("VideoGalleryPanel: error scanning figures: " + e.getMessage());
         }
         return imgs;
     }

     // 从资源加载图片并绘制为圆角、cover（裁剪填充）形式，padding 为边框厚度，确保图片尽量填满边框
     private ImageIcon createRoundedFilledImageIcon(String resourcePath, int width, int height, int arc, int padding, String overlayText) {
         try {
             java.net.URL res = null;
             try { res = getClass().getResource(resourcePath); } catch (Throwable ignored) {}
             if (res == null) {
                 try { res = getClass().getClassLoader().getResource(resourcePath.replaceFirst("^/", "")); } catch (Throwable ignored) {}
             }
             java.awt.image.BufferedImage srcImg = null;
             if (res != null) {
                 try { srcImg = javax.imageio.ImageIO.read(res); } catch (Throwable ignored) {}
             }
             if (srcImg == null) {
                 // 回退到文件系统
                 try {
                     String userDir = System.getProperty("user.dir");
                     String candidate = userDir + File.separator + "client" + File.separator + "src" + File.separator + "main" + File.separator + "resources" + resourcePath.replaceFirst("^/", "");
                     File f = new File(candidate);
                     if (f.exists() && f.isFile()) {
                         srcImg = javax.imageio.ImageIO.read(f);
                     }
                 } catch (Throwable ignored) {}
             }
             if (srcImg == null) return null;

             int rw = width;
             int rh = height;
             java.awt.image.BufferedImage out = new java.awt.image.BufferedImage(rw, rh, java.awt.image.BufferedImage.TYPE_INT_ARGB);
             Graphics2D g = out.createGraphics();
             try {
                 g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                 g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);

                 // 绘制圆角背景为透明
                 g.setComposite(AlphaComposite.Src);
                 g.setColor(new Color(0,0,0,0));
                 g.fillRect(0,0,rw,rh);

                 // 计算目标区域（内部区域，减去 padding）
                 int tx = padding;
                 int ty = padding;
                 int tw = Math.max(1, rw - 2*padding);
                 int th = Math.max(1, rh - 2*padding);

                 // cover 模式：按较大缩放比例填满区域，中心裁剪
                 int sw = srcImg.getWidth();
                 int sh = srcImg.getHeight();
                 double scale = Math.max((double)tw / sw, (double)th / sh);
                 int dw = (int)Math.round(sw * scale);
                 int dh = (int)Math.round(sh * scale);
                 int dx = tx + (tw - dw) / 2;
                 int dy = ty + (th - dh) / 2;

                 // 圆角裁剪
                 java.awt.geom.RoundRectangle2D.Double clip = new java.awt.geom.RoundRectangle2D.Double(0, 0, rw, rh, arc, arc);
                 g.setClip(clip);

                 // 绘制放大图像
                 g.drawImage(srcImg, dx, dy, dw, dh, null);

                 // 在图片底部绘制半透明渐变遮罩并显示白色课程名（如果提供）
                 if (overlayText != null && !overlayText.trim().isEmpty()) {
                     try {
                         int overlayH = Math.max(28, rh / 6);
                         int oy = rh - overlayH;
                         // 从透明到半透明黑色的渐变
                         GradientPaint gp = new GradientPaint(0, oy, new Color(0,0,0,0), 0, rh, new Color(0,0,0,180));
                         g.setComposite(AlphaComposite.SrcOver);
                         g.setPaint(gp);
                         g.fillRect(0, oy, rw, overlayH);

                         // 绘制文本（先绘制暗色偏移作为描边/阴影，再绘制白色文本）
                         Font font = new Font("微软雅黑", Font.BOLD, Math.max(12, overlayH * 2 / 3));
                         g.setFont(font);
                         g.setColor(new Color(0,0,0,120));
                         FontMetrics fm = g.getFontMetrics();
                         int twText = fm.stringWidth(overlayText);
                         int txText = Math.max(8, (rw - twText) / 2);
                         // 计算基线：垂直居中于 overlay 区域
                         int baseline = oy + (overlayH + fm.getAscent() - fm.getDescent()) / 2;
                         g.drawString(overlayText, txText + 1, baseline + 1);
                         g.setColor(Color.WHITE);
                         g.drawString(overlayText, txText, baseline);
                     } catch (Throwable ignored) {}
                 }
              } finally {
                  g.dispose();
              }
              return new ImageIcon(out);
          } catch (Throwable t) {
              System.err.println("VideoGalleryPanel: createRoundedFilledImageIcon error for " + resourcePath + ", ex=" + t);
              return null;
          }
      }

      // 生成简单的占位图，确保卡片始终显示图片
      private ImageIcon createPlaceholderIcon(String text, int width, int height) {
          try {
             java.awt.image.BufferedImage img = new java.awt.image.BufferedImage(width, height, java.awt.image.BufferedImage.TYPE_INT_RGB);
             Graphics2D g = img.createGraphics();
             try {
                 g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                 // 背景
                 g.setColor(new Color(0xDDDDDD));
                 g.fillRect(0, 0, width, height);

                 // 底部渐变遮罩
                 int overlayH = Math.max(28, height / 6);
                 int oy = height - overlayH;
                 GradientPaint gp = new GradientPaint(0, oy, new Color(0,0,0,0), 0, height, new Color(0,0,0,180));
                 g.setPaint(gp);
                 g.fillRect(0, oy, width, overlayH);

                 // 课程名：白色，带轻微阴影
                 Font font = new Font("微软雅黑", Font.BOLD, Math.max(12, overlayH * 2 / 3));
                 g.setFont(font);
                 FontMetrics fm = g.getFontMetrics();
                 String display = text == null ? "" : text;
                 int tw = fm.stringWidth(display);
                 int tx = Math.max(8, (width - tw) / 2);
                 int baseline = oy + (overlayH + fm.getAscent() - fm.getDescent()) / 2;
                 g.setColor(new Color(0,0,0,120));
                 g.drawString(display, tx + 1, baseline + 1);
                 g.setColor(Color.WHITE);
                 g.drawString(display, tx, baseline);
             } finally {
                 g.dispose();
             }
             return new ImageIcon(img);
          } catch (Throwable t) {
              return null;
          }
      }

     // 自定义圆角边框，绘制圆角矩形边框并提供内边距信息
     private static class RoundedBorder extends javax.swing.border.AbstractBorder {
         private final Color color;
         private final int thickness;
         private final int arc;

         RoundedBorder(Color color, int thickness, int arc) {
             this.color = color;
             this.thickness = Math.max(1, thickness);
             this.arc = Math.max(0, arc);
         }

         @Override
         public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
             Graphics2D g2 = (Graphics2D) g.create();
             try {
                 g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                 g2.setColor(color);
                 g2.setStroke(new BasicStroke(thickness));
                 int half = thickness / 2;
                 g2.drawRoundRect(x + half, y + half, width - thickness, height - thickness, arc, arc);
             } finally {
                 g2.dispose();
             }
         }

         @Override
         public Insets getBorderInsets(Component c) {
             return new Insets(thickness, thickness, thickness, thickness);
         }

         @Override
         public Insets getBorderInsets(Component c, Insets insets) {
             insets.left = insets.top = insets.right = insets.bottom = thickness;
             return insets;
         }
     }

    // 向上查找从 startPath 开始的祖先目录中是否包含名为 childName 的子目录，返回该子目录的 Path
    private Path findAncestorContainingDirectory(String startPath, String childName) {
        try {
            Path p = Paths.get(startPath).toAbsolutePath();
            for (Path cur = p; cur != null; cur = cur.getParent()) {
                Path candidate = cur.resolve(childName);
                if (Files.exists(candidate) && Files.isDirectory(candidate)) return candidate;
            }
        } catch (Throwable ignored) {}
        return null;
    }

    // 从配置文件加载 videos.path，顺序尝试：classpath config/client.properties -> workingDir/config/client.properties -> HARDCODED_PROJECT_ROOT/config/client.properties
    private String loadConfiguredVideosPath() {
        String[] tryPaths = new String[] {
                "config/client.properties",
                System.getProperty("user.dir") + File.separator + "config" + File.separator + "client.properties",
                HARDCODED_PROJECT_ROOT + File.separator + "config" + File.separator + "client.properties"
        };
        java.util.Properties props = new java.util.Properties();
        for (String p : tryPaths) {
            try {
                // 首先尝试 classpath
                java.io.InputStream is = getClass().getClassLoader().getResourceAsStream(p);
                if (is == null) {
                    java.io.File f = new java.io.File(p);
                    if (f.exists() && f.isFile()) is = new java.io.FileInputStream(f);
                }
                if (is != null) {
                    try (java.io.InputStream in = is) {
                        props.load(in);
                        String v = props.getProperty("videos.path");
                        if (v != null && !v.trim().isEmpty()) {
                            System.err.println("VideoGalleryPanel: loaded videos.path from config: " + v);
                            return v.trim();
                        }
                    }
                }
            } catch (Throwable ignored) {}
        }
        return null;
    }
 }
