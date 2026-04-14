package edu.wearpark.backend;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;

import java.io.File;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * WearPark Pipeline Smoke Test
 * ============================
 * Standalone main — run from IntelliJ or Maven (does NOT run with mvn test).
 *
 * Simulates the full flow:
 *   Backend login → Netty TLS (fake IMU data) → ML inference → GET /prediction/latest
 *
 * Prerequisites:
 *   - Backend running on localhost:8080
 *   - Netty server running on localhost:9000
 *   - MongoDB seeded (admin@admin.com user + device-test device)
 *   - ML_HF_TOKEN set in application.yaml
 */
public class PipelineSmokeTest {

    // ── Config ────────────────────────────────────────────────────────────────

    static final String BACKEND_URL  = "http://localhost:8080";
    static final String NETTY_HOST   = "localhost";
    static final int    NETTY_PORT   = 9000;
    static final String EMAIL        = "admin@admin.com";
    static final String PASSWORD     = "Admin123$";
    static final String CA_CERT      = "e2e/certs/ca-test.crt";
    static final String DEVICE_CERT  = "e2e/certs/device-test.crt";
    static final String DEVICE_KEY   = "e2e/certs/device-test.key";
    static final int    N_SAMPLES    = 1001;  // triggers 1 full batch of 1000
    static final int    POLL_TIMEOUT = 30;    // seconds

    // Protocol
    static final byte MSG_TIMESTAMP   = 0x00;
    static final byte MSG_SINGLE_DATA = 0x01;

    static final ObjectMapper JSON   = new ObjectMapper();
    static final HttpClient   HTTP   = HttpClient.newHttpClient();
    static final Random       RANDOM = new Random();

    // ── Entry point ───────────────────────────────────────────────────────────

    public static void main(String[] args) throws Exception {
        banner();
        try {
            step(1, "Backend login");
            String jwt = login();
            ok("Authenticated as " + EMAIL);

            step(2, "Netty TLS — sending " + N_SAMPLES + " fake IMU samples");
            sendNettyData();

            step(3, "Polling backend for prediction (timeout=" + POLL_TIMEOUT + "s)");
            JsonNode prediction = pollPrediction(jwt);

            separator();
            if (prediction != null) {
                System.out.println(GREEN + BOLD + "  ✓  PIPELINE OK — full flow working!" + RESET);
            } else {
                System.out.println(RED + BOLD + "  ✗  PIPELINE FAILED — check backend logs" + RESET);
                System.exit(1);
            }
            separator();

        } catch (Exception e) {
            System.out.println(RED + "\n  ERROR: " + e.getMessage() + RESET);
            e.printStackTrace();
            System.exit(1);
        }
    }

    // ── Step 1: Login ─────────────────────────────────────────────────────────

    static String login() throws Exception {
        String body = JSON.writeValueAsString(new LoginPayload(EMAIL, PASSWORD));
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BACKEND_URL + "/auth/login"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        HttpResponse<String> res = HTTP.send(req, HttpResponse.BodyHandlers.ofString());
        if (res.statusCode() != 200)
            throw new RuntimeException("Login failed — HTTP " + res.statusCode() + ": " + res.body());

        return JSON.readTree(res.body()).get("jwt").asText();
    }

    // ── Step 2: Netty TLS client ──────────────────────────────────────────────

    static void sendNettyData() throws Exception {
        SslContext sslCtx = SslContextBuilder.forClient()
                .trustManager(new File(CA_CERT))
                .keyManager(new File(DEVICE_CERT), new File(DEVICE_KEY))
                .build();

        CompletableFuture<Void> done = new CompletableFuture<>();

        EventLoopGroup group = new NioEventLoopGroup(1);
        try {
            Bootstrap b = new Bootstrap();
            b.group(group)
             .channel(NioSocketChannel.class)
             .handler(new ChannelInitializer<SocketChannel>() {
                 @Override
                 protected void initChannel(SocketChannel ch) {
                     ch.pipeline().addLast(sslCtx.newHandler(ch.alloc(), NETTY_HOST, NETTY_PORT));
                     ch.pipeline().addLast(new DeviceSimulator(done));
                 }
             });

            ChannelFuture connect = b.connect(NETTY_HOST, NETTY_PORT).sync();
            ok("TLS connected to " + NETTY_HOST + ":" + NETTY_PORT);

            // Wait for the simulator to finish sending all data
            done.get(20, TimeUnit.SECONDS);
            connect.channel().close().sync();

        } finally {
            group.shutdownGracefully();
        }
    }

    // ── Step 3: Poll /prediction/latest ──────────────────────────────────────

    static JsonNode pollPrediction(String jwt) throws Exception {
        long deadline = System.currentTimeMillis() + POLL_TIMEOUT * 1000L;
        while (System.currentTimeMillis() < deadline) {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(BACKEND_URL + "/prediction/latest"))
                    .header("Authorization", "Bearer " + jwt)
                    .GET()
                    .build();

            HttpResponse<String> res = HTTP.send(req, HttpResponse.BodyHandlers.ofString());
            if (res.statusCode() == 200) {
                JsonNode p = JSON.readTree(res.body());
                ok("Prediction received!");
                System.out.println();
                printField("state",       p.get("state").asText());
                printField("label",       p.get("label").asText());
                printField("probability", String.format("%.4f", p.get("probability").asDouble()));
                printField("confidence",  p.get("confidence").asText());
                printField("prediction",  p.get("prediction").asText());
                printField("createdAt",   p.get("createdAt").asText());
                return p;
            }

            int remaining = (int) ((deadline - System.currentTimeMillis()) / 1000);
            System.out.print("  waiting for ML response... (" + remaining + "s left)\r");
            Thread.sleep(2000);
        }
        System.out.println();
        fail("No prediction received after " + POLL_TIMEOUT + "s");
        return null;
    }

    // ── Netty device simulator handler ────────────────────────────────────────

    static class DeviceSimulator extends SimpleChannelInboundHandler<ByteBuf> {

        private final CompletableFuture<Void> done;
        private int okCount = 0;

        DeviceSimulator(CompletableFuture<Void> done) {
            this.done = done;
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            done.completeExceptionally(cause);
            ctx.close();
        }

        @Override
        protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) throws Exception {
            String text = msg.toString(StandardCharsets.UTF_8).trim();
            if (!text.equals("OK")) return;

            okCount++;
            if (okCount == 1) {
                // Server ready — send TIMESTAMP
                ctx.writeAndFlush(buildTimestamp());
                ok("Server greeting received — TIMESTAMP sent");
            } else if (okCount == 2) {
                // TIMESTAMP ack — send all IMU samples then disconnect
                ok("TIMESTAMP acknowledged — sending " + N_SAMPLES + " samples...");
                sendImuData(ctx);
                done.complete(null);
            }
        }

        private void sendImuData(ChannelHandlerContext ctx) throws Exception {
            int chunkSize = 50; // messages per write
            for (int start = 0; start < N_SAMPLES; start += chunkSize) {
                int end = Math.min(start + chunkSize, N_SAMPLES);
                ByteBuf chunk = Unpooled.buffer(29 * (end - start));
                for (int i = start; i < end; i++) {
                    writeImuSample(chunk, i);
                }
                ctx.writeAndFlush(chunk).sync();
                System.out.print("  → " + end + "/" + N_SAMPLES + " samples\r");
                Thread.sleep(5);
            }
            System.out.println();
            ok(N_SAMPLES + " samples sent — 1 full batch (1000) queued for ML");
        }

        private static ByteBuf buildTimestamp() {
            ByteBuf buf = Unpooled.buffer(9);
            buf.writeByte(MSG_TIMESTAMP);
            buf.writeLongLE(System.currentTimeMillis());
            return buf;
        }

        private static void writeImuSample(ByteBuf buf, int i) {
            buf.writeByte(MSG_SINGLE_DATA);
            buf.writeIntLE(i * 10);                              // offsetMs: 10ms per sample = 100 Hz
            buf.writeFloat((float) (RANDOM.nextGaussian() * 0.05));  // ax (m/s²) — light tremor
            buf.writeFloat((float) (RANDOM.nextGaussian() * 0.05));  // ay
            buf.writeFloat(9.81f + (float) (RANDOM.nextGaussian() * 0.02)); // az ≈ gravity
            buf.writeFloat((float) (RANDOM.nextGaussian() * 0.01));  // gx (rad/s)
            buf.writeFloat((float) (RANDOM.nextGaussian() * 0.01));  // gy
            buf.writeFloat((float) (RANDOM.nextGaussian() * 0.01));  // gz
        }
    }

    // ── Protocol payloads ─────────────────────────────────────────────────────

    record LoginPayload(String email, String password) {}

    // ── Console formatting ────────────────────────────────────────────────────

    static final String GREEN = "\033[92m";
    static final String RED   = "\033[91m";
    static final String CYAN  = "\033[96m";
    static final String RESET = "\033[0m";
    static final String BOLD  = "\033[1m";

    static void banner() {
        System.out.println();
        System.out.println(BOLD + "  WearPark Pipeline Smoke Test" + RESET);
        System.out.println("  Backend : " + BACKEND_URL);
        System.out.println("  Netty   : " + NETTY_HOST + ":" + NETTY_PORT);
    }

    static void step(int n, String title) {
        System.out.println();
        System.out.println(CYAN + BOLD + "  ── Step " + n + ": " + title + RESET);
    }

    static void ok(String msg)         { System.out.println(GREEN + "  ✓  " + RESET + msg); }
    static void fail(String msg)       { System.out.println(RED   + "  ✗  " + RESET + msg); }
    static void separator()            { System.out.println("\n  " + "─".repeat(50)); }
    static void printField(String k, String v) {
        System.out.printf("  %-14s %s%n", k, BOLD + v + RESET);
    }
}
