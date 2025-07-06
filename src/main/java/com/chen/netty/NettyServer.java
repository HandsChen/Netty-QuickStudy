package com.chen.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

//传统的NIO多事件无法同时进行，例如，如果你正在处理一个read事件，那么新来的ACCEPT事件你是无法处理的
//因此为乐保证我们在读✍写数据的时候，还能处理新的连接，我们就需要将一个selector变成两个selector，即将建立连接和读写事件分开了
//进一步，如果客户端很多，一个线程就无法处理这么多的读写事件了，因此，需要对读写事件进一步分离，使用多个线程而不是一个线程处理读写事件

public class NettyServer {
    public static void main(String[] args) {
        //模仿将接收到的数据存入DB
        Map<Channel, List<String>> db = new ConcurrentHashMap<>();

        ServerBootstrap serverBootstrap = new ServerBootstrap(); //组合所有组件进行通信
        serverBootstrap.group(new NioEventLoopGroup(), new NioEventLoopGroup()); //分别创建boss线程组和worker线程组
        serverBootstrap.channel(NioServerSocketChannel.class); //选择使用哪种channel,因为我们是服务器所以我们选择使用NioServerSocketChannel
        //每一个SocketChannel（即客户端）都有一个独立的pipeline
        /**
         * childHandler 干嘛用的？	给每个“客户端连接的通道（SocketChannel）”设置 pipeline 处理逻辑
         * 为啥 SocketChannel 而不是 ServerSocketChannel？	ServerSocketChannel 只是接收连接，不做读写处理；真正通信的是 SocketChannel
         * ch.pipeline() 是谁的？	是当前连接进来的 SocketChannel 的 pipeline（处理链）
         */
        serverBootstrap.childHandler(new ChannelInitializer<SocketChannel>() {//你对每一个子连接需要设计什么handler(一般我们这里写初始化handler)，因为每服务端和客户端都会维护pipeline
            @Override
            protected void initChannel(SocketChannel socketChannel) throws Exception {
                socketChannel.pipeline().addLast(new LineBasedFrameDecoder(1024)).addLast(new StringDecoder())//拿到socketChannel中的pipeLine，并在后面加上我们真正的handler
                        .addLast(new StringEncoder()) //这里出站要加在业务代码前面
                        .addLast(new SimpleChannelInboundHandler<String>() {
                            @Override
                            protected void channelRead0(ChannelHandlerContext channelHandlerContext, String s) throws Exception {
                                System.out.println(s);//打印消息，这个消息来自于前面的解码器解码
                                //1. 现在我们想将接收到的消息稍加处理后写回客户端，应该如何做？
                                String msg = s + " world\n";
                                //channelHandlerContext.writeAndFlush()和下面的有什么区别，主要区别在于channelHandlerContext.writeAndFlush是从当前Handler返回找出站处理器，
                                // 而channelHandlerContext.channel().writeAndFlush是从尾部返回找出站处理器
                                channelHandlerContext.channel().writeAndFlush(msg); //要注意加出站处理器
                                //将接收到的消息继续流转到下一个handler，这是因为如果pipeLine是双向链表，你不手动向下传，它永远接收不到
                                channelHandlerContext.fireChannelRead(s);
                            }
                        }).addLast(new SimpleChannelInboundHandler<String>() {
                            @Override
                            protected void channelRead0(ChannelHandlerContext channelHandlerContext, String s) throws Exception {
                                List<String> msgList = db.computeIfAbsent(channelHandlerContext.channel(), v -> new ArrayList<>());
                                msgList.add(s);
                            }

                            @Override
                            public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
                                System.out.println(ctx.channel() + " 注册了");
                            }

                            @Override
                            public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
                                System.out.println(ctx.channel() + " 解除注册了");
                            }

                            @Override
                            public void channelActive(ChannelHandlerContext ctx) throws Exception {
                                System.out.println(ctx.channel() + " 可以使用了");
                            }

                            @Override
                            public void channelInactive(ChannelHandlerContext ctx) throws Exception {
                                //在断开连接的时候将DB中存的数据打印出来
                                List<String> strings = db.get(ctx.channel());
                                System.out.println(strings);
                            }
                        });
            }
        });
        //设置好服务器上应该让其绑定端口进行运行
        ChannelFuture bindFuture = serverBootstrap.bind(8080);
        bindFuture.addListener(f -> {
            if (f.isSuccess()) {
                System.out.println("服务器成功监听端口: " + 8080);
            } else {
                System.out.println("服务器监听失败");
            }
        });
    }
}
