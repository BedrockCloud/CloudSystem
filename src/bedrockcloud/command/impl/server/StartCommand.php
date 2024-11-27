<?php

namespace bedrockcloud\command\impl\server;

use bedrockcloud\command\Command;
use bedrockcloud\command\sender\ICommandSender;
use bedrockcloud\language\Language;
use bedrockcloud\server\CloudServerManager;
use bedrockcloud\template\TemplateManager;

class StartCommand extends Command {

    public function execute(ICommandSender $sender, string $label, array $args): bool {
        if (empty($args)) {
            return false;
        }

        $templateName = $args[0];
        $template = TemplateManager::getInstance()->getTemplateByName($templateName);

        if ($template === null) {
            $sender->error(Language::current()->translate("template.not.found"));
            return true;
        }

        $count = isset($args[1]) && is_numeric($args[1]) && intval($args[1]) > 0
            ? intval($args[1])
            : 1;

        CloudServerManager::getInstance()->startServer($template, $count);
        return true;
    }
}
