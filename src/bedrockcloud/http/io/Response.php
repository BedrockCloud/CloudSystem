<?php

namespace bedrockcloud\http\io;

use DateTimeInterface;
use bedrockcloud\http\util\HttpUtils;
use bedrockcloud\http\util\StatusCodes;
use JetBrains\PhpStorm\ArrayShape;
use function date;
use function implode;
use function is_array;
use function json_encode;
use function strlen;

final class Response {

    private string $body = "";
    private ?string $customResponseCodeMessage = null;
    private array $headers = [
        "Content-Type" => "text/plain",
        "Content-Length" => 0,
        "Connection" => "close"
    ];

    public function __construct(private int $statusCode = 200) { }

    public function code(int $statusCode): void {
        $this->statusCode = $statusCode;
    }

    public function body(string|array $body): void {
        if (is_array($body)) {
            $this->contentType("application/json");
            $body = json_encode($body);
        }
        $this->body = $body;
        $this->headers["Content-Length"] = strlen($this->body);
    }

    public function html(string $body): void {
        $this->contentType("text/html");
        $this->body($body);
    }

    public function redirect(string $url, bool $updateBody = true): void {
        $this->headers["Location"] = $url;
        $this->code(302);

        if ($updateBody) {
            $this->html("<p>Redirecting to <a href='" . htmlspecialchars($url, ENT_QUOTES, 'UTF-8') . "'>" . htmlspecialchars($url, ENT_QUOTES, 'UTF-8') . "</a></p>");
        }
    }

    public function contentType(string $type): void {
        $this->headers["Content-Type"] = $type;
    }

    public function customResponseCodeMessage(string $message): void {
        $this->customResponseCodeMessage = $message;
    }

    public function __toString(): string {
        $this->headers += $this->getOverwriteHeaders();
        $statusMessage = $this->customResponseCodeMessage ?? StatusCodes::RESPOND_CODES[$this->statusCode] ?? "None";

        return sprintf(
            "HTTP/1.1 %d %s\r\n%s\r\n\r\n%s",
            $this->statusCode,
            $statusMessage,
            implode("\r\n", HttpUtils::encodeHeaders($this->headers)),
            $this->body
        );
    }

    #[ArrayShape(["Date" => "string"])]
    private function getOverwriteHeaders(): array {
        return [
            "Date" => date(DateTimeInterface::RFC7231)
        ];
    }
}