package de.cloud.routes.handler;

import spark.ExceptionHandler;
import spark.Request;
import spark.Response;

public class CustomExceptionHandler implements ExceptionHandler<Exception> {

    @Override
    public void handle(Exception e, Request request, Response response) {
        response.status(500);
        response.body("Internal Server Error: " + e.getMessage());
    }
}
