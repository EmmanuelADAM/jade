<meta name="description" content="Programming multi-agent in Java : use of an updated version of the Jade 
platform. Materials for Jade Tutorial : communication, protocols, votes, services, behaviors, ..." />

# Programming agents in Jade

[(web version)](https://emmanueladam.github.io/jade/)
[(french version)](https://github.com/EmmanuelADAM/jade/tree/master/)

Case study on the Circular Economy.

----
## Circular economy

Here is a scenario:
 
 - some users have products
 - some product can be broken or become obsolescent 
 - several solutions exists:
   - tuto on website
   - repair coffees
   - spare parts stores
   - distributors.

- When the product doesn't work anymore; the user try to find a help : by itself, locally (repair coffee), in a larger 
region, and finally to the distributors.

- Solutions can exist on web, but the user can need a repair coffee to understand, to fix its product.

We can add these specification for a simple scenario : 
- a product have from 1 to 4 removable/fixable elements, we name them el1, el2,el3,el4.
- a breakdown is focused on 1 elt.
- a breakdown can be very light (0), easy(1), average (2), difficult(3), definitive (4)
  - this level of pb is detected during the reparation  
- a user has a skill for repairing,this skill can be of a level: 
  - 0 (unable to repair by itself and understand), 
  - 1 (can understand breakdown level up to 1, but only repair level 0),
  - 2 (can understand and repair breakdown level up to 2,
  - 3 (can understand and repair breakdown level up to 3,

- repair coffees have no items; they advise the user about the elt to find/buy
- spare parts stores have only 10 items of elt 1,2 or 3 and do not have elt4
- distributors have not pb of elt

- repair coffees have a cost of 5€/elt (the drink you buy)
- spare parts stores have a cost of 30€/elt
- spare parts stores specialised on second-hand material have a cost of 15€/elt
- distributors have a cost of 60€/elt

- a user has a limited amount of money, and time. 
  - we suppose he/she chooses to go to repair coffees and if the reparation is impossible (no more time, enough money)  : 
    - he/she go the  higher level (a spare parts store)
    - or stop and let the elt of her/his product to the repair coffee..

- a user can have preference, and different level on confidence about the repair coffees, spare parts stores (less confidence in second hand product), distributors.

---
### Design
1. Define the agents and draw the diagrams (sequences diagram,  activities (optional for the moment), states(optional for the moment), ...) with Plant-UML 
to simulate the behaviour in several use cases you choose (ask for a repair, ....).

2. Build some agents using the new Jade Library to simulate this behaviour.
- Generate a random object for the user agents, 
- and random elt in the store and coffee.
- 
---
Next repair coffee can interact between them to exchange information.
 - this has a cost in time..1/2 day/elt

We add a second criteria, the time:
- repair coffee have a cost of 3 days/elt (you have to plan a 'rendez-vous', they teach you how to repair, and they do not have elt, only tools)
- spare parts stores  have a cost of 2 day (they have the specific piece well adapt, 1 day to send, but you need 1 day to install it)
- distributors have a cost of 1 day (they simply exchange the elt)

---
