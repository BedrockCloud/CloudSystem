<?php

namespace bedrockcloud\command\impl\template;

use bedrockcloud\command\Command;
use bedrockcloud\command\sender\ICommandSender;
use bedrockcloud\language\Language;
use bedrockcloud\template\TemplateManager;

class DeleteCommand extends Command {

    public function execute(ICommandSender $sender, string $label, array $args): bool {
        if (isset($args[0])) {
            if (($template = TemplateManager::getInstance()->getTemplateByName($args[0])) !== null) {
                TemplateManager::getInstance()->deleteTemplate($template);
            } else $sender->error(Language::current()->translate("template.not.found"));
        } else return false;
        return true;
    }
}