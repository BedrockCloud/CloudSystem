<?php

namespace bedrockcloud\network\packet\impl\normal;

use bedrockcloud\network\client\ServerClient;
use bedrockcloud\network\packet\CloudPacket;
use bedrockcloud\network\packet\utils\PacketData;
use bedrockcloud\server\CloudServer;

class ServerSyncPacket extends CloudPacket {

    public function __construct(
        private ?CloudServer $server = null,
        private bool $removal = false
    ) {}

    public function encodePayload(PacketData $packetData): void {
        $packetData->writeServer($this->server);
        $packetData->write($this->removal);
    }

    public function decodePayload(PacketData $packetData): void {
        $this->server = $packetData->readServer();
        $this->removal = $packetData->readBool();
    }

    public function getServer(): ?CloudServer {
        return $this->server;
    }

    public function isRemoval(): bool {
        return $this->removal;
    }

    public function handle(ServerClient $client): void {}
}