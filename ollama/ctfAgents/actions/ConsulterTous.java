package ollama.ctfAgents.actions;

import jade.core.AID;
import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import ollama.ctfAgents.commun.Ctf;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import jade.core.AID;
import jade.core.Agent;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.proto.ContractNetInitiator;
import ollama.ctfAgents.commun.Ctf;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * action « consulter_tous » (défi d7) : la PREMIÈRE moitié d'un Contract Net, avec {@link ContractNetInitiator}.
 * CFP à tous les décodeurs automatiques, collecte de toutes les propositions... et AUCUN choix :
 * handleAllResponses n'ajoute aucune acceptation, le protocole s'arrête là.
 * Le choix est DIFFÉRÉ : c'est le LLM qui choisit ensuite, avec l'action « retenir », qui répond alors
 * ACCEPT / REJECT aux PROPOSE gardés ici (les décodeurs n'attendent pas de réponse pour continuer).
 * <p>
 * Comme pour appel_offres : executer() tourne dans le thread du LLM et attend le résultat du protocole,
 * exécuté par le thread de l'agent.
 */
public class ConsulterTous extends ActionAgent {
    /** date limite des propositions (les décodeurs consultent leurs collègues avant de répondre) */
    private static final long DELAI_PROPOSITIONS = 8_000;

    private final Reputation reputation;
    /** propositions de la dernière consultation (pour l'action retenir) */
    private volatile List<ACLMessage> dernieresPropositions = List.of();

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
                + "chaque candidat a : la chaîne de méthodes appliquées, les agents qui l'ont produit, le texte obtenu et sa lisibilité "
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

        var cfp = new ACLMessage(ACLMessage.CFP);
        decodeurs.forEach(cfp::addReceiver);
        cfp.setProtocol(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET);
        cfp.setLanguage(Ctf.LANGAGE);
        cfp.setContent(new JSONObject().put("operation", "auto").put("donnees", donnees).put("deja", new JSONArray()).toString());
        cfp.setReplyByDate(new Date(System.currentTimeMillis() + DELAI_PROPOSITIONS));
        compter(decodeurs.size(), 0);

        CompletableFuture<List<ACLMessage>> reponses = new CompletableFuture<>();
        agent.addBehaviour(new ContractNetInitiator(agent, cfp) {
            @Override
            protected void handleAllResponses(List<ACLMessage> toutes, List<ACLMessage> acceptations) {
                // AUCUNE acceptation ici : le choix appartient au LLM (action retenir)
                reponses.complete(new ArrayList<>(toutes));
            }

            @Override
            public int onEnd() {
                if (!reponses.isDone()) reponses.complete(List.of());
                return super.onEnd();
            }
        });

        List<ACLMessage> toutes;
        try {
            toutes = reponses.get(DELAI_PROPOSITIONS + DELAI, TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            throw new Exception("consultation sans réponse dans les délais");
        }
        compter(0, toutes.size());

        List<ACLMessage> propositions = new ArrayList<>();
        var refus = new JSONArray();
        Set<String> repondu = new HashSet<>();
        for (ACLMessage rep : toutes) {
            repondu.add(rep.getSender().getLocalName());
            if (rep.getPerformative() == ACLMessage.PROPOSE) propositions.add(rep);
            else refus.put(new JSONObject().put("agent", rep.getSender().getLocalName())
                    .put("raison", Ctf.contenu(rep).optString("raison")));
        }
        dernieresPropositions = propositions;

        // présentation au LLM : sans doublons (même texte obtenu par plusieurs chemins : on garde le premier)
        var props = new JSONArray();
        Set<String> dejaVus = new HashSet<>();
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
