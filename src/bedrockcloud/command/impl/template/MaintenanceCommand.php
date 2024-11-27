<?php

namespace bedrockcloud\command\impl\template;

use bedrockcloud\command\Command;
use bedrockcloud\command\sender\ICommandSender;
use bedrockcloud\config\impl\MaintenanceList;
use bedrockcloud\language\Language;

class MaintenanceCommand extends Command {

    public function execute(ICommandSender $sender, string $label, array $args): bool {
        if (empty($args)) {
            return false;
        }

        $subCommand = strtolower($args[0]);
        array_shift($args);

        switch ($subCommand) {
            case "add":
                if (empty($args)) {
                    return false;
                }

                $target = trim(implode(" ", $args));
                if (!MaintenanceList::is($target)) {
                    $sender->info(Language::current()->translate("command.maintenance.success.first", $target));
                    MaintenanceList::add($target);
                } else {
                    $sender->error(Language::current()->translate("command.maintenance.failed.first"));
                }
                break;

            case "remove":
                if (empty($args)) {
                    return false;
                }

                $target = trim(implode(" ", $args));
                if (MaintenanceList::is($target)) {
                    $sender->info(Language::current()->translate("command.maintenance.success.second", $target));
                    MaintenanceList::remove($target);
                } else {
                    $sender->error(Language::current()->translate("command.maintenance.failed.second"));
                }
                break;

            case "list":
                $players = MaintenanceList::all();
                $sender->info("Players: §8(§e" . count($players) . "§8)");
                if (empty($players)) {
                    $sender->info(Language::current()->translate("command.maintenance.failed.third"));
                } else {
                    $sender->info("§e" . implode("§8, §e", $players));
                }
                break;

            default:
                return false;
        }

        return true;
    }
}