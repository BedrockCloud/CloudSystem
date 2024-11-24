<?php

namespace bedrockcloud\web;

use bedrockcloud\BedrockCloud;
use bedrockcloud\console\log\Logger;
use bedrockcloud\util\CloudLogger;
use bedrockcloud\util\Utils;

final class WebAccount {

    public function __construct(
        private readonly string $name,
        private string $password,
        private bool $initialPassword,
        private WebAccountRoles $role
    ) {}

    public function setPassword(string $password): void {
        if (empty($password)) {
            throw new \InvalidArgumentException("Password cannot be empty.");
        }
        $this->password = $password;
    }

    public function setInitialPassword(bool $initialPassword): void {
        $this->initialPassword = $initialPassword;
    }

    public function setRole(WebAccountRoles $role): void {
        $this->role = $role;
    }

    public function getName(): string {
        return $this->name;
    }

    public function getPassword(): string {
        return $this->password;
    }

    public function isInitialPassword(): bool {
        return $this->initialPassword;
    }

    public function getRole(): WebAccountRoles {
        return $this->role;
    }

    public function toArray(): array {
        return [
            "username" => $this->name,
            "password" => $this->password,
            "initialPassword" => $this->initialPassword,
            "role" => $this->role->roleName()
        ];
    }

    public static function create(string $name, string $password, bool $initialPassword, WebAccountRoles $role): self {
        self::validateName($name);
        self::validatePassword($password);

        return new self($name, $password, $initialPassword, $role);
    }

    public static function fromArray(array $data): ?self {
        if (!Utils::containKeys($data, "username", "password", "initialPassword", "role")) {
            return null;
        }

        $role = WebAccountRoles::from($data["role"]);
        if (!is_bool($data["initialPassword"])) {
            return null;
        }

        try {
            return self::create($data["username"], $data["password"], $data["initialPassword"], $role);
        } catch (\InvalidArgumentException $e) {
            CloudLogger::get()->exception($e);
            return null;
        }
    }

    private static function validateName(string $name): void {
        if (empty($name) || strlen($name) > 50) {
            throw new \InvalidArgumentException("Invalid username: must be between 1 and 50 characters.");
        }
    }

    private static function validatePassword(string $password): void {
        if (strlen($password) < 8) {
            throw new \InvalidArgumentException("Password must be at least 8 characters long.");
        }
    }
}