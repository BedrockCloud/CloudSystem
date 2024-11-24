<?php

namespace bedrockcloud\event\impl\server;

use bedrockcloud\event\Event;
use bedrockcloud\server\CloudServer;

abstract class ServerEvent extends Event {

    public function __construct(private readonly CloudServer $server) {}

    public function getServer(): CloudServer {
        return $this->server;
    }
}