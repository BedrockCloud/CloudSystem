<?php

namespace bedrockcloud\network\packet\impl\normal;

use bedrockcloud\network\client\ServerClient;
use bedrockcloud\network\packet\CloudPacket;
use bedrockcloud\network\packet\impl\types\CommandExecutionResult;
use bedrockcloud\network\packet\utils\PacketData;
use bedrockcloud\promise\Promise;

class CommandSendAnswerPacket extends CloudPacket {

    public function __construct(private ?CommandExecutionResult $result = null) {}

    public function encodePayload(PacketData $packetData): void {
        $packetData->writeCommandExecutionResult($this->result);
    }

    public function decodePayload(PacketData $packetData): void {
        $this->result = $packetData->readCommandExecutionResult();
    }

    public function getResult(): ?CommandExecutionResult {
        return $this->result;
    }

    public function handle(ServerClient $client): void {
        if (($server = $client->getServer()) !== null) {
            $promise = $server->getCloudServerStorage()->get("command_promise");
            if ($promise instanceof Promise) {
                $promise->resolve($this->result);
            }
        }
    }
}