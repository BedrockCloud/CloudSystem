<?php

namespace bedrockcloud\http\endpoint\impl\plugin;

use bedrockcloud\http\io\Request;
use bedrockcloud\http\io\Response;
use bedrockcloud\http\util\Router;
use bedrockcloud\plugin\CloudPlugin;
use bedrockcloud\plugin\CloudPluginManager;
use bedrockcloud\http\endpoint\EndPoint;

class CloudPluginListEndPoint extends EndPoint {

    public function __construct() {
        parent::__construct(Router::GET, "/plugin/list/");
    }

    public function handleRequest(Request $request, Response $response): array {
        $loadedPlugins = array_map(fn(CloudPlugin $plugin) => $plugin->getDescription()->getName(), CloudPluginManager::getInstance()->getPlugins());
        $enabledPlugins = array_map(fn(CloudPlugin $plugin) => $plugin->getDescription()->getName(), CloudPluginManager::getInstance()->getEnabledPlugins());
        $disabledPlugins = array_filter($loadedPlugins, fn(string $name) => !in_array($name, $enabledPlugins));
        return [
            "loadedPlugins" => array_values($loadedPlugins),
            "enabledPlugins" => array_values($enabledPlugins),
            "disabledPlugins" => array_values($disabledPlugins)
        ];
    }

    public function isBadRequest(Request $request): bool {
        return false;
    }
}