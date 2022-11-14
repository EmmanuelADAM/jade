# Jade : Agents

## Exemple de filtrage de message dans les boites aux lettres

---

- [SenderAgent](https://github.com/EmmanuelADAM/jade/blob/english/ticTac/AgentPosteur.java) : is a class for an 
  agent that has 2 communication behaviors :
    - a cyclic behavior which every 1000ms emits a message tagged "CLOCK"
    - a delayed behavior which after 10000ms emits a message tagged "BOOM"
- [DeminerAgent](https://github.com/EmmanuelADAM/jade/blob/english/ticTac/AgentDemineur.java) : is a class for an 
  agent picking messages of different types from its mailbox (CLOCK, BOOM).
- [LaunchAgents](https://https://github.com/EmmanuelADAM/jade/blob/english/protocoles/voteBorda/launch/LaunchAgents.java) : **
  Main class**, launch Jade and create 2 agents.

---
Bellow a communicative diagram between the 2 agents.
<!--
```
@startuml tictac
participant sender
participant deminer
group TickerBehaviour : TicTacBehaviour [each seconde]
  sender -> deminer  : "TicTac"
  deminer -> deminer : display "tictac"
end

group WakerBehaviour : [in 10 secondes]
    sender -> deminer: "boom"
    deminer -> deminer : display "alert!"
    sender -> sender : remove 'TicTacBehaviour'
end

@enduml```
-->

![](tictac.png)