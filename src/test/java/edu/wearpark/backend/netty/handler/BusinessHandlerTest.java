package edu.wearpark.backend.netty.handler;

import edu.wearpark.backend.domain.Device;
import edu.wearpark.backend.netty.Attributes;
import edu.wearpark.backend.netty.protocol.MessageType;
import edu.wearpark.backend.netty.protocol.SingleMessage;
import edu.wearpark.backend.netty.protocol.TimestampMessage;
import edu.wearpark.backend.repository.MotionEntryRepository;
import edu.wearpark.backend.util.MotionDataListWrapper;
import edu.wearpark.backend.util.MotionDataWrapper;
import io.netty.channel.embedded.EmbeddedChannel;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;

import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class BusinessHandlerTest {

    private Logger log;
    private MotionEntryRepository repo;
    private BusinessHandler handler;
    private EmbeddedChannel channel;

    @BeforeEach
    void setup() {
        log = mock(Logger.class);
        repo = mock(MotionEntryRepository.class);
        handler = new BusinessHandler(log, repo);

        channel = new EmbeddedChannel(handler);
    }

    @Test
    void shouldHandleTimestampMessage() {
        Instant now = Instant.now();

        TimestampMessage msg = mock(TimestampMessage.class);
        when(msg.getMessageType()).thenReturn(MessageType.TIMESTAMP);
        when(msg.getTimestamp()).thenReturn(now);

        channel.writeInbound(msg);

        verify(log).info("RECEIVED TIMESTAMP");
        assert channel.readOutbound().equals("OK\n");
    }

    @Test
    void shouldRejectSingleMessageWithoutTimestamp() {
        SingleMessage msg = mock(SingleMessage.class);
        when(msg.getMessageType()).thenReturn(MessageType.SINGLE_DATA);

        channel.attr(Attributes.DEVICE).set(mock(Device.class));

        channel.writeInbound(msg);

        assert channel.readOutbound().equals("NO_TIMESTAMP");
        assert !channel.isOpen();
    }

    @Test
    void shouldSaveMotionEntryWhenBufferFull() {
        SingleMessage msg = mock(SingleMessage.class);
        when(msg.getMessageType()).thenReturn(MessageType.SINGLE_DATA);

        Device device = mock(Device.class);
        when(device.getUserId()).thenReturn(new ObjectId());

        Instant ts = Instant.now();

        MotionDataListWrapper wrapper = mock(MotionDataListWrapper.class);
        when(wrapper.get()).thenReturn(null); // force save path
        when(wrapper.size()).thenReturn(5);
        when(wrapper.getBuffer()).thenReturn(java.nio.ByteBuffer.allocate(10));
        when(wrapper.getLast()).thenReturn(mock(MotionDataWrapper.class));
        when(wrapper.getLast().offsetMs()).thenReturn(1);

        channel.attr(Attributes.DEVICE).set(device);
        channel.attr(Attributes.TIMESTAMP).set(ts);
        channel.attr(Attributes.LAST_ENTRY).set(ts);
        channel.attr(Attributes.DATA_LIST).set(wrapper);

        channel.writeInbound(msg);

        verify(repo).save(any());
    }
}