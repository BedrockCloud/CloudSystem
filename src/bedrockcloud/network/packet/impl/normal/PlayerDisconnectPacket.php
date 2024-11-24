<?php

namespace bedrockcloud\network\packet\impl\normal;

use bedrockcloud\network\client\ServerClient;
use bedrockcloud\network\client\ServerClientManager;
use bedrockcloud\network\packet\CloudPacket;
use bedrockcloud\network\packet\utils\PacketData;
use bedrockcloud\player\CloudPlayerManager;
use bedrockcloud\template\TemplateType;

class PlayerDisconnectPacket extends CloudPacket {

    public function __construct(private ?string $playerName = "") {}

    public function encodePayload(PacketData $packetData): void {
        $packetData->write($this->playerName);
    }

    public function decodePayload(PacketData $packetData): void {
        $this->playerName = $packetData->readString();
    }

    public function getPlayer(): string {
        return $this->playerName;
    }

    public function handle(ServerClient $client): void {
        if (($player = CloudPlayerManager::getInstance()->getPlayerByName($this->playerName)) !== null) {
            if ($player->getCurrentProxy() === null) {
                CloudPlayerManager::getInstance()->removePlayer($player);
            } else {
                if (($server = ServerClientManager::getInstance()->getServerOfClient($client)) !== null) {
                    if ($server->getTemplate()->getTemplateType() === TemplateType::PROXY()) {
                        CloudPlayerManager::getInstance()->removePlayer($player);
                    }
                }
            }
        }
    }
}