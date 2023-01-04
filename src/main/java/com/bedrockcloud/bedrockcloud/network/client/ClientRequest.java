package com.bedrockcloud.bedrockcloud.network.client;

import java.io.*;
import com.bedrockcloud.bedrockcloud.BedrockCloud;
import jdk.net.ExtendedSocketOptions;

import java.net.Socket;
import java.net.SocketOption;
import java.net.StandardSocketOptions;

public class ClientRequest extends Thread implements AutoCloseable {
    private final Socket socket;
    private DataOutputStream dataOutputStream;
    private DataInputStream dataInputStream;

    public ClientRequest(final Socket socket) {
        this.socket = socket;
        try {
            this.dataInputStream = new DataInputStream(socket.getInputStream());
            this.dataOutputStream = new DataOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            BedrockCloud.getLogger().exception(e);
        }
    }

    public Socket getSocket() {
        return this.socket;
    }

    @Override
    public void run() {
        while (!this.socket.isClosed()) {
            String line = null;
            try {
                line = this.dataInputStream.readLine();
                if (line == null) {
                    return;
                }
                try {
                    BedrockCloud.getPacketHandler().handleCloudPacket(BedrockCloud.getPacketHandler().handleJsonObject(BedrockCloud.getPacketHandler().getPacketNameByRequest(line), line), this);
                } catch (NullPointerException ex) {
                    BedrockCloud.getLogger().exception(ex);
                }
            } catch (NullPointerException | IOException ex1) {
                BedrockCloud.getLogger().exception(ex1);
            }
        }
        BedrockCloud.getLogger().warning("Server connection failed, stopping thread.");
        this.stop();
        try {
            close();
        } catch (Exception ignored) {}
    }

    @Override
    public void close() throws Exception {
    }
}