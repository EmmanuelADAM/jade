# Jade : Agents 

## Exemple de diffusion d'information par broadcast

---

- [AgentDiffuseur](https://github.com/EmmanuelADAM/jade/blob/master/radio/agents/AgentDiffuseur.java) : classe pour un agent qui diffuse de l'information sur un topic radio
- [AgentAuditeur](https://github.com/EmmanuelADAM/jade/blob/master/radio/agents/AgentAuditeur.java) : classe pour un agent qui se branche sur un topic radio pour écouter les messages et les affiche
- [AgentWindowed](https://github.com/EmmanuelADAM/jade/blob/master/radio/agents/AgentWindowed.java) : est une classe d'agent qui étend légèrement la classe `GuiAgent` pour offrir aux agents une mini interface graphique
- [SimpleWindow4Agent](https://github.com/EmmanuelADAM/jade/blob/master/radio/gui/SimpleWindow4Agent.java) : une classe pour une fenêtre minimale, liée à un agent de type *GuiAgent*. 

- au lancement, 4 agents sont lancés, un émetteur, 3 auditeurs, leur nombre n'est pas limité hormis par la capacité de la machine.
