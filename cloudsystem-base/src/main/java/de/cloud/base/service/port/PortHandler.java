package de.cloud.base.service.port;

import de.cloud.base.Base;
import de.cloud.api.groups.ServiceGroup;

import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.ServerSocket;

public final class PortHandler {

    private static final int PORTS_BOUNCE_PROXY = Base.getInstance().getConfig().getProxyStartPort();
    private static final int PORTS_BOUNCE = Base.getInstance().getConfig().getMinecraftStartPort();

    public static int getNextPort(ServiceGroup service) {
        var port = service.getGameServerVersion().isProxy() ? PORTS_BOUNCE_PROXY : PORTS_BOUNCE;
        while (isPortUsed(port)) {
            port++;
        }
        return port;
    }

    private static boolean isPortUsed(int port) {
        for (final var service : Base.getInstance().getServiceManager().getAllCachedServices()) {
            if (service.getNode().equals(Base.getInstance().getNode().getName())) {
                if (service.getPort() == port) return true;
            }
        }
        try (final var serverSocket = new DatagramSocket(port)) {
            serverSocket.close();
            return false;
        } catch (Exception exception) {
            return true;
        }
    }
}
