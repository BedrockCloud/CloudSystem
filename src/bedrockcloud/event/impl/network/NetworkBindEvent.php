<?php

namespace bedrockcloud\event\impl\network;

use bedrockcloud\event\Event;
use bedrockcloud\util\Address;

class NetworkBindEvent extends Event {

    public function __construct(private readonly Address $address) {}

    public function getAddress(): Address {
        return $this->address;
    }
}