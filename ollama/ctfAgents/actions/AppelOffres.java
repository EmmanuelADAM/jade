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
 * action « appel_offres » : Contract Net écrit « à la main » (pour voir chaque étape).
 * <pre>
 *  CFP              -> tous les agents du service
 *  PROPOSE / REFUSE <- chacun (attente limitée)
 *  ACCEPT_PROPOSAL  -> la proposition au plus petit délai ; REJECT_PROPOSAL -> les autres
 *  INFORM / FAILURE <- l'agent choisi
 * </pre>
 * Le contenu du CFP (et de l'ACCEPT) est la tâche : tous les arguments sauf "service".
 */
public class AppelOffres extends ActionAgent {
    /** attente des propositions (ms) */
    private static final long DELAI_PROPOSITIONS = 5_000;

    public AppelOffres(Agent agent) {
        super(agent);
    }

    @Override
    public String nom() {
        return "appel_offres";
    }

    @Override
    public String description() {
        return "lance un appel d'offres (Contract Net) auprès de TOUS les agents d'un service : seuls ceux qui savent faire "
                + "l'opération répondent, le plus rapide est choisi et exécute la tâche. Utile quand tu ne sais pas quel agent peut faire "
                + "l'opération. Exemple : {\"service\":\"decodeur\",\"operation\":\"cesar_toutes_cles\",\"donnees\":\"...\"}";
    }

    @Override
    public JSONObject parametres() {
        return new JSONObject()
                .put("service", "\"decodeur\" ou \"analyste\"")
                .put("...", "les autres champs (operation, donnees...) décrivent la tâche");
    }

    @Override
    public JSONObject executer(JSONObject arguments) throws Exception {
        String service = arguments.optString("service");
        List<AID> candidats = Ctf.chercherAID(agent, service);
        if (candidats.isEmpty()) throw new IllegalArgumentException("aucun agent pour le service '" + service + "'");
        var tache = new JSONObject(arguments.toString());
        tache.remove("service");

        // 1. CFP à tous
        String conv = nouvelleConversation();
        var cfp = new ACLMessage(ACLMessage.CFP);
        candidats.forEach(cfp::addReceiver);
        cfp.setLanguage(Ctf.LANGAGE);
        cfp.setContent(tache.toString());
        cfp.setConversationId(conv);
        envoyer(cfp);

        // 2. collecte des réponses (jusqu'à toutes, ou délai dépassé)
        List<ACLMessage> propositions = new ArrayList<>();
        var refus = new JSONArray();
        long fin = System.currentTimeMillis() + DELAI_PROPOSITIONS;
        int reponses = 0;
        while (reponses < candidats.size() && System.currentTimeMillis() < fin) {
            ACLMessage rep = attendre(conv, Math.max(1, fin - System.currentTimeMillis()));
            if (rep == null) break;
            reponses++;
            if (rep.getPerformative() == ACLMessage.PROPOSE) propositions.add(rep);
            else refus.put(rep.getSender().getLocalName());
        }
        var bilan = new JSONObject().put("candidats", candidats.size()).put("refus", refus);
        if (propositions.isEmpty())
            throw new Exception("aucune proposition (" + refus.length() + " refus, " + (candidats.size() - reponses) + " sans réponse)");

        // 3. choix : le plus petit délai annoncé
        ACLMessage meilleure = propositions.get(0);
        for (ACLMessage p : propositions)
            if (Ctf.contenu(p).optLong("delai_ms") < Ctf.contenu(meilleure).optLong("delai_ms")) meilleure = p;
        var props = new JSONArray();
        for (ACLMessage p : propositions) {
            props.put(new JSONObject().put("agent", p.getSender().getLocalName()).put("delai_ms", Ctf.contenu(p).optLong("delai_ms")));
            ACLMessage r = p.createReply();
            r.setLanguage(Ctf.LANGAGE);
            if (p == meilleure) {
                r.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
                r.setContent(tache.toString());
            } else {
                r.setPerformative(ACLMessage.REJECT_PROPOSAL);
                r.setContent("{}");
            }
            envoyer(r);
        }
        String choisi = meilleure.getSender().getLocalName();
        bilan.put("propositions", props).put("choisi", choisi);

        // 4. résultat de l'agent choisi
        long delaiAnnonce = Ctf.contenu(meilleure).optLong("delai_ms");
        ACLMessage resultat;
        do {
            resultat = attendre(conv, delaiAnnonce + DELAI);
        } while (resultat != null && !resultat.getSender().equals(meilleure.getSender())); // réponses tardives des autres ignorées
        if (resultat == null) throw new Exception(choisi + " a été choisi mais n'a pas répondu (arrêté ?) ; " + bilan);
        if (resultat.getPerformative() != ACLMessage.INFORM)
            throw new Exception(ACLMessage.getPerformative(resultat.getPerformative()) + " de " + choisi + " : " + resultat.getContent());
        return bilan.put("contenu", Ctf.contenu(resultat));
    }
}
