<?php

namespace bedrockcloud\command\impl\general;

use bedrockcloud\command\Command;
use bedrockcloud\command\CommandManager;
use bedrockcloud\command\sender\ICommandSender;
use bedrockcloud\language\Language;

class HelpCommand extends Command {

    public function execute(ICommandSender $sender, string $label, array $args): bool {
        $sender->info("Commands §8(§e" . count(CommandManager::getInstance()->getCommands()) . "§8)§r:");
        foreach (CommandManager::getInstance()->getCommands() as $command) {
            $sender->info("§e" . $command->getName() . " §8- §e" . Language::current()->translate($command->getDescription()) . " §8- §e" . $command->getUsage() . " §8- §e" . (empty($command->getAliases()) ? "§cNo Aliases" : implode(", ", $command->getAliases())));
        }
        return true;
    }
}