<?php

namespace bedrockcloud\event;

use Closure;
use bedrockcloud\plugin\CloudPlugin;
use bedrockcloud\util\SingletonTrait;
use ReflectionClass;
use ReflectionMethod;

final class EventManager {
    use SingletonTrait;

    private array $handlers = [];

    public function __construct() {
        self::setInstance($this);
    }

    /**
     * Register an event handler for a specific event class.
     */
    public function registerEvent(string $eventClass, Closure $closure, CloudPlugin $plugin): void {
        if (!is_subclass_of($eventClass, Event::class)) {
            throw new \InvalidArgumentException("The event class must be a subclass of Event.");
        }

        $pluginName = $plugin->getDescription()->getFullName();
        $this->handlers[$pluginName][$eventClass][] = $closure;
    }

    /**
     * Register listener methods for events.
     */
    public function registerListener(Listener $listener, CloudPlugin $plugin): void {
        $pluginName = $plugin->getDescription()->getFullName();
        $reflection = new ReflectionClass($listener);

        foreach ($reflection->getMethods(ReflectionMethod::IS_PUBLIC) as $method) {
            $params = $method->getParameters();
            if (count($params) === 1 && is_subclass_of($params[0]->getType()->getName(), Event::class)) {
                $event = $params[0]->getType()->getName();
                $this->handlers[$pluginName][$event][] = $method->getClosure($listener);
            }
        }
    }

    /**
     * Remove all event handlers for a plugin.
     */
    public function removeHandlers(CloudPlugin $plugin): void {
        $pluginName = $plugin->getDescription()->getFullName();
        unset($this->handlers[$pluginName]);
    }

    /**
     * Remove all event handlers globally.
     */
    public function removeAll(): void {
        $this->handlers = [];
    }

    /**
     * Call the event handlers for a specific event.
     */
    public function callEvent(Event $event): void {
        foreach ($this->handlers as $pluginHandlers) {
            foreach ($pluginHandlers[get_class($event)] ?? [] as $handler) {
                if ($handler != null) $handler($event);
            }
        }
    }

    public static function getInstance(): self {
        return self::$instance ??= new self;
    }
}