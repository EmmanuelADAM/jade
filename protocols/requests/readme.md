# Jade : Agents and protocols

##  FIPA Request Protocol example

---
### Jade Agent-Oriented Programming Course Materials

---

---

Here you will find an example of communication via the [FIPA Request Interaction](http://www.fipa.org/specs/fipa00026/SC00026H.html) protocol.

- An 
[AgentRequestSender](https://github.com/EmmanuelADAM/jade/blob/english/protocols/requests/agents/AgentRequestSender.java)
  agent
sends a requests  (a sum : "34+12+45" for example) to  [AgentRequestResponder](https://github.com/EmmanuelADAM/jade/blob/english/protocols/requests/agents/AgentRequestResponder.java) agents that agree to answer 
  or refuse.
- The AchieveRE  protocol obliges the recipients to respond (refusal, error, agreement) and to inform of a result 
  if  agreement.
- The sender must therefore plan to process these different return messages. A protocol facilitates this support of 
  exchanges
- [LaunchAgents](https://https://github.com/EmmanuelADAM/jade/blob/english/protocols/requests/launch/LaunchAgents.
  java) : **main class**, launches Jade and creates 10 agents: 1 Sender, 10 responders.

Protocol for the sender: 

<!--
```
@startuml RequestInitiator
!pragma layout smetana

hide empty description
state CreateRequest : nb receivers\ni<-0
[*] -- > CreateRequest
CreateRequest -- > WaitMsg
handleRefuse<-WaitMsg : refuse
state c <<choice>>
handleRefuse -- > c:i<-i+1
c-> WaitMsg:i<nb

WaitMsg->handleAgree : agree
state forkAgree   <<fork>>


handleAgree -- > forkAgree
WaitMsg <- handleAgree  
WaitMsg -- > forkAgree
forkAgree -- > handleInform : inform msg \nfollowing an agree msg 
handleInform -- > c:i<-i+1
c--- > handleAllResults:[i==nb]\nall results


handleAllResults -- > [*]

@enduml```
-->

![](RequestInitiator.png)


Protocol for the responder:


<!--
```
@startuml RequestResponder

hide empty description
[*] -- > WaitRequest
state answerChoice <<choice>>
WaitRequest-- >answerChoice
answerChoice -- > Refuse
answerChoice -- > NotUnderstood
answerChoice -- > Accept
Accept-- > Inform
Refuse -- > [*]
NotUnderstood -- > [*]
Inform -- > [*]

@enduml```
-->

![](RequestResponder.png)

---