# Jade : Agents et protocole

## Exemple : FIPA Request Interaction Protocole en Jade

---

- Ici, vous trouverez un exemple de communication via le protocole [FIPA Request Interaction](http://www.fipa.org/specs/fipa00026/SC00026H.html)
  - un agent de type [AgentEmissionARE](https://github.com/EmmanuelADAM/jade/blob/master/protocoles/AgentEmissionARE.java) émet une requête 
  - auprès d'agencesVoyages.agents de type [AgentReceptionARE](https://github.com/EmmanuelADAM/jade/blob/master/protocoles/AgentReceptionARE.java)
  - Le protocole oblige les destinataires à répondre (refus, erreur, accord) et à informer d'un résultat en cas d'accord
  - L'émetteur doit donc prévoir de traiter ces différents messages de retour.
  - L'utilisation de protocole facilite cette prise en charge des échanges

- L'exemple est basique, les agencesVoyages.agents sont accompagnés de fenêtres pour affichage et communiquent directement 
  - l'agent "a" émet une requête "sum 4,5,6" auprès des agens "b", "c", "d" qui calculent et retournent leurs résultats

- Au lancement, 4 agencesVoyages.agents sont donc lancés : 1 émetteur, 3 destinataires

- Il est possible de modifier le code pour que "a" émette plusieurs requêtes 
 ---