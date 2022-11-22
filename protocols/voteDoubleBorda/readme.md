# Jade : Agents, protocole et services

## Exemple : FIPA Contract Net Protocole en Jade

### Utilisation pour un vote de Borda

---

Ici, vous trouverez un exemple de communication via le
protocole [FIPA Contract Net](http://www.fipa.org/specs/fipa00029/SC00029H.html) pour un vote selon Borda (les votants
classent les options en les notant, une somme des notes est ensuite effectuée).

- *Principe du vote "de Borda" itéré en* **~*un tour*~**
    - a1 classe option1>option3>option2
    - a2 classe option1>option3>option2
    - a3 classe option3>option2>option1
    - Chaque option reçoit des points en fonction de son classement (sur n options, la 1ere reçoit n points, ..., la
      dernière 1 point)
    - Ici le bureau de vote somme les points
        - option1 : 3+3+1 = 7 points
        - option2 : 1+1+2 = 4 points
        - option3 : 2+2+3 = 7 points
    - Si ex-aequo entre options, un nouveau décompte est effectué parmi celles-ci uniquement (ici option1 & option3) : 
      - on garde de a1  option1>option3; de a2 option1>option3; de a3  option3>option1
      - Ici le bureau de vote somme les points
          - option1 : 2+2+1 = 5 points
          - option2 : 1+1+2 = 4 points => l'option1 est élue
    - Si des options sont de nouveau ex-aequo, un tirage aléatoire est réalisé

---

- un agent de
  type [AgentBureauVote](https://github.com/EmmanuelADAM/jade/blob/master/protocoles/voteBorda/agents/AgentBureauVote.java)
  émet un appel à vote sur des noms de restaurants : pizzeria, legumes, sushi, ...
- auprès d'agents de
  type [AgentParticipant](https://github.com/EmmanuelADAM/jade/blob/master/protocoles/voteBorda/agents/AgentParticipant.java)
- Le protocole oblige :
    - les destinataires à répondre (refus, erreur, accord), si accord, la réponse est de la forme : 
      legumes>pizzeria>sushi>...
    - au bureau de vote :
        - de sommer les points envoyés par les votants (si choix entre n restos, pour chaque vote, le 1er reçoit n 
          points, le 2e n-1, ...),
        - d'envoyer le choix gagnant à tous les votants (ou les choix si ex-aequo)
        - chaque votant confirme la reception et l'acceptation du résultat du vote

- [LaunchAgents](https://https://github.com/EmmanuelADAM/jade/blob/master/protocoles/voteBorda/launch/LaunchAgents.java) : **
  classe principale**, lançant Jade et créant les agents
    - Au lancement, 1 bureau et 5 participants sont lancés.
    - Le bureau de votes lance un vote à chaque clic sur 'go'..

 ---