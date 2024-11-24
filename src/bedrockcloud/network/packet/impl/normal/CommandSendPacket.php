<?php

namespace bedrockcloud\network\packet\impl\normal;

use bedrockcloud\network\client\ServerClient;
use bedrockcloud\network\packet\CloudPacket;
use bedrockcloud\network\packet\utils\PacketData;

class CommandSendPacket extends CloudPacket {

    public function __construct(private string $commandLine = "") {}

    public function encodePayload(PacketData $packetData): void {
        $packetData->write($this->commandLine);
    }

    public function decodePayload(PacketData $packetData): void {
        $this->commandLine = $packetData->readString();
    }

    public function getCommandLine(): string {
        return $this->commandLine;
    }

    public function handle(ServerClient $client): void {}
}