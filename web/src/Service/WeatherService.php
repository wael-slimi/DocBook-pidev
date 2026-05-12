<?php

namespace App\Service;

use Symfony\Contracts\HttpClient\HttpClientInterface;

/**
 * Fetches current weather from Open-Meteo API for health & weather awareness.
 * Uses a short in-memory cache to avoid calling the API on every request.
 */
class WeatherService
{
    private const API_URL = 'https://api.open-meteo.com/v1/forecast?latitude=36.8&longitude=10.1&current_weather=true';
    private const CACHE_TTL_SECONDS = 300; // 5 minutes

    private static ?array $cached = null;
    private static ?float $cachedAt = null;

    public function __construct(
        private readonly HttpClientInterface $httpClient
    ) {
    }

    /**
     * Returns current weather data or null on failure.
     *
     * @return array{temperature: float, weathercode: int, windspeed: float}|null
     */
    public function getCurrentWeather(): ?array
    {
        $now = microtime(true);
        if (self::$cached !== null && self::$cachedAt !== null && ($now - self::$cachedAt) < self::CACHE_TTL_SECONDS) {
            return self::$cached;
        }

        try {
            $response = $this->httpClient->request('GET', self::API_URL, [
                'timeout' => 2,
                'max_duration' => 2,
            ]);
            $data = $response->toArray();
            $current = $data['current_weather'] ?? null;
            if ($current === null) {
                self::$cached = null;
                self::$cachedAt = $now;
                return null;
            }
            $result = [
                'temperature' => (float) ($current['temperature'] ?? 0),
                'weathercode' => (int) ($current['weathercode'] ?? 0),
                'windspeed' => (float) ($current['windspeed'] ?? 0),
            ];
            self::$cached = $result;
            self::$cachedAt = $now;
            return $result;
        } catch (\Throwable $e) {
            self::$cached = null;
            self::$cachedAt = $now; // cache failure so we don't retry every request
            return null;
        }
    }
}
