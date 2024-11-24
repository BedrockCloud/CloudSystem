<?php

namespace bedrockcloud\command\impl\template;

use bedrockcloud\command\Command;
use bedrockcloud\command\sender\ICommandSender;
use bedrockcloud\language\Language;
use bedrockcloud\setup\impl\TemplateSetup;
use bedrockcloud\template\Template;
use bedrockcloud\template\TemplateManager;
use bedrockcloud\template\TemplateSettings;
use bedrockcloud\template\TemplateType;

class CreateCommand extends Command {

    public function execute(ICommandSender $sender, string $label, array $args): bool {
        if (isset($args[0])) {
            if (strtolower($args[0]) == "setup") {
                (new TemplateSetup())->startSetup();
            } else {
                if (!TemplateManager::getInstance()->checkTemplate($args[0])) {
                    $templateType = TemplateType::SERVER();
                    if (isset($args[1])) $templateType = TemplateType::get($args[1]) ?? TemplateType::SERVER();

                    TemplateManager::getInstance()->createTemplate(Template::create($args[0], TemplateSettings::create(false, true, false, 20, 0, 2, false, false), $templateType));
                } else $sender->error(Language::current()->translate("template.already.exists"));
            }
        } else (new TemplateSetup())->startSetup();
        return true;
    }
}