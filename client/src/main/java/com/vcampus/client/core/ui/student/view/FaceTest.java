package com.vcampus.client.core.ui.student.view;

import java.io.File;

public class FaceTest {
    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("请指定图片路径作为参数");
            return;
        }
        // 自动查找 DLL 路径
        String[] dllDirs = {"client/lib", "lib", "../lib", "./lib"};
        String dllName = "opencv_java4110.dll";
        String foundDllPath = null;
        System.out.println("当前工作目录: " + System.getProperty("user.dir"));
        System.out.println("尝试查找 OpenCV DLL，以下是所有尝试路径:");
        for (String dir : dllDirs) {
            String candidate = new File(dir, dllName).getAbsolutePath();
            System.out.println("  " + candidate);
            if (new File(candidate).exists()) {
                foundDllPath = candidate;
                System.out.println("找到 DLL: " + foundDllPath);
                break;
            }
        }
        if (foundDllPath == null) {
            System.out.println("OpenCV DLL未找到，请确认 DLL 是否存在于上述路径之一。");
            return;
        }
        System.load(foundDllPath);
            String cascadePath = new File("client/resources/haarcascade_frontalface_default.xml").getAbsolutePath();
            System.out.println("检测模型路径: " + cascadePath);
            if (!new File(cascadePath).exists()) {
                System.out.println("人脸检测模型未找到: " + cascadePath);
                return;
            } else {
                System.out.println("人脸检测模型已找到，大小: " + new File(cascadePath).length() + " 字节");
            }
            String imgPath = args[0];
            System.out.println("待检测图片路径: " + imgPath);
            if (!new File(imgPath).exists()) {
                System.out.println("图片文件未找到: " + imgPath);
                return;
            } else {
                System.out.println("图片已找到，大小: " + new File(imgPath).length() + " 字节");
            }
            FaceDetector detector = new FaceDetector(cascadePath);
            boolean hasFace = detector.hasFace(imgPath);
            System.out.println("检测结果: " + (hasFace ? "检测到人脸！" : "未检测到人脸。"));
        }
    }

