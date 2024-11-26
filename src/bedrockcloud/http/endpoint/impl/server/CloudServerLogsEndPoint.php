<?php

namespace bedrockcloud\http\endpoint\impl\server;

use bedrockcloud\http\io\Request;
use bedrockcloud\http\io\Response;
use bedrockcloud\http\util\Router;
use bedrockcloud\http\endpoint\EndPoint;
use bedrockcloud\server\CloudServerManager;

class CloudServerLogsEndPoint extends EndPoint {

    public function __construct() {
        parent::__construct(Router::GET, "/server/logs/");
    }

    public function handleRequest(Request $request, Response $response): array {
        $serverName = $request->data()->queries()->get("server");
        $logType = (int)$request->data()->queries()->get("type", 0);

        if ((CloudServerManager::getInstance()->getServerByName($serverName)) === null) {
            return ["error" => "Server not found"];
        }

        $logs = self::getServerLogs($serverName, $logType);
        if ($logs === null) {
            return ["error" => "Logs not found"];
        }

        return ["server" => $serverName, "logs" => $logs];
    }

    public static function getServerLogs(string $server, int $type = 0): ?array {
        $basePath = CLOUD_PATH . "/tmp/" . $server . "/";
        $logFile = $type === 0 ? "server.log" : "logs/server.log";

        if (file_exists($basePath . $logFile)) {
            return explode("\n", file_get_contents($basePath . $logFile));
        }

        return null;
    }

    public function isBadRequest(Request $request): bool {
        return !$request->data()->queries()->has("server");
    }
}