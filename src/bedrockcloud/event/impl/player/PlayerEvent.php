<?php

namespace bedrockcloud\event\impl\player;

use bedrockcloud\event\Event;
use bedrockcloud\player\CloudPlayer;

abstract class PlayerEvent extends Event {

    public function __construct(private readonly CloudPlayer $player) {}

    public function getPlayer(): CloudPlayer {
        return $this->player;
    }
}