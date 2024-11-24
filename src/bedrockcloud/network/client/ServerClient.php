<?php

namespace bedrockcloud\network\client;

use bedrockcloud\network\Network;
use bedrockcloud\network\packet\CloudPacket;
use bedrockcloud\server\CloudServer;
use bedrockcloud\util\Address;

readonly class ServerClient {

    public function __construct(private Address $address) {}

    public function sendPacket(CloudPacket $packet): bool {
        return Network::getInstance()->sendPacket($packet, $this);
    }

    public function getAddress(): Address {
        return $this->address;
    }

    public function getServer(): ?CloudServer {
        return ServerClientManager::getInstance()->getServerOfClient($this);
    }
}