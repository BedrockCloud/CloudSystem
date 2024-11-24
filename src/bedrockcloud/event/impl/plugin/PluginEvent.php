<?php

namespace bedrockcloud\event\impl\plugin;

use bedrockcloud\event\Event;
use bedrockcloud\plugin\CloudPlugin;

abstract class PluginEvent extends Event {

    public function __construct(private readonly CloudPlugin $plugin) {}

    public function getPlugin(): CloudPlugin {
        return $this->plugin;
    }
}