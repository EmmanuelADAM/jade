# Jade : Agents

## Exemple d'inscription à un service et d'utilisation des Pages Jaunes

---

- [HelloAgent](https://github.com/EmmanuelADAM/jade/blob/master/HelloWorldService/agents/HelloAgent.java) : est une
  classe d'agent liée à une interface graphique.
    - l'agent s'inscrit dans les pages jaunes dans le service de type "cordialite", et au hasard dans le sous service "reception" ou "accueil"
    - un clic sur sa fenêtre associée déclenche un événement qu'il capture pour effectuer les actions :
        - sélection aléatoire du sous-service avec lequel communiquer
        - recherche des agents de ce service
        - envoi du texte inscrit dans le champs texte aux agents de ce service (sauf lui-même)
    - l'agent possède un comportement cyclique qui affiche sur sa fenêtre principale les messages qui lui sont destinés
- [SimpleGui4Agent](https://github.com/EmmanuelADAM/jade/blob/master/HelloWorldService/gui/SimpleGui4Agent.java) : une
  classe pour une fenêtre minimale, liée à un agent de type *GuiAgent*.
- [LaunchAgents](https://https://github.com/EmmanuelADAM/jade/blob/master/helloWorldService/launch/LaunchAgents.java) : **
  classe principale**, lançant Jade et créant les agents

- au lancement, 10 agents sont lancés, le nombre n'est pas limité hormis par la capacité de la machine. 
