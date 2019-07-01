# jade
supports pour cours de jade

liste de supports pour le cours de programmation orientée agent en Jade
## Eléments de base : agent, comportement et envoi de messages
### Agents sans comportement ni communication
- Pour tester l'installation de Jade, le classique HelloWorld
    - [AgentHello](https://github.com/EmmanuelADAM/jade/blob/master/helloworldSolo/AgentHello.java) : code pour un agent simple affichant un message sur la console
    - [AgentHelloParametre](https://github.com/EmmanuelADAM/jade/blob/master/helloworldSolo/AgentHelloParametre.java) : également code pour un agent simple affichant un message sur la console, mais le message est passé en paramètre
### Agents avec comportement
- Ajout de comportements simples à un agent, exemple d'exécution
    - [AgentHelloSalut](https://github.com/EmmanuelADAM/jade/blob/master/testComp01/AgentHelloSalut.java) : code pour un agent doté de comportements simples
### Agents communiquant
- Exemple classique de test de communication entre 2 agents : le ping-pong
    - [AgentPingPong](https://github.com/EmmanuelADAM/jade/blob/master/pingPong/AgentPingPong.java) : code pour qui permet de lancer deux agents qui communiquent entre eux. L'agent ping envoie un balle à l'agent pong qui la retourne à ping qui la renvoie à son tour, ...
### Agents interactifs
- Ajout d'une petite interface graphique pour faciliter le dialogue avec l'utilisateur
    - [fenetre](https://github.com/EmmanuelADAM/jade/tree/master/fenetre) : codes pour des agents liés à des fenêtres
## Gestion de services : déclaration et recherche
- [HelloWorld](https://github.com/EmmanuelADAM/jade/tree/master/HelloWorld) : codes pour des agents liés chacun à une fenêtre. Ces agents se découvrent par reherche de services, et s'envoient des messages simples

## Protocoles d'interactions entre agents
- [AchieveRE](https://github.com/EmmanuelADAM/jade/tree/master/protocoles) : codes illustrant la communication entre agents par le protocoles AchieveRE. Un agent soumet un requête à un agent par ce protocole et attend sa réponse.
- [agentsVoyage](https://github.com/EmmanuelADAM/jade/tree/master/agentsVoyage) : codes pour des agents simulant des agences de voyages
