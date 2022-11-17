package fsm.review.agents;


import jade.core.AgentServicesTools;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.FSMBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.WakerBehaviour;
import jade.gui.AgentWindowed;
import jade.gui.SimpleWindow4Agent;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.awt.*;
import java.util.Arrays;
import java.util.HashMap;

import static java.lang.System.out;


/**
 * class for an journal agent that use a Finite State MAchine behavior to manage its process of submission of an article
 *
 * @author eadam
 */
public class JournalAgent extends AgentWindowed {
    final String WAIT_ARTICLE = "wait_proposal";
    final String SEND_TO_3REVIEWERS = "send_for_review";
    final String WAIT_3REVIEWS = "wait_reviews";
    final String SEND_DECISION = "send_decision";
    final String WAIT_ACK = "wait_acknowledgment_of_receipt";
    final String END = "terminate_session";
    volatile HashMap<String, Object> ds = new HashMap<>();

    int nbReviewers;

    /**
     * A journal waits for an article, forwards it to reviewers, sends the result to the author
     * and stops if return is acceptance or refusal; or awaits feedback from the author whether  to resubmit
     */
    protected void setup() {
        window = new SimpleWindow4Agent(getAID().getName(), this);
        println("Hello! Agent  " + getLocalName() + " is ready, my address is " + this.getAID().getName());
        window.setButtonActivated(false);
        window.setBackgroundTextColor(Color.ORANGE);


        //creation of finite state machine type behavior
        FSMBehaviour fsm = new FSMBehaviour(this) {
            public int onEnd() {
                out.println("FSM behaviour termin�, je m'en vais");
                myAgent.doDelete();
                return super.onEnd();
            }
        };

        //____THE STATES
        // INITIAL STATE
        fsm.registerFirstState(waitForProposal(ds), WAIT_ARTICLE);
        // other states
        fsm.registerState(sendForReviewing(ds), SEND_TO_3REVIEWERS);
        fsm.registerState(attendreReviews(ds), WAIT_3REVIEWS);
        fsm.registerState(sendResult(ds), SEND_DECISION);
        fsm.registerState(attendreAvisAuteur(ds), WAIT_ACK);
        //FINAL STATES
        fsm.registerLastState(arreter(ds), END);

        //____TRANSITIONS
        fsm.registerDefaultTransition(WAIT_ARTICLE, SEND_TO_3REVIEWERS);
        fsm.registerDefaultTransition(SEND_TO_3REVIEWERS, WAIT_3REVIEWS);
        fsm.registerDefaultTransition(WAIT_3REVIEWS, SEND_DECISION);
        //if decision is to accept (2) or reject (0), terminate the behavior
        fsm.registerTransition(SEND_DECISION, END, 0);
        fsm.registerTransition(SEND_DECISION, END, 2);
        //if decision is to propose to send a revision (1), wait for the author decision
        fsm.registerTransition(SEND_DECISION, WAIT_ACK, 1);
        //if author decision is to cancel (0), terminate the behavior
        fsm.registerTransition(WAIT_ACK, END, 0);
        //if author decision is to send a new version (1), reset the behaviors and go back to send to reviewers step
        fsm.registerTransition(WAIT_ACK, SEND_TO_3REVIEWERS, 1, new String[]{WAIT_3REVIEWS, SEND_DECISION, WAIT_ACK});

        // add the FSM behavior in 100ms
        addBehaviour(new WakerBehaviour(this, 100, a->a.addBehaviour(fsm)));
    }


    /**
     * Wait for a message containing an ""article""
     * @param ds a data store to store the keys used to stamp the msgs
     * @return a one shot behavior that listen for a "propose" msg, and store its key
     */
    private Behaviour waitForProposal(HashMap<String, Object> ds) {
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
                ds.put("key", msg.getConversationId());

                println("---> from %s, I received this '%s' with the key %s".formatted(msg.getSender().getLocalName(),
                        msg.getContent(), msg.getConversationId()));
            }
        };
        return b;
    }

    /**
     * search for 3 reviewers and send them an article to evaluate
     * @param ds a data store to store the keys used to stamp the msgs
     * @return a one shot behavior that send a msg to 3 reviewer agents
     */
    private Behaviour sendForReviewing(HashMap<String, Object> ds) {
        Behaviour b = new OneShotBehaviour(this) {

            @Override
            public void action() {
                var reviewers = AgentServicesTools.searchAgents(myAgent, "journal", "reviewer");
                nbReviewers = reviewers.length;
                ACLMessage msg = (ACLMessage) ds.get("article");
                String key = (String) ds.get("key");
                ACLMessage forward = msg.createReply();
                forward.clearAllReceiver();
                forward.setContent(msg.getContent());
                forward.addReceivers(reviewers);
                forward.setConversationId(key);
                myAgent.send(forward);
                println("I've sent the article to evaluate (with the key " + key + ") to  " + Arrays.toString(reviewers));
            }
        };
        return b;
    }

    /**
     * wait for 3 evaluations from the reviewer; compute the global evaluation (2 if all reviewers give the best
     * mark, 0 if at least one of them has rejected the article and 1 in other cases)
     * @param ds a data store to store the keys used to stamp the msgs
     * @return a one shot behavior that wait for 3 msg on a given key
     */
    private Behaviour attendreReviews(HashMap<String, Object> ds) {
        Behaviour b = new Behaviour(this) {
            int i = 0;
            int val = 1;
            MessageTemplate mt;

            @Override
            public void onStart() {
                String key = (String) ds.get("key");
                mt = MessageTemplate.MatchConversationId(key);
                println("I wait for messages from reviewers on this key " + key);
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
                    println("--> I received the mark  %s from %s".formatted(msg.getContent(),
                            msg.getSender().getLocalName()));
                } else block();
            }

            /**done when all the reviewers answered*/
            @Override
            public boolean done() {
                return i == nbReviewers;
            }

            @Override
            public int onEnd() {
                if (val == 8) val = 2;
                else if (val != 0) val = 1;
                ds.put("eval", val);
                println("Evaluation finished with this evaluation : " + val);
                return val;
            }
        };
        return b;
    }

    /**
     * send the result of the evaluation to the author
     * @param ds a data store to store the keys used to stamp the msgs
     * @return a one shot behavior that send a msg to the author
     */
    private Behaviour sendResult(HashMap<String, Object> ds) {
        Behaviour b = new OneShotBehaviour(this) {
            int val = 0;

            @Override
            public void action() {
                val = (Integer) (ds.get("eval"));
                ACLMessage msg = (ACLMessage) ds.get("article");
                ACLMessage reply = msg.createReply();
                switch (val) {
                    case 0 -> reply.setContent("0: Sorry, your article has not been accepted.... Persevere and try again next time");
                    case 1 -> reply.setContent("1: The article is accepted subject to change ...");
                    case 2 -> reply.setContent("2: It's a pleasure to inform you that your article is accepted !");
                }
                myAgent.send(reply);
                println("I send the result to '" + msg.getSender().getLocalName() + "' : \"" + reply.getContent() +"\"");
            }

            @Override
            public int onEnd() {
                return val;
            }
        };
        return b;
    }

    /**
     * wait the decision of the author (new revision or abandon)
     * @param ds a data store to store the keys used to stamp the msgs
     * @return a one shot behavior that wait for a decision msg from the author
     */
    private Behaviour attendreAvisAuteur(HashMap<String, Object> ds) {
        Behaviour b = new Behaviour(this) {
            boolean end = false;
            int val = 0;
            String key;
            MessageTemplate mt;

            @Override
            public void onStart() {
                key = String.valueOf(ds.get("key"));
                mt = MessageTemplate.MatchConversationId(key);
                window.setButtonActivated(false);
            }

            @Override
            public void reset() {
                key = String.valueOf(ds.get("key"));
                mt = MessageTemplate.MatchConversationId(key);
            }


            @Override
            public void action() {
                ACLMessage msg = blockingReceive(mt);
                if (msg != null) {
                    println("I received this: " + msg.getContent());
                    if (msg.getPerformative() == ACLMessage.CANCEL) {
                        println("--> The author does not wish to continue  ...");
                        val = 0;
                    }
                    if (msg.getPerformative() == ACLMessage.PROPOSE) {
                        ds.put("key", msg.getConversationId());
                        ds.put("article", msg);
                        println("--> The author submits a new version.");
                        val = 1;
                    }
                    end = true;
                    println("-".repeat(40));
                } else block();
            }

            @Override
            public boolean done() {
                return end;
            }

            @Override
            public int onEnd() {
                return val;
            }

        };
        return b;
    }

    /**
     * Close the process
     * @return a One-shot behavior that displays a msg indicating the process is finished
     */
    private Behaviour arreter(HashMap<String, Object> ds) {
        return new OneShotBehaviour(this, a -> {
            println("the submission process is finished ... ");
            println("~".repeat(40));
        });
    }


}
