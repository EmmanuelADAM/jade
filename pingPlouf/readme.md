# Jade : Agents and communication

## Basic example : "Ping"-"Pong" in Jade, with error

---

Jade Agent-Oriented Programming Course Materials

- [AgentPingPlouf](https://github.com/EmmanuelADAM/jade/blob/english/pingPong/AgentPingPlouf.java) : code for an 
  agent that sends a message to an agent that does'nt exist in the platform! and wait for a response... This one 
  will comme from the white pages (AMS) indicating it that there is no one at the given address...

---

<!--
```
@startuml pingplouf
participant ping
participant pong
participant AMS 

ping ->] : msg for 'tzoing'
AMS -[#red]> ping :  FAILURE 
@enduml
```
-->

![](pingplouf.png)