<?php

namespace bedrockcloud\http;

use Closure;
use pmmp\thread\ThreadSafeArray;
use bedrockcloud\config\impl\DefaultConfig;
use bedrockcloud\event\impl\http\HttpServerInitializeEvent;
use bedrockcloud\http\endpoint\EndpointRegistry;
use bedrockcloud\http\io\Request;
use bedrockcloud\http\io\Response;
use bedrockcloud\http\network\SocketClient;
use bedrockcloud\http\util\HttpUtils;
use bedrockcloud\http\util\Router;
use bedrockcloud\http\util\UnhandledHttpRequest;
use bedrockcloud\language\Language;
use bedrockcloud\BedrockCloud;
use bedrockcloud\thread\Thread;
use bedrockcloud\util\Address;
use bedrockcloud\util\CloudLogger;
use bedrockcloud\util\Reloadable;
use pocketmine\snooze\SleeperHandlerEntry;
use Socket;
use Throwable;

final class HttpServer extends Thread implements Reloadable {

    public const REQUEST_READ_LENGTH = 8192;

    private bool $connected = false;

    protected ?Socket $socket = null;
    private ThreadSafeArray $buffer;
    private SleeperHandlerEntry $entry;
    private ?Closure $invalidUrlHandler = null;

    public function __construct(private readonly Address $address) {
        $this->buffer = new ThreadSafeArray();
    }

    /**
     * Handles the server's running process.
     */
    public function onRun(): void {
        while ($this->connected) {
            try {
                $client = $this->accept();
                if ($client) {
                    $buffer = $client->read(self::REQUEST_READ_LENGTH);
                    if ($buffer) {
                        $this->buffer[] = new UnhandledHttpRequest($buffer, $client);
                        $this->entry->createNotifier()->wakeupSleeper();
                    }
                }
            } catch (Throwable $exception) {
                CloudLogger::get()->exception($exception);
            }
        }
    }

    /**
     * Sets a custom handler for invalid URLs.
     *
     * @param Closure $closure The custom handler
     */
    public function default(Closure $closure): void {
        $this->invalidUrlHandler = $closure;
    }

    /**
     * Processes an incoming HTTP request and generates a response.
     *
     * @param Address $address The client's address
     * @param string $request The request data
     * @return string The generated response
     */
    private function handleRequest(Address $address, string $request): string {
        try {
            $parsedRequest = HttpUtils::parseRequest($address, $request);
            if (!$parsedRequest instanceof Request) {
                return (new Response(500));
            }

            if (Router::getInstance()->isRegistered($parsedRequest)) {
                return Router::getInstance()->execute($parsedRequest);
            }

            $response = new Response(404);
            if ($this->invalidUrlHandler !== null) {
                ($this->invalidUrlHandler)($parsedRequest, $response);
            }
            return $response;
        } catch (Throwable $exception) {
            CloudLogger::get()->exception($exception);
            return (new Response(500));
        }
    }

    /**
     * Initializes the server.
     */
    public function init(): void {
        if (DefaultConfig::getInstance()->isHttpServerEnabled()) {
            (new HttpServerInitializeEvent())->call();
            EndpointRegistry::registerDefaults();

            try {
                if ($this->bind()) {
                    CloudLogger::get()->info(Language::current()->translate("httpServer.bound", $this->address->getPort()));
                } else {
                    CloudLogger::get()->error(Language::current()->translate("httpServer.bind.failed", $this->address->getPort()));
                    return;
                }
            } catch (Throwable $exception) {
                CloudLogger::get()->error(Language::current()->translate("httpServer.bind.failed.reason", $this->address->getPort(), $exception->getMessage()));
                return;
            }

            $this->entry = BedrockCloud::getInstance()->getSleeperHandler()->addNotifier(function (): void {
                while (($data = $this->buffer->shift()) !== null) {
                    $client = $data->getClient();
                    $buf = $data->getBuffer();
                    try {
                        $response = $this->handleRequest($client->getAddress(), $buf);

                        if (DefaultConfig::getInstance()->isHttpServerOnlyLocal() && !$client->getAddress()->isLocalHost()) {
                            $response = (new Response(403));
                            $response->customResponseCodeMessage("Not authorized");
                        }

                        $client->write($response);
                        $client->close();
                    } catch (Throwable $exception) {
                        CloudLogger::get()->warn(Language::current()->translate("httpServer.request.invalid", $client->getAddress()->__toString()));
                        CloudLogger::get()->debug($buf);
                        CloudLogger::get()->exception($exception);
                    }
                }
            });

            $this->start();
        }
    }

    /**
     * Binds the server to the provided address and port.
     *
     * @return bool True if the binding was successful, otherwise false
     */
    public function bind(): bool {
        try {
            $this->socket = socket_create(AF_INET, SOCK_STREAM, SOL_TCP);
            socket_set_option($this->socket, SOL_SOCKET, SO_REUSEADDR, 1);

            if (!socket_bind($this->socket, $this->address->getAddress(), $this->address->getPort())) {
                throw new \RuntimeException(socket_strerror(socket_last_error()));
            }

            $this->connected = true;
            return socket_listen($this->socket);
        } catch (Throwable $exception) {
            CloudLogger::get()->exception($exception);
            $this->close();
            return false;
        }
    }

    /**
     * Accepts a client connection.
     *
     * @return SocketClient|null The client connection, or null if no client
     */
    public function accept(): ?SocketClient {
        if (!$this->connected) {
            return null;
        }

        try {
            $clientSocket = @socket_accept($this->socket);
            if ($clientSocket instanceof Socket) {
                return SocketClient::fromSocket($clientSocket);
            }
        } catch (Throwable $exception) {
            CloudLogger::get()->exception($exception);
        }

        return null;
    }

    /**
     * Closes the server connection.
     */
    public function close(): void {
        if (!$this->connected) {
            return;
        }

        $this->connected = false;
        if ($this->socket) {
            @socket_shutdown($this->socket);
            @socket_close($this->socket);
        }
    }

    /**
     * Reloads the HTTP server.
     *
     * @return bool Always returns true
     */
    public function reload(): bool {
        if (DefaultConfig::getInstance()->isHttpServerEnabled() && !$this->connected) {
            if ($this->isRunning()) {
                $this->quit();
            }
            $this->init();
            $this->start();
        }
        return true;
    }

    /**
     * Gets the address the server is bound to.
     *
     * @return Address The server address
     */
    public function getAddress(): Address {
        return $this->address;
    }
}