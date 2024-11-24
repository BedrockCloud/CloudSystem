<?php

namespace bedrockcloud\server\utils;

final class PortManager {

    /** @var array<int, bool> */
    private static array $usedPorts = [];

    public static function addPort(int $port): void {
        self::$usedPorts[$port] = true;
    }

    public static function removePort(int $port): void {
        unset(self::$usedPorts[$port]);
    }

    public static function getFreePort(): int {
        while (true) {
            $port = random_int(40000, 65535);

            if (!isset(self::$usedPorts[$port])) {
                $socket = socket_create(AF_INET, SOCK_DGRAM, SOL_UDP);

                if ($socket && @socket_bind($socket, "127.0.0.1", $port)) {
                    socket_close($socket);
                    return $port;
                }

                if ($socket) {
                    socket_close($socket);
                }
            }
        }
    }

    public static function getFreeProxyPort(): ?int {
        for ($port = 19132; $port < 20000; $port++) {
            if (!isset(self::$usedPorts[$port])) {
                return $port;
            }
        }

        return null;
    }
}