<?php

namespace bedrockcloud\network\packet\impl\request;

use bedrockcloud\network\client\ServerClient;
use bedrockcloud\network\packet\impl\response\CloudServerStopResponsePacket;
use bedrockcloud\network\packet\impl\types\ErrorReason;
use bedrockcloud\network\packet\RequestPacket;
use bedrockcloud\server\CloudServerManager;
use bedrockcloud\template\TemplateManager;
use bedrockcloud\network\packet\utils\PacketData;

class CloudServerStopRequestPacket extends RequestPacket {

    public function __construct(private string $server = "") {}

    public function encodePayload(PacketData $packetData): void {
        $packetData->write($this->server);
    }

    public function decodePayload(PacketData $packetData): void {
        $this->server = $packetData->readString();
    }

    public function getServer(): string {
        return $this->server;
    }

    public function handle(ServerClient $client): void {
        if (($server = CloudServerManager::getInstance()->getServerByName($this->server)) !== null) {
            CloudServerManager::getInstance()->stopServer($server);
            $this->sendResponse(new CloudServerStopResponsePacket(ErrorReason::NO_ERROR()), $client);
        } else if (($template = TemplateManager::getInstance()->getTemplateByName($this->server)) !== null) {
            CloudServerManager::getInstance()->stopTemplate($template);
            $this->sendResponse(new CloudServerStopResponsePacket(ErrorReason::NO_ERROR()), $client);
        } else if ($this->server == "all") {
            CloudServerManager::getInstance()->stopAll();
            $this->sendResponse(new CloudServerStopResponsePacket(ErrorReason::NO_ERROR()), $client);
        } else $this->sendResponse(new CloudServerStopResponsePacket(ErrorReason::SERVER_EXISTENCE()), $client);
    }
}