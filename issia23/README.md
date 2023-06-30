<meta name="description" content="Programming multi-agent in Java : use of an updated version of the Jade 
platform. Materials for Jade Tutorial : communication, protocols, votes, services, behaviors, ..." />

# Programming agents in Jade

[(web version)](https://emmanueladam.github.io/jade/)
[(french version)](https://github.com/EmmanuelADAM/jade/tree/master/)

Case study for ISSIA'23.

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

- repair coffee have only 4 items of elt 1,2 or 3 and do not have elt4
- spare parts stores have only 10 items of elt 1,2 or 3 and do not have elt4
- distributors have not pb of elt

- repair coffee have a cost of 10€/elt (second hand)
- spare parts stores  have a cost of 30€/elt
- distributors have a cost of 60€/elt

- a user has a limited amount of money. 
  - we suppose he/she chooses to go to repair coffee and if the reparation is impossible here : 
    - he/she go the  higher level (a spare parts store)
    - or stop and let the elt of her/his product to the repair coffee..

---
Draw and build some agents using the new Jade Library to simulate this behaviour.
- Generate a random object for the user agents, 
- and random elt in the store and coffee.
- 
---
Next repair coffee can interact between them to exchange piece.
 - this has a cost in time..1 day/elt

We add a second criteria, the time:
- repair coffee have a cost of 2 days/elt (they teach you how to repair, and the ele doesn't fit very much)
- spare parts stores  have a cost of 1 day (they have the specific piece well adapt but you need 1 day to install it)
- distributors have a cost of 1 day (they simply exchange the elt)

---
