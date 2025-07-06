package com.chen.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

public class NioServer {
    public static void main(String[] args) throws IOException {
        // 1. 创建ServerSocketChannel并绑定端口
        ServerSocketChannel serverChannel = ServerSocketChannel.open();
        serverChannel.bind(new InetSocketAddress(8080));
        serverChannel.configureBlocking(false); // 设置非阻塞模式

        // 2. 创建Selector并注册ServerChannel
        Selector selector = Selector.open();
        serverChannel.register(selector, SelectionKey.OP_ACCEPT);

        System.out.println("NIO Echo Server 启动，监听端口 8080...");

        while (true) {
            selector.select(); // 阻塞，直到至少有一个事件
            Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
            while (iterator.hasNext()) {
                SelectionKey key = iterator.next();
                iterator.remove(); //这里每次拿到SelectionKey之后要将其从迭代器iterator中移除掉，因为你selector只负责通知，它不知道你是不是已经对当前的SelectionKey是否处理完

                try {
                    if (key.isAcceptable()) {
                        // 接收连接
                        SocketChannel clientChannel = serverChannel.accept(); //获取客户端channel，接受新的客户端连接，返回serverChannel
                        clientChannel.configureBlocking(false);
                        clientChannel.register(selector, SelectionKey.OP_READ); //将监听器注册到客户端channel中，并监听读事件
                        System.out.println("客户端连接：" + clientChannel.getRemoteAddress());
                    } else if (key.isReadable()) {
                        // 读取数据
                        SocketChannel clientChannel = (SocketChannel) key.channel(); //获取之前注册到selector的客户端通道，返回SocketChannel
                        ByteBuffer buffer = ByteBuffer.allocate(1024);
                        int len = clientChannel.read(buffer);
                        if (len == -1) { //如果已经读完
                            clientChannel.close();
                        } else {
                            /* buffer.flip
                             * 设置 limit = position
                             * 设置 position = 0
                             * 准备从头读取 buffer 中刚刚写入的数据。
                             */
                            buffer.flip(); //将 ByteBuffer 从“写模式”切换到“读模式”。
                            clientChannel.write(buffer); // 回写给客户端
                            /* buffer.clear
                                设置 position = 0
                                设置 limit = capacity
                                表示可以重新写入整个 buffer。
                             */
                            buffer.clear();
                        }
                    }
                } catch (IOException e) {
                    key.channel().close();
                    key.cancel();
                    e.printStackTrace();
                    System.out.println("连接异常关闭：" + e.getMessage());
                }
            }
        }
    }
}
