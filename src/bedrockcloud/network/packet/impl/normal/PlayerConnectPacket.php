<?php

namespace bedrockcloud\network\packet\impl\normal;

use bedrockcloud\network\client\ServerClient;
use bedrockcloud\network\packet\CloudPacket;
use bedrockcloud\network\packet\utils\PacketData;
use bedrockcloud\player\CloudPlayer;
use bedrockcloud\player\CloudPlayerManager;
use bedrockcloud\template\TemplateType;

class PlayerConnectPacket extends CloudPacket {

    public function __construct(private ?CloudPlayer $player = null) {}

    public function encodePayload(PacketData $packetData): void {
        $packetData->writePlayer($this->player);
    }

    public function decodePayload(PacketData $packetData): void {
        $this->player = $packetData->readPlayer();
    }

    public function getPlayer(): ?CloudPlayer{
        return $this->player;
    }

    public function handle(ServerClient $client): void {
        if (($server = $client->getServer()) !== null) {
            if (CloudPlayerManager::getInstance()->getPlayerByName($this->player->getName()) === null) {
                if ($server->getTemplate()->getTemplateType() === TemplateType::SERVER()) $this->player->setCurrentServer($server);
                else $this->player->setCurrentProxy($server);
                CloudPlayerManager::getInstance()->addPlayer($this->player);
            }
        }
    }
}