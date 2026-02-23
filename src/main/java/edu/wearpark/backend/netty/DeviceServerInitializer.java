package edu.wearpark.backend.netty;

import edu.wearpark.backend.netty.handler.BusinessHandler;
import edu.wearpark.backend.netty.handler.ProtocolDecoder;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.ssl.SslContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DeviceServerInitializer extends ChannelInitializer<SocketChannel> {
    final private SslContext sslCtx;
    final private DeviceServerHandler deviceHandler;
    final private BusinessHandler businessHandler;
    @Override
    protected void initChannel(SocketChannel sc) throws Exception {
        sc.pipeline()
                .addFirst(sslCtx.newHandler(sc.alloc()))
                .addLast(new StringEncoder())
                .addLast(deviceHandler)
                .addLast(new ProtocolDecoder())
                .addLast(businessHandler);

    }
}
