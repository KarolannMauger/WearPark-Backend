package edu.wearpark.backend.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.net.ssl.SSLException;

@Component
@RequiredArgsConstructor
public class DeviceServer {
    final private Logger logger;
    final private DeviceServerInitializer initializer;
    @Value("${netty.port}")
    private Integer PORT;

    @PostConstruct
    public void start() throws SSLException {
        new Thread(this::run).start();
    }
    private void run(){
        try {
            EventLoopGroup masterGroup = new NioEventLoopGroup();
            EventLoopGroup slaveGroup  = new NioEventLoopGroup();

            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(masterGroup, slaveGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(initializer);
            ChannelFuture future = bootstrap.bind(PORT).sync();
            logger.info("TCP netty server listening on port " + PORT);
            future.channel().closeFuture().sync();
        } catch (Exception error) {
            logger.error(error.toString());
            Thread.currentThread().interrupt();
        }
    }
}
