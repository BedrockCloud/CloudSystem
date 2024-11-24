<?php

namespace bedrockcloud\web;

use bedrockcloud\config\Config;
use bedrockcloud\config\impl\DefaultConfig;
use bedrockcloud\config\type\ConfigTypes;
use bedrockcloud\http\endpoint\EndpointRegistry;
use bedrockcloud\http\endpoint\impl\web\WebAccountCreateEndPoint;
use bedrockcloud\http\endpoint\impl\web\WebAccountGetEndPoint;
use bedrockcloud\http\endpoint\impl\web\WebAccountListEndPoint;
use bedrockcloud\http\endpoint\impl\web\WebAccountRemoveEndPoint;
use bedrockcloud\http\endpoint\impl\web\WebAccountUpdateEndPoint;
use bedrockcloud\util\Reloadable;
use bedrockcloud\util\SingletonTrait;

final class WebAccountManager implements Reloadable {
    use SingletonTrait;

    /** @var array<WebAccount> */
    private array $accounts = [];
    private Config $accountsConfig;

    private bool $isWebEnabled;

    public function __construct() {
        self::setInstance($this);
        $this->accountsConfig = new Config(WEB_PATH . "accounts.json", ConfigTypes::JSON());
        $this->isWebEnabled = DefaultConfig::getInstance()->isWebEnabled();
    }

    public function loadAccounts(): void {
        if (!$this->isWebEnabled) return;

        $data = $this->accountsConfig->getAll();
        foreach ($data as $accountData) {
            $account = WebAccount::fromArray($accountData);
            if ($account !== null) {
                $this->accounts[$account->getName()] = $account;
            }
        }

        $this->registerEndpoints();
    }

    public function createAccount(WebAccount $account): void {
        if (!$this->isWebEnabled) return;

        $this->accounts[$account->getName()] = $account;
        $this->accountsConfig->set($account->getName(), $account->toArray());
        $this->saveConfig();
    }

    public function updateAccount(WebAccount $account, ?string $password, ?WebAccountRoles $role): void {
        if (!$this->isWebEnabled) return;

        if ($password !== null) {
            $account->setPassword($password);
            $account->setInitialPassword(false);
        }

        if ($role !== null) {
            $account->setRole($role);
        }

        $this->accountsConfig->set($account->getName(), $account->toArray());
        $this->saveConfig();
    }

    public function removeAccount(WebAccount $account): void {
        if (!$this->isWebEnabled) return;

        if ($this->checkAccount($account->getName())) {
            unset($this->accounts[$account->getName()]);
        }

        $this->accountsConfig->remove($account->getName());
        $this->saveConfig();
    }

    public function reload(): bool {
        $this->accounts = [];
        if ($this->isWebEnabled) {
            $this->accountsConfig->reload();
            $this->loadAccounts();
        } else {
            unset($this->accountsConfig);
            $this->unregisterEndpoints();
        }

        return true;
    }

    public function checkAccount(string $name): bool {
        return isset($this->accounts[$name]);
    }

    public function getAccount(string $name): ?WebAccount {
        return $this->isWebEnabled ? $this->accounts[$name] ?? null : null;
    }

    public function getAccounts(): array {
        return $this->isWebEnabled ? $this->accounts : [];
    }

    public static function getInstance(): self {
        return self::$instance ??= new self;
    }

    private function saveConfig(): void {
        $this->accountsConfig->save();
    }

    private function registerEndpoints(): void {
        EndpointRegistry::addEndPoint(new WebAccountCreateEndPoint());
        EndpointRegistry::addEndPoint(new WebAccountRemoveEndPoint());
        EndpointRegistry::addEndPoint(new WebAccountGetEndPoint());
        EndpointRegistry::addEndPoint(new WebAccountUpdateEndPoint());
        EndpointRegistry::addEndPoint(new WebAccountListEndPoint());
    }

    private function unregisterEndpoints(): void {
        EndpointRegistry::removeEndPoint(new WebAccountCreateEndPoint());
        EndpointRegistry::removeEndPoint(new WebAccountRemoveEndPoint());
        EndpointRegistry::removeEndPoint(new WebAccountGetEndPoint());
        EndpointRegistry::removeEndPoint(new WebAccountUpdateEndPoint());
        EndpointRegistry::removeEndPoint(new WebAccountListEndPoint());
    }
}