<?php

namespace bedrockcloud\http\endpoint\impl\cloud;

use bedrockcloud\http\io\Request;
use bedrockcloud\http\io\Response;
use bedrockcloud\http\util\Router;
use bedrockcloud\network\Network;
use bedrockcloud\player\CloudPlayer;
use bedrockcloud\player\CloudPlayerManager;
use bedrockcloud\plugin\CloudPlugin;
use bedrockcloud\plugin\CloudPluginManager;
use bedrockcloud\http\endpoint\EndPoint;
use bedrockcloud\server\CloudServer;
use bedrockcloud\server\CloudServerManager;
use bedrockcloud\template\Template;
use bedrockcloud\template\TemplateManager;
use bedrockcloud\util\VersionInfo;

class CloudInfoEndPoint extends EndPoint {

    public function __construct() {
        parent::__construct(Router::GET, "/cloud/info/");
    }

    public function handleRequest(Request $request, Response $response): array {
        $templates = array_map(fn(Template $template) => $template->getName(), TemplateManager::getInstance()->getTemplates());
        $runningServers = array_map(fn(CloudServer $cloudServer) => $cloudServer->getName(), CloudServerManager::getInstance()->getServers());
        $loadedPlugins = array_map(fn(CloudPlugin $plugin) => $plugin->getDescription()->getName(), CloudPluginManager::getInstance()->getPlugins());
        $enabledPlugins = array_map(fn(CloudPlugin $plugin) => $plugin->getDescription()->getName(), CloudPluginManager::getInstance()->getEnabledPlugins());
        $disabledPlugins = array_filter($loadedPlugins, fn(string $name) => !in_array($name, $enabledPlugins));
        $players = array_map(fn(CloudPlayer $player) => $player->getName(), CloudPlayerManager::getInstance()->getPlayers());

        return [
            "version" => VersionInfo::VERSION,
            "developer" => VersionInfo::DEVELOPERS,
            "templates" => array_values($templates),
            "runningServers" => array_values($runningServers),
            "players" => array_values($players),
            "loadedPlugins" => array_values($loadedPlugins),
            "enabledPlugins" => array_values($enabledPlugins),
            "disabledPlugins" => array_values($disabledPlugins),
            "network_address" => Network::getInstance()->getAddress()->__toString()
        ];
    }

    public function isBadRequest(Request $request): bool {
        return false;
    }
}