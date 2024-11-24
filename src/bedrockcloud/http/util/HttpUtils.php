<?php

namespace bedrockcloud\http\util;

use bedrockcloud\http\io\data\Queries;
use bedrockcloud\http\io\data\RequestData;
use bedrockcloud\http\io\Request;
use bedrockcloud\util\Address;
use bedrockcloud\util\CloudLogger;
use UnexpectedValueException;

final class HttpUtils {
	
	private const LOCALHOST_PREFIX = "http://localhost";

    public static function fillRequest(Request $request, string $baseUrl): void {
        $base = rtrim(str_replace("\\", "/", $baseUrl), "/");
        $match = rtrim(str_replace("\\", "/", $request->data()->path()), "/");

        if (str_contains($base, "?")) {
            $baseQueryParams = self::remapQueries(substr($base, strpos($base, "?") + 1));
            $matchQueryParams = self::remapQueries(substr($match, strpos($match, "?") + 1));

            foreach ($baseQueryParams as $key => $name) {
                if (isset($matchQueryParams[$key])) {
                    $request->{$name} = $matchQueryParams[$key];
                }
            }

            $base = substr($base, 0, strpos($base, "?"));
            $match = substr($match, 0, strpos($match, "?"));
        }

        $baseSegments = explode("/", $base);
        $matchSegments = explode("/", $match);

        if (count($baseSegments) !== count($matchSegments)) {
            throw new UnexpectedValueException("Mismatch in segment count between baseUrl and request path.");
        }

        foreach ($baseSegments as $index => $segment) {
            if (str_starts_with($segment, "#")) {
                $propertyName = substr($segment, 1);
                $request->{$propertyName} = $matchSegments[$index];
            } elseif (str_starts_with($segment, "{") && str_ends_with($segment, "}")) {
                $parameterDetails = json_decode($segment, true);
                if (!isset($parameterDetails["name"])) {
                    throw new UnexpectedValueException("Parameter name is missing in segment: $segment");
                }

                $propertyName = $parameterDetails["name"];
                $request->{$propertyName} = $matchSegments[$index];

                if (isset($parameterDetails["pattern"]) && !Pattern::isValid($matchSegments[$index], $parameterDetails["pattern"])) {
                    throw new UnexpectedValueException("Value '{$matchSegments[$index]}' does not match the expected pattern for parameter '$propertyName'.");
                }
            } elseif ($segment !== $matchSegments[$index]) {
                throw new UnexpectedValueException("Path segment mismatch at index $index: expected '$segment', got '{$matchSegments[$index]}'.");
            }
        }
    }


    public static function parseRequest(Address $address, string $request): ?Request {
        [$headers, $bodyLines] = self::splitData(explode("\r\n", $request));

        if (empty($headers)) {
            CloudLogger::get()->debug("Empty headers received in HTTP request.");
            return null;
        }

        $requestLine = trim(array_shift($headers));
        $requestParts = explode(" ", $requestLine);
        $method = strtoupper(array_shift($requestParts));

        if (!in_array($method, Request::SUPPORTED_REQUEST_METHODS, true)) {
            CloudLogger::get()->debug("Unsupported HTTP method: $method");
            return null;
        }

        $path = "/" . trim(array_shift($requestParts) ?? "", "/");
        if (!self::isValidPath($path)) {
            CloudLogger::get()->debug("Invalid HTTP path: $path");
            return null;
        }

        $queries = "";
        if (str_contains($path, "?")) {
            [$path, $queries] = explode("?", $path, 2);
        }

        return new Request(
            self::decodeHeaders($headers),
            new RequestData(
                $address,
                $method,
                urldecode($path),
                new Queries(self::remapQueries($queries))
            ),
            implode(PHP_EOL, array_filter($bodyLines, fn($line) => !empty($line)))
        );
    }

    private static function decodeHeaders(array $headers): array {
        $parsedHeaders = [];
        foreach ($headers as $header) {
            if (!str_contains($header, ": ")) {
                CloudLogger::get()->debug("Malformed header: $header");
                continue;
            }

            [$key, $value] = explode(": ", $header, 2);
            $parsedHeaders[trim($key)] = trim($value);
        }
        return $parsedHeaders;
    }

    public static function encodeHeaders(array $headers): array {
        return array_map(fn($key, $value) => "$key: $value", array_keys($headers), $headers);
    }

    private static function splitData(array $lines): array {
        $headers = [];
        $bodyLines = [];

        $headerSection = true;
        foreach ($lines as $line) {
            if ($headerSection && trim($line) === "") {
                $headerSection = false;
                continue;
            }

            if ($headerSection) {
                $headers[] = $line;
            } else {
                $bodyLines[] = $line;
            }
        }

        return [$headers, $bodyLines];
    }

    private static function remapQueries(string $queries): array {
        if (empty($queries)) return [];

        $mappedQueries = [];
        foreach (explode("&", $queries) as $pair) {
            if (str_contains($pair, "=")) {
                [$key, $value] = explode("=", $pair, 2);
                $mappedQueries[urldecode($key)] = urldecode($value);
            }
        }

        return $mappedQueries;
    }

    private static function isValidPath(string $path): bool {
        $fullUrl = self::LOCALHOST_PREFIX . $path;
        return filter_var($fullUrl, FILTER_VALIDATE_URL) === $fullUrl;
    }

    public static function matchPath(string $base, string $match): bool {
        if (str_contains($base, "?")) {
            if (!str_contains($match, "?")) return false;
            if (!self::matchQueries(substr($base, strpos($base, "?") + 1), substr($match, strpos($match, "?") + 1))) return false;
            $base = substr($base, 0, strpos($base, "?"));
        }
        if (str_contains($match, "?")) {
            $match = substr($match, 0, strpos($match, "?"));
        }

        $baseSegments = explode("/", rtrim(str_replace("\\", "/", $base), "/"));
        $matchSegments = explode("/", rtrim(str_replace("\\", "/", $match), "/"));

        if (count($baseSegments) !== count($matchSegments)) {
            return false;
        }

        foreach ($baseSegments as $i => $segment) {
            if ($segment === "#") continue;
            if ($segment[0] === "{" && $segment[-1] === "}") continue;
            if ($segment !== $matchSegments[$i]) {
                return false;
            }
        }

        return true;
    }

    public static function matchQueries(string $base, string $match): bool {
        $baseQueries = self::remapQueries($base);
        $matchQueries = self::remapQueries($match);

        return empty(array_diff_key($baseQueries, $matchQueries));
    }
}
