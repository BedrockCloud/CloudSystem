<?php

namespace bedrockcloud\command\impl\template;

use bedrockcloud\command\Command;
use bedrockcloud\command\sender\ICommandSender;
use bedrockcloud\language\Language;
use bedrockcloud\template\TemplateHelper;
use bedrockcloud\template\TemplateManager;

class EditCommand extends Command {

    public function execute(ICommandSender $sender, string $label, array $args): bool {
        if (isset($args[0]) && isset($args[1]) && isset($args[2])) {
            if (($template = TemplateManager::getInstance()->getTemplateByName($args[0])) !== null) {
                if (TemplateHelper::isValidEditKey($args[1])) {
                    if (TemplateHelper::isValidEditValue($args[2], $args[1], $expected, $realValue)) {
                        TemplateManager::getInstance()->editTemplate(
                            $template,
                            ($args[1] == "lobby" ? $realValue : null),
                            ($args[1] == "maintenance" ? $realValue : null),
                            ($args[1] == "static" ? $realValue : null),
                            ($args[1] == "maxPlayerCount" ? $realValue : null),
                            ($args[1] == "minServerCount" ? $realValue : null),
                            ($args[1] == "maxServerCount" ? $realValue : null),
                            ($args[1] == "startNewWhenFull" ? $realValue : null),
                            ($args[1] == "autoStart" ? $realValue : null),
                        );
                    } else $sender->error(Language::current()->translate("command.edit.failed.second", $args[1], $expected));
                } else $sender->error(Language::current()->translate("command.edit.failed.first"));
            } else $sender->error(Language::current()->translate("template.not.found"));
        } else return false;
        return true;
    }
}