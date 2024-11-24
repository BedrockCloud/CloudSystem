<?php

namespace bedrockcloud\network\packet\impl\normal;

use bedrockcloud\library\Library;
use bedrockcloud\library\LibraryManager;
use bedrockcloud\network\client\ServerClient;
use bedrockcloud\network\packet\CloudPacket;
use bedrockcloud\network\packet\utils\PacketData;

class LibrarySyncPacket extends CloudPacket {

    private array $data = [];

    public function __construct() {
        foreach (array_filter(LibraryManager::getInstance()->getLibraries(), fn(Library $library) => $library->isCloudBridgeOnly()) as $lib) {
            $this->data[] = [
                "name" => $lib->getName(),
                "path" => $lib->getUnzipLocation()
            ];
        }
    }

    public function encodePayload(PacketData $packetData): void {
        $packetData->write($this->data);
    }

    public function decodePayload(PacketData $packetData): void {
        $this->data = $packetData->readArray();
    }

    public function getData(): array {
        return $this->data;
    }

    public function handle(ServerClient $client): void {}
}