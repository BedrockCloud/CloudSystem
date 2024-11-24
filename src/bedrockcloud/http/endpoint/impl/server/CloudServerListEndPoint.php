<?php

namespace bedrockcloud\http\endpoint\impl\server;

use bedrockcloud\http\io\Request;
use bedrockcloud\http\io\Response;
use bedrockcloud\http\util\Router;
use bedrockcloud\http\endpoint\EndPoint;
use bedrockcloud\server\CloudServer;
use bedrockcloud\server\CloudServerManager;
use bedrockcloud\template\TemplateManager;

class CloudServerListEndPoint extends EndPoint {

    public function __construct() {
        parent::__construct(Router::GET, "/server/list/");
    }

    public function handleRequest(Request $request, Response $response): array {
        $template = $request->data()->queries()->get("template");

        if ($template === null) {
            return array_values(array_map(fn(CloudServer $cloudServer) => $cloudServer->getName(), CloudServerManager::getInstance()->getServers()));
        } else {
            if (($template = TemplateManager::getInstance()->getTemplateByName($template)) !== null) {
                return array_values(array_map(fn(CloudServer $cloudServer) => $cloudServer->getName(), CloudServerManager::getInstance()->getServersByTemplate($template)));
            } else {
                return ["error" => "The template doesn't exists!"];
            }
        }
    }

    public function isBadRequest(Request $request): bool {
        return false;
    }
}