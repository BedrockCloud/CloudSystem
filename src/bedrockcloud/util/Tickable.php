<?php

namespace bedrockcloud\util;

interface Tickable {

    public function tick(int $currentTick): void;
}