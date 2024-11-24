<?php

namespace bedrockcloud\command\impl\general;

use bedrockcloud\command\Command;
use bedrockcloud\command\sender\ICommandSender;
use bedrockcloud\language\Language;
use bedrockcloud\BedrockCloud;

class ExitCommand extends Command {

    public function execute(ICommandSender $sender, string $label, array $args): bool {
        if (isset($args[0])) {
            if (strtolower($args[0]) == "confirm") BedrockCloud::getInstance()->shutdown();
            else $sender->info(Language::current()->translate("command.exit.confirm"));
        } else $sender->info(Language::current()->translate("command.exit.confirm"));
        return true;
    }
}