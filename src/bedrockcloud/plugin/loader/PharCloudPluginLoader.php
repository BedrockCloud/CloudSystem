<?php

namespace bedrockcloud\plugin\loader;

use Phar;
use bedrockcloud\plugin\CloudPlugin;
use bedrockcloud\plugin\CloudPluginDescription;
use bedrockcloud\BedrockCloud;
use bedrockcloud\util\Utils;

final class PharCloudPluginLoader implements CloudPluginLoader {

    public function canLoad(string $path): bool {
        if (is_file($path) && pathinfo($path, PATHINFO_EXTENSION) == "phar") {
            $phar = new Phar($path);
            return isset($phar["plugin.yml"]) && isset($phar["src"]);
        }
        return false;
    }

    public function loadPlugin(string $path): string|CloudPlugin {
        $phar = new Phar($path);
        $pluginYml = yaml_parse(file_get_contents($phar["plugin.yml"]->getPathname()));
        if (!$pluginYml) return "Can't parse plugin.yml";
        $pluginYml = CloudPluginDescription::fromArray($pluginYml);
        if ($pluginYml === null) return "Incorrect plugin.yml";

        BedrockCloud::getInstance()->getClassLoader()->addPath("", "phar://" . $path . "/src/");
        #Utils::requireDirectory("phar://" . $path . "/src");
        $plugin = new ($pluginYml->getMain())($pluginYml);
        if (!is_subclass_of($plugin, CloudPlugin::class)) return "Is not a valid CloudPlugin";
        return $plugin;
    }
}