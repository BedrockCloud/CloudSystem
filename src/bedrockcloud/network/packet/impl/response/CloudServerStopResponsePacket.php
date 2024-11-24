<?php

namespace bedrockcloud\network\packet\impl\response;

use bedrockcloud\network\packet\impl\types\ErrorReason;
use bedrockcloud\network\packet\ResponsePacket;
use bedrockcloud\network\packet\utils\PacketData;

class CloudServerStopResponsePacket extends ResponsePacket {

    public function __construct(private ?ErrorReason $errorReason = null) {}

    public function encodePayload(PacketData $packetData): void {
        $packetData->writeErrorReason($this->errorReason);
    }

    public function decodePayload(PacketData $packetData): void {
        $this->errorReason = $packetData->readErrorReason();
    }

    public function getErrorReason(): ?ErrorReason {
        return $this->errorReason;
    }
}