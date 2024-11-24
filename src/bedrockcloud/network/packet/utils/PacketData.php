<?php

namespace bedrockcloud\network\packet\utils;

use JsonSerializable;
use bedrockcloud\network\packet\impl\types\CommandExecutionResult;
use bedrockcloud\network\packet\impl\types\DisconnectReason;
use bedrockcloud\network\packet\impl\types\ErrorReason;
use bedrockcloud\network\packet\impl\types\LogType;
use bedrockcloud\network\packet\impl\types\TextType;
use bedrockcloud\network\packet\impl\types\VerifyStatus;
use bedrockcloud\player\CloudPlayer;
use bedrockcloud\server\CloudServer;
use bedrockcloud\server\status\ServerStatus;
use bedrockcloud\template\Template;

final class PacketData implements JsonSerializable {

    private array $data;

    public function __construct(array $data = []) {
        $this->data = $data;
    }

    public function write(mixed $value): self {
        $this->data[] = $value;
        return $this;
    }

    public function writeServer(CloudServer $server): self {
        return $this->write($server->toArray());
    }

    public function writeCommandExecutionResult(CommandExecutionResult $result): self {
        return $this->write($result->toArray());
    }

    public function writeLogType(LogType $logType): self {
        return $this->write($logType->getName());
    }

    public function writeServerStatus(ServerStatus $status): self {
        return $this->write($status->getName());
    }

    public function writeTemplate(Template $template): self {
        return $this->write($template->toArray());
    }

    public function writePlayer(CloudPlayer $player): self {
        return $this->write($player->toArray());
    }

    public function writeDisconnectReason(DisconnectReason $disconnectReason): self {
        return $this->write($disconnectReason->getName());
    }

    public function writeErrorReason(ErrorReason $errorReason): self {
        return $this->write($errorReason->getName());
    }

    public function writeVerifyStatus(VerifyStatus $verifyStatus): self {
        return $this->write($verifyStatus->getName());
    }

    public function writeTextType(TextType $textType): self {
        return $this->write($textType->getName());
    }

    public function read(): mixed {
        return array_shift($this->data);
    }

    public function readString(): ?string {
        $value = $this->read();
        return is_string($value) ? $value : null;
    }

    public function readInt(): ?int {
        $value = $this->read();
        return is_numeric($value) ? (int) $value : null;
    }

    public function readFloat(): ?float {
        $value = $this->read();
        return is_numeric($value) ? (float) $value : null;
    }

    public function readBool(): ?bool {
        $value = $this->read();
        return is_bool($value) ? $value : filter_var($value, FILTER_VALIDATE_BOOLEAN, FILTER_NULL_ON_FAILURE);
    }

    public function readArray(): ?array {
        $value = $this->read();
        return is_array($value) ? $value : null;
    }

    public function readServer(): ?CloudServer {
        $data = $this->readArray();
        return $data ? CloudServer::fromArray($data) : null;
    }

    public function readCommandExecutionResult(): ?CommandExecutionResult {
        $data = $this->readArray();
        return $data ? CommandExecutionResult::fromArray($data) : null;
    }

    public function readLogType(): ?LogType {
        $name = $this->readString();
        return $name ? LogType::getTypeByName($name) : null;
    }

    public function readServerStatus(): ?ServerStatus {
        $name = $this->readString();
        return $name ? ServerStatus::getServerStatusByName($name) : null;
    }

    public function readTemplate(): ?Template {
        $data = $this->readArray();
        return $data ? Template::fromArray($data) : null;
    }

    public function readPlayer(): ?CloudPlayer {
        $data = $this->readArray();
        return $data ? CloudPlayer::fromArray($data) : null;
    }

    public function readDisconnectReason(): ?DisconnectReason {
        $name = $this->readString();
        return $name ? DisconnectReason::getReasonByName($name) : null;
    }

    public function readErrorReason(): ?ErrorReason {
        $name = $this->readString();
        return $name ? ErrorReason::getReasonByName($name) : null;
    }

    public function readVerifyStatus(): ?VerifyStatus {
        $name = $this->readString();
        return $name ? VerifyStatus::getStatusByName($name) : null;
    }

    public function readTextType(): ?TextType {
        $name = $this->readString();
        return $name ? TextType::getTypeByName($name) : null;
    }

    public function jsonSerialize(): array {
        return $this->data;
    }
}