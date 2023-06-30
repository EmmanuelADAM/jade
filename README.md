<meta name="description" content="Programming multi-agent in Java : use of an updated version of the Jade 
platform. Materials for Jade Tutorial : communication, protocols, votes, services, behaviors, ..." />

# Programming agents in Jade

[(web version)](https://emmanueladam.github.io/jade/)
[(french version)](https://github.com/EmmanuelADAM/jade/tree/master/)

Set of source codes for the course/tutorial on multi-agent programming with the JADE platform. 

To run these codes, it is necessary to import the library "[JadeUPHF.jar](https://github.com/EmmanuelADAM/JadeUPHF/blob/master/JadeUPHF.jar)". 
This library is an update, with JAVA 17, of the last official version of  [Jade]
(https://jade.tilab.com) of Tilab and add functionalities to easier the implementation of agents.

The source of this new version, the library and notes, can be accessed from here : [JadeUPHF](https://emmanueladam.github.io/JadeUPHF/)".

_The following codes need, thus, this library and Java >= 17._

><small>*NB. if you get this error in your prefered IDE while launching an example :*</small>
>><small>*SEVERE: Communication failure while joining agent platform: No ICP active*</small>
> 
><small>*The reason is that you have not closed the previous jade instance. Completely stop/close a jade run before 
> launching another one*</small>


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
    - [AgentHelloSalut](https://github.com/EmmanuelADAM/jade/tree/english/behaviorTests) : code for an agent with 
      simple behaviors
    - [AgentHelloEuropeenSequentiel](https://github.com/EmmanuelADAM/jade/tree/english/behaviorTests) :  code that 
      launches 2 agents that owns behaviors executing in sequential. 
    - [AgentHelloEuropeenParallel](https://github.com/EmmanuelADAM/jade/tree/english/behaviorTests) : code that launches 2 agents that owns behaviors executing in parallel.

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

## Interactive agents

- Use of a small graphical interface to facilitate dialogue with the user
    - [window](https://github.com/EmmanuelADAM/jade/tree/english/window) : codes for agents linked to windows
    - [radio](https://github.com/EmmanuelADAM/jade/tree/english/radio) : codes that illustrate broadcast 
      communication. An agent no longer targets recipients, but a channel that other agents are listening to

---
## Service Management

- [helloWorldService](https://github.com/EmmanuelADAM/jade/tree/english/helloWorldService) : Codes for agents each
  linked to a window. These agents discover each other by searching for services and send each other simple messages
- [serviceDetection](https://github.com/EmmanuelADAM/jade/tree/english/serviceDetection) :  Some agents register to the
  yellow pages (DF) for a given service; little by little.
  Another agent has subscribed to (DF) to be notified when an agent registers or unsubscribes from this service.

---
## Agents with FSM behaviors

- Agents that have behaviors organized according to a finite state machine (FSM)  : [fsm](https://github.com/EmmanuelADAM/jade/tree/english/fsm).
  Two examples are given : 
  - [salutations](https://github.com/EmmanuelADAM/jade/tree/english/fsm/salutations) : sequences of behavior for 
    European greetings within an agent,
  - [review](https://github.com/EmmanuelADAM/jade/tree/english/fsm/review) : process of submitting and reviewing an 
    article between an author agent, a journal agent and three reviewer agents


---
## Interaction protocols

- [Requests](https://github.com/EmmanuelADAM/jade/tree/english/protocols/requests) : codes for communication of 
  requests using the AchieveRE protocol.
- [English Auction](https://github.com/EmmanuelADAM/jade/tree/english/protocols/sealedEnglishAuction): codes 
  using the  communication protocol ContractNet to simulate a sealed english auction (1 round).
- [Vote Borda](https://github.com/EmmanuelADAM/jade/tree/english/protocols/bordaCount) : codes illustrating 
  communication between agents by the ContractNet protocol for vote by Borda count.
- *<yellow>(the following codes will be translated soon)*</yellow>
- [Vote DoubleBorda](https://github.com/EmmanuelADAM/jade/tree/english/protocols/voteDoubleBorda) : codes illustrant la
  communication entre agents par le protocole ContractNet pour un vote de Borda augmenté pour être effectué en 1 tour.
- [Vote Condorcet](https://github.com/EmmanuelADAM/jade/tree/english/protocols/voteCondorcet) : codes illustrant la 
  communication entre agents par le protocole ContractNet pour un vote de Condorcet augmenté pour être effectué en 1 
  tour.
- [Negociation](https://github.com/EmmanuelADAM/jade/tree/english/protocols/negociation) : codes à compléter 
  pour coder la négociation 1-1.

<!--## Exemple complet, support à développements :

- [agentsVoyage](https://github.com/EmmanuelADAM/jade/tree/master/agencesVoyages) : codes pour des agents simulant des
  agences de voyages
-->
---

## ISSIA 23 - Scenario

A (very) little scenario based on a true project about circular economy : [here](https://github.com/EmmanuelADAM/jade/tree/english/issia23)
