package ollama.ctfAgents.launch;

import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.util.ExtendedProperties;
import jade.wrapper.AgentContainer;
import jade.wrapper.StaleProxyException;
import ollama.ctfAgents.agents.*;

/**
 * Lance le CTF : crée un conteneur principal JADE, les agents « purs », puis le ou les capitaines d"équipes.
 *
 * <h2>Agents créés</h2>
 * <table border="1">
 *     <caption>Agents lancés dans tous les modes</caption>
 *     <tr><th>nom</th><th>classe</th><th>rôle / arguments</th></tr>
 *     <tr><td>{@code arbitre}</td><td>{@link AgentArbitre}</td><td>flags, essais et scores</td></tr>
 *     <tr><td>{@code decodeur1}</td><td>{@link AgentDecodeur}</td>
 *         <td>rapide (200 ms) mais incomplet : base64, hex, rot13, cesar</td></tr>
 *     <tr><td>{@code decodeur2}</td><td>{@link AgentDecodeur}</td>
 *         <td>complet mais lent (3000 ms) : seul à savoir faire {@code cesar_toutes_cles}.
 *         Differentes capacités pour tester l'appel d'offres (d4).</td></tr>
 *     <tr><td>{@code analyste}</td><td>{@link AgentAnalyste}</td><td>fichier {@code journal_connexions.csv} (d3)</td></tr>
 *     <tr><td>{@code archiviste1} à {@code archiviste3}</td><td>{@link AgentArchiviste}</td>
 *         <td>les 3 fragments du flag de d5</td></tr>
 *     <tr><td>{@code auto_base64}, {@code auto_hex}, {@code auto_rot13}, {@code auto_atbash},
 *         {@code auto_inverse}, {@code auto_morse}</td><td>{@link AgentDecodeurAuto}</td>
 *         <td>un décodeur automatique par méthode (d7)</td></tr>
 *     <tr><td>{@code auto_xor}</td><td>{@link AgentDecodeurAuto}</td>
 *         <td>le menteur (d7), sauf si {@code -Dctf.menteur=false}</td></tr>
 * </table>
 * {@link AgentTesteur} compte sur ces noms et ces réglages : si on les modifie, il faut adapter son scénario.
 *
 * <h2>Modes</h2>
 * Le premier mot des arguments choisit le mode :
 * <ul>
 *     <li>{@code test} : l'agent {@code testeur}, <b>sans LLM</b> (scénario écrit à la main, trace en console) ;</li>
 *     <li>autre chose : un seul {@code capitaine} LLM, avec sa fenêtre de dialogue.</li>
 * </ul>
 *
 * <h2>Options (propriétés système)</h2>
 * <ul>
 *     <li>{@code -Djade.gui=false} : sans l'interface graphique de JADE (RMA) ;</li>
 *     <li>{@code -Dctf.menteur=false} : sans le décodeur menteur {@code auto_xor} ;</li>
 *     <li>{@code -Dctf.bonus=true} : ajoute le défi piégé d6 (voir {@link AgentArbitre}) ;</li>
 *     <li>{@code -Dctf.prudent=false} : retire la consigne de sécurité du prompt des capitaines (voir {@link AgentCapitaine}) ;</li>
 *     <li>{@code -Dctf.etapes=N} : nombre maximal d'étapes de la boucle agentique des capitaines ;</li>
 *     <li>{@code -Dctf.quitter=true} : en mode {@code test}, arrête la JVM à la fin du scénario ;</li>
 *     <li>{@code -Dctf.dossier=...} : dossier des données du CTF ;</li>
 *     <li>{@code -Dollama.modele=...} : modèle par défaut du client LLM. Il ne sert que si aucun modèle n'est
 *         imposé et qu'Ollama est injoignable ; sinon, le capitaine choisit parmi les modèles installés.</li>
 * </ul>
 * Le répertoire de travail doit être la racine du projet, car les données sont cherchées dans {@code ollama/ctfAgents}.
 * Sinon, il faut indiquer {@code -Dctf.dossier=chemin/vers/ollama/ctfAgents}.
 */
public class LanceurCTF {
    /**
     * Crée le conteneur principal et lance tous les agents du CTF.
     * @param args mode ({@code test}, {@code duel} ou autre) suivi des modèles en mode duel ;
     *             actuellement ignorés, voir la documentation de la classe
     * @throws StaleProxyException si un agent ne peut pas être créé
     */
    public static void main(String... args) throws StaleProxyException {
        String mode = args.length > 0 ? args[0] : "llm";
        var pp = new ExtendedProperties();
        pp.setProperty(Profile.GUI, System.getProperty("jade.gui", "true"));
        AgentContainer c = Runtime.instance().createMainContainer(new ProfileImpl(pp));

        c.createNewAgent("arbitre", AgentArbitre.class.getName(), null).start();
        // decodeur1 rapide mais incomplet, decodeur2 complet mais lent : intérêt de l'appel d'offres
        c.createNewAgent("decodeur1", AgentDecodeur.class.getName(), new Object[]{"base64,hex,rot13,cesar", "200"}).start();
        c.createNewAgent("decodeur2", AgentDecodeur.class.getName(),
                new Object[]{"base64,hex,rot13,cesar,cesar_toutes_cles", "3000"}).start();
        c.createNewAgent("analyste", AgentAnalyste.class.getName(), new Object[]{"journal_connexions.csv"}).start();
        c.createNewAgent("archiviste1", AgentArchiviste.class.getName(), new Object[]{"1", "CTF{les_"}).start();
        c.createNewAgent("archiviste2", AgentArchiviste.class.getName(), new Object[]{"2", "agents_"}).start();
        c.createNewAgent("archiviste3", AgentArchiviste.class.getName(), new Object[]{"3", "cooperent}"}).start();
        // défi d7 : un décodeur automatique par méthode ; "auto_xor" est le menteur (désactivable : -Dctf.menteur=false)
        for (String m : new String[]{"base64", "hex", "rot13", "atbash", "inverse", "morse"})
            c.createNewAgent("auto_" + m, AgentDecodeurAuto.class.getName(), new Object[]{m}).start();
        if (!"false".equals(System.getProperty("ctf.menteur")))
            c.createNewAgent("auto_xor", AgentDecodeurAuto.class.getName(), new Object[]{"menteur"}).start();

        //un mode duel pourra être ajouté en fonction de l'avancée du TP.
        switch (mode) {
            case "test" -> c.createNewAgent("testeur", AgentTesteur.class.getName(), null).start();
            default -> c.createNewAgent("capitaine", AgentCapitaine.class.getName(), null).start();
        }
    }
}
