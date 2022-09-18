# Jade : Agents, protocole et services

## Exemple : FIPA Contract Net Protocole en Jade

### Utilisation pour un vote de Concorcet

---

Ici, vous trouverez un exemple de communication via le
protocole [FIPA Contract Net](http://www.fipa.org/specs/fipa00029/SC00029H.html) pour un vote de Condorcet (les 
votants classent les options en les notant, un calcul du nombre de duels gagné est effectué)


- *Principe du vote "de Condorcet"* ***en un tour***
  - a1 propose o6 > o4 > o5 > o7 > o1 > o3 > o2
  - a2 propose o7 > o6 > o3 > o1 > o4 > o2 > o5
  - a3 propose o1 > o2 > o3 > o4 > o6 > o7 > o5
  - a4 propose o4 > o7 > o2 > o1 > o6 > o5 > o3
  - a5 propose o1 > o2 > o4 > o3 > o5 > o7 > o6


- le bureau de vote affecte des valeurs sur le nombre de duels gagnés-nombre de duels perdus.
    - Ex. : option1 est classée 4 fois devant option2 et 1 fois derrière, donc la valeur duel(option1, option2)=3 
      et ici inversement duel(option2, option1)=-3 (dans la proposition initiale de Condorcet, on ne compte pas les 
      duels perdus)
    - ci-dessous la matrice des décomptes des duels.
      - option1, option4 et option7 ont chacune gagné devant 5 autres options


|   - | o1  | o2  | o3  | o4  | o5  | o6  | o7  | duels gagnés | points |
|----:|:---:|:---:|:---:|:---:|:---:|:---:|:---:|:------------:|:------:|
|  o1 | 0   |  3  |  3  |  1  | 3   |  1  | -1  |      5       |   10   |
|  o2 | -3  |  0  |  1  | -1  |  3  |  1  | -1  |      3       |   0    |
|  o3 | -3  | -1  |  0  | -1  |  1  | -1  | -1  |      1       |   -6   |
|  o4 | -1  |  1  |  1  |  0  |  5  |  1  |  3  |      5       |   10   |
|  o5 | -3  | -3  | -1  | -5  |  0  | -3  | -1  |      0       |  -16   |
|  o6 | -1  | -1  |  1  | -1  |  3  |  0  | -1  |      2       |   0    |
|  o7 |  1  |  1  |  1  | -3  |  1  |  1  |  0  |      5       |   2    |

   

- Selon le principe du vote de Condorcet, l'option gagnante est celle qui remporté le plus de séries de duels 
  devant chacune des autres options.
- En cas d'ex-aequo, on les départagent par rapport au nombre de points (nb de duels gagnés-nb de duels perdus) 
  - Ici, l'option 1 et l'option 4 ont toutes deux 10 points, l'option 7 n'en recolte que 2. Il faut alors départager 
    les deux premières.

- Il existe, dans la littérature, de nombreuses propositions pour tenter de résoudre ce type de cas
    - ici, on crée une nouvelle matrice de gain basé uniquement sur les options retenues (ici option 1 & option 4).

| -   | o1  | o2  | o3  | o4  | o5  | o6  | o7  | duels gagnés | points |
|:---:|:---:|:---:|:---:|:---:|:---:|:---:|:---:|:------------:|:------:|
| o1  |  0  |  0  |  0  |  1  |  0  |  0  |  0  |      1       |   1    |
| o2  |  0  |  0  |  0  |  0  |  0  |  0  |  0  |      0       |   0    |
| o3  |  0  |  0  |  0  |  0  |  0  |  0  |  0  |      0       |   0    |
| o4  | -1  |  0  |  0  |  0  |  0  |  0  |  0  |      0       |   -1   |
| o5  |  0  |  0  |  0  |  0  |  0  |  0  |  0  |      0       |   0    |
| o6  |  0  |  0  |  0  |  0  |  0  |  0  |  0  |      0       |   0    |
| o7  |  0  |  0  |  0  |  0  |  0  |  0  |  0  |      0       |   0    |

  - option1 reporte le plus de série de duels (1 contre option4 (on a 3x o1>o4 et 2x o4>o1)), c'est donc cette option 
    qui est finalement déclarée élue.

- Si un ex-aequo subsiste encore entre des options après cette 2e phase, un tirage aléatoire est réalisé entre 
  celles-ci.

----
### Codage

- un agent de
  type [AgentBureauVote](https://github.com/EmmanuelADAM/jade/blob/master/protocoles/voteBorda/agents/AgentBureauVote.java)
  émet un appel à vote sur des noms de restaurants : pizzeria, legumes, sushi, ...
- auprès d'agents de
  type [AgentParticipant](https://github.com/EmmanuelADAM/jade/blob/master/protocoles/voteBorda/agents/AgentParticipant.java)
- Le protocole oblige :
    - les destinataires à répondre (refus, erreur, accord), si accord, la réponse est de la forme : 
      legumes>pizzeria>sushi>...
    - au bureau de vote :
      - de créer la matrice des duels
          - d'envoyer le choix gagnant à tous les votants (ou les choix si ex-aequo)
          - chaque votant confirme la reception et l'acceptation du résultat du vote
    - si ex-aequo (en nb de duels et nb de points), le vote est relancé pour les choix non départagés

- [LaunchAgents](https://https://github.com/EmmanuelADAM/jade/blob/master/protocoles/voteBorda/launch/LaunchAgents.java) : **
  classe principale**, lançant Jade et créant les agents
    - Au lancement, 1 bureau et 5 participants sont lancés.
    - Le bureau de votes lance un vote à chaque clic sur 'go'..

 ---