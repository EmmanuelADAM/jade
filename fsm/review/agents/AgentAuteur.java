package fsm.review.agents;


import jade.core.AID;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.FSMBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.gui.AgentWindowed;
import jade.gui.GuiEvent;
import jade.gui.SimpleWindow4Agent;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.util.HashMap;

import static java.lang.System.out;


/**
 * agent qui attend un message à partir du protocole CFP, prépare la réponse et la retourne
 *
 * @author eadam
 */
public class AgentAuteur extends AgentWindowed {

    final String SOUMETTRE = "soumettre";
    final String ATTENDREAVIS = "attendre_avis";
    final String POURSUIVRE = "poursuivre";
    final String ARRETER = "arreter";
    final String FETER = "feter";

    FSMBehaviour fsm;

    /**
     * un agent auteur soumet un article, attends le resultat puis fete, arrete ou resoumet selon le cas
     */
    protected void setup() {
        window = new SimpleWindow4Agent(getAID().getName(), this);
        window.setButtonActivated(true);
        println("Hello! Agent  " + getLocalName() + " is ready, my address is " + this.getAID().getName());
        println("- ".repeat(20));

        HashMap<String, Object> ds = new HashMap<>();


        //creation du comportement de type machine d'etats finis
        fsm = new FSMBehaviour(this) {
            public int onEnd() {
                out.println("FSM behaviour terminé, je m'en vais");
                myAgent.doDelete();
                return super.onEnd();
            }
        };

        //____LES ETATS
        //ETAT INITIAL
        fsm.registerFirstState(soumettre(ds), SOUMETTRE);
        // autres etats
        fsm.registerState(attendreAvis(ds), ATTENDREAVIS);
        fsm.registerState(resoumettre(ds), POURSUIVRE);
        //ETATS FINAUX
        fsm.registerLastState(arreter(), ARRETER);
        fsm.registerLastState(feter(), FETER);

        //____LES TRANSITIONS
        fsm.registerDefaultTransition(SOUMETTRE, ATTENDREAVIS);
        //si le comportement lie a attendre avis retourne 0, c'est fini
        fsm.registerTransition(ATTENDREAVIS, ARRETER, 0);
        //si le comportement lie a attendre avis retourne 2, c'est fini mais on fete cela
        fsm.registerTransition(ATTENDREAVIS, FETER, 2);
        //si le comportement lie a attendre avis retourne 1, on regarde pour poursuivre
        fsm.registerTransition(ATTENDREAVIS, POURSUIVRE, 1);
        //si le comportement lie a attendre poursuivre retourne 0, on a decide d'arreter
        fsm.registerTransition(POURSUIVRE, ARRETER, 0);
        //si le comportement lie a attendre poursuivre retourne 1, on retente une nouvelle soumission et on attends un nouvel avis (on reinitialise certains comportements)
        fsm.registerTransition(POURSUIVRE, ATTENDREAVIS, 1, new String[]{ATTENDREAVIS, POURSUIVRE});


    }

    @Override
    protected void onGuiEvent(GuiEvent arg0) {
        fsm.reset();
        addBehaviour(fsm);
        window.setButtonActivated(false);
    }

    /**
     * soumettre un article a l'agent journal
     */
    private Behaviour soumettre(HashMap<String, Object> ds) {
        Behaviour b = new OneShotBehaviour(this) {
            @Override
            public void action() {
                String cle = "msg" + System.currentTimeMillis();
                ds.put("cle", cle);
                ACLMessage msg = new ACLMessage(ACLMessage.PROPOSE);
                msg.setContent("voici ma prose originale");
                msg.setConversationId(cle);
                println("J'envoie \"%s\" avec la cle (%s)".formatted(msg.getContent(), ds.get("cle")));
                msg.addReceiver(new AID("j", AID.ISLOCALNAME));
                send(msg);
            }
        };
        return b;
    }

    /**
     * attendre un retour correspondant a l'article envoyé
     */
    private Behaviour attendreAvis(HashMap<String, Object> ds) {
        Behaviour b = new OneShotBehaviour(this) {
            int retour = 1;
            String cle;
            MessageTemplate mt;

            @Override
            public void onStart() {
            }

            @Override
            public void reset() {
                retour = 1;
            }

            @Override
            public void action() {
                cle = String.valueOf(ds.get("cle"));
                mt = MessageTemplate.MatchConversationId(cle);
                println("j'attends un retour avec cette cle : " + mt);
                ACLMessage msg = null;
                while ((msg = blockingReceive(mt)) == null) block();
                retour = Integer.parseInt(msg.getContent().split(":")[0]);
                println("j'ai recu cet avis : " + msg.getContent());
            }

            @Override
            public int onEnd() {
                return retour;
            }

        };
        return b;
    }


    /**
     * resoumettre un article a l'agent journal
     */
    private Behaviour resoumettre(HashMap<String, Object> ds) {
        Behaviour b = new OneShotBehaviour(this) {
            int retour = 1;
            int i = 1;

            @Override
            public void reset() {
            }

            @Override
            public void action() {
                ACLMessage msg = new ACLMessage(ACLMessage.PROPOSE);
                msg.setConversationId(String.valueOf(ds.get("cle")));
                msg.addReceiver(new AID("j", AID.ISLOCALNAME));
                if (Math.random() < 0.3) {
                    msg.setPerformative(ACLMessage.CANCEL);
                    msg.setContent("je ne souhaite pas poursuivre...");
                    println("je ne souhaite pas poursuivre...");
                    retour = 0;
                } else {
                    msg.setContent("voici ma prose R#" + i);
                    println("j'envoie ma prose R#" + i + " avec la cle " + msg.getConversationId());
                    i++;
                }
                println("-".repeat(40));
                send(msg);
            }

            @Override
            public int onEnd() {
                return retour;
            }
        };
        return b;
    }

    /**
     * resoumettre un article a l'agent journal
     */
    private Behaviour arreter() {
        Behaviour b = new OneShotBehaviour(this) {
            int i = 0;

            @Override
            public void reset() {
                i = 0;
            }

            @Override
            public void action() {
                println("J'arrete la, a une prochaine fois !");
                println("~".repeat(40));
            }
        };
        return b;
    }

    /**
     * resoumettre un article a l'agent journal
     */
    private Behaviour feter() {
        Behaviour b = new OneShotBehaviour(this) {
            @Override
            public void action() {

                println("super, un article accepte !!");
                println("~".repeat(40));
            }
        };
        return b;
    }

}
