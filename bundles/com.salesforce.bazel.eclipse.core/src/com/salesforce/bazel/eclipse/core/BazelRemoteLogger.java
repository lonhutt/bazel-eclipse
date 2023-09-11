package com.salesforce.bazel.eclipse.core;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BazelRemoteLogger {

    private static class BazelWebSocketServer extends WebSocketServer {

        public boolean isRunning = false;
        List<WebSocket> infoClients = new ArrayList<>();
        List<WebSocket> warnClients = new ArrayList<>();
        List<WebSocket> errorClients = new ArrayList<>();

        public BazelWebSocketServer(InetSocketAddress address) {
            super(address);
        }

        @Override
        public void onClose(WebSocket conn, int code, String reason, boolean remote) {
            isRunning = false;
            LOGGER.info(
                "closed " + conn.getRemoteSocketAddress() + " with exit code " + code + " additional info: " + reason);
        }

        @Override
        public void onError(WebSocket conn, Exception ex) {
            LOGGER.error("an error occurred on connection " + conn.getRemoteSocketAddress() + ":" + ex);
        }

        @Override
        public void onMessage(WebSocket conn, ByteBuffer message) {
            LOGGER.info("received ByteBuffer from " + conn.getRemoteSocketAddress());
        }

        @Override
        public void onMessage(WebSocket conn, String message) {
            LOGGER.info("received message from " + conn.getRemoteSocketAddress() + ": " + message);
        }

        @Override
        public void onOpen(WebSocket conn, ClientHandshake handshake) {
            conn.send(String.format("%s client to the Bazel WebSocket Server", handshake.getResourceDescriptor())); //This method sends a message to the new client
            switch (handshake.getResourceDescriptor()) {
                case "/info":
                    infoClients.add(conn);
                    break;
                case "/warn":
                    warnClients.add(conn);
                    break;
                case "/error":
                    errorClients.add(conn);
                    break;
                default:
                    break;
            }
            LOGGER.info("new connection to " + conn.getRemoteSocketAddress());
        }

        @Override
        public void onStart() {
            isRunning = true;
            LOGGER.info("BazelWebSocketServer started successfully");
        }
    }

    private static BazelWebSocketServer server;

    private static Logger LOGGER = LoggerFactory.getLogger(BazelRemoteLogger.class);

    public static void error(String msg) {
        initServer();
        BazelRemoteLogger.server.broadcast(msg, BazelRemoteLogger.server.errorClients);
    }

    public static void info(String msg) {
        initServer();
        BazelRemoteLogger.server.broadcast(msg, BazelRemoteLogger.server.warnClients);
    }

    private static void initServer() {
        try {
            if ((BazelRemoteLogger.server == null) || !BazelRemoteLogger.server.isRunning) {
                BazelRemoteLogger.server = new BazelWebSocketServer(new InetSocketAddress("localhost", 5001));
                BazelRemoteLogger.server.start();
                BazelRemoteLogger.server.isRunning = true;
            }
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage());
        }
    }

    public static void warn(String msg) {
        initServer();
        BazelRemoteLogger.server.broadcast(msg, BazelRemoteLogger.server.infoClients);
    }
}
