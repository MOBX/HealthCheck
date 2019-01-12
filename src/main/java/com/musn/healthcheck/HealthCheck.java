/*
 * Copyright 2015-2020 msun.com All right reserved.
 */
package com.musn.healthcheck;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

import com.lamfire.json.JSON;
import com.lamfire.utils.DateFormatUtils;

/**
 * Java应用程序健康检查工具(嵌入式,对非web程序很适合,轻量级仅依赖JDK)
 * 
 * @author luckscript Sep 1, 2017 6:14:52 PM
 */
public class HealthCheck {

    public static void main(String[] args) throws Exception {
        final int port = 8080;

        Thread thread = new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    health(port);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }, "HealthCheckThread");
        thread.start();
        Thread.currentThread().join();
    }

    @SuppressWarnings("resource")
    private static void health(int port) throws IOException {
        ServerSocket serverSocket = new ServerSocket(port);
        System.err.println("Server on port : " + port);

        while (true) {
            Socket clientSocket = serverSocket.accept();

            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));

            String s;
            while ((s = in.readLine()) != null) {
                System.out.println(s);
                if (s.isEmpty()) break;
            }

            JSON json = current();
            out.write("HTTP/1.0 200 OK\r\n");
            out.write("Date: Fri, 31 Dec 2017 23:59:59 GMT\r\n");
            out.write("Server: HealthCheck/0.0.1\r\n");
            out.write("Content-Type: application/json;charset=utf-8\r\n");
            out.write("Content-Length: " + json.toJSONString().getBytes().length + "\r\n");
            out.write("Expires: Sat, 01 Jan 2017 00:59:59 GMT\r\n");
            out.write("Last-modified: Fri, 09 Aug 2017 14:21:40 GMT\r\n");
            out.write("\r\n");
            out.write(json.toJSONString());

            out.close();
            in.close();
            clientSocket.close();
        }
    }

    public static JSON current() {
        JSON json = new JSON();
        json.put("status", 200);
        json.put("time", DateFormatUtils.format(System.currentTimeMillis(), "yyyy-MM-dd HH:mm:ss:SSS"));
        return json;
    }
}

