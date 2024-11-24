<?php

namespace bedrockcloud\event\impl\network;

use bedrockcloud\network\client\ServerClient;
use bedrockcloud\network\packet\CloudPacket;

class NetworkPacketSendEvent extends NetworkEvent {

    public function __construct(
        CloudPacket $packet,
        ServerClient $client,
        private readonly bool $success
    ) {
        parent::__construct($packet, $client);
    }

    public function isSuccess(): bool {
        return $this->success;
    }
}