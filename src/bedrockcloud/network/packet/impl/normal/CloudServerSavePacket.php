<?php

namespace bedrockcloud\network\packet\impl\normal;

use bedrockcloud\network\client\ServerClient;
use bedrockcloud\network\packet\CloudPacket;
use bedrockcloud\server\CloudServerManager;

class CloudServerSavePacket extends CloudPacket {

    public function handle(ServerClient $client): void {
        if (($server = $client->getServer()) !== null) {
            CloudServerManager::getInstance()->saveServer($server);
        }
    }
}