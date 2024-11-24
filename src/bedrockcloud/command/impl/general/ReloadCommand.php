<?php

namespace bedrockcloud\command\impl\general;

use bedrockcloud\command\Command;
use bedrockcloud\command\sender\ICommandSender;
use bedrockcloud\BedrockCloud;

class ReloadCommand extends Command {

    public function execute(ICommandSender $sender, string $label, array $args): bool {
        if (!BedrockCloud::getInstance()->isReloading()) {
            BedrockCloud::getInstance()->reload();
        }
        return true;
    }
}