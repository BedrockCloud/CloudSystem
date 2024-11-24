<?php

namespace bedrockcloud\util;

use Closure;
use bedrockcloud\scheduler\AsyncClosureTask;
use bedrockcloud\scheduler\AsyncPool;
use bedrockcloud\scheduler\AsyncTask;

final class AsyncExecutor {

    public static function execute(Closure $asyncClosure, ?Closure $syncClosure = null, mixed ...$args): void {
        AsyncPool::getInstance()->submitTask(new AsyncClosureTask(fn(AsyncTask $task) => ($asyncClosure)(), function(mixed $result) use($syncClosure, $args): void {
            if ($syncClosure !== null) $syncClosure($result, ...$args);
        }));
    }
}