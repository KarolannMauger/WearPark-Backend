package edu.wearpark.backend.netty;

import edu.wearpark.backend.domain.Device;
import edu.wearpark.backend.repository.DeviceRepository;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.ssl.SslHandler;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;

import javax.naming.InvalidNameException;
import javax.naming.ldap.LdapName;
import javax.naming.ldap.Rdn;
import javax.net.ssl.SSLPeerUnverifiedException;
import java.security.cert.X509Certificate;

@Component
@ChannelHandler.Sharable
@RequiredArgsConstructor
public class DeviceServerHandler extends ChannelInboundHandlerAdapter {
    final private DeviceRepository deviceRepo;
    final private Logger log;

    private String extractCommonName(ChannelHandlerContext ctx) throws SSLPeerUnverifiedException, InvalidNameException {
        var ssl = ctx.pipeline().get(SslHandler.class);
        var session = ssl.engine().getSession();
        X509Certificate cert = (X509Certificate) session.getPeerCertificates()[0];
        LdapName ldapDN = new LdapName(
                cert.getSubjectX500Principal().getName()
        );
        for (Rdn rdn : ldapDN.getRdns()) {
            if ("CN".equalsIgnoreCase(rdn.getType())) {
                return rdn.getValue().toString();
            }
        }
        log.error("No common name inside client certificate");
        throw new RuntimeException("Common name not inside client certificate");
    }
    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        log.info("New TCP connection: " + ctx.name());
        ctx.pipeline().get(SslHandler.class).handshakeFuture().addListener(future -> {
            if(!future.isSuccess()) {
                log.error("TCP Handshake failed");
                return;
            }
            try {
                var name = extractCommonName(ctx);
                var optionalDevice = deviceRepo.findByDeviceKey(name);
                if(optionalDevice.isEmpty()) {
                    ctx.writeAndFlush("NO_DEVICE\n");
                    ctx.close();
                    log.info("No Device found: " + name);
                    return;
                }
                var device = optionalDevice.get();
                if(device.getUserId() == null) {
                    ctx.writeAndFlush("NO_USER\n");
                    ctx.close();
                    log.info("No user associated with device: "+ device.getDeviceKey());
                    return;
                }
                ctx.channel().attr(Attributes.DEVICE).set(device);
                ctx.writeAndFlush("OK\n");
            } catch (Exception ex) {
                log.error(ex.toString());
            }
        });
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        log.info("TCP connection close: " + ctx.name());
    }
}
