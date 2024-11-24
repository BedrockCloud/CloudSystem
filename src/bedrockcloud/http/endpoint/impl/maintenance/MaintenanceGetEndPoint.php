<?php

namespace bedrockcloud\http\endpoint\impl\maintenance;

use bedrockcloud\config\impl\MaintenanceList;
use bedrockcloud\http\endpoint\EndPoint;
use bedrockcloud\http\io\Request;
use bedrockcloud\http\io\Response;
use bedrockcloud\http\util\Router;

class MaintenanceGetEndPoint extends EndPoint {

    public function __construct() {
        parent::__construct(Router::GET, "/maintenance/get/");
    }

    public function handleRequest(Request $request, Response $response): array {
        $player = $request->data()->queries()->get("player");
        return ["player" => $player, "status" => MaintenanceList::is($player)];
    }

    public function isBadRequest(Request $request): bool {
        return !$request->data()->queries()->has("player");
    }
}