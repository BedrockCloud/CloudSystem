<?php

namespace bedrockcloud\http\endpoint\impl\web;

use bedrockcloud\http\endpoint\EndPoint;
use bedrockcloud\http\io\Request;
use bedrockcloud\http\io\Response;
use bedrockcloud\http\util\Router;
use bedrockcloud\util\Utils;
use bedrockcloud\web\WebAccount;
use bedrockcloud\web\WebAccountManager;
use bedrockcloud\web\WebAccountRoles;

class WebAccountCreateEndPoint extends EndPoint {

    public function __construct() {
        parent::__construct(Router::POST, "/webaccount/create/");
    }

    public function handleRequest(Request $request, Response $response): array {
        $name = $request->data()->queries()->get("name");
        $role = WebAccountRoles::from($request->data()->queries()->get("role", "default")) ?? WebAccountRoles::DEFAULT;

        if (WebAccountManager::getInstance()->checkAccount($name)) {
            return ["error" => "A web account with that name already exists!"];
        }

        WebAccountManager::getInstance()->createAccount(new WebAccount(
            $name, password_hash($pw = Utils::generateString(6), PASSWORD_BCRYPT), true, $role
        ));

        return ["success" => "The web account was created!", "initial_password" => $pw];
    }

    public function isBadRequest(Request $request): bool {
        return !$request->data()->queries()->has("name");
    }
}