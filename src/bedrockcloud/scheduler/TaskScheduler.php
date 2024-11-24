<?php

namespace bedrockcloud\scheduler;

use bedrockcloud\plugin\CloudPlugin;
use bedrockcloud\util\Tickable;

final class TaskScheduler implements Tickable {

    /** @var array<int, TaskHandler> */
    private array $tasks = [];

    public function __construct(private readonly CloudPlugin $owner) {}

    /**
     * @param Task $task
     * @param int $delay
     * @param int $period
     * @param bool $repeat
     */
    private function scheduleTask(Task $task, int $delay, int $period, bool $repeat): void {
        $taskHandler = new TaskHandler($task, $delay, $period, $repeat, $this->owner);
        $task->setTaskHandler($taskHandler);
        $this->tasks[$taskHandler->getId()] = $taskHandler;
    }

    public function scheduleDelayedTask(Task $task, int $delay): void {
        $this->scheduleTask($task, $delay, -1, false);
    }

    public function scheduleRepeatingTask(Task $task, int $period): void {
        $this->scheduleTask($task, -1, $period, true);
    }

    public function scheduleDelayedRepeatingTask(Task $task, int $delay, int $period): void {
        $this->scheduleTask($task, $delay, $period, true);
    }

    /**
     * @param Task $task
     */
    public function cancel(Task $task): void {
        $taskHandler = $task->getTaskHandler();
        if (isset($this->tasks[$taskHandler->getId()])) {
            $taskHandler->cancel();
            unset($this->tasks[$taskHandler->getId()]);
        }
    }

    public function cancelAll(): void {
        foreach ($this->tasks as $taskHandler) {
            $taskHandler->cancel();
        }
        $this->tasks = [];
    }

    /**
     * @param int $id
     * @return Task|null
     */
    public function getTaskById(int $id): ?Task {
        return $this->tasks[$id]->getTask() ?? null;
    }

    /**
     * @param int $currentTick
     */
    public function tick(int $currentTick): void {
        foreach ($this->tasks as $id => $taskHandler) {
            if ($taskHandler->isCancelled()) {
                unset($this->tasks[$id]);
                continue;
            }
            $taskHandler->onUpdate($currentTick);
        }
    }

    /**
     * @return array<int, TaskHandler>
     */
    public function getTasks(): array {
        return $this->tasks;
    }

    /**
     * @return CloudPlugin
     */
    public function getOwner(): CloudPlugin {
        return $this->owner;
    }
}