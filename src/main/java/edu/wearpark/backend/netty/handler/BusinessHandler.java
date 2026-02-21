package edu.wearpark.backend.netty.handler;

import edu.wearpark.backend.domain.MotionEntry;
import edu.wearpark.backend.netty.Attributes;
import edu.wearpark.backend.netty.protocol.Message;
import edu.wearpark.backend.netty.protocol.SingleMessage;
import edu.wearpark.backend.netty.protocol.TimestampMessage;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;

import javax.naming.InvalidNameException;
import javax.net.ssl.SSLPeerUnverifiedException;
import java.time.Instant;

@Component
@ChannelHandler.Sharable
@RequiredArgsConstructor
public class BusinessHandler extends SimpleChannelInboundHandler<Message> {
    private final Logger log;
    private void handleSingle(ChannelHandlerContext ctx, SingleMessage message) throws InvalidNameException, SSLPeerUnverifiedException {
        var device = ctx.channel().attr(Attributes.DEVICE).get();
        log.info("\nDEVICE: " + device.getDeviceKey() + "\nax: " + message.getAx());
    }
    private void handleTimestamp(ChannelHandlerContext ctx, TimestampMessage message) throws Exception {
        log.info("Timestamp: " + message.getTimestamp());
        ctx.writeAndFlush("OK\n");
    }
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Message message) throws Exception {
        switch(message.getMessageType()) {
            case SINGLE_DATA -> handleSingle(ctx, (SingleMessage) message);
            case TIMESTAMP -> handleTimestamp(ctx, (TimestampMessage) message);
        }
    }
}
