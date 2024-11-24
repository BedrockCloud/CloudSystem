<?php

namespace bedrockcloud\event\impl\player;

use bedrockcloud\player\CloudPlayer;
use bedrockcloud\server\CloudServer;

class PlayerDisconnectEvent extends PlayerEvent {

    public function __construct(
        CloudPlayer $player,
        private readonly CloudServer $server
    ) {
        parent::__construct($player);
    }

    public function getServer(): CloudServer {
        return $this->server;
    }
}