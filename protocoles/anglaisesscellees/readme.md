# Jade : Agents, protocole et services

## Exemple : FIPA Contract Net Protocole en Jade
### Utilisation pour une enchère

---

Ici, vous trouverez un exemple de communication via le protocole [FIPA Contract Net](http://www.fipa.org/specs/fipa00029/SC00029H.html) pour une enchère anglaise scellée, donc en  1 tour (le plus offrant remporte immédiatement l'enchère)
 - un agent de type [AgentCommissairePriseur](https://github.com/EmmanuelADAM/jade/blob/master/protocoles/anglaisesscellees/agents/AgentCommissairePriseur.java) émet un appel à enchères 
 - auprès d'agents de type [AgentParticipant](https://github.com/EmmanuelADAM/jade/blob/master/protocoles/anglaisesscellees/agents/AgentParticipant.java)
 - Le protocole oblige 
   - les destinataires à répondre (refus, erreur, accord)
   - au commissaire priseur 
     - de faire un choix et d'informer les agents n'ayant pas été retenus du rejet de leur offre
     - et d'informer l'agent choisit de l'acceptation de l'offre
     - l'agent ayant remporté l'enchère confirme ensuite sa position

 - [LaunchAgents](https://https://github.com/EmmanuelADAM/jade/blob/master/protocoles/requetes/launch/LaunchAgents.java) : **classe principale**, lançant Jade et créant les agents
    - Au lancement,  1 commissaire priseur et 10 participants sont lancés. 
    - Le commissaire priseur active une enchère à chaque clic sur 'go'..
    - un participant peut décider de refuser et de ne pas soumettre d'offre


Il est possible de modifier le code pour une enchère anglaise 'classique' :
- le commissaire priseur soumet un objet avec un prix de base, 
- recoit les offres
- re-emet l'objet avec comme base la meilleure offre
- et ainsi de suite tant que des offres sont proposées

 ---