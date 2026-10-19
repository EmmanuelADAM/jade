package ollama.ctfAgents.actions;

import jade.core.Agent;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import ollama.ctfAgents.commun.Ctf;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

/**
 * Action « chercher_agents » : recherche dans les pages jaunes (DF) les agents qui offrent un service du CTF.
 * <p>
 * C'est en général la première action du LLM : il ne connaît pas à l'avance les noms des agents ni ce
 * qu'ils savent faire. Il les découvre ici, puis les sollicite avec {@link Demander}, {@link AppelOffres}
 * ou {@link ConsulterTous}.
 * <p>
 * Contrairement aux autres actions, celle-ci n'envoie aucun message ACL aux agents du CTF : elle interroge
 * seulement le DF, avec {@link Ctf#chercher}. Elle n'entre donc pas dans les compteurs de messages.
 *
 * <h2>Arguments et résultat</h2>
 * <pre>
 *  arguments : {"service":"decodeur"}
 *              (un parmi decodeur, decodeur_auto, analyste, archiviste, arbitre)
 *  résultat  : {"service":"decodeur",
 *               "agents":[{"agent":"decodeur1","operations":"base64,hex,rot13,cesar","delai_ms":"200"}, ...]}
 * </pre>
 * Chaque agent trouvé est décrit par son nom local et par les propriétés qu'il a publiées en s'inscrivant
 * (opérations, délai, méthode, conditions…). Toutes les valeurs sont des chaînes. La liste est vide si aucun
 * agent n'offre ce service.
 * <p>
 * Un service inconnu lève une {@link IllegalArgumentException}. La {@code BoiteAOutils} la transforme
 * en réponse {@code "ok":false} pour le LLM, qui peut alors corriger son appel.
 */
public class ChercherAgents extends ActionAgent {

    /** @param agent l'agent (capitaine ou testeur) qui fait la recherche */
    public ChercherAgents(Agent agent) {
        super(agent);
    }

    @Override
    public String nom() {
        return "chercher_agents";
    }

    @Override
    public String description() {
        return "cherche dans les pages jaunes les agents qui offrent un service du CTF, avec leurs capacités "
                + "(opérations, délai, conditions). A faire AVANT de solliciter un agent : tu ne connais pas leurs noms à l'avance.";
    }

    @Override
    public JSONObject parametres() {
        return new JSONObject().put("service", "un parmi \"decodeur\", \"decodeur_auto\", \"analyste\", \"archiviste\", \"arbitre\"");
    }

    /**
     * Cherche au DF les agents qui offrent le service demandé.
     * @param arguments {@code {"service": ...}}
     * @return {@code {"service": ..., "agents": [{"agent": nom, ...propriétés}, ...]}}
     * @throws IllegalArgumentException si le service n'est pas un service du CTF
     */
    @Override
    public JSONObject executer(JSONObject arguments) {
        //TODO:completer
        return null;
    }
}
