<?php

namespace bedrockcloud\update;

use Exception;
use JsonException;
use Phar;
use bedrockcloud\config\impl\DefaultConfig;
use bedrockcloud\language\Language;
use bedrockcloud\software\SoftwareManager;
use bedrockcloud\util\AsyncExecutor;
use bedrockcloud\util\CloudLogger;
use bedrockcloud\util\SingletonTrait;
use bedrockcloud\util\Utils;
use bedrockcloud\util\VersionInfo;

final class UpdateChecker {
    use SingletonTrait;

    private array $data = [];

    public function __construct() {
        self::setInstance($this);
    }

    public function check(): void {
        $this->checkCloud();
        $this->checkPlugin();
        $this->checkServerSoftware();
    }

    private function checkCloud(): void {
        AsyncExecutor::execute(function(): false|string|null {
            try {
                $ch = curl_init("https://api.github.com/repos/BedrockCloud/Cloud/releases/latest");
                curl_setopt_array($ch, [
                        CURLOPT_SSL_VERIFYPEER => false,
                        CURLOPT_SSL_VERIFYHOST => false,
                        CURLOPT_RETURNTRANSFER => true,
                        CURLOPT_HEADER => false,
                        CURLOPT_USERAGENT => "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1)"
                    ]
                );

                $result = curl_exec($ch);
                $data = json_decode($result, true, flags: JSON_THROW_ON_ERROR);
                if ($data === false || $data === null) {
                    return false;
                } else {
                    if (isset($data["message"]) && str_contains($data["message"], "API rate limit")) return null;
                    return $data["tag_name"] ?? false;
                }
            } catch (JsonException $e) {
                CloudLogger::get()->exception($e);
                return false;
            }
        }, function(null|string|false $result): void {
            if ($result === false) {
                CloudLogger::get()->error(Language::current()->translate("updatechecker.error.check"));
            } elseif ($result === null) {
                CloudLogger::get()->error(Language::current()->translate("updatechecker.error.ratelimit"));
            } else {
                $current = explode(".", UpdateChecker::getInstance()->getCurrentVersion());
                $latest = explode(".", $result);
                $outdated = false;
                $highVersion = false;

                $i = 0;
                foreach ($current as $number) {
                    if (intval($latest[$i]) > intval($number)) {
                        $outdated = true;
                        break;
                    } else if (intval($number) > intval($latest[$i])) {
                        $highVersion = !VersionInfo::BETA;
                        break;
                    }
                    $i++;
                }

                UpdateChecker::getInstance()->setData(["outdated" => $outdated, "newest_version" => $result]);

                if ($outdated) {
                    CloudLogger::get()->warn(Language::current()->translate("updatechecker.error.outdated", "https://github.com/BedrockCloud/CloudSystem/releases/latest"));
                    CloudLogger::get()->warn(Language::current()->translate("updatechecker.info.version"));
                    CloudLogger::get()->warn(Language::current()->translate("updatechecker.info.plugins"));
                } elseif ($highVersion) {
                    CloudLogger::get()->warn(Language::current()->translate("updatechecker.error.toohigh", "https://github.com/BedrockCloud/CloudSystem/releases/latest"));
                    CloudLogger::get()->warn(Language::current()->translate("updatechecker.info.version"));
                    CloudLogger::get()->warn(Language::current()->translate("updatechecker.info.plugins"));
                } else {
                    CloudLogger::get()->warn(Language::current()->translate("updatechecker.info.uptodate"));
                }
            }
        });
    }

    private function checkPlugin(): void {
        try {
            $downloadNewest = false;
            $ch = curl_init("https://api.github.com/repos/BedrockCloud/CloudBridge/releases/latest");
            curl_setopt_array($ch, [
                    CURLOPT_SSL_VERIFYPEER => false,
                    CURLOPT_SSL_VERIFYHOST => false,
                    CURLOPT_RETURNTRANSFER => true,
                    CURLOPT_HEADER => false,
                    CURLOPT_USERAGENT => "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1)"
                ]
            );

            $result = curl_exec($ch);
            $data = json_decode($result, true, flags: JSON_THROW_ON_ERROR);
            if (is_array($data) && isset($data["tag_name"])) {
                $phar = new Phar(SERVER_PLUGINS_PATH . "CloudBridge.phar");
                if (isset($phar["plugin.yml"])) {
                    $yaml = yaml_parse($phar["plugin.yml"]->getContent());
                    if (isset($yaml["version"]) && $yaml["version"] !== $data["tag_name"]) {
                        CloudLogger::get()->warn(Language::current()->translate("updatechecker.cloudbridge.outdated"));

                        if (DefaultConfig::getInstance()->isExecuteUpdates()) {
                            CloudLogger::get()->warn(Language::current()->translate("updatechecker.download.latest"));
                            $downloadNewest = true;
                        } else {
                            CloudLogger::get()->warn(Language::current()->translate("updatechecker.info.download", "https://github.com/BedrockCloud/CloudBridge/releases/latest"));
                        }
                    }
                }
            }

            if ($downloadNewest) {
                @unlink(SERVER_PLUGINS_PATH . "CloudBridge.phar");
                Utils::downloadFiles();
            }
        } catch (Exception $e) {
            CloudLogger::get()->exception($e);
        }
    }

    private function checkServerSoftware(): void {
        try {
            $downloadNewest = false;
            $ch = curl_init("https://update.pmmp.io/api");
            curl_setopt_array($ch, [
                    CURLOPT_SSL_VERIFYPEER => false,
                    CURLOPT_SSL_VERIFYHOST => false,
                    CURLOPT_RETURNTRANSFER => true,
                    CURLOPT_HEADER => false,
                    CURLOPT_USERAGENT => "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1)"
                ]
            );

            $result = curl_exec($ch);
            $data = json_decode($result, true, flags: JSON_THROW_ON_ERROR);
            $currentGitCommit = $data["git_commit"];
            $pharGitCommit = str_repeat("00", 20);
            if (isset(($phar = new Phar(SOFTWARE_PATH . "PocketMine-MP.phar"))->getMetadata()["git"])) {
                $pharGitCommit = $phar->getMetadata()["git"];
            }

            if ($currentGitCommit !== $pharGitCommit) {
                CloudLogger::get()->warn(Language::current()->translate("updatechecker.pocketmine.outdated"));

                if (DefaultConfig::getInstance()->isExecuteUpdates()) {
                    CloudLogger::get()->warn(Language::current()->translate("updatechecker.download.latest"));
                    $downloadNewest = true;
                } else {
                    CloudLogger::get()->warn(Language::current()->translate("updatechecker.info.download", "https://github.com/pmmp/PocketMine-MP/releases/latest"));
                }
            }

            if ($downloadNewest) {
                SoftwareManager::getInstance()->downloadSoftware(SoftwareManager::getInstance()->getSoftwareByName("PocketMine-MP"));
            }
        } catch (Exception $e) {
            CloudLogger::get()->exception($e);
        }
    }

    public function isOutdated(): ?bool {
        return $this->data["outdated"] ?? null;
    }

    public function isUpToDate(): bool {
        return !$this->isOutdated();
    }

    public function getNewestVersion(): ?string {
        return $this->data["newest_version"] ?? null;
    }

    public function getCurrentVersion(): string {
        return VersionInfo::VERSION;
    }

    public function setData(array $data): void {
        $this->data = $data;
    }

    public function getData(): array {
        return $this->data;
    }

    public static function getInstance(): self {
        return self::$instance ??= new self;
    }
}