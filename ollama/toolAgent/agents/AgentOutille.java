package ollama.toolAgent.agents;

import jade.core.behaviours.OneShotBehaviour;
import jade.gui.GuiAgent;
import jade.gui.GuiEvent;
import ollama.internationalCuisine.gui.GuiOllamaAgent;
import ollama.internationalCuisine.tools.OllamaTools;
import ollama.toolAgent.llm.BoucleAgentique;
import ollama.toolAgent.llm.ClientLLM;
import ollama.toolAgent.outils.BoiteAOutils;
import ollama.toolAgent.outils.OutilDate;
import ollama.toolAgent.outils.OutilMeteo;
import ollama.toolAgent.outils.OutilVacances;

/**
 * Agent JADE "outillé" : il dialogue avec l'utilisateur via un LLM (Ollama)
 * et laisse le LLM décider des outils à utiliser (date, météo, calendrier scolaire).
 * <p>
 * Exemples de questions :
 * <ul>
 *     <li>quel temps fait-il ?</li>
 *     <li>quand sont les prochaines vacances ? dans combien de jours ?</li>
 *     <li>pendant les prochaines vacances, je pars à Nice : quel temps y fait-il en ce moment ?</li>
 *     <li>combien font 12 fois 7 ? (aucun outil nécessaire)</li>
 * </ul>
 * Les échanges JSON entre le LLM et l'agent sont affichés dans la fenêtre : c'est le but !
 */
public class AgentOutille extends GuiAgent {
    /** fenêtre de dialogue (reprise de l'exemple internationalCuisine) */
    private GuiOllamaAgent window;
    /** la boucle LLM <-> outils */
    private BoucleAgentique boucle;
    /** ville de l'utilisateur (donnée comme contexte au LLM, PAS d'appel météo automatique) */
    private String ville = "Valenciennes";

    @Override
    protected void setup() {
        window = new GuiOllamaAgent(this);
        window.println("Bonjour ! Je suis " + getLocalName() + ", un agent qui laisse un LLM utiliser des outils.");

        // 1. la boîte à outils de l'agent (série d'appels, je n'aime pas trop, mais c'est moderne...)
        BoiteAOutils outils = new BoiteAOutils()
                .ajouter(new OutilDate())
                .ajouter(new OutilMeteo())
                .ajouter(new OutilVacances());
        window.println("outils disponibles : " + outils.noms());

        // 2. le modèle LLM (utilisation de la librairie perso OllamaTools)
        ClientLLM llm = new ClientLLM();
        try {
            String[] modeles = new OllamaTools().listModels();
            String modele = OllamaTools.chooseModel(modeles);
            llm.setModele(modele);
            window.setModels(OllamaTools.chatModels(modeles), modele);
        } catch (Exception e) {
            window.println("Ollama injoignable (" + e.getMessage() + "), modèle par défaut : " + llm.getModele());
        }
        window.println("modèle utilisé : " + llm.getModele());

        // 3. la boucle agentique, qui affiche ses échanges dans la fenêtre
        boucle = new BoucleAgentique(llm, outils);
        //petite astuce pour rediriger les texte vers la console, la fenetre, un fichier, etc.
        boucle.setTrace(window::println);
        //indiquer au llm le contexte (le lieu)
        majContexte();
        window.setCity(ville);
        window.println("Posez une question (ex. \"quand sont les prochaines vacances ?\")");
    }

    /** le contexte donné au LLM : où se trouve l'utilisateur */
    private void majContexte() {
        boucle.setContexte("l'utilisateur se trouve à " + ville + ". Utilise cette ville si la question ne précise pas de lieu.");
    }

    /** pose la question au LLM, qui pourra utiliser des outils avant de répondre */
    private void poserQuestion(String question) {
        if (question == null || question.isBlank()) return;
        addBehaviour(new OneShotBehaviour(this) {
            public void action() {
                window.println("\n>>> " + question);
                window.startWaiting("le LLM réfléchit (" + boucle.getLlm().getModele() + ")...");
                try {
                    //fait travailler le LLM jusqu'à avoir une réponse finale, jusqu'à un certain nombre de de boucles
                    String reponse = boucle.resoudre(question);
                    window.stopWaiting();
                    window.print(reponse);   // la réponse finale, au format markdown
                    window.println("");
                    window.println("(appels réels à l'API météo depuis le lancement : " + OutilMeteo.getNbAppelsApi() + ")");
                    window.println("~".repeat(50));
                } catch (Exception e) {
                    window.stopWaiting();
                    window.println("erreur : " + e.getMessage());
                }
            }
        });
    }

    @Override
    protected void onGuiEvent(GuiEvent ev) {
        switch (ev.getType()) {
            case GuiOllamaAgent.SENDQUERY -> poserQuestion(window.lowTextArea.getText());
            case GuiOllamaAgent.CHANGEMODEL -> {
                boucle.getLlm().setModele((String) ev.getParameter(0));
                window.println("modèle utilisé : " + boucle.getLlm().getModele());
            }
            case GuiOllamaAgent.CHANGECITY -> {
                ville = (String) ev.getParameter(0);
                majContexte();
                window.println("ville de l'utilisateur : " + ville + " (la météo ne sera demandée que si le LLM le décide)");
            }
            case GuiOllamaAgent.QUITCODE -> {
                window.dispose();
                doDelete();
                System.exit(0);
            }
        }
    }

    @Override
    protected void takeDown() {
        window.println("Agent " + getLocalName() + " quitte la plateforme.");
    }

    /** lance JADE et un agent outillé */
    public static void main(String[] args) {
        jade.Boot.main(new String[]{"-gui", "assistant:ollama.toolAgent.agents.AgentOutille"});
    }
}
