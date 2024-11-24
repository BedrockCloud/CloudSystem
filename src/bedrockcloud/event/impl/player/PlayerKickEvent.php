<?php

namespace bedrockcloud\event\impl\player;

use bedrockcloud\event\Cancelable;
use bedrockcloud\event\CancelableTrait;
use bedrockcloud\player\CloudPlayer;

class PlayerKickEvent extends PlayerEvent implements Cancelable {
    use CancelableTrait;

    public function __construct(
        CloudPlayer $player,
        private readonly string $reason
    ) {
        parent::__construct($player);
    }

    public function getReason(): string {
        return $this->reason;
    }
}