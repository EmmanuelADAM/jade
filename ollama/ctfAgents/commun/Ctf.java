package ollama.ctfAgents.commun;

import jade.core.AID;
import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.Property;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import org.json.JSONException;
import org.json.JSONObject;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Constantes et petites méthodes partagées par tous les agents du CTF (classe utilitaire, non instanciable).
 *
 * <h2>Pages jaunes</h2>
 * Tous les agents du CTF s'inscrivent au DF avec le type {@value #TYPE} et un nom de service :
 * {@value #ARBITRE}, {@value #DECODEUR}, {@value #DECODEUR_AUTO}, {@value #ANALYSTE} ou {@value #ARCHIVISTE}.
 * Leurs capacités sont décrites dans les propriétés de la {@link ServiceDescription}
 * (ex. {@code operations = "base64,hex,rot13"}). C'est ce qui permet au capitaine de découvrir les agents
 * et ce qu'ils savent faire, sans rien connaître d'eux à l'avance.
 * <ul>
 *     <li>{@link #inscrire} / {@link #desinscrire} : inscription au démarrage, désinscription dans {@code takeDown} ;</li>
 *     <li>{@link #chercher} / {@link #chercherAID} : recherche des agents qui proposent un service ;</li>
 *     <li>{@link #proprietes} : lecture des capacités d'une fiche du DF.</li>
 * </ul>
 *
 * <h2>Messages</h2>
 * Tous les contenus de messages sont des objets JSON, et le langage du message vaut {@value #LANGAGE}.
 * <ul>
 *     <li>{@link #message} : nouveau message ;</li>
 *     <li>{@link #repondre} : réponse à un message reçu (même conversation) ;</li>
 *     <li>{@link #contenu} : lecture du contenu JSON d'un message reçu.</li>
 * </ul>
 *
 * <h2>Divers</h2>
 * {@link #dossier()} donne le dossier des données du CTF, et {@link #log} un affichage console homogène.
 */
public final class Ctf {
    /** type de service commun à tous les agents du CTF */
    public static final String TYPE = "ctf";
    /** nom de service de l'arbitre ({@code AgentArbitre}) */
    public static final String ARBITRE = "arbitre";
    /** nom de service des décodeurs à opérations explicites ({@code AgentDecodeur}) */
    public static final String DECODEUR = "decodeur";
    /** nom de service de l'analyste de fichiers CSV ({@code AgentAnalyste}) */
    public static final String ANALYSTE = "analyste";
    /** nom de service des archivistes, détenteurs des fragments du flag d5 ({@code AgentArchiviste}) */
    public static final String ARCHIVISTE = "archiviste";
    /** nom de service des décodeurs automatiques du défi d7 : une méthode chacun, appels d'offres « auto » ({@code AgentDecodeurAuto}) */
    public static final String DECODEUR_AUTO = "decodeur_auto";
    /** langage des contenus de messages */
    public static final String LANGAGE = "JSON";

    private Ctf() {
    }

    /**
     * Dossier des données du CTF ({@code defis.json}, {@code journal_connexions.csv}).
     * Réglable par {@code -Dctf.dossier=...} ; par défaut {@code ollama/ctfAgents}, relatif au répertoire de lancement.
     * @return le chemin du dossier
     */
    public static Path dossier() {
        return Path.of(System.getProperty("ctf.dossier", "ollama/ctfAgents"));
    }

    /**
     * Inscrit l'agent aux pages jaunes, avec le type {@value #TYPE}, un nom de service et des propriétés
     * décrivant ses capacités. En cas d'échec, l'erreur est affichée sur la sortie d'erreur, sans exception.
     * @param agent l'agent à inscrire
     * @param service nom du service, ex. {@link #DECODEUR}
     * @param proprietes capacités de l'agent, ex. {@code operations -> "base64,hex"}
     */
    public static void inscrire(Agent agent, String service, Map<String, String> proprietes) {
        var dfd = new DFAgentDescription();
        dfd.setName(agent.getAID());
        var sd = new ServiceDescription();
        sd.setType(TYPE);
        sd.setName(service);
        proprietes.forEach((cle, valeur) -> sd.addProperties(new Property(cle, valeur)));
        dfd.addServices(sd);
        try {
            DFService.register(agent, dfd);
        } catch (FIPAException e) {
            System.err.println(agent.getLocalName() + " : inscription au DF impossible : " + e.getMessage());
        }
    }

    /**
     * Désinscrit l'agent des pages jaunes, à appeler dans {@code takeDown}. Les erreurs sont ignorées.
     * @param agent l'agent à désinscrire
     */
    public static void desinscrire(Agent agent) {
        try {
            DFService.deregister(agent);
        } catch (FIPAException ignored) {
        }
    }

    /**
     * Recherche dans les pages jaunes les agents qui proposent un service du CTF.
     * @param agent l'agent qui fait la recherche
     * @param service nom du service recherché, ex. {@link #ARBITRE}
     * @return les fiches trouvées (avec leurs propriétés) ; liste vide si aucune ou en cas d'erreur
     */
    public static List<DFAgentDescription> chercher(Agent agent, String service) {
        var modele = new DFAgentDescription();
        var sd = new ServiceDescription();
        sd.setType(TYPE);
        sd.setName(service);
        modele.addServices(sd);
        try {
            return List.of(DFService.search(agent, modele));
        } catch (FIPAException e) {
            return List.of();
        }
    }

    /**
     * Comme {@link #chercher}, mais ne garde que les identifiants des agents.
     * @param agent l'agent qui fait la recherche
     * @param service nom du service recherché
     * @return les AID des agents trouvés ; liste vide si aucun
     */
    public static List<AID> chercherAID(Agent agent, String service) {
        List<AID> res = new ArrayList<>();
        for (DFAgentDescription d : chercher(agent, service)) res.add(d.getName());
        return res;
    }

    /**
     * Lit les propriétés de la première {@link ServiceDescription} d'une fiche du DF.
     * @param fiche une fiche obtenue par {@link #chercher}
     * @return les propriétés au format JSON ({@code nom -> valeur} en texte) ; objet vide si la fiche n'a pas de service
     */
    public static JSONObject proprietes(DFAgentDescription fiche) {
        var res = new JSONObject();
        List<ServiceDescription> services = fiche.getAllServices();
        if (!services.isEmpty()) {
            Iterator<Property> props = services.get(0).getAllProperties();
            while (props.hasNext()) {
                Property p = props.next();
                res.put(p.getName(), String.valueOf(p.getValue()));
            }
        }
        return res;
    }

    /**
     * Lit le contenu JSON d'un message. Cette méthode ne lève jamais d'exception :
     * <ul>
     *     <li>contenu absent ou vide : objet vide ;</li>
     *     <li>contenu qui n'est pas du JSON : {@code {"texte": <contenu brut>}}.</li>
     * </ul>
     * @param msg le message reçu
     * @return le contenu sous forme d'objet JSON
     */
    public static JSONObject contenu(ACLMessage msg) {
        try {
            String c = msg.getContent();
            return c == null || c.isBlank() ? new JSONObject() : new JSONObject(c);
        } catch (JSONException e) {
            return new JSONObject().put("texte", msg.getContent());
        }
    }

    /**
     * Prépare la réponse à un message, avec un contenu JSON. Grâce à {@code createReply()}, la réponse
     * reprend le destinataire, l'identifiant de conversation et le {@code reply-with} du message d'origine.
     * Le message n'est pas envoyé : il faut appeler {@code send}.
     * @param msg le message auquel on répond
     * @param performatif le performatif de la réponse, ex. {@link ACLMessage#INFORM}
     * @param contenu le contenu JSON
     * @return la réponse prête à envoyer
     */
    public static ACLMessage repondre(ACLMessage msg, int performatif, JSONObject contenu) {
        ACLMessage rep = msg.createReply();
        rep.setPerformative(performatif);
        rep.setLanguage(LANGAGE);
        rep.setContent(contenu.toString());
        return rep;
    }

    /**
     * Crée un nouveau message avec un contenu JSON. Le message n'est pas envoyé : il faut appeler {@code send}.
     * Pour un suivi de conversation, l'appelant fixe lui-même l'identifiant ({@code setConversationId}).
     * @param performatif le performatif, ex. {@link ACLMessage#REQUEST}
     * @param destinataire l'agent destinataire
     * @param contenu le contenu JSON
     * @return le message prêt à envoyer
     */
    public static ACLMessage message(int performatif, AID destinataire, JSONObject contenu) {
        var msg = new ACLMessage(performatif);
        msg.addReceiver(destinataire);
        msg.setLanguage(LANGAGE);
        msg.setContent(contenu.toString());
        return msg;
    }

    /**
     * Affichage console homogène, de la forme {@code nomAgent -> texte}.
     * @param agent l'agent qui écrit
     * @param texte le message à afficher
     */
    public static void log(Agent agent, String texte) {
        System.out.println(agent.getLocalName() + " -> " + texte);
    }
}
