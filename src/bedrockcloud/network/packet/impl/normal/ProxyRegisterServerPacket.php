<?php

namespace bedrockcloud\network\packet\impl\normal;

use bedrockcloud\network\client\ServerClient;
use bedrockcloud\network\packet\CloudPacket;
use bedrockcloud\network\packet\utils\PacketData;

class ProxyRegisterServerPacket extends CloudPacket {

    public function __construct(
        private string $serverName = "",
        private int $port = 0
    ) {}

    public function encodePayload(PacketData $packetData): void {
        $packetData->write($this->serverName);
        $packetData->write($this->port);
    }

    public function decodePayload(PacketData $packetData): void {
        $this->serverName = $packetData->readString();
        $this->port = $packetData->readInt();
    }

    public function getServerName(): string {
        return $this->serverName;
    }

    public function getPort(): int {
        return $this->port;
    }

    public function handle(ServerClient $client): void {}
}