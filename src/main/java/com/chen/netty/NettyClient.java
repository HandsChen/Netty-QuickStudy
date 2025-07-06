package com.chen.netty;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

import java.util.concurrent.TimeUnit;

public class NettyClient {
    public static void main(String[] args) {
        Bootstrap bootstrap = new Bootstrap(); //注意不是ServerBootstrap
        bootstrap.group(new NioEventLoopGroup()); //客户端没有读写事件，所以只需要一个监听连接的group
        bootstrap.channel(NioSocketChannel.class).handler(new ChannelInitializer<SocketChannel>() { //这里不需要使用childHandler配置每一个连接，因为客户端只对应一个服务端
            @Override
            protected void initChannel(SocketChannel socketChannel) throws Exception {
                //1.只写
//                socketChannel.pipeline().addLast(new StringEncoder());
                //2.加入了服务端回传打印
                socketChannel.pipeline().addLast(new LineBasedFrameDecoder(1024))
                        .addLast(new StringDecoder())//拿到socketChannel中的pipeLine，并在后面加上我们真正的handler
                        .addLast(new StringEncoder()) //这里出站要加在业务代码前面
                        .addLast(new SimpleChannelInboundHandler<String>() {
                            @Override
                            protected void channelRead0(ChannelHandlerContext channelHandlerContext, String s) throws Exception {
                                System.out.println(s);//打印消息，这个消息来自于前面的解码器解码
                            }
                        });
            }
        });
        //客户端配置完成后需要进行连接服务端
        ChannelFuture connectFuture = bootstrap.connect("localhost", 8080);
        connectFuture.addListener(f -> {
            if (f.isSuccess()) {
                System.out.println("客户端成功链接了8080服务器");
                //1.直接发送
//                connectFuture.channel().writeAndFlush("hello\n"); //直接发送
                //2.周期发送
                EventLoop eventExecutors = connectFuture.channel().eventLoop(); //拿到通道对应的线程
                eventExecutors.scheduleAtFixedRate(() -> {
                    connectFuture.channel().writeAndFlush("hello  " + System.currentTimeMillis() + "\n");
                }, 0, 1, TimeUnit.SECONDS);
            } else {
                System.out.println("客户端连接服务端失败");
            }
        });
    }
}
