# Et MCP ?

## Le problème

Dans [toolAgent](readme.md), nous avons inventé **notre propre format** pour décrire des outils (`Outil` : nom, description, paramètres)
et pour les appeler (`appel_outil` / `resultat_outil`). Cela fonctionne... mais uniquement pour **notre** agent.

Imaginons maintenant que chaque application d'IA (assistant de chat, IDE, agent maison...) fasse de même, et que chaque service
(météo, agenda, base de données, GitHub, fichiers...) veuille être utilisable par toutes ces applications :
il faudrait écrire un connecteur pour chaque couple *application × service*. C'est le problème des **N × M intégrations**.

## La réponse : MCP (Model Context Protocol)

**MCP** est un protocole ouvert, proposé par Anthropic fin 2024 et aujourd'hui géré par l'*Agentic AI Foundation*
(Linux Foundation). Il standardise **la façon dont une application d'IA découvre et utilise des outils et des données externes**.
Un service écrit **un seul serveur MCP**, utilisable par **toutes** les applications compatibles. On passe de N × M connecteurs à N + M.

### Les "acteurs"

```
 +------------------- HÔTE (application d'IA) --------------------+
 |                                                                |
 |   LLM  <-->  logique de l'agent  <-->  client MCP 1  <---------|----> serveur MCP "meteo"
 |                                        client MCP 2  <---------|----> serveur MCP "calendrier"
 |                                        client MCP 3  <---------|----> serveur MCP "fichiers"
 +----------------------------------------------------------------+
```
Cela ressemble à la gestion de services web.

- **Hôte** : l'application qui contient le LLM et la logique de l'agent (Claude Desktop, un IDE, ... ou notre `AgentOutille`).
- **Client MCP** : dans l'hôte, une connexion vers un serveur.
- **Serveur MCP** : un programme (local ou distant) qui expose des capacités :
  - des **outils** (*tools*) : des actions que le LLM peut demander (ex. `meteo(ville)`) ;
  - des **ressources** (*resources*) : des données à lire (un fichier, un calendrier `.ics`...) ;
  - des **prompts** : des modèles de requêtes prêts à l'emploi.

Les messages sont en **JSON-RPC 2.0**, transportés soit par l'entrée/sortie standard (serveur local lancé par l'hôte),  soit par HTTP (serveur distant).

### Un échange MCP

1. **Initialisation** : client et serveur échangent leurs versions et leurs capacités (`initialize`).
2. **Découverte** : le client demande la liste des outils (`tools/list`). Le serveur répond avec, pour chaque outil,
   un nom, une description et un **schéma JSON** des paramètres :
   ```json
   {"name": "vacances_scolaires",
    "description": "donne les prochains évènements du calendrier scolaire officiel...",
    "inputSchema": {"type": "object",
                    "properties": {"zone": {"type": "string", "enum": ["A", "B", "C"]},
                                   "nombre": {"type": "integer"}}}}
   ```
3. **Appel** : quand le LLM décide d'utiliser un outil, l'hôte envoie `tools/call` :
   ```json
   {"jsonrpc": "2.0", "id": 7, "method": "tools/call",
    "params": {"name": "vacances_scolaires", "arguments": {"zone": "B", "nombre": 2}}}
   ```
   et reçoit le résultat (ou une erreur signalée par `isError`), qu'il redonne au LLM :
   ```json
   {"jsonrpc": "2.0", "id": 7,
    "result": {"content": [{"type": "text", "text": "Vacances de la Toussaint : du 17/10 au 02/11..."}],
               "isError": false}}
   ```

## Le parallèle avec l'agent du projet

| Le `toolAgent` | MCP |
|---|---|
| interface `Outil` (`nom`, `description`, `parametres`) | définition d'un *tool* (`name`, `description`, `inputSchema`) |
| `BoiteAOutils.catalogue()` | `tools/list` |
| `BoiteAOutils.executer(nom, arguments)` | `tools/call` |
| `{"type":"resultat_outil", "ok":false, "erreur":...}` | résultat avec `isError: true` |
| les outils sont des classes Java **dans** l'agent | les outils sont dans des **serveurs séparés**, découverts à la connexion |
| `OutilVacances` lit un fichier `.ics` | ce fichier pourrait être une **ressource** MCP |
| `AgentOutille` | l'**hôte** |

**Ce qui est identique** : un outil se *décrit* (et c'est la description qui fait que le LLM le choisit),  le LLM ne fait que *demander*, et c'est le programme hôte qui *exécute* et garde le contrôle.

**Ce que MCP ne standardise pas** : le dialogue entre le LLM et l'hôte (notre classe `Protocole`).
Chaque hôte traduit les outils MCP dans le format de son LLM : champ `tools` de l'API d'Ollama, *function calling* des autres fournisseurs... ou un schéma JSON maison comme le nôtre. MCP standardise le côté **agent ↔ outils**,  pas le côté **agent ↔ LLM**.

## Relativement aux systèmes multi-agents ?

Cela ressemble aux pages jaunes :

| FIPA / JADE | MCP |
|---|---|
| le **DF** (pages jaunes) : on cherche un agent qui rend un service | `tools/list` : on découvre les outils d'un serveur |
| message ACL `REQUEST` puis `INFORM` / `FAILURE` | `tools/call` puis résultat / `isError` |
| ontologie partagée | schéma JSON des paramètres |
| langage ACL, protocoles d'interaction standardisés | JSON-RPC, protocole standardisé |

Différence essentielle : dans MCP, le serveur est un **outil passif** qui obéit ; dans un SMA, chaque agent est **autonome**
et peut refuser, négocier, prendre l'initiative. 
Pour faire dialoguer des agents LLM (dits agents IA) entre eux, des protocoles apparaissent comme [Agent2Agent](https://a2a-protocol.org/).


## Attention : sécurité

Brancher un serveur MCP, c'est donner au LLM la possibilité d'agir avec **vos** droits, données.  
Quelques risques à connaître :
- **injection de prompt** par les résultats : un résultat d'outil (page web, mail, fichier `.ics`...) peut contenir des instructions
  que le LLM risque de suivre (voir la piste de TD 5) ;
- **empoisonnement d'outil** : un serveur malveillant peut cacher des instructions dans la *description* de ses outils,
  lue par le LLM à chaque requête ;
- **droits trop larges** et **chaîne d'approvisionnement** : un serveur installé sans vérification peut lire, écrire, envoyer...

Bonnes pratiques : n'installer que des serveurs de confiance, donner le minimum de droits, demander une **confirmation humaine** avant toute action irréversible, traiter tout résultat d'outil comme une **donnée** et jamais comme une consigne.
