package com.chen.nio;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.net.InetSocketAddress;
import java.io.OutputStream;


public class NioClient {
    public static void main(String[] args) throws IOException {
        Socket socket = new Socket();
        socket.connect(new InetSocketAddress("localhost", 8080));

        OutputStream outputStream = socket.getOutputStream();
        InputStream inputStream = socket.getInputStream();

        byte[] readBuffer = new byte[1024];

        for (int i = 0; i < 10; i++) {
            String msg = Thread.currentThread().getName() + i;
            outputStream.write(msg.getBytes());
            outputStream.flush();

            // 👇 必须读取服务端回显数据！
            int len = inputStream.read(readBuffer);
            if (len != -1) {
                System.out.println("客户端[" + Thread.currentThread().getName() + "] 收到回显: " +
                        new String(readBuffer, 0, len));
            }
        }

        socket.close(); // 读取完之后再关闭
    }
}
