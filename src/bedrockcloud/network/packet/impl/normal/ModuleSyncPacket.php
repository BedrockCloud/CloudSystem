<?php

namespace bedrockcloud\network\packet\impl\normal;

use bedrockcloud\config\impl\ModuleConfig;
use bedrockcloud\network\client\ServerClient;
use bedrockcloud\network\packet\CloudPacket;
use bedrockcloud\network\packet\utils\PacketData;

class ModuleSyncPacket extends CloudPacket {

    private array $data;

    public function __construct() {
        $this->data = [
            "sign" => ["enabled" => ModuleConfig::getInstance()->isSignModule()],
            "npc" => ["enabled" => ModuleConfig::getInstance()->isNpcModule()],
            "hub_command" => ["enabled" => ModuleConfig::getInstance()->isHubCommandModule()],
        ];
    }

    public function encodePayload(PacketData $packetData): void {
        $packetData->write($this->data);
    }

    public function decodePayload(PacketData $packetData): void {
        $this->data = $packetData->readArray();
    }

    public function getData(): array {
        return $this->data;
    }

    public function handle(ServerClient $client): void {}
}