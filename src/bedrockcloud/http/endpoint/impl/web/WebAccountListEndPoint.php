<?php

namespace bedrockcloud\http\endpoint\impl\web;

use bedrockcloud\http\endpoint\EndPoint;
use bedrockcloud\http\io\Request;
use bedrockcloud\http\io\Response;
use bedrockcloud\http\util\Router;
use bedrockcloud\web\WebAccount;
use bedrockcloud\web\WebAccountManager;

class WebAccountListEndPoint extends EndPoint {

    public function __construct() {
        parent::__construct(Router::GET, "/webaccount/list/");
    }

    public function handleRequest(Request $request, Response $response): array {
        return array_map(fn(WebAccount $account) => $account->toArray(), array_values(WebAccountManager::getInstance()->getAccounts()));
    }

    public function isBadRequest(Request $request): bool {
        return false;
    }
}