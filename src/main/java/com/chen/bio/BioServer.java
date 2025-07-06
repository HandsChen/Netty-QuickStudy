package com.chen.bio;

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class BioServer {
    //BIO的本质就是只要inputStream.read(buffer))不返回-1，我就认为你这个连接还存在，其他线程就会继续阻塞在这里，传统的BIO是面向流的
    public static void main(String[] args)  {
        try {
            ServerSocket socket = new ServerSocket(8080); //服务端的socket
            while (true){
                Socket accept = socket.accept(); //客户端的socket，接收一个客户端的连接，如果一直没有连接，那么就会一直阻塞
                InputStream inputStream = accept.getInputStream();
                byte[] buffer = new byte[1024];
                int length;
                while ((length = inputStream.read(buffer)) != -1) { //如果可以读到东西
                    String message = new String(buffer, 0, length); //那么就将读到的东西转成字符串打印出来
                    System.out.println(message);
                }
                System.out.println("客户端断开连接");
            }
        }catch (IOException e){
            e.printStackTrace();
        }
    }
}
