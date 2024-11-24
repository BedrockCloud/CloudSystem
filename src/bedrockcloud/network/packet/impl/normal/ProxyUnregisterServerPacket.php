<?php

namespace bedrockcloud\network\packet\impl\normal;

use bedrockcloud\network\client\ServerClient;
use bedrockcloud\network\packet\CloudPacket;
use bedrockcloud\network\packet\utils\PacketData;

class ProxyUnregisterServerPacket extends CloudPacket {

    public function __construct(private string $serverName = "") {}

    public function encodePayload(PacketData $packetData): void {
        $packetData->write($this->serverName);
    }

    public function decodePayload(PacketData $packetData): void {
        $this->serverName = $packetData->readString();
    }

    public function getServerName(): string {
        return $this->serverName;
    }

    public function handle(ServerClient $client): void {}
}