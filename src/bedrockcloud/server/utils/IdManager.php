<?php

namespace bedrockcloud\server\utils;

use bedrockcloud\template\Template;

final class IdManager {

    /** @var array<string, array<int, bool>> */
    private static array $ids = [];

    public static function addId(Template $template, int $id): void {
        $templateName = $template->getName();

        if (!isset(self::$ids[$templateName])) {
            self::$ids[$templateName] = [];
        }

        self::$ids[$templateName][$id] = true;
    }

    public static function removeId(Template $template, int $id): void {
        $templateName = $template->getName();

        if (!empty(self::$ids[$templateName])) {
            unset(self::$ids[$templateName][$id]);
        }

        if (empty(self::$ids[$templateName])) {
            unset(self::$ids[$templateName]);
        }
    }

    public static function getFreeId(Template $template): int {
        $templateName = $template->getName();

        if (!isset(self::$ids[$templateName])) {
            self::$ids[$templateName] = [];
        }

        $maxServerCount = $template->getSettings()->getMaxServerCount();

        for ($i = 1; $i <= $maxServerCount; $i++) {
            if (!isset(self::$ids[$templateName][$i])) {
                return $i;
            }
        }

        return -1;
    }
}
