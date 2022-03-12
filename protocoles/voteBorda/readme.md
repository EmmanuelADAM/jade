# Jade : Agents, protocole et services

## Exemple : FIPA Contract Net Protocole en Jade

### Utilisation pour un vote

---

Ici, vous trouverez un exemple de communication via le
protocole [FIPA Contract Net](http://www.fipa.org/specs/fipa00029/SC00029H.html) pour un vote selon Borda (les votants
classent les options en les notant. une somme des notes est ensuite effectuées)

- un agent de
  type [AgentBureauVote](https://github.com/EmmanuelADAM/jade/blob/master/protocoles/voteBorda/agents/AgentBureauVote.java)
  émet un appel à vote sur des noms de restaurants : pizzeria, legumes, sushi, ...
- auprès d'agents de
  type [AgentParticipant](https://github.com/EmmanuelADAM/jade/blob/master/protocoles/voteBorda/agents/AgentParticipant.java)
- Le protocole oblige :
    - les destinataires à répondre (refus, erreur, accord), si accord, la reponse est de la forme : legumes-1,
      pizzeria-2, sushi-3, ...
    - au bureau de vote :
        - de sommer les points envoyés par les votants,
        - d'envoyer le choix gagnant à tous les votants (ou les choix si ex aequo)
        - chaque votant confirme la reception et l'acceptation du résultat du vote
    - si ex-aequo, le vote est relancé pour les choix non départagés

- [LaunchAgents](https://https://github.com/EmmanuelADAM/jade/blob/master/protocoles/voteBorda/launch/LaunchAgents.java) : **
  classe principale**, lançant Jade et créant les agents
    - Au lancement, 1 commissaire-priseur et 5 participants sont lancés.
    - Le bureau de votes lance un vote à chaque clic sur 'go'..

 ---