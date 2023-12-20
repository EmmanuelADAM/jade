<meta name="description" content="Programming multi-agent in Java : use of an updated version of the Jade 
platform. Materials for Jade Tutorial : communication, protocols, votes, services, behaviors, ..." />

# Programming agents in Jade

[(web version)](https://emmanueladam.github.io/jade/)
[(french version)](https://github.com/EmmanuelADAM/jade/tree/master/)

**Practical work : Case study on the Circular Economy.**
- by 2 persons max.
- no copy/paste between groups
- to provide : the source code, and a report containing explanation and uml diagrams
- detail about the report :
  - **Instructions**
    - to be carried out alone or in pairs (indicate the names of the authors in each class)
    - No identical codes between groups 
    - Join a readme.md that contains the following points :
      - Authors' names
      - General and up-to-date information on: the circular economy, intelligent workflows
      - Presentation of the practical work (10 lines max)
      - Diagrams made (in PNG or Plantuml sources)
      - A short text outlining what works, what didn't, what it struggles, and what you would have liked to have implemented 
    - No copy/paste :
      - between the readme.md files
      - from large blocks coming from Chat-GPT or other LLM-based engines, 
    - The readme.md has to be consistent in itself and with the codes.
  - deliver everything (source codes + readme) in the form of a ZIP file

----
## Circular economy

Here is a scenario:

- some users have products
- some products can be broken or become obsolescent
- several solutions exists:
    - tuto on website
    - repair coffees
    - spare parts stores
    - distributors.

- When the product doesn't work anymore; the user try to find a help : by itself, locally (repair coffee), in a larger
  region, and finally to the distributors.

- Solutions can exist on web, but the user can need a repair coffee to understand, to fix its product.

We can add these specification for a simple scenario :
- there are type of products : mouse :computer_mouse:, screen :desktop_computer:,  coffeeMaker :coffee:, washingMachine :dress: :jeans:, dishwasher :plate_with_cutlery:,  vacuumCleaner :tornado:
- the price of each product is (= ± 15%): mouse  (30€), screen(150€),  coffeeMaker(50€), washingMachine(300€), dishwasher(300€),  vacuumCleaner(100€)
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
- spare parts stores add a cost of at most 30% / elt
- spare parts stores specialised on second-hand material have a cost of at most 30% / elt but the cost of an elt is divided / 2
- for distributors the replacement is the price of the product (generated randomly from the specification)

- a user has a limited amount of money, and time.
    - we suppose he/she chooses to go to repair coffees and if the reparation is impossible (no more time, enough money)  :
        - he/she go the  higher level (a spare parts store)
        - or stop and let the elt of her/his product to the repair coffee..

- a user can have preference, and different level on confidence about the repair coffees, spare parts stores (less confidence in second hand product), distributors.

---
### Design
1. Define the agents and draw the diagrams (sequences diagram, use at least on activities diagram, or a state diagram to precise a part of your algorithm (ex. process of choice to repair to rebuy)) with Plant-UML
   to simulate the behaviour in several use cases you choose (ask for a repair, ....). **[8 points]**
    - elaborate all the different cases according to the description of this subject

2. Build some agents (2 users, 4 repair coffee, 3 part stores, 3 distributors) using the new Jade Library to simulate this behaviour.
    - Generate a random objects for the user agents and distributors,
    - random parts for the spare part stores.
    - and random specialities in repair coffee.
    - propose some gui **[5 points]**
    - <span style="background-color:yellow">*(some examples are given, see the ``agents``, ``data`` and ``gui`` packages; launch the ``LauchSimu`` class)*</span>

3. A user agent launch a CFP to find the best appropriate repair coffee.
    -  a repair coffee:
        - can accept if the product correspond to one of its specialities
        - propose a date (from the current date + 1 to 3 days (see LocalDate from Java)
    - the user:
        - choose the repair coffee with the nearest date
    - THE repair coffee
        - help the user to find which part to buy, *if it's repairable*.
            - if it's not repairable, the user
                - launches a CFP to find a new product
                - and chooses according to the price and its confidence to 2nde hand product or not
    - the user:
        - decides if he/she prefers buy a new product (or a second-hand product) or repair the product.
        - if he/she wants to repair:
            - launches a CFP to find the appropriate part and choose
            - repairs by her/him self if he/she has capability
            - or asks for a rendez-vous to a repair coffee
        - if he/she wants to buy a new one, the user launch a CFP to the distributors

      **[7 points]**

---

We add a second criteria, the time:
- repair coffee have a cost of 3 days/elt (you have to plan a 'rendez-vous', they teach you how to repair, and they do not have elt, only tools)
- spare parts stores  have a cost of 2 day (they have the specific piece well adapt, 1 day to send, but you need 1 day to install it)
- distributors have a cost of 1 day (they simply exchange the object)

---