<?php

namespace bedrockcloud\event\impl\template;

use bedrockcloud\event\Event;
use bedrockcloud\template\Template;

abstract class TemplateEvent extends Event {

    public function __construct(private readonly Template $template) {}

    public function getTemplate(): Template {
        return $this->template;
    }
}