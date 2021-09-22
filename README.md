# jade

[(version web)](https://emmanueladam.github.io/jade/)

Liste de supports pour le cours de programmation orientée agent en [Jade](https://jade.tilab.com).
Pour l'ensemble de ces codes, il est nécessaire de disposer dans vos projet de la librairie "jade.jar"..
Si le site de Jade est inaccessible, voici des liens pour télécharger la librairie :
  - [jade.jar](http://emmanuel.adam.free.fr/jade/lib/jade.jar) : librairie principale,
  - [commons-codec-1.3.jar](http://emmanuel.adam.free.fr/jade/lib/commons-codec/commons-codec-1.3.jar) : librairie à utiliser lors d ela manipulation d'ontologie,
  - [jadeExamples.jar](http://emmanuel.adam.free.fr/jade/lib/jadeExamples.jar) : librairie contenant les exemples de la documentation Jade.
  
La documentation et l'API sont également accessibles [sur le lien](http://emmanuel.adam.free.fr/jade/doc/index.html).

## Eléments de base : agent, comportement et envoi de messages

### Agents sans comportement ni communication
- Pour tester l'installation de Jade, le classique HelloWorld
    - [AgentHello](https://github.com/EmmanuelADAM/jade/blob/master/helloworldSolo/AgentHello.java) : code pour un agent simple affichant un message sur la console
    - [AgentHelloParametre](https://github.com/EmmanuelADAM/jade/blob/master/helloworldSolo/AgentHelloParametre.java) : également code pour un agent simple affichant un message sur la console, mais le message est passé en paramètre

### Agents avec comportement
- Ajout de agencesVoyages.comportements simples à un agent, exemple d'exécution
    - [AgentHelloSalut](https://github.com/EmmanuelADAM/jade/tree/master/testComportement) : code pour un agent doté de agencesVoyages.comportements simples

### Agents communiquant
- Exemple classique de test de communication entre 2 agencesVoyages.agents : le ping-pong
    - [AgentPingPong](https://github.com/EmmanuelADAM/jade/blob/master/pingPong/AgentPingPong.java) : code pour qui permet de lancer deux agencesVoyages.agents qui communiquent entre eux. L'agent ping envoie un balle à l'agent pong qui la retourne à ping qui la renvoie à son tour, ...
- Exemple avec filtrage de messages : 
    - [ticTac](https://github.com/EmmanuelADAM/jade/tree/master/ticTac) : un agent envoie des messages taggés avec 2 types différents; un agent les reçoit et les traite différement selon leurs types. agent "decompte" envoie des msgs "tictac" taggés "CLOCK" toutes les secondes, puis un msg taggé "BOOM" au bout de 10s à l'agent "agentPiege"

### Agents interactifs et communicants
- Ajout d'une petite interface graphique pour faciliter le dialogue avec l'utilisateur
    - [fenetre](https://github.com/EmmanuelADAM/jade/tree/master/fenetre) : codes pour des agencesVoyages.agents liés à des fenêtres
    - [radio](https://github.com/EmmanuelADAM/jade/tree/master/radio) : codes qui ilustrent la communication par broadcast. Un agent ne cible plus des destinataires, mais un canal qu'écoutent d'autres agencesVoyages.agents

## Gestion de services : déclaration et recherche
- [HelloWorld](https://github.com/EmmanuelADAM/jade/tree/master/HelloWorld) : codes pour des agencesVoyages.agents liés chacun à une fenêtre. Ces agencesVoyages.agents se découvrent par reherche de services, et s'envoient des messages simples

## Protocoles d'interactions entre agencesVoyages.agents
- [AchieveRE](https://github.com/EmmanuelADAM/jade/tree/master/protocoles) : codes illustrant la communication entre agencesVoyages.agents par le protocoles AchieveRE. Un agent soumet un requête à des agencesVoyages.agents par ce protocole et attend leurs réponses.
- [agentsVoyage](https://github.com/EmmanuelADAM/jade/tree/master/agentsVoyage) : codes pour des agencesVoyages.agents simulant des agences de voyages
