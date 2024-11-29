<?php

namespace bedrockcloud\network\packet\impl\request;

use bedrockcloud\config\impl\MaintenanceList;
use bedrockcloud\network\client\ServerClient;
use bedrockcloud\network\packet\impl\response\CheckPlayerExistsResponsePacket;
use bedrockcloud\network\packet\RequestPacket;
use bedrockcloud\network\packet\utils\PacketData;
use bedrockcloud\player\CloudPlayer;
use bedrockcloud\player\CloudPlayerManager;

class CheckPlayerExistsRequestPacket extends RequestPacket {

    public function __construct(private string $player = "") {}

    public function encodePayload(PacketData $packetData): void {
        $packetData->write($this->player);
    }

    public function decodePayload(PacketData $packetData): void {
        $this->player = $packetData->readString();
    }

    public function getPlayer(): string {
        return $this->player;
    }

    public function handle(ServerClient $client): void {
        $isPlayer = (CloudPlayerManager::getInstance()->getPlayerByName($this->player) != null);
        $this->sendResponse(new CheckPlayerExistsResponsePacket($isPlayer), $client);
    }
}