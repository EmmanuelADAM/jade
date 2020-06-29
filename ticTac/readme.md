# Jade : Agents 

## Exemple de filtrage de message dans les boites aux lettres

---

- [AgentPosteur](https://github.com/EmmanuelADAM/jade/blob/master/ticTac/AgentPosteur.java) : est une classe  pour un agent qui possède 2 comportements de communication : 
  - un comportement cyclique qui toutes les 1000ms émet un message taggé "CLOCK"
  - un comportement à retardement qui au bout de 10000ms émet un message taggé "BOOM"
- [AgentLecteur](https://github.com/EmmanuelADAM/jade/blob/master/ticTac/AgentLecteur.java) : est une classe pour un agent piochant dans sa boite aux lettres des messages de différents types (CLOCK, BOOM).

- au lancement, 2 agents sont lancés