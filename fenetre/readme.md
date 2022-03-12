# Jade : Agents & Fenêtres

## Exemple basique de communication directe via une fenêtre de dialogue.

---

Supports pour le cours de programmation orientée agent en Jade

Les agents ici sont de types `AgentWindowed`, ils possèdent une simple fenêtre (`SimpleWindow4Agent`) pour y poster des messages.
 - Cette fenêtre possède également 1 bouton, activable.
 - Un clic sur le bouton poste un événement de type `GuiEvent`  (`SimpleWindow4Agent.OK_EVENT` (= à 1)) à l'agent
 - Une fermeture de la fenêtre poste un événement de type `GuiEvent` (`SimpleWindow4Agent.QUIT_EVENT` (= à -1)) à l'agent
 - Par defaut, la fonction de réaction à un événement `protected void onGuiEvent(GuiEvent ev)` tue l'agent en cas de fermeture de fenêtre


- [AgentDirectEmissionButton](https://github.com/EmmanuelADAM/jade/blob/master/fenetre/agents/AgentDirectEmissionButton.java) :
  agent de type  *AgentWindowed*
    - en réaction au clic sur le bouton, il envoie un simple message texte à 3 agents "b", "c"
      & "d"
- [AgentReceptionClassique](https://github.com/EmmanuelADAM/jade/blob/master/fenetre/agents/AgentReceptionClassique.java) :
  agent de type  *AgentWindowed*
    - l'agent possède un comportement cyclique qui affiche sur sa fenêtre les messages qu'il reçoit
- [LaunchAgents](https://github.com/EmmanuelADAM/jade/blob/master/HelloWorld/launch/LaunchAgents.java) : **classe
  principale**, lançant Jade et créant les agents

- Au lancement, 4 agents sont lancés, "a" apte à émettre un message, "b", "c", "d" qui attendent des messages

---