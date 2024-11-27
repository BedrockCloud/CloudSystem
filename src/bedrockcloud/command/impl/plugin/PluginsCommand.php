<?php

namespace bedrockcloud\command\impl\plugin;

use bedrockcloud\command\Command;
use bedrockcloud\command\sender\ICommandSender;
use bedrockcloud\language\Language;
use bedrockcloud\plugin\CloudPluginManager;

class PluginsCommand extends Command {

    public function execute(ICommandSender $sender, string $label, array $args): bool {
        $pluginManager = CloudPluginManager::getInstance();
        $plugins = $pluginManager->getPlugins();
        $pluginCount = count($plugins);

        $sender->info("Plugins §8(§e{$pluginCount}§8)§r:");

        if (empty($plugins)) {
            $sender->info(Language::current()->translate("command.plugins.none"));
            return true;
        }

        foreach ($plugins as $plugin) {
            $description = $plugin->getDescription();
            $sender->info("Name: §e" . $description->getName());

            if ($description->getDescription() !== null) {
                $sender->info(Language::current()->translate("raw.description") . ": §e" . $description->getDescription());
            }

            $sender->info("Version: §ev" . $description->getVersion());

            if (!empty($description->getAuthors())) {
                $sender->info(Language::current()->translate("raw.author") . ": §e" . implode(", ", $description->getAuthors()));
            }

            $sender->info("FullName: §e" . $description->getFullName());
            $sender->info(Language::current()->translate("raw.enabled") . ": " .
                ($plugin->isEnabled()
                    ? "§a" . Language::current()->translate("raw.yes")
                    : "§c" . Language::current()->translate("raw.no")
                )
            );
        }

        return true;
    }
}