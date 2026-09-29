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
        // the content of the request is "meteo in <town>" (or only "<town>")
        addBehaviour(new ReceiverBehaviour(this,  -1, modele,true, (a, msg) -> {
            String town = msg.getContent().replaceFirst("^\\s*meteo in\\s+", "").trim();
            var reply = msg.createReply();
            String weather = getWeatherDescription(town);
            if (weather != null) {
                reply.setPerformative(ACLMessage.INFORM);
                reply.setContent(weather);
            } else {
                reply.setPerformative(ACLMessage.FAILURE);
                reply.setContent("données météo non disponibles pour " + town);
            }
            a.send(reply);
            println(" -> I send a msg to " + msg.getSender().getLocalName() + " with content: " + reply.getContent());
        }
        ));
    }

    /**
     * current weather of a town, in a short text for a LLM
     * ex. "chaud, 22,3°C (ressenti 21,8°C), ciel dégagé, humidité 60%, vent 12 km/h"
     * @return the description, or null if the town is unknown or the service is not available
     */
    String getWeatherDescription(String town) {
        Meteo.WeatherData weather = new Meteo().getWeatherByCity(town);
        if (weather == null || !weather.isValid()) return null;
        return "%s, %.1f°C (ressenti %.1f°C), %s, humidité %d%%, vent %.0f km/h".formatted(
                getNature(weather.getTemperature()), weather.getTemperature(), weather.getFeelsLike(),
                weather.getDescription(), weather.getHumidity(), weather.getWindSpeedKmh());
    }


     String getNatureTemperature(String town) {
        Meteo service = new Meteo();
        Meteo.WeatherData weather = service.getWeatherByCity(town);
        if (weather != null && weather.isValid()) {
            return getNature(weather.getTemperature());
        } else {
            return "données météo non disponibles";
        }
    }

    /** nature of a temperature (froid, tempéré, chaud...) */
    String getNature(double temp) {
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
    }

    /**I inform the user when I leave the platform*/
    @Override
    protected void takeDown() {
        out.println(getLocalName() + " -> I leave the plateform ! ");
    }

}