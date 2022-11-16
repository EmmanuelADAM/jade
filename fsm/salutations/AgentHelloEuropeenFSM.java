package fsm.salutations;

import jade.core.behaviours.FSMBehaviour;
import jade.core.behaviours.WakerBehaviour;
import jade.gui.AgentWindowed;
import jade.gui.SimpleWindow4Agent;


/**
 * class for an agent that contains 1 behavior defined with a finite state machine (6 etats)
 * A <---\
 * /   \    \
 * B     C    \
 * / \    |    |
 * D   \   |    |
 * \   \ /     |
 * \-->E-----/
 * |
 * F
 *
 * @author emmanueladam
 * @since 2021-11-24
 */
public class AgentHelloEuropeenFSM extends AgentWindowed {

    /**
     * main function
     * start some agents (of type AgentHelloEuropeenFSM )
     */
    public static void main(String[] args) {
        String[] jadeArgs = new String[2];
        StringBuilder sbAgents = new StringBuilder();
        sbAgents.append("a1:fsm.salutations.AgentHelloEuropeenFSM").append(";");
        sbAgents.append("a2:fsm.salutations.AgentHelloEuropeenFSM").append(";");
        jadeArgs[0] = "-gui";
        jadeArgs[1] = sbAgents.toString();
        jade.Boot.main(jadeArgs);
    }

    /**
     * Initialisation de l'agent
     */
    @Override
    protected void setup() {
        window = new SimpleWindow4Agent(getAID().getName(), this);
        println("Hello! I'm ready, my address is " + this.getAID().getName());
        println("I execute the behaviors according to a finite state machine");

        //a Finite State Machine behavior
        FSMBehaviour fsm = new FSMBehaviour(this) {
            public int onEnd() {
                println("FSM behaviour ended, I leave");
                myAgent.doDelete();
                return super.onEnd();
            }
        };

        //____THE STATES
        //we store the states (ideally, names "A", "B", .. "F" should be in constants
        //INITIAL STATE (2 x bonjour)
        fsm.registerFirstState(new EuropeanBehaviour("bonjour", 2), "A");
        // other states
        fsm.registerState(new EuropeanBehaviour("hallo", 2), "B");
        fsm.registerState(new EuropeanBehaviour("buongiorno", 3), "C");
        fsm.registerState(new EuropeanBehaviour("buenos dias", 3), "D");
        fsm.registerState(new EuropeanBehaviour("Olá", 1), "E");
        //FINAL STATE (1 x saluton)
        fsm.registerLastState(new EuropeanBehaviour("saluton", 1), "F");

        // TRANSITIONS
        // from A, we go to B if the behavior linked to A returns 0 On End
        fsm.registerTransition("A", "B", 0);
        // from A, we go to C if the behavior linked to A returns 1 On End
        fsm.registerTransition("A", "C", 1);
        // from B, we go to D if the behavior linked to B returns 0 On End
        fsm.registerTransition("B", "D", 0);
        // from B, we go to E if the behavior linked to B returns 1 On End
        fsm.registerTransition("B", "E", 1);
        // after C, we go to E without any condition
        fsm.registerDefaultTransition("C", "E");
        // after D, we go to E without any condition
        fsm.registerDefaultTransition("D", "E");
        // from E, we go to F if the behavior linked to E returns 0 On End
        fsm.registerTransition("E", "F", 0);
        // from E, we go to A if the behavior linked to E returns 1 On End
        //then we indicate which behaviors have to be reset
        //if we do not "reset" them, the 'EuropeanBehaviors' will not reset the current number of cycles
        fsm.registerTransition("E", "A", 1, new String[]{"A", "B", "C", "D", "E", "F"});

        // add the fsm behavior in  100ms
        addBehaviour(new WakerBehaviour(this, 100, a-> a.addBehaviour(fsm)));
    }

}
