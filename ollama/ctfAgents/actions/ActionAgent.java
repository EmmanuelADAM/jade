package ollama.ctfAgents.actions;

import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import ollama.ctfAgents.commun.Outil;

import java.util.UUID;

/**
 * Base des « outils » du Capitaine : ici, un outil n'est plus une méthode locale
 * mais une ACTION D'AGENT (recherche DF, envoi de messages ACL et attente de la réponse).
 * <p>
 * On réutilise l'interface {@link Outil} vue dans  {@link ollama.toolAgent.outils.BoiteAOutils} de toolAgent :
 * pour le LLM, rien ne change ; c'est l'agent qui, derrière, dialogue avec les autres agents.
 * <p>
 *     des fonctions de bases (envoyer, recevoir un message...), sont encapsulées pour compter les messages échangés
 * <p>
 * DEUX THREADS : ces actions sont appelées par la boucle du LLM, qui tourne dans un thread à part
 * (ThreadedBehaviourFactory, voir AgentCapitaine).
 * Les actions simples (demander, proposer_flag) attendent leur réponse avec blockingReceive (délai max) dans ce thread ;
 * les protocoles (appel_offres, consulter_tous) confient un ContractNetInitiator au thread de l'agent et attendent son résultat.
 * Le thread de l'agent reste donc libre : il exécute les protocoles, reçoit les scores, répond à la fenêtre.
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
        compter(countReceivers(msg), 0);
    }

    /** comptage des messages (appelé depuis le thread du LLM ET depuis celui de l'agent : synchronisé) */
    protected static synchronized void compter(int envoyes, int recus) {
        nbEnvoyes += envoyes;
        nbRecus += recus;
    }

    /** attend une réponse dans une conversation donnée, null si délai dépassé */
    protected ACLMessage attendre(String conversation, long delai) {
        ACLMessage rep = agent.blockingReceive(MessageTemplate.MatchConversationId(conversation), delai);
        if (rep != null) compter(0, 1);
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

    public static synchronized String statistiques() {
        return nbEnvoyes + " messages ACL envoyés, " + nbRecus + " reçus";
    }

    public static synchronized void razStatistiques() {
        nbEnvoyes = 0;
        nbRecus = 0;
    }
}
