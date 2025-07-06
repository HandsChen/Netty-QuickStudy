package com.chen.bio;
import java.io.IOException;
import java.net.Socket;
import java.net.InetSocketAddress;
import java.io.OutputStream;
public class BioClient {
    /**
     * 两个线程一个建立连接后，另一个是无法再向服务端发送内容的
     */
    public static void main(String[] args) throws Exception {
        Thread t1 = new Thread(() -> {
            try {
                sendHello();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }, "jerry");
        Thread t2 = new Thread(() -> {
            try {
                sendHello();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }, "tom");
        t1.start();
        t2.start();
        t1.join();
        t2.join();

    }
    private static void sendHello() throws Exception{
        Socket socket = new Socket();
        socket.connect(new InetSocketAddress("localhost", 8080));
        OutputStream outputStream = socket.getOutputStream();
        for (int i = 0; i < 10; i++) {
            outputStream.write((Thread.currentThread().getName() + i).getBytes());
            outputStream.flush(); //写完之后要刷一下马桶
        }
        Thread.sleep(1000); // 给服务端时间完成写回
        socket.close();
    }
}
