# ParkSmart - Application mobile de prédiction de disponibilité des parkings

ParkSmart est une application Android qui aide l’utilisateur à trouver un parking proche d’une destination et à estimer s’il restera au moins une place disponible selon son temps d’arrivée.  
Le projet relie une application mobile Android, une API FastAPI, un backend Python, une base PostgreSQL et une pipeline AutoML.

## Objectif du projet

L’objectif de ParkSmart est de transformer une simple information de disponibilité actuelle en une aide à la décision plus utile :  
**si l’utilisateur arrive dans 15, 30 ou 60 minutes, est-ce qu’il est probable qu’une place soit encore disponible ?**

L’application propose aussi un espace administrateur pour piloter la partie data et modèle : import de dataset, entraînement, évaluation et consultation des métriques.

## Fonctionnalités

### Espace utilisateur

- Création de compte et connexion.
- Recherche de parkings proches d’une destination.
- Affichage des parkings sur une carte.
- Consultation des informations d’un parking : nom, adresse, distance, capacité, prix et disponibilité.
- Choix d’une durée d’arrivée : 15, 30 ou 60 minutes.
- Prédiction de disponibilité sous forme simple : oui / non.
- Sauvegarde des parkings favoris.
- Consultation et suppression de l’historique des recherches.
- Affichage d’un itinéraire vers un parking sélectionné.
- Consultation du profil utilisateur.

### Espace administrateur

- Accès à un tableau de bord.
- Import d’un dataset.
- Lancement de l’entraînement du modèle.
- Lancement de l’évaluation du modèle.
- Consultation des métriques produites par la pipeline AutoML.
- Consultation de l’historique des jobs.

## Architecture du projet

Le projet est séparé en deux grandes parties :

```bash
Devops-Mobile-Automl/
│
├── ParkSmartFront/   # Application mobile Android
│
└── ParkSmartBack/    # Backend FastAPI, PostgreSQL et AutoML
```

### Frontend Android

Le frontend gère l’interface mobile et les interactions avec l’utilisateur :

- écrans de connexion et d’inscription,
- carte et recherche de parkings,
- affichage des résultats,
- historique,
- parkings sauvegardés,
- profil utilisateur,
- espace administrateur.

L’application communique avec le backend à l’aide de Retrofit et consomme les réponses JSON de l’API.

### Backend FastAPI

Le backend centralise la logique métier du projet :

- authentification,
- recherche de parkings,
- gestion des parkings sauvegardés,
- historique utilisateur,
- prédiction de disponibilité,
- routes administrateur,
- import de dataset,
- entraînement et évaluation AutoML.

La base PostgreSQL permet de stocker les utilisateurs, les historiques, les parkings sauvegardés, les datasets, les jobs et les résultats du modèle.

## Technologies utilisées

### Frontend

- Java
- Android Studio
- Retrofit
- OkHttp
- Gson
- Google Maps
- osmdroid
- Material Design

### Backend

- Python
- FastAPI
- PostgreSQL
- SQLAlchemy
- Docker
- Docker Compose
- JWT
- scikit-learn
- pandas
- Open-Meteo
- Google Places / Geocoding API

## Répertoire AutoML utilisé

La partie prédiction du projet s’appuie sur une pipeline AutoML développée dans un autre dépôt GitHub.  
Ce dépôt contient le travail lié au challenge machine learning, notamment la préparation des données, l’entraînement des modèles et l’évaluation des performances.

Lien du dépôt AutoML :

```bash
https://github.com/elouarddine/ChallengeML
```

## Prérequis

Avant de lancer le projet, il faut avoir installé :

- Git
- Docker
- Docker Compose
- Android Studio
- JDK 11 ou plus récent

## Installation du backend

Accédez au dossier backend :

```bash
cd ParkSmartBack
```

Copiez le fichier d’environnement :

```bash
cp .env.example .env
```

Lancez le backend :

```bash
./run.sh start
```

Le script lance automatiquement :

- la base PostgreSQL,
- l’API FastAPI,
- la création des tables,
- le compte administrateur de démonstration,
- les données de test.

Une fois lancé, l’API est disponible ici :

```bash
http://localhost:8000
```

La documentation Swagger est disponible ici :

```bash
http://localhost:8000/docs
```

## Commandes utiles du backend

```bash
./run.sh start
./run.sh logs
./run.sh status
./run.sh down
./run.sh reset
```

## Compte administrateur de test

Un compte administrateur est créé automatiquement pour les tests :

```bash
Email : admin@parksmart.com
Mot de passe : Admin1234
```

## Configuration de Google Maps côté backend

Pour utiliser la recherche dynamique de parkings réels autour d’une adresse, ajoutez une clé Google Maps serveur dans le fichier `.env` :

```env
GOOGLE_MAPS_API_KEY=votre_cle_google_maps
```

Les API nécessaires sont :

- Places API
- Geocoding API

Si aucune clé n’est renseignée, le backend utilise les parkings de démonstration enregistrés en base.

## Installation du frontend Android

Accédez au dossier frontend :

```bash
cd ParkSmartFront
```

Ouvrez ensuite le projet avec Android Studio, puis lancez l’application sur un émulateur ou un téléphone Android.

Pour compiler le projet en ligne de commande :

```bash
./gradlew assembleDebug
```

## Configuration de l’URL API côté Android

Dans le fichier suivant :

```bash
ParkSmartFront/app/src/main/java/com/example/parksmart/network/ApiClient.java
```

L’URL utilisée par défaut est :

```java
private static final String BASE_URL = "http://10.0.2.2:8000/";
```

Cette adresse fonctionne avec un émulateur Android.

Si l’application est lancée sur un téléphone réel, il faut remplacer `10.0.2.2` par l’adresse IP locale de la machine qui lance le backend, par exemple :

```java
private static final String BASE_URL = "http://192.168.1.20:8000/";
```

## Principales routes API

### Authentification

- `POST /auth/register` : créer un compte.
- `POST /auth/login` : se connecter.
- `GET /auth/me` : récupérer le profil connecté.

### Utilisateur

- `POST /v1/search` : rechercher des parkings.
- `POST /v1/parkings/predict` : prédire la disponibilité d’un parking.
- `GET /v1/history` : consulter l’historique.
- `DELETE /v1/history` : vider l’historique.
- `GET /v1/saved-parkings` : consulter les parkings sauvegardés.
- `POST /v1/saved-parkings` : sauvegarder un parking.
- `DELETE /v1/saved-parkings/{parking_id}` : supprimer un parking sauvegardé.
- `GET /v1/parkings/{parking_id}` : consulter les détails d’un parking.

### Administrateur

- `GET /v1/admin/dashboard` : consulter le tableau de bord.
- `POST /v1/admin/datasets/import` : importer un dataset.
- `POST /v1/admin/train` : lancer l’entraînement.
- `POST /v1/admin/evaluate` : lancer l’évaluation.
- `GET /v1/admin/results` : consulter les résultats.
- `GET /v1/admin/jobs` : consulter l’historique des jobs.

## Logique de prédiction

La prédiction repose sur une classification binaire.  
À partir des informations disponibles au moment de la recherche, le système estime si au moins une place sera probablement disponible pour le parking choisi après une durée d’arrivée donnée.

Les données utilisées peuvent inclure :

- la position du parking,
- la capacité,
- le nombre de places disponibles au moment de la requête,
- la météo,
- la date,
- le type de jour,
- la durée d’arrivée choisie.

Le résultat est volontairement simple pour l’utilisateur : une réponse de type **oui / non**.

## Structure backend

```bash
ParkSmartBack/
│
├── src/
│   ├── api/            # Routes FastAPI
│   ├── backServices/   # Logique métier
│   ├── domain/         # Orchestration
│   ├── predictor/      # Module de prédiction
│   ├── security/       # JWT et mots de passe
│   └── storage/        # Base de données et repositories
│
├── DockerFiles/
├── docker-compose.yml
├── requirements.txt
└── run.sh
```

## Structure frontend

```bash
ParkSmartFront/
│
├── app/
│   ├── src/main/java/com/example/parksmart/
│   │   ├── adapters/
│   │   ├── controllers/
│   │   ├── fragments/
│   │   ├── models/
│   │   ├── network/
│   │   ├── repository/
│   │   ├── utils/
│   │   └── view/
│
├── build.gradle.kts
└── settings.gradle.kts
```

## Sécurité

Le projet intègre plusieurs éléments de sécurité :

- authentification avec JWT,
- mots de passe hashés,
- séparation des rôles utilisateur et administrateur,
- protection des routes administrateur,
- validation des données côté backend,
- réponses d’erreur standardisées.

## Notes

- Le backend doit être lancé avant l’application Android.
- Sur émulateur Android, utilisez `http://10.0.2.2:8000/`.
- Sur téléphone réel, utilisez l’adresse IP locale de la machine.
- Les prédictions peuvent utiliser un modèle AutoML réel si les fichiers de modèle sont disponibles.
- En absence de modèle ou de données suffisantes, un mécanisme de fallback peut être utilisé côté backend.
