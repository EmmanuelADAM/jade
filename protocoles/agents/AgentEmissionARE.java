package protocoles.agents;


import protocoles.gui.SimpleWindow4Agent;
import jade.core.AID;
import jade.core.behaviours.WakerBehaviour;
import jade.lang.acl.ACLMessage;
import jade.proto.AchieveREInitiator;

/**
 * classe d'un agent qui soumet une requête de somme à un autre agent et gère l'échange par le protocole AchieveRE
 * @author eadam
 */
public class AgentEmissionARE extends AgentWindowed{

    /**ajout du suivi de protocole AchieveRE*/
    protected void setup() {
        window = new SimpleWindow4Agent(getAID().getName(), this);
        window.println("Hello! Agent  " +  getLocalName() + " is ready, my address is " + this.getAID().getName());


        ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
        msg.setConversationId("123");
        msg.setContent("sum 4,5,6");
        msg.addReceiver(new AID("b", false));
        msg.addReceiver(new AID("c", false));
        msg.addReceiver(new AID("d", false));

        window.println("j'envoie une requete sur " + msg.getContent());
        AchieveREInitiator init = new AchieveREInitiator(this, msg){
            //fonction lancée dès accord
            protected void handleAgree(ACLMessage agree){
                window.println("recu un accord de " + agree.getSender().getLocalName());
            }
            //fonction lancée dès reception information
            protected void handleInform(ACLMessage inform){
                window.println("recu  de " + inform.getSender().getLocalName() +
                        ", ce resultat " + inform.getContent());
            }
            //fonction lancée dès que toutes les réponses ont été reçues
            protected void handleAllResultNotifications(java.util.Vector responses)
            {
                window.println("c'est bon, j'ai toutes les réponses. Pour rappel : ");
                for(int i=0; i<responses.size(); i++)
                {
                    var msg = (ACLMessage)responses.get(i);
                    window.println("---de " + msg.getSender().getLocalName() + " : " + msg.getContent());
                }
            }
        };

        addBehaviour(new WakerBehaviour(this, 0) {
            public void  onWake() {addBehaviour(init);}});
    }
}
