# Jade : Agents and protocols

##  FIPA Contract-Net Protocol example

---
### Jade Agent-Oriented Programming Course Materials
#### Vickrey auction

---

Here, you can find an example of communication through the well known Contract-Net protocol [FIPA Contract Net]
(http://www.fipa.org/specs/fipa00029/SC00029H.html) for a 
Vickrey auction.
A Vickrey auction is a sealed-bid second-price auction (SBSPA); a 1 round auction where the agent that have proposed 
the highest price wins the auction, and pays the 2nd highest price... 


- an agent  
- [AuctioneerAgent.java](https://github.
  com/EmmanuelADAM/jade/blob/master/protocoles/vickrey/agents/AuctioneerAgent.java)
  launches a call for bidding to
- agents
 [ParticipantAgent](https://github.com/EmmanuelADAM/jade/blob/master/protocoles/vickrey/agents/ParticipantAgent.java)
- The protocole obliges
    - receivers : to respond (refuse, error, agree) to
    - the auctioneer agent to make a choice and :
      - to send a reject message to agents whose the bid is rejected
      - to send an agree message to the winner
    - the winner : to confirm its bidding

- [LaunchAgents](https://https://github.com/EmmanuelADAM/jade/blob/master/protocoles/anglaisesscellees/launch/LaunchAgents.java) : **
  - 1 auctioneer and 10 participants are created.
  - The auctioneer launches an auction for each activation of the 'go' button...
  - A participant can decide to refuse and to not submit a bid

---
