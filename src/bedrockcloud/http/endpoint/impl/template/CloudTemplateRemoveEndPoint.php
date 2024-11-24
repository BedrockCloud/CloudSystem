<?php

namespace bedrockcloud\http\endpoint\impl\template;

use bedrockcloud\http\io\Request;
use bedrockcloud\http\io\Response;
use bedrockcloud\http\util\Router;
use bedrockcloud\http\endpoint\EndPoint;
use bedrockcloud\template\TemplateManager;

class CloudTemplateRemoveEndPoint extends EndPoint {

    public function __construct() {
        parent::__construct(Router::DELETE, "/template/delete/");
    }

    public function handleRequest(Request $request, Response $response): array {
        $name = $request->data()->queries()->get("name");
        $template = TemplateManager::getInstance()->getTemplateByName($name);

        if ($template === null) {
            return ["error" => "The template doesn't exists!"];
        }

        TemplateManager::getInstance()->deleteTemplate($template);
        return ["success" => "The template was deleted!"];
    }

    public function isBadRequest(Request $request): bool {
        return !$request->data()->queries()->has("name");
    }
}