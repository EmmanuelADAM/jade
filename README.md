<meta name="description" content="Programming multi-agent in Java : use of an updated version of the Jade 
platform. Materials for Jade Tutorial : communication, protocols, votes, services, behaviors, ..." />

# Programming agents in Jade

[(version web)](https://emmanueladam.github.io/jade/)

Set of source code or the course/tutorial about multiagent programming with the JADE platform. 

To run these codes, it is necessary to import the library "[JadeUPHF.jar](https://github.com/EmmanuelADAM/JadeUPHF/blob/master/JadeUPHF.jar)". 
This library is a update, with JAVA 17, of the last official version of  [Jade]
(https://jade.tilab.com) of Tilab and add functionalities to easier the implementation of agents.

The source code of these new version, the library and notes about it are accessible from here : [JadeUPHF](https://emmanueladam.github.io/JadeUPHF/)".

_The following codes need, thus, this library and Java >= 17._

----

## Basics: Agent, Behavior, and Sending Messages

### Agents without behavior or communication

- To test the installation of Jade, the classic HelloWorld
    - [AgentHello](https://github.com/EmmanuelADAM/jade/tree/english/helloworldSolo/AgentHello.java) : code for a 
      simple agent displaying a message on the console
    - [AgentHelloParametre](https://github.com/EmmanuelADAM/jade/tree/english/helloworldSolo/AgentHelloParametre.java) :
      also code for a simple agent displaying a message on the console, but the message is passed as a parameter

### Agents with behaviors

- Add simple behaviors to an agent, examples
    - [AgentHelloSalut](https://github.com/EmmanuelADAM/jade/tree/english/testComportement) : code for an agent with 
      simple behaviors
    - [AgentHelloEuropeenSequentiel](https://github.com/EmmanuelADAM/jade/tree/english/testComportement) : code pour deux
      agents dotés de comportements simples s'exécutant séquentiellement
    - [AgentHelloEuropeenParallel](https://github.com/EmmanuelADAM/jade/tree/english/testComportement) : code pour deux
      agents dotés de comportements simples s'exécutant en parallèle

### Communicating Agents

- Classic example of a communication test between 2 agents: ping-pong
    - [pingPong](https://github.com/EmmanuelADAM/jade/tree/english/pingPong): Code that launches two agents that 
      communicate with each other. The ping agent sends a ball to the pong agent which returns it to ping which in 
      turn returns it, ...
    - [AgentPingPlouf](https://github.com/EmmanuelADAM/jade/tree/english/pingPlouf): code for an agent that sends a 
      random message and waits for a response..! This will come from the white pages agent (AMS) telling him that 
      there is no one at the address indicated...
- Example with message filtering:
    - [ticTac](https://github.com/EmmanuelADAM/jade/tree/english/ticTac) : an agent sends messages tagged with 2 
      different types; an agent receives them and treats them differently according to their types.

---

## Interactive and communicative agents

- Use of a small graphical interface to facilitate dialogue with the user
    - [window](https://github.com/EmmanuelADAM/jade/tree/english/window) : codes for agents linked to windows
    - [radio](https://github.com/EmmanuelADAM/jade/tree/english/radio) : codes that illustrate broadcast 
      communication. An agent no longer targets recipients, but a channel that other agents are listening to

---
## Agents with FSM behaviors

- Agents that have behaviors organized according to a finite state machine (FSM)  : [fsm](https://github.com/EmmanuelADAM/jade/tree/english/fsm).
  Two examples are given : 
  - [salutations](https://github.com/EmmanuelADAM/jade/tree/english/fsm/salutations) : sequences of behavior for 
    European greetings within an agent,
  - [review](https://github.com/EmmanuelADAM/jade/tree/english/fsm/review) : process of submitting and reviewing an 
    article between an author agent, a journal agent and three reviewer agents

---
## Service Management: Reporting and Research

- [helloWorldService](https://github.com/EmmanuelADAM/jade/tree/english/helloWorldService) : Codes for agents each 
  linked to a window. These agents discover each other by searching for services and send each other simple messages
- [serviceDetection](https://github.com/EmmanuelADAM/jade/tree/english/serviceDetection) :  Some agents register to the 
  yellow pages (DF) for a given service; little by little.
  Another agent has subscribed to (DF) to be notified when an agent registers or unsubscribes from this service.

---
## Protocoles d'interactions entre agents

- [Requete](https://github.com/EmmanuelADAM/jade/tree/master/protocoles/requetes) : codes illustrant la communication
  entre agents par le protocole AchieveRE.
- [Enchere](https://github.com/EmmanuelADAM/jade/tree/master/protocoles/anglaisesscellees) : codes illustrant la
  communication entre agents par le protocole ContractNet pour une enchère anglaise à un tour (simple appel d'offres).
- [Enchere de Vickrey](https://github.com/EmmanuelADAM/jade/tree/master/protocoles/vickrey) : codes 
  illustrant la communication entre agents par le protocole ContractNet pour une enchère de Vickrey.
- [Vote Borda](https://github.com/EmmanuelADAM/jade/tree/master/protocoles/voteBorda) : codes illustrant la
  communication entre agents par le protocole ContractNet pour un vote de Borda.
- [Vote DoubleBorda](https://github.com/EmmanuelADAM/jade/tree/master/protocoles/voteDoubleBorda) : codes illustrant la
  communication entre agents par le protocole ContractNet pour un vote de Borda augmenté pour être effectué en 1 tour.
- [Vote Condorcet](https://github.com/EmmanuelADAM/jade/tree/master/protocoles/voteCondorcet) : codes illustrant la 
  communication entre agents par le protocole ContractNet pour un vote de Condorcet augmenté pour être effectué en 1 
  tour.
- [Negociation](https://github.com/EmmanuelADAM/jade/tree/master/protocoles/negociation) : codes à compléter 
  pour coder la négociation 1-1.

## Exemple complet, support à développements :

- [agentsVoyage](https://github.com/EmmanuelADAM/jade/tree/master/agencesVoyages) : codes pour des agents simulant des
  agences de voyages
