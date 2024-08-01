package de.cloud;

import de.cloud.api.CloudAPI;
import de.cloud.api.logger.LogType;
import de.cloud.routes.DefaultRoute;
import de.cloud.routes.handler.CustomExceptionHandler;
import spark.Spark;

import java.io.IOException;
import java.net.ServerSocket;

public class RestBootstrap {
    private static String PASSWORD = null;

    public RestBootstrap(String restPassword) {
        PASSWORD = restPassword;

        if (isPortAvailable()) {
            Spark.port(8080);
        } else {
            throw new RuntimeException("Port is not available");
        }
        Spark.before((req, res) -> {
            String password = req.headers("X-Password");
            if (password == null || !password.equals(PASSWORD)) {
                Spark.halt(401, "Unauthorized");
            }
        });

        Spark.get("/", new DefaultRoute());

        Spark.exception(Exception.class, new CustomExceptionHandler());

        CloudAPI.getInstance().getLogger().log("§aRestAPI loaded successfully.", LogType.INFO);
    }

    private boolean isPortAvailable() {
        try (var serverSocket = new ServerSocket(8080)) {
            return true;
        } catch (IOException e) {
            return false;
        }
    }
}
