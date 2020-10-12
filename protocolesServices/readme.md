# Jade : Agents, protocole et services

## Exemple : FIPA Request Interaction Protocole en Jade
### Et utilisation de services

---

- Ici, vous trouverez un exemple de communication via le protocole [FIPA Request Interaction](http://www.fipa.org/specs/fipa00026/SC00026H.html)
  - un agent de type [AgentEmissionARE](https://github.com/EmmanuelADAM/jade/blob/master/protocoles/AgentEmissionARE.java) émet une requête 
  - auprès d'agents de type [AgentReceptionARE](https://github.com/EmmanuelADAM/jade/blob/master/protocoles/AgentReceptionARE.java)
  - Le protocole oblige les destinataires à répondre (refus, erreur, accord) et à informer d'un résultat en cas d'accord
  - L'émetteur doit donc prévoir de traiter ces différents messages de retour.
  - L'utilisation de protocole facilite cette prise en charge des échanges


- L'exemple est utilise la notion de services, par rapport à l'exemple simple sur le protocole AchiveRE. Les agents de calcul se déclarent auprès des pages jaunes; et l'agent demandeur récupère auprès des pages jaunes la liste des agents de calcul..

- Au lancement,  1 émetteur, des destinataires sont lancés. Un agent de calcul peut décider de refuser ou d'accepter la requête


- Il est possible de modifier le code pour que "a" émette plusieurs requêtes 
 ---