package td.ollamaVotes.agents;

import jade.core.Agent;
import jade.core.behaviours.ThreadedBehaviourFactory;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.FailureException;
import jade.domain.FIPAAgentManagement.RefuseException;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.ContractNetResponder;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import td.ollamaVotes.tools.OllamaClient;

import java.util.*;

/**
 * Agent votant dont les preferences sont produites par un LLM (Ollama).
 * Argument 0 : la description de sa personnalite.
 * Il repond a un CFP par un PROPOSE dont le contenu (langage JSON) est
 * {"classement":["B","C","A"],"justification":"..."}
 */
public class AgentVotant extends Agent {
    private static final int NB_ESSAIS = 3;
    private static final int MOTS_MAX = 15;

    /**personnalité propre de l'agent..*/
    private String personnalite;
    /**boite à outils pour interroger un LLM via OLLAMA et recupere des donnees */
    private final OllamaClient llm = new OllamaClient();
    /**petit processus pour lancer al requete au LLM en parallèle (on aurait pu passer par un behaviour pour rester 100% JADE)*/
    private final ThreadedBehaviourFactory fabriqueThreads = new ThreadedBehaviourFactory();

    @Override
    protected void setup() {
        Object[] args = getArguments();
        personnalite = (args != null && args.length > 0) ? args[0].toString() : "Tu es une personne sans opinion marquee.";
        afficher("pret. Personnalite : " + personnalite);

        // inscription aux pages jaunes
        var carnet = new DFAgentDescription();
        carnet.setName(getAID());
        var service = new ServiceDescription();
        service.setType("vote");
        service.setName("vote-llm");
        carnet.addServices(service);
        try { DFService.register(this, carnet); } catch (FIPAException fe) { fe.printStackTrace(); }

        var modele = MessageTemplate.and(
                MessageTemplate.MatchProtocol(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET),
                MessageTemplate.MatchPerformative(ACLMessage.CFP));

        var repondeur = new ContractNetResponder(this, modele) {
            @Override
            protected ACLMessage handleCfp(ACLMessage cfp) throws RefuseException {
                var demande = new JSONObject(cfp.getContent());
                String sujet = demande.getString("sujet");
                JSONObject options = demande.getJSONObject("options");

                JSONObject vote = voter(sujet, options);
                if (vote == null) {
                    afficher("pas de vote valide apres " + NB_ESSAIS + " essais, je refuse.");
                    throw new RefuseException("vote-invalide");
                }
                afficher("je vote " + String.join(">", toList(vote.getJSONArray("classement")))
                        + " : " + vote.getString("justification"));

                ACLMessage propose = cfp.createReply();
                propose.setPerformative(ACLMessage.PROPOSE);
                propose.setLanguage("JSON");
                propose.setContent(vote.toString());
                return propose;
            }

            @Override
            protected ACLMessage handleAcceptProposal(ACLMessage cfp, ACLMessage propose, ACLMessage accept)
                    throws FailureException {
                afficher("resultat annonce : " + accept.getContent());
                ACLMessage inform = accept.createReply();
                inform.setPerformative(ACLMessage.INFORM);
                inform.setContent("vote pris en compte");
                return inform;
            }

            @Override
            protected void handleRejectProposal(ACLMessage cfp, ACLMessage propose, ACLMessage reject) {
                afficher("mon vote a ete ecarte : " + reject.getContent());
            }
        };
        // l'appel au LLM bloque plusieurs secondes : on fait tourner le repondeur dans son propre thread
        // pour ne pas geler les autres comportements de l'agent
        addBehaviour(fabriqueThreads.wrap(repondeur));
    }

    /** Interroge le LLM jusqu'a obtenir un classement valide ; null si echec. */
    private JSONObject voter(String sujet, JSONObject candidats) {
        List<String> lettres = new ArrayList<>(candidats.keySet());
        Collections.sort(lettres);

        // schema impose : un tableau de lettres prises parmi les candidats + une justification
        var schema = new JSONObject()
                .put("type", "object")
                .put("properties", new JSONObject()
                        .put("classement", new JSONObject()
                                .put("type", "array")
                                .put("items", new JSONObject().put("type", "string").put("enum", new JSONArray(lettres))))
                        .put("justification", new JSONObject().put("type", "string")))
                .put("required", new JSONArray().put("classement").put("justification"))
                .put("additionalProperties", false);

        String systeme = personnalite + " Tu reponds uniquement en JSON, en francais, dans le role de ce personnage.";

        for (int essai = 1; essai <= NB_ESSAIS; essai++) {
            // ordre de presentation melange a chaque essai pour limiter le biais de position
            List<String> ordre = new ArrayList<>(lettres);
            Collections.shuffle(ordre);
            var question = new StringBuilder("Sujet du vote : ").append(sujet).append("\nPropositions :\n");
            for (String l : ordre) question.append(l).append(" = ").append(candidats.getString(l)).append('\n');
            question.append("Classe TOUTES les propositions (").append(lettres.size())
                    .append(" lettres, chacune une seule fois) de ta preferee a la moins aimee, ")
                    .append("puis justifie ton choix en une phrase de ").append(MOTS_MAX).append(" mots maximum.")
                    .append("\nReponds avec ce schema JSON : ").append(schema);

            try {
                JSONObject reponse = llm.demander(systeme, question.toString(), schema, 0.7);
                // JSON bien forme mais cles inattendues : on affiche ce que le modele a vraiment renvoye
                if (!reponse.has("classement") || !reponse.has("justification")) {
                    afficher("essai " + essai + " : cles manquantes, recu " + reponse);
                    continue;
                }
                List<String> classement = toList(reponse.getJSONArray("classement"));
                // le schema ne garantit pas l'unicite : on verifie que c'est une permutation
                boolean valide = classement.size() == lettres.size() && new HashSet<>(classement).containsAll(lettres);
                if (valide) {
                    reponse.put("justification", tronquer(reponse.getString("justification")));
                    return reponse;
                }
                afficher("essai " + essai + " invalide : " + classement);
            } catch (JSONException e) {
                afficher("essai " + essai + " : JSON illisible (" + e.getMessage() + ")");
            } catch (Exception e) {
                afficher("essai " + essai + " : erreur Ollama (" + e + ")");
            }
        }
        return null;
    }

    /**transforme le tableau issue du format JSON en liste des choix
     * */
    private static List<String> toList(JSONArray tab) {
        List<String> l = new ArrayList<>();
        for (int i = 0; i < tab.length(); i++) l.add(tab.getString(i));
        return l;
    }

    private static String tronquer(String texte) {
        String[] mots = texte.trim().split("\\s+");
        if (mots.length <= MOTS_MAX) return texte.trim();
        return String.join(" ", Arrays.copyOf(mots, MOTS_MAX)) + "...";
    }

    private void afficher(String msg) {
        System.out.println(getLocalName() + " -> " + msg);
    }

    @Override
    protected void takeDown() {
        try { DFService.deregister(this); } catch (FIPAException fe) { fe.printStackTrace(); }
        fabriqueThreads.interrupt();
        afficher("quitte la plateforme.");
    }
}