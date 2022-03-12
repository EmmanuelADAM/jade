# Jade : Agents, protocole et services

## Exemple : FIPA Contract Net Protocole en Jade

### Utilisation pour une enchère

---

Ici, vous trouverez un exemple de communications suivant une machine à états finis (FSM (Finite States Machine)).

Il s'agit de reproduire un processus de soumission par un auteur d'un article à un journal. Ce journal transmet la
propoisiton à 3 reviewers qui attribuent une note entre 0(rejet)  & 2(acceptation franche). Le journal reçoit les notes,
les aggrége et transmet à l'auteur une acceptation, un rejet ou une propoisiton de corrections, alors l'auteur peut
décider de renvoyer une nouvelle version ou d'arrêter.

