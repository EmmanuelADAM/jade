package ollama.toolAgent.launch;

import ollama.toolAgent.llm.BoucleAgentique;
import ollama.toolAgent.llm.ClientLLM;
import ollama.toolAgent.outils.BoiteAOutils;
import ollama.toolAgent.outils.OutilDate;
import ollama.toolAgent.outils.OutilVacances;

/**
 * Test de la boucle agentique SANS JADE, dans la console.
 * Seuls les outils "date" et "vacances_scolaires" sont branchés : l'API météo (limitée) n'est pas utilisée.
 * <p>
 * Lancement : java -Dollama.modele=llama3.2:3b ollama.toolAgent.launch.TestBoucleAgentique "ma question"
 */
public class TestBoucleAgentique {
    public static void main(String[] args) throws Exception {
        BoiteAOutils outils = new BoiteAOutils()
                .ajouter(new OutilDate())
                .ajouter(new OutilVacances());

        BoucleAgentique boucle = new BoucleAgentique(new ClientLLM(), outils);
        boucle.setContexte("l'utilisateur se trouve à Valenciennes.");

        String[] questions = args.length > 0 ? args : new String[]{
                "Bonjour, qui es-tu ?",                                   // aucun outil attendu
                "Quand sont les prochaines vacances scolaires ?",          // date puis vacances_scolaires
                "Et dans combien de jours commencent-elles ?",             // utilise l'historique
        };
        for (String q : questions) {
            System.out.println("\n>>> " + q);
            System.out.println("<<< " + boucle.resoudre(q));
        }
    }
}
