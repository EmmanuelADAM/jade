package ollama.toolAgent.llm;

import ollama.toolAgent.outils.BoiteAOutils;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

/**
 * La boucle "agentique" (schéma ReAct : raisonner -> agir -> observer -> ...) :
 * <pre>
 *   question
 *      |
 *      v
 *   +--------+  appel_outil   +--------------+
 *   |  LLM   | -------------> | BoiteAOutils |
 *   |        | <------------- | (exécution)  |
 *   +--------+ resultat_outil +--------------+
 *      |
 *      | reponse_finale
 *      v
 *   réponse à l'utilisateur
 * </pre>
 * Le LLM ne fait que DÉCIDER (quel outil, avec quels arguments) ; c'est le programme (l'agent)
 * qui AGIT et qui garde le contrôle : nombre d'étapes limité, appels en double refusés, erreurs gérées.
 * <p>
 * Cette classe ne dépend pas de JADE : elle peut être utilisée seule (voir TestBoucleAgentique)
 * ou par un agent JADE (voir AgentOutille).
 */
public class BoucleAgentique {
    /** le modèle de langage */
    private final ClientLLM llm;
    /** les outils utilisables */
    private final BoiteAOutils outils;
    /** nombre maximum d'appels au LLM pour une question (évite les boucles infinies) */
    private int maxEtapes = 6;
    /** informations ajoutées au prompt système (ex. "l'utilisateur habite Valenciennes") */
    private String contexte = "";
    /** questions et réponses finales précédentes (pour pouvoir dire "et à Lille ?") */
    private final List<JSONObject> historique = new ArrayList<>();
    /** pour afficher le déroulement (console, fenêtre...) */
    private Consumer<String> trace = System.out::println;

    public BoucleAgentique(ClientLLM llm, BoiteAOutils outils) {
        this.llm = llm;
        this.outils = outils;
    }

    public void setContexte(String contexte) {
        this.contexte = contexte;
    }

    public void setTrace(Consumer<String> trace) {
        this.trace = trace;
    }

    public void setMaxEtapes(int maxEtapes) {
        this.maxEtapes = maxEtapes;
    }

    public ClientLLM getLlm() {
        return llm;
    }

    /**
     * fait travailler le LLM et les outils jusqu'à obtenir une réponse finale
     *
     * @param question question de l'utilisateur
     * @return la réponse finale du LLM
     */
    public String resoudre(String question) throws Exception {
        String schema = Protocole.schema(outils);
        List<JSONObject> messages = new ArrayList<>();
        messages.add(Protocole.message("system", Protocole.promptSysteme(outils, contexte)));
        messages.addAll(historique);
        messages.add(Protocole.message("user", question));

        Set<String> appelsDejaFaits = new HashSet<>();
        //tente maxEtapes au plus de trouver une reponse à la question via les outils
        int i=1;
        boolean reponseOk = false;
        String reponse = null;
        while (i<=maxEtapes && !reponseOk)
        {
            // 1. le LLM décide : à partir d'un message contenant la question et des precedents messages
            // le LLM fourni une reponse au format JSON demandé
            JSONObject decision = llm.discuter(messages, schema);
            String texteDecision = Protocole.texte(decision);
            trace.accept("[étape " + i + "] LLM -> agent : " + texteDecision);
            messages.add(Protocole.message("assistant", texteDecision));

            // 2a. réponse finale : c'est fini
            String type = decision.optString("type");
            if (Protocole.REPONSE_FINALE.equals(type)) {
                reponse = decision.optString("reponse", "");
                memoriser(question, decision);
                reponseOk = true;
            }
            else {
                // 2b. c'est un appel  d'outil : l'agent exécute (ou refuse) et renvoie le résultat au LLM
                String nomOutil = decision.optString("outil");
                JSONObject arguments = decision.optJSONObject("arguments", new JSONObject());
                String signature = nomOutil + " " + arguments; // ex. meteo {"ville":"Lille"}
                JSONObject resultat;
                if (!appelsDejaFaits.add(signature)) { //l'appel a deja ete demande
                    resultat = new JSONObject().put("type", Protocole.RESULTAT_OUTIL).put("outil", nomOutil)
                            .put("ok", false)
                            .put("erreur", "appel déjà effectué avec ces arguments : utilise le résultat précédent ou donne ta réponse finale");
                } else {//sinon, on execute
                    resultat = outils.executer(nomOutil, arguments);
                }
                trace.accept("[étape " + i + "] agent -> LLM : " + resultat.toString());
                messages.add(Protocole.message("user", resultat.toString()));

                // dernière étape possible : on exige une réponse
                if (i == maxEtapes - 1)
                    messages.add(Protocole.message("user",
                            "{\"type\":\"consigne\",\"texte\":\"plus d'appel d'outil possible, donne maintenant ta reponse_finale\"}"));
                //on reboucle pour envoyer les nouveaux messages au LLM
            }
        }
        if (!reponseOk)  reponse =  "Désolé, je n'ai pas réussi à répondre en " + maxEtapes + " étapes.";
        return reponse;
    }

    /** garde la question et la réponse finale (sans les échanges avec les outils) */
    private void memoriser(String question, JSONObject decision) {
        historique.add(Protocole.message("user", question));
        historique.add(Protocole.message("assistant", Protocole.texte(decision)));
        // on ne garde que les 5 derniers échanges
        while (historique.size() > 10) historique.removeFirst();
    }

    /** oublie les échanges précédents */
    public void oublier() {
        historique.clear();
    }
}
