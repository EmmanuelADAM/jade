# Jade : Agents and protocols

##  FIPA Contract-Net Protocol example

---
### Jade Agent-Oriented Programming Course Materials

---


--- 

Here, you can find an example of communication through the well known Contract-Net protocol [FIPA Contract Net](http://www.fipa.org/specs/fipa00029/SC00029H.html).
A ``first-price sealed-bid'' is an english auction with only 1 round; it is in fact just a simple call for proposal.
The participant who proposed the most interesting offer wins the auction.<br>
It is up to you to define what a "best" proposition is.


- an [AuctioneerAgent](https://github.com/EmmanuelADAM/jade/blob/master/protocoles/anglaisesscellees/agents/AuctioneerAgent.java) agents launches a call for bid to [ParticipantAgent](https://github.com/EmmanuelADAM/jade/blob/master/protocoles/anglaisesscellees/agents/AgentParticipant.java) agents
- The protocol obliges:
    - the participants to answer (refuse, error, proposal)
    - the auctioneer:
        - to choose one proposal and to inform: 
          - the winner(s), if applicable, that it/they has/ve been selected
          - the others that their proposals are rejected 
    - each winner (if it exists) to confirm or cancel its offer

- the [LaunchAgents](https://https://github.com/EmmanuelADAM/jade/blob/master/protocoles/anglaisesscellees/launch/LaunchAgents.java) *main class*, launches Jade and create the agents
    - 1 auctioneer and 10 participants are created.
    - The auctioneer launches an auction for each activation of the 'go' button...
    - A participant can decide to refuse and to not submit a bid

---
You can change the code on this page to design a "classic" english auction: 

- the auctioneer : 
  - propose an object with a initial mark
  - wait for bid,
  - repropose the object with the best mark,
  - and so on as long as bids are proposed.

 ---