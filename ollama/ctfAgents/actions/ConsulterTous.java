package ollama.ctfAgents.actions;

import jade.core.AID;
import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import ollama.ctfAgents.commun.Ctf;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * action « consulter_tous » (défi d7) : la PREMIÈRE moitié d'un Contract Net.
 * CFP à tous les décodeurs automatiques, collecte de toutes les propositions... et AUCUN choix :
 * c'est le LLM qui choisit ensuite, avec l'action « retenir ».
 * <p>
 * Les propositions sont gardées par le programme (pour pouvoir répondre ACCEPT / REJECT ensuite).
 */
public class ConsulterTous extends ActionAgent {
    /** attente max des propositions (les décodeurs consultent leurs collègues avant de répondre) */
    private static final long DELAI_PROPOSITIONS = 8_000;

    private final Reputation reputation;
    /** propositions de la dernière consultation (pour l'action retenir) */
    private List<ACLMessage> dernieresPropositions = List.of();

    public ConsulterTous(Agent agent, Reputation reputation) {
        super(agent);
        this.reputation = reputation;
    }

    public List<ACLMessage> getDernieresPropositions() {
        return dernieresPropositions;
    }

    @Override
    public String nom() {
        return "consulter_tous";
    }

    @Override
    public String description() {
        return "soumet un texte dont on ne connaît pas le codage à TOUS les décodeurs automatiques (une méthode chacun ; "
                + "ils se relaient entre eux si un résultat semble encore codé). Renvoie toutes leurs propositions : "
                + "chaque candidat a la chaîne de méthodes appliquées, les agents qui l'ont produit, le texte obtenu et sa lisibilité "
                + "(part de caractères de texte, PAS une preuve de sens), et la réputation de l'agent. "
                + "Tu dois ensuite choisir le candidat qui a du sens avec l'action retenir.";
    }

    @Override
    public JSONObject parametres() {
        return new JSONObject().put("donnees", "le texte à décoder, recopié exactement");
    }

    @Override
    public JSONObject executer(JSONObject arguments) throws Exception {
        String donnees = arguments.optString("donnees").trim();
        if (donnees.isEmpty()) throw new IllegalArgumentException("le paramètre 'donnees' est obligatoire");
        List<AID> decodeurs = Ctf.chercherAID(agent, Ctf.DECODEUR_AUTO);
        if (decodeurs.isEmpty()) throw new Exception("aucun décodeur automatique dans les pages jaunes");

        String conv = nouvelleConversation();
        var cfp = new ACLMessage(ACLMessage.CFP);
        decodeurs.forEach(cfp::addReceiver);
        cfp.setLanguage(Ctf.LANGAGE);
        cfp.setConversationId(conv);
        cfp.setContent(new JSONObject().put("operation", "auto").put("donnees", donnees).put("deja", new JSONArray()).toString());
        envoyer(cfp);

        List<ACLMessage> propositions = new ArrayList<>();
        var refus = new JSONArray();
        List<String> repondu = new ArrayList<>();
        long fin = System.currentTimeMillis() + DELAI_PROPOSITIONS;
        while (repondu.size() < decodeurs.size() && System.currentTimeMillis() < fin) {
            ACLMessage rep = attendre(conv, Math.max(1, fin - System.currentTimeMillis()));
            if (rep == null) break;
            repondu.add(rep.getSender().getLocalName());
            if (rep.getPerformative() == ACLMessage.PROPOSE) propositions.add(rep);
            else refus.put(new JSONObject().put("agent", rep.getSender().getLocalName())
                    .put("raison", Ctf.contenu(rep).optString("raison")));
        }
        dernieresPropositions = propositions;

        // présentation au LLM : sans doublons (même texte obtenu par plusieurs chemins : on garde le plus court)
        var props = new JSONArray();
        java.util.Set<String> dejaVus = new java.util.HashSet<>();
        for (ACLMessage p : propositions) {
            String nom = p.getSender().getLocalName();
            JSONArray cands = Ctf.contenu(p).optJSONArray("candidats", new JSONArray());
            var gardes = new JSONArray();
            for (int i = 0; i < cands.length(); i++)
                if (dejaVus.add(cands.getJSONObject(i).getString("texte"))) gardes.put(cands.getJSONObject(i));
            if (!gardes.isEmpty())
                props.put(new JSONObject().put("agent", nom).put("reputation", reputation.de(nom)).put("candidats", gardes));
        }
        var sansReponse = new JSONArray();
        for (AID a : decodeurs) if (!repondu.contains(a.getLocalName())) sansReponse.put(a.getLocalName());
        return new JSONObject().put("propositions", props).put("refus", refus).put("sans_reponse", sansReponse);
    }
}
