<?php

namespace bedrockcloud\network\packet\impl\response;

use bedrockcloud\network\packet\ResponsePacket;
use bedrockcloud\network\packet\utils\PacketData;

class CheckPlayerExistsResponsePacket extends ResponsePacket {

    public function __construct(private bool $value = false) {}

    public function encodePayload(PacketData $packetData): void {
        $packetData->write($this->value);
    }

    public function decodePayload(PacketData $packetData): void {
        $this->value = $packetData->readBool();
    }

    public function getValue(): bool {
        return $this->value;
    }
}