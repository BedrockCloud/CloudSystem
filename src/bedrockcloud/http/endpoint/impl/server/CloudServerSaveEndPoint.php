<?php

namespace bedrockcloud\http\endpoint\impl\server;

use bedrockcloud\http\io\Request;
use bedrockcloud\http\io\Response;
use bedrockcloud\http\util\Router;
use bedrockcloud\http\endpoint\EndPoint;
use bedrockcloud\server\CloudServerManager;

class CloudServerSaveEndPoint extends EndPoint {

    public function __construct() {
        parent::__construct(Router::POST, "/server/save/");
    }

    public function handleRequest(Request $request, Response $response): array {
        $name = $request->data()->queries()->get("server");
        $server = CloudServerManager::getInstance()->getServerByName($name);

        if ($server === null) {
            return ["error" => "The server doesn't exists!"];
        }

        CloudServerManager::getInstance()->saveServer($server);
        return ["success" => "The cloud is successfully trying to save the given server!"];
    }

    public function isBadRequest(Request $request): bool {
        return !$request->data()->queries()->has("server");
    }
}