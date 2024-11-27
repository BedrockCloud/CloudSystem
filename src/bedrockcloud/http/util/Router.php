<?php

namespace bedrockcloud\http\util;

use Closure;
use bedrockcloud\http\io\Request;
use bedrockcloud\http\io\Response;
use bedrockcloud\util\SingletonTrait;

final class Router {
    use SingletonTrait;

    public const GET = "GET";
    public const POST = "POST";
    public const PUT = "PUT";
    public const PATCH = "PATCH";
    public const DELETE = "DELETE";

    /** @var array<string, array<string, Closure>> $routes */
    protected readonly array $routes;

    public function __construct() {
        self::setInstance($this);
        $this->routes = [];
    }

    /**
     * Adds a route.
     * @param string $method HTTP method
     * @param string $route Route path
     * @param Closure $closure Callback for the route
     */
    private function add(string $method, string $route, Closure $closure): void {
        $this->routes[$method][$route] = $closure;
    }

    /**
     * Registers a GET route.
     * @param string $path The path for the route
     * @param Closure $closure Callback for the route
     */
    public function get(string $path, Closure $closure): void {
        $this->add(self::GET, $path, $closure);
    }

    /**
     * Registers a POST route.
     * @param string $path The path for the route
     * @param Closure $closure Callback for the route
     */
    public function post(string $path, Closure $closure): void {
        $this->add(self::POST, $path, $closure);
    }

    /**
     * Registers a PUT route.
     * @param string $path The path for the route
     * @param Closure $closure Callback for the route
     */
    public function put(string $path, Closure $closure): void {
        $this->add(self::PUT, $path, $closure);
    }

    /**
     * Registers a PATCH route.
     * @param string $path The path for the route
     * @param Closure $closure Callback for the route
     */
    public function patch(string $path, Closure $closure): void {
        $this->add(self::PATCH, $path, $closure);
    }

    /**
     * Registers a DELETE route.
     * @param string $path The path for the route
     * @param Closure $closure Callback for the route
     */
    public function delete(string $path, Closure $closure): void {
        $this->add(self::DELETE, $path, $closure);
    }

    /**
     * Checks if a route is registered for the request.
     * @param Request $request The request to check
     * @return bool True if the route is registered, otherwise False
     */
    public function isRegistered(Request $request): bool {
        return $this->pickRoute($request->data()->method(), $request->data()->path()) !== null;
    }

    /**
     * Executes the route and returns a response.
     * @param Request $request The request
     * @return Response The response
     */
    public function execute(Request $request): Response {
        $response = new Response();
        $route = $this->pickRoute($request->data()->method(), $request->data()->path());

        if ($route !== null) {
            [$expectedPath, $closure] = $route;
            HttpUtils::fillRequest($request, $expectedPath);
            $closure($request, $response);
        }

        return $response;
    }

    /**
     * Picks the route for the given HTTP method and path.
     * @param string $method The HTTP method (GET, POST, etc.)
     * @param string $path The path of the request
     * @return array<string, mixed>|null The route and the callback if found, otherwise null
     */
    public function pickRoute(string $method, string $path): ?array {
        foreach ($this->routes[$method] ?? [] as $expectedPath => $closure) {
            if (HttpUtils::matchPath($expectedPath, $path)) {
                return [$expectedPath, $closure];
            }
        }
        return null;
    }

    /**
     * Returns the Router instance (Singleton).
     * @return self The Router instance
     */
    public static function getInstance(): self {
        return self::$instance ??= new self;
    }
}