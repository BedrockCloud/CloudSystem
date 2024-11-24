<?php

namespace bedrockcloud\network\packet\impl\normal;

use bedrockcloud\network\client\ServerClient;
use bedrockcloud\network\packet\CloudPacket;
use bedrockcloud\network\packet\utils\PacketData;
use bedrockcloud\server\status\ServerStatus;

class CloudServerStatusChangePacket extends CloudPacket {

    public function __construct(private ?ServerStatus $newStatus = null) {}

    public function encodePayload(PacketData $packetData): void {
        $packetData->writeServerStatus($this->newStatus);
    }

    public function decodePayload(PacketData $packetData): void {
        $this->newStatus = $packetData->readServerStatus();
    }

    public function getNewStatus(): ?ServerStatus {
        return $this->newStatus;
    }

    public function handle(ServerClient $client): void {
        if (($server = $client->getServer()) !== null) {
            $server->setServerStatus($this->newStatus);
        }
    }
}