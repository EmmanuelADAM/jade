package testComportement;

import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.SequentialBehaviour;
import jade.core.behaviours.WakerBehaviour;

import static java.lang.System.out;

/**
 * classe d'un agent qui contient 1 comportement séquentiel contenant plusieurs comportements s'activant 3 fois
 *
 * @author emmanueladam
 */
public class AgentHelloEuropeenSequentiel extends Agent {
    /**
     * procédure principale.
     * lance 2 agents dont les comportements s'exécutent en sequence
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
        out.println("Moi, Agent " + getLocalName() + ", mon  adresse est " + getAID());
        out.println("J'exécute des comportements en séquentiel");

        SequentialBehaviour seqB = new SequentialBehaviour();
        seqB.addSubBehaviour(europeanBehaviour("bonjour"));
        seqB.addSubBehaviour(europeanBehaviour("hallo"));
        seqB.addSubBehaviour(europeanBehaviour("buongiorno"));
        seqB.addSubBehaviour(europeanBehaviour("buenos dias"));
        seqB.addSubBehaviour(europeanBehaviour("Olá"));
        seqB.addSubBehaviour(europeanBehaviour("saluton"));

        // ajout d'un comportement qui ajoute le comportement seuentiel dans 100ms
        addBehaviour(new WakerBehaviour(this, 100, a->a.addBehaviour(seqB)));


    }

    /**
     * @return un comportement qui affiche le message transmis, 3 fois
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
