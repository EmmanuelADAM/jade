package protocoles.agents;


import protocoles.gui.SimpleWindow4Agent;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.AchieveREResponder;


//import jade.proto.AchieveREInitiator;



/**
 * agent qui attend un message à partir du protocole AchieveRE, prépare la réponse et la retourne
 * @author eadam
 */
@SuppressWarnings("serial")
public class AgentReceptionARE extends AgentWindowed{

    /**ajout du suivi de protocole AchieveRE*/
    protected void setup() {
        window = new SimpleWindow4Agent(getAID().getName(), this);
        window.println("Hello! Agent  " +  getLocalName() + " is ready, my address is " + this.getAID().getName());


        MessageTemplate model = MessageTemplate.MatchConversationId("123");
        AchieveREResponder init = new AchieveREResponder(this, model){
            //fonction déclenchée à la réception d'un message par le protocole correspondant
            protected ACLMessage handleRequest(ACLMessage request){
                window.println("recu  " + request.getContent() );
                ACLMessage answer = request.createReply();
                answer.setPerformative(ACLMessage.AGREE);
                return answer;
            }
            //fonction utilisée pour retourner un résultat sou forme de message
            //request = requete initiale, response = reponse que cet agent avait envoyé
            protected ACLMessage prepareResultNotification(ACLMessage request, ACLMessage response){
                String content = request.getContent();
                String[] terms = content.split(" ");
                String[] strValues = terms[1].split(",");
                int somme = 0;
                for(String strV:strValues)
                    somme += Integer.parseInt(strV);
                ACLMessage answer = request.createReply();
                answer.setPerformative(ACLMessage.INFORM);
                answer.setContent("resultat = " + somme );
                window.println("j envoie " + somme);
                return answer;
            }
        };

        addBehaviour(init);
    }
}
