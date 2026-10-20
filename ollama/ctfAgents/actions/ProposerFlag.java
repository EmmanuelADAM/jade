package ollama.ctfAgents.actions;

import jade.core.AID;
import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import ollama.ctfAgents.commun.Ctf;
import org.json.JSONObject;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Action « proposer_flag » : propose un flag à l'arbitre ({@code PROPOSE}) et renvoie sa décision.
 * <p>
 * L'équipe est l'agent qui envoie le message (le capitaine) : l'arbitre ne se fie qu'à l'expéditeur réel.
 * Chaque proposition peut consommer un des 3 essais autorisés par défi : la description de l'action
 * le rappelle au LLM.
 *
 * <h2>Ce que le programme garde en main</h2>
 * <ul>
 *     <li><b>Flags validés</b> : les flags acceptés sont mémorisés ici, par le programme et non par le LLM
 *         ({@link #getFlagsValides()}). Ils servent de preuves auprès des archivistes (d5) et sont rappelés
 *         au LLM dans son contexte.</li>
 *     <li><b>Réputation</b> (d7) : si le flag provient du dernier candidat retenu avec {@code retenir},
 *         les agents de sa chaîne gagnent 1 point quand le flag est accepté, et en perdent 1 quand il est refusé
 *         (voir {@link Reputation}).</li>
 * </ul>
 *
 * <h2>Arguments et résultat</h2>
 * <pre>
 *  arguments : {"defi":"d1", "flag":"CTF{...}"}
 *  résultat  : le contenu de la réponse de l'arbitre, plus son performatif, ex.
 *              {"defi":"d1", "accepte":true, "points":100, "score_equipe":100, "performatif":"ACCEPT_PROPOSAL"}
 *              {"defi":"d2", "accepte":false, "essais_restants":2, "performatif":"REJECT_PROPOSAL"}
 *              + "reputation_mise_a_jour":{"agents":[...], "variation":1|-1, "reputations":{...}} si la réputation a changé
 * </pre>
 * Un refus de l'arbitre n'est <b>pas</b> une erreur : l'action réussit et renvoie {@code "accepte":false}.
 *
 * <h2>Échecs</h2>
 * L'action lève une exception, transformée en réponse {@code "ok":false} pour le LLM, si :
 * <ul>
 *     <li>{@code defi} est absent ou le flag n'a pas la forme {@code CTF{...}} ({@link IllegalArgumentException}).
 *         Dans ce cas, rien n'est envoyé et aucun essai n'est consommé ;</li>
 *     <li>aucun arbitre n'est inscrit au DF ;</li>
 *     <li>l'arbitre ne répond pas avant {@link ActionAgent#DELAI}.</li>
 * </ul>
 */
public class ProposerFlag extends ActionAgent {
    /** défi -> flag validé */
    private final Map<String, String> flagsValides = new TreeMap<>();
    /** réputation des décodeurs automatiques (d7), mise à jour selon la réponse de l'arbitre ; peut être null */
    private Reputation reputation;

    /** @param agent l'agent (capitaine ou testeur) qui propose les flags, au nom de son équipe */
    public ProposerFlag(Agent agent) {
        super(agent);
    }

    /**
     * Branche la réputation des décodeurs automatiques (d7). Sans cet appel, la réputation n'est pas mise à jour.
     * @param reputation la réputation partagée avec {@link ConsulterTous} et {@link Retenir}
     */
    public void setReputation(Reputation reputation) {
        this.reputation = reputation;
    }

    /**
     * @return les flags acceptés par l'arbitre, triés par défi ({@code défi -> flag}).
     *         La map est modifiable : c'est la mémoire interne de l'action, pas une copie.
     */
    public Map<String, String> getFlagsValides() {
        return flagsValides;
    }

    @Override
    public String nom() {
        return "proposer_flag";
    }

    @Override
    public String description() {
        return "propose un flag à l'arbitre. ATTENTION : 3 essais maximum par défi, ne propose qu'un flag obtenu grâce aux agents.";
    }

    @Override
    public JSONObject parametres() {
        return new JSONObject().put("defi", "identifiant du défi, ex. \"d1\"").put("flag", "le flag complet, ex. \"CTF{...}\"");
    }

    /**
     * Vérifie la forme du flag, l'envoie au premier arbitre trouvé au DF et attend sa décision.
     * Si le flag est accepté, il est mémorisé ; dans tous les cas, la réputation est mise à jour si besoin.
     * @param arguments {@code defi} et {@code flag}
     * @return le contenu de la réponse de l'arbitre, avec son {@code performatif}
     *         et, le cas échéant, {@code reputation_mise_a_jour}
     * @throws IllegalArgumentException si {@code defi} manque ou si le flag n'a pas la forme {@code CTF{...}}
     * @throws Exception s'il n'y a pas d'arbitre ou s'il ne répond pas à temps
     */
    @Override
    public JSONObject executer(JSONObject arguments) throws Exception {
        String defi = arguments.optString("defi");
        String flag = arguments.optString("flag").trim();
        if (defi.isBlank() || !flag.startsWith("CTF{") || !flag.endsWith("}"))
            throw new IllegalArgumentException("il faut 'defi' et un 'flag' de la forme CTF{...}");
        List<AID> arbitres = Ctf.chercherAID(agent, Ctf.ARBITRE);
        if (arbitres.isEmpty()) throw new Exception("pas d'arbitre dans les pages jaunes");

        ACLMessage prop = Ctf.message(ACLMessage.PROPOSE, arbitres.getFirst(), new JSONObject().put("defi", defi).put("flag", flag));
        ACLMessage rep = envoyerEtAttendre(prop, DELAI);
        JSONObject c = Ctf.contenu(rep);
        boolean accepte = rep.getPerformative() == ACLMessage.ACCEPT_PROPOSAL;
        if (accepte) flagsValides.put(defi, flag);
        c.put("performatif", ACLMessage.getPerformative(rep.getPerformative()));
        if (reputation != null) {
            var agents = reputation.resultat(flag, accepte);
            if (!agents.isEmpty()) c.put("reputation_mise_a_jour", new JSONObject().put("agents", agents)
                    .put("variation", accepte ? 1 : -1).put("reputations", reputation.enJson()));
        }
        return c;
    }
}
