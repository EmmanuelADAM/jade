package td.negoHumanAgents.agents;

import jade.core.AID;
import jade.core.AgentServicesTools;
import jade.core.behaviours.*;
import jade.gui.AgentWindowed;
import jade.gui.GuiEvent;
import jade.gui.SimpleWindow4Agent;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.awt.*;
import java.time.LocalTime;
import java.util.Random;


/**
 * agents linked to a window, sends a radio message on a channel
 *
 * @author eadam
 */
@SuppressWarnings("serial")
public class SellerAgent extends AgentWindowed {
    enum object{book, watch, mouse};
    /**
     * address of the radio topic
     */
    AID topic;
    /**
     * no of the sent msg
     */
    double prixVendeur;
    double prixMin;
    double delta = 0.05;
    int nbEchanges;
    AID[] lastBuyer = {null};
    double[] lastOffer = {0};
    int[] i = {0};
    boolean sold = false;



    protected void setup() {
        window = new SimpleWindow4Agent(getAID().getName(), this);
        println("Hello! I'm ready, my address is " + this.getAID().getName());
        println("click to the button to launch an auction");
        window.setButtonActivated(true);
        window.setBackgroundTextColor(Color.ORANGE);
        //Create a "radio channel" with the name 'BestAgentsCharts'
        topic = AgentServicesTools.generateTopicAID(this, "Negociation");
    }

    /**
     * reaction to a gui event
     */
    protected void onGuiEvent(GuiEvent ev) {
        if (ev.getType() == SimpleWindow4Agent.OK_EVENT) {
            startNego();
        }
    }

    /**
     * send messages on the "radio channel"
     */
    private void startNego() {
        //START NEGOCIATION
        Object o = initNegociation();
        //
        println("I launched a negociation with a starting price of "+prixVendeur + " for " + o);
        LocalTime[] endTime = {LocalTime.now().plusSeconds(3)};


        //SEND A NEW OFFER EVERY 3 SECONDS IF A PROPOSAL WAS RECEIVED
        TickerBehaviour relanceBehaviour = getRelanceBehaviour(i, lastOffer);
        addBehaviour(relanceBehaviour);

        // wait for a proposal from the buyers
        Behaviour waitProposal = getWaitProposalBehaviour(lastOffer, lastBuyer, endTime, i);
        addBehaviour(waitProposal);


        var modele4 = MessageTemplate.MatchPerformative(ACLMessage.ACCEPT_PROPOSAL);
        // add a behaviour that wait for an eventual failure msg
        addBehaviour(new ReceiverBehaviour(this,  -1, modele4,true, (a, msg) ->
        {   println(getLocalName() + " -> " + msg.getSender().getLocalName() + " accepted the offer  : " + msg.getContent());
            var endMsg = new ACLMessage(ACLMessage.INFORM);
            endMsg.addReceiver(topic);
            endMsg.setContent("offer accepter for " + lastOffer[0] + " to " + lastBuyer[0].getLocalName() + "...");
            a.send(endMsg);}
        ));


    }

    private Behaviour getWaitProposalBehaviour(double[] lastOffer, AID[] lastBuyer, LocalTime[] endTime, int[] i) {
        var model = MessageTemplate.and(MessageTemplate.MatchTopic(topic), MessageTemplate.MatchPerformative(ACLMessage.PROPOSE));
        model = MessageTemplate.and(model, MessageTemplate.not(MessageTemplate.MatchSender(getAID())));
        Behaviour waitProposal = new ReceiverBehaviour(this, -1, model, true,(a,msg)->{
            var offer = Double.parseDouble(msg.getContent());
            if (offer > lastOffer[0]) {
                lastOffer[0] = Double.parseDouble(msg.getContent());
                lastBuyer[0] = msg.getSender();
            }
            println("received \"%.2f\" on the topic channel '%s', sent by %s".
                    formatted(lastOffer[0], topic.getLocalName(), msg.getSender().getLocalName()));
            endTime[0] = LocalTime.now().plusSeconds(3);
//            i[0] = 0;
        });
        return waitProposal;
    }

    private TickerBehaviour getRelanceBehaviour(int[] i, double[] lastOffer) {
        TickerBehaviour decompteTemps = new TickerBehaviour(this, 3000, agent -> {
            i[0]++;
            if(lastOffer[0]>0 && i[0]<4 && !sold) {
                if (lastOffer[0] > Math.max(prixVendeur * (1 - delta), prixMin)) {
                    sold = true;
                    ACLMessage msgAccept = new ACLMessage(ACLMessage.ACCEPT_PROPOSAL);
                    msgAccept.setContent(lastBuyer[0].getLocalName() + ":ADJUGE VENDU pour " + lastOffer[0] + " € !");
                    msgAccept.addReceiver(topic);
                    send(msgAccept);
                    println("ADJUGE VENDU pour " + lastOffer[0] + " à " + lastBuyer[0].getLocalName() + "...");
                }
            }
            if(!sold) {
                println("::j'attends encore un peu pour faire une contre proposition...");
                if (lastOffer[0] > 0 && i[0] > 3) {
                    prixVendeur = Math.max(prixVendeur * (1 - delta), prixMin);
                    ACLMessage msg = new ACLMessage(ACLMessage.PROPOSE);
                    msg.setConversationId("SELLER-NEGO");
                    msg.setContent(String.valueOf(prixVendeur));
//                msg.setContent(lastOffer[0]+ ":pour " + lastOffer[0] + ", " + i[0] + " fois...");
                    msg.addReceiver(topic);
                    send(msg);
                    println("::j'ai proposé ce nouveau prix : %.2f".formatted(prixVendeur));
                    i[0] = 0;
                    //TODO: stop other behaviours
//                decompteTemps.stop();
//                removeBehaviour(decompteTemps);
//                removeBehaviour(waitProposal);

                }
            }
        });
        return decompteTemps;
    }

    private Object initNegociation() {
        ACLMessage firstMsg = new ACLMessage(ACLMessage.PROPOSE);
        firstMsg.setConversationId("INIT-NEGO");
        firstMsg.addReceiver(topic);
        Random r = new Random();
        Object o = object.values()[r.nextInt(object.values().length)];
        prixVendeur = r.nextInt(200)+100;
        firstMsg.setContent(o+":"+prixVendeur);
        send(firstMsg);
        sold = false;
        return o;
    }

}
