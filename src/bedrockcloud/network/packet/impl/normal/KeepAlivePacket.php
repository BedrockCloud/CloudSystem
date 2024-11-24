<?php

namespace bedrockcloud\network\packet\impl\normal;

use bedrockcloud\network\client\ServerClient;
use bedrockcloud\network\packet\CloudPacket;

class KeepAlivePacket extends CloudPacket {

    public function handle(ServerClient $client): void {
        if (($server = $client->getServer()) !== null) {
            $server->setLastCheckTime(time());
            $server->sendPacket(new KeepAlivePacket());
        }
    }
}