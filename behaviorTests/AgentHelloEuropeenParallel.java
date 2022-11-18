package behaviorTests;

import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.ParallelBehaviour;
import jade.core.behaviours.WakerBehaviour;

import static java.lang.System.out;

/**
 * class for an agent having 1 parallel behavior that contains several behaviors acting 3 times
 * s'activant 3 fois
 *
 * @author emmanueladam
 */
public class AgentHelloEuropeenParallel extends Agent {
    /**
     * main function
     * launch 2 agents whom the behaviors act in parallel
     *
     */
    public static void main(String[] args) {
        String[] jadeArgs = new String[2];
        StringBuilder sbAgents = new StringBuilder();
        sbAgents.append("a1:behaviorTests.AgentHelloEuropeenParallel").append(";");
        sbAgents.append("a2:behaviorTests.AgentHelloEuropeenParallel").append(";");
        jadeArgs[0] = "-gui";
        jadeArgs[1] = sbAgents.toString();
        jade.Boot.main(jadeArgs);
    }

    /**
     * agent set-up
     */
    @Override
    protected void setup() {
        out.println("I, Agent " + getLocalName() + ", my address is " + getAID());
        out.println("I execute several behaviors in parallel");

        //create a parallel behavior
        ParallelBehaviour paraB = new ParallelBehaviour();
        paraB.addSubBehaviour(europeanBehaviour("bonjour"));
        paraB.addSubBehaviour(europeanBehaviour("hallo"));
        paraB.addSubBehaviour(europeanBehaviour("buongiorno"));
        paraB.addSubBehaviour(europeanBehaviour("buenos dias"));
        paraB.addSubBehaviour(europeanBehaviour("Olá"));
        paraB.addSubBehaviour(europeanBehaviour("saluton"));


        // add a behavior that adds the parallel behavior in 100ms
        addBehaviour(new WakerBehaviour(this, 100, a->a.addBehaviour(paraB)));

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
                printf("%s -> %s (%d/3)\n", new Object[]{getLocalName(), msg, (i+1)});
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
