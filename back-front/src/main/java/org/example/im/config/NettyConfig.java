package org.example.im.config;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolConfig;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import org.example.im.handler.WebSocketHandler;
import org.example.im.service.MessageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

@Configuration
public class NettyConfig {

    @Value("${netty.port}")
    private int port;

    private final WebSocketHandler webSocketHandler;

    public NettyConfig(WebSocketHandler webSocketHandler) {
        this.webSocketHandler = webSocketHandler; 
    }

    @PostConstruct
    public void startNettyServer() {
        new Thread(() -> {
            EventLoopGroup bossGroup = new NioEventLoopGroup();
            EventLoopGroup workerGroup = new NioEventLoopGroup();
            try {
                ServerBootstrap bootstrap = new ServerBootstrap();
                bootstrap.group(bossGroup, workerGroup)
                         .channel(NioServerSocketChannel.class)
                         .childHandler(new ChannelInitializer<SocketChannel>() {
                             @Override
                             protected void initChannel(SocketChannel ch) {
                                 ChannelPipeline pipeline = ch.pipeline();
                                 // 添加原始请求日志
//                                 pipeline.addLast(new ChannelInboundHandlerAdapter() {
//                                     @Override
//                                     public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
//                                         System.out.println("📥 原始消息类型: " + msg.getClass().getSimpleName());
//                                         ctx.fireChannelRead(io.netty.util.ReferenceCountUtil.retain(msg));
//                                     }
//                                 });
                                 pipeline.addLast(new HttpServerCodec());
                                 pipeline.addLast(new HttpObjectAggregator(65536));
                                 pipeline.addLast(new WebSocketServerProtocolHandler(
                                         WebSocketServerProtocolConfig.newBuilder()
                                                                      .websocketPath("/ws")
                                                                      .checkStartsWith(true) //  允许 /ws?userId=xxx
                                                                      .allowExtensions(true)
                                                                      .build()
                                 ));
                                 pipeline.addLast(webSocketHandler); // 单例
                             }
                         });

                ChannelFuture future = bootstrap.bind(port).sync();
                System.out.println("Netty WebSocket server started on port: " + port);
                future.channel().closeFuture().sync();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                bossGroup.shutdownGracefully();
                workerGroup.shutdownGracefully();
            }
        }, "NettyServerThread").start();
    }
}
