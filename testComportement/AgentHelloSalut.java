package testComportement;

import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.core.behaviours.WakerBehaviour;

import static java.lang.System.out;

/**
 * classe d'un agent qui contient 2 comportements sans fin, qui affichent l'un 'bonjour', l'autre 'salut'
 * l'agent possède aussi un comportement à allumage retardé qui le supprime de la plateforme
 *
 * @author emmanueladam
 */
public class AgentHelloSalut extends Agent {
    /**
     * procédure principale.
     * lance 2 agents qui agissent en "parallèle"
     */
    public static void main(String[] args) {
        String[] jadeArgs = new String[2];
        StringBuilder sbAgents = new StringBuilder();
        sbAgents.append("a1:testComportement.AgentHelloSalut").append(";");
        sbAgents.append("a2:testComportement.AgentHelloSalut").append(";");
        jadeArgs[0] = "-gui";
        jadeArgs[1] = sbAgents.toString();
        jade.Boot.main(jadeArgs);
    }

    /**
     * Initialisation de l'agent
     */
    @Override
    protected void setup() {
        out.println("Moi, Agent " + getLocalName() + ", mon  adresse est " + getAID());

        // ajout d'un comportement "éternel" qui, à chaque passage, affiche bonjour et fait une pause d'au plus 200 ms
        //(devrait être remplacé par un comportement cyclique, voir le comportement suivant)
        addBehaviour(new Behaviour(this) {
            public void action() {
                println("De l'agent " + getLocalName() + " : Bonjour à toutezétatousse");
                // attente au plus 200ms, ou moins si l'agent reçoit un message
                block(200);
            }

            public boolean done() {
                return false;
            }
        });

        // ajout d'un comportement cyclique qui, à chaque passage, affiche salut et fait une pause de 300 ms
        addBehaviour(new TickerBehaviour(this, 300, a->{println("De l'agent " + a.getLocalName() + " : Salut à " +
                "toutezétatousse");}));

        // ajout d'un comportement qui retire l'agent dans 1000 ms
        addBehaviour(new WakerBehaviour(this, 1000, a->{
            out.println("De l'agent " + a.getLocalName() + " : bon j'y vais...");
            a.doDelete();
        }));
    }

    // 'Nettoyage' de l'agent
    @Override
    protected void takeDown() {
        out.println("Moi, Agent " + getLocalName() + " je quitte la plateforme ! ");
    }
}
