<?php

namespace bedrockcloud\http\endpoint\impl\maintenance;

use bedrockcloud\config\impl\MaintenanceList;
use bedrockcloud\http\endpoint\EndPoint;
use bedrockcloud\http\io\Request;
use bedrockcloud\http\io\Response;
use bedrockcloud\http\util\Router;

class MaintenanceListEndPoint extends EndPoint {

    public function __construct() {
        parent::__construct(Router::GET, "/maintenance/list/");
    }

    public function handleRequest(Request $request, Response $response): array {
        return MaintenanceList::all();
    }

    public function isBadRequest(Request $request): bool {
        return false;
    }
}