<?php

namespace bedrockcloud\event\impl\command;

use bedrockcloud\command\Command;
use bedrockcloud\command\sender\ICommandSender;

class CommandExecuteEvent extends CommandEvent {

    public function __construct(
        private readonly ICommandSender $sender,
        Command $command
    ) {
        parent::__construct($command);
    }

    public function getSender(): ICommandSender {
        return $this->sender;
    }
}