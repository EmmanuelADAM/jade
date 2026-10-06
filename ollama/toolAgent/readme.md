# Agent LLM outillé : sa petite plateforme "agentique"

Dans les exemples précédents, le programme interroge le LLM et récupère une réponse (texte ou JSON).
Ici, on **inverse le contrôle** : c'est le LLM qui **décide** d'utiliser des outils (date, météo, calendrier scolaire...),
et c'est l'agent qui les **exécute** puis lui renvoie les résultats, jusqu'à ce que le LLM puisse répondre.

C'est le principe du *function calling* ou *tool use* des grands LLM et du protocole MCP,
mais en version minimale, sans bibliothèque, où **tous les échanges sont visibles**.

---

## La boucle agentique

```
 question de l'utilisateur
        |
        v
   +---------+   {"type":"appel_outil", ...}      +--------------+
   |   LLM   | ---------------------------------> | BoiteAOutils |  date, meteo, vacances_scolaires
   | (décide)| <--------------------------------- |   (agit)     |
   +---------+   {"type":"resultat_outil", ...}   +--------------+
        |
        |  {"type":"reponse_finale", ...}
        v
 réponse à l'utilisateur
```

Le LLM ne fait que produire du texte (du JSON) : il **ne peut rien exécuter lui-même**.
C'est l'agent (le programme) qui garde la main : nombre d'étapes limité, appels en double refusés, erreurs gérées.

---

## Le protocole (le "petit format standard")

**LLM → agent** : à chaque tour, UN objet JSON, toujours avec les mêmes champs :

```json
{"pensee": "il me faut la date du jour", "type": "appel_outil", "outil": "date", "arguments": {}, "reponse": ""}
{"pensee": "j'ai tout", "type": "reponse_finale", "outil": "aucun", "arguments": {}, "reponse": "Les prochaines vacances..."}
```

**agent → LLM** : le résultat de l'outil (ou une erreur, que le LLM peut corriger) :

```json
{"type": "resultat_outil", "outil": "meteo", "ok": true,  "resultat": {"ville": "Lille", "temperature_c": 12.4, ...}}
{"type": "resultat_outil", "outil": "meteo", "ok": false, "erreur": "le paramètre 'ville' est obligatoire"}
```

Pour que même un petit modèle respecte ce format, la réponse est **imposée à Ollama par un schéma JSON**
(sortie structurée, voir [ollamaVotes](../ollamaVotes/readme.md)). Le champ `outil` est une énumération des outils existants :
le LLM ne peut pas inventer un outil. Le champ `pensee` est placé en premier : le modèle "réfléchit" avant de décider.

Le **catalogue des outils** est donné au LLM dans le prompt système : chaque outil se décrit lui-même
(nom, description, paramètres). C'est cette description qui permet au LLM de choisir le bon outil.

---

## Les classes

| Paquetage | Classe | Rôle |
|---|---|---|
| `outils` | `Outil` | interface d'un outil : `nom()`, `description()`, `parametres()`, `executer(JSONObject)` |
| `outils` | `BoiteAOutils` | registre des outils : catalogue pour le LLM, exécution + mise au format du protocole |
| `outils` | `OutilDate` | date et heure actuelles (un LLM ne connaît pas la date du jour !) |
| `outils` | `OutilMeteo` | météo actuelle d'une ville (classe `Meteo` d'internationalCuisine), **avec cache de 15 min** |
| `outils` | `OutilVacances` | prochains évènements du calendrier scolaire officiel (fichier [Zone-B.ics](https://fr.ftp.opendatasoft.com/openscol/fr-en-calendrier-scolaire/Zone-B.ics)) |
| `llm` | `Protocole` | schéma JSON, prompt système, constantes du protocole |
| `llm` | `ClientLLM` | appel à `/api/chat` d'Ollama avec sortie structurée |
| `llm` | `BoucleAgentique` | la boucle LLM ↔ outils (indépendante de JADE) |
| `agents` | `AgentOutille` | agent JADE avec fenêtre de dialogue, qui affiche tous les échanges JSON |
| `launch` | `TestBoucleAgentique` | test de la boucle en console, sans JADE et **sans l'API météo** |

---

## Lancement

1. Ollama lancé (`ollama serve`) avec un modèle de chat (un modèle de 3-4 milliards de paramètres suffit, ex. `llama3.2:3b`, `qwen3:4b`...)
2. Lancer le `main` de [AgentOutille](agents/AgentOutille.java) et poser par exemple les questions :
   - *Quand sont les prochaines vacances ? Dans combien de jours ?* → `date` puis `vacances_scolaires`
   - *Quel temps fait-il ?* → `meteo` sur la ville du contexte
   - *Je pars à Nice pendant les prochaines vacances, quel temps y fait-il en ce moment ?* → 2 ou 3 outils
   - *Combien font 12 fois 7 ?* → aucun outil
3. Ou, le main de `TestBoucleAgentique` pour exécuter des questions tests 

**API météo** : la clé gratuite d'OpenWeatherMap est limitée. Changer de ville dans la fenêtre ne fait plus d'appel ;
seul le LLM déclenche la météo, et le résultat est gardé 15 min en cache. Le nombre d'appels réels est affiché après chaque réponse.
  - **N.B.** _ce projet fait appel à la classe Meteo développée dans un projet voisin_

---
