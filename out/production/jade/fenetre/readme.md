# Jade : Agents & Fenêtres

## Exemple basique de communication directe via une fenêtre de dialogue.

---

Supports pour le cours de programmation orientée agent en Jade
- [AgentWindowed](https://github.com/EmmanuelADAM/jade/blob/master/fenetre/agents/AgentWindowed.java) : petite classe qui étend la classe `GuiAgent` capable de recevoir des événements d'une fenêtre
- [AgentDirectEmissionButton](https://github.com/EmmanuelADAM/jade/blob/master/fenetre/agents/AgentDirectEmissionButton.java) : agent de type  *AgentWindowed* 
   - à la reception d'un événement venant de sa fenêtre associée, il envoie un simple message texte à 3 agents "b", "c" & "d"
- [AgentReceptionClassique](https://github.com/EmmanuelADAM/jade/blob/master/fenetre/agents/AgentReceptionClassique.java) : agent de type  *AgentWindowed* 
  - l'agent possède un comportement cyclique qui affiche sur sa fenêtre *tous* les messages qu'il reçoit
- [SimpleGui4Agent](https://github.com/EmmanuelADAM/jade/blob/master/HelloWorld/gui/SimpleGui4Agent.java) : une classe pour une fenêtre minimale, liée à un agent de type *GuiAgent*. 
  - Pour dialoguer avec son agent (codé comme processus), cette fenêtre (nécessairement un processus) lui poste un événement

- au lancement, 4 agents sont lancés, "a" apte à émettre un message, "b", "c", "d" qui attendent des messages
---