<?php

namespace bedrockcloud\web;

enum WebAccountRoles: string {

    case ADMIN = "admin";
    case DEVELOPER = "developer";
    case DEFAULT = "default";

    public function roleName(): string {
        return $this->value;
    }
}