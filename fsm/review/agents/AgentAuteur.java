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
 * class for an author agent that use a Finite State MAchine behavior to manage its process of submission of an article
 *
 * @author eadam
 */
public class AgentAuteur extends AgentWindowed {

    final String SUBMIT = "submission";
    final String WAITRESULT = "wait_decision";
    final String CONTINUE = "continue?";
    final String STOP = "abandon";
    final String CELEBRATE = "celebrate";

    FSMBehaviour fsm;

    /**
     * an author agent submits an article, waits for the decision and then celebrates, stops or resubmits as appropriate
     */
    protected void setup() {
        window = new SimpleWindow4Agent(getAID().getName(), this);
        window.setButtonActivated(true);
        println("Hello! I'm ready, my address is " + this.getAID().getName());
        println("- ".repeat(20));

        HashMap<String, Object> ds = new HashMap<>();


        //creation of finite state machine type behavior
        fsm = new FSMBehaviour(this) {
            public int onEnd() {
                out.println("FSM behaviour ended, I'm leaving");
                myAgent.doDelete();
                return super.onEnd();
            }
        };

        //____THE STATES
        // INITIAL STATE
        fsm.registerFirstState(submit(ds), SUBMIT);
        // other states
        fsm.registerState(waitEvaluation(ds), WAITRESULT);
        fsm.registerState(resubmit(ds), CONTINUE);
        //FINAL STATES
        fsm.registerLastState(arreter(), STOP);
        fsm.registerLastState(celebrate(), CELEBRATE);

        //____TRANSITIONS
        fsm.registerDefaultTransition(SUBMIT, WAITRESULT);
        //If the behavior related to Wait results returns 0, it's over
        fsm.registerTransition(WAITRESULT, STOP, 0);
        //If the behavior related to Wait results returns 0, it's over but I celebrate
        fsm.registerTransition(WAITRESULT, CELEBRATE, 2);
        //If the behavior related to Wait results returns 1, I decide if I submit again
        fsm.registerTransition(WAITRESULT, CONTINUE, 1);
        //If the behavior related to the reflexion about resubmission returns 0, I decide to stop
        fsm.registerTransition(CONTINUE, STOP, 0);
        //If the behavior related to the reflexion about resubmission returns 0, I try a new submission, and I reset some behaviors
        fsm.registerTransition(CONTINUE, WAITRESULT, 1, new String[]{WAITRESULT, CONTINUE});


    }

    /**click on the button => submission (only 1 click allowed)*/
    @Override
    protected void onGuiEvent(GuiEvent arg0) {
        fsm.reset();
        addBehaviour(fsm);
        window.setButtonActivated(false);
    }

    /**
     * Submit an article to the journal agent
     * @param ds a data store to store the keys used to stamp the msgs
     * @return a one shot behavior that sends a msg to the journal agent, and store its key
     */
    private Behaviour submit(HashMap<String, Object> ds) {
        return  new OneShotBehaviour(this, a-> {
                String key = "msg" + System.currentTimeMillis();
                ds.put("key", key);
                ACLMessage msg = new ACLMessage(ACLMessage.PROPOSE);
                msg.setContent("here is my original prose...");
                msg.setConversationId(key);
                msg.addReceiver("j");
                send(msg);
                println("I've sent \"%s\" with the key (%s)".formatted(msg.getContent(), ds.get("key")));
        });
    }

    /**
     * wait for a response corresponding to the article sent
     * @param ds the data store containing the keys linked to the msg sent
     * @return a one shot behaviour that wait for a response
     */
    private Behaviour waitEvaluation(HashMap<String, Object> ds) {
        Behaviour b = new OneShotBehaviour(this) {
            int evaluation = 1;
            String key;
            MessageTemplate mt;

            @Override
            public void onStart() {
            }

            @Override
            public void reset() {
                evaluation = 1;
            }

            @Override
            public void action() {
                key = String.valueOf(ds.get("key"));
                mt = MessageTemplate.MatchConversationId(key);
                println("I wait for a response with this key : " + mt);
                ACLMessage msg = null;
                while ((msg = blockingReceive(mt)) == null) block();
                evaluation = Integer.parseInt(msg.getContent().split(":")[0]);
                println("I received this evaluation : " + msg.getContent());
            }

            @Override
            public int onEnd() {
                return evaluation;
            }

        };
        return b;
    }


    /**
     * Decision to continue or not the submission
     * @param ds a data store to store the keys used to stamp the msgs
     * @return a One-shot behavior that sends an MSG to the journal agent indicating whether the author is abandoning or proposing a new version
     */
    private Behaviour resubmit(HashMap<String, Object> ds) {
        Behaviour b = new OneShotBehaviour(this) {
            int decision = 1;
            int i = 1;

            @Override
            public void reset() {
            }

            @Override
            public void action() {
                ACLMessage msg = new ACLMessage(ACLMessage.PROPOSE);
                msg.setConversationId(String.valueOf(ds.get("key")));
                msg.addReceiver(new AID("j", AID.ISLOCALNAME));
                if (Math.random() < 0.3) {
                    msg.setPerformative(ACLMessage.CANCEL);
                    msg.setContent("I propose to stop and cancel the submission...");
                    println("I decided to stop and cancel the submission...");
                    decision = 0;
                } else {
                    msg.setContent("here is my revision R#" + i);
                    println("I send my revision R#" + i + " with the key " + msg.getConversationId());
                    i++;
                }
                println("-".repeat(40));
                send(msg);
            }

            @Override
            public int onEnd() {
                return decision;
            }
        };
        return b;
    }

    /**
     * Abandon
     * @return a One-shot behavior that displays a msg indicating the abandon of the process
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
                println("I decide to stop and try other thing, see you !");
                println("~".repeat(40));
            }
        };
        return b;
    }

    /**
     * Celebration
     * @return a One-shot behavior that displays a msg indicating the author is happy
     */
    private Behaviour celebrate() {
        return new OneShotBehaviour(this, a->{
                println("Great !! My new article has been accepted !!");
                println("~".repeat(40));
            });
    }

}
