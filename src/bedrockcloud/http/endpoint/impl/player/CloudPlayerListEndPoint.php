<?php

namespace bedrockcloud\http\endpoint\impl\player;

use bedrockcloud\http\io\Request;
use bedrockcloud\http\io\Response;
use bedrockcloud\http\util\Router;
use bedrockcloud\player\CloudPlayer;
use bedrockcloud\player\CloudPlayerManager;
use bedrockcloud\http\endpoint\EndPoint;

class CloudPlayerListEndPoint extends EndPoint {

    public function __construct() {
        parent::__construct(Router::GET, "/player/list/");
    }

    public function handleRequest(Request $request, Response $response): array {
        return array_values(array_map(fn(CloudPlayer $player) => $player->getName(), CloudPlayerManager::getInstance()->getPlayers()));
    }

    public function isBadRequest(Request $request): bool {
        return false;
    }
}