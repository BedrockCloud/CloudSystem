<?php

namespace bedrockcloud\http\endpoint\impl\module;

use bedrockcloud\config\impl\ModuleConfig;
use bedrockcloud\http\endpoint\EndPoint;
use bedrockcloud\http\io\Request;
use bedrockcloud\http\io\Response;
use bedrockcloud\http\util\Router;

class ModuleGetEndPoint extends EndPoint {

    public function __construct() {
        parent::__construct(Router::GET, "/module/get/");
    }

    public function handleRequest(Request $request, Response $response): array {
        $module = strtolower($request->data()->queries()->get("module"));

        if (in_array($module, ["sign", "signmodule", "cloudsigns"])) {
            return ["module" => "signModule", "enabled" => ModuleConfig::getInstance()->isSignModule()];
        } else if (in_array($module, ["npc", "npcmodule", "cloudnpcs"])) {
            return ["module" => "npcModule", "enabled" => ModuleConfig::getInstance()->isNpcModule()];
        } else if (in_array($module, ["hub", "hubcommand", "hubcommandmodule"])) {
            return ["module" => "hubCommandModule", "enabled" => ModuleConfig::getInstance()->isHubCommandModule()];
        }

        return ["error" => "The module doesn't exists!"];
    }

    public function isBadRequest(Request $request): bool {
        return !$request->data()->queries()->has("module");
    }
}