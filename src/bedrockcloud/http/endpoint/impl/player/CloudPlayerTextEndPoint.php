<?php

namespace bedrockcloud\http\endpoint\impl\player;

use bedrockcloud\http\io\Request;
use bedrockcloud\http\io\Response;
use bedrockcloud\http\util\Router;
use bedrockcloud\network\packet\impl\types\TextType;
use bedrockcloud\player\CloudPlayerManager;
use bedrockcloud\http\endpoint\EndPoint;

class CloudPlayerTextEndPoint extends EndPoint {

    public function __construct() {
        parent::__construct(Router::POST, "/player/text/");
    }

    public function handleRequest(Request $request, Response $response): array {
        $identifier = $request->data()->queries()->get("identifier");
        $player = CloudPlayerManager::getInstance()->getPlayerByName($identifier) ?? CloudPlayerManager::getInstance()->getPlayerByUniqueId($identifier) ?? CloudPlayerManager::getInstance()->getPlayerByXboxUserId($identifier);
        if ($player === null) {
            return ["error" => "Player is not online!"];
        }

        $textType = TextType::getTypeByName($request->data()->queries()->get("text_type", "MESSAGE"));
        $text = $request->data()->queries()->get("text");

        switch ($textType) {
            case TextType::TITLE(): {
                $player->sendTitle($text);
                break;
            }
            case TextType::POPUP(): {
                $player->sendPopup($text);
                break;
            }
            case TextType::TIP(): {
                $player->sendTip($text);
                break;
            }
            case TextType::ACTION_BAR(): {
                $player->sendActionBarMessage($text);
                break;
            }
            default: {
                $player->sendMessage($text);
                break;
            }
        }

        return ["success" => "Text was successfully sent to the player!"];
    }

    public function isBadRequest(Request $request): bool {
        if ($request->data()->queries()->has("identifier") && $request->data()->queries()->has("text_type") && $request->data()->queries()->has("text")) return false;
        return true;
    }
}