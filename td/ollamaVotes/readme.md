# Vote BORDA par votants pilotés par un LLM 

Ce code reprend le vote par Borda mais maintenant les votants utilisent des LLM.

Chaque agent votant a sa personnalité ("vegan", "sportive", .....) et effectue un classement parmi une liste de restaurants.

NB. Ces restaurants doivent donc avoir des noms explicites "pizzeria", "produit de la mer", "legumes sur table", ....

Le bureau de vote reste un agent pur.

Les votants utilisent Ollama... Ils doivent retourner un classement à partir d'un modèle.

Ollama permet de structurer la sortie : https://docs.ollama.com/capabilities/structured-outputs

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

Lancer [LanceurVote](https://github.com/EmmanuelADAM/jade/blob/english/td/ollamaVotes/launch/LanceurVote.java).

Et voici un exemple de sortie :

```
ines -> pret. Personnalite : Tu es Ines, 20 ans, étudiante boursière, tu fais très attention à chaque euro dépensé.
hugo -> pret. Personnalite : Tu es Hugo, 23 ans, sportif pro lanceur de poids, tu privilégie les apports en glucides et protéines.
karim -> pret. Personnalite : Tu es Karim, 21 ans, gamer pro qui reste concentré, tu préfères des sucres lents.
lea -> pret. Personnalite : Tu es Lea, 22 ans, végane et militante écologiste, tu détestes tout ce qui touche a la viande.
zoe -> pret. Personnalite : Tu es Zoe, 22 ans, gourmande et curieuse, tu adores découvrir des cuisines épicées.

Organisateur -> 5 votants, sujet : Sortie de fin d'annee de la promo

zoe -> je vote B>D>A>E>C : J'adore les épices indiennes, puis la pizza épicée, ensuite steak, moules, légumes.
ines -> je vote C>D>B>E>A : Je préfère le végétarien, puis la pizzeria, l'indien, la brasserie, enfin le steakhouse.
lea -> je vote C>B>D>E>A : je préfère le végétal, puis options végétariennes, évite fruits de mer et viande rouge
karim -> je vote D>A>B>E>C : Pizza donne énergie, steak protéine, indian épices, moules faibles, légumes trop peu.
hugo -> je vote A>D>E>B>C : Steakhouse offre protéines et glucides, pizzeria carb-rich, moules-protéines, indien équilibré, légumes faibles.

===== Bulletins =====
zoe      B>D>A>E>C  "J'adore les épices indiennes, puis la pizza épicée, ensuite steak, moules, légumes."
ines     C>D>B>E>A  "Je préfère le végétarien, puis la pizzeria, l'indien, la brasserie, enfin le steakhouse."
lea      C>B>D>E>A  "je préfère le végétal, puis options végétariennes, évite fruits de mer et viande rouge"
karim    D>A>B>E>C  "Pizza donne énergie, steak protéine, indian épices, moules faibles, légumes trop peu."
hugo     A>D>E>B>C  "Steakhouse offre protéines et glucides, pizzeria carb-rich, moules-protéines, indien équilibré, légumes faibles."

===== Resultat Borda =====
D   15 pts  restaurant la pizzeria
B   12 pts  restaurant indien
A    9 pts  diner dans un steakhouse
C    8 pts  restaurant 'légumes sur tables'
E    6 pts  brasserie moules-frittes

lea -> resultat annonce : Gagnant : D (restaurant la pizzeria) | scores {A=9, B=12, C=8, D=15, E=6}
ines -> resultat annonce : Gagnant : D (restaurant la pizzeria) | scores {A=9, B=12, C=8, D=15, E=6}
karim -> resultat annonce : Gagnant : D (restaurant la pizzeria) | scores {A=9, B=12, C=8, D=15, E=6}
zoe -> resultat annonce : Gagnant : D (restaurant la pizzeria) | scores {A=9, B=12, C=8, D=15, E=6}
hugo -> resultat annonce : Gagnant : D (restaurant la pizzeria) | scores {A=9, B=12, C=8, D=15, E=6}
Organisateur -> 5 votants ont pris acte du resultat.
karim -> quitte la plateforme.
hugo -> quitte la plateforme.
ines -> quitte la plateforme.
zoe -> quitte la plateforme.
lea -> quitte la plateforme.
```