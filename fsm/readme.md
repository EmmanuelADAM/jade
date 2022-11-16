# Jade : Agents et states behaviors

## FMS : Finite State Machine

---

Jade Agent-Oriented Programming Course Materials

- [AgentHelloEuropeenFSM](https://github.com/EmmanuelADAM/jade/blob/english/fsm/salutations/AgentHelloEuropeenFSM.
  java) :
  code for an agent that organizes 6 greeting behaviors according to a finite state machine.
    - it starts performing behavior A (initial state), then performs B or C depending on the result of A.
    - according to the return of B, executes D or E
    - end of C, end of D lead to E
    - E leads to F (final state) or returns to A

- In the package [Review](https://github.com/EmmanuelADAM/jade/blob/english/fsm/review/) agents simulate the 
  principle of depositing a research article:
    - an author submits an article to a journal
    - this journal receives it and sends this article to 3 reviewers
    - when all 3 evaluations are received (a score of 0 to 2),
        - if a 0 is received, the item is refused
        - if all marks are 2, the article is accepted without modification
        - otherwise it is proposed to the author to correct
    - if the author agrees to correct, he returns the document to the journal (return step 2)
    - otherwise, he refuses to continue and informs the newspaper.

- Now create the trading principle with FSM-like behaviors...
  - two agents: Buyer and Seller interact
    - Buyer has a max _maxi_ price that he does not wish to exceed
    - Seller has a minimum price that he does not wish to exceed and offers a sale price
    - Buyer offers a price < _maxi_
    - Seller offers a price lower than the sale price
    - this continues until the positive or negative stop (rejection of the negotiation) by one of the two agents
    