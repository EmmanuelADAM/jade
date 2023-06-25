# Jade : Agents and protocols

##  FIPA Contract-Net Protocol example

---
### Jade Agent-Oriented Programming Course Materials
#### Condorcet vote

---

Here, you can find an example of communication through the well known Contract-Net protocol [FIPA Contract Net]
(http://www.fipa.org/specs/fipa00029/SC00029H.html) for a
Condorcet vote (Voters rank the options by evaluating them, a calculation of the nb of duels (head-to-head election) won is made)


- *Method "de Condorcet"* ***in a single ballot***
  - a1 proposes o6 > o4 > o5 > o7 > o1 > o3 > o2
  - a2 proposes o7 > o6 > o3 > o1 > o4 > o2 > o5
  - a3 proposes o1 > o2 > o3 > o4 > o6 > o7 > o5
  - a4 proposes o4 > o7 > o2 > o1 > o6 > o5 > o3
  - a5 proposes o1 > o2 > o4 > o3 > o5 > o7 > o6


- The polling station assigns values on the number of duels won-number of duels lost.
    - Ex. : option1 is preferred 4 times  to option2 and 1 one time behind, thus value of the duel(option1, 
      option2)=3 
      and here, in return duel(option2, option1)=-3 (in Condorcet's initial proposal, we do not count the lost duels )
    - bellow the matrix of the duels values.
      - option1, option4 and option7 wons against 5 other options


|   - | o1  | o2  | o3  | o4  | o5  | o6  | o7  | duels gagnés | points |
|----:|:---:|:---:|:---:|:---:|:---:|:---:|:---:|:------------:|:------:|
|  o1 | 0   |  3  |  3  |  1  | 3   |  1  | -1  |      5       |   10   |
|  o2 | -3  |  0  |  1  | -1  |  3  |  1  | -1  |      3       |   0    |
|  o3 | -3  | -1  |  0  | -1  |  1  | -1  | -1  |      1       |   -6   |
|  o4 | -1  |  1  |  1  |  0  |  5  |  1  |  3  |      5       |   10   |
|  o5 | -3  | -3  | -1  | -5  |  0  | -3  | -1  |      0       |  -16   |
|  o6 | -1  | -1  |  1  | -1  |  3  |  0  | -1  |      2       |   0    |
|  o7 |  1  |  1  |  1  | -3  |  1  |  1  |  0  |      5       |   2    |

   

- According to the principle of the Condorcet vote, the winning option is the one that wins the most series of duels 
  in front of each of the other options.
- In case of a tie, the winners are decided according to the number of points (number of duels won-number of duels lost)
  - Here, option1 and option4 have both 10 points, option 7 only gets 2. 
  - It is then necessary to decide between the first two.

- In the literature, many proposals  try to solve this kind of cases
- Here, we create a new gain matrix based solely on the selected options (here option 1 & option 4).

| -   | o1  | o2  | o3  | o4  | o5  | o6  | o7  | duels gagnés | points |
|:---:|:---:|:---:|:---:|:---:|:---:|:---:|:---:|:------------:|:------:|
| o1  |  0  |  0  |  0  |  1  |  0  |  0  |  0  |      1       |   1    |
| o2  |  0  |  0  |  0  |  0  |  0  |  0  |  0  |      0       |   0    |
| o3  |  0  |  0  |  0  |  0  |  0  |  0  |  0  |      0       |   0    |
| o4  | -1  |  0  |  0  |  0  |  0  |  0  |  0  |      0       |   -1   |
| o5  |  0  |  0  |  0  |  0  |  0  |  0  |  0  |      0       |   0    |
| o6  |  0  |  0  |  0  |  0  |  0  |  0  |  0  |      0       |   0    |
| o7  |  0  |  0  |  0  |  0  |  0  |  0  |  0  |      0       |   0    |

  - Option1 wins the most series of duels (1 against option4 (one has 3x o1>o4 and 2x o4>o1)), it is therefore this 
    option that is finally declared elected.

- If a tie still exists between options after this 2nd phase, a random draw is made between them.

----
### Codage

- an agent [PollingStationAgent](https://github.com/EmmanuelADAM/jade/blob/master/protocoles/voteCondorcet/agents/PollingStationAgent.java)
   calls for a vote on restaurants : pizzeria, vegetables, sushi, ...
- to agents [ParticipantAgent](https://github.com/EmmanuelADAM/jade/blob/master/protocoles/voteCondorcet/agents/ParticipantAgent.java)
- The protocole obliges :
    - the receivers : to respond (refuse, error, agree), if agreement, the answer has this form : 
      legumes>pizzeria>sushi>...
    - the polling station :
      - creates the duels' matrix :
          - It sends the winning choice to all voters (or the choices if tied)
          - each voter confirms receipt and acceptance of the result of the vote
    - If tied (in number of duels and number of points), the vote is restarted for the choices not decided

- [LaunchAgents](https://https://github.com/EmmanuelADAM/jade/blob/master/protocoles/anglaisesscellees/launch
  /LaunchAgents.java) : **main class**
    - 1 polling station and 5 voting agent are created.
    - The polling station launches call for voting for each activation of the 'go' button...


 ---