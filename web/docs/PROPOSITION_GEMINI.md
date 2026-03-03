# Proposition d’intégration de Gemini dans DocBook

## 1. Où l’ajouter (logique métier)

| Emplacement | Rôle | Intérêt |
|-------------|------|--------|
| **Fiche dossier (show)** | Résumé automatique du dossier + documents | Gain de temps pour le médecin, vue synthétique |
| **Création / édition de document** | Aide à la rédaction (formulations, complétion) | Meilleure qualité des comptes rendus |
| **Recherche** | Recherche sémantique (par sens, pas seulement mots-clés) | Trouver des dossiers par thème (ex. « allergies », « antécédents cardiaques ») |
| **Dashboard admin** | Synthèse globale, alertes (ex. dossiers sans document récent) | Pilotage et suivi |
| **Export PDF** | Résumé en en-tête ou encadré « Synthèse IA » | PDF plus utile pour transmission |

Proposition pour le projet : **commencer par la fiche dossier (résumé IA)** puis, si le temps le permet, **aide à la rédaction** sur les documents.

---

## 2. Scénarios proposés (convaincants pour le prof)

### Scénario 1 – Résumé automatique de dossier (prioritaire)
- **Quoi :** Sur la fiche d’un dossier médical, un bouton « Résumer avec l’IA » envoie à Gemini les infos du dossier + les contenus des documents.
- **Prompt :** « Tu es un assistant médical. À partir des informations suivantes (dossier + documents), rédige un résumé synthétique en 2 à 4 phrases. Ne rien inventer, ne citer que les informations fournies. »
- **Où :** Page « Dossier » (Admin / Médecin) – bloc « Aide IA » avec le résumé affiché.
- **Valeur :** Montre l’usage d’une API de type LLM (Gemini), intégration propre (service dédié, clé en .env), et utilité métier (synthèse pour le médecin).

### Scénario 2 – Aide à la rédaction d’un document
- **Quoi :** Lors de la création/édition d’un document, un champ « Suggestion IA » : l’utilisateur envoie le début du contenu (ou le titre + type), Gemini propose une suite ou des formulations.
- **Où :** Formulaire document (Admin / Médecin).
- **Valeur :** Montre l’IA comme outil d’aide à la saisie, pas seulement en lecture.

### Scénario 3 – Recherche sémantique (optionnel)
- **Quoi :** En plus de la recherche par mot-clé, une recherche « par sens » (ex. « documents qui parlent d’allergies ou d’intolérances ») en envoyant la requête + extraits de documents à Gemini pour filtrage/pertinence.
- **Où :** Liste dossiers / documents.
- **Valeur :** Illustre le passage d’une recherche classique à une recherche « intelligente ».

### Scénario 4 – Synthèse dans l’export PDF (optionnel)
- **Quoi :** Lors de l’export PDF d’un dossier, appeler Gemini pour générer un encadré « Synthèse » en première page.
- **Où :** `PdfExportService` (ou contrôleur d’export).
- **Valeur :** Montre l’IA dans un flux d’export existant.

---

## 3. Confidentialité et cadre (pour le prof / rapport)

- **Données envoyées :** Les textes du dossier et des documents sont envoyés à l’API Google (Gemini) pour générer le résumé. En production santé, cela pose des questions de **secret médical** et **RGPD**.
- **Clé API :** À mettre dans `.env` ou `.env.local` (`GEMINI_API_KEY=...`). Ne pas commiter la clé en production ; utiliser `.env.local` (ignoré par git) ou un gestionnaire de secrets.
- **Ce qu’on suppose pour le projet :**
  - Usage **pédagogique / démo** : pas de données réelles de patients, ou données anonymisées.
  - Clé API dans **`.env`** (jamais en dur dans le code), documentée dans le rapport.
- **Pour un vrai contexte médical :**
  - Préférer un modèle **hébergé en interne** (ou un fournisseur avec BAA / garanties santé).
  - Ou limiter l’envoi à des **extraits non identifiants** et avec **consentement** explicite.

Dans le rapport, on peut ajouter une phrase du type : *« L’intégration Gemini est réalisée à des fins de démonstration ; en environnement de production, la confidentialité des données de santé imposerait un hébergement maîtrisé ou des garanties contractuelles avec le fournisseur. »*

---

## 4. Implémentation technique (résumé)

- **Service dédié :** `App\Service\GeminiService` – appel à l’API Gemini (modèle `gemini-1.5-flash` ou `gemini-pro`) via HTTP.
- **Configuration :** `GEMINI_API_KEY` dans `.env` (et `.env.example` documenté).
- **Premier scénario livré :** Résumé de dossier sur la fiche dossier (Admin), appel en AJAX ou au chargement, affichage dans une carte « Résumé IA (Gemini) ».

---

## 5. Récap pour la soutenance / le prof

- **Où :** Fiche dossier (et éventuellement formulaire document, recherche, PDF).
- **Scénarios :** (1) Résumé automatique du dossier, (2) Aide à la rédaction, (3) Recherche sémantique, (4) Synthèse dans le PDF.
- **Confidentialité :** Démo pédagogique ; en production, nécessité de traiter la donnée santé selon la réglementation (RGPD, secret médical, hébergement).
