<?php

namespace bedrockcloud\network\packet\impl\request;

use bedrockcloud\language\Language;
use bedrockcloud\network\client\ServerClient;
use bedrockcloud\network\client\ServerClientManager;
use bedrockcloud\network\Network;
use bedrockcloud\network\packet\impl\normal\KeepAlivePacket;
use bedrockcloud\network\packet\impl\normal\ServerSyncPacket;
use bedrockcloud\network\packet\impl\response\LoginResponsePacket;
use bedrockcloud\network\packet\impl\types\VerifyStatus;
use bedrockcloud\network\packet\RequestPacket;
use bedrockcloud\server\CloudServerManager;
use bedrockcloud\server\status\ServerStatus;
use bedrockcloud\util\CloudLogger;
use bedrockcloud\network\packet\utils\PacketData;

class LoginRequestPacket extends RequestPacket {

    public function __construct(
        private string $serverName = "",
        private int $processId = 0,
        private int $maxPlayers = 0
    ) {}

    public function encodePayload(PacketData $packetData): void {
        $packetData->write($this->serverName);
        $packetData->write($this->processId);
        $packetData->write($this->maxPlayers);
    }

    public function decodePayload(PacketData $packetData): void {
        $this->serverName = $packetData->readString();
        $this->processId = $packetData->readInt();
        $this->maxPlayers = $packetData->readInt();
    }

    public function handle(ServerClient $client): void {
        if (($server = CloudServerManager::getInstance()->getServerByName($this->serverName)) !== null) {
            ServerClientManager::getInstance()->addClient($server, $client);
            CloudLogger::get()->info(Language::current()->translate("server.started", $server->getName()));
            $server->getCloudServerData()->setMaxPlayers($this->maxPlayers);
            $server->getCloudServerData()->setProcessId($this->processId);
            $server->setVerifyStatus(VerifyStatus::VERIFIED());
            $this->sendResponse(new LoginResponsePacket(VerifyStatus::VERIFIED()), $client);
            Network::getInstance()->broadcastPacket(new ServerSyncPacket($server), $client);
            $server->sync();
            $server->setServerStatus(ServerStatus::ONLINE());
            $server->sendPacket(new KeepAlivePacket());
        } else $this->sendResponse(new LoginResponsePacket(VerifyStatus::DENIED()), $client);
    }
}