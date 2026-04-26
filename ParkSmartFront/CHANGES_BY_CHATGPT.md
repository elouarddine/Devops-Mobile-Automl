# Modifications réalisées

## Front Android
- Remplacement de l’affichage carte par **OpenStreetMap (osmdroid)** pour éviter le blocage de l’écran quand Google Maps n’affiche rien.
- Ajout du `userAgent` osmdroid et activation explicite de la connexion data pour permettre le chargement réel des tuiles.
- Recherche utilisateur branchée sur le backend : l’app envoie désormais le texte de destination + l’option d’arrivée, puis affiche les parkings renvoyés par l’API.
- Affichage des marqueurs de parkings sur la carte et ouverture du détail au clic sur un marqueur.
- Bottom sheet revu : ouverture réelle après sélection d’un onglet, repli sur clic carte, poignée plus exploitable, hauteur rehaussée.
- Sauvegardes, historique et profil reliés au backend.
- Cartes parking redesignées : disponibilité actuelle, prévision, distance, prix/capacité.
- Écran détail parking revu avec bouton d’itinéraire.
- Page profil utilisateur redesignée.
- Ajout d’un **vrai écran admin** relié aux endpoints backend : dashboard, import dataset, train, evaluate, résultats, historique des jobs, profil, déconnexion.
- Amélioration des messages d’erreur réseau côté recherche.

## Points à vérifier côté machine locale
- Si vous testez sur **émulateur Android**, gardez `http://10.0.2.2:8000/` dans `ApiClient`.
- Si vous testez sur **téléphone réel**, remplacez cette URL par l’IP locale de votre machine.
- Le backend doit être relancé avec la version corrigée fournie avec cette archive.
