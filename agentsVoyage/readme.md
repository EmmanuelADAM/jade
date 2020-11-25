# Jade : Agents 

## Agence de voyages

---

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

Le code utilise 
 - la librairie opencsv ([http://opencsv.sourceforge.net](http://opencsv.sourceforge.net)) version 3.9 attachée à ce dossier.
 - la librairie jade.jar à télécharger sur le site de [jade](https://jade.tilab.com).
-----
Le code s'exécute tel quel, mais le client ne peut effectuer qu'un choix par durée la plus courte parmi les voyages proposées.  
- <span style='color:red'> **Proposez et codez** la décrémentation du nb de places par voyage (on pose 3 places par trajet en voiture, 50 en bus, et 200 places par trajet en train),</span>
- <span style='color:red'>  **Proposez et codez** le comportement de réaction suite à la réception d'un message d'alerte sur un tronçon donné; 
  - pour les agences (disparition des trajets impactés), 
  - pour le client (relance d'une demande de trajet si impacté, achat des billets permettant de compléter le trajet). </span>

-----

<span style='color:red'>**Enchères, choix** : </span>
- **enchères** : un client impacté par une route bloquée peut se retrouver avec des tickets achetés non remoursables.
  - pour chaque billet acheté, il lance une enchère hollandaise; partant du prix d'achat; diminuant jusqu'à 1€. Si aucun acheteur, le billet est abandonné.
  - un voyageur peut également devoir abandonner son déplacement. Proposez une adaptation de sa fenêtre afin de lui permettre de lancer des enchèses.
    - un trajet peut subitement devenir très prisé. Un voyageur peut décider de revendre un trajet plutôt que de le réalliser en faisant un bénéfice. Implémentez une enchère ascendante en un tour (cf. enchère de Vikrey)

---

Pour information, les trajets possibles sont les suivants : 
- en bus, entre les villes a<->b, b<->c, b<->d, c<->d, c<>-e, d<->e, e<->f, 
- en train, entre les villes a<->d, d<->f, 
- en voiture (covoiturage), entre les villes a<->f, c<->f
- les coûts, vitesses, émissions de co2, confort ... dépendent du moyen utilisé

```
A-------B
* \\   /|         -- : bus
*   \\/ |         == : train
*    /\\|         ** : voiture
*   C---D\\
*   *\  | ||
*   * \ | ||
*   *  \| || 
 *  *   E ||
 *  *  /  //
 *  * /  //
  * */  //
   *F==//
```

---

## Autres agences 
- Ajoutez 2 autres agences : l'une pour les bus utilisant les voyages de busAutre.csv, l'autre pour les voitures utilisant le fichier carAutre.csv
- Créez une classe PortailAgence. Une classe portail agence se comporte comme une agence mais ne dispose pas de moyens de locomotion. Le client n'envoie plus de demande auprès des agences mais auprès des portails.
- Créez un agent PortailBus qui sert d'intermédiaire entre les clients et les bus, et un agent PortailCar qui fait de même pour les agences de voiture. L'agent agence de train, se définit lui même commu une agence..

- un achat auprès d'un portail est répercuté au niveau de l'agence.


