package com.bedrockcloud.bedrockcloud.network.client;

import java.io.*;
import com.bedrockcloud.bedrockcloud.BedrockCloud;
import com.google.gson.JsonObject;
import jdk.net.ExtendedSocketOptions;
import org.json.simple.JSONObject;

import java.net.Socket;
import java.net.SocketOption;
import java.net.StandardSocketOptions;

public class ClientRequest extends Thread implements AutoCloseable {
    private static final int BUFFER_SIZE = 1024;
    private final Socket socket;
    private DataOutputStream dataOutputStream;
    private DataInputStream dataInputStream;

    public ClientRequest(final Socket socket) {
        this.socket = socket;
        try {
            this.dataInputStream = new DataInputStream(new BufferedInputStream(socket.getInputStream(), BUFFER_SIZE));
            this.dataOutputStream = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream(), BUFFER_SIZE));
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
                    close();
                    return;
                }
                try {
                    JSONObject packet = BedrockCloud.getPacketHandler().handleJsonObject(BedrockCloud.getPacketHandler().getPacketNameByRequest(line), line);
                    if (packet != null) {
                        BedrockCloud.getPacketHandler().handleCloudPacket(packet, this);
                    }
                } catch (NullPointerException ex) {
                    BedrockCloud.getLogger().exception(ex);
                }
            } catch (Exception ex1) {
                BedrockCloud.getLogger().exception(ex1);
            }
        }
        BedrockCloud.getLogger().warning("Server connection failed, stopping thread.");
        try {
            close();
        } catch (Exception ignored) {}
    }

    @Override
    public void close() throws Exception {
        if (this.socket != null) {
            this.socket.close();
        }
    }
}