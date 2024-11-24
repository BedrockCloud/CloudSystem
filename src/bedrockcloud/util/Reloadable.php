<?php

namespace bedrockcloud\util;

interface Reloadable {

    public function reload(): bool;
}