<?php

namespace App\Twig;

use App\Service\WeatherService;
use Twig\Extension\AbstractExtension;
use Twig\Extension\GlobalsInterface;

class WeatherExtension extends AbstractExtension implements GlobalsInterface
{
    public function __construct(
        private readonly WeatherService $weatherService
    ) {
    }

    public function getGlobals(): array
    {
        try {
            $weather = $this->weatherService->getCurrentWeather();
        } catch (\Throwable $e) {
            $weather = null;
        }
        return [
            'current_weather' => $weather,
        ];
    }
}
