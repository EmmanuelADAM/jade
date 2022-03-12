# Jade : Agents

## Exemple de diffusion d'information par broadcast

---

- [AgentDiffuseur](https://github.com/EmmanuelADAM/jade/blob/master/radio/agents/AgentDiffuseur.java) : classe pour un
  agent qui diffuse de l'information sur un topic radio
- [AgentAuditeur](https://github.com/EmmanuelADAM/jade/blob/master/radio/agents/AgentAuditeur.java) : classe pour un
  agent qui se branche sur un topic radio pour écouter les messages et les affiche
- [LaunchAgents](https://github.com/EmmanuelADAM/jade/blob/master/HelloWorld/launch/LaunchAgents.java) : **classe
  principale**, lançant Jade et créant les agents

- au lancement, 4 agents sont lancés, un émetteur, 3 auditeurs, leur nombre n'est pas limité hormis par la capacité de
  la machine.
