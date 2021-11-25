# Jade : Agents et comportements à états

## FMS : Machine State Machine

---

Supports pour le cours de programmation orientée agent en Jade
- [AgentHelloEuropeenFSM](https://github.com/EmmanuelADAM/jade/blob/master/fsm/salutations/AgentHelloEuropeenFSM.java) : code pour un agent qui organise 6 comportements de salutations selon une machine d'états finis.
  - il commence à effectuer le comportement A (état initial), puis soit exécute B ou C selon le retour de A. 
  - selon le retour de B, exécute D ou E
  - la fin de C, la fin de D  mènent à à E
  - E mène à F (état final)  ou retourne à A

- Créez dans le package review des agents simulant le principe de dépôt d'article de recherche : 
  - un auteur soumet un article à un journal
  - ce journal réceptionne et envoie l'article à 3 reviewers
  - lorsque les 3 reviews sont reçues (une note de 0 à 2), 
    - si un 0 est reçu, l'article est refusé
    - si toutes les notes sont de 2, l'article est accepté sans modification
    - sinon il est proposé à l'auteur de corriger
  - si l'auteur accepte de corriger, il retourne le document au journal (retour étape 2)
  - sinon il refuse de poursuivre et en informe le journal
  Reprenez pour cela la trame des agents utilisé lors d'enchère ou de vote..
  
- Créez maintenant le principe d'enchère anglaise ouverte avec des comportement de type FSM...