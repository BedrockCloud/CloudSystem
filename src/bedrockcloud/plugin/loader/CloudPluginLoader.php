<?php

namespace bedrockcloud\plugin\loader;

use bedrockcloud\plugin\CloudPlugin;

interface CloudPluginLoader {

    public function canLoad(string $path): bool;

    public function loadPlugin(string $path): string|CloudPlugin;
}