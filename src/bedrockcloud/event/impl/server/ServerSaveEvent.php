<?php

namespace bedrockcloud\event\impl\server;

use bedrockcloud\event\Cancelable;
use bedrockcloud\event\CancelableTrait;

class ServerSaveEvent extends ServerEvent implements Cancelable {
    use CancelableTrait;
}