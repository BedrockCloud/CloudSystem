<?php

namespace bedrockcloud\network\client;

use Closure;
use JetBrains\PhpStorm\Pure;
use bedrockcloud\server\CloudServer;
use bedrockcloud\server\CloudServerManager;
use bedrockcloud\util\Address;
use bedrockcloud\util\SingletonTrait;

final class ServerClientManager {
    use SingletonTrait;

    /** @var array<ServerClient> */
    private array $clients = [];

    public function __construct() {
        self::setInstance($this);
    }

    public function addClient(CloudServer $server, ServerClient $client): void {
        if (!$this->issetClient($client)) $this->clients[$server->getName()] = $client;
    }

    public function removeClient(ServerClient|CloudServer $client): void {
        $client = $client instanceof CloudServer ? $this->getClientOfServer($client) : $client;
        if ($client !== null) {
            if ($this->issetClient($client)) unset($this->clients[array_search($client, $this->clients)]);
        }
    }

    public function issetClient(ServerClient $client): bool {
        return in_array($client, $this->clients);
    }

    public function pickClients(Closure $conditionClosure): array {
        return array_filter($this->clients, $conditionClosure);
    }

    #[Pure] public function getClientOfServer(CloudServer $server): ?ServerClient {
        return $this->clients[$server->getName()] ?? null;
    }

    public function getServerOfClient(ServerClient $client): ?CloudServer {
        return $this->issetClient($client) ? CloudServerManager::getInstance()->getServerByName(array_search($client, $this->clients)) : null;
    }

    #[Pure] public function getClientByAddress(Address $address): ?ServerClient {
        foreach ($this->clients as $client) if ($client->getAddress()->equals($address)) return $client;
        return null;
    }

    public function getClients(): array {
        return $this->clients;
    }

    public static function getInstance(): self {
        return self::$instance ??= new self;
    }
}