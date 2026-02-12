# Documentation d'implémentation — Gestion des patients (Dossiers médicaux & Documents)

Ce document détaille **pas à pas** la mise en place de la gestion des **dossiers médicaux** et des **documents** dans l'application DocBook. Il suit la même logique que la documentation de référence fournie en exemple et s'adresse à un profil débutant.

---

## Sommaire

1. [Contexte et stack technique](#1-contexte-et-stack-technique)
2. [Création des modèles (entités)](#2-création-des-modèles-entités)
3. [Migrations et base de données](#3-migrations-et-base-de-données)
4. [Repositories](#4-repositories)
5. [Formulaires (Form Types)](#5-formulaires-form-types)
6. [Controllers — Dossiers médicaux (CRUD)](#6-controllers--dossiers-médicaux-crud)
7. [Controllers — Documents (CRUD)](#7-controllers--documents-crud)
8. [Controllers par acteur (Admin, Médecin, Patient)](#8-controllers-par-acteur-admin-médecin-patient)
9. [Templates (vues)](#9-templates-vues)
10. [Tri, filtres et validation](#10-tri-filtres-et-validation)
11. [Recherche et filtres par AJAX](#11-recherche-et-filtres-par-ajax)
12. [Accueil et navigation](#12-accueil-et-navigation)
13. [Résumé des routes par acteur](#13-résumé-des-routes-par-acteur)
14. [Résumé des commandes et bonnes pratiques](#14-résumé-des-commandes-et-bonnes-pratiques)

---

## 1. Contexte et stack technique

- **Framework** : Symfony 6.4 (PHP 8.1+)
- **Base de données** : MySQL 8 (Doctrine ORM), connexion via `DATABASE_URL` dans `.env`
- **Front** : Twig + CSS intégré (pas de Bootstrap) + JavaScript vanilla pour l'AJAX
- **Bundles/Extensions principaux** :
  - `doctrine/doctrine-bundle` et `doctrine/doctrine-migrations-bundle`
  - `symfony/form`, `symfony/validator`, `symfony/twig-bundle`
  - Pas d'authentification ni de sécurité CSRF : toutes les routes sont accessibles sans restriction

L'application gère trois **acteurs** (Admin, Médecin, Patient) qui accèdent aux mêmes entités `DossierMedical` et `Document` via un **préfixe d'URL** propre :

- **Admin** : `localhost:8000/admin/`
- **Médecin** : `localhost:8000/medecin/`
- **Patient** : `localhost:8000/patient/`

Aucune gestion des utilisateurs n'est implémentée : le rôle est déduit uniquement du chemin d'accès. Un service `RoleAccessService` était prévu pour restreindre certaines actions (ex. médecin en lecture seule sur les dossiers) ; dans la version finale, toutes les actions (création, modification, suppression) sont autorisées pour tous les acteurs.

---

## 2. Création des modèles (entités)

### 2.1 Entité `DossierMedical` (`src/Entity/DossierMedical.php`)

- **Rôle** : représente le dossier médical d'un patient (identité, coordonnées, remarques).
- **Champs principaux** :
  - `numeroDossier` (string, unique) — numéro du dossier
  - `patientNom`, `patientPrenom` (string) — nom et prénom du patient
  - `dateNaissance` (date, nullable)
  - `genre` (string, nullable) — valeurs : `M`, `F`, `Autre`
  - `email`, `telephone` (string, nullable)
  - `adresse`, `remarques` (text, nullable)
  - `dateCreation` (datetime_immutable), `dateModification` (datetime, nullable)
- **Relations** :
  - `OneToMany` vers `Document` (`documents`), avec `cascade: ['persist', 'remove']` et `orphanRemoval: true`
  - Tri des documents par `dateDocument` et `dateCreation` (DESC) via `#[ORM\OrderBy]`
- **Validations (Assert, PHP uniquement)** :
  - `NotBlank` + `Length(min: 2, max: 50)` sur `numeroDossier`
  - `NotBlank` + `Length(min: 3, max: 120)` sur `patientNom` et `patientPrenom`
  - `NotNull` + `LessThan('today')` sur `dateNaissance`
  - `Choice(['M', 'F', 'Autre'])` sur `genre`
  - `Email` + `Length(max: 180)` sur `email`
  - `Regex` sur `telephone` (chiffres, espaces, +, -, parenthèses, 8 à 30 caractères)
  - `Length(max: 2000)` sur `adresse`, `Length(max: 5000)` sur `remarques`
- **Cycle de vie** : `#[ORM\PrePersist]` pour `dateCreation`, `#[ORM\PreUpdate]` pour `dateModification`

### 2.2 Entité `Document` (`src/Entity/Document.php`)

- **Rôle** : représente un document attaché à un dossier médical (ordonnance, rapport, examen, etc.).
- **Champs principaux** :
  - `dossierMedical` (ManyToOne obligatoire vers `DossierMedical`)
  - `titre` (string)
  - `typeDocument` (string) — constantes : `ordonnance`, `rapport`, `examen`, `compte_rendu`, `autre`
  - `dateDocument` (date)
  - `contenu` (text, nullable), `fichierPath` (string, nullable)
  - `dateCreation`, `dateModification`
- **Validations (Assert)** :
  - `NotBlank` + `Length(min: 2, max: 200)` sur `titre`
  - `NotBlank` + `Choice(choices: self::TYPES)` sur `typeDocument`
  - `NotNull` + `LessThanOrEqual('today')` sur `dateDocument`
  - `Length(max: 10000)` sur `contenu`, `Length(max: 500)` sur `fichierPath`
- **Relations** : `ManyToOne` vers `DossierMedical` avec `onDelete: 'CASCADE'`
- **Cycle de vie** : `PrePersist` / `PreUpdate` pour les dates

Les messages d'erreur de validation sont affichés **uniquement via PHP/Twig** (blocs `form.vars.errors`, `form.champ.vars.errors`) ; les formulaires ont `novalidate="novalidate"` pour désactiver la validation HTML.

---

## 3. Migrations et base de données

1. **Fichiers de migration** (dossier `migrations/`) :
   - `Version20260203124358.php` : table `user` (entité existante du projet)
   - `Version20260211160000.php` : tables `dossier_medical` et `document`

2. **Structure des tables** :
   - **dossier_medical** : colonnes en snake_case (`numero_dossier`, `patient_nom`, `date_creation`, etc.), index unique sur `numero_dossier`, charset `utf8mb4`.
   - **document** : clé étrangère `dossier_medical_id` vers `dossier_medical(id)` avec `ON DELETE CASCADE`, index sur `dossier_medical_id`.

3. **Commandes** :
   ```bash
   php bin/console doctrine:database:create --if-not-exists
   php bin/console doctrine:migrations:migrate --no-interaction
   php bin/console doctrine:migrations:status
   ```

La configuration Doctrine (`config/packages/doctrine.yaml`) utilise `naming_strategy: underscore_number_aware` et l’URL de connexion est lue depuis `DATABASE_URL` dans `.env` (MySQL, sans mot de passe en local si besoin).

---

## 4. Repositories

### 4.1 `DossierMedicalRepository` (`src/Repository/DossierMedicalRepository.php`)

- **Méthodes principales** :
  - `searchAndFilter($search, $tri, $ordre, $dateDebut, $dateFin, $genre, $limit, $offset)` : retourne une liste de `DossierMedical` filtrée et triée.
    - Recherche texte (`q`) dans : `numeroDossier`, `patientNom`, `patientPrenom`, `email` (LIKE).
    - Filtres : `dateDebut` / `dateFin` sur `dateCreation`, `genre`.
    - Tri : champs autorisés `dateCreation`, `dateModification`, `patientNom`, `patientPrenom`, `numeroDossier` ; ordre `ASC` ou `DESC`.
  - `countSearchAndFilter($search, $dateDebut, $dateFin, $genre)` : compte le nombre de résultats (pour pagination ou affichage du total).
  - `save(DossierMedical $entity, bool $flush)`, `remove(DossierMedical $entity, bool $flush)` : persistance et suppression.

### 4.2 `DocumentRepository` (`src/Repository/DocumentRepository.php`)

- **Méthodes principales** :
  - `searchAndFilterByDossier(DossierMedical $dossier, $search, $tri, $ordre, $typeDocument, $dateDebut, $dateFin, $limit, $offset)` : liste de documents pour un dossier donné.
    - Recherche dans `titre` et `contenu`.
    - Filtres : `typeDocument`, `dateDebut` / `dateFin` sur `dateDocument`.
    - Tri : `dateDocument`, `dateCreation`, `titre`, `typeDocument`.
  - `countSearchAndFilterByDossier(...)` : compte des documents pour le même jeu de filtres.
  - `save` / `remove` : idem que pour les dossiers.

Les repositories sont injectés dans tous les controllers (Admin, Médecin, Patient) pour éviter la duplication de logique.

---

## 5. Formulaires (Form Types)

### 5.1 `DossierMedicalType` (`src/Form/DossierMedicalType.php`)

- Champs : `numeroDossier`, `patientNom`, `patientPrenom`, `dateNaissance` (DateType en `single_text`), `genre` (ChoiceType : M/F/Autre), `email` (EmailType), `telephone`, `adresse` (TextareaType), `remarques` (TextareaType).
- Les contraintes de validation viennent des **Assert** sur l’entité (pas de contraintes en HTML). Les erreurs sont affichées dans les templates via des blocs dédiés sous chaque `form_row`.

### 5.2 `DocumentType` (`src/Form/DocumentType.php`)

- Champs : `titre`, `typeDocument` (ChoiceType basé sur `Document::TYPES`), `dateDocument` (DateType), `contenu` (TextareaType), `fichierPath` (optionnel).
- Option `with_dossier` (bool) : si `true`, ajoute un champ `dossierMedical` (EntityType) pour choisir le dossier ; en création/édition depuis une fiche dossier, on utilise `with_dossier: false` et on assigne le dossier en PHP.

Les formulaires n’incluent pas de champ CSRF (protection CSRF désactivée au niveau du framework).

---

## 6. Controllers — Dossiers médicaux (CRUD)

Les dossiers sont gérés par trois ensembles de controllers selon l’acteur : Admin, Médecin, Patient. La structure est identique pour chacun ; seuls le préfixe de route et le namespace changent.

### 6.1 Listing (`index`)

- Récupération des paramètres : `q`, `tri`, `ordre`, `date_debut`, `date_fin`, `genre`.
- Conversion des dates (format `Y-m-d`) et appel à `DossierMedicalRepository::searchAndFilter` et `countSearchAndFilter`.
- **Réponse** :
  - Si requête AJAX (`X-Requested-With: XMLHttpRequest` ou `?ajax=1`) : rendu de la partial `_list_rows.html.twig` + en-tête HTTP `X-Total-Count` pour mettre à jour le compteur côté client.
  - Sinon : rendu de la page complète `index.html.twig` avec les mêmes variables (`dossiers`, `total`, `q`, `tri`, `ordre`, filtres, `can_edit_dossier`, `can_delete_dossier`).

### 6.2 Création (`new`)

- Instanciation d’un `DossierMedical`, création du formulaire `DossierMedicalType`, `handleRequest`.
- Si le formulaire est soumis et valide : `repository->save($dossier, true)`, message flash, redirection vers l’index des dossiers de l’acteur.

### 6.3 Affichage (`show`)

- Paramètre : `DossierMedical $dossier` (résolution par id). Rendu de `show.html.twig` avec le dossier et des liens vers l’édition et la liste des documents du dossier.

### 6.4 Édition (`edit`)

- Même schéma que la création avec l’entité existante. Redirection vers `show` du dossier après succès.

### 6.5 Suppression (`delete`)

- Méthode `POST`, paramètre `DossierMedical $dossier`. Aucune vérification CSRF (désactivée). Appel à `repository->remove($dossier, true)`, message flash, redirection vers l’index.

Les vues sont dans `templates/{admin|medecin|patient}/dossier_medical/`.

---

## 7. Controllers — Documents (CRUD)

Les documents sont toujours rattachés à un dossier ; les routes incluent donc `dossierId` dans le chemin.

### 7.1 Listing (`index`)

- Paramètres : `dossierId` (URL), puis `q`, `tri`, `ordre`, `type`, `date_debut`, `date_fin`.
- Récupération du dossier via `getDossier($dossierId)` (sinon 404). Appel à `DocumentRepository::searchAndFilterByDossier` et `countSearchAndFilterByDossier`.
- Réponse en page complète ou en partial AJAX + en-tête `X-Total-Count`, comme pour les dossiers.

### 7.2 Création (`new`)

- Récupération du dossier ; création d’un `Document`, affectation du `dossierMedical`, formulaire `DocumentType` avec `with_dossier: false`. Après validation : `persist` + `flush`, flash, redirection vers l’index des documents du dossier.

### 7.3 Affichage (`show`) et Édition (`edit`)

- Vérification que le document appartient bien au dossier (`dossierId`). Sinon 404. Édition : même logique que pour les dossiers, redirection vers l’index des documents du dossier.

### 7.4 Suppression (`delete`)

- Vérification du dossier + suppression du document, flash, redirection vers l’index des documents.

Les vues sont dans `templates/{admin|medecin|patient}/document/`.

---

## 8. Controllers par acteur (Admin, Médecin, Patient)

| Acteur   | Dossiers                    | Documents                          | Fichiers |
|----------|-----------------------------|------------------------------------|----------|
| **Admin**   | `Admin\DossierMedicalController`  | `Admin\DocumentController`        | `src/Controller/Admin/*.php` |
| **Médecin** | `Medecin\DossierMedicalController` | `Medecin\DocumentController`      | `src/Controller/Medecin/*.php` |
| **Patient**  | `Patient\DossierMedicalController` | `Patient\DocumentController`      | `src/Controller/Patient/*.php` |

- **Préfixes de route** : `/admin/dossiers`, `/admin/dossiers/{dossierId}/documents` ; idem avec `/medecin/` et `/patient/`.
- Aucune restriction métier par rôle dans la version actuelle : tous les acteurs ont accès au CRUD complet sur dossiers et documents. Les variables `can_edit_dossier` et `can_delete_dossier` sont passées à `true` partout pour afficher tous les boutons.
- Un `RoleAccessService` existe (`src/Service/RoleAccessService.php`) et détermine le rôle à partir du chemin de la requête ; il n’est plus utilisé pour bloquer des actions.

---

## 9. Templates (vues)

- **Organisation** : un répertoire par acteur sous `templates/` : `admin/`, `medecin/`, `patient/`. Chaque acteur a les sous-dossiers `dossier_medical/` et `document/`.

- **Structure type** :
  - **index.html.twig** : titre, lien « Nouveau dossier » ou « Nouveau document », bloc des filtres (recherche, tri, ordre, dates, genre/type), paragraphe avec le compteur (`<span id="list-total">`), tableau dont le `<tbody id="...-tbody">` est remplacé en AJAX.
  - **_list_rows.html.twig** : partial contenant uniquement les `<tr>` (ou un message « Aucun dossier/document »). Inclus dans l’index au chargement initial et renvoyé seul en réponse AJAX.
  - **form.html.twig** : formulaire Symfony (form_row, form_rest), affichage des erreurs globales et par champ via Twig (pas de validation HTML). Attribut `novalidate="novalidate"` sur la balise `<form>`.
  - **show.html.twig** : fiche détaillée (tableau des champs) + liens vers modifier et vers la liste des documents (pour un dossier).

- **Base commune** : `templates/base.html.twig` (navbar avec liens Admin, Médecin, Patient et Accueil, blocs pour les messages flash, styles CSS inline pour tableaux, boutons, formulaires). Toutes les vues étendent cette base.

- Aucun bundle front (Bootstrap, Stimulus) : CSS et JS sont intégrés dans les templates ou en bloc `javascripts`.

---

## 10. Tri, filtres et validation

- **Tri** :
  - Dossiers : paramètre `tri` (valeurs : `dateCreation`, `dateModification`, `patientNom`, `numeroDossier`) et `ordre` (`ASC` / `DESC`).
  - Documents : `tri` (`dateDocument`, `dateCreation`, `titre`, `typeDocument`) et `ordre`.

- **Filtres** :
  - Dossiers : `q` (recherche texte), `date_debut`, `date_fin` (sur `dateCreation`), `genre` (M, F, Autre).
  - Documents : `q` (titre, contenu), `type` (type de document), `date_debut`, `date_fin` (sur `dateDocument`).
  - Les valeurs vides signifient « pas de filtre ».

- **Validation** :
  - Uniquement côté serveur via les **Assert** sur les entités (Length, NotBlank, Email, Regex, Choice, LessThan, etc.). Les messages sont affichés dans les templates avec des blocs du type `form.champ.vars.errors`. Aucune contrainte HTML5 (required, pattern, etc.) pour respecter la règle « contrôle de saisie uniquement en PHP ».

---

## 11. Recherche et filtres par AJAX

- **Principe** : la liste (tableau) et le compteur sont mis à jour sans rechargement de page.
  1. Un script JS récupère les valeurs des champs de recherche et des selects (tri, ordre, dates, genre/type).
  2. Il construit une query string avec `ajax=1` et envoie une requête `fetch` avec l’en-tête `X-Requested-With: XMLHttpRequest`.
  3. Le serveur retourne le HTML de la partial `_list_rows.html.twig` et l’en-tête `X-Total-Count`.
  4. Le script remplace le contenu du `<tbody>` par le HTML reçu et met à jour le texte du compteur (`#list-total`) à partir de `X-Total-Count`.

- **Déclencheurs** :
  - Clic sur le bouton « Rechercher ».
  - Événement `change` sur tous les `<select>` (tri, ordre, genre, type) et sur les `<input type="date">`.
  - Événement `input` sur le champ de recherche texte, avec un **debounce** de 300 ms pour limiter le nombre de requêtes.
  - Touche Entrée dans le champ recherche déclenche aussi une recherche.

- **Portée** : même mécanisme pour les 6 listes (admin/medecin/patient × dossiers/documents). Chaque page index contient son propre bloc `<script>` qui appelle la route correspondante (ex. `app_admin_dossier_index`, `app_admin_document_index` avec `dossierId`).

---

## 12. Accueil et navigation

- **Page d’accueil** : `HomeController#index` (route `/`). Template `templates/home/index.html.twig` qui étend `base.html.twig` et affiche trois liens vers les espaces Admin, Médecin et Patient (respectivement `app_admin_dossier_index`, `app_medecin_dossier_index`, `app_patient_dossier_index`).

- **Navigation** : la barre dans `base.html.twig` propose en permanence les liens « DocBook » (accueil), « Admin », « Médecin », « Patient ». Aucune session ni identification : l’accès à une URL suffit pour « être » dans l’espace de l’acteur.

- Il n’y a pas de tableaux de bord ni de mise en avant de dossiers/documents sur l’accueil ; uniquement le choix de l’espace (admin, medecin, patient).

---

## 13. Résumé des routes par acteur

| Acteur   | Dossiers médicaux | Documents |
|----------|-------------------|-----------|
| **Admin**   | `/admin/dossiers`<br>`/admin/dossiers/nouveau`<br>`/admin/dossiers/{id}`<br>`/admin/dossiers/{id}/modifier`<br>`/admin/dossiers/{id}` (POST delete) | `/admin/dossiers/{dossierId}/documents`<br>`/admin/dossiers/{dossierId}/documents/nouveau`<br>`/admin/dossiers/{dossierId}/documents/{id}`<br>`/admin/dossiers/{dossierId}/documents/{id}/modifier`<br>POST delete |
| **Médecin** | `/medecin/dossiers` (+ new, show, edit, delete) | `/medecin/dossiers/{dossierId}/documents` (+ new, show, edit, delete) |
| **Patient**  | `/patient/dossiers` (+ new, show, edit, delete) | `/patient/dossiers/{dossierId}/documents` (+ new, show, edit, delete) |

- **Accueil** : `GET /` → `app_home`.
- Toutes les routes sont en GET sauf les suppressions (POST). Aucune protection par rôle ni par token CSRF.

---

## 14. Résumé des commandes et bonnes pratiques

| Action | Commande |
|--------|----------|
| Créer la base si nécessaire | `php bin/console doctrine:database:create --if-not-exists` |
| Exécuter les migrations | `php bin/console doctrine:migrations:migrate --no-interaction` |
| Statut des migrations | `php bin/console doctrine:migrations:status` |
| Lancer le serveur web | `php -S localhost:8000 -t public` |

- **Messages flash** : utilisés dans tous les controllers après création, modification et suppression (`$this->addFlash('success', '...')`).
- **Validation** : uniquement via les Assert sur les entités ; affichage des erreurs dans les templates avec `form.vars.errors` et `form.champ.vars.errors` ; pas de validation HTML.
- **Réutilisation** : mêmes repositories et mêmes Form Types pour les trois acteurs ; seuls les controllers et les templates sont dupliqués par espace (admin, medecin, patient).
- **AJAX** : toutes les listes partagent le même schéma (paramètres en query, partial + `X-Total-Count`, debounce sur la recherche texte, `change` sur les filtres).

Ce guide couvre l’implémentation de la gestion des patients (dossiers médicaux et documents) de A à Z. Pour aller plus loin, on peut s’appuyer sur la [documentation Symfony](https://symfony.com/doc/current/index.html) et la [documentation Doctrine](https://www.doctrine-project.org/projects/doctrine-orm/en/current/index.html).
