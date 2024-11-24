<?php

namespace bedrockcloud\http\endpoint;

use bedrockcloud\http\io\Request;
use bedrockcloud\http\io\Response;

abstract class EndPoint {

    public function __construct(
        private readonly string $requestMethod,
        private readonly string $path
    ) {}

    /**
     * @param Request $request
     * @param Response $response
     * @return array the body response
     */
    abstract public function handleRequest(Request $request, Response $response): array;

    abstract public function isBadRequest(Request $request): bool;

    public function getRequestMethod(): string {
        return $this->requestMethod;
    }

    public function getPath(): string {
        return $this->path;
    }
}