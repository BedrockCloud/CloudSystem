<?php

namespace bedrockcloud\command\impl\server;

use bedrockcloud\command\Command;
use bedrockcloud\command\sender\ICommandSender;
use bedrockcloud\language\Language;
use bedrockcloud\server\CloudServerManager;

class SaveCommand extends Command {

    public function execute(ICommandSender $sender, string $label, array $args): bool {
        if (empty($args)) {
            return false;
        }

        $serverName = $args[0];
        $server = CloudServerManager::getInstance()->getServerByName($serverName);

        if ($server === null) {
            $sender->error(Language::current()->translate("server.not.found"));
            return true;
        }

        CloudServerManager::getInstance()->saveServer($server);
        return true;
    }
}