# ParkSmart Backend

Backend FastAPI + PostgreSQL pour ParkSmart, structuré dans l'esprit du projet MetaAgent :
- `src/api` : routes et démarrage API
- `src/backServices` : logique métier
- `src/security` : JWT / mots de passe
- `src/storage` : DB, modèles ORM, repositories
- `src/domain` : orchestration / ports

## Lancement rapide

```bash
cp .env.example .env
./run.sh start
```

Le script :
- crée/charge `.env`
- build les images Docker
- démarre PostgreSQL et l'API
- vérifie `http://localhost:8000/health`
- affiche des logs colorés séparés par service
- enregistre les logs dans `logs/run_YYYYMMDD_HHMMSS/`

## Comptes de démo

Admin seedé automatiquement :
- email : `admin@parksmart.com`
- mot de passe : `Admin1234`

## Compatibilité Android actuelle

Pour coller exactement à ton front Retrofit/Gson actuel :
- `POST /auth/register` retourne un JSON plat avec `success` et `message`
- `POST /auth/login` retourne un JSON plat avec `access_token`, `role`, `user_id`, `full_name`
- les routes `/v1/...` restent disponibles avec le format standardisé du contrat d'API

## Commandes utiles

```bash
./run.sh start
./run.sh logs
./run.sh status
./run.sh down
./run.sh reset
```


## Nouvelles routes utiles pour le front utilisateur

- `GET /auth/me` : profil connecté
- `POST /v1/search` : recherche de parkings
- `GET /v1/history` : historique backend des recherches
- `GET /v1/saved-parkings` : parkings sauvegardés
- `POST /v1/saved-parkings` : ajouter un parking sauvegardé
- `DELETE /v1/saved-parkings/{parking_id}` : retirer un parking sauvegardé
- `GET /v1/parkings/{parking_id}` : détails d'un parking

Après l'ajout de la table `saved_parkings`, fais un `./run.sh reset` si ta base existe déjà pour repartir proprement.


## Clé Google Maps côté backend

Pour activer la recherche dynamique de parkings réels autour d'une adresse, ajoute une **clé serveur** dans `.env` :

```env
GOOGLE_MAPS_API_KEY=ta_cle_serveur_google
```

Important : la clé Android placée dans le `AndroidManifest.xml` ne suffit pas pour le backend.
Crée une **deuxième clé** Google Cloud pour le serveur, avec au minimum les API :
- Places API
- Geocoding API

Ensuite redémarre complètement le backend :

```bash
./run.sh down
./run.sh start
```

Sans cette variable, le backend retombe sur les parkings locaux seedés en base.
