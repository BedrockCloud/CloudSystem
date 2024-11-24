<?php

namespace bedrockcloud\command\impl\plugin;

use bedrockcloud\command\Command;
use bedrockcloud\command\sender\ICommandSender;
use bedrockcloud\language\Language;
use bedrockcloud\plugin\CloudPluginManager;

class DisableCommand extends Command {

    public function execute(ICommandSender $sender, string $label, array $args): bool {
        if (isset($args[0])) {
            if (($plugin = CloudPluginManager::getInstance()->getPluginByName($args[0])) !== null) {
                if ($plugin->isEnabled()) {
                    CloudPluginManager::getInstance()->disablePlugin($plugin);
                } else $sender->error(Language::current()->translate("command.disable.failed"));
            } else $sender->error(Language::current()->translate("plugin.not.found"));
        } else return false;
        return true;
    }
}