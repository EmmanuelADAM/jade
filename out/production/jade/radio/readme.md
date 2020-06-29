# Jade : Agents 

## Exemple de diffusion d'information par broadcast
---

- [AgentDiffuseur](https://github.com/EmmanuelADAM/jade/blob/master/radio/agents/AgentDiffuseur.java) : classe pour un agent qui diffuse de l'information sur un topic radio
- [AgentAuditeur](https://github.com/EmmanuelADAM/jade/blob/master/radio/agents/AgentAuditeur.java) : classe pour un agent qui se branche sur un topic radio pour 
- [HelloAgent](https://github.com/EmmanuelADAM/jade/blob/master/HelloWorld/agents/HelloAgent.java) : est une classe d'agent liée à une interface graphique.
  - l'agent s'inscrit dans les pages jaunes dans le service de type "cordialite"
  - un clic sur sa fenêtre associée déclenche un événement qu'il capture pour effectuer les actions :
    - recherche des autres agents de service cordialités
    - envoi du message texte transmis par l'événement
  - l'agent possède un comportement cyclique qui affiche sur sa fenêtre *tous* les messages qu'il reçoit
- [SimpleGui4Agent](https://github.com/EmmanuelADAM/jade/blob/master/HelloWorld/gui/SimpleGui4Agent.java) : une classe pour une fenêtre minimale, liée à un agent de type *GuiAgent*. 
  - Pour dialoguer avec son agent (codé comme processus), cette fenêtre (nécessairement un processus) lui poste un événement

- au lancement, 3 agents sont lancés, le nombre n'est pas limité hormis par la capacité de la machine. Dans le code de [LaunchAgents](https://github.com/EmmanuelADAM/jade/blob/master/HelloWorld/launch/LaunchAgents.java) se trouve un code dommenté pour tester 100 agents...
