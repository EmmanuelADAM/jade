package ollama.ctfAgents.agents;

import jade.core.Agent;
import jade.core.behaviours.WakerBehaviour;
import ollama.ctfAgents.actions.*;
import ollama.toolAgent.outils.BoiteAOutils;
import ollama.toolAgent.outils.Outil;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Agent TESTEUR, <b>sans LLM</b> : il joue le rôle du capitaine en suivant un scénario écrit à la main.
 * <p>
 * Il utilise <b>exactement</b> les mêmes actions que {@link AgentCapitaine}, rangées dans la même
 * {@link BoiteAOutils}. La seule différence est que les décisions prises par le LLM chez le capitaine
 * (quelle action lancer, quel texte « est du français ») sont ici codées en dur.
 *
 * <h2>Intérêt pédagogique</h2>
 * <ul>
 *     <li>tester les agents purs et les protocoles <b>avant</b> d'y brancher un LLM (partie A du sujet) ;</li>
 *     <li>obtenir la « trace idéale » d'une résolution complète, à comparer avec celle du LLM.</li>
 * </ul>
 *
 * <h2>Scénario</h2>
 * Après 1,5 s (le temps que les autres agents s'inscrivent au DF), le testeur enchaîne :
 * <ol>
 *     <li><b>d0</b> : vérifie que les décodeurs, l'analyste, les archivistes et l'arbitre sont au DF ;</li>
 *     <li><b>d1</b> : décodage base64 ;</li>
 *     <li><b>d2</b> : hexadécimal puis rot13, et vérification qu'un faux flag est refusé ;</li>
 *     <li><b>d3</b> : filtres successifs chez l'analyste, puis IP la plus fréquente ;</li>
 *     <li><b>d4</b> : appel d'offres pour {@code cesar_toutes_cles}, qui doit choisir {@code decodeur2},
 *         puis sélection de la phrase en français ;</li>
 *     <li><b>d5</b> : une preuve inventée est refusée, puis les 3 fragments sont obtenus avec les preuves
 *         de d1, d2 et d3. La réutilisation d'une preuve auprès d'un autre archiviste doit être refusée ;</li>
 *     <li><b>d7</b> : codage inconnu, avec piège du menteur (voir {@code scenarioD7()}) ;</li>
 *     <li><b>erreurs de protocole</b> : opération inconnue ({@code REFUSE}), données invalides ({@code FAILURE}),
 *         agent inexistant.</li>
 * </ol>
 * Chaque échange est affiché sur la console. Chaque résultat inattendu est signalé comme une
 * « ANOMALIE », et leur nombre est affiché à la fin avec les statistiques des échanges.
 * <p>
 * Le scénario suppose la configuration du lanceur : agents nommés {@code decodeur1}, {@code decodeur2},
 * {@code analyste}, {@code archiviste1} à {@code archiviste3}, dont seul {@code decodeur2} sait faire
 * {@code cesar_toutes_cles}, avec les données de {@code defis.json} et {@code journal_connexions.csv}.
 *
 * <h2>Option</h2>
 * Avec {@code -Dctf.quitter=true}, la JVM est arrêtée à la fin du scénario, avec le code de sortie 0
 * s'il n'y a aucune anomalie et 1 sinon. Le testeur peut ainsi servir de test automatique.
 */
public class AgentTesteur extends Agent {
    /** les mêmes actions que le capitaine */
    private BoiteAOutils actions;
    /** nombre d'anomalies constatées pendant le scénario */
    private int erreurs = 0;

    @Override
    protected void setup() {
        var reputation = new Reputation();
        var proposer = new ProposerFlag(this);
        proposer.setReputation(reputation);
        var consulter = new ConsulterTous(this, reputation);
        actions = new BoiteAOutils()
                .ajouter((Outil) new ChercherAgents(this))
                .ajouter((Outil) new Demander(this))
                .ajouter((Outil) new AppelOffres(this))
                .ajouter((Outil) consulter)
                .ajouter((Outil) new Retenir(this, consulter, reputation))
                .ajouter((Outil) proposer);
        addBehaviour(new WakerBehaviour(this, 1500) {
            @Override
            protected void onWake() {
                scenario();
                System.out.println("\n===== testeur : fin du scénario, " + ActionAgent.statistiques() + ", " + erreurs + " anomalie(s) =====");
                if (Boolean.getBoolean("ctf.quitter")) System.exit(erreurs == 0 ? 0 : 1);
            }
        });
    }

    /**
     * Appelle une action et affiche l'échange, comme dans la trace du capitaine.
     * @param action nom de l'action (ex. {@code demander})
     * @param args arguments de l'action
     * @return la réponse de l'action
     */
    private JSONObject appel(String action, JSONObject args) {
        JSONObject res = actions.executer(action, args);
        System.out.println("testeur : " + action + " " + args + "\n    <- " + res);
        return res;
    }

    /**
     * Vérifie une condition ; si elle est fausse, compte et affiche une anomalie.
     * @param condition ce qui doit être vrai
     * @param quoi description de la vérification
     */
    private void verifier(boolean condition, String quoi) {
        if (!condition) {
            erreurs++;
            System.out.println("    !!! ANOMALIE : " + quoi);
        }
    }

    /**
     * Raccourci pour écrire les arguments : {@code a("cle1", valeur1, "cle2", valeur2, ...)}.
     * @param cv alternance de clés (String) et de valeurs
     * @return l'objet JSON correspondant
     */
    private static JSONObject a(Object... cv) {
        var o = new JSONObject();
        for (int i = 0; i < cv.length; i += 2) o.put((String) cv[i], cv[i + 1]);
        return o;
    }

    /** @return le champ {@code resultat} du contenu de la réponse d'un décodeur, obtenue par {@code demander} */
    private String resultat(JSONObject r) {
        return r.getJSONObject("resultat").getJSONObject("contenu").get("resultat").toString();
    }

    /** @return vrai si la réponse de {@code proposer_flag} indique que l'arbitre a accepté le flag */
    private boolean accepte(JSONObject r) {
        return r.optBoolean("ok") && r.getJSONObject("resultat").optBoolean("accepte");
    }

    /**
     * Aplatit le résultat de {@code consulter_tous} : tous les candidats de toutes les propositions.
     * @param consultation la réponse de l'action {@code consulter_tous}
     * @return pour chaque candidat : {@code [agent qui propose, texte, chaîne de méthodes en JSON]}
     */
    private java.util.List<String[]> candidats(JSONObject consultation) {
        var res = new java.util.ArrayList<String[]>();
        JSONArray props = consultation.getJSONObject("resultat").getJSONArray("propositions");
        for (int i = 0; i < props.length(); i++) {
            JSONObject p = props.getJSONObject(i);
            JSONArray cs = p.getJSONArray("candidats");
            for (int j = 0; j < cs.length(); j++)
                res.add(new String[]{p.getString("agent"), cs.getJSONObject(j).getString("texte"), cs.getJSONObject(j).getJSONArray("chaine").toString()});
        }
        return res;
    }

    /**
     * Défi d7 : codage inconnu, résolu en consultant tous les décodeurs automatiques.
     * <p>
     * Le « choix du sens » est fait ici par une règle écrite à la main ; dans le capitaine, c'est le rôle du LLM.
     * <ol>
     *     <li>Tour 1 : {@code consulter_tous} sur les données. Si le menteur a proposé un flag, on le retient
     *         <b>exprès</b>, pour vérifier que l'arbitre le refuse et que la réputation du menteur baisse.</li>
     *     <li>On retient la chaîne {@code hex -> base64}, produite par les relais entre décodeurs.
     *         Elle n'est pas encore en français (c'est de l'atbash).</li>
     *     <li>Tour 2 : {@code consulter_tous} sur ce texte. On retient le candidat en français
     *         (qui commence par « bravo ») et on propose le flag qu'il contient.</li>
     * </ol>
     */
    private void scenarioD7() {
        String donnees = "65576c365a5777734947393249485676656e5167646d686e4946684856587476656c393162475a76646c3933646d6866656e52326257646f58337066615870796147787466513d3d";
        // tour 1
        var c1 = candidats(appel("consulter_tous", a("donnees", donnees)));
        System.out.println("    -> " + c1.size() + " candidats au tour 1");
        String[] piege = c1.stream().filter(c -> c[1].startsWith("CTF{")).findFirst().orElse(null);
        if (piege != null) { // le menteur a répondu : on tombe dans le piège exprès
            appel("retenir", a("agent", piege[0], "texte", piege[1]));
            JSONObject r = appel("proposer_flag", a("defi", "d7", "flag", piege[1]));
            verifier(!accepte(r), "le flag du menteur doit être refusé");
            verifier(r.getJSONObject("resultat").has("reputation_mise_a_jour"), "la réputation du menteur doit baisser");
        }
        // la chaîne hex -> base64 produite par les relais entre décodeurs (pas encore du français : de l'atbash)
        String[] etape = c1.stream().filter(c -> c[2].equals("[\"hex\",\"base64\"]")).findFirst().orElse(null);
        verifier(etape != null, "chaîne hex -> base64 attendue (relais entre décodeurs)");
        if (etape == null) return;
        appel("retenir", a("agent", etape[0], "texte", etape[1]));
        // tour 2 sur le candidat retenu
        var c2 = candidats(appel("consulter_tous", a("donnees", etape[1])));
        String[] clair = c2.stream().filter(c -> c[1].startsWith("bravo")).findFirst().orElse(null);
        verifier(clair != null, "candidat en français attendu au tour 2");
        if (clair == null) return;
        appel("retenir", a("agent", clair[0], "texte", clair[1]));
        String flag = clair[1].substring(clair[1].indexOf("CTF{"));
        JSONObject r = appel("proposer_flag", a("defi", "d7", "flag", flag));
        verifier(accepte(r), "flag d7");
    }

    /** Le scénario complet, décrit dans la documentation de la classe. */
    private void scenario() {
        System.out.println("\n===== d0 : pages jaunes =====");
        for (String s : new String[]{"decodeur", "analyste", "archiviste", "arbitre"})
            verifier(!appel("chercher_agents", a("service", s)).getJSONObject("resultat").getJSONArray("agents").isEmpty(), "service " + s);

        System.out.println("\n===== d1 : base64 =====");
        String f1 = resultat(appel("demander", a("agent", "decodeur1", "operation", "base64", "donnees", "Q1RGe2IwbmpvdXJfbGVzX2FnZW50c30=")));
        verifier(accepte(appel("proposer_flag", a("defi", "d1", "flag", f1))), "flag d1");

        System.out.println("\n===== d2 : hex puis rot13 (enchaînement) =====");
        String h = resultat(appel("demander", a("agent", "decodeur1", "operation", "hex",
                "donnees", "5047537b7172686b5f706268707572665f696e797261675f7a7672686b7d")));
        String f2 = resultat(appel("demander", a("agent", "decodeur1", "operation", "rot13", "donnees", h)));
        verifier(!accepte(appel("proposer_flag", a("defi", "d2", "flag", "CTF{essai_faux}"))), "un flag faux doit être refusé");
        verifier(accepte(appel("proposer_flag", a("defi", "d2", "flag", f2))), "flag d2");

        System.out.println("\n===== d3 : analyste =====");
        appel("demander", a("agent", "analyste", "operation", "filtrer", "colonne", "resultat", "valeur", "ECHEC"));
        appel("demander", a("agent", "analyste", "operation", "filtrer", "colonne", "horodatage", "contient", "T02:"));
        JSONObject top = appel("demander", a("agent", "analyste", "operation", "compter_par", "colonne", "ip", "top", 3));
        String ip = top.getJSONObject("resultat").getJSONObject("contenu").getJSONArray("classement").getJSONObject(0).getString("valeur");
        verifier(accepte(appel("proposer_flag", a("defi", "d3", "flag", "CTF{" + ip + "}"))), "flag d3");

        System.out.println("\n===== d4 : appel d'offres (seul decodeur2 sait faire) =====");
        JSONObject offres = appel("appel_offres", a("service", "decodeur", "operation", "cesar_toutes_cles",
                "donnees", "wp oclaplf pde npdlc ltxp wpd lrpyed"));
        verifier("decodeur2".equals(offres.getJSONObject("resultat").optString("choisi")), "decodeur2 doit être choisi");
        JSONArray cles = offres.getJSONObject("resultat").getJSONObject("contenu").getJSONArray("resultat");
        String clair = "";
        for (int i = 0; i < cles.length(); i++) // ici, le « choix du français » est fait à la main : c'est le rôle du LLM
            if (cles.getJSONObject(i).getString("texte").startsWith("le ")) clair = cles.getJSONObject(i).getString("texte");
        String f4 = "CTF{" + clair.substring(clair.indexOf(" est ") + 5).replace(' ', '_') + "}";
        verifier(accepte(appel("proposer_flag", a("defi", "d4", "flag", f4))), "flag d4");

        System.out.println("\n===== d5 : archivistes =====");
        verifier(!appel("demander", a("agent", "archiviste1", "demande", "fragment",
                "preuve", a("defi", "d1", "flag", "CTF{invente}"))).optBoolean("ok"), "preuve fausse refusée");
        String[] fragments = new String[3];
        String[][] preuves = {{"d1", f1}, {"d2", f2}, {"d3", "CTF{" + ip + "}"}};
        for (int i = 0; i < 3; i++) {
            JSONObject r = appel("demander", a("agent", "archiviste" + (i + 1), "demande", "fragment",
                    "preuve", a("defi", preuves[i][0], "flag", preuves[i][1])));
            JSONObject c = r.getJSONObject("resultat").getJSONObject("contenu");
            fragments[c.getInt("numero") - 1] = c.getString("fragment");
        }
        verifier(!appel("demander", a("agent", "archiviste2", "demande", "fragment",
                "preuve", a("defi", "d1", "flag", f1))).optBoolean("ok"), "preuve d1 déjà utilisée chez archiviste1");
        verifier(accepte(appel("proposer_flag", a("defi", "d5", "flag", String.join("", fragments)))), "flag d5");

        System.out.println("\n===== d7 : codage inconnu, tous les décodeurs automatiques proposent =====");
        scenarioD7();

        System.out.println("\n===== erreurs de protocole =====");
        verifier(!appel("demander", a("agent", "decodeur1", "operation", "cesar_toutes_cles", "donnees", "abc")).optBoolean("ok"), "REFUSE attendu");
        verifier(!appel("demander", a("agent", "decodeur1", "operation", "base64", "donnees", "pas du base64 !")).optBoolean("ok"), "FAILURE attendu");
        verifier(!appel("demander", a("agent", "fantome", "operation", "base64", "donnees", "QQ==")).optBoolean("ok"), "agent inexistant");
    }
}
