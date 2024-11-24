<?php

namespace bedrockcloud\scheduler;

use pmmp\thread\Thread;
use bedrockcloud\config\impl\DefaultConfig;
use bedrockcloud\util\SingletonTrait;
use bedrockcloud\util\Tickable;
use pocketmine\snooze\SleeperHandler;
use SplQueue;

final class AsyncPool implements Tickable {
    use SingletonTrait;

    private int $size = 10;
    private SleeperHandler $eventLoop;

    /** @var array<int, SplQueue<AsyncTask>> */
    private array $taskQueues = [];
    /** @var array<int, AsyncWorker> */
    private array $workers = [];
    /** @var array<int, int> */
    private array $workerLastUsed = [];

    public function __construct() {
        self::setInstance($this);
        $this->eventLoop = new SleeperHandler();
    }

    public function increaseSize(int $newSize): void {
        if ($newSize > $this->size) {
            $this->size = $newSize;
        }
    }

    public function getSize(): int {
        return $this->size;
    }

    public function getRunningWorkers(): array {
        return array_keys($this->workers);
    }

    private function getOrCreateWorker(int $worker): AsyncWorker {
        if (!isset($this->workers[$worker])) {
            $notifier = $this->eventLoop->addNotifier(fn() => $this->collectTasksFromWorker($worker));
            $this->workers[$worker] = $asyncWorker = new AsyncWorker(
                $worker,
                DefaultConfig::getInstance()->getMemoryLimit(),
                $notifier
            );
            $asyncWorker->start(Thread::INHERIT_INI);
            $this->taskQueues[$worker] = new SplQueue();
        }
        return $this->workers[$worker];
    }

    public function submitTaskToWorker(AsyncTask $task, int $worker): void {
        if ($worker < 0 || $worker >= $this->size || $task->isSubmitted()) {
            return;
        }

        $task->setSubmitted(true);
        $this->getOrCreateWorker($worker)->stack($task);
        $this->taskQueues[$worker]->enqueue($task);
        $this->workerLastUsed[$worker] = time();
    }

    private function selectWorker(): int {
        $leastUsedWorker = null;
        $minTasks = PHP_INT_MAX;

        foreach ($this->taskQueues as $workerId => $queue) {
            $taskCount = $queue->count();
            if ($taskCount < $minTasks) {
                $leastUsedWorker = $workerId;
                $minTasks = $taskCount;
                if ($taskCount === 0) {
                    break;
                }
            }
        }

        if ($leastUsedWorker === null || ($minTasks > 0 && count($this->workers) < $this->size)) {
            for ($i = 0; $i < $this->size; $i++) {
                if (!isset($this->workers[$i])) {
                    $leastUsedWorker = $i;
                    break;
                }
            }
        }

        assert($leastUsedWorker !== null);
        return $leastUsedWorker;
    }

    public function submitTask(AsyncTask $task): int {
        if ($task->isSubmitted()) {
            return -1;
        }

        $worker = $this->selectWorker();
        $this->submitTaskToWorker($task, $worker);
        return $worker;
    }

    private function collectTasksFromWorker(int $worker): bool {
        if (!isset($this->taskQueues[$worker])) {
            return false;
        }

        $queue = $this->taskQueues[$worker];
        $hasPendingTasks = false;

        while (!$queue->isEmpty()) {
            /** @var AsyncTask $task */
            $task = $queue->bottom();

            if ($task->isDone()) {
                $queue->dequeue();
                $task->onCompletion();
            } else {
                $hasPendingTasks = true;
                break;
            }
        }

        $this->workers[$worker]->collect();
        return $hasPendingTasks;
    }

    public function collectTasks(): bool {
        $hasPendingTasks = false;

        foreach ($this->taskQueues as $workerId => $queue) {
            if ($this->collectTasksFromWorker($workerId)) {
                $hasPendingTasks = true;
            }
        }

        return $hasPendingTasks;
    }

    public function getTaskQueueSizes(): array {
        return array_map(fn(SplQueue $queue) => $queue->count(), $this->taskQueues);
    }

    public function shutdownUnusedWorkers(): int {
        $shutdownCount = 0;
        $currentTime = time();

        foreach ($this->taskQueues as $workerId => $queue) {
            if ((!isset($this->workerLastUsed[$workerId]) || $this->workerLastUsed[$workerId] + 300 < $currentTime) && $queue->isEmpty()) {
                $this->workers[$workerId]->quit();
                $this->eventLoop->removeNotifier($this->workers[$workerId]->getEntry()->getNotifierId());
                unset($this->workers[$workerId], $this->taskQueues[$workerId], $this->workerLastUsed[$workerId]);
                $shutdownCount++;
            }
        }

        return $shutdownCount;
    }

    public function tick(int $currentTick): void {
        $this->collectTasks();
    }

    public function shutdown(): void {
        while ($this->collectTasks()) {}
        foreach ($this->workers as $worker) {
            $worker->quit();
            $this->eventLoop->removeNotifier($worker->getEntry()->getNotifierId());
        }
        $this->workers = [];
        $this->taskQueues = [];
        $this->workerLastUsed = [];
    }

    public static function getInstance(): self {
        return self::$instance ??= new self;
    }
}