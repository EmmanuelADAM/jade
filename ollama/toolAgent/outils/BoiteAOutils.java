package ollama.toolAgent.outils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Registre des outils connus de l'agent : c'est le coeur de notre "mini plateforme agentique".
 * <ul>
 *     <li>il fournit le catalogue des outils (pour le prompt système du LLM)</li>
 *     <li>il exécute un outil demandé par le LLM et met le résultat au format du protocole</li>
 * </ul>
 * Toutes les erreurs (outil inconnu, argument manquant, panne réseau...) sont transformées
 * en message d'erreur JSON renvoyé au LLM : le LLM peut alors corriger sa demande.
 */
public class BoiteAOutils {
    /** les outils, indexés par leur nom (ordre d'ajout conservé) */
    private final Map<String, Outil> outils = new LinkedHashMap<>();

    /** ajoute un outil à la boîte (retourne la boîte pour enchaîner les ajouts) */
    public BoiteAOutils ajouter(Outil outil) {
        outils.put(outil.nom(), outil);
        return this;
    }

    /** noms des outils disponibles */
    public Set<String> noms() {
        return outils.keySet();
    }

    /** catalogue des outils, au format JSON (tableau de fiches) */
    public JSONArray catalogue() {
        JSONArray tab = new JSONArray();
        outils.values().forEach(o -> tab.put(o.fiche()));
        return tab;
    }

    /**
     * exécute l'outil demandé par le LLM
     *
     * @param nom       nom de l'outil
     * @param arguments arguments donnés par le LLM (peut être null)
     * @return un message "resultat_outil" du protocole :
     * {"type":"resultat_outil", "outil":"...", "ok":true, "resultat":{...}}
     * ou {"type":"resultat_outil", "outil":"...", "ok":false, "erreur":"..."}
     */
    public JSONObject executer(String nom, JSONObject arguments) {
        JSONObject message = new JSONObject().put("type", "resultat_outil").put("outil", nom);
        Outil outil = outils.get(nom);
        if (outil == null)
            return message.put("ok", false)
                    .put("erreur", "outil inconnu '" + nom + "'. Outils disponibles : " + outils.keySet());
        try {
            JSONObject resultat = outil.executer(arguments == null ? new JSONObject() : arguments);
            return message.put("ok", true).put("resultat", resultat);
        } catch (IllegalArgumentException e) {
            return message.put("ok", false).put("erreur", "argument invalide : " + e.getMessage());
        } catch (Exception e) {
            return message.put("ok", false).put("erreur", "échec de l'outil : " + e.getMessage());
        }
    }
}
