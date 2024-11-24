<?php

namespace bedrockcloud\console;

use pmmp\thread\ThreadSafeArray;
use bedrockcloud\command\CommandManager;
use bedrockcloud\BedrockCloud;
use bedrockcloud\setup\Setup;
use bedrockcloud\thread\Thread;
use pocketmine\snooze\SleeperHandlerEntry;

final class Console extends Thread {

    private ThreadSafeArray $buffer;
    private SleeperHandlerEntry $entry;

    public function __construct() {
        $this->buffer = new ThreadSafeArray();

        $this->entry = BedrockCloud::getInstance()->getSleeperHandler()->addNotifier(function (): void {
            while (($line = $this->buffer->shift()) !== null) if (!BedrockCloud::getInstance()->isReloading()) {
                if (($setup = Setup::getCurrentSetup()) === null) {
                    CommandManager::getInstance()->execute($line);
                } else {
                    $setup->handleInput($line);
                }
            }
        });
    }

    public function onRun(): void {
        $input = fopen("php://stdin", "r");
        while ($this->isRunning()) {
            $this->buffer[] = trim(fgets($input));
            $this->entry->createNotifier()->wakeupSleeper();
        }

        fclose($input);
    }
}