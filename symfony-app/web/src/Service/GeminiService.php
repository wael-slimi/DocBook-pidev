<?php

declare(strict_types=1);

namespace App\Service;

final class GeminiService
{
    private const BASE_URL = 'https://generativelanguage.googleapis.com/v1beta/models';
    private const MODEL = 'gemini-2.5-flash';

    public function __construct(
        private readonly string $apiKey
    ) {
    }

    public function generate(string $prompt, int $maxOutputTokens = 1024): ?string
    {
        $key = trim($this->apiKey);
        if ($key === '' || $key === '0') {
            return null;
        }

        $url = sprintf('%s/%s:generateContent?key=%s', self::BASE_URL, self::MODEL, $key);
        $payload = [
            'contents' => [
                [
                    'parts' => [
                        ['text' => $prompt],
                    ],
                ],
            ],
            'generationConfig' => [
                'maxOutputTokens' => $maxOutputTokens,
                'temperature' => 0.3,
            ],
        ];

        try {
            $json = json_encode($payload, JSON_THROW_ON_ERROR);
            $context = stream_context_create([
                'http' => [
                    'method' => 'POST',
                    'header' => "Content-Type: application/json\r\n",
                    'content' => $json,
                    'timeout' => 30,
                ],
            ]);
            $response = @file_get_contents($url, false, $context);
            if ($response === false) {
                return null;
            }
            $data = json_decode($response, true, 512, JSON_THROW_ON_ERROR);
        } catch (\Throwable $e) {
            return null;
        }

        $text = $data['candidates'][0]['content']['parts'][0]['text'] ?? null;
        return $text !== null ? trim($text) : null;
    }

    public function summarizeDossier(string $dossierAndDocumentsText): ?string
    {
        $prompt = <<<PROMPT
Tu es un assistant médical professionnel. À partir des informations de dossier médical ci-dessous, rédige un **résumé détaillé et structuré** en français, d'environ 150 à 250 mots. Ne rien inventer : utilise uniquement les informations fournies. Sois factuel, clair et professionnel.

Structure attendue du résumé :
1. **Identité et situation du patient** : nom, prénom, âge/date de naissance si disponible, genre, coordonnées utiles (email, téléphone, adresse) si mentionnées.
2. **Synthèse du dossier** : numéro de dossier, date de création, remarques éventuelles.
3. **Documents et contenus** : pour chaque document (ordonnance, rapport, examen, compte rendu, etc.), indique brièvement le type, la date et les éléments importants du contenu (traitements, conclusions, recommandations). Ne pas tout recopier : extraire les points clés.
4. **Points d'attention** : antécédents, allergies, traitements en cours ou éléments à retenir pour la prise en charge, s'ils apparaissent dans les données.

Si une section n'a pas d'information disponible, tu peux l'omettre ou indiquer « Non renseigné ». Réponds uniquement par le résumé, sans introduction ni conclusion métier du type « Ce résumé a été généré par… ».

Informations du dossier et des documents :
---
%s
---
Résumé détaillé :
PROMPT;

        $prompt = sprintf($prompt, $dossierAndDocumentsText);
        return $this->generate($prompt, 1536);
    }
}
