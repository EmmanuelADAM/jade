package ollama.ctfAgents.actions;

import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import ollama.ctfAgents.commun.Ctf;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Action « retenir » (défi d7) : la <b>seconde</b> moitié du Contract Net, dont la décision revient au LLM.
 * <p>
 * Après {@link ConsulterTous}, le LLM a reçu toutes les propositions des décodeurs automatiques. Avec cette
 * action, il désigne le candidat qui a du sens. Le programme envoie alors un {@code ACCEPT_PROPOSAL} à l'agent
 * choisi et un {@code REJECT_PROPOSAL} à tous les autres agents qui avaient fait une proposition. Les agents
 * qui avaient refusé ne reçoivent rien.
 *
 * <h2>Garde-fou</h2>
 * Le programme vérifie que le texte retenu a <b>vraiment</b> été proposé par cet agent lors de la dernière
 * consultation. La comparaison se fait au texte exact, aux espaces de début et de fin près. Le LLM ne peut
 * donc pas inventer un candidat, ni « corriger » un texte avant de le retenir.
 *
 * <h2>Arguments et résultat</h2>
 * <pre>
 *  arguments : {"agent":"auto_hex", "texte":"..."}   (agent = celui qui a fait la proposition,
 *                                                   c'est-à-dire le premier de la liste "par")
 *  messages  : ACCEPT_PROPOSAL {"texte":"..."} à auto_hex, REJECT_PROPOSAL {} aux autres proposants
 *  résultat  : {"retenu":"auto_hex", "chaine":["hex","base64"], "texte":"..."}
 * </pre>
 * Le choix est aussi transmis à la {@link Reputation} : si le flag proposé ensuite avec {@link ProposerFlag}
 * vient de ce texte, tous les agents de la chaîne ({@code par}) verront leur réputation monter ou baisser.
 * <p>
 * Aucune réponse n'est attendue des décodeurs : l'action ne bloque pas.
 *
 * <h2>Échecs</h2>
 * L'action lève une {@link IllegalArgumentException}, transformée en réponse {@code "ok":false} pour le LLM, si :
 * <ul>
 *     <li>aucune consultation n'a eu lieu, ou la dernière n'a reçu aucune proposition ;</li>
 *     <li>le texte ne figure pas parmi les candidats de l'agent indiqué.</li>
 * </ul>
 * Dans ces deux cas, aucun message n'est envoyé.
 * <p>
 * <b>Remarque</b> : les propositions de la consultation ne sont pas effacées après un choix. Un second appel
 * sur la même consultation renvoie donc de nouveaux {@code ACCEPT_PROPOSAL} / {@code REJECT_PROPOSAL}
 * et remplace le choix mémorisé pour la réputation.
 */
public class Retenir extends ActionAgent {
    /** l'action de consultation, qui garde les propositions de la dernière consultation */
    private final ConsulterTous consultation;
    /** réputation des décodeurs automatiques, informée du candidat retenu */
    private final Reputation reputation;

    /**
     * @param agent l'agent (capitaine ou testeur) qui répond aux propositions
     * @param consultation l'action {@code consulter_tous} dont on traite les propositions
     * @param reputation la réputation partagée avec {@link ConsulterTous} et {@link ProposerFlag}
     */
    public Retenir(Agent agent, ConsulterTous consultation, Reputation reputation) {
        super(agent);
        this.consultation = consultation;
        this.reputation = reputation;
    }

    @Override
    public String nom() {
        return "retenir";
    }

    @Override
    public String description() {
        return "après consulter_tous : retient UN candidat (celui qui a du sens). Recopie exactement son texte et le nom de l'agent "
                + "qui a fait la proposition (le premier de la liste 'par'). Si le texte retenu semble encore codé, "
                + "relance consulter_tous sur ce texte.";
    }

    @Override
    public JSONObject parametres() {
        return new JSONObject().put("agent", "nom de l'agent dont tu retiens la proposition")
                .put("texte", "le texte du candidat retenu, recopié exactement");
    }

    /**
     * Vérifie que le candidat a bien été proposé par l'agent indiqué, répond à toutes les propositions
     * (accepte celle-ci, rejette les autres) et mémorise le choix pour la réputation.
     * @param arguments {@code agent} et {@code texte} du candidat retenu
     * @return {@code {"retenu": agent, "chaine": [...], "texte": ...}}
     * @throws IllegalArgumentException s'il n'y a pas de proposition en cours, ou si ce texte n'a pas été proposé par cet agent
     */
    @Override
    public JSONObject executer(JSONObject arguments) {
        String nomAgent = arguments.optString("agent");
        String texte = arguments.optString("texte").trim();
        List<ACLMessage> props = consultation.getDernieresPropositions();
        if (props.isEmpty()) throw new IllegalArgumentException("aucune consultation en cours : appelle d'abord consulter_tous");

        ACLMessage choisie = null;
        JSONObject candidat = null;
        for (ACLMessage p : props) {
            if (!p.getSender().getLocalName().equals(nomAgent)) continue;
            JSONArray cands = Ctf.contenu(p).optJSONArray("candidats", new JSONArray());
            for (int i = 0; i < cands.length(); i++)
                if (cands.getJSONObject(i).getString("texte").trim().equals(texte)) {
                    choisie = p;
                    candidat = cands.getJSONObject(i);
                }
        }
        if (choisie == null)
            throw new IllegalArgumentException("ce texte n'a pas été proposé par '" + nomAgent + "' : recopie exactement un candidat");

        for (ACLMessage p : props) {
            ACLMessage r = p.createReply();
            r.setLanguage(Ctf.LANGAGE);
            r.setPerformative(p == choisie ? ACLMessage.ACCEPT_PROPOSAL : ACLMessage.REJECT_PROPOSAL);
            r.setContent(p == choisie ? new JSONObject().put("texte", texte).toString() : "{}");
            envoyer(r);
        }
        List<String> par = new ArrayList<>();
        candidat.getJSONArray("par").toList().forEach(o -> par.add(o.toString()));
        reputation.retenir(texte, par);
        return new JSONObject().put("retenu", nomAgent).put("chaine", candidat.getJSONArray("chaine")).put("texte", texte);
    }
}
