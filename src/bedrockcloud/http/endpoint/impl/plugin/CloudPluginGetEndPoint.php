<?php

namespace bedrockcloud\http\endpoint\impl\plugin;

use bedrockcloud\http\io\Request;
use bedrockcloud\http\io\Response;
use bedrockcloud\http\util\Router;
use bedrockcloud\plugin\CloudPluginManager;
use bedrockcloud\http\endpoint\EndPoint;

class CloudPluginGetEndPoint extends EndPoint {

    public function __construct() {
        parent::__construct(Router::GET, "/plugin/get");
    }

    public function handleRequest(Request $request, Response $response): array {
        $plugin = CloudPluginManager::getInstance()->getPluginByName($request->data()->queries()->get("plugin"));
        if ($plugin === null) {
            return ["error" => "Plugin wasn't found!"];
        }

        return array_merge($plugin->getDescription()->toArray(), ["enabled" => $plugin->isEnabled()]);
    }

    public function isBadRequest(Request $request): bool {
        return !$request->data()->queries()->has("plugin");
    }
}