package ollama.ctfAgents.actions;

import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import ollama.toolAgent.outils.Outil;

import java.util.UUID;

/**
 * Base des « outils » du Capitaine : ici, un outil n'est plus une méthode locale
 * mais une ACTION D'AGENT (recherche DF, envoi de messages ACL et attente de la réponse).
 * <p>
 * On réutilise l'interface {@link Outil} et la {@link ollama.toolAgent.outils.BoiteAOutils} de toolAgent :
 * pour le LLM, rien ne change ; c'est l'agent qui, derrière, dialogue avec les autres agents.
 * <p>
 * ATTENTION : ces actions sont exécutées dans un comportement du Capitaine et utilisent blockingReceive
 * (avec un délai max) : pendant ce temps le Capitaine ne fait rien d'autre. Les messages qui ne
 * correspondent pas au modèle attendu restent dans sa boîte aux lettres.
 */
public abstract class ActionAgent implements Outil {
    /** délai max d'attente d'une réponse (ms), réglable par -Dctf.delai=... */
    public static final long DELAI = Long.getLong("ctf.delai", 15_000);

    /** compteurs de messages, pour les mesures demandées dans le sujet */
    private static int nbEnvoyes = 0;
    private static int nbRecus = 0;

    protected final Agent agent;

    protected ActionAgent(Agent agent) {
        this.agent = agent;
    }

    /** identifiant unique de conversation */
    protected String nouvelleConversation() {
        return agent.getLocalName() + "-" + nom() + "-" + UUID.randomUUID().toString().substring(0, 8);
    }

    /** envoie un message (en comptant) */
    protected void envoyer(ACLMessage msg) {
        agent.send(msg);
        nbEnvoyes += countReceivers(msg);
    }

    /** attend une réponse dans une conversation donnée, null si délai dépassé */
    protected ACLMessage attendre(String conversation, long delai) {
        ACLMessage rep = agent.blockingReceive(MessageTemplate.MatchConversationId(conversation), delai);
        if (rep != null) nbRecus++;
        return rep;
    }

    /** envoie le message dans une nouvelle conversation et attend LA réponse */
    protected ACLMessage envoyerEtAttendre(ACLMessage msg, long delai) throws Exception {
        String conv = nouvelleConversation();
        msg.setConversationId(conv);
        envoyer(msg);
        ACLMessage rep = attendre(conv, delai);
        if (rep == null) throw new Exception("pas de réponse en " + delai / 1000 + " s (agent absent, occupé ou arrêté ?)");
        return rep;
    }

    private static int countReceivers(ACLMessage msg) {
        int n = 0;
        var it = msg.getAllReceiver();
        while (it.hasNext()) {
            it.next();
            n++;
        }
        return n;
    }

    public static String statistiques() {
        return nbEnvoyes + " messages ACL envoyés, " + nbRecus + " reçus";
    }

    public static void razStatistiques() {
        nbEnvoyes = 0;
        nbRecus = 0;
    }
}
