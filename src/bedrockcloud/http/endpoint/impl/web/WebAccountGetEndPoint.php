<?php

namespace bedrockcloud\http\endpoint\impl\web;

use bedrockcloud\http\endpoint\EndPoint;
use bedrockcloud\http\io\Request;
use bedrockcloud\http\io\Response;
use bedrockcloud\http\util\Router;
use bedrockcloud\web\WebAccountManager;

class WebAccountGetEndPoint extends EndPoint {

    public function __construct() {
        parent::__construct(Router::GET, "/webaccount/get/");
    }

    public function handleRequest(Request $request, Response $response): array {
        $name = $request->data()->queries()->get("name");

        if (($account = WebAccountManager::getInstance()->getAccount($name)) === null) {
            return ["error" => "A web account with that name doesn't exists!"];
        }

        return $account->toArray();
    }

    public function isBadRequest(Request $request): bool {
        return !$request->data()->queries()->has("name");
    }
}