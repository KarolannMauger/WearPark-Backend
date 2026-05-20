package edu.wearpark.backend.config;

import io.netty.handler.ssl.ClientAuth;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.net.ssl.SSLException;
import java.io.File;

@Configuration
public class SSLConfig {
    @Bean
    SslContext sslContext(
            @Value("${netty.server-cert-path}") String serverCertPath,
            @Value("${netty.server-key-path}") String serverKeyPath,
            @Value("${netty.ca-cert-path}") String caCertPath
    ) throws SSLException {
        return SslContextBuilder.forServer(
                new File(serverCertPath),
                new File(serverKeyPath))
                .trustManager(new File(caCertPath))
                .clientAuth(ClientAuth.REQUIRE)
                .build();
    }
}
