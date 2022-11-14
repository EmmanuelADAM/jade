# Jade : Agents

## Exemple de comportements sur un agent "Hello World"

---

- [AgentHelloSalut](https://github.com/EmmanuelADAM/jade/tree/english/testComp01/AgentHelloSalut.java) : code for an 
  agent that owns 3 behaviors:
    - a behavior displaying "hello" every 200ms, endlessly
    - a cyclic behavior with activation every 300ms
    - a delayed behavior causing the removal of the agent after 1000ms
    - initially, 2 agents are launched.
<!--
```
@startuml helloSalut

start
while (While agent alive) is (ok)
if (activatable behavior ?) then ([select next behavior])
    fork
    partition "Behaviour" {
      partition "action" {
          ::display "Hello everybody";
          :pause 200ms;
      }
      partition "done" {
          :return False;
      }
    }
    fork again
    partition "CyclicBehaviour: each 300ms" {
      partition "onTick" {
          ::display "Hi !";
      }
    }
    fork again
    partition "WakerBehaviour: in 1000ms" {
      partition "onWake" {
          ::delete Agent;
      }
    }
    end fork
 else(no)
 endif 
  endwhile (deleted)
stop

@enduml```
-->

![](helloSalut.png)

- [AgentHelloEuropeenParallel](https://github.com/EmmanuelADAM/jade/blob/master/testComp01/AgentHelloEuropeenParallel.java) :
  code pour deux agent qui possèdent contient des comportements s'exécutant en **parallèle**. Ces comportments
  s'exécutent 3 fois et  affichent des salutations dans différentes langues européennes..
- JADE partage bien les ressources entre les agents, et pour chaque agent entre ses comportements s'exécutant en
  parallèle.

- Voici une vue du fonctionnement de comportements en séquentiel.


<!--
```
@startuml HelloEuropeenParallel

start
while (TQ agent vivant) is (ok)
if (comportement activable ?) then ([choisir prochain comportement])
    fork
    partition "EuropeanBehaviour A" {
      partition "action" {
          :afficher "bonjour"
          i <- i + 1;
      }
      partition "done" {
      if (i==3) then (true)
        :remove 
        behaviour;
      else (false)
      endif 
      }
    }
    fork again
    partition "EuropeanBehaviour B" {
      partition "action" {
          :afficher "hallo"
          i <- i + 1;
      }
      partition "done" {
      if (i==3) then (true)
        :remove 
        behaviour;
      else (false)
      endif 
      }
    }
    fork again
    partition "EuropeanBehaviour C" {
      partition "action" {
          :afficher "buongiorno"
          i <- i + 1;
      }
      partition "done" {
      if (i==3) then (true)
        :remove 
        behaviour;
      else (false)
      endif 
      }
    }
    end fork
else (non)
 endif 
  endwhile (deleted)
stop

@enduml```
-->

![](HelloEuropeenParallel.png)

```
a1 -> bonjour 1 fois
a2 -> bonjour 1 fois
a1 -> hallo 1 fois
a2 -> hallo 1 fois
a1 -> buongiorno 1 fois
a2 -> buongiorno 1 fois
a1 -> buenos dias 1 fois
a2 -> buenos dias 1 fois
a1 -> OlÃ¡ 1 fois
a2 -> OlÃ¡ 1 fois
a1 -> saluton 1 fois
a2 -> saluton 1 fois
a1 -> bonjour 2 fois
a2 -> bonjour 2 fois
a1 -> hallo 2 fois
a2 -> hallo 2 fois
a1 -> buongiorno 2 fois
a2 -> buongiorno 2 fois
a1 -> buenos dias 2 fois
a2 -> buenos dias 2 fois
...
```

- [AgentHelloEuropeenSequentiel](https://github.com/EmmanuelADAM/jade/blob/master/testComp01/AgentHelloEuropeenSequentiel.java) :
  code pour deux agent qui possèdent contient des comportements s'exécutant en **séquentiel**. Ces comportments
  s'exécutent 3 fois  et  affichent des salutations dans différentes langues européennes..
- JADE partage bien les ressources entre les agents, et cette fois, pour chaque agent le même comportement est 
  appelé tant qu'il n'est pas terminé, les autres comportement s'exécutant dans l'ordre de leurs déclarations.

- Voici une vue du fonctionnement de comportements en séquentiel.
<!--
```
@startuml HelloEuropeenSequentiel

start
while (TQ agent vivant) is (ok)
  if (EuropeanBehaviour A 
  exists) then (true)
    partition "EuropeanBehaviour A" {
      partition "action" {
          :afficher "bonjour"
          i <- i + 1;
      }
      partition "done" {
      if (i==3) then (true)
        :remove 
        behaviour;
      else (false)
      endif 
      }
    }
  elseif (EuropeanBehaviour B 
exists) then (true)
    partition "EuropeanBehaviour B" {
      partition "action" {
          :afficher "hallo"
          i <- i + 1;
      }
      partition "done" {
      if (i==3) then (true) 
        :remove 
        behaviour;
      else (false)
      endif 
      }
    }
  elseif (EuropeanBehaviour C 
exists) then (true)
    partition "EuropeanBehaviour C" {
      partition "action" {
          :afficher "buongiorno"
          i <- i + 1;
      }
      partition "done" {
      if (i==3) then (true)
        :remove 
        behaviour;
      else (false)
      endif 
      }
    }
  endif
  endwhile (deleted)
stop

@enduml```
-->

![](HelloEuropeenSequentiel.png)

```
a1 -> bonjour 1 fois
a2 -> bonjour 1 fois
a1 -> bonjour 2 fois
a2 -> bonjour 2 fois
a2 -> bonjour 3 fois
a1 -> bonjour 3 fois
a2 -> hallo 1 fois
a1 -> hallo 1 fois
a2 -> hallo 2 fois
a1 -> hallo 2 fois
a2 -> hallo 3 fois
a1 -> hallo 3 fois
a2 -> buongiorno 1 fois
a1 -> buongiorno 1 fois
a2 -> buongiorno 2 fois
a1 -> buongiorno 2 fois
a2 -> buongiorno 3 fois
a1 -> buongiorno 3 fois
a2 -> buenos dias 1 fois
a1 -> buenos dias 1 fois
a2 -> buenos dias 2 fois
a1 -> buenos dias 2 fois
a2 -> buenos dias 3 fois
a1 -> buenos dias 3 fois
a2 -> OlÃ¡ 1 fois
...
```