<?php

namespace bedrockcloud\http\endpoint\impl\player;

use bedrockcloud\http\io\Request;
use bedrockcloud\http\io\Response;
use bedrockcloud\http\util\Router;
use bedrockcloud\player\CloudPlayerManager;
use bedrockcloud\http\endpoint\EndPoint;

class CloudPlayerGetEndPoint extends EndPoint {

    public function __construct() {
        parent::__construct(Router::GET, "/player/get/");
    }

    public function handleRequest(Request $request, Response $response): array {
        $identifier = $request->data()->queries()->get("identifier");
        $player = CloudPlayerManager::getInstance()->getPlayerByName($identifier) ?? CloudPlayerManager::getInstance()->getPlayerByUniqueId($identifier) ?? CloudPlayerManager::getInstance()->getPlayerByXboxUserId($identifier);
        if ($player === null) {
            return ["error" => "Player is not online!"];
        }

        return $player->toArray();
    }

    public function isBadRequest(Request $request): bool {
        return !$request->data()->queries()->has("identifier");
    }
}