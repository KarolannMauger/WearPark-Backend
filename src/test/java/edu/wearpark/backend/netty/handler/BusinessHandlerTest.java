package edu.wearpark.backend.netty.handler;

import edu.wearpark.backend.domain.Device;
import edu.wearpark.backend.domain.MotionEntry;
import edu.wearpark.backend.netty.Attributes;
import edu.wearpark.backend.netty.protocol.MessageType;
import edu.wearpark.backend.netty.protocol.SingleMessage;
import edu.wearpark.backend.netty.protocol.TimestampMessage;
import edu.wearpark.backend.repository.MotionEntryRepository;
import edu.wearpark.backend.util.MotionDataListWrapper;
import edu.wearpark.backend.util.MotionDataWrapper;
import edu.wearpark.backend.ws.WsMotionHandler;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.Attribute;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.springframework.web.socket.BinaryMessage;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class BusinessHandlerTest {

    private Logger logger;
    private MotionEntryRepository motionEntryRepo;
    private WsMotionHandler wsMotionHandler;

    private BusinessHandler handler;

    private ChannelHandlerContext ctx;
    private Channel channel;

    @BeforeEach
    void setUp() {
        logger = mock(Logger.class);
        motionEntryRepo = mock(MotionEntryRepository.class);
        wsMotionHandler = mock(WsMotionHandler.class);

        handler = new BusinessHandler(
                logger,
                motionEntryRepo,
                wsMotionHandler
        );

        ctx = mock(ChannelHandlerContext.class);
        channel = mock(Channel.class);

        when(ctx.channel()).thenReturn(channel);
        when(ctx.writeAndFlush(any())).thenReturn(mock(ChannelFuture.class));
    }

    @SuppressWarnings("unchecked")
    private <T> Attribute<T> mockAttribute(T value) {
        Attribute<T> attr = mock(Attribute.class);
        when(attr.get()).thenReturn(value);
        return attr;
    }

    @Test
    void channelRead0_shouldHandleTimestampMessage() throws Exception {
        Instant timestamp = Instant.parse("2026-01-01T00:00:00Z");

        TimestampMessage message = mock(TimestampMessage.class);

        when(message.getMessageType())
                .thenReturn(MessageType.TIMESTAMP);

        when(message.getTimestamp())
                .thenReturn(timestamp);

        Attribute<Instant> timestampAttr = mock(Attribute.class);
        Attribute<MotionDataListWrapper> dataListAttr = mock(Attribute.class);
        Attribute<Instant> lastEntryAttr = mock(Attribute.class);

        when(channel.attr(Attributes.TIMESTAMP))
                .thenReturn(timestampAttr);

        when(channel.attr(Attributes.DATA_LIST))
                .thenReturn(dataListAttr);

        when(channel.attr(Attributes.LAST_ENTRY))
                .thenReturn(lastEntryAttr);

        handler.channelRead(ctx, message);

        verify(timestampAttr).set(timestamp);

        verify(dataListAttr)
                .set(any(MotionDataListWrapper.class));

        verify(lastEntryAttr)
                .set(timestamp);

        verify(ctx).writeAndFlush("OK\n");

        verify(logger).info("RECEIVED TIMESTAMP");
    }


    @Test
    void channelRead0_shouldPersistMotionEntryWhenBufferIsFull() throws Exception {
        ObjectId userId = new ObjectId();

        Device device = Device.builder()
                .userId(userId)
                .deviceKey("device-1")
                .build();

        Instant timestamp = Instant.now();

        byte[] bytes = new byte[28];
        MotionDataListWrapper dataList =
                new MotionDataListWrapper(bytes);

        // Fill internal pointer
        assertNotNull(dataList.get());

        SingleMessage message = mock(SingleMessage.class);

        MotionDataWrapper inputWrapper =
                new MotionDataWrapper(new byte[28], 0);

        when(message.getMessageType())
                .thenReturn(MessageType.SINGLE_DATA);

        when(message.getWrapper())
                .thenReturn(inputWrapper);

        Attribute<Device> deviceAttr = mockAttribute(device);
        Attribute<Instant> timestampAttr = mockAttribute(timestamp);
        Attribute<Instant> lastEntryAttr = mock(Attribute.class);
        Attribute<MotionDataListWrapper> dataListAttr =
                mockAttribute(dataList);

        when(lastEntryAttr.get()).thenReturn(timestamp);

        when(channel.attr(Attributes.DEVICE))
                .thenReturn(deviceAttr);

        when(channel.attr(Attributes.TIMESTAMP))
                .thenReturn(timestampAttr);

        when(channel.attr(Attributes.LAST_ENTRY))
                .thenReturn(lastEntryAttr);

        when(channel.attr(Attributes.DATA_LIST))
                .thenReturn(dataListAttr);

        handler.channelRead(ctx, message);

        verify(motionEntryRepo)
                .save(any(MotionEntry.class));

        verify(lastEntryAttr)
                .set(any(Instant.class));
    }

}
