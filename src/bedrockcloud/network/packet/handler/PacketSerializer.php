<?php

namespace bedrockcloud\network\packet\handler;

use Exception;
use bedrockcloud\config\impl\DefaultConfig;
use bedrockcloud\network\packet\CloudPacket;
use bedrockcloud\network\packet\pool\PacketPool;
use bedrockcloud\network\packet\utils\PacketData;
use bedrockcloud\util\CloudLogger;
use bedrockcloud\util\ExceptionHandler;
use ReflectionClass;

final class PacketSerializer {

    public static function encode(CloudPacket $packet): string {
        try {
            $packet->encode($buffer = new PacketData());
            return DefaultConfig::getInstance()->isNetworkEncryptionEnabled() ? base64_encode(json_encode($buffer, JSON_THROW_ON_ERROR)) : json_encode($buffer, JSON_THROW_ON_ERROR);
        } catch (Exception $exception) {
            CloudLogger::get()->error("§cFailed to encode packet: §e" . (new ReflectionClass($packet))->getShortName());
            CloudLogger::get()->exception($exception);
        }
        return "";
    }

    public static function decode(string $buffer): ?CloudPacket {
        if (trim($buffer) == "") return null;
        $data = ExceptionHandler::tryCatch(fn() => json_decode((DefaultConfig::getInstance()->isNetworkEncryptionEnabled() ? base64_decode($buffer) : $buffer),  true, flags: JSON_THROW_ON_ERROR), "Failed to decode packet", onExceptionClosure: fn() => CloudLogger::get()->debug("Buffer: " . $buffer));
        if (is_array($data)) {
            if (isset($data[0])) {
                if (($packet = PacketPool::getInstance()->getPacketById($data[0])) !== null) {
                    $packet->decode(new PacketData($data));
                    return $packet;
                }
            }
        }
        return null;
    }
}