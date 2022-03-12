package fsm.review.agents;


import jade.core.AgentServicesTools;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.FSMBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.WakerBehaviour;
import jade.gui.AgentWindowed;
import jade.gui.GuiEvent;
import jade.gui.SimpleWindow4Agent;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.awt.*;
import java.util.Arrays;
import java.util.HashMap;

import static java.lang.System.out;


/**
 * classe d'un agent qui soumet un appel d'offres a d'autres agents  par le protocole ContractNet
 *
 * @author eadam
 */
public class AgentJournal extends AgentWindowed {
    final String ATTENDRE_ARTICLE = "attendre_article";
    final String ENVOYER_3REVIEWERS = "envoyer_pour_review";
    final String ATTENDRE_REVIEWS = "attendre_reviews";
    final String ENVOYER_RESULTAT = "transmettre_avis";
    final String ATTENDRE_AR = "attendre_retour_auteur";
    final String FINIR = "fin_session";
    volatile HashMap<String, Object> ds = new HashMap<>();

    int nbReviewers;

    /**
     * un journal attend un article, le transmets aux reviewers, envoi le resultat a l'auteur
     * et stoppe si retour est acceptation ou refus; ou attend un retour de l'auteur s'il souhaite resoumetre ou non
     */
    protected void setup() {
        window = new SimpleWindow4Agent(getAID().getName(), this);
        println("Hello! Agent  " + getLocalName() + " is ready, my address is " + this.getAID().getName());
        window.setButtonActivated(false);
        window.setBackgroundTextColor(Color.ORANGE);


        //creation du comportement de type machine d'etat finis
        FSMBehaviour fsm = new FSMBehaviour(this) {
            public int onEnd() {
                out.println("FSM behaviour terminé, je m'en vais");
                myAgent.doDelete();
                return super.onEnd();
            }
        };

        //____LES ETATS
        fsm.registerFirstState(attendreArticle(ds), ATTENDRE_ARTICLE);
        fsm.registerState(envoyerPourRelecture(ds), ENVOYER_3REVIEWERS);
        fsm.registerState(attendreReviews(ds), ATTENDRE_REVIEWS);
        fsm.registerState(envoyerResultat(ds), ENVOYER_RESULTAT);
        fsm.registerState(attendreAvisAuteur(ds), ATTENDRE_AR);
        fsm.registerLastState(arreter(ds), FINIR);

        //____LES TRANSITIONS
        fsm.registerDefaultTransition(ATTENDRE_ARTICLE, ENVOYER_3REVIEWERS);
        fsm.registerDefaultTransition(ENVOYER_3REVIEWERS, ATTENDRE_REVIEWS);
        fsm.registerDefaultTransition(ATTENDRE_REVIEWS, ENVOYER_RESULTAT);
        fsm.registerTransition(ENVOYER_RESULTAT, FINIR, 0);
        fsm.registerTransition(ENVOYER_RESULTAT, FINIR, 2);
        fsm.registerTransition(ENVOYER_RESULTAT, ATTENDRE_AR, 1);
        fsm.registerTransition(ATTENDRE_AR, FINIR, 0);
        fsm.registerTransition(ATTENDRE_AR, ENVOYER_3REVIEWERS, 1, new String[]{ATTENDRE_REVIEWS, ENVOYER_RESULTAT, ATTENDRE_AR});

        // ajout d'un comportement qui ajoute le comportement fsm dans 100ms
        addBehaviour(new WakerBehaviour(this, 100) {
            protected void onWake() {
                myAgent.addBehaviour(fsm);
            }
        });
    }


    private Behaviour attendreArticle(HashMap<String, Object> ds) {
        Behaviour b = new OneShotBehaviour(this) {
            MessageTemplate mt;

            @Override
            public void onStart() {
                mt = MessageTemplate.MatchPerformative(ACLMessage.PROPOSE);
            }

            @Override
            public void action() {
                ACLMessage msg = null;
                while ((msg = blockingReceive(mt)) == null) block();
                ds.put("article", msg);
                ds.put("cle", msg.getConversationId());

                println("de %s, j'ai recu ceci '%s' avec la cle %s".formatted(msg.getSender().getLocalName(), msg.getContent(), msg.getConversationId()));
            }
        };
        return b;
    }

    private Behaviour envoyerPourRelecture(HashMap<String, Object> ds) {
        Behaviour b = new OneShotBehaviour(this) {

            @Override
            public void action() {
                var tab = AgentServicesTools.searchAgents(myAgent, "journal", "reviewer");
                nbReviewers = tab.length;
                ACLMessage msg = (ACLMessage) ds.get("article");
                String cle = (String) ds.get("cle");
                ACLMessage forward = msg.createReply();
                forward.clearAllReceiver();
                forward.setContent(msg.getContent());
                forward.addReceivers(tab);
                forward.setConversationId(cle);
                myAgent.send(forward);
                println("j'envoie le doc a reviewer (avec la cle " + cle + ") a " + Arrays.toString(tab));
            }
        };
        return b;
    }

    private Behaviour attendreReviews(HashMap<String, Object> ds) {
        Behaviour b = new Behaviour(this) {
            int i = 0;
            int val = 1;
            MessageTemplate mt;

            @Override
            public void onStart() {
                String cle = (String) ds.get("cle");
                mt = MessageTemplate.MatchConversationId(cle);
                println("je suis dans l'attente des reviewers sur la cle " + cle);
            }

            @Override
            public void reset() {
                i = 0;
                val = 1;
            }

            @Override
            public void action() {
                ACLMessage msg = blockingReceive(mt);

                if (msg != null) {
                    i++;
                    val = val * Integer.parseInt(msg.getContent());
                    println("j'ai recu la note %s de la part de %s".formatted(msg.getContent(), msg.getSender().getLocalName()));
                } else block();
            }

            @Override
            public boolean done() {
                return i == nbReviewers;
            }

            @Override
            public int onEnd() {
                println("attendreReviews a fini avec i=" + i);
                if (val == 8) val = 2;
                else if (val != 0) val = 1;
                ds.put("val", val);
                return val;
            }

        };
        return b;
    }

    private Behaviour envoyerResultat(HashMap<String, Object> ds) {
        Behaviour b = new OneShotBehaviour(this) {
            int val = 0;

            @Override
            public void action() {
                val = (Integer) (ds.get("val"));
                ACLMessage msg = (ACLMessage) ds.get("article");
                ACLMessage reply = msg.createReply();
                switch (val) {
                    case 0 -> reply.setContent("0: Désolé votre article n'a pas ete accepte.... Perseverez et retentez une prochaine fois");
                    case 1 -> reply.setContent("1: Votre article est accepte sous reserve de modification...");
                    case 2 -> reply.setContent("2: Nous avons le plaisir de vous informer que votre article est accepte !");
                }
                myAgent.send(reply);
                println("j'envoie cet avis a '" + msg.getSender().getLocalName() + "' : " + reply.getContent());
            }

            @Override
            public int onEnd() {
                return val;
            }
        };
        return b;
    }

    private Behaviour attendreAvisAuteur(HashMap<String, Object> ds) {
        Behaviour b = new Behaviour(this) {
            boolean fin = false;
            int val = 0;
            String cle;
            MessageTemplate mt;

            @Override
            public void onStart() {
                cle = String.valueOf(ds.get("cle"));
                mt = MessageTemplate.MatchConversationId(cle);
                window.setButtonActivated(false);
            }

            @Override
            public void reset() {
                cle = String.valueOf(ds.get("cle"));
                mt = MessageTemplate.MatchConversationId(cle);
            }


            @Override
            public void action() {
                ACLMessage msg = blockingReceive(mt);
                if (msg != null) {
                    println("j'ai recu ceci : " + msg.getContent());
                    if (msg.getPerformative() == ACLMessage.CANCEL) {
                        println("l'auteur ne souhaite pas poursuivre...");
                        val = 0;
                    }
                    if (msg.getPerformative() == ACLMessage.PROPOSE) {
                        ds.put("cle", msg.getConversationId());
                        ds.put("article", msg);
                        println("l'auteur a soumis une revision");
                        val = 1;
                    }
                    fin = true;
                    println("-".repeat(40));
                } else block();
            }

            @Override
            public boolean done() {
                return fin;
            }

            @Override
            public int onEnd() {
                return val;
            }

        };
        return b;
    }

    private Behaviour arreter(HashMap<String, Object> ds) {
        Behaviour b = new OneShotBehaviour(this) {
            @Override
            public void action() {
                println("le processus est fini ... ");
                println("~".repeat(40));
            }
        };
        return b;
    }

    @Override
    protected void onGuiEvent(GuiEvent arg0) {

    }


}
