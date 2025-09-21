# Jade : TD1 - Négociation

## Exemple basique : la négociation 1-1 
 
---

Sur base du code sur le [PingPong](https://github.com/EmmanuelADAM/jade/blob/master/pingPong/), deux agents 
vendeur et acheteur négocient autour d'un prix.

Définissez les échanges de message sachant que le vendeur initie la négociation en proposant un prix.
 - Le vendeur dispose : 
   - d'un prix qu'il propose
   - d'un seuil sous lequel il met fin à la négociation
   - d'un nombre de tours avant de mettre fin à la négociation

 - L'acheteur dispose :
   - d'un prix qu'il propose
   - d'un seuil au-dessus lequel il met fin à la négociation
   - d'un nombre de tours avant de mettre fin à la négociation

 - pour l'acheteur : 
   - si le nb de tours dépasse le nb max, il répond avec un rejet ;
   - si le prix reçu est au-dessus du seuil haut, il répond avec un rejet ;
   - si le prix reçu semblable au prix proposé, il répond avec une confirmation ;
   - si le prix est entre le prix proposé et le seuil, l'acheteur augmente sa proposition initiale de x%.

- pour le vendeur :
  - si le nb de tours dépasse le nb max, il répond avec un rejet ;
  - si le prix reçu est sous le seuil bas, il répond avec un rejet ;
  - si le prix reçu semblable au prix proposé, il répond avec une confirmation ;
  - si le prix est entre le prix proposé et le seuil, le vendeur baisse sa proposition initiale de x%.

Regardez les classes proposées, et lancez le `main` de la classe `Main`.

**Question 1:**
- 1 seul acheteur est un humain, les autres sont des agents.
- ajoutez la classe permettant de lancer plusieurs acheteurs (1 humain et n agents).

- **Question 2** 
- Le vendeur doit être paramétrable (prix proposé, prix min, nb de cycles).
  - il faut alors créer une fenêtre spécifiquement pour le vendeur et la relier à l'agent. 

**Question 3** les pourcentages $\epsilon$ pour la diminution et l'augmentation du prix sont calculés en fonction du prix proposé initialement, du prix seuil et du nombre de cycles autorisés.
- ex. prix de base = 100, prix max = 200, nb de cycles = 10, alors $\epsilon$ = 8% : 
  - 100, 108, 116, 124, 132, 140, 148, 156, 164, 172, 180, 188, 196



---