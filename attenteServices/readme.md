# Jade : Agents

## Exemple de détection d'inscription à un service

---

- [AgentPointeur](https://github.com/EmmanuelADAM/jade/blob/master/attenteServices/agents/AgentPointeur.java) : classe pour un
  agent qui guette les inscriptions et desinscription sur un service donné. A chaque arrivée, départ, l'agent transmet les noms des membres du groupe à ces derniers.
- [AgentVenant](https://github.com/EmmanuelADAM/jade/blob/master/attenteServices/agents/AgentVenant.java) : classe pour un
  agent qui, au bout d'un certain temps s'inscrit à un service. L'agent affiche les messages qu'il reçoit. Clore la fenêtre tue l'agent qui se desinscrit de son service.
- [LaunchAgents](https://github.com/EmmanuelADAM/jade/blob/master/attenteServices/launch/LaunchAgents.java) : **classe
  principale**, lançant Jade et créant les agents

- au lancement, 11 agents sont lancés, un pointeur, 10 venant, leur nombre n'est pas limité hormis par la capacité de
  la machine.


