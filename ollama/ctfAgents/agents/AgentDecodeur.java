package ollama.ctfAgents.agents;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import ollama.ctfAgents.commun.Ctf;
import org.json.JSONArray;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Agent DÉCODEUR : agent "pur" (sans LLM) qui applique des décodages simples à un texte.
 * <p>
 * Chaque décodeur ne connaît qu'une partie des opérations et peut être plus ou moins lent : il faut
 * donc choisir le bon agent, soit directement ({@code REQUEST}), soit par appel d'offres ({@code CFP}).
 * Au démarrage, il s'inscrit au DF sous le type {@link Ctf#DECODEUR} en publiant ses opérations et son délai.
 * <p>
 * <b>Arguments de lancement</b> (facultatifs) :
 * <ul>
 *     <li>{@code [0]} opérations connues, séparées par des virgules (par défaut, toutes : {@link #TOUTES}) ;</li>
 *     <li>{@code [1]} délai de traitement simulé, en ms (0 par défaut).</li>
 * </ul>
 *
 * <h2>Opérations</h2>
 * <table border="1">
 *     <caption>Opérations de décodage</caption>
 *     <tr><th>operation</th><th>paramètres</th><th>résultat</th></tr>
 *     <tr><td>{@code base64}</td><td>aucun</td><td>texte décodé (UTF-8)</td></tr>
 *     <tr><td>{@code hex}</td><td>aucun</td><td>texte décodé (UTF-8) ; les espaces sont ignorés</td></tr>
 *     <tr><td>{@code rot13}</td><td>aucun</td><td>texte décalé de 13 lettres</td></tr>
 *     <tr><td>{@code cesar}</td><td>{@code cle} (entier, obligatoire)</td>
 *         <td>texte déchiffré : {@code cle} est le décalage utilisé au chiffrement, chaque lettre recule donc de {@code cle}</td></tr>
 *     <tr><td>{@code cesar_toutes_cles}</td><td>aucun</td>
 *         <td>tableau des 25 déchiffrements possibles : {@code [{"cle":1,"texte":...}, ...]}</td></tr>
 * </table>
 * Les décalages ne touchent que les lettres non accentuées {@code a-z} et {@code A-Z} ; les autres caractères sont conservés.
 *
 * <h2>Protocole</h2>
 * Le contenu est toujours de la forme {@code {"operation":"hex","donnees":"...", ...paramètres}}.
 * <table border="1">
 *     <caption>Messages traités par le décodeur</caption>
 *     <tr><th>message reçu</th><th>réponse</th></tr>
 *     <tr><td>{@code REQUEST}</td>
 *         <td>{@code INFORM {"ok":true,"operation":...,"resultat":...}}, ou {@code FAILURE} si les données sont invalides</td></tr>
 *     <tr><td>{@code CFP}</td><td>{@code PROPOSE {"operation":...,"delai_ms":...}} (le délai sert à comparer les offres)</td></tr>
 *     <tr><td>{@code ACCEPT_PROPOSAL}</td><td>exécution, puis {@code INFORM} ou {@code FAILURE}, comme pour un {@code REQUEST}.
 *         Le contenu doit donc reprendre l'opération et les données.</td></tr>
 *     <tr><td>{@code REJECT_PROPOSAL}</td><td>aucune</td></tr>
 *     <tr><td>autre performatif</td><td>{@code NOT_UNDERSTOOD}</td></tr>
 * </table>
 * Si l'opération n'est pas connue de ce décodeur, un {@code REQUEST}, un {@code CFP} ou un {@code ACCEPT_PROPOSAL}
 * reçoit un {@code REFUSE} qui liste les opérations qu'il sait faire.
 * <p>
 * <b>Attention</b> : le délai simulé est un {@code Thread.sleep} ; pendant ce temps, l'agent est occupé
 * et ne traite aucun autre message.
 */
public class AgentDecodeur extends Agent {
    /** toutes les opérations de décodage existantes */
    public static final List<String> TOUTES = List.of("base64", "hex", "rot13", "cesar", "cesar_toutes_cles");

    /** opérations que ce décodeur sait faire */
    private Set<String> operations = new LinkedHashSet<>(TOUTES);
    /** délai de traitement simulé (ms) avant chaque exécution */
    private long delai = 0;

    @Override
    protected void setup() {
        Object[] args = getArguments();
        if (args != null && args.length > 0) operations = new LinkedHashSet<>(List.of(String.valueOf(args[0]).split("\\s*,\\s*")));
        if (args != null && args.length > 1) delai = Long.parseLong(String.valueOf(args[1]));
        Ctf.inscrire(this, Ctf.DECODEUR, Map.of(
                "operations", String.join(",", operations),
                "delai_ms", String.valueOf(delai)));
        Ctf.log(this, "prêt, opérations " + operations + ", délai " + delai + " ms");

        //TODO: ajouter le cyclic behaviour qui recoit un message et selon request ou accept execute ou refuse l'operation
        //lance un cfp si demande dans le message; revot l'acception finale si c'est indiqué dans le message (accept-proposal)
        addBehaviour(new CyclicBehaviour(this) {
            @Override
            public void action() {
                //TODO votre code ici
            }
        });
    }

    /**
     * Répond {@code REFUSE} à une demande portant sur une opération inconnue de ce décodeur,
     * en indiquant les opérations qu'il sait faire.
     * @param msg le message reçu
     * @param op l'opération demandée
     */
    private void refuser(ACLMessage msg, String op) {
        //TODO: compléter
    }

    /**
     * Attend le délai simulé, exécute le décodage puis répond {@code INFORM} avec le résultat,
     * ou {@code FAILURE} avec le message d'erreur si le décodage échoue.
     * @param msg le message reçu ({@code REQUEST} ou {@code ACCEPT_PROPOSAL})
     * @param tache son contenu : {@code operation}, {@code donnees} et éventuels paramètres
     */
    private void executer(ACLMessage msg, JSONObject tache) {
        String op = tache.getString("operation");
        //TODO:completer
    }

    /**
     * Les décodages eux-mêmes (méthodes statiques : testables sans JADE).
     * @param op nom de l'opération (voir {@link #TOUTES})
     * @param donnees texte à décoder (les espaces en début et fin sont retirés)
     * @param parametres paramètres de l'opération, ex. {@code cle} pour {@code cesar}
     * @return le texte décodé, ou un {@link JSONArray} pour {@code cesar_toutes_cles}
     * @throws IllegalArgumentException si l'opération est inconnue, un paramètre manque ou les données sont invalides
     */
    public static Object decoder(String op, String donnees, JSONObject parametres) {
        String d = donnees.trim();
        return switch (op) {
            case "base64" -> {
                try {
                    yield new String(Base64.getDecoder().decode(d), StandardCharsets.UTF_8);
                } catch (IllegalArgumentException e) {
                    throw new IllegalArgumentException("données non base64 : " + e.getMessage());
                }
            }
            case "hex" -> hex(d);
            case "rot13" -> decaler(d, 13);
            case "cesar" -> {
                if (!parametres.has("cle")) throw new IllegalArgumentException("paramètre 'cle' (entier) obligatoire");
                yield decaler(d, parametres.getInt("cle"));
            }
            case "cesar_toutes_cles" -> {
                var tab = new JSONArray();
                for (int k = 1; k < 26; k++) tab.put(new JSONObject().put("cle", k).put("texte", decaler(d, k)));
                yield tab;
            }
            default -> throw new IllegalArgumentException("opération inconnue : " + op);
        };
    }

    /**
     * Décode une chaîne hexadécimale (espaces ignorés) en texte UTF-8.
     * @throws IllegalArgumentException si la chaîne n'est pas de l'hexadécimal valide (longueur impaire, caractère interdit)
     */
    static String hex(String d) {
        String s = d.replaceAll("\\s", "");
        if (s.length() % 2 != 0 || !s.matches("[0-9a-fA-F]*"))
            throw new IllegalArgumentException("données non hexadécimales");
        byte[] octets = new byte[s.length() / 2];
        for (int i = 0; i < octets.length; i++) octets[i] = (byte) Integer.parseInt(s.substring(2 * i, 2 * i + 2), 16);
        return new String(octets, StandardCharsets.UTF_8);
    }

    /**
     * Déchiffre un décalage de lettres : chaque lettre {@code a-z} / {@code A-Z} recule de {@code cle} positions,
     * en restant dans l'alphabet et en gardant sa casse ; les autres caractères sont conservés.
     * Toute clé entière est acceptée (négative ou supérieure à 25, prise modulo 26).
     */
    static String decaler(String s, int cle) {
        int k = ((cle % 26) + 26) % 26;
        var sb = new StringBuilder();
        for (char c : s.toCharArray()) {
            if (c >= 'a' && c <= 'z') sb.append((char) ('a' + (c - 'a' - k + 26) % 26));
            else if (c >= 'A' && c <= 'Z') sb.append((char) ('A' + (c - 'A' - k + 26) % 26));
            else sb.append(c);
        }
        return sb.toString();
    }

    @Override
    protected void takeDown() {
        Ctf.desinscrire(this);
    }
}
