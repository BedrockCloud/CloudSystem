<?php

namespace bedrockcloud\http\endpoint\impl\template;

use bedrockcloud\http\io\Request;
use bedrockcloud\http\io\Response;
use bedrockcloud\http\util\Router;
use bedrockcloud\http\endpoint\EndPoint;
use bedrockcloud\template\Template;
use bedrockcloud\template\TemplateManager;

class CloudTemplateListEndPoint extends EndPoint {

    public function __construct() {
        parent::__construct(Router::GET, "/template/list/");
    }

    public function handleRequest(Request $request, Response $response): array {
        return array_values(array_map(fn(Template $template) => $template->getName(), TemplateManager::getInstance()->getTemplates()));
    }

    public function isBadRequest(Request $request): bool {
        return false;
    }
}