package com.vcampus.demo.socket;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * 纯Java Socket通信示例（仅演示，不用于项目主流程）
 * 服务端和客户端通过InputStream/OutputStream直接传输字符串
 */
public class SimpleSocketDemo {
    // 服务端
    public static class SimpleServer {
        public static void main(String[] args) throws IOException {
            ServerSocket serverSocket = new ServerSocket(9000);
            System.out.println("服务器启动，等待连接...");
            Socket clientSocket = serverSocket.accept();
            System.out.println("客户端已连接: " + clientSocket.getInetAddress());
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
            String line;
            while ((line = in.readLine()) != null) {
                System.out.println("收到客户端: " + line);
                out.println("服务端收到: " + line);
            }
            clientSocket.close();
            serverSocket.close();
        }
    }

    // 客户端
    public static class SimpleClient {
        public static void main(String[] args) throws IOException {
            Socket socket = new Socket("localhost", 9000);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            out.println("你好，服务端！");
            String response = in.readLine();
            System.out.println("收到服务端: " + response);
            socket.close();
        }
    }
}

