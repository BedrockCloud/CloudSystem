package de.cloud.routes;

import spark.Request;
import spark.Response;
import spark.Route;

public class DefaultRoute implements Route {


    @Override
    public Object handle(Request request, Response response) throws Exception {
        return "BedrockCloud RestAPI";
    }
}
