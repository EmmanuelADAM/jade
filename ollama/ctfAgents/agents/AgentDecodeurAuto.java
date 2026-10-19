package ollama.ctfAgents.agents;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.WakerBehaviour;
import jade.lang.acl.ACLMessage;
import ollama.ctfAgents.commun.Ctf;
import org.json.JSONArray;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Décodeur AUTOMATIQUE (défi {@code d7}) : agent "pur" (sans LLM) qui connaît <b>une seule</b> méthode
 * de décodage et répond aux appels d'offres « auto ».
 * <p>
 * Contrairement à {@link AgentDecodeur}, on ne lui dit pas quelle opération faire : on lui soumet un texte
 * dont le codage est inconnu, et il propose ce que donne sa méthode. C'est ensuite au demandeur
 * (le LLM du capitaine) de juger quel candidat a du sens. Au démarrage, l'agent s'inscrit au DF
 * sous le type {@link Ctf#DECODEUR_AUTO} en publiant sa méthode.
 * <p>
 * <b>Argument de lancement</b> (obligatoire) : {@code [0]} la méthode ({@code base64}, {@code hex},
 * {@code rot13}, {@code atbash}, {@code inverse}, {@code morse}) ou {@code menteur}.
 *
 * <h2>Protocole (Contract Net)</h2>
 * <pre>
 *  CFP     {"operation":"auto","donnees":"...","deja":[...]}        (deja = agents déjà dans la chaîne de relais)
 *  PROPOSE {"methode":"hex","candidats":[{"chaine":["hex","base64"],"par":["auto_hex","auto_b64"],
 *                                         "texte":"...","lisibilite":0.97}, ...]}
 *  REFUSE  {"raison":"..."}                                         (méthode inapplicable ou résultat illisible)
 *  ACCEPT_PROPOSAL / REJECT_PROPOSAL : décision du capitaine (en fait, du LLM)
 * </pre>
 * Un candidat décrit une chaîne de décodages : les méthodes appliquées dans l'ordre ({@code chaine}),
 * les agents qui les ont appliquées ({@code par}), le texte obtenu et sa lisibilité. Le premier candidat
 * est toujours le résultat propre du décodeur. Le résultat étant déjà dans la proposition, un
 * {@code ACCEPT_PROPOSAL} est seulement journalisé ; un {@code REJECT_PROPOSAL} est ignoré.
 * <p>
 * Le décodeur refuse ({@code REFUSE}) si sa méthode ne s'applique pas, si elle ne change pas le texte,
 * ou si la lisibilité du résultat est inférieure à 0,6 : cela évite d'inonder
 * le demandeur de bruit.
 *
 * <h2>Les décodeurs se parlent entre eux (relais)</h2>
 * Si son résultat semble <b>encore</b> codé (base64, hexadécimal ou morse), le décodeur lance lui-même
 * un appel d'offres auprès de ses collègues, sauf ceux déjà dans la chaîne. Il attend leurs propositions
 * <b>sans se bloquer</b> : il continue à répondre aux autres demandes. Il propose ensuite au demandeur son
 * propre résultat, suivi des chaînes complètes obtenues.
 * <ul>
 *     <li>La profondeur est limitée à 2 relais ({@code PROFONDEUR_MAX}).</li>
 *     <li>L'attente s'arrête quand tous les collègues ont répondu, ou au bout d'un délai
 *         (3 s pour le premier décodeur, 1,5 s au relais suivant) : les réponses tardives sont ignorées.</li>
 * </ul>
 *
 * <h2>Le menteur</h2>
 * L'agent lancé avec {@code menteur} joue une source compromise. Il s'annonce au DF avec une méthode
 * au nom crédible ({@code xor_magique}) et propose toujours, quel que soit le texte, le même faux flag
 * avec une lisibilité de 0,99. Comme il répond aussi aux relais de ses collègues, son faux flag peut
 * apparaître au bout d'une chaîne. La réputation tenue par le capitaine sert à s'en méfier.
 *
 * <h2>Limite volontaire</h2>
 * {@code rot13}, {@code atbash} et {@code inverse} ne s'appliquent qu'à des phrases (texte avec espaces).
 * Sur du base64 ou de l'hexadécimal, elles produiraient du bruit « lisible ». C'est un choix de conception,
 * à discuter avec les élèves : un message rot13 sans espace leur échappera.
 */
public class AgentDecodeurAuto extends Agent {
    /** profondeur max de la chaîne de relais */
    private static final int PROFONDEUR_MAX = 2;
    /** en dessous de ce score de lisibilité, le décodeur refuse (évite d'inonder le demandeur de bruit) */
    private static final double LISIBILITE_MIN = 0.6;

    /** méthode de décodage de cet agent (ou "menteur") */
    private String methode;
    /** vrai si l'agent joue la source compromise */
    private boolean menteur;

    /** un relais en cours : la demande d'origine, notre propre candidat, les réponses des collègues */
    private static class Relais {
        ACLMessage origine;
        JSONObject propre;
        final List<ACLMessage> reponses = new ArrayList<>();
        int attendus;
    }

    /** conversation (de notre propre CFP aux collègues) -> relais */
    private final Map<String, Relais> relais = new HashMap<>();

    @Override
    protected void setup() {
        methode = String.valueOf(getArguments()[0]);
        menteur = "menteur".equals(methode);
        // le menteur ne se présente pas comme tel !
        Ctf.inscrire(this, Ctf.DECODEUR_AUTO, Map.of("methode", menteur ? "xor_magique" : methode));
        Ctf.log(this, "prêt (décodeur automatique, méthode " + (menteur ? "xor_magique" : methode) + ")");

        addBehaviour(new CyclicBehaviour(this) {
            @Override
            public void action() {
                ACLMessage msg = receive();
                if (msg == null) {
                    block();
                    return;
                }
                Relais r = relais.get(msg.getConversationId());
                if (r != null) { // réponse d'un collègue à NOTRE appel d'offres
                    r.reponses.add(msg);
                    if (r.reponses.size() >= r.attendus) terminer(msg.getConversationId());
                    return;
                }
                switch (msg.getPerformative()) {
                    case ACLMessage.CFP -> traiterCfp(msg);
                    case ACLMessage.ACCEPT_PROPOSAL -> Ctf.log(myAgent, "ma proposition est retenue par " + msg.getSender().getLocalName());
                    default -> { /* REJECT, ou réponse tardive d'un collègue : rien à faire */ }
                }
            }
        });
    }

    /**
     * Traite un appel d'offres : applique la méthode, puis soit refuse, soit propose directement son résultat,
     * soit (si le résultat semble encore codé) lance un relais auprès des collègues. Dans ce dernier cas,
     * la réponse au demandeur est envoyée plus tard par {@link #terminer(String)}.
     * @param cfp l'appel d'offres reçu
     */
    private void traiterCfp(ACLMessage cfp) {
        JSONObject c = Ctf.contenu(cfp);
        String donnees = c.optString("donnees").trim();
        //TODO:completer
    }

    /**
     * Fin d'un relais (toutes les réponses reçues, ou délai écoulé) : on répond au demandeur d'origine.
     * Chaque candidat proposé par un collègue est prolongé en tête par notre méthode et notre nom.
     * Sans effet si le relais est déjà terminé.
     * @param conv identifiant de la conversation du relais
     */
    private void terminer(String conv) {
        Relais r = relais.remove(conv);
        if (r == null) return; // déjà terminé
        List<JSONObject> chaines = new ArrayList<>();
        for (ACLMessage rep : r.reponses) {
            if (rep.getPerformative() != ACLMessage.PROPOSE) continue;
            JSONArray cands = Ctf.contenu(rep).optJSONArray("candidats", new JSONArray());
            for (int i = 0; i < cands.length(); i++) {
                JSONObject sc = cands.getJSONObject(i);
                List<String> chaine = new ArrayList<>(List.of(methode));
                sc.getJSONArray("chaine").toList().forEach(o -> chaine.add(o.toString()));
                List<String> par = new ArrayList<>(List.of(getLocalName()));
                sc.getJSONArray("par").toList().forEach(o -> par.add(o.toString()));
                chaines.add(candidat(chaine, par, sc.getString("texte"), sc.getDouble("lisibilite")));
            }
        }
        proposer(r.origine, r.propre, chaines);
    }

    /**
     * Envoie le {@code PROPOSE} au demandeur : notre propre candidat en premier, puis les chaînes des collègues.
     * @param cfp l'appel d'offres auquel on répond
     * @param propre notre candidat
     * @param chaines les candidats obtenus par relais (éventuellement aucun)
     */
    private void proposer(ACLMessage cfp, JSONObject propre, List<JSONObject> chaines) {
        var cands = new JSONArray().put(propre);
        chaines.forEach(cands::put);
        send(Ctf.repondre(cfp, ACLMessage.PROPOSE, new JSONObject().put("methode", methode).put("candidats", cands)));
    }

    /**
     * Construit un candidat JSON.
     * @param chaine méthodes appliquées, dans l'ordre
     * @param par agents qui les ont appliquées, dans le même ordre
     * @param texte texte obtenu
     * @param lisibilite score de lisibilité (arrondi à 2 décimales)
     */
    private static JSONObject candidat(List<String> chaine, List<String> par, String texte, double lisibilite) {
        return new JSONObject().put("chaine", new JSONArray(chaine)).put("par", new JSONArray(par))
                .put("texte", texte).put("lisibilite", arrondi(lisibilite));
    }

    private static double arrondi(double x) {
        return Math.round(x * 100) / 100.0;
    }

    // ------------------------------------------------------------------ les méthodes (statiques, testables seules)

    /** les méthodes qui travaillent lettre à lettre sur une PHRASE (sinon, sur du base64 ou de l'hexa, elles produisent du bruit lisible) */
    private static final Set<String> SUR_PHRASES = Set.of("rot13", "atbash", "inverse");

    /**
     * Applique une méthode de décodage (méthode statique : testable sans JADE).
     * @param methode {@code base64}, {@code hex}, {@code rot13}, {@code atbash}, {@code inverse} ou {@code morse}
     * @param d texte à décoder
     * @return le texte décodé
     * @throws IllegalArgumentException si la méthode est inconnue ou ne s'applique pas à ce texte
     *         (pas une phrase pour rot13/atbash/inverse, base64, hexadécimal ou morse invalide)
     */
    public static String appliquer(String methode, String d) {
        if (SUR_PHRASES.contains(methode) && !d.contains(" "))
            throw new IllegalArgumentException("je ne traite que des phrases (texte avec des espaces)");
        return switch (methode) {
            case "base64" -> {
                if (!d.matches("[A-Za-z0-9+/]+=*") || d.length() % 4 != 0) throw new IllegalArgumentException("pas du base64");
                yield new String(Base64.getDecoder().decode(d), StandardCharsets.UTF_8);
            }
            case "hex" -> AgentDecodeur.hex(d);
            case "rot13" -> AgentDecodeur.decaler(d, 13);
            case "atbash" -> atbash(d);
            case "inverse" -> new StringBuilder(d).reverse().toString();
            case "morse" -> morse(d);
            default -> throw new IllegalArgumentException("méthode inconnue " + methode);
        };
    }

    /** Atbash : alphabet renversé (a↔z, b↔y...), casse conservée ; les autres caractères sont gardés. */
    static String atbash(String s) {
        var sb = new StringBuilder();
        for (char c : s.toCharArray()) {
            if (c >= 'a' && c <= 'z') sb.append((char) ('z' - (c - 'a')));
            else if (c >= 'A' && c <= 'Z') sb.append((char) ('Z' - (c - 'A')));
            else sb.append(c);
        }
        return sb.toString();
    }

    /** code morse -> lettre minuscule ou chiffre */
    private static final Map<String, Character> MORSE = new HashMap<>();

    static {
        String[] codes = {".-", "-...", "-.-.", "-..", ".", "..-.", "--.", "....", "..", ".---", "-.-", ".-..", "--",
                "-.", "---", ".--.", "--.-", ".-.", "...", "-", "..-", "...-", ".--", "-..-", "-.--", "--.."};
        for (int i = 0; i < 26; i++) MORSE.put(codes[i], (char) ('a' + i));
        String[] chiffres = {"-----", ".----", "..---", "...--", "....-", ".....", "-....", "--...", "---..", "----."};
        for (int i = 0; i < 10; i++) MORSE.put(chiffres[i], (char) ('0' + i));
    }

    /**
     * Décode du morse : codes séparés par des espaces, mots séparés par {@code /}.
     * Seuls les lettres et les chiffres sont reconnus ; le résultat est en minuscules.
     * @throws IllegalArgumentException si le texte contient autre chose que {@code . - /} et des espaces,
     *         ou un code inconnu
     */
    static String morse(String d) {
        if (!d.matches("[.\\-/ ]+")) throw new IllegalArgumentException("pas du morse");
        var sb = new StringBuilder();
        for (String mot : d.trim().split("\\s*/\\s*")) {
            for (String code : mot.trim().split("\\s+")) {
                Character c = MORSE.get(code);
                if (c == null) throw new IllegalArgumentException("code morse inconnu " + code);
                sb.append(c);
            }
            sb.append(' ');
        }
        return sb.toString().trim();
    }

    /**
     * Part des caractères « de texte » (lettres, chiffres, espaces, ponctuation courante), entre 0 et 1.
     * Ce score mesure seulement l'allure du texte, pas son sens : un texte encore codé en base64 obtient 1.
     */
    static double lisibilite(String t) {
        if (t.isEmpty()) return 0;
        int ok = 0;
        for (char c : t.toCharArray())
            if (Character.isLetterOrDigit(c) || " .,;:'!?-_{}()".indexOf(c) >= 0) ok++;
        return (double) ok / t.length();
    }

    /**
     * Le texte ressemble-t-il encore à un codage connu ? Vrai pour du morse, ou pour un mot sans espace
     * qui a l'allure d'hexadécimal ou de base64. Les textes de moins de 8 caractères ne sont jamais considérés comme codés.
     */
    static boolean sembleCode(String t) {
        String s = t.trim();
        if (s.length() < 8) return false;
        if (s.matches("[.\\-/ ]+")) return true;                                   // morse
        if (s.contains(" ")) return false;
        return s.matches("[0-9a-fA-F]+") || (s.matches("[A-Za-z0-9+/]+=*") && s.length() % 4 == 0); // hex, base64
    }

    @Override
    protected void takeDown() {
        Ctf.desinscrire(this);
    }
}
