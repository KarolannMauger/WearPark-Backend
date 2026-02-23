package edu.wearpark.backend.netty;

import edu.wearpark.backend.domain.Device;
import edu.wearpark.backend.repository.DeviceRepository;
import io.netty.channel.*;
import io.netty.handler.ssl.SslHandler;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.slf4j.Logger;

import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLEngine;
import javax.security.auth.x500.X500Principal;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class DeviceServerHandlerTest {

    private DeviceRepository repo;
    private Logger log;
    private DeviceServerHandler handler;

    private ChannelHandlerContext ctx;
    private ChannelPipeline pipeline;
    private SslHandler sslHandler;
    private Future<Channel> handshakeFuture;

    @BeforeEach
    void setup() {
        repo = mock(DeviceRepository.class);
        log = mock(Logger.class);
        handler = new DeviceServerHandler(repo, log);

        ctx = mock(ChannelHandlerContext.class);
        pipeline = mock(ChannelPipeline.class);
        sslHandler = mock(SslHandler.class);
        handshakeFuture = mock(Future.class);

        when(ctx.pipeline()).thenReturn(pipeline);
        when(ctx.name()).thenReturn("test");
        when(pipeline.get(SslHandler.class)).thenReturn(sslHandler);
        when(sslHandler.handshakeFuture()).thenReturn(handshakeFuture);
        when(handshakeFuture.isSuccess()).thenReturn(true);
        when(ctx.writeAndFlush(any())).thenReturn(mock(ChannelFuture.class));
    }

    private void triggerHandshake() throws Exception {
        ArgumentCaptor<GenericFutureListener<Future<Channel>>> captor =
                ArgumentCaptor.forClass(GenericFutureListener.class);

        verify(handshakeFuture).addListener(captor.capture());
        captor.getValue().operationComplete(handshakeFuture);
    }

    private void mockCertificate(String cn) throws Exception {
        SSLEngine engine = mock(SSLEngine.class);
        SSLSession session = mock(SSLSession.class);
        X509Certificate cert = mock(X509Certificate.class);

        when(sslHandler.engine()).thenReturn(engine);
        when(engine.getSession()).thenReturn(session);
        when(session.getPeerCertificates()).thenReturn(new Certificate[]{cert});
        when(cert.getSubjectX500Principal())
                .thenReturn(new X500Principal("CN=" + cn));
    }

    @Test
    void shouldSendNoDeviceIfNotFound() throws Exception {
        mockCertificate("device-1");
        when(repo.findByDeviceKey("device-1")).thenReturn(Optional.empty());

        handler.channelActive(ctx);
        triggerHandshake();

        verify(ctx).writeAndFlush("NO_DEVICE\n");
        verify(ctx).close();
    }

    @Test
    void shouldSendNoUserIfDeviceHasNoUser() throws Exception {
        mockCertificate("device-2");

        Device device = mock(Device.class);
        when(device.getUserId()).thenReturn(null);
        when(repo.findByDeviceKey("device-2")).thenReturn(Optional.of(device));

        handler.channelActive(ctx);
        triggerHandshake();

        verify(ctx).writeAndFlush("NO_USER\n");
        verify(ctx).close();
    }

    @Test
    void shouldSetAttributeAndSendOkIfValidDevice() throws Exception {
        mockCertificate("device-3");

        Device device = mock(Device.class);
        when(device.getUserId()).thenReturn(new ObjectId("000000000000000000000001"));
        when(repo.findByDeviceKey("device-3")).thenReturn(Optional.of(device));

        Channel channel = mock(Channel.class);
        Attribute<Device> attribute = mock(Attribute.class);

        when(ctx.channel()).thenReturn(channel);
        when(channel.attr(any(AttributeKey.class))).thenReturn(attribute);

        handler.channelActive(ctx);
        triggerHandshake();

        verify(attribute).set(device);
        verify(ctx).writeAndFlush("OK\n");
        verify(ctx, never()).close();
    }
}