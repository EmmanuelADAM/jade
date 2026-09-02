# Jade : Agents and protocols

##  FIPA Contract-Net Protocol example

---

### Jade Agent-Oriented Programming Course Materials

---

**Application to Borda Vote**

---

Here you will find an example of communication via the [FIPA Contract Net](http://www.fipa.org/specs/fipa00029/SC00029H.html) protocol 
for a "Borda" count for a vote : 
voters rank the options by rating them, a sum of the notes is then made.


- *Principle of voting "de Borda"*
  - a1 ranks option1 > option3 > option2
  - a2 ranks option1 > option3 > option2
  - a3 ranks option3 > option2 > option1
  - Each option receives points according to its ranking (on n options, the 1st receives n points,..., the last 1 point)
  - Here, the polling station sums up the points
      - option1 : 3+3+1 = 7 points
      - option2 : 1+1+2 = 4 points
      - option3 : 2+2+3 = 7 points
  - If ex-aequo, a new vote is asked between them (here option1 and option3)

---


- a  [PollingStationAgent](https://github.com/EmmanuelADAM/jade/blob/master/protocoles/voteBorda/agents/AgentBureauVote.java) agent issues a call for votes on restaurant names: pizzeria, vegetables, sushi,
- towards 
 [ParticipantAgent](https://github.com/EmmanuelADAM/jade/blob/master/protocoles/voteBorda/agents/AgentParticipant.java) agents
- The protocole obliges :
    - Protocol obliges recipients to respond (refuse, error, agreement).
      If agreed, the answer is in the form : vegetables > pizzeria > sushi > ...
    - the polling station :
        - to sum the points sent by the voters (if choice between n restaurants, for each vote, the 1st receives n 
          points, the 2nd (n-1),...)
        - to send the winning choice to all voters (or the choices if tied)
        - each voter confirms receipt and acceptance of the result of the vote
    - If tied, the vote is relaunched for the undecided choices

- The [LaunchAgents](https://https://github.com/EmmanuelADAM/jade/blob/master/protocoles/voteBorda/launch/LaunchAgents.java) : **main class**, launch Jade and create the agents :
    - 1 polling station and 5 participants are launched.
    - The polling station start a new vote when the button 'go' is clicked ...

 ---