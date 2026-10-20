package ollama.ctfAgents.agents;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import ollama.ctfAgents.commun.Ctf;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Agent ARCHIVISTE : agent "pur" (sans LLM) qui détient <b>un</b> fragment du flag du défi {@code d5}.
 * <p>
 * Pour reconstituer le flag de {@code d5}, une équipe doit obtenir les fragments de tous les archivistes.
 * Chacun ne donne le sien qu'en échange d'une <i>preuve</i> : un couple (défi, flag) que l'équipe
 * a déjà fait valider par l'arbitre. Au démarrage, l'archiviste s'inscrit au DF sous le type
 * {@link Ctf#ARCHIVISTE} en publiant son numéro de fragment et cette condition.
 * <p>
 * <b>Arguments de lancement</b> (obligatoires) : {@code [0]} numéro du fragment, {@code [1]} texte du fragment.
 *
 * <h2>Protocole</h2>
 * L'archiviste ne connaît pas les flags : il fait vérifier chaque preuve par l'arbitre.
 * <pre>
 *  REQUEST  équipe -> archiviste      {"demande":"fragment","preuve":{"defi":"d1","flag":"CTF{...}"}}
 *  QUERY_IF archiviste -> arbitre     {"equipe":"capitaine","defi":"d1","flag":"CTF{...}"}   (nouvelle conversation)
 *  INFORM   arbitre -> archiviste     {"valide":true|false, "raison":...}
 *  INFORM   archiviste -> équipe      {"ok":true,"numero":2,"fragment":"agents_"}
 *        ou REFUSE                    {"ok":false,"erreur":"preuve refusée : ..."}
 * </pre>
 * Autres réponses possibles à l'équipe :
 * <ul>
 *     <li>{@code REFUSE} si la requête n'a pas le format attendu ;</li>
 *     <li>{@code FAILURE} si aucun arbitre n'est inscrit au DF ;</li>
 *     <li>{@code NOT_UNDERSTOOD} pour tout message qui n'est ni une {@code REQUEST}
 *         ni une réponse attendue de l'arbitre.</li>
 * </ul>
 *
 * <h2>Fonctionnement non bloquant</h2>
 * L'agent ne reste jamais bloqué à attendre l'arbitre. Chaque vérification ouvre une conversation
 * d'identifiant unique ({@code preuve-<uuid>}), associée à la requête d'origine dans {@code enAttente}.
 * Quand un message arrive sur une de ces conversations, c'est la réponse de l'arbitre et l'archiviste
 * répond alors à l'équipe. Il peut donc traiter plusieurs demandes en parallèle.
 *
 * <h2>Règles</h2>
 * <ul>
 *     <li>L'équipe transmise à l'arbitre est l'<b>expéditeur réel</b> de la requête ({@code getSender()}) :
 *         une équipe ne peut pas se servir des flags d'une autre.</li>
 *     <li>L'arbitre refuse qu'une même preuve serve auprès de deux archivistes différents :
 *         il faut une preuve distincte pour chaque fragment.</li>
 * </ul>
 */
public class AgentArchiviste extends Agent {
    /** numéro du fragment détenu (sa position dans le flag reconstitué) */
    private int numero;
    /** texte du fragment, donné contre une preuve valide */
    private String fragment;
    /** conversation avec l'arbitre -> requête d'origine en attente de réponse */
    private final Map<String, ACLMessage> enAttente = new HashMap<>();

    @Override
    protected void setup() {
        Object[] args = getArguments();
        numero = Integer.parseInt(String.valueOf(args[0]));
        fragment = String.valueOf(args[1]);
        Ctf.inscrire(this, Ctf.ARCHIVISTE, Map.of(
                "numero", String.valueOf(numero),
                "condition", "donne son fragment contre une preuve {defi, flag} déjà validée par l'arbitre, une preuve différente par archiviste"));
        Ctf.log(this, "prêt, je détiens le fragment n°" + numero);

        addBehaviour(new CyclicBehaviour(this) {
            @Override
            public void action() {
                ACLMessage msg = receive();
                if (msg == null) {
                    block();
                    return;
                }
                ACLMessage origine = enAttente.remove(msg.getConversationId());
                if (origine != null) repondreApresVerification(origine, msg);
                else if (msg.getPerformative() == ACLMessage.REQUEST) demanderVerification(msg);
                else send(Ctf.repondre(msg, ACLMessage.NOT_UNDERSTOOD, new JSONObject().put("erreur", "j'attends une REQUEST")));
            }
        });
    }

    /**
     * Étape 1 : une équipe demande le fragment.
     * <p>
     * Vérifie le format de la requête, puis envoie un {@code QUERY_IF} au premier arbitre trouvé au DF,
     * dans une nouvelle conversation. La requête d'origine est mise en attente de l'avis de l'arbitre.
     * @param requete la requête de l'équipe
     */
    private void demanderVerification(ACLMessage requete) {
        JSONObject c = Ctf.contenu(requete);
        JSONObject preuve = c.optJSONObject("preuve");
        if (!"fragment".equals(c.optString("demande")) || preuve == null || !preuve.has("defi") || !preuve.has("flag")) {
            send(Ctf.repondre(requete, ACLMessage.REFUSE, new JSONObject().put("ok", false)
                    .put("erreur", "format attendu : {\"demande\":\"fragment\",\"preuve\":{\"defi\":\"dX\",\"flag\":\"CTF{...}\"}}")));
            return;
        }
        var arbitres = Ctf.chercherAID(this, Ctf.ARBITRE);
        if (arbitres.isEmpty()) {
            send(Ctf.repondre(requete, ACLMessage.FAILURE, new JSONObject().put("ok", false).put("erreur", "pas d'arbitre pour vérifier")));
            return;
        }
        AID arbitre = arbitres.getFirst();
        ACLMessage question = Ctf.message(ACLMessage.QUERY_IF, arbitre, new JSONObject()
                .put("equipe", requete.getSender().getLocalName())
                .put("defi", preuve.getString("defi"))
                .put("flag", preuve.getString("flag")));
        String conv = "preuve-" + UUID.randomUUID();
        question.setConversationId(conv);
        enAttente.put(conv, requete);
        send(question);
    }

    /**
     * Étape 2 : l'arbitre a répondu ; on répond à l'équipe.
     * <p>
     * Le fragment n'est donné que si l'arbitre a répondu par un {@code INFORM} avec {@code "valide":true} ;
     * sinon l'équipe reçoit un {@code REFUSE} qui reprend la raison donnée par l'arbitre.
     * @param requete la requête d'origine de l'équipe
     * @param avis la réponse de l'arbitre
     */
    private void repondreApresVerification(ACLMessage requete, ACLMessage avis) {
        JSONObject c = Ctf.contenu(avis);
        String equipe = requete.getSender().getLocalName();
        if (avis.getPerformative() == ACLMessage.INFORM && c.optBoolean("valide")) {
            Ctf.log(this, "preuve " + c.optString("defi") + " acceptée, fragment n°" + numero + " donné à " + equipe);
            send(Ctf.repondre(requete, ACLMessage.INFORM, new JSONObject().put("ok", true).put("numero", numero).put("fragment", fragment)));
        } else {
            Ctf.log(this, "preuve refusée pour " + equipe + " : " + c);
            send(Ctf.repondre(requete, ACLMessage.REFUSE, new JSONObject().put("ok", false)
                    .put("erreur", "preuve refusée : " + c.optString("raison", c.optString("erreur")))));
        }
    }

    @Override
    protected void takeDown() {
        Ctf.desinscrire(this);
    }
}
