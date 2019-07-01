# Agence de voyages

Ces codes illustrent un petit cas d'étude permettant de manipuler le protocole de ContractNet.

Un voyageur souhaite aller d'un point `a` à un point `b` :
- il lance un appel d'offre à plusieurs agences de voyages, 
- certaines sont spécialisées dans les bus, les trains et.ou les voitures.
- ces agences envoient leurs catalogues de voyages possible
- le client fait son choix, et peut combiner différentes offres selon ses critères (coûts, temps, émission de CO2, ...)


- Des classes sont déjà proposées : 
    - La classe [AgenceAgent](https://github.com/EmmanuelADAM/jade/blob/master/agentsVoyage/agents/AgenceAgent.java) est une classe représentant une agence de voyage. Une agence dispose d'un catalogue de voyages qu'elle crée à partir d'un fichier csv (bus.csv, car.csv ou train.csv).
    - La classe [TravellerAgent](https://github.com/EmmanuelADAM/jade/blob/master/agentsVoyage/agents/TravellerAgent.java) représente le client, qui émet l'appel d'offres et effectue sont choix, **à coder**
    - La classe [AlertAgent](https://github.com/EmmanuelADAM/jade/blob/master/agentsVoyage/agents/AlertAgent.java) représente un agent capable d'émettre un appel radio (broadcast) signalant une alerte sur un tronçon de route.
    - La classe [LaunchSimu](https://github.com/EmmanuelADAM/jade/blob/master/agentsVoyage/launch/LaunchSimu.java) lance un client, 3 agents agence de voyages spécialisée, et 1 agent d'alertes.
    - La classe [ContractNetVente](https://github.com/EmmanuelADAM/jade/blob/master/agentsVoyage/comportements/ContractNetVente.java) code le comportement de réponse à un appel d'offre, il consiste simplement à envoyer le catalogue complet des voyages, il peut être optimisé.
    - La classe [ContractNetAchat](https://github.com/EmmanuelADAM/jade/blob/master/agentsVoyage/comportements/ContractNetAchat.java) code le comportement de lancement et de gestion d'un appel d'offres.  

- Dans le package [data](https://github.com/EmmanuelADAM/jade/tree/master/agentsVoyage/data) se trouvent les classes qui consruisent les chemins possibles.
- Dans le package [gui](https://github.com/EmmanuelADAM/jade/tree/master/agentsVoyage/gui) se trouvent les classes qui consruisent les feneêtre de dialogue avec les agents.

Le code s'exécute tel quel, mais le client ne peut effectuer qu'un choix par durée la plus courte parmi les voyages proposées.  
- **Proposez et codez** d'autres critères de choix
- **Proposez et codez** le comportement d'adaptation suite à la réception d'un message d'alerte sur un tronçon donné; pour le client, pour les agences.

**Beaucoup d'autres extensions sont possibles** : 
- négociations,
- multiplication d'agences, et gestion de la renommée
- création d'agence virtuelle : ne possédant pas les voyages, mais dotés d'une bonne renomée et faisant office de passerelle entre les clients et les vendeurs de voyage.


Pour information, les trajets possibles sont les suivants : 
- en bus, entre les villes a<->b, b<->c, b<->d, c<->d, c<>-e, d<->e, e<->f, 
- en train, entre les villes a<->d, d<->f, 
- en voiture (covoiturage), entre les villes a<->f, c<->f
- les coûts, vitesses, émissions de co2, confort ... dépendent du moyen utilisé
