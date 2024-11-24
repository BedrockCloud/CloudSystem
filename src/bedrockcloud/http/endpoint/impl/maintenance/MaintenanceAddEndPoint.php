<?php

namespace bedrockcloud\http\endpoint\impl\maintenance;

use bedrockcloud\config\impl\MaintenanceList;
use bedrockcloud\http\endpoint\EndPoint;
use bedrockcloud\http\io\Request;
use bedrockcloud\http\io\Response;
use bedrockcloud\http\util\Router;

class MaintenanceAddEndPoint extends EndPoint {

    public function __construct() {
        parent::__construct(Router::POST, "/maintenance/add/");
    }

    public function handleRequest(Request $request, Response $response): array {
        $player = $request->data()->queries()->get("player");

        if (MaintenanceList::is($player)) {
            return ["error" => "The player is already on the maintenance list!"];
        }

        MaintenanceList::add($player);
        return ["success" => "The player was added to the maintenance list!"];
    }

    public function isBadRequest(Request $request): bool {
        return !$request->data()->queries()->has("player");
    }
}