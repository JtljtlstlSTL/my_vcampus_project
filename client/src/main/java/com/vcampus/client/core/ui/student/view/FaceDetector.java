package com.vcampus.client.core.ui.student.view;

// 需要: opencv-4110.jar, opencv_java4110.dll, haarcascade_frontalface_default.xml
// 用法: System.load("client/lib/opencv_java4110.dll");
//       new FaceDetector("client/resources/haarcascade_frontalface_default.xml").hasFace(imagePath);

public class FaceDetector {
    private Object faceCascade;

    public FaceDetector(String cascadePath) {
        try {
            System.out.println("[调试] 开始加载 CascadeClassifier, 路径: " + cascadePath);
            Class<?> cascadeClass = Class.forName("org.opencv.objdetect.CascadeClassifier");
            faceCascade = cascadeClass.getConstructor(String.class).newInstance(cascadePath);
            System.out.println("[调试] CascadeClassifier 加载成功: " + (faceCascade != null));
        } catch (Exception e) {
            faceCascade = null;
            System.out.println("[调试] CascadeClassifier 加载失败: " + e);
            e.printStackTrace();
        }
    }

    public boolean hasFace(String imagePath) {
        try {
            if (faceCascade == null) {
                System.out.println("[调试] faceCascade 未初始化");
                return false;
            }
            System.out.println("[调试] 开始加载图片: " + imagePath);
            Class<?> matClass = Class.forName("org.opencv.core.Mat");
            Class<?> imgcodecsClass = Class.forName("org.opencv.imgcodecs.Imgcodecs");
            Class<?> matOfRectClass = Class.forName("org.opencv.core.MatOfRect");
            Class<?> sizeClass = Class.forName("org.opencv.core.Size");

            Object image = imgcodecsClass.getMethod("imread", String.class).invoke(null, imagePath);
            boolean empty = (boolean) matClass.getMethod("empty").invoke(image);
            System.out.println("[调试] 图片加载结果: " + (empty ? "图片为空或无法读取" : "图片读取成功"));
            if (empty) return false;
            Object faces = matOfRectClass.getConstructor().newInstance();
            Object minSize = sizeClass.getConstructor(double.class, double.class).newInstance(10, 10); // 最小人脸尺寸调为10x10
            Object maxSize = sizeClass.getConstructor().newInstance(); // 不限制最大
            System.out.println("[调试] 开始人脸检测，参数: scaleFactor=1.05, minNeighbors=1, minSize=10x10");
            faceCascade.getClass().getMethod("detectMultiScale", matClass, matOfRectClass, double.class, int.class, int.class, sizeClass, sizeClass)
                .invoke(faceCascade, image, faces, 1.05, 1, 0, minSize, maxSize);
            Object[] arr = (Object[]) matOfRectClass.getMethod("toArray").invoke(faces);
            System.out.println("[调试] 检测到人脸数量: " + arr.length);
            return arr.length > 0;
        } catch (Exception e) {
            System.out.println("[调试] 检测过程异常: " + e);
            e.printStackTrace();
            return false;
        }
    }
}
