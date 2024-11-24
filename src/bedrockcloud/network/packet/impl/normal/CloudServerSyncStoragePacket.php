<?php

namespace bedrockcloud\network\packet\impl\normal;

use bedrockcloud\network\client\ServerClient;
use bedrockcloud\network\Network;
use bedrockcloud\network\packet\CloudPacket;
use bedrockcloud\network\packet\utils\PacketData;

//coming from sub server
class CloudServerSyncStoragePacket extends CloudPacket {

    public function __construct(private array $data = []) {}

    public function encodePayload(PacketData $packetData): void {
        $packetData->write($this->data);
    }

    public function decodePayload(PacketData $packetData): void {
        $this->data = $packetData->readArray();
    }

    public function getData(): array {
        return $this->data;
    }

    public function handle(ServerClient $client): void {
        $client->getServer()?->getCloudServerStorage()->sync($this->data);
        Network::getInstance()->broadcastPacket(new CloudSyncStoragesPacket());
    }
}