# Jade : TD2 - Gestion d'un Emploi Du Temps 
*(benchmark asa)*

Trois enseignants ($e_1$, $e_2$ et $e_3$) doivent proposer chacun deux enseignements de deux heures, répartis sur deux jours ($j_1$ et $j_2$), à trois groupes d'étudiants ($g_1$, $g_2$ et $g_3$).

Les créneaux sont donc des créneaux de deux heures, s'étalant sur deux jours, de 8h à 18h, avec une pause de deux heures entre 12h et 14h.

Les enseignants possèdent des contraintes horaires, listées pour chacun par ordre d’importance :
  - $e_1$ ne peut enseigner : (1) le jour $j_1$ de 16h à 18h ; (2) le jour $j_2$ de 14h à 16h. 
  - $e_2$ ne peut enseigner : (1) le jour $j_2$ de 10h à 12h ; (2) le jour $j_1$ de 16h à 18h. 
  - $e_3$ ne peut enseigner : (1) le jour $j_1$ de 14h à 16h ; (2) le jour $j_2$ de 8h à 10h.

Les trois salles s1, s2 et s3 sont disponibles pour ces enseignements, cependant elles sont soumises elles aussi à des contraintes, telles que par exemple :
  - la salle $s_1$ n'est pas disponible le jour $j_1$ de 10h à 12h,
  - la salle $s_2$ n'est pas disponible le jour $j_2$ de 16h à 18h et de 8h à 10h,
  - la sale $s_3$ n'est pas disponible le jour $j_2$ de 16h à 18h et le jour $j_1$ de 14h à 16h. 
  - Seules les salles $s_1$ et $s_2$ possèdent un rétroprojecteur.

Les enseignants possèdent également des contraintes matérielles; par exemple :
    - tous les enseignants veulent utiliser un rétroprojecteur au moins une fois pour chaque groupe sur  les deux jours.
 
Idéalement les contraintes posées par les enseignants doivent être respectées. 
Si p1 et p2 sont les poids associés aux respects des contraintes sur les jours (avec $p1 + p2 = 1$ et $p1 > p2$), 
le degré de satisfaction d’un enseignant e est défini par la fonction utilité :

$u(e) = (p_1 \times ok_{contrainte_1} + p2 \times ok_{contrainte_2})$ avec $ok_{contrainte_i} =$ 0 ou 1 si la contrainte (i) est respectée ou non.

Mais il se peut que ce ne soit pas possible.

## Agentification 
  - Quel(s) mécanisme(s) de résolution utilisez vous ? (vote, négociation, adaptation )
  - Donner votre point de vue multi-agent sur la résolution de ce système (nombre d'agents, leurs rôles, composition de l'environnement).

## Spécification
  - Utilisez de préférence PlantUML pour générer les diagrammes de classes, d'états, d'acitité et  de séquence
  - Précisez les types comportements permettant l'implémentation des rôles que vous avez décrits. Vous ne donnerez que leurs noms et types (simples, cycliques, ...), leurs fonctionnements sont à décrire en texte simple.
  - Donnez les diagrammes de dialogue JADE entre les agents pour la résolution du problème de l'EDT en précisant les comportements impliqués dans les échanges.
