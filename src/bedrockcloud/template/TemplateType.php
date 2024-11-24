<?php

namespace bedrockcloud\template;

use bedrockcloud\software\Software;
use bedrockcloud\software\SoftwareManager;
use bedrockcloud\util\EnumTrait;

/**
 * @method static TemplateType SERVER()
 * @method static TemplateType PROXY()
 */
final class TemplateType {
    use EnumTrait;

    protected static function init(): void {
        self::register("server", new TemplateType("SERVER", SoftwareManager::getInstance()->getSoftwareByName("PocketMine-MP")));
        self::register("proxy", new TemplateType("PROXY", SoftwareManager::getInstance()->getSoftwareByName("WaterdogPE")));
    }

    public static function get(string $name): ?TemplateType {
        self::check();
        return self::$members[strtoupper($name)] ?? null;
    }

    /** @return array<TemplateType> */
    public static function getAll(): array {
        self::check();
        return self::$members;
    }

    public function __construct(
        private readonly string $name,
        private readonly Software $software
    ) {}

    public function __toString(): string {
        return $this->name;
    }

    public function getName(): string {
        return $this->name;
    }

    public function getSoftware(): Software {
        return $this->software;
    }
}