package ollama.ctfAgents.actions;

import jade.core.AID;
import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import ollama.ctfAgents.commun.Ctf;
import org.json.JSONObject;

/**
 * Action « demander » : envoie un {@code REQUEST} à <b>un</b> agent précis, puis attend sa réponse.
 * <p>
 * L'action est générique : elle sert pour les décodeurs, l'analyste et les archivistes. Tous les arguments
 * sauf {@code agent} forment le contenu du message et sont transmis tels quels. C'est donc au LLM de respecter
 * le format attendu par l'agent destinataire : il le découvre avec {@link ChercherAgents} et grâce aux
 * exemples de {@link #description()}.
 *
 * <h2>Arguments et résultat</h2>
 * <pre>
 *  arguments : {"agent":"decodeur1", "operation":"base64", "donnees":"..."}
 *  message   : REQUEST à decodeur1, contenu {"operation":"base64", "donnees":"..."}
 *  résultat  : {"de":"decodeur1", "performatif":"INFORM", "contenu":{"ok":true, "resultat":...}}
 * </pre>
 *
 * <h2>Échecs</h2>
 * Seule une réponse {@code INFORM} est un succès. Dans les autres cas, l'action lève une exception, et la
 * {@code BoiteAOutils} la transforme en réponse {@code "ok":false} pour le LLM, avec le message d'erreur :
 * <ul>
 *     <li>argument {@code agent} absent ({@link IllegalArgumentException}) ;</li>
 *     <li>réponse {@code REFUSE}, {@code FAILURE} ou {@code NOT_UNDERSTOOD} : l'erreur reprend le performatif,
 *         l'expéditeur et le contenu de la réponse, pour que le LLM comprenne ce qui ne va pas ;</li>
 *     <li>aucune réponse avant {@link ActionAgent#DELAI} (15 s par défaut) : agent absent, occupé ou arrêté.</li>
 * </ul>
 * <p>
 * Comme les autres actions, l'attente utilise {@code blockingReceive} : pendant ce temps, l'agent appelant
 * ne fait rien d'autre (voir {@link ActionAgent}).
 */
public class Demander extends ActionAgent {

    /** @param agent l'agent (capitaine ou testeur) qui envoie les requêtes */
    public Demander(Agent agent) {
        super(agent);
    }

    @Override
    public String nom() {
        return "demander";
    }

    @Override
    public String description() {
        return "envoie une requête à UN agent (trouvé par chercher_agents) et renvoie sa réponse. "
                + "Exemples : {\"agent\":\"decodeur1\",\"operation\":\"base64\",\"donnees\":\"...\"} ; "
                + "{\"agent\":\"decodeur1\",\"operation\":\"cesar\",\"cle\":3,\"donnees\":\"...\"} ; "
                + "{\"agent\":\"analyste\",\"operation\":\"filtrer\",\"colonne\":\"resultat\",\"valeur\":\"ECHEC\"} ; "
                + "{\"agent\":\"analyste\",\"operation\":\"filtrer\",\"colonne\":\"horodatage\",\"contient\":\"T02:\"} ; "
                + "{\"agent\":\"analyste\",\"operation\":\"compter_par\",\"colonne\":\"ip\",\"top\":3} ; "
                + "{\"agent\":\"archiviste1\",\"demande\":\"fragment\",\"preuve\":{\"defi\":\"d1\",\"flag\":\"CTF{...}\"}}";
    }

    @Override
    public JSONObject parametres() {
        return new JSONObject()
                .put("agent", "nom de l'agent destinataire (obligatoire)")
                .put("...", "les autres champs (operation, donnees, cle, colonne, valeur, contient, top, demande, preuve...) "
                        + "sont transmis tels quels à l'agent");
    }

    /**
     * Envoie la requête à l'agent nommé, dans une nouvelle conversation, et attend sa réponse.
     * @param arguments {@code agent} (obligatoire) et le contenu à transmettre
     * @return {@code {"de": agent, "performatif": "INFORM", "contenu": contenu JSON de la réponse}}
     * @throws IllegalArgumentException si l'argument {@code agent} est absent
     * @throws Exception si la réponse n'est pas un {@code INFORM}, ou si aucune réponse n'arrive à temps
     */
    @Override
    public JSONObject executer(JSONObject arguments) throws Exception {
        String nomAgent = arguments.optString("agent");
        if (nomAgent.isBlank()) throw new IllegalArgumentException("le paramètre 'agent' est obligatoire");
        var contenu = new JSONObject(arguments.toString());
        contenu.remove("agent");

        ACLMessage req = Ctf.message(ACLMessage.REQUEST, new AID(nomAgent, AID.ISLOCALNAME), contenu);
        ACLMessage rep = envoyerEtAttendre(req, DELAI);
        String perf = ACLMessage.getPerformative(rep.getPerformative());
        if (rep.getPerformative() != ACLMessage.INFORM)
            throw new Exception(perf + " de " + rep.getSender().getLocalName() + " : " + rep.getContent());
        return new JSONObject().put("de", rep.getSender().getLocalName()).put("performatif", perf).put("contenu", Ctf.contenu(rep));
    }
}
