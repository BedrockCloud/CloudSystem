<?php

namespace bedrockcloud\http\endpoint\impl\module;

use bedrockcloud\http\endpoint\EndPoint;
use bedrockcloud\http\io\Request;
use bedrockcloud\http\io\Response;
use bedrockcloud\http\util\Router;

class ModuleListEndPoint extends EndPoint {

    public function __construct() {
        parent::__construct(Router::GET, "/module/list/");
    }

    public function handleRequest(Request $request, Response $response): array {
        return ["signModule", "npcModule", "hubCommandModule"];
    }

    public function isBadRequest(Request $request): bool {
        return false;
    }
}