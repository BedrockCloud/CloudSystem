<?php

namespace bedrockcloud\http\io;

use bedrockcloud\config\impl\DefaultConfig;
use bedrockcloud\http\io\data\RequestData;
use stdClass;

final class Request extends stdClass {

    public const SUPPORTED_REQUEST_METHODS = ["GET", "POST", "PUT", "DELETE", "PATCH"];

    public function __construct(
        private readonly array $headers,
        private readonly RequestData $requestData,
        private readonly ?string $body = null
    ) {}

    public function authorized(): bool {
        $authKey = $this->headers["auth-key"] ?? null;
        return $authKey === DefaultConfig::getInstance()->getHttpServerAuthKey();
    }

    public function getBody(): ?string {
        return $this->body;
    }

    public function getHeaders(): array {
        return $this->headers;
    }

    public function data(): RequestData {
        return $this->requestData;
    }
}