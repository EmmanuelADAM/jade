# Jade : Agents et comportements à états

## FMS : Machine State Machine

---

Supports pour le cours de programmation orientée agent en Jade

- [AgentHelloEuropeenFSM](https://github.com/EmmanuelADAM/jade/blob/master/fsm/salutations/AgentHelloEuropeenFSM.java) :
  code pour un agent qui organise 6 comportements de salutations selon une machine d'états finis.
    - il commence à effectuer le comportement A (état initial), puis soit exécute B ou C selon le retour de A.
    - selon le retour de B, exécute D ou E
    - la fin de C, la fin de D mènent à à E
    - E mène à F (état final)  ou retourne à A
