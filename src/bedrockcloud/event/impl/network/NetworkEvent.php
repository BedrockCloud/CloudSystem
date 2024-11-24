<?php

namespace bedrockcloud\event\impl\network;

use bedrockcloud\event\Event;
use bedrockcloud\network\client\ServerClient;
use bedrockcloud\network\packet\CloudPacket;

abstract class NetworkEvent extends Event {

    public function __construct(
        private readonly CloudPacket $packet,
        private readonly ServerClient $client
    ) {}

    public function getPacket(): CloudPacket {
        return $this->packet;
    }

    public function getClient(): ServerClient {
        return $this->client;
    }
}