<?php

namespace bedrockcloud\network\packet\impl\normal;

use bedrockcloud\event\impl\server\ServerCrashEvent;
use bedrockcloud\event\impl\server\ServerDisconnectEvent;
use bedrockcloud\language\Language;
use bedrockcloud\network\client\ServerClient;
use bedrockcloud\network\client\ServerClientManager;
use bedrockcloud\network\packet\CloudPacket;
use bedrockcloud\network\packet\impl\types\DisconnectReason;
use bedrockcloud\network\packet\utils\PacketData;
use bedrockcloud\server\CloudServer;
use bedrockcloud\server\CloudServerManager;
use bedrockcloud\server\crash\CrashChecker;
use bedrockcloud\server\status\ServerStatus;
use bedrockcloud\util\CloudLogger;
use bedrockcloud\util\Utils;

class DisconnectPacket extends CloudPacket {

    public function __construct(private ?DisconnectReason $disconnectReason = null) {}

    public function encodePayload(PacketData $packetData): void {
        $packetData->writeDisconnectReason($this->disconnectReason);
    }

    public function decodePayload(PacketData $packetData): void {
        $this->disconnectReason = $packetData->readDisconnectReason();
    }

    public function handle(ServerClient $client): void {
        if (($server = $client->getServer()) !== null) {
            if ($server->getServerStatus() === ServerStatus::OFFLINE()) {
                if (isset(CloudServerManager::getInstance()->getServers()[$server->getName()])) CloudServerManager::getInstance()->removeServer($server);
                return;
            }

            $server->setServerStatus(ServerStatus::OFFLINE());
            (new ServerDisconnectEvent($server))->call();
            if (CrashChecker::checkCrashed($server, $crashData)) {
                (new ServerCrashEvent($server, $crashData))->call();
                CloudLogger::get()->info(Language::current()->translate("server.crashed", $server->getName()));
                CloudServerManager::getInstance()->printServerStackTrace($server->getName(), $crashData);
                CrashChecker::writeCrashFile($server, $crashData);
            } else {
                CloudLogger::get()->info(Language::current()->translate("server.stopped", $server->getName()));
            }

            if ($server->getCloudServerData()->getProcessId() !== 0) Utils::kill($server->getCloudServerData()->getProcessId());

            ServerClientManager::getInstance()->removeClient($server);
            CloudServerManager::getInstance()->removeServer($server);
            if (!$server->getTemplate()->getSettings()->isStatic()) Utils::deleteDir($server->getPath());
        }
    }

    public function getDisconnectReason(): ?DisconnectReason {
        return $this->disconnectReason;
    }
}