<?php

namespace bedrockcloud\http\endpoint\impl\module;

use bedrockcloud\config\impl\ModuleConfig;
use bedrockcloud\http\endpoint\EndPoint;
use bedrockcloud\http\io\Request;
use bedrockcloud\http\io\Response;
use bedrockcloud\http\util\Router;
use bedrockcloud\network\packet\impl\normal\ModuleSyncPacket;
use bedrockcloud\server\CloudServerManager;
use bedrockcloud\template\TemplateType;

class ModuleEditEndPoint extends EndPoint {

    public function __construct() {
        parent::__construct(Router::PATCH, "/module/edit/");
    }

    public function handleRequest(Request $request, Response $response): array {
        $module = strtolower($request->data()->queries()->get("module"));
        $value = strtolower($request->data()->queries()->get("value")) == "true";

        if (in_array($module, ["sign", "signmodule", "cloudsigns"])) {
            ModuleConfig::getInstance()->setSignModule($value);
            ModuleConfig::getInstance()->save();
            $this->sync();
            return ["success" => "The module state has been changed!"];
        } else if (in_array($module, ["npc", "npcmodule", "cloudnpcs"])) {
            ModuleConfig::getInstance()->setNpcModule($value);
            ModuleConfig::getInstance()->save();
            $this->sync();
            return ["success" => "The module state has been changed!"];
        } else if (in_array($module, ["hub", "hubcommand", "hubcommandmodule"])) {
            ModuleConfig::getInstance()->setHubCommandModule($value);
            ModuleConfig::getInstance()->save();
            $this->sync();
            return ["success" => "The module state has been changed!"];
        }

        return ["error" => "The module doesn't exists!"];
    }

    public function isBadRequest(Request $request): bool {
        if ($request->data()->queries()->has("module") && $request->data()->queries()->has("value")) return false;
        return true;
    }

    private function sync(): void {
        foreach (CloudServerManager::getInstance()->getServers() as $server) {
            if ($server->getTemplate()->getTemplateType() === TemplateType::SERVER()) {
                $server->sendPacket(new ModuleSyncPacket());
            }
        }
    }
}