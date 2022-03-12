# Jade : Agents

## Agence de voyages

---

Ces codes illustrent un petit cas d'étude permettant de manipuler le protocole de ContractNet.

Un voyageur souhaite aller d'un point `a` à un point `b` :

- il lance un appel d'offre à plusieurs agences de voyages,
- certaines sont spécialisées dans les bus, les trains ou les voitures.
- ces agences envoient leurs catalogues de voyages possibles
- le client fait son choix et peut combiner différentes offres selon ses critères (coûts, temps, émission de CO2, ...)


- Des classes sont déjà proposées :
    - La classe [AgenceAgent](https://github.com/EmmanuelADAM/jade/blob/master/agentsVoyage/agents/AgenceAgent.java) est
      une classe représentant une agence de voyage. Une agence dispose d'un catalogue de voyages qu'elle crée à partir
      d'un fichier csv (bus.csv, car.csv ou train.csv).
    - La
      classe [TravellerAgent](https://github.com/EmmanuelADAM/jade/blob/master/agentsVoyage/agents/TravellerAgent.java)
      représente le client, qui émet l'appel d'offres et effectue sont choix, **à coder**
    - La classe [AlertAgent](https://github.com/EmmanuelADAM/jade/blob/master/agentsVoyage/agents/AlertAgent.java)
      représente un agent capable d'émettre un appel radio (broadcast) signalant une alerte sur un tronçon de route.
    - La classe [LaunchSimu](https://github.com/EmmanuelADAM/jade/blob/master/agentsVoyage/launch/LaunchSimu.java) lance
      un client, 3 agents agence de voyages spécialisée, et 1 agent d'alertes.
    - La
      classe [ContractNetVente](https://github.com/EmmanuelADAM/jade/blob/master/agentsVoyage/comportements/ContractNetVente.java)
      code le comportement de réponse à un appel d'offre, il consiste simplement à envoyer le catalogue complet des
      voyages, il peut être optimisé.
    - La
      classe [ContractNetAchat](https://github.com/EmmanuelADAM/jade/blob/master/agentsVoyage/comportements/ContractNetAchat.java)
      code le comportement de lancement et de gestion d'un appel d'offres.

- Dans le package [agencesVoyages.data](https://github.com/EmmanuelADAM/jade/tree/master/agentsVoyage/data) se trouvent
  les classes qui consruisent les chemins possibles.
- Dans le package [agencesVoyages.gui](https://github.com/EmmanuelADAM/jade/tree/master/agentsVoyage/gui) se trouvent
  les classes qui consruisent les feneêtre de dialogue avec les agencesVoyages.agents.

Le code utilise

- la librairie opencsv ([http://opencsv.sourceforge.net](http://opencsv.sourceforge.net)) version 3.9 attachée à ce
  dossier.
- la librairie jade.jar à télécharger sur le site de [jade](https://jade.tilab.com).

-----
Le code s'exécute tel quel, mais le client ne peut effectuer qu'un choix par durée la plus courte parmi les voyages
proposés.

- **Proposez et codez** la décrémentation du nb de places par voyage (on pose 3 places par trajet en voiture, 50 en bus,
  et 200 places par trajet en train),
- **Proposez et codez** le comportement de réaction suite à la réception d'un message d'alerte sur un tronçon donné;
    - pour les agences (disparition des trajets impactés),
    - pour le client (relance d'une demande de trajet si impacté, achat des billets permettant de compléter le trajet).

> si code correct => + 5 points

-----

<span style='color:red'>**Enchères, choix** : </span>

- **enchères** : un client impacté par une route bloquée peut se retrouver avec des tickets achetés non remoursables.
    - pour chaque billet acheté,
        - il lance une enchère hollandaise; partant du prix d'achat; diminuant jusqu'à 1€. Si aucun acheteur, le billet
          est abandonné.
        - ou il lance une enchère anglaise (1 un tour (**facile**, mais pas de bonus de points)), en n tours
          classiquement; l'enchère grimpe au fur et à mesure des annonces et stoppe lorsqu'il n'y en a plus. le
          vainqueur étant celui ayant donné le prix le plus haut)
    - un voyageur peut également devoir abandonner son déplacement. Proposez une adaptation de sa fenêtre afin de lui
      permettre de lancer des enchères.
        - un trajet peut subitement devenir très prisé. Un voyageur peut décider de revendre un trajet plutôt que de le
          réaliser en faisant un bénéfice. Implémentez une enchère ascendante en un tour (cf. enchère de Vikrey)

> si code enchères correct => + 5 points (+ 4 points par autre type d'enchère)
>
> si code de modification d'une fenêtre client correct => + 3 points

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

- Ajoutez 2 autres agences : l'une pour les bus utilisant les voyages de busAutre.csv, l'autre pour les voitures
  utilisant le fichier carAutre.csv
- Créez une classe PortailAgence. Une classe portail agence se comporte comme une agence, mais ne dispose pas de moyens
  de locomotion. Le client n'envoie plus de demande auprès des agences, mais auprès des portails.
- Créez un agent PortailBus qui sert d'intermédiaire entre les clients et les bus, et un agent PortailCar qui fait de
  même pour les agences de voiture. L'agent agence de train, se définit lui-même comme une agence..

- un achat auprès d'un portail est répercuté au niveau de l'agence.

> si code portail correct => + 5 points

---

## Confiance

**Confiance envers l'autre**

- Ajoutez une notion d'évaluation aux agences, sur une valeur de 0 à 10.
    - Un agent peut maintenant avoir une nature courageuse ou prudente ou neutre.
    - Le courageux ne tient pas compte des avis et prendra l'offre la plus intéressante quelque soit l'évaluation de
      l'agence.
    - Le prudent se basera plutôt sur l'évaluation que sur l'intérêt de l'offre; sur un odre de 90/10 : si le confort
      est demandé et qu'un trajet en voiture est proposé par une agence de faible renomée, le voyageur préfèrera
      éventuellement prendre le bus proposé par une agence renommée.
    - Le neutre effectue un ratio 50/50 sur la renommée et l'intérêt de l'offre pour faire son choix

> si code "Confiance envers l'autre" correct => + 5 points

**Confiance dans le service**

- Ajoutez maintenance une notion de confiance envers le réseau routier...
    - pour chaque axe, ajoutez une valeur de confiance (confiance dans la relation A-B, ..... , E-F)
    - à chaque problème sur un axe, la confiance envers la relation passe à 0
    - à la restauration du problème; la confiance se rétablit "petit à petit" selon une vitesse plus ou moins rapide
      selon que l'agent soit rancunier ou confiant.
    - un acheteur qui se voit proposer un déplacement sur un axe décidera de risquer de prendre cet axe ou non selon la
      confiance qu'il lui accorde (par un tirage aléatoire, si le nombre est dessous la confiance accordée, le chemin
      sera pris ou non)
    - une agence choisira de même de proposer dans son catalogue les trajets selon la confiance accordée à ceux-ci

> si code "Confiance dans le service" correct => + 5 points

---

