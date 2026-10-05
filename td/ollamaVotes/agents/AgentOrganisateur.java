package td.ollamaVotes.agents;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.WakerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.proto.ContractNetInitiator;
import org.json.JSONObject;

import java.util.*;

/**
 * Organisateur du vote : envoie un CFP aux agents du service "vote",
 * recoit les classements (JSON), calcule le resultat par la methode de Borda
 * et l'annonce a tous les votants (ACCEPT_PROPOSAL).
 */
public class AgentOrganisateur extends Agent {

    private static final String SUJET = "Sortie de fin d'annee de la promo";
    private static final Map<String, String> OPTIONS = new TreeMap<>(Map.of(
            "A", "diner dans un steakhouse",
            "B", "restaurant indien",
            "C", "restaurant 'légumes sur tables'",
            "D", "restaurant la pizzeria",
            "E", "brasserie moules-frittes"));

    @Override
    protected void setup() {
        // on laisse quelques secondes aux votants pour s'inscrire aux pages jaunes
        addBehaviour(new WakerBehaviour(this, 3000) {
            @Override
            protected void onWake() { lancerVote(); }
        });
    }

    private void lancerVote() {
        List<AID> votants = chercherVotants();
        if (votants.isEmpty()) {
            System.out.println("Organisateur -> aucun votant trouve.");
            return;
        }
        System.out.println("Organisateur -> " + votants.size() + " votants, sujet : " + SUJET);

        var cfp = new ACLMessage(ACLMessage.CFP);
        votants.forEach(cfp::addReceiver);
        cfp.setProtocol(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET);
        //pas oblige mais pour la forme, on structure les echanges entre agents JADE en JSON
        cfp.setLanguage("JSON");
        //les candidats recoivent le sujet de vote
        cfp.setContent(new JSONObject().put("sujet", SUJET).put("options", new JSONObject(OPTIONS)).toString());
        // un LLM peut prendre du temps, on va limiter le temps de reponse
        cfp.setReplyByDate(new Date(System.currentTimeMillis() + 120_000));

        addBehaviour(new ContractNetInitiator(this, cfp) {
            @Override
            protected void handleRefuse(ACLMessage refuse) {
                System.out.println("Organisateur -> " + refuse.getSender().getLocalName() + " n'a pas pu voter.");
            }

            @Override
            protected void handleAllResponses(List<ACLMessage> reponses, List<ACLMessage> acceptations) {
                Map<String, Integer> scores = new TreeMap<>();
                OPTIONS.keySet().forEach(c -> scores.put(c, 0));
                int n = OPTIONS.size();
                List<ACLMessage> propositions = new ArrayList<>();

                System.out.println("\n===== Bulletins =====");
                for (ACLMessage msg : reponses) {
                    if (msg.getPerformative() != ACLMessage.PROPOSE) continue;
                    var vote = new JSONObject(msg.getContent());
                    var classement = vote.getJSONArray("classement");
                    // Borda : n-1 points pour le 1er, n-2 pour le 2e, ..., 0 pour le dernier
                    var lisible = new StringJoiner(">");
                    for (int rang = 0; rang < classement.length(); rang++) {
                        String c = classement.getString(rang);
                        scores.merge(c, n - 1 - rang, Integer::sum);
                        lisible.add(c);
                    }
                    System.out.printf("%-8s %s  \"%s\"%n", msg.getSender().getLocalName(),
                            lisible, vote.getString("justification"));
                    propositions.add(msg);
                }

                if (propositions.isEmpty()) {
                    System.out.println("Organisateur -> aucun bulletin valide, pas de resultat.");
                    return;
                }

                List<String> resultat = new ArrayList<>(scores.keySet());
                resultat.sort((a, b) -> scores.get(b) - scores.get(a));
                var annonce = new StringBuilder("Gagnant : ").append(resultat.get(0))
                        .append(" (").append(OPTIONS.get(resultat.get(0))).append(") | scores ").append(scores);

                System.out.println("===== Resultat Borda =====");
                for (String c : resultat)
                    System.out.printf("%s  %3d pts  %s%n", c, scores.get(c), OPTIONS.get(c));
                System.out.println();

                // annonce du resultat a chaque votant ; les messages ajoutes ici sont envoyes automatiquement
                for (ACLMessage msg : propositions) {
                    ACLMessage accept = msg.createReply();
                    accept.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
                    accept.setContent(annonce.toString());
                    acceptations.add(accept);
                }
            }

            @Override
            protected void handleAllResultNotifications(List<ACLMessage> notifications) {
                System.out.println("Organisateur -> " + notifications.size() + " votants ont pris acte du resultat.");
            }
        });
    }

    private List<AID> chercherVotants() {
        var modele = new DFAgentDescription();
        var sd = new ServiceDescription();
        sd.setType("vote");
        modele.addServices(sd);
        List<AID> res = new ArrayList<>();
        try {
            for (DFAgentDescription fiche : DFService.search(this, modele)) res.add(fiche.getName());
        } catch (FIPAException fe) { fe.printStackTrace(); }
        return res;
    }
}
