# Vote BORDA par votants pilotés par un LLM 

Ce code reprend le vote par Borda, mais maintenant les votants utilisent des LLM.

Chaque agent votant a sa personnalité ("vegan", "sportive", .....) et effectue un classement parmi une liste de restaurants.  
Ces restaurants doivent donc avoir des noms explicites "pizzeria", "produit de la mer", "legumes sur table", ....

Le bureau de vote reste un agent pur.

Les votants utilisent donc un modèle LLM via Ollama; *mais ils doivent retourner un classement* à partir d'un modèle.
  - Chaque modèle actuel peut structurer sa réponse au format JSON.  
  - Ollama permet de définir le format de sortie : https://docs.ollama.com/capabilities/structured-outputs

Exemple de schema de sortie :

```java
var schema = new JSONObject()
.put("type", "object")
  .put("properties", new JSONObject()
    .put("classement", new JSONObject()
      .put("type", "array")
        .put("items", new JSONObject().put("type", "string").put("enum", new JSONArray(lettres))))
    .put("justification", new JSONObject().put("type", "string")))
  .put("required", new JSONArray().put("classement").put("justification"))
  .put("additionalProperties", false); //pour empecher les LLM complexes d'ajouter des interprétations
```
----

**Exemple de creation d'agents votants**

Lancer [LanceurVote](https://github.com/EmmanuelADAM/jade/blob/english/td/ollamaVotes/launch/LanceurVote.java).  
Ce code lance un agent bureau de vote et  des agents votants pilotés par des LLM orientés selon leurs personnalités.  
Le vote est directement lancé et le résultat s'affiche dans la console.


Au lancement du SMA, on peut définir ces agents en passant les caractéristiques en paramètre au lancement des agents :
 - "lea",  "Tu es Lea, 22 ans, végane et militante écologiste, tu détestes tout ce qui touche a la viande.",
 - "karim", "Tu es Karim, 21 ans, gamer pro qui reste concentré, tu préfères des sucres lents.",
 - "ines", "Tu es Ines, 20 ans, étudiante boursière, tu fais très attention à chaque euro dépensé.",
 - "hugo", "Tu es Hugo, 23 ans, sportif pro lanceur de poids, tu privilégie les apports en glucides et protéines.",
 - "zoe",  "Tu es Zoe, 22 ans, gourmande et curieuse, tu adores découvrir des cuisines épicées."


Voici des restaurants possibles à tester :
 - "A", "diner dans un steakhouse",
 - "B", "restaurant indien",
 - "C", "restaurant 'légumes sur tables'",
 - "D", "restaurant la pizzeria",
 - "E", "brasserie moules frittes"


Et voici un exemple de sortie :

```
hugo -> pret. Personnalite : Tu es Hugo, 23 ans, sportif pro lanceur de poids, tu privilégie les apports en glucides et protéines.
ines -> pret. Personnalite : Tu es Ines, 20 ans, étudiante boursière, tu fais très attention à chaque euro dépensé.
karim -> pret. Personnalite : Tu es Karim, 21 ans, gamer pro qui reste concentré, tu préfères des sucres lents.
lea -> pret. Personnalite : Tu es Lea, 22 ans, végane et militante écologiste, tu détestes tout ce qui touche a la viande.
zoe -> pret. Personnalite : Tu es Zoe, 22 ans, gourmande et curieuse, tu adores découvrir des cuisines épicées.
Organisateur -> 5 votants, sujet : Sortie de fin d'annee de la promo
Voici les options possibles :
A: diner dans un steakhouse
B: restaurant indien
C: restaurant 'légumes sur tables'
D: restaurant la pizzeria
E: brasserie moules-frittes
~~~~~~~~~~~~~~~~~~~~
ines -> je vote C>D>B>E>A : Je choisis le restaurant végétarien car c'est généralement le moins cher, suivi de la pizzeria qui reste un classique abordable, alors que le steakhouse est hors de budget pour moi.
karim -> je vote C>A>B>E>D : Je choisis les légumes pour les sucres lents qui maintiennent ma concentration, loin du crash du sucre des pizzas.
lea -> je vote C>B>D>E>A : Le restaurant végétal est évidemment mon premier choix, tandis que le steakhouse est une horreur absolue que je refuse de fréquenter par respect pour les animaux et la planète.
zoe -> je vote B>A>E>D>C : Je vote pour le restaurant indien sans hésiter car je suis accro aux saveurs épicées et currynées, alors que les légumes seuls m'ennuieraient beaucoup trop !
hugo -> je vote A>E>B>D>C : En tant que lanceur de poids, je privilégie le steakhouse pour le maximum de protéines, suivi des moules-frittes et du indien pour les glucides nécessaires à mes entraînements.
===== Resultat Borda =====
B   13 pts  restaurant indien
C   12 pts  restaurant 'légumes sur tables'
A   10 pts  diner dans un steakhouse
E    8 pts  brasserie moules-frittes
D    7 pts  restaurant la pizzeria

zoe -> resultat annonce : Gagnant : B (restaurant indien) | scores {A=10, B=13, C=12, D=7, E=8}
hugo -> resultat annonce : Gagnant : B (restaurant indien) | scores {A=10, B=13, C=12, D=7, E=8}
lea -> resultat annonce : Gagnant : B (restaurant indien) | scores {A=10, B=13, C=12, D=7, E=8}
karim -> resultat annonce : Gagnant : B (restaurant indien) | scores {A=10, B=13, C=12, D=7, E=8}
ines -> resultat annonce : Gagnant : B (restaurant indien) | scores {A=10, B=13, C=12, D=7, E=8}
Organisateur -> 5 votants ont pris acte du resultat.
hugo -> quitte la plateforme.
karim -> quitte la plateforme.
lea -> quitte la plateforme.
zoe -> quitte la plateforme.
ines -> quitte la plateforme.
```