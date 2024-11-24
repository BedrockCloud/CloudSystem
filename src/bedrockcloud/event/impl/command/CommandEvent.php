<?php

namespace bedrockcloud\event\impl\command;

use bedrockcloud\command\Command;
use bedrockcloud\event\Event;

abstract class CommandEvent extends Event {

    public function __construct(private readonly Command $command) {}

    public function getCommand(): Command {
        return $this->command;
    }
}