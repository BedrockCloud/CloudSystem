package de.cloud.base;

import de.cloud.api.groups.ServiceGroup;
import de.cloud.api.logger.LogType;
import de.cloud.api.network.packet.service.ServiceAddPacket;
import de.cloud.api.service.ServiceState;
import de.cloud.base.service.LocalService;
import de.cloud.base.service.SimpleServiceManager;
import de.cloud.base.service.port.PortHandler;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class WorkerThread extends Thread {

    private static final int MAX_BOOTABLE_SERVICES = 1;

    private final Base base;
    private final StringBuilder stringBuilder = new StringBuilder();
    private final byte[] buffer = new byte[2048];
    private final List<Runnable> tasks = new ArrayList<>();

    private static final Logger LOGGER = Logger.getLogger(WorkerThread.class.getName());

    WorkerThread(Base base) {
        super("Cloud-Worker-Thread");
        this.base = base;
    }

    @Override
    public void run() {
        while (this.base.isRunning()) {
            checkForQueue();

            this.base.getServiceManager().getAllCachedServices().stream()
                .filter(service -> service instanceof LocalService localService && localService.getProcess() != null)
                .map(service -> (LocalService) service)
                .forEach(localService -> {
                    var process = localService.getProcess();
                    if (process.isAlive()) {
                        readStream(process.getErrorStream(),
                            s -> this.base.getLogger().log("[" + localService.getName() + "] " + s, LogType.ERROR));
                        if (localService.isScreen()) {
                            readStream(process.getInputStream(),
                                s -> this.base.getLogger().log("[" + localService.getName() + "] " + s, LogType.INFO));
                        }
                    }
                });

            tasks.forEach(Runnable::run);
            tasks.clear();

            try {
                Thread.sleep(200L);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                LOGGER.log(Level.SEVERE, "Worker thread interrupted", e);
            }
        }
    }

    private void checkForQueue() {
        addServiceToQueueWhereProvided();
        if (minBootableServiceExists()) return;

        var services = this.base.getServiceManager().getAllServicesByState(ServiceState.PREPARED).stream()
            .filter(service -> service.getGroup().getNode().equalsIgnoreCase(this.base.getNode().getName()))
            .toList();
        if (!services.isEmpty()) {
            ((SimpleServiceManager) this.base.getServiceManager()).start(services.get(0));
        }
    }

    private void addServiceToQueueWhereProvided() {
        this.base.getGroupManager().getAllCachedServiceGroups().stream()
            .filter(serviceGroup -> serviceGroup.getNode().equalsIgnoreCase(this.base.getNode().getName()))
            .filter(serviceGroup -> getAmountOfGroupServices(serviceGroup) < serviceGroup.getMinOnlineService())
            .forEach(serviceGroup -> {
                var service = new LocalService(serviceGroup, getPossibleServiceIDByGroup(serviceGroup),
                    PortHandler.getNextPort(serviceGroup), this.base.getNode().getHostName());
                this.base.getServiceManager().getAllCachedServices().add(service);
                this.base.getNode().sendPacketToAll(new ServiceAddPacket(service));
                this.base.getLogger().log(String.format(
                    "§7The group '§b%s§7' is starting a new instance of '§b%s§7' (§6Prepared§7)",
                    serviceGroup.getName(),
                    service.getName()
                ));
            });
    }

    private boolean minBootableServiceExists() {
        return getAmountOfBootableServices() >= MAX_BOOTABLE_SERVICES;
    }

    private int getAmountOfBootableServices() {
        return this.base.getServiceManager().getAllServicesByState(ServiceState.STARTING).size();
    }

    private int getAmountOfGroupServices(ServiceGroup serviceGroup) {
        return (int) this.base.getServiceManager().getAllCachedServices().stream()
            .filter(service -> service.getGroup().equals(serviceGroup)).count();
    }

    public int getPossibleServiceIDByGroup(ServiceGroup serviceGroup) {
        int id = 1;
        while (isServiceIdAlreadyExists(serviceGroup, id)) id++;
        return id;
    }

    private boolean isServiceIdAlreadyExists(ServiceGroup serviceGroup, int id) {
        return this.base.getServiceManager().getAllServicesByGroup(serviceGroup).stream()
            .anyMatch(service -> id == service.getServiceId());
    }

    private void readStream(InputStream inputStream, Consumer<String> consumer) {
        try {
            int length;
            while (inputStream.available() > 0 && (length = inputStream.read(buffer)) != -1) {
                stringBuilder.append(new String(buffer, 0, length, StandardCharsets.UTF_8));
                processBuffer(consumer);
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error reading from stream", e);
        }
    }

    private void processBuffer(Consumer<String> consumer) {
        var content = stringBuilder.toString();
        if (content.contains("\n")) {
            for (var line : content.split("\n")) consumer.accept(line);
        }
        stringBuilder.setLength(0);
    }

    public void addRunnable(Runnable runnable) {
        tasks.add(runnable);
    }
}
