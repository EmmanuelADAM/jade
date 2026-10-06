package ollama.toolAgent.llm;

import ollama.toolAgent.outils.BoiteAOutils;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Le petit protocole (format standard) d'échange entre le LLM et l'agent.
 * <p>
 * <b>LLM -> agent</b> : à chaque tour, le LLM répond par UN objet JSON
 * <pre>
 * {"reflexion":"...", "type":"appel_outil",    "outil":"meteo", "arguments":{"ville":"Lille"}, "reponse":""}
 * {"reflexion":"...", "type":"reponse_finale", "outil":"aucun", "arguments":{},                "reponse":"Il fait 12°C..."}
 * </pre>
 * <b>agent -> LLM</b> : après l'exécution d'un outil, l'agent renvoie
 * <pre>
 * {"type":"resultat_outil", "outil":"meteo", "ok":true,  "resultat":{...}}
 * {"type":"resultat_outil", "outil":"meteo", "ok":false, "erreur":"..."}
 * </pre>
 * Le format de la réponse du LLM est IMPOSÉ à Ollama par un schéma JSON (sortie structurée, champ "format") :
 * même un petit modèle produit alors un JSON valide.
 * <p>
 * C'est exactement l'idée des "function calling" / "tool use" des API des grands LLM et du protocole MCP,
 * en version minimale et entièrement visible.
 */
public final class Protocole {
    public static final String APPEL_OUTIL = "appel_outil";
    public static final String REPONSE_FINALE = "reponse_finale";
    public static final String RESULTAT_OUTIL = "resultat_outil";
    /** valeur du champ "outil" quand aucun outil n'est appelé */
    public static final String AUCUN = "aucun";

    private Protocole() {
    }

    /**
     * schéma JSON que doit respecter chaque réponse du LLM.
     * Les noms d'outils possibles sont listés et précisés : le LLM ne peut pas en inventer.
     * <p>
     * ATTENTION à l'ordre des propriétés : Ollama fait générer les champs dans l'ordre du schéma.
     * "reflexion" est en premier (le modèle "réfléchit" avant de décider), "reponse" en dernier.
     * Un JSONObject ne conserve pas l'ordre des clés : le schéma est donc écrit sous forme de texte.
     * <p>Example of return:</p>
     * {"type" : "object",<br>
     *  "properties": {<br>
     *  "reflexion":    {"type": "string"},<br>
     *  "type":      {"type": "string", "enum": ["appel_outil", "reponse_finale"]},<br>
     *  "outil":     {"type": "string", "enum": ["date","meteo","vacances_scolaires","aucun"]},<br>
     *  "arguments": {"type": "object"},<br>
     *  "reponse":   {"type": "string"}<br>
     *  },<br>
     *  "required": ["reflexion", "type", "outil", "arguments", "reponse"]<br>
     *  }<br>
     */
    public static String schema(BoiteAOutils outils) {
        JSONArray nomsOutils = new JSONArray(outils.noms()).put(AUCUN);
        return """
                {"type": "object",
                 "properties": {
                   "reflexion":    {"type": "string"},
                   "type":      {"type": "string", "enum": ["%s", "%s"]},
                   "outil":     {"type": "string", "enum": %s},
                   "arguments": {"type": "object"},
                   "reponse":   {"type": "string"}
                 },
                 "required": ["reflexion", "type", "outil", "arguments", "reponse"]
                }""".formatted(APPEL_OUTIL, REPONSE_FINALE, nomsOutils);
    }

    /**
     * prompt système : rôle de l'assistant + catalogue des outils (et leurs descriptions) + règles du protocole
     *
     * @param outils   la boîte à outils de l'agent
     * @param contexte informations supplémentaires (ex. ville de l'utilisateur), peut être vide
     */
    public static String promptSysteme(BoiteAOutils outils, String contexte) {
        StringBuilder sb = new StringBuilder();
        sb.append("""
                Tu es un assistant sympathique qui peut utiliser des OUTILS pour obtenir les informations que tu ne connais pas \
                (date du jour, météo actuelle, calendrier scolaire...). Tu ne dois JAMAIS inventer ces informations : \
                appelle l'outil adapté.

                OUTILS DISPONIBLES (nom, description, paramètres) :
                """);
        //ajout des outils du catalogues, avec leurs descriptions completes; important !!!
        for (Object o : outils.catalogue()) {
            JSONObject fiche = (JSONObject) o;
            sb.append("- ").append(fiche.getString("nom")).append(" : ").append(fiche.getString("description"))
                    .append("\n  paramètres : ").append(fiche.getJSONObject("parametres")).append('\n');
        }
        sb.append("""

                PROTOCOLE : tu réponds TOUJOURS par un unique objet JSON, sous l'une de ces deux formes :
                1) pour appeler un outil :
                {"reflexion": "pourquoi j'ai besoin de cet outil", "type": "appel_outil", "outil": "<nom de l'outil>", "arguments": {<paramètres>}, "reponse": ""}
                2) quand tu as toutes les informations :
                {"reflexion": "ce que je vais répondre", "type": "reponse_finale", "outil": "aucun", "arguments": {}, "reponse": "<ta réponse à l'utilisateur, en français>"}

                RÈGLES :
                - un seul outil par message ; après chaque appel tu reçois un message {"type": "resultat_outil", ...}
                - tu peux enchaîner plusieurs appels (ex. d'abord la date, puis le calendrier) avant de répondre
                - si un résultat contient "ok": false, lis l'erreur et corrige ton appel (ou explique le problème)
                - n'appelle pas deux fois le même outil avec les mêmes arguments
                - si la question ne nécessite aucun outil, réponds directement (reponse_finale)
                """);
        if (contexte != null && !contexte.isBlank()) sb.append("\nCONTEXTE : ").append(contexte).append('\n');
        return sb.toString();
    }

    /**
     * décision du LLM écrite dans l'ordre du protocole (reflexion, type, outil, arguments, reponse) :
     * plus lisible à l'affichage que JSONObject.toString() qui mélange les clés
     */
    public static String texte(JSONObject decision) {
        return "{\"reflexion\":" + JSONObject.quote(decision.optString("reflexion"))
                + ", \"type\":" + JSONObject.quote(decision.optString("type"))
                + ", \"outil\":" + JSONObject.quote(decision.optString("outil"))
                + ", \"arguments\":" + decision.optJSONObject("arguments", new JSONObject())
                + ", \"reponse\":" + JSONObject.quote(decision.optString("reponse")) + "}";
    }

    /** un message du dialogue (role = "system", "user" ou "assistant") */
    public static JSONObject message(String role, String contenu) {
        return new JSONObject().put("role", role).put("content", contenu);
    }
}
