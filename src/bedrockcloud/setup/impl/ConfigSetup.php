<?php

namespace bedrockcloud\setup\impl;

use bedrockcloud\config\impl\DefaultConfig;
use bedrockcloud\language\Language;
use bedrockcloud\setup\QuestionBuilder;
use bedrockcloud\setup\Setup;
use bedrockcloud\util\CloudLogger;
use bedrockcloud\util\Utils;

final class ConfigSetup extends Setup {

    public function onStart(): void {
        CloudLogger::get()->info("Welcome to the §6Bedrock§eCloud§r-Setup!");
    }

    public function onCancel(): void {
        CloudLogger::get()->warn(Language::current()->translate("setup.default.cancelled"));
        $this->handleResults([]);
    }

    public function applyQuestions(): array {
        return [
            QuestionBuilder::builder()
                ->key("language")
                ->question("What language do you wanna use?")
                ->canSkipped(true)
                ->parser(fn(string $input) => $input)
                ->possibleAnswers("German", "English")
                ->default("English")
                ->recommendation("English")
                ->resultHandler(fn(string $result) => DefaultConfig::getInstance()->setLanguage($result))
            ->build(),
            QuestionBuilder::builder()
                ->key("networkPort")
                ->question("setup.default.question.cloud_port")
                ->canSkipped(true)
                ->default(3656)
                ->parser(function(string $input): ?int {
                    if (!is_numeric($input)) return null;
                    return intval($input);
                })
            ->build(),
            QuestionBuilder::builder()
                ->key("memoryLimit")
                ->question("setup.default.question.memory_limit")
                ->recommendation("512")
                ->default("512")
                ->canSkipped(true)
                ->parser(function(string $input): ?int {
                    if (!is_numeric($input)) return null;
                    return intval($input);
                })
            ->build(),
            QuestionBuilder::builder()
                ->key("debugMode")
                ->question("setup.default.question.debug_mode")
                ->canSkipped(true)
                ->default("yes")
                ->parser(fn(string $input) => strtolower($input) == "yes")
                ->possibleAnswers("yes", "no")
                ->recommendation("yes")
            ->build(),
            QuestionBuilder::builder()
                ->key("updateChecks")
                ->question("setup.default.question.update_checks")
                ->canSkipped(true)
                ->default("yes")
                ->parser(fn(string $input) => strtolower($input) == "yes")
                ->possibleAnswers("yes", "no")
                ->recommendation("yes")
            ->build(),
            QuestionBuilder::builder()
                ->key("executeUpdates")
                ->question("setup.default.question.execute_updates")
                ->canSkipped(true)
                ->default("yes")
                ->parser(fn(string $input) => strtolower($input) == "yes")
                ->possibleAnswers("yes", "no")
                ->recommendation("yes")
                ->build(),
            QuestionBuilder::builder()
                ->key("startMethod")
                ->question("setup.default.question.start_method")
                ->canSkipped(true)
                ->default((Utils::isTmuxInstalled() ? "tmux" : "screen"))
                ->parser(fn(string $input) => $input)
                ->possibleAnswers("tmux", "screen")
                ->recommendation((Utils::isTmuxInstalled() ? "tmux" : "screen"))
            ->build(),
            QuestionBuilder::builder()
                ->key("httpServerEnabled")
                ->question("setup.default.question.http_server")
                ->canSkipped(true)
                ->default("yes")
                ->parser(fn(string $input) => strtolower($input) == "true")
                ->possibleAnswers("yes", "no")
                ->recommendation("yes")
            ->build(),
            QuestionBuilder::builder()
                ->key("httpServerPort")
                ->question("setup.default.question.http_server_port")
                ->canSkipped(true)
                ->default("8000")
                ->parser(function(string $input): ?int {
                    if (!is_numeric($input)) return null;
                    return intval($input);
                })
            ->build(),
            QuestionBuilder::builder()
                ->key("defaultLobbyTemplate")
                ->question("setup.default.question.default_lobby")
                ->canSkipped(true)
                ->parser(fn(string $input) => strtolower($input) == "true")
                ->default("yes")
                ->possibleAnswers("yes", "no")
                ->recommendation("yes")
            ->build(),
            QuestionBuilder::builder()
                ->key("defaultProxyTemplate")
                ->question("setup.default.question.default_proxy")
                ->canSkipped(true)
                ->parser(fn(string $input) => strtolower($input) == "true")
                ->default("yes")
                ->possibleAnswers("yes", "no")
                ->recommendation("yes")
            ->build()
        ];
    }

    public function handleResults(array $results): void {
        DefaultConfig::getInstance()->setNetworkPort($results["networkPort"] ?? 3656);
        DefaultConfig::getInstance()->setMemoryLimit($results["memoryLimit"] ?? 512);
        DefaultConfig::getInstance()->setDebugMode($results["debugMode"] ?? true);
        DefaultConfig::getInstance()->setStartMethod($results["startMethod"] ?? (Utils::isTmuxInstalled() ? "tmux" : "screen"));
        DefaultConfig::getInstance()->setHttpServerEnabled($results["httpServerEnabled"] ?? true);
        DefaultConfig::getInstance()->setHttpServerPort($results["httpServerPort"] ?? 8000);
        DefaultConfig::getInstance()->save();
    }
}