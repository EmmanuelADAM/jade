# Jade : Agents et comportements à états

## FMS : Machine State Machine

---

Supports pour le cours de programmation orientée agent en Jade


- Dans le package [Review](https://github.com/EmmanuelADAM/jade/blob/master/fsm/review/) des agents simulent le principe
  de dépôt d'article de recherche :
    - un auteur soumet un article à un journal
    - ce journal réceptionne et envoie l'article à 3 reviewers
    - lorsque les 3 reviews sont reçues (une note de 0 à 2),
        - si un 0 est reçu, l'article est refusé
        - si toutes les notes sont de 2, l'article est accepté sans modification
        - sinon il est proposé à l'auteur de corriger
    - si l'auteur accepte de corriger, il retourne le document au journal (retour étape 2)
    - sinon il refuse de poursuivre et en informe le journal Reprenez pour cela la trame des agents utilisé lors
      d'enchère ou de vote..

<!-- 
```
@startuml fsmReview

hide empty description

[*] -> A:soumission
A:soumission -- > J:reception
state JDispatch <<fork>>
J:reception -- > JDispatch
JDispatch -- > R1:Relecture
JDispatch -- > R2:Relecture
JDispatch -- > R3:Relecture
state JCollect <<fork>>
R1:Relecture -- > JCollect
R2:Relecture -- > JCollect
R3:Relecture -- > JCollect
state resultat <<choice>>
JCollect -- > resultat
resultat -- > J:Refus
resultat -- > J:Acceptation
resultat -- > J:Corrections
J:Refus -- > [*]
J:Acceptation -- > [*]
A:Decision <-- J:Corrections
state decision <<choice>>
decision <-- A:Decision
A:ReSoumission <-- decision
J:reception <-- A:ReSoumission
[*] <-- decision 
@enduml```
-->

<img src="fsmReview.png" alt="reseau v2" height="400"/>

