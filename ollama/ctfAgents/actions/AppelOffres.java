package ollama.ctfAgents.actions;

import jade.core.AID;
import jade.core.Agent;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.proto.ContractNetInitiator;
import ollama.ctfAgents.commun.Ctf;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * action « appel_offres » : un Contract Net FIPA, avec le comportement {@link ContractNetInitiator} de JADE.
 * <pre>
 *  CFP              -> tous les agents du service
 *  PROPOSE / REFUSE <- chacun (jusqu'à la date limite du CFP)
 *  ACCEPT_PROPOSAL  -> la proposition au plus petit délai ; REJECT_PROPOSAL -> les autres   (handleAllResponses)
 *  INFORM / FAILURE <- l'agent choisi                                                         (handleInform / handleFailure)
 * </pre>
 * <b>Deux threads</b> : cette méthode executer() est appelée par la boucle du LLM, qui tourne dans son PROPRE thread
 * (ThreadedBehaviourFactory, voir AgentCapitaine). Elle ajoute le ContractNetInitiator à l'agent, puis attend
 * son résultat (CompletableFuture). Pendant ce temps, le thread de l'agent exécute le protocole.
 * Si la boucle du LLM tournait dans le thread de l'agent, le ContractNetInitiator ne démarrerait jamais :
 * l'agent attendrait un résultat que lui seul peut produire (interblocage).
 */
public class AppelOffres extends ActionAgent {
    /** attente des propositions (ms) : date limite du CFP */
    private static final long DELAI_PROPOSITIONS = 5_000;

    public AppelOffres(Agent agent) {
        super(agent);
    }

    @Override
    public String nom() {
        return "appel_offres";
    }

    /**descrip^tion des capacités de l'agent pour le LLM*/
    @Override
    public String description() {
        return "lance un appel d'offres (Contract Net) auprès de TOUS les agents d'un service : seuls ceux qui savent faire "
                + "l'opération répondent, le plus rapide est choisi et exécute la tâche. Utile quand tu ne sais pas quel agent peut faire "
                + "l'opération. Exemple : {\"service\":\"decodeur\",\"operation\":\"cesar_toutes_cles\",\"donnees\":\"...\"}";
    }

    /**modele JSON des paramètres que doit fournir le LLM à la fonction à exécuter */
    @Override
    public JSONObject parametres() {
        return new JSONObject()
                .put("service", "\"decodeur\" ou \"analyste\"")
                .put("...", "les autres champs (operation, donnees...) décrivent la tâche");
    }

    /**fonction lancée par la boucle LLM si l'agent est choisi*/
    @Override
    public JSONObject executer(JSONObject arguments) throws Exception {
        String service = arguments.optString("service");
        List<AID> candidats = Ctf.chercherAID(agent, service);
        if (candidats.isEmpty()) throw new IllegalArgumentException("aucun agent pour le service '" + service + "'");
        var tache = new JSONObject(arguments.toString());
        tache.remove("service");

        var cfp = new ACLMessage(ACLMessage.CFP);
        candidats.forEach(cfp::addReceiver);
        cfp.setProtocol(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET);
        cfp.setLanguage(Ctf.LANGAGE);
        cfp.setContent(tache.toString());
        cfp.setReplyByDate(new Date(System.currentTimeMillis() + DELAI_PROPOSITIONS));
        compter(candidats.size(), 0);

        // le résultat sera fourni par le ContractNetInitiator (thread de l'agent) au thread du LLM
        CompletableFuture<JSONObject> resultat = new CompletableFuture<>();
        var bilan = new JSONObject().put("candidats", candidats.size());

        agent.addBehaviour(new ContractNetInitiator(agent, cfp) {
            /** l'agent retenu (pour ignorer d'éventuels messages d'autres agents) */
            private AID choisi;

            @Override
            protected void handleAllResponses(List<ACLMessage> reponses, List<ACLMessage> acceptations) {
                compter(0, reponses.size());
                var refus = new JSONArray();
                ACLMessage meilleure = null;
                for (ACLMessage r : reponses) {
                    if (r.getPerformative() != ACLMessage.PROPOSE) {
                        refus.put(r.getSender().getLocalName());
                    } else if (meilleure == null
                            || Ctf.contenu(r).optLong("delai_ms") < Ctf.contenu(meilleure).optLong("delai_ms")) {
                        meilleure = r;
                    }
                }
                bilan.put("refus", refus).put("sans_reponse", candidats.size() - reponses.size());
                if (meilleure == null) { // personne ne sait faire : le protocole s'arrête (aucune acceptation)
                    resultat.completeExceptionally(new Exception("aucune proposition ; " + bilan));
                    return;
                }
                var props = new JSONArray();
                for (ACLMessage r : reponses) {
                    if (r.getPerformative() != ACLMessage.PROPOSE) continue;
                    long delai = Ctf.contenu(r).optLong("delai_ms");
                    props.put(new JSONObject().put("agent", r.getSender().getLocalName()).put("delai_ms", delai));
                    ACLMessage rep = r.createReply();
                    rep.setLanguage(Ctf.LANGAGE);
                    if (r == meilleure) {
                        rep.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
                        rep.setContent(tache.toString());
                        // sans date limite, le ContractNetInitiator attendrait le résultat indéfiniment
                        rep.setReplyByDate(new Date(System.currentTimeMillis() + delai + DELAI));
                    } else {
                        rep.setPerformative(ACLMessage.REJECT_PROPOSAL);
                        rep.setContent("{}");
                    }
                    acceptations.add(rep); // envoyées automatiquement à la fin de cette méthode
                }
                compter(acceptations.size(), 0);
                choisi = meilleure.getSender();
                bilan.put("propositions", props).put("choisi", choisi.getLocalName());
            }

            @Override
            protected void handleInform(ACLMessage inform) {
                compter(0, 1);
                if (inform.getSender().equals(choisi)) resultat.complete(bilan.put("contenu", Ctf.contenu(inform)));
            }

            @Override
            protected void handleFailure(ACLMessage echec) {
                compter(0, 1);
                resultat.completeExceptionally(new Exception("FAILURE de " + echec.getSender().getLocalName() + " : " + echec.getContent()));
            }

            @Override
            protected void handleAllResultNotifications(List<ACLMessage> notifications) {
                if (!resultat.isDone())
                    resultat.completeExceptionally(new Exception(bilan.optString("choisi") + " a été choisi mais n'a pas répondu (arrêté ?) ; " + bilan));
            }

            @Override
            public int onEnd() { // filet de sécurité : le protocole est terminé sans résultat
                if (!resultat.isDone()) resultat.completeExceptionally(new Exception("protocole terminé sans résultat ; " + bilan));
                return super.onEnd();
            }
        });

        // le thread du LLM attend ici (avec une limite)
        try {
            return resultat.get(DELAI_PROPOSITIONS + 2 * DELAI, TimeUnit.MILLISECONDS);
        } catch (ExecutionException e) {
            throw new Exception(e.getCause().getMessage());
        } catch (TimeoutException e) {
            throw new Exception("appel d'offres sans réponse dans les délais ; " + bilan);
        }
    }
}
