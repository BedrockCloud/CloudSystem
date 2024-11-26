<?php

namespace bedrockcloud\http\endpoint;

use bedrockcloud\http\endpoint\impl\cloud\CloudInfoEndPoint;
use bedrockcloud\http\endpoint\impl\maintenance\MaintenanceAddEndPoint;
use bedrockcloud\http\endpoint\impl\maintenance\MaintenanceGetEndPoint;
use bedrockcloud\http\endpoint\impl\maintenance\MaintenanceListEndPoint;
use bedrockcloud\http\endpoint\impl\maintenance\MaintenanceRemoveEndPoint;
use bedrockcloud\http\endpoint\impl\module\ModuleEditEndPoint;
use bedrockcloud\http\endpoint\impl\module\ModuleGetEndPoint;
use bedrockcloud\http\endpoint\impl\module\ModuleListEndPoint;
use bedrockcloud\http\endpoint\impl\player\CloudPlayerGetEndPoint;
use bedrockcloud\http\endpoint\impl\player\CloudPlayerKickEndPoint;
use bedrockcloud\http\endpoint\impl\player\CloudPlayerListEndPoint;
use bedrockcloud\http\endpoint\impl\player\CloudPlayerTextEndPoint;
use bedrockcloud\http\endpoint\impl\plugin\CloudPluginDisableEndPoint;
use bedrockcloud\http\endpoint\impl\plugin\CloudPluginEnableEndPoint;
use bedrockcloud\http\endpoint\impl\plugin\CloudPluginGetEndPoint;
use bedrockcloud\http\endpoint\impl\plugin\CloudPluginListEndPoint;
use bedrockcloud\http\endpoint\impl\server\CloudServerExecuteEndPoint;
use bedrockcloud\http\endpoint\impl\server\CloudServerGetEndPoint;
use bedrockcloud\http\endpoint\impl\server\CloudServerListEndPoint;
use bedrockcloud\http\endpoint\impl\server\CloudServerLogsEndPoint;
use bedrockcloud\http\endpoint\impl\server\CloudServerSaveEndPoint;
use bedrockcloud\http\endpoint\impl\server\CloudServerStartEndPoint;
use bedrockcloud\http\endpoint\impl\server\CloudServerStopEndPoint;
use bedrockcloud\http\endpoint\impl\template\CloudTemplateCreateEndPoint;
use bedrockcloud\http\endpoint\impl\template\CloudTemplateEditEndPoint;
use bedrockcloud\http\endpoint\impl\template\CloudTemplateGetEndPoint;
use bedrockcloud\http\endpoint\impl\template\CloudTemplateListEndPoint;
use bedrockcloud\http\endpoint\impl\template\CloudTemplateRemoveEndPoint;
use bedrockcloud\http\io\Request;
use bedrockcloud\http\io\Response;
use bedrockcloud\http\util\Router;
use bedrockcloud\language\Language;
use bedrockcloud\util\CloudLogger;

final class EndpointRegistry {

    /** @var array<EndPoint> */
    private static array $endPoints = [];

    public static function registerDefaults(): void {
        $endPoints = [
            new CloudInfoEndPoint(),
            new CloudPlayerGetEndPoint(), new CloudPlayerTextEndPoint(), new CloudPlayerKickEndPoint(), new CloudPlayerListEndPoint(),
            new CloudPluginGetEndPoint(), new CloudPluginEnableEndPoint(), new CloudPluginDisableEndPoint(), new CloudPluginListEndPoint(),
            new CloudTemplateCreateEndPoint(), new CloudTemplateRemoveEndPoint(), new CloudTemplateGetEndPoint(), new CloudTemplateListEndPoint(), new CloudTemplateEditEndPoint(),
            new CloudServerStartEndPoint(), new CloudServerStopEndPoint(), new CloudServerSaveEndPoint(), new CloudServerExecuteEndPoint(), new CloudServerGetEndPoint(), new CloudServerListEndPoint(), new CloudServerLogsEndPoint(),
            new ModuleGetEndPoint(), new ModuleListEndPoint(), new ModuleEditEndPoint(),
            new MaintenanceAddEndPoint(), new MaintenanceRemoveEndPoint(), new MaintenanceGetEndPoint(), new MaintenanceListEndPoint()
        ];

        foreach ($endPoints as $endPoint) {
            self::addEndPoint($endPoint);
        }
    }

    public static function addEndPoint(EndPoint $endPoint): void {
        if (in_array(strtoupper($endPoint->getRequestMethod()), Request::SUPPORTED_REQUEST_METHODS)) {
            self::$endPoints[$endPoint->getPath()] = $endPoint;
            Router::getInstance()->{strtolower($endPoint->getRequestMethod())}($endPoint->getPath(), function(Request $request, Response $response) use ($endPoint): void {
                $response->contentType("application/json");
                if (!$request->authorized()) {
                    $response->code(401);
                    CloudLogger::get()->warn(Language::current()->translate("httpServer.request.unauthorized", $request->data()->address()->__toString()));
                    return;
                }

                if ($endPoint->isBadRequest($request)) {
                    $response->code(400);
                    return;
                }

                $response->body($endPoint->handleRequest($request, $response));
            });
        } else CloudLogger::get()->error(Language::current()->translate("httpServer.endPoint.add.failed", $endPoint->getPath(), implode("§8, §e", Request::SUPPORTED_REQUEST_METHODS)));
    }

    public static function removeEndPoint(EndPoint $endPoint): void {
        if (isset(self::$endPoints[$endPoint->getPath()])) unset(self::$endPoints[$endPoint->getPath()]);
    }

    public static function getEndPoint(string $path): ?EndPoint {
        return self::$endPoints[$path] ?? null;
    }
}