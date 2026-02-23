package edu.wearpark.backend.netty;

import edu.wearpark.backend.netty.handler.BusinessHandler;
import edu.wearpark.backend.netty.handler.ProtocolDecoder;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

import javax.net.ssl.SSLEngine;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class DeviceServerInitializerTest {

    private SslContext sslContext;
    private DeviceServerHandler deviceHandler;
    private BusinessHandler businessHandler;
    private SocketChannel socketChannel;
    private ChannelPipeline pipeline;
    private SslHandler sslHandler;
    private SSLEngine sslEngine;

    private DeviceServerInitializer initializer;

    @BeforeEach
    void setUp() {
        sslContext = mock(SslContext.class);
        deviceHandler = mock(DeviceServerHandler.class);
        businessHandler = mock(BusinessHandler.class);
        socketChannel = mock(SocketChannel.class);
        pipeline = mock(ChannelPipeline.class);
        sslHandler = mock(SslHandler.class);
        sslEngine = mock(SSLEngine.class);

        when(socketChannel.pipeline()).thenReturn(pipeline);
        when(socketChannel.alloc()).thenReturn(mock(io.netty.buffer.ByteBufAllocator.class));
        when(sslContext.newHandler(any())).thenReturn(sslHandler);

        when(pipeline.addFirst(any())).thenReturn(pipeline);
        when(pipeline.addLast(any())).thenReturn(pipeline);

        initializer = new DeviceServerInitializer(sslContext, deviceHandler, businessHandler);
    }

    @Test
    void initChannel_shouldConfigurePipelineInCorrectOrder() throws Exception {
        initializer.initChannel(socketChannel);

        InOrder inOrder = inOrder(pipeline);

        inOrder.verify(pipeline).addFirst(sslHandler);
        inOrder.verify(pipeline).addLast(any(StringEncoder.class));
        inOrder.verify(pipeline).addLast(deviceHandler);
        inOrder.verify(pipeline).addLast(any(ProtocolDecoder.class));
        inOrder.verify(pipeline).addLast(businessHandler);
    }

    @Test
    void initChannel_shouldAddAllExpectedHandlers() throws Exception {
        initializer.initChannel(socketChannel);

        verify(pipeline).addFirst(sslHandler);
        verify(pipeline).addLast(any(StringEncoder.class));
        verify(pipeline).addLast(deviceHandler);
        verify(pipeline).addLast(any(ProtocolDecoder.class));
        verify(pipeline).addLast(businessHandler);

        verifyNoMoreInteractions(pipeline);
    }

    @Test
    void shouldCallSslContextNewHandler() throws Exception {
        initializer.initChannel(socketChannel);

        verify(sslContext).newHandler(any());
    }
}