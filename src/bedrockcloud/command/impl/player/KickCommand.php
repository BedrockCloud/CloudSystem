<?php

namespace bedrockcloud\command\impl\player;

use bedrockcloud\command\Command;
use bedrockcloud\command\sender\ICommandSender;
use bedrockcloud\language\Language;
use bedrockcloud\player\CloudPlayerManager;

class KickCommand extends Command {

    public function execute(ICommandSender $sender, string $label, array $args): bool {
        if (isset($args[0])) {
            if (($player = CloudPlayerManager::getInstance()->getPlayerByName(array_shift($args))) !== null) {
                $sender->info(Language::current()->translate("command.kick.success", $player->getName()));
                $player->kick(implode(" ", $args));
            } else $sender->error(Language::current()->translate("command.kick.failed"));
        } else return false;
        return true;
    }
}