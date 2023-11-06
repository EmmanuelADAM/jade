package td.englishAuctionOpenCry.agents;

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
public class AuctioneerAgent extends AgentWindowed {
    enum object{book, watch, mouse};
    /**
     * address of the radio topic
     */
    AID topic;
    /**
     * no of the sent msg
     */
    int i = 0;

    protected void setup() {
        window = new SimpleWindow4Agent(getAID().getName(), this);
        println("Hello! I'm ready, my address is " + this.getAID().getName());
        println("click to the button to launch an auction");
        window.setButtonActivated(true);
        window.setBackgroundTextColor(Color.ORANGE);
        //Create a "radio channel" with the name 'BestAgentsCharts'
        topic = AgentServicesTools.generateTopicAID(this, "Auction");
    }

    /**
     * reaction to a gui event
     */
    protected void onGuiEvent(GuiEvent ev) {
        if (ev.getType() == SimpleWindow4Agent.OK_EVENT) {
            startAuction();
        }
    }

    /**
     * send messages on the "radio channel"
     */
    private void startAuction() {
        //START AUCTION
        ACLMessage firstMsg = new ACLMessage(ACLMessage.INFORM);
        firstMsg.addReceiver(topic);
        Random r = new Random();
        Object o = object.values()[r.nextInt(object.values().length)];
        int startPrice = r.nextInt(100)+30;
        firstMsg.setContent(startPrice+":"+o);
        send(firstMsg);
        println("I launched an auction with a starting price of "+startPrice + " for " + o);
        LocalTime[] endTime = {LocalTime.now().plusSeconds(3)};
        AID[] bestBidder = {null};
        int[] bestOffer = {0};
        int[]i = {0};
        //WAIT 4 BID
        TickerBehaviour decompteTemps = new TickerBehaviour(this, 3000, agent -> {
            i[0]++;
            if(bestOffer[0]>0 && i[0]<4) {
                ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
                msg.setContent(bestOffer[0]+ ":pour " + bestOffer[0] + ", " + i[0] + " fois...");
                msg.addReceiver(topic);
                send(msg);
                println("::pour " + bestOffer[0] + ", " + i[0] + " fois...");
            }
        });
        addBehaviour(decompteTemps);


        var model = MessageTemplate.and(MessageTemplate.MatchTopic(topic), MessageTemplate.MatchPerformative(ACLMessage.PROPOSE));
        Behaviour waitProposal = new ReceiverBehaviour(this, -1, model, true,(a,msg)->{
            bestOffer[0] = Integer.parseInt(msg.getContent());
            bestBidder[0] = msg.getSender();
            println("received \"%d\" on the topic channel '%s', sent by %s".
                    formatted(bestOffer[0], topic.getLocalName(), msg.getSender().getLocalName()));
            endTime[0] = LocalTime.now().plusSeconds(3);
            i[0] = 0;
            decompteTemps.restart();
        });
        addBehaviour(waitProposal);

        Behaviour verifieFin = new Behaviour(this){
            boolean done = false;
            @Override
            public void action() {
                if (bestOffer[0] > 0 && i[0] > 3) {
                    done = true;
                    ACLMessage msg = new ACLMessage(ACLMessage.CONFIRM);
                    msg.setContent("ADJUGE VENDU pour " + bestOffer[0] + " à " + bestBidder[0].getLocalName() + "...");
                    msg.addReceiver(topic);
                    send(msg);
                    println("ADJUGE VENDU pour " + bestOffer[0] + " à " + bestBidder[0].getLocalName() + "...");
                }
            }
            @Override public boolean done(){
                return done;
            }

            @Override
            public int onEnd() {
                decompteTemps.stop();
                removeBehaviour(decompteTemps);
                removeBehaviour(waitProposal);
                return 0;
            }
        };
        addBehaviour(verifieFin);

    }

}
