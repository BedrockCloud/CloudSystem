<?php

namespace bedrockcloud\command\impl\server;

use bedrockcloud\command\Command;
use bedrockcloud\command\sender\ICommandSender;
use bedrockcloud\language\Language;
use bedrockcloud\server\CloudServerManager;
use bedrockcloud\template\TemplateManager;

class StopCommand extends Command {

    public function execute(ICommandSender $sender, string $label, array $args): bool {
        if (empty($args)) {
            return false;
        }

        $target = strtolower($args[0]);

        if (($template = TemplateManager::getInstance()->getTemplateByName($target)) !== null) {
            CloudServerManager::getInstance()->stopTemplate($template);
            return true;
        }

        if (($server = CloudServerManager::getInstance()->getServerByName($target)) !== null) {
            CloudServerManager::getInstance()->stopServer($server);
            return true;
        }

        if ($target === "all") {
            $servers = CloudServerManager::getInstance()->getServers();
            if (empty($servers)) {
                $sender->error(Language::current()->translate("command.stop.failed"));
                return true;
            }
            CloudServerManager::getInstance()->stopAll();
            return true;
        }

        $sender->error(Language::current()->translate("server.not.found"));
        return true;
    }
}