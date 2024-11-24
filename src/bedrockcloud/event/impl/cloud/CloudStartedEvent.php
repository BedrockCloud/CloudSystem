<?php

namespace bedrockcloud\event\impl\cloud;

use bedrockcloud\event\Event;

class CloudStartedEvent extends Event {

    public function __construct(private readonly float $time) {}

    public function getTime(): float {
        return $this->time;
    }
}