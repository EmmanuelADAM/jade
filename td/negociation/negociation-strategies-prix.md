# Négociation JADE : au-delà du ±10% — stratégies d'évolution du prix

*Complément au TD [`td.negociation`](https://github.com/EmmanuelADAM/jade/tree/master/td/negociation) (voir `readme.md` du TD pour l'énoncé de base).*

---

## 1. Rappel : le modèle de base du TD

Dans `BuyerAgent` et `SellerAgent`, à chaque tour où l'offre reçue n'est ni acceptable ni inacceptable, l'agent **concède un pourcentage fixe** de son prix courant :

```java
double coef = 0.1; // 10%, fixe, identique à chaque tour

// côté acheteur
proposedPrice = proposedPrice * (1 + coef);   // il monte

// côté vendeur
proposedPrice = proposedPrice * (1 - coef);   // il baisse
```

C'est simple et ça fonctionne, mais c'est **naïf** :

- la concession est la même au 1er tour qu'au dernier tour, qu'il reste beaucoup ou peu de temps ;
- elle ne dépend ni de l'écart avec l'autre agent, ni du comportement de l'adversaire ;
- elle est totalement prévisible : un agent adverse rationnel peut exploiter ce fait (attendre, ne jamais bouger, laisser l'autre faire tout le chemin).

La recherche en négociation automatique date d'une trentaine d'années (cf. Faratin, Sierra & Jennings, *"[Negotiation decision functions for autonomous agents](https://jmvidal.cse.sc.edu/library/faratin98a.pdf)"*, 1998) et propose des familles de stratégies plus "intelligentes", plus riches. On en présente trois ici, directement transposables dans le code du TD.

---

## 2. Stratégie temporelle : concéder selon le temps restant

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

### Adaptation Java (côté vendeur, qui descend de `proposedPriceInit` vers `threshold`)

```java
double proposedPriceInit; // sauvegardé une fois au setup()
double beta = 0.3;        // < 1 => vendeur "dur" (Boulware)

// dans le behaviour, à la place de : this.proposedPrice = this.proposedPrice * (1 - coef);
double t = Math.min(1.0, (double) nbTours[0] / maxRounds);
this.proposedPrice = proposedPriceInit - (proposedPriceInit - threshold) * Math.pow(t, 1.0 / beta);
```

**Piste d'exercice :** faire varier `beta` aléatoirement à la création de chaque agent (comme `maxRounds` l'est déjà) pour obtenir une population d'agents aux « personnalités » différentes, et observer qui l'emporte.

---

## 3. Stratégie dépendante de l'écart de prix (*resource/distance-dependent*)

Idée : la taille de la concession dépend de **l'écart courant** entre les deux offres, pas seulement du temps. Deux logiques opposées et toutes deux défendables :

- **Concession proportionnelle à l'écart** : plus l'écart est grand, plus on concède franchement (pour converger vite quand on est loin, puis affiner près du but) ;
- **Concession inversement proportionnelle à l'écart** : au contraire, on ne fait de « gros pas » qu'en fin de négociation, quand on est déjà proche (stratégie plus prudente, on ne donne pas trop tôt un signal de faiblesse).

### 3.1 Formule générique

```
ecart = |prixRecu − prixPropose|
nouveauPrix = prixPropose ± k × ecart        // k ∈ ]0, 1[, le "taux d'ajustement à l'écart"
```

- `k` proche de 0 → petits pas prudents ;
- `k` proche de 1 → l'agent va presque directement au niveau de l'offre adverse (quasi-acceptation).

### 3.2 Adaptation Java (côté acheteur, qui monte vers `receivedPrice`)

```java
double k = 0.35; // taux d'ajustement, à faire varier pour comparer les styles

// à la place de : proposedPrice = proposedPrice * (1 + coef);
double ecart = receivedPrice - proposedPrice; // > 0 ici car receivedPrice > proposedPrice
proposedPrice = proposedPrice + k * ecart;
```

Avec `k = 0.5`, l'agent propose systématiquement le **milieu** entre son offre et celle qu'il vient de recevoir : c'est la tactique dite du *"split the difference"*, qui converge très vite (en 3-4 tours en général) et qui est robuste car symétrique — si les deux agents l'utilisent, ils convergent naturellement vers le juste milieu de leurs positions initiales.

### 3.3 Variante « petits pas puis grand saut » (inverse)

```java
// concession plus franche seulement quand on est déjà proche (ecart relatif faible)
double ecartRelatif = ecart / proposedPrice;
double k = ecartRelatif < 0.15 ? 0.6 : 0.15; // bascule en fin de négo
proposedPrice = proposedPrice + k * ecart;
```

Cette variante illustre qu'on peut **combiner** un critère d'écart et un seuil, sans passer par une formule continue.

---

## 4. Stratégie combinée : temps **et** écart (la plus réaliste)

En pratique, les négociateurs humains combinent les deux : ils sont patients au début (facteur temps), mais accélèrent d'autant plus que l'écart restant est faible et le temps presse.

```
concession = alpha × f_temps(t) + (1 − alpha) × g_ecart(ecart)
```

Par exemple, une combinaison simple pondérée :

```java
double t = (double) nbTours[0] / maxRounds;
double ecartRelatif = Math.abs(receivedPrice - proposedPrice) / proposedPrice;
double alpha = 0.5; // poids respectif du temps et de l'écart

double concessionTemps  = Math.pow(t, 1.0 / beta); 
double concessionEcart  = ecartRelatif; 

double tauxConcession = alpha * concessionTemps + (1 - alpha) * concessionEcart;
proposedPrice = proposedPrice + tauxConcession * (receivedPrice - proposedPrice);
```

---

## 5. Stratégie réactive au comportement adverse (*tit-for-tat*)

Idée : au lieu de décider seul de sa concession, l'agent **imite (en atténué) la dernière concession de l'adversaire** : si l'autre a beaucoup bougé au tour précédent, je bouge aussi beaucoup ; s'il n'a pas bougé, je reste ferme.

Cela nécessite de mémoriser l'offre précédente de l'adversaire pour calculer *sa* dernière concession :

```java
double[] dernierePropositionAdverse = {Double.NaN};

// dans le behaviour, avant de mettre à jour proposedPrice :
double concessionAdverse = Double.isNaN(dernierePropositionAdverse[0])
        ? 0
        : Math.abs(receivedPrice - dernierePropositionAdverse[0]);
dernierePropositionAdverse[0] = receivedPrice;

double imitation = 0.8; // < 1 : on n'imite qu'une fraction de la concession adverse (prudence)
proposedPrice = proposedPrice + imitation * concessionAdverse; // (adapter le signe selon acheteur/vendeur)
```

C'est une stratégie « punitive/récompensante » : un adversaire têtu (qui ne bouge jamais) provoque en retour un agent têtu — la négociation cale, ce qui est réaliste et pédagogiquement intéressant à observer (comparé au modèle actuel, qui converge presque toujours).

---

## 6. Ajouter un peu d'aléatoire (éviter la prévisibilité totale)

Quelle que soit la stratégie choisie, on peut ajouter un petit bruit aléatoire à la concession pour rendre l'agent moins déterministe (et éviter qu'un adversaire n'exploite une formule parfaitement prévisible) :

```java
double bruit = 1 + (Math.random() - 0.5) * 0.1; // ±5% de variation autour du calcul
proposedPrice = proposedPrice * bruit;
```

---

## 7. Synthèse comparative
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

Implémenter ces stratégies et faites les concourir pour noter les succès, échec..
