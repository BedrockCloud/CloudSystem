<?php

namespace bedrockcloud\http\endpoint\impl\plugin;

use bedrockcloud\http\io\Request;
use bedrockcloud\http\io\Response;
use bedrockcloud\http\endpoint\EndPoint;
use bedrockcloud\http\util\Router;
use bedrockcloud\plugin\CloudPluginManager;

class CloudPluginEnableEndPoint extends EndPoint {

    public function __construct() {
        parent::__construct(Router::POST, "/plugin/enable/");
    }

    public function handleRequest(Request $request, Response $response): array {
        $plugin = CloudPluginManager::getInstance()->getPluginByName($request->data()->queries()->get("plugin"));
        if ($plugin === null) {
            return ["error" => "Plugin wasn't found!"];
        }

        if ($plugin->isEnabled()) {
            return ["error" => "Plugin is already enabled!"];
        }

        CloudPluginManager::getInstance()->enablePlugin($plugin);
        return ["success" => "Plugin was enabled!"];
    }

    public function isBadRequest(Request $request): bool {
        return !$request->data()->queries()->has("plugin");
    }
}