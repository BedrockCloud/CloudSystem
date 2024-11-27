<?php

namespace bedrockcloud\command\impl\template;

use bedrockcloud\command\Command;
use bedrockcloud\command\sender\ICommandSender;
use bedrockcloud\language\Language;
use bedrockcloud\template\TemplateHelper;
use bedrockcloud\template\TemplateManager;

class EditCommand extends Command {

    public function execute(ICommandSender $sender, string $label, array $args): bool {
        if (count($args) < 3) {
            return false;
        }

        [$templateName, $editKey, $editValue] = $args;

        $template = TemplateManager::getInstance()->getTemplateByName($templateName);
        if ($template === null) {
            $sender->error(Language::current()->translate("template.not.found"));
            return true;
        }

        if (!TemplateHelper::isValidEditKey($editKey)) {
            $sender->error(Language::current()->translate("command.edit.failed.first"));
            return true;
        }

        if (!TemplateHelper::isValidEditValue($editValue, $editKey, $expected, $realValue)) {
            $sender->error(Language::current()->translate(
                "command.edit.failed.second",
                $editKey,
                $expected
            ));
            return true;
        }

        $editFields = [
            "lobby" => null,
            "maintenance" => null,
            "static" => null,
            "maxPlayerCount" => null,
            "minServerCount" => null,
            "maxServerCount" => null,
            "startNewWhenFull" => null,
            "autoStart" => null,
        ];

        if (array_key_exists($editKey, $editFields)) {
            $editFields[$editKey] = $realValue;
        }

        TemplateManager::getInstance()->editTemplate(
            $template,
            $editFields["lobby"],
            $editFields["maintenance"],
            $editFields["static"],
            $editFields["maxPlayerCount"],
            $editFields["minServerCount"],
            $editFields["maxServerCount"],
            $editFields["startNewWhenFull"],
            $editFields["autoStart"]
        );

        return true;
    }
}