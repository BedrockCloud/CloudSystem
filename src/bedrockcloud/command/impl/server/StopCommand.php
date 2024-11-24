<?php

namespace bedrockcloud\command\impl\server;

use bedrockcloud\command\Command;
use bedrockcloud\command\sender\ICommandSender;
use bedrockcloud\language\Language;
use bedrockcloud\server\CloudServerManager;
use bedrockcloud\template\TemplateManager;

class StopCommand extends Command {

    public function execute(ICommandSender $sender, string $label, array $args): bool {
        if (isset($args[0])) {
            if (($template = TemplateManager::getInstance()->getTemplateByName($args[0])) !== null) {
                CloudServerManager::getInstance()->stopTemplate($template);
            } else if (($server = CloudServerManager::getInstance()->getServerByName($args[0])) !== null) {
                CloudServerManager::getInstance()->stopServer($server);
            } else if (strtolower($args[0]) == "all") {
                if (empty(CloudServerManager::getInstance()->getServers())) {
                    $sender->error(Language::current()->translate("command.stop.failed"));
                    return true;
                }
                CloudServerManager::getInstance()->stopAll();
            } else $sender->error(Language::current()->translate("server.not.found"));
        } else return false;
        return true;
    }
}