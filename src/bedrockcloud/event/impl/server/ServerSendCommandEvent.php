<?php

namespace bedrockcloud\event\impl\server;

use bedrockcloud\event\Cancelable;
use bedrockcloud\event\CancelableTrait;
use bedrockcloud\server\CloudServer;

class ServerSendCommandEvent extends ServerEvent implements Cancelable {
    use CancelableTrait;

    public function __construct(
        CloudServer $server,
        private readonly string $commandLine
    ) {
        parent::__construct($server);
    }

    public function getCommandLine(): string {
        return $this->commandLine;
    }
}