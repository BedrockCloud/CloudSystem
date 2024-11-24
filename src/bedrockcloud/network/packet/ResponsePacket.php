<?php

namespace bedrockcloud\network\packet;

use bedrockcloud\network\client\ServerClient;
use bedrockcloud\network\packet\utils\PacketData;

abstract class ResponsePacket extends CloudPacket {

    private string $requestId = "";

    public function encode(PacketData $packetData): void {
        parent::encode($packetData);
        $packetData->write($this->requestId);
    }

    public function decode(PacketData $packetData): void {
        parent::decode($packetData);
        $this->requestId = $packetData->readString();
    }

    public function getRequestId(): string {
        return $this->requestId;
    }

    public function setRequestId(string $requestId): self {
        $this->requestId = $requestId;
        return $this;
    }

    final public function handle(ServerClient $client) {}
}