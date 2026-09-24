# Jade : TD - NégociationS

## La négociation 1-1 
 
---

Deux agents vendeur et acheteur négocient autour d'un prix.

Définissez les échanges de message sachant que le vendeur initie la négociation en proposant un prix.
 - L'acheteur et le vendeur disposent : 
   - d'une offre initiale, 
   - d'un seuil (prix sous ou au-dessus duquel l'agent stoppe la négociaton)
   - d'un nombre de tours avant de mettre fin à la négociation

 - pour l'acheteur : 
   - si le nb de tours dépasse le nb max, il répond avec un rejet ;
   - si le prix reçu est au-dessus du seuil haut, il répond avec un rejet ;
   - si le prix est entre le prix proposé et le seuil, l'acheteur augmente sa poposition  selon une stratégie choisie.

- pour le vendeur :
  - si le nb de tours dépasse le nb max, il répond avec un rejet ;
  - si le prix reçu est sous le seuil bas, il répond avec un rejet ;
  - si le prix est entre le prix proposé et le seuil, le vendeur baisse sa poposition selon une stratégie choisie.

---

## 1. structure du code
 - ``agents`` : les classes des agents ``BuyerAgent`` et ``SellerAgent``
 - ``behaviour`` : contient le comportement ``NegociationBehaviour`` et la liste des strategies (package ) ``behaviour.strategies``
 - ``gui`` : leurs interfaces pour fixer le prix initial, le prix seuil et le nombre de tour.
    - cliquer sur ``start`` en premier sur seller, puis ``send`` sur buyer pour démarrer la négociation
Chaque agent charge le comportement dans son setup; l'affectation des valeurs et de la strategie se fait suite aux clics sur start et send (cf. le code)

---

## Liste des strategies proposées

### Stratégie temporelle : concéder selon le temps restant

Idée : la vitesse de concession dépend du **nombre de tours déjà écoulés** par rapport au nombre de tours max (`nbTours[0] / maxRounds`), et non d'un pourcentage fixe.

On définit un prix cible entre le prix initial `prixInit` et le seuil limite `threshold`, paramétré par un exposant β :

```
t = nbToursEcoules / maxRounds        // ∈ [0, 1], avance dans le temps
prix(t) = prixInit − (prixInit − threshold) × t^(1/β)      // cas acheteur (prix qui monte : inverser le signe)
```

Le paramètre **β** fixe le *caractère* du négociateur :

| β | Comportement | Effet |
|---|---|---|
| β < 1 (ex. 0.2) | **dur**  | reste proche de son prix initial longtemps, ne cède fortement qu'en toute fin de négociation |
| β = 1 | **régulier** | concède régulièrement, à vitesse constante dans le temps |
| β > 1 (ex. 5) | **conciliant**  | cède beaucoup dès le début, presque plus rien à la fin |


---

### Stratégie dépendante de l'écart de prix

Idée : la taille de la concession dépend de **l'écart courant** entre les deux offres, pas seulement du temps. Deux logiques :
  - **Concession proportionnelle à l'écart** : plus l'écart est grand, plus on concède franchement (pour converger vite quand on est loin, puis affiner près du but) ;
  - **Concession inversement proportionnelle à l'écart** : au contraire, on ne fait de « gros pas » qu'en fin de négociation, quand on est déjà proche (stratégie plus prudente, on ne donne pas trop tôt un signal de faiblesse).

**Formule générique**

```
ecart = |prixRecu − prixPropose|
nouveauPrix = prixPropose ± k × ecart        // k ∈ ]0, 1[, le "taux d'ajustement à l'écart"
```
 - `k` proche de 0 → petits pas prudents ;
 - `k` proche de 1 → l'agent va presque directement au niveau de l'offre adverse (quasi-acceptation).
 - Avec `k = 0.5`, l'agent propose systématiquement le **milieu** entre son offre et celle qu'il vient de recevoir : c'est la tactique dite du *"split the difference"*, qui converge très vite (en 3-4 tours en général) et qui est robuste car symétrique — si les deux agents l'utilisent, ils convergent naturellement vers le juste milieu de leurs positions initiales.

**Stratégie combinée : temps _et_ écart (la plus réaliste)**
 - En pratique, les négociateurs humains combinent les deux : ils sont patients au début (facteur temps), mais accélèrent d'autant plus que l'écart restant est faible et le temps presse.
C'est ce qui est implémenté dans la stratégie

--- 

### Stratégie réactive au comportement adverse (*tit-for-tat*, _coup-pour-coup_)

Idée : au lieu de décider seul de sa concession, l'agent **imite (en atténué, ou en augmenté) la dernière concession de l'adversaire**.

Cela nécessite de mémoriser l'offre précédente de l'adversaire pour calculer *sa* dernière concession :

  - C'est une stratégie « punitive/récompensante » : un adversaire têtu (qui ne bouge jamais) provoque en retour un agent têtu — la négociation cale, ce qui est réaliste.

---

### Astuce ! Ajouter un peu d'aléatoire

Quelle que soit la stratégie choisie, on peut ajouter un petit bruit aléatoire à la concession pour rendre l'agent moins déterministe (ainsi éviter qu'un adversaire n'exploite une formule parfaitement prévisible, et donner une impression de "vivant") :

```java
double bruit = 1 + (Math.random() - 0.5) * 0.1; // ±5% de variation autour du calcul
proposedPrice = proposedPrice * bruit;
```

---

## Synthèse comparative
Compléter le tableau...

| Stratégie | Dépend de | Avantage | Limite |
|---|---|---|---|
| **±10% fixe** (TD de base) | rien (constant) | . | . |
| **Temporelle (β)** | temps restant  | . | . |
| **Écart de prix (k)** | écart courant  | . | . |
| **Combinée temps+écart** | les deux  | . | . |
| **Tit-for-tat** | comportement adverse  | . | . |
| **+ bruit** | (bonus, cumulable)  | . | . |
---

## TRAVAIL A FAIRE

Faites concourir les stratégies pour noter les succès, échec..

---