# Jade : TD1 - Négociation

## Exemple basique : la négociation 1-1 

---

Sur base du code sur le [PingPong](https://github.com/EmmanuelADAM/jade/blob/master/pingPong/), deux agents 
vendeur et acheteur négocient autour d'un prix.

Définissez les échanges de message sachant que le vendeur initie la négociation en proposant un prix.
 - Le vendeur dispose : 
   - d'un prix qu'il propose
   - d'une seuil sous lequel il met fin à la négociation
   - d'un nombre de tours avant de mettre fin à la négociation

 - L'acheteur dispose :
   - d'un prix qu'il propose
   - d'une seuil au dessus lequel il met fin à la négociation
   - d'un nombre de tours avant de mettre fin à la négociation

 - pour l'acheteur : 
   - si le nb de tours dépasse le nb max, il répond avec un rejet;
   - si le prix reçu est au-dessus du seuil haut, il répond avec un rejet;
   - si le prix reçu semblable au prix proposé, il répond avec une confirmation
   - si le prix est entre le prix proposé et le seuil, l'acheteur augmente sa poposition initiale de x%

- pour le vendeur :
  - si le nb de tours dépasse le nb max, il répond avec un rejet;
  - si le prix reçu est sous le seuil bas, il répond avec un rejet;
  - si le prix reçu semblable au prix proposé, il répond avec une confirmation
  - si le prix est entre le prix proposé et le seuil, le vendeur baisse sa poposition initiale de x%

Regardez les classes proposées, et lancez le `main` de la classe `Main`.

---