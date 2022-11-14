package testComportement;

import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.SequentialBehaviour;
import jade.core.behaviours.WakerBehaviour;

import static java.lang.System.out;

/**
 * class for an agent having 1 sequential behavior that contains several behaviors acting 3 times
 *
 * @author emmanueladam
 */
public class AgentHelloEuropeenSequentiel extends Agent {
    /**
     * main function
     * launch 2 agents whom the behaviors act in sequence
     *
     */
    public static void main(String[] args) {
        String[] jadeArgs = new String[2];
        StringBuilder sbAgents = new StringBuilder();
        sbAgents.append("a1:testComportement.AgentHelloEuropeenSequentiel").append(";");
        sbAgents.append("a2:testComportement.AgentHelloEuropeenSequentiel").append(";");
        jadeArgs[0] = "-gui";
        jadeArgs[1] = sbAgents.toString();
        jade.Boot.main(jadeArgs);
    }

    /**
     * Initialisation de l'agent
     */
    @Override
    protected void setup() {
        out.printf("""
                I, Agent %s, my address is %s
                    I execute several behaviors in parallel%n""", getLocalName(), getAID());

        SequentialBehaviour seqB = new SequentialBehaviour();
        seqB.addSubBehaviour(europeanBehaviour("bonjour"));
        seqB.addSubBehaviour(europeanBehaviour("hallo"));
        seqB.addSubBehaviour(europeanBehaviour("buongiorno"));
        seqB.addSubBehaviour(europeanBehaviour("buenos dias"));
        seqB.addSubBehaviour(europeanBehaviour("Olá"));
        seqB.addSubBehaviour(europeanBehaviour("saluton"));

        // add a behavior that adds the parallel behavior in 100ms
        addBehaviour(new WakerBehaviour(this, 100, a->a.addBehaviour(seqB)));


    }

    /**
     * create a behavior that displays a message; this behavior can be executed 3 times
     * @param msg the msg to display
     * @return  a simple behavior that can be executed 3 times
     */
    private Behaviour europeanBehaviour(String msg) {
        Behaviour b = new Behaviour(this) {
            int i = 0;

            @Override
            public void action() {
                println("%s -> %s (%d/3)".formatted(getLocalName(), msg, (i+1)));
                i++;
            }

            @Override
            public boolean done() {
                return i >= 3;
            }
        };
        return b;
    }
}
