# jade

[(version web)](https://emmanueladam.github.io/jade/)

Liste de supports pour le cours de programmation orientée agent en JADE. 
Pour l'ensemble de ces codes, il est nécessaire de disposer dans vos projet de la librairie "[jade2022c.jar](https://github.com/EmmanuelADAM/JadeUPHF/releases/download/v22c/jade2022c.jar)". 
Cette librairie est une mise à jour basée sur Java 17 de la version de [Jade](https://jade.tilab.com) de Tilab et y ajoute des fonctionnalités pour un codage simplifié.


## Eléments de base : agent, comportement et envoi de messages

### Agents sans comportement ni communication

- Pour tester l'installation de Jade, le classique HelloWorld
    - [AgentHello](https://github.com/EmmanuelADAM/jade/blob/master/helloworldSolo/AgentHello.java) : code pour un agent
      simple affichant un message sur la console
    - [AgentHelloParametre](https://github.com/EmmanuelADAM/jade/blob/master/helloworldSolo/AgentHelloParametre.java) :
      également code pour un agent simple affichant un message sur la console, mais le message est passé en paramètre

### Agents avec comportements

- Ajout de comportements simples à un agent, exemple d'exécution
    - [AgentHelloSalut](https://github.com/EmmanuelADAM/jade/blob/master/testComportement) : code pour un agent doté de
      comportements simples
    - [AgentHelloEuropeenSequentiel](https://github.com/EmmanuelADAM/jade/blob/master/testComportement) : code pour deux
      agents dotés de comportements simples s'exécutant séquentiellement
    - [AgentHelloEuropeenParallel](https://github.com/EmmanuelADAM/jade/blob/master/testComportement) : code pour deux
      agents dotés de comportements simples s'exécutant en parallèle

### Agents communiquant

- Exemple classique de test de communication entre 2 agents : le ping-pong
    - [pingPong](https://github.com/EmmanuelADAM/jade/blob/master/pingPong) : code pour qui permet de lancer deux agents
      qui communiquent entre eux. L'agent ping envoie un balle à l'agent pong qui la retourne à ping qui la renvoie à
      son tour, ...
- Exemple avec filtrage de messages :
    - [ticTac](https://github.com/EmmanuelADAM/jade/tree/master/ticTac) : un agent envoie des messages taggés avec 2
      types différents; un agent les reçoit et les traite différement selon leurs types. agent "decompte" envoie des
      msgs "tictac" taggés "CLOCK" toutes les secondes, puis un msg taggé "BOOM" au bout de 10s à l'agent "agentPiege"

### Agents interactifs et communicants

- Utilisation d'une petite interface graphique pour faciliter le dialogue avec l'utilisateur
    - [fenetre](https://github.com/EmmanuelADAM/jade/tree/master/fenetre) : codes pour des agents liés à des fenêtres
    - [radio](https://github.com/EmmanuelADAM/jade/tree/master/radio) : codes qui illustrent la communication par
      broadcast. Un agent ne cible plus des destinataires, mais un canal qu'écoutent d'autres agents

### Agents avec comportements de type FSM

- Agents qui possèdent des comportements organisés selon une machine d'états finis (FMS)  : [fsm](https://github.com/EmmanuelADAM/jade/tree/master/fsm). 
Deux exemples sont donnés : 
  - [salutations](https://github.com/EmmanuelADAM/jade/tree/master/fsm/salutations) : enchaînements de comportement de salutations européennes au sein d'un agent,
  - [review](https://github.com/EmmanuelADAM/jade/tree/master/fsm/review) : processus de soumission et de review d'un article entre un agent auteur, un agent journal et trois agents reviewers

## Gestion de services : déclaration et recherche

- [helloWorldService](https://github.com/EmmanuelADAM/jade/tree/master/helloWorldService) : codes pour des agents liés
  chacun à une fenêtre. Ces agents se découvrent par recherche de services, et s'envoient des messages simples
- [attenteServices](https://github.com/EmmanuelADAM/jade/tree/master/attenteServices) : codes pour des agents liés
  chacun à une fenêtre. Des agents se déclarent aurpès des pages jaunes pour un service donné. 
  Un agent a demandé aux pages jaunes (DF) d'être averti en cas d'inscription/desinsciption à ce service.

## Protocoles d'interactions entre agents

- [Requete](https://github.com/EmmanuelADAM/jade/tree/master/protocoles/requetes) : codes illustrant la communication
  entre agents par le protocoles AchieveRE.
- [Enchere](https://github.com/EmmanuelADAM/jade/tree/master/protocoles/anglaisesscellees) : codes illustrant la
  communication entre agents par le protocole ContractNet pour une enchère simple.
- [Vote](https://github.com/EmmanuelADAM/jade/tree/master/protocoles/voteBorda) : codes illustrant la communication
  entre agents par le protocole ContractNet pour un vote.

## Exemple complet, support à développements :

- [agentsVoyage](https://github.com/EmmanuelADAM/jade/tree/master/agencesVoyages) : codes pour des agents simulant des
  agences de voyages
