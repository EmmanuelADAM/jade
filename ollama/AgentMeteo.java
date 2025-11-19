package ollama;


import jade.core.*;
import jade.core.behaviours.ReceiverBehaviour;
import jade.core.behaviours.WakerBehaviour;
import jade.gui.AgentWindowed;
import jade.gui.SimpleWindow4Agent;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import static java.lang.System.out;

/**
 * Agent class to allow exchange of messages between an agent named ping, that initiates the 'dialog', and an agent
 * named 'pong'
 *
 * @author emmanueladam
 */
public class AgentMeteo extends AgentWindowed {

    String laMeteo = "très chaud, 29 degrés C";
    /**
     * agent setup, adds its behaviours
     */
    @Override
    protected void setup() {
        window = new SimpleWindow4Agent(this);

        println(getLocalName() + " -> Hello, my address is " + getAID());
        // if the agent names "ping"
        // add a behaviour that will send the first "ball" msg in 10 sec. to "pong" agent
        long temps = 10;
        out.println(getLocalName() + " -> I start in" + temps + " ms");
        addBehaviour(new WakerBehaviour(this, temps) {
            protected void onWake() {
                laMeteo = getNatureTemperature("Belém");
                println("I have this information about the weather : " + laMeteo);
            }
        });

        var modele = MessageTemplate.and(
                MessageTemplate.MatchConversationId("METEO"),
                MessageTemplate.MatchPerformative(ACLMessage.REQUEST));
        // add a behaviour that wait for an eventual failure msg
        addBehaviour(new ReceiverBehaviour(this,  -1, modele,true, (a, msg) -> {
            var reply = msg.createReply();
            reply.setPerformative(ACLMessage.INFORM);
            reply.setContent(laMeteo);
            a.send(reply);
            println(" -> I send a msg to " + msg.getSender().getLocalName() + " with content: " + laMeteo);
        }
        ));
    }


     String getNatureTemperature(String town) {
        Meteo service = new Meteo();
        Meteo.WeatherData weather = service.getWeatherByCity(town);
        if (weather != null && weather.isValid()) {
            double temp = weather.getTemperature();
            if (temp < 0) {
                return "très froid";
            } else if (temp < 10) {
                return "froid";
            } else if (temp < 17) {
                return "tempéré";
            } else if (temp < 26) {
                return "chaud";
            } else if (temp < 35) {
                return "très chaud";
            } else {
                return "extrêmement chaud";
            }
        } else {
            return "données météo non disponibles";
        }
    }

    /**I inform the user when I leave the platform*/
    @Override
    protected void takeDown() {
        out.println(getLocalName() + " -> I leave the plateform ! ");
    }

}