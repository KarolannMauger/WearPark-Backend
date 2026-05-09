package edu.wearpark.backend.netty.handler;

import edu.wearpark.backend.domain.MotionEntry;
import edu.wearpark.backend.netty.Attributes;
import edu.wearpark.backend.netty.protocol.Message;
import edu.wearpark.backend.netty.protocol.SingleMessage;
import edu.wearpark.backend.netty.protocol.TimestampMessage;
import edu.wearpark.backend.repository.MotionEntryRepository;
import edu.wearpark.backend.util.MotionDataListWrapper;
import edu.wearpark.backend.ws.WsMotionHandler;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.BinaryMessage;
import org.w3c.dom.Attr;

import javax.naming.InvalidNameException;
import javax.net.ssl.SSLPeerUnverifiedException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.time.Instant;

@Component
@ChannelHandler.Sharable
@RequiredArgsConstructor
public class BusinessHandler extends SimpleChannelInboundHandler<Message> {
    private final Logger log;
    private final MotionEntryRepository motionEntryRepo;
    private final WsMotionHandler wsMotionHandler;
    private void broadcastWs(ObjectId userId, SingleMessage message) throws IOException {
        ByteBuffer buffer = ByteBuffer
                .allocate(4)
                .order(ByteOrder.LITTLE_ENDIAN);
        buffer.putFloat(0, message.getWrapper().accGeometricMean());
        wsMotionHandler.sendTo(userId, new BinaryMessage(buffer));
    }
    private void handleSingle(ChannelHandlerContext ctx, SingleMessage message) throws InvalidNameException, IOException {
        var device    = ctx.channel().attr(Attributes.DEVICE).get();
        var timestamp = ctx.channel().attr(Attributes.TIMESTAMP).get();
        var lastEntry = ctx.channel().attr(Attributes.LAST_ENTRY).get();
        var dataList  = ctx.channel().attr(Attributes.DATA_LIST).get();
        if(timestamp == null) {
            ctx.writeAndFlush("NO_TIMESTAMP");
            ctx.close();
            return;
        }
        ///
        var output = dataList.get();
        if(output == null) {
            Instant     end         = Instant.ofEpochMilli(dataList.getLast().offsetMs() + lastEntry.toEpochMilli());
            MotionEntry motionEntry = MotionEntry.builder()
                    .start(lastEntry)
                    .end(end)
                    .userId(device.getUserId())
                    .nbEntries(dataList.size())
                    .data(dataList.getBuffer().array().clone())
                    .build();
            motionEntryRepo.save(motionEntry);
            ctx.channel().attr(Attributes.LAST_ENTRY).set(end);
            dataList.reset();
            output = dataList.get();
        }
        if(output == null)
            return;
        var input = message.getWrapper();
        output.copyFrom(input);
        System.out.println("TCP DATA IN: [" + device.getDeviceKey() + "] " + input.accGeometricMean());
        output.setOffsetMs((int) (input.offsetMs() - lastEntry.toEpochMilli() + timestamp.toEpochMilli()));
        ///
        broadcastWs(device.getUserId(), message);
    }
    private void handleTimestamp(ChannelHandlerContext ctx, TimestampMessage message) throws Exception {
        ctx.channel().attr(Attributes.TIMESTAMP).set(message.getTimestamp());
        ctx.channel().attr(Attributes.DATA_LIST).set(new MotionDataListWrapper(new byte[28*1000]));
        ctx.channel().attr(Attributes.LAST_ENTRY).set(message.getTimestamp());
        ctx.writeAndFlush("OK\n");
        log.info("RECEIVED TIMESTAMP");
    }
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Message message) throws Exception {
        switch(message.getMessageType()) {
            case SINGLE_DATA -> handleSingle(ctx, (SingleMessage) message);
            case TIMESTAMP   -> handleTimestamp(ctx, (TimestampMessage) message);
        }
    }
}
